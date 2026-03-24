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

<#
.SYNOPSIS
    Create a compact native application image using jpackage on Windows.

.DESCRIPTION
    Uses jpackage (--type app-image) to bundle the application JARs and a minimal
    JRE into a native Windows application with an exe launcher.
    The resulting app-image directory is then compressed into a distributable zip.

    This script should be called from the project root directory after
    mvn clean package -DskipTests.
#>

$ErrorActionPreference = 'Stop'
$stepStart = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " JVM Explorer - Package App Image (Windows)" -ForegroundColor Cyan
Write-Host " Started: $($stepStart.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Step 1: Prerequisite checks
Write-Host "`n[1/4] Checking prerequisites..." -ForegroundColor Cyan

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw @"
jpackage not found on PATH.
jpackage is included with JDK 14 and later. Make sure JDK 21 is installed and JAVA_HOME/bin is on PATH.
"@
}
Write-Host "  jpackage: found" -ForegroundColor Gray

# Step 2: Resolve version and jar
Write-Host "`n[2/4] Resolving version and jar..." -ForegroundColor Cyan
$version = $env:APP_VERSION
if (-not $version) {
    Write-Host "  APP_VERSION not set, extracting from Maven..." -ForegroundColor Yellow
    $version = & mvn -q -DforceStdout 'help:evaluate' -Dexpression='project.version' 2>$null
    $version = $version.Trim()
    if (-not $version) { throw 'APP_VERSION not set and failed to extract version from Maven' }
}
Write-Host "  Version: $version" -ForegroundColor Gray

$jarName = 'explorer.jar'
$jarPath = "explorer\target\$jarName"
if (-not (Test-Path $jarPath)) { throw "Jar not found: $jarPath" }
$jarSize = (Get-Item $jarPath).Length
Write-Host "  Jar: $jarPath ($([math]::Round($jarSize / 1MB, 1)) MB)" -ForegroundColor Gray

# Step 3: Run jpackage --type app-image
Write-Host "`n[3/4] Running jpackage (app-image)..." -ForegroundColor Cyan

$jpackageInput = 'jpackage-input'
if (Test-Path $jpackageInput) { Remove-Item -Path $jpackageInput -Recurse -Force }
New-Item -ItemType Directory -Path $jpackageInput -Force | Out-Null
New-Item -ItemType Directory -Path "$jpackageInput\lib" -Force | Out-Null
New-Item -ItemType Directory -Path "$jpackageInput\agent" -Force | Out-Null

Copy-Item -Path $jarPath -Destination $jpackageInput
Copy-Item -Path 'explorer\target\lib\*' -Destination "$jpackageInput\lib" -Recurse
Copy-Item -Path 'agent\target\agent.jar' -Destination "$jpackageInput\agent"
Copy-Item -Path 'launch-agent\target\launch-agent.jar' -Destination "$jpackageInput\agent"
Write-Host "  Input dir: $jpackageInput" -ForegroundColor Gray

if (-not (Test-Path dist)) { New-Item -ItemType Directory -Path dist | Out-Null }

$appImageDir = 'app-image-out'
if (Test-Path $appImageDir) { Remove-Item -Path $appImageDir -Recurse -Force }

$modules = 'java.se,jdk.attach,jdk.compiler,jdk.unsupported,jdk.zipfs,jdk.management,jdk.crypto.ec,jdk.localedata,jdk.charsets'

$jpackageArgs = @(
    '--input', $jpackageInput,
    '--name', 'JVM-Explorer',
    '--main-jar', $jarName,
    '--main-class', 'com.tlcsdm.jvmexplorer.Startup',
    '--type', 'app-image',
    '--java-options', '-Xms64m',
    '--java-options', '-Xmx512m',
    '--java-options', '--add-modules=jdk.attach',
    '--java-options', '-Dfile.encoding=UTF-8',
    '--add-modules', $modules,
    '--jlink-options', '--strip-debug --no-man-pages --no-header-files --compress zip-6',
    '--app-version', $version,
    '--vendor', 'unknowIfGuestInDream',
    '--description', 'A JavaFX-based tool to explore and debug running JVM processes',
    '--dest', $appImageDir
)

$icoFile = 'explorer\src\main\resources\icons\icon.ico'
if (Test-Path $icoFile) {
    $jpackageArgs += '--icon', $icoFile
    Write-Host "  Icon: $icoFile" -ForegroundColor Gray
}

Write-Host "  Building app image..." -ForegroundColor Gray
& jpackage @jpackageArgs

if ($LASTEXITCODE -ne 0) { throw "jpackage failed with exit code $LASTEXITCODE" }

$appDir = Get-ChildItem -Path $appImageDir -Directory | Select-Object -First 1
if ($null -eq $appDir) { throw 'No output directory found after jpackage' }
Write-Host "  App image: $($appDir.FullName)" -ForegroundColor Gray

$appSize = (Get-ChildItem -Path $appDir.FullName -Recurse -File | Measure-Object -Property Length -Sum).Sum
Write-Host "  App image size: $([math]::Round($appSize / 1MB, 1)) MB" -ForegroundColor Gray

Copy-Item -Path 'README.md' -Destination $appDir.FullName
Copy-Item -Path 'LICENSE' -Destination $appDir.FullName
Write-Host "  Copied README.md and LICENSE" -ForegroundColor Gray

# Step 4: Zip and cleanup
Write-Host "`n[4/4] Packaging and cleaning up..." -ForegroundColor Cyan

$zipName = "jvm-explorer-windows-$version.zip"
$zipFull = Join-Path (Resolve-Path 'dist') $zipName
if (Test-Path $zipFull) { Remove-Item $zipFull -Force }

Add-Type -AssemblyName System.IO.Compression
$stagingFull = $appDir.FullName
$zipStream = [System.IO.File]::Create($zipFull)
$zip = New-Object System.IO.Compression.ZipArchive($zipStream, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in (Get-ChildItem $stagingFull -Recurse -File)) {
        $entryName = $file.FullName.Substring($stagingFull.Length).TrimStart('\', '/').Replace('\', '/')
        $entry = $zip.CreateEntry($entryName, [System.IO.Compression.CompressionLevel]::Optimal)
        $es = $entry.Open()
        try {
            $fs = [System.IO.FileStream]::new(
                $file.FullName,
                [System.IO.FileMode]::Open,
                [System.IO.FileAccess]::Read,
                ([System.IO.FileShare]::ReadWrite -bor [System.IO.FileShare]::Delete)
            )
            try { $fs.CopyTo($es) } finally { $fs.Dispose() }
        } finally { $es.Dispose() }
    }
} finally {
    $zip.Dispose()
    $zipStream.Dispose()
}

$zipSize = (Get-Item $zipFull).Length
Write-Host "  Zip: dist/$zipName ($([math]::Round($zipSize / 1MB, 1)) MB)" -ForegroundColor Gray

Remove-Item -Path $jpackageInput -Recurse -Force
Remove-Item -Path $appImageDir -Recurse -Force
Write-Host "  Cleaned up temp directories" -ForegroundColor Gray

$elapsed = (Get-Date) - $stepStart
Write-Host "`nApp image packaged successfully. ($('{0:mm\:ss}' -f $elapsed) elapsed)" -ForegroundColor Green
