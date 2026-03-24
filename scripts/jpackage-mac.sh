#!/bin/bash

#
# Copyright (c) 2026 unknowIfGuestInDream.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in the
# documentation and/or other materials provided with the distribution.
#     * Neither the name of unknowIfGuestInDream, any associated website, nor the
# names of its contributors may be used to endorse or promote products
# derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL UNKNOWIFGUESTINDREAM BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

# Create a compact native application image using jpackage on macOS.
#
# Uses jpackage (--type app-image) to bundle the application JARs and a minimal
# JRE into a native macOS .app bundle with a launcher.
# The resulting app-image is then compressed into a distributable zip.
#
# This script should be called from the project root directory after
# mvn clean package -DskipTests.

set -e
stepStart=$(date +%s)

# Detect architecture
arch=$(uname -m)
if [ "$arch" = "arm64" ] || [ "$arch" = "aarch64" ]; then
  archLabel="aarch64"
else
  archLabel="x64"
fi

echo "========================================"
echo " JVM Explorer - Package App Image (macOS $archLabel)"
echo " Started: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"

# Step 1: Prerequisite checks
echo ""
echo "[1/4] Checking prerequisites..."

if ! command -v jpackage &> /dev/null; then
  echo "jpackage not found on PATH." >&2
  echo "jpackage is included with JDK 14 and later. Make sure JDK 21 is installed and JAVA_HOME/bin is on PATH." >&2
  exit 1
fi
echo "  jpackage: found"

# Step 2: Resolve version and jar
echo ""
echo "[2/4] Resolving version and jar..."

version="${APP_VERSION}"
if [ -z "$version" ]; then
  echo "  APP_VERSION not set, extracting from Maven..."
  version=$(mvn -q -DforceStdout 'help:evaluate' -Dexpression=project.version 2>/dev/null | tr -d '\r')
  if [ -z "$version" ]; then
    echo "APP_VERSION not set and failed to extract version from Maven" >&2
    exit 1
  fi
fi
echo "  Version: $version"

jarName="explorer.jar"
jarPath="explorer/target/$jarName"
if [ ! -f "$jarPath" ]; then
  echo "Jar not found: $jarPath" >&2
  exit 1
fi
jarSize=$(stat -f%z "$jarPath" 2>/dev/null || stat -c%s "$jarPath")
echo "  Jar: $jarPath ($((jarSize / 1048576)) MB)"

# Step 3: Run jpackage --type app-image
echo ""
echo "[3/4] Running jpackage (app-image)..."

inputDir="jpackage-input"
rm -rf "$inputDir"
mkdir -p "$inputDir/lib" "$inputDir/agent"

cp "$jarPath" "$inputDir/"
cp explorer/target/lib/*.jar "$inputDir/lib/"
cp agent/target/agent.jar "$inputDir/agent/"
cp launch-agent/target/launch-agent.jar "$inputDir/agent/"
echo "  Input dir: $inputDir"

mkdir -p dist
appImageDir="app-image-out"
rm -rf "$appImageDir"

modules="java.se,jdk.attach,jdk.compiler,jdk.unsupported,jdk.zipfs,jdk.management,jdk.crypto.ec,jdk.localedata,jdk.charsets"

jpackage \
  --input "$inputDir" \
  --name "JVM-Explorer" \
  --main-jar "$jarName" \
  --main-class "com.tlcsdm.jvmexplorer.Startup" \
  --type app-image \
  --java-options "-Xms64m" \
  --java-options "-Xmx512m" \
  --java-options "--add-modules=jdk.attach" \
  --java-options "-Dfile.encoding=UTF-8" \
  --add-modules "$modules" \
  --jlink-options "--strip-debug --no-man-pages --no-header-files --compress zip-6" \
  --app-version "$version" \
  --vendor "Tlcsdm" \
  --description "A JavaFX-based tool to explore and debug running JVM processes" \
  --dest "$appImageDir"

if [ $? -ne 0 ]; then
  echo "jpackage failed" >&2
  exit 1
fi

# On macOS jpackage creates a .app bundle
appDir=$(find "$appImageDir" -maxdepth 1 -mindepth 1 \( -type d -name "*.app" -o -type d \) | head -1)
if [ -z "$appDir" ]; then
  echo "No output directory found after jpackage" >&2
  exit 1
fi
echo "  App image: $appDir"

appSize=$(find "$appDir" -type f -print0 | xargs -0 stat -f%z 2>/dev/null | awk '{s+=$1}END{print s}' || find "$appDir" -type f -exec stat -c%s {} + | awk '{s+=$1}END{print s}')
echo "  App image size: $((appSize / 1048576)) MB"

# Step 4: Zip and cleanup
echo ""
echo "[4/4] Packaging and cleaning up..."

zipName="jvm-explorer-mac-${archLabel}-${version}.zip"
rm -f "dist/$zipName"

cd "$appImageDir"
zip -r -y "../dist/$zipName" .
cd ..

zipSize=$(stat -f%z "dist/$zipName" 2>/dev/null || stat -c%s "dist/$zipName")
echo "  Zip: dist/$zipName ($((zipSize / 1048576)) MB)"

rm -rf "$inputDir" "$appImageDir"
echo "  Cleaned up temp directories"

stepEnd=$(date +%s)
elapsed=$((stepEnd - stepStart))
printf "\nApp image packaged successfully. (%02d:%02d elapsed)\n" $((elapsed / 60)) $((elapsed % 60))
