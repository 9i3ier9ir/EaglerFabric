#!/usr/bin/env bash
set -euo pipefail

usage() {
  printf 'Usage: %s SOURCE_DIR OUTPUT.fmod.zip MOD_ID TITLE [VERSION] [DESCRIPTION]\n' "$0" >&2
  printf 'Compiles supported EaglerMod Java sources, translates them to EBC, and packages an executable .fbm payload.\n' >&2
  exit 2
}

[[ $# -ge 4 && $# -le 6 ]] || usage
source_dir=$1
output=$2
mod_id=$3
title=$4
version=${5:-1.0.0}
description=${6:-Compiled Java mod}

[[ -d "$source_dir" ]] || { printf 'Source directory not found: %s\n' "$source_dir" >&2; exit 1; }
[[ "$mod_id" =~ ^[a-z0-9][a-z0-9._-]{0,63}$ ]] || { printf 'Invalid mod ID: %s\n' "$mod_id" >&2; exit 1; }
command -v javac >/dev/null || { printf 'javac is required\n' >&2; exit 1; }
command -v zip >/dev/null || { printf 'zip is required\n' >&2; exit 1; }
command -v java >/dev/null || { printf 'java is required\n' >&2; exit 1; }

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
output_dir=$(CDPATH= cd -- "$(dirname -- "$output")" && pwd)
output_file=$output_dir/$(basename -- "$output")
temp_dir=$(mktemp -d)
trap 'rm -rf "$temp_dir"' EXIT

find "$source_dir" -type f -name '*.java' -print0 > "$temp_dir/sources.list"
[[ -s "$temp_dir/sources.list" ]] || { printf 'No Java sources found in %s\n' "$source_dir" >&2; exit 1; }
tr '\0' '\n' < "$temp_dir/sources.list" > "$temp_dir/sources.args"
mkdir -p "$temp_dir/classes"

# Compile against the small source-level API used by the translator. The stubs
# are removed before packaging; the EBC payload is the executable artifact.
mkdir -p "$temp_dir/api/net/lax1dude/eaglercraft/mod"
cat > "$temp_dir/api/net/lax1dude/eaglercraft/mod/EaglerMod.java" <<'JAVA'
package net.lax1dude.eaglercraft.mod;
public final class EaglerMod {
  public static void log(String message) {}
  public static void registerCommand(String name) {}
  public static void registerKeybind(String keyName, int defaultKeyCode) {}
  public static void onKeyPressed(String keyName, int keyCode, boolean pressed) {}
  public static void openScreen(String screenName) {}
  public static void onTick() {}
  public static void onLoad() {}
  public static void onUnload() {}
  public static void registerEvent(String event) {}
  public static void setConfig(String key, String value) {}
  public static void registerConfig(String key, String defaultValue) {}
  public static String getConfig(String key, String defaultValue) { return defaultValue; }
}
JAVA
cat > "$temp_dir/api/net/lax1dude/eaglercraft/mod/FMod.java" <<'JAVA'
package net.lax1dude.eaglercraft.mod;
public final class FMod {
  public static void log(String message) { EaglerMod.log(message); }
  public static void registerCommand(String name) { EaglerMod.registerCommand(name); }
  public static void registerKeybind(String keyName, int defaultKeyCode) { EaglerMod.registerKeybind(keyName, defaultKeyCode); }
  public static void onKeyPressed(String keyName, int keyCode, boolean pressed) { EaglerMod.onKeyPressed(keyName, keyCode, pressed); }
  public static void openScreen(String screenName) { EaglerMod.openScreen(screenName); }
  public static void onTick() { EaglerMod.onTick(); }
  public static void onLoad() { EaglerMod.onLoad(); }
  public static void onUnload() { EaglerMod.onUnload(); }
  public static void registerEvent(String event) { EaglerMod.registerEvent(event); }
  public static void setConfig(String key, String value) { EaglerMod.setConfig(key, value); }
  public static void registerConfig(String key, String defaultValue) { EaglerMod.registerConfig(key, defaultValue); }
  public static String getConfig(String key, String defaultValue) { return EaglerMod.getConfig(key, defaultValue); }
}
JAVA
javac -d "$temp_dir/classes" \
  "$temp_dir/api/net/lax1dude/eaglercraft/mod/EaglerMod.java" \
  "$temp_dir/api/net/lax1dude/eaglercraft/mod/FMod.java" \
  @"$temp_dir/sources.args"

cat > "$temp_dir/JavaToEbc.java" <<'JAVA'
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.*;

public class JavaToEbc {
  private static final Pattern LOG = Pattern.compile("(?:EaglerMod|FMod)\\.log\\s*\\(\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"\\s*\\)\\s*;");
  private static final Pattern COMMAND = Pattern.compile("(?:EaglerMod|FMod)\\.registerCommand\\s*\\(\\s*\\\"([a-z0-9._-]+)\\\"\\s*\\)\\s*;");
  private static final Pattern REGISTER_KEYBIND = Pattern.compile("(?:EaglerMod|FMod)\\.registerKeybind\\s*\\(\\s*\\\"([a-z0-9._-]+)\\\"\\s*,\\s*(\\-?\\d+)\\s*\\)\\s*;");
  private static final Pattern ON_KEY_PRESSED = Pattern.compile("(?:EaglerMod|FMod)\\.onKeyPressed\\s*\\(\\s*\\\"([a-z0-9._-]+)\\\"\\s*,\\s*(\\-?\\d+)\\s*,\\s*(true|false)\\s*\\)\\s*;");
  private static final Pattern OPEN_SCREEN = Pattern.compile("(?:EaglerMod|FMod)\\.openScreen\\s*\\(\\s*\\\"([a-z0-9._-]+)\\\"\\s*\\)\\s*;");
  private static final Pattern REGISTER_CONFIG = Pattern.compile("(?:EaglerMod|FMod)\\.registerConfig\\s*\\(\\s*\\\"([a-z0-9._-]+)\\\"\\s*,\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"\\s*\\)\\s*;");
  private static final Pattern TICK = Pattern.compile("(?:EaglerMod|FMod)\\.onTick\\s*\\(\\s*\\)\\s*;");
  private static final Pattern LOAD = Pattern.compile("(?:EaglerMod|FMod)\\.onLoad\\s*\\(\\s*\\)\\s*;");
  private static final Pattern UNLOAD = Pattern.compile("(?:EaglerMod|FMod)\\.onUnload\\s*\\(\\s*\\)\\s*;");
  private static final Pattern EVENT = Pattern.compile("(?:EaglerMod|FMod)\\.registerEvent\\s*\\(\\s*\\\"([a-z0-9._-]+)\\\"\\s*\\)\\s*;");
  private static final Pattern SET_CONFIG = Pattern.compile("(?:EaglerMod|FMod)\\.setConfig\\s*\\(\\s*\\\"([a-z0-9._-]+)\\\"\\s*,\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"\\s*\\)\\s*;");
  private static final Pattern CALL = Pattern.compile("(?:EaglerMod|FMod)\\.(?:log|registerCommand|registerKeybind|onKeyPressed|openScreen|registerConfig|onTick|onLoad|onUnload|registerEvent|setConfig)\\s*\\(");
  private static final int MAX_STRING = 4096;
  private static final ByteArrayOutputStream out = new ByteArrayOutputStream();
  private static void string(String value) throws Exception {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    if (bytes.length > MAX_STRING) throw new Exception("EBC string is too long");
    out.write(bytes.length >>> 8); out.write(bytes.length & 255); out.write(bytes);
  }
  private static String unescape(String value) {
    return value.replace("\\\\\"", "\"").replace("\\\\\\\\", "\\").replace("\\\\n", " ");
  }
  public static void main(String[] args) throws Exception {
    out.write(new byte[] { 'E', 'B', 'C', '1', 1 });
    boolean found = false;
    try (var paths = Files.walk(Paths.get(args[0]))) {
      for (Path path : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".java"))::iterator) {
        String source = Files.readString(path);
        Matcher calls = CALL.matcher(source);
        while (calls.find()) {
          String rest = source.substring(calls.start());
          Matcher log = LOG.matcher(rest), command = COMMAND.matcher(rest), tick = TICK.matcher(rest), load = LOAD.matcher(rest), unload = UNLOAD.matcher(rest), event = EVENT.matcher(rest), setConfig = SET_CONFIG.matcher(rest);
          if (log.lookingAt()) { out.write(1); string(unescape(log.group(1))); found = true; }
          else if (command.lookingAt()) { out.write(2); string(command.group(1)); found = true; }
          else if (registerKeybind.lookingAt()) { out.write(8); string(registerKeybind.group(1)); int value = Integer.parseInt(registerKeybind.group(2)); out.write((byte) (value >>> 24)); out.write((byte) ((value >>> 16) & 255)); out.write((byte) ((value >>> 8) & 255)); out.write((byte) (value & 255)); found = true; }
          else if (onKeyPressed.lookingAt()) { out.write(9); string(onKeyPressed.group(1)); int value = Integer.parseInt(onKeyPressed.group(2)); out.write((byte) (value >>> 24)); out.write((byte) ((value >>> 16) & 255)); out.write((byte) ((value >>> 8) & 255)); out.write((byte) (value & 255)); out.write(Boolean.parseBoolean(onKeyPressed.group(3)) ? 1 : 0); found = true; }
          else if (openScreen.lookingAt()) { out.write(10); string(openScreen.group(1)); found = true; }
          else if (registerConfig.lookingAt()) { out.write(11); string(registerConfig.group(1)); string(unescape(registerConfig.group(2))); found = true; }
          else if (tick.lookingAt()) { out.write(3); found = true; }
          else if (load.lookingAt()) { out.write(4); found = true; }
          else if (unload.lookingAt()) { out.write(5); found = true; }
          else if (event.lookingAt()) { out.write(6); string(event.group(1)); found = true; }
          else if (setConfig.lookingAt()) { out.write(7); string(setConfig.group(1)); string(unescape(setConfig.group(2))); found = true; }
          else throw new Exception("unsupported mod call in " + path);
        }
      }
    }
    if (!found) throw new Exception("no supported mod calls found");
    out.write(0);
    Files.write(Paths.get(args[1]), out.toByteArray());
  }
}
JAVA
javac -d "$temp_dir" "$temp_dir/JavaToEbc.java"
java -cp "$temp_dir" JavaToEbc "$source_dir" "$temp_dir/main.ebc"

mkdir -p "$temp_dir/package/payload"
printf 'id=%s\ntitle=%s\ndescription=%s\nversion=%s\nsource=java\nformat=ebc\n' \
  "${mod_id//$'\n'/ }" "${title//$'\n'/ }" "${description//$'\n'/ }" "${version//$'\n'/ }" > "$temp_dir/package/mod.fbt"

cp -- "$temp_dir/main.ebc" "$temp_dir/package/payload/main.ebc.fbm"

mkdir -p "$output_dir"
rm -f -- "$output_file"
(cd "$temp_dir/package" && zip -q -r "$output_file" .)
printf 'Created %s\n' "$output_file"
printf 'Translated supported EaglerMod calls into executable EBC.\n'
