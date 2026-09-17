#!/usr/bin/env python3
"""Convert a Fabric mod JAR into an inspectable Eagler .fmod.zip package.

This packages Fabric bytecode and resources; it does not make Fabric APIs
available to Eaglercraft. A compatible runtime adapter is still required to
execute the resulting .fbm payloads.
"""
import argparse
import json
import zipfile
from pathlib import PurePosixPath

MAX_ENTRY_SIZE = 16 * 1024 * 1024
MAX_TOTAL_SIZE = 128 * 1024 * 1024


def safe_name(name):
    path = PurePosixPath(name)
    return not path.is_absolute() and all(part not in ("", ".", "..") for part in path.parts)


def read_entry(archive, info):
    if info.file_size > MAX_ENTRY_SIZE:
        raise ValueError(f"archive entry is too large: {info.filename}")
    data = archive.read(info)
    if len(data) != info.file_size:
        raise ValueError(f"archive entry size changed while reading: {info.filename}")
    return data


def normalize_metadata(manifest, source_name):
    mod_id = str(manifest.get("id") or manifest.get("name") or manifest.get("title") or "unnamed-mod").strip()
    if not mod_id or not mod_id.replace(".", "").replace("-", "").replace("_", "").isalnum() or "/" in mod_id:
        raise ValueError("fabric.mod.json contains an invalid id")
    title = str(manifest.get("name") or manifest.get("title") or manifest.get("displayName") or mod_id).strip()
    description = str(manifest.get("description") or "").replace("\r", " ").replace("\n", " ")
    version = str(manifest.get("version") or "1.0.0").strip()
    return {
        "id": mod_id,
        "name": title,
        "title": title,
        "description": description,
        "version": version,
        "source": source_name,
        "format": "raw-fabric",
    }


def convert(source, destination):
    with zipfile.ZipFile(source) as fabric:
        manifest = json.loads(read_entry(fabric, fabric.getinfo("fabric.mod.json")))
        metadata = normalize_metadata(manifest, "fabric")
        with zipfile.ZipFile(destination, "w", zipfile.ZIP_DEFLATED) as output:
            output.writestr("mod.fbt", "".join(
                f"{key}={str(value).replace(chr(10), ' ').replace(chr(13), ' ')}\n"
                for key, value in metadata.items()
            ))
            total_size = 0
            payload_count = 0
            for info in fabric.infolist():
                if info.is_dir() or not safe_name(info.filename) or info.filename == "fabric.mod.json":
                    continue
                data = read_entry(fabric, info)
                total_size += len(data)
                if total_size > MAX_TOTAL_SIZE:
                    raise ValueError("Fabric mod payload is too large")
                output.writestr(f"payload/{info.filename}.fbm", data)
                payload_count += 1
            if payload_count == 0:
                raise ValueError("Fabric mod archive contains no payload entries")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("fabric_jar")
    parser.add_argument("output")
    args = parser.parse_args()
    convert(args.fabric_jar, args.output)


if __name__ == "__main__":
    main()
