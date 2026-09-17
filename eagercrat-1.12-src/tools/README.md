# Eagler mod packages

A mod package is a ZIP file (normally named `.fmod.zip`) containing:

- `mod.fbt`: UTF-8 `key=value` metadata. Required keys are `id` and `title`; optional keys are `description`, `version`, and `source`.
- One or more `.fbm` files: opaque payloads. The converter stores each Fabric JAR class/resource as a separate `.fbm` entry.

`ModPackageReader` validates package paths, metadata, duplicate payloads, and bounded entry/total sizes before exposing the package to a future platform-specific loader.

The shared Java API is `net.lax1dude.eaglercraft.mod.ModPackageReader`. Use it to
read an imported byte array or stream, then add the result to
`ModPackageRegistry` for a menu or storage adapter. The registry is currently
in-memory; platform storage and a bytecode/runtime adapter still need to be
implemented before `.fbm` payloads can execute. `ModLoader` is the extension
point for that adapter; the menu reports `loader unavailable` when none is
registered.

## Fabric conversion

```sh
python3 tools/fabric_to_fmod.py path/to/mod.jar path/to/mod.fmod.zip
```

For a no-install browser converter, open `tools/fabric_to_fmod.html` directly,
drop in a Fabric `.jar`, and choose **Convert and download**. It uses JSZip from
jsDelivr, so the page needs network access when opened. If the input already
contains an `.ebc` payload, it is emitted as executable `.ebc.fbm`; ordinary
Fabric `.class` files are preserved but labeled `raw-fabric` because they cannot
run in Eaglercraft without translation. The Python converter is the offline
alternative.

The same page can create an executable EBC package directly: choose **EBC mod**,
fill in the metadata and instructions, then choose **Create and download**.
This produces `payload/main.ebc.fbm` and accepts `log message`, `command name`,
`tick`, comments beginning with `#`, and a final `end` instruction.

To translate Java in the browser, choose **Java source ZIP**. The ZIP must
contain `fabric.mod.json` and one or more `.java` files, for example:

```text
my-mod-source.zip
├── fabric.mod.json
└── src/example/ExampleMod.java
```

The page translates supported `EaglerMod` calls and downloads an executable
`format=ebc` package containing `payload/main.ebc.fbm`.

## Compile Java sources into EBC

To compile supported Java mod sources, translate them into EBC, and package an
executable `.fmod.zip`:

```sh
tools/compile_java_mod.sh path/to/src output.fmod.zip my-mod "My Mod" 1.0.0 "Description"
```

The script requires `bash`, `java`, `javac`, and `zip`. It compiles the Java
sources for syntax checking, translates supported `EaglerMod` calls, and writes
`payload/main.ebc.fbm`. See `examples/java-mod` for a complete source example.

## Executable EBC mods

Executable packages use an `.ebc.fbm` payload. Create one with the assembler:

```sh
printf 'log Hello from my mod\ncommand greet\ntick\nend\n' > mod.ebc.txt
python3 tools/ebc_assemble.py mod.ebc.txt main.ebc
```

Place `main.ebc` in `payload/main.ebc.fbm` alongside `mod.fbt`. The built-in
loader executes `log`, `command`, and `tick` instructions through the safe
Eagler mod host. Fabric `.class` files are still not executable EBC payloads;
the Fabric converter preserves them for inspection and must later gain a real
Fabric-to-EBC compiler for automatic conversion.
