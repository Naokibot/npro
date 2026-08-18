#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
API_JAR="${1:-}"

if [[ -z "$API_JAR" || ! -f "$API_JAR" ]]; then
  echo "usage: scripts/build-release.sh <spigot-api-1.21.1.jar>" >&2
  exit 2
fi

BUILD="$ROOT/build/release"
BASE="$ROOT/base/Npro-1.1.0.jar"
OUTPUT="$ROOT/build/libs/Npro-1.2.0-Spigot-1.21.1.jar"
EXPORT="java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED"

rm -rf "$BUILD" "$ROOT/build/libs"
mkdir -p \
  "$BUILD/classes" \
  "$BUILD/patcher" \
  "$BUILD/base" \
  "$BUILD/patched/com/sagakenichi/npro" \
  "$ROOT/build/libs"

javac -source 21 -target 21 \
  --add-exports "$EXPORT" \
  -d "$BUILD/patcher" \
  "$ROOT/tools/PatchNpro.java"

(
  cd "$BUILD/base"
  jar xf "$BASE" com/sagakenichi/npro/NproPlugin.class
)

java --add-exports "$EXPORT" \
  -cp "$BUILD/patcher" \
  PatchNpro \
  "$BUILD/base/com/sagakenichi/npro/NproPlugin.class" \
  "$BUILD/patched/com/sagakenichi/npro/NproPlugin.class"

javac --release 21 \
  -cp "$API_JAR:$BUILD/patched:$BASE" \
  -d "$BUILD/classes" \
  "$ROOT/src/main/java/com/sagakenichi/npro/DailyLoginRewardListener.java"

cp "$BASE" "$OUTPUT"
jar uf "$OUTPUT" \
  -C "$BUILD/patched" com/sagakenichi/npro/NproPlugin.class \
  -C "$BUILD/classes" com/sagakenichi/npro/DailyLoginRewardListener.class \
  -C "$ROOT/src/main/resources" plugin.yml \
  -C "$ROOT/src/main/resources" config.yml

unzip -t "$OUTPUT" >/dev/null
printf '%s\n' "$OUTPUT"
