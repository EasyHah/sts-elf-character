# Explicit deploy: copies target\ElfPrototypeMod.jar into <sts.home>\mods\. Build never does this implicitly.
# Usage:  powershell -ExecutionPolicy Bypass -File deploy.ps1 [-Remove]
param([switch]$Remove)
$ErrorActionPreference = 'Stop'
$cfg = Join-Path $PSScriptRoot '.mvn\maven.config'
if (-not (Test-Path $cfg)) { throw "Missing .mvn\maven.config (copy .mvn\maven.config.example and edit paths)" }
$stsHome = (Get-Content $cfg | Where-Object { $_ -like '-Dsts.home=*' }) -replace '^-Dsts.home=', ''
if (-not (Test-Path (Join-Path $stsHome 'desktop-1.0.jar'))) { throw "sts.home '$stsHome' has no desktop-1.0.jar" }
$mods = Join-Path $stsHome 'mods'
$dest = Join-Path $mods 'ElfPrototypeMod.jar'
if ($Remove) {
    if (Test-Path $dest) { Remove-Item $dest -Confirm:$false; "Removed $dest" } else { "Not deployed: $dest" }
    return
}
$jar = Join-Path $PSScriptRoot 'target\ElfPrototypeMod.jar'
if (-not (Test-Path $jar)) { throw "Build first: mvn package (missing $jar)" }
New-Item -ItemType Directory $mods -Force | Out-Null
Copy-Item $jar $dest -Force
"Deployed $dest"
"SHA256 " + (Get-FileHash -Algorithm SHA256 $dest).Hash
