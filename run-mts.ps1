# Starts ModTheSpire with the game's bundled JRE, skipping the launcher GUI.
# Usage:  powershell -ExecutionPolicy Bypass -File run-mts.ps1 [-Mods basemod,elfprototype] [-Vanilla]
#   -Mods basemod             -> loader + BaseMod only (baseline)
#   -Mods basemod,elfprototype (default) -> with the test skin
#   -Vanilla                  -> plain game, no ModTheSpire
param([string]$Mods = 'basemod,elfprototype', [switch]$Vanilla, [string]$LogFile = '')
$ErrorActionPreference = 'Stop'
$cfg = Join-Path $PSScriptRoot '.mvn\maven.config'
$lines = Get-Content $cfg
$stsHome = ($lines | Where-Object { $_ -like '-Dsts.home=*' }) -replace '^-Dsts.home=', ''
$workshop = ($lines | Where-Object { $_ -like '-Dsts.workshop=*' }) -replace '^-Dsts.workshop=', ''
$java = Join-Path $stsHome 'jre\bin\java.exe'
if (-not $LogFile) { $LogFile = Join-Path $PSScriptRoot ("logs\run-{0}.log" -f (Get-Date -Format 'yyyyMMdd-HHmmss')) }
New-Item -ItemType Directory (Split-Path $LogFile) -Force | Out-Null
Push-Location $stsHome
try {
    if ($Vanilla) {
        $jargs = @('-Xmx1G', '-jar', 'desktop-1.0.jar')
    } else {
        $mts = Join-Path $workshop '1605060445\ModTheSpire.jar'
        $jargs = @('-Xmx1G', '-jar', $mts, '--skip-launcher', '--skip-intro', '--mods', $Mods)
    }
    "java: $java"; "args: $($jargs -join ' ')"; "log:  $LogFile"
    $p = Start-Process -FilePath $java -ArgumentList $jargs -WorkingDirectory $stsHome -PassThru `
        -RedirectStandardOutput $LogFile -RedirectStandardError ($LogFile + '.err')
    "pid:  $($p.Id)"
} finally { Pop-Location }
