#!/bin/sh
set -eu

for candidate in \
    "/usr/local/sdkman/candidates/java/21.0.10-ms" \
    "/usr/local/sdkman/candidates/java/21.0.9-ms" \
    "/usr/lib/jvm/java-21-openjdk-amd64" \
    "/usr/lib/jvm/msopenjdk-21-amd64"; do
    if [ -x "$candidate/bin/java" ]; then
        export JAVA_HOME="$candidate"
        export PATH="$JAVA_HOME/bin:$PATH"
        break
    fi
done

chmod +x gradlew
./gradlew generateWasmGC
