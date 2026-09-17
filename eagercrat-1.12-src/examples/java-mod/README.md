# Java EBC mod example

This example uses the supported Java mod API and is translated into executable
EBC by the shell compiler.

From `eagercrat-1.12-src`:

```sh
tools/compile_java_mod.sh \
  examples/java-mod/src \
  examples/java-mod/example-mod.fmod.zip \
  example-mod \
  "Example Mod" \
  1.0.0 \
  "A Java mod translated to EBC"
```

The output contains:

```text
mod.fbt
payload/main.ebc.fbm
```

Supported calls are `FMod.log("message")`,
`FMod.registerCommand("name")`, `FMod.onTick()`, and the lifecycle hooks
`FMod.onLoad()` / `FMod.onUnload()`. The older `EaglerMod` alias remains
supported for compatibility.
