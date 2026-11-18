#!/usr/bin/env sh
set -eu

GRADLE_VERSION="9.2.1"
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
GRADLE_HOME="$GRADLE_USER_HOME/deadly-bootstrap/gradle-$GRADLE_VERSION"
ZIP_FILE="$GRADLE_USER_HOME/deadly-bootstrap/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
    mkdir -p "$GRADLE_USER_HOME/deadly-bootstrap"
    if [ ! -f "$ZIP_FILE" ]; then
        if command -v curl >/dev/null 2>&1; then
            curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP_FILE"
        elif command -v wget >/dev/null 2>&1; then
            wget "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -O "$ZIP_FILE"
        else
            echo "Gradle is not installed and neither curl nor wget is available." >&2
            exit 1
        fi
    fi
    command -v unzip >/dev/null 2>&1 || {
        echo "The unzip command is required to bootstrap Gradle." >&2
        exit 1
    }
    unzip -q -o "$ZIP_FILE" -d "$GRADLE_USER_HOME/deadly-bootstrap"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
