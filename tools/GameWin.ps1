# Test helper for in-game verification: window screenshot + simple mouse/keyboard input.
# Usage examples:
#   . .\tools\GameWin.ps1; Shot out.png            # capture the game window
#   . .\tools\GameWin.ps1; Click 720 450           # click at window-relative coords (client area px)
#   . .\tools\GameWin.ps1; Keys '`'                # send keys (SendKeys syntax)
Add-Type -AssemblyName System.Drawing, System.Windows.Forms
if (-not ('GW.Native' -as [type])) {
Add-Type -Namespace GW -Name Native -MemberDefinition @'
[DllImport("user32.dll")] public static extern bool SetProcessDPIAware();
[DllImport("user32.dll")] public static extern bool GetClientRect(IntPtr h, out RECT r);
[DllImport("user32.dll")] public static extern bool ClientToScreen(IntPtr h, ref POINT p);
[DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr h);
[DllImport("user32.dll")] public static extern bool SetCursorPos(int x, int y);
[DllImport("user32.dll")] public static extern void mouse_event(int f, int x, int y, int d, int e);
[DllImport("user32.dll")] public static extern void keybd_event(byte vk, byte scan, int flags, int extra);
public struct RECT { public int Left, Top, Right, Bottom; }
public struct POINT { public int X, Y; }
'@
}
[GW.Native]::SetProcessDPIAware() | Out-Null

function Get-GameWindow {
    $p = Get-Process java, javaw -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowHandle -ne 0 -and $_.MainWindowTitle -match 'Slay|Spire|Modded' } | Select-Object -First 1
    if (-not $p) { throw 'game window not found' }
    $p
}
function Get-ClientOrigin($h) {
    $pt = New-Object GW.Native+POINT; [GW.Native]::ClientToScreen($h, [ref]$pt) | Out-Null
    $r = New-Object GW.Native+RECT; [GW.Native]::GetClientRect($h, [ref]$r) | Out-Null
    @{ X = $pt.X; Y = $pt.Y; W = $r.Right; H = $r.Bottom }
}
function Shot([string]$path) {
    $p = Get-GameWindow; $h = $p.MainWindowHandle
    [GW.Native]::SetForegroundWindow($h) | Out-Null; Start-Sleep -Milliseconds 300
    $o = Get-ClientOrigin $h
    $bmp = New-Object System.Drawing.Bitmap $o.W, $o.H
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.CopyFromScreen($o.X, $o.Y, 0, 0, $bmp.Size)
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png); $g.Dispose(); $bmp.Dispose()
    "saved $path ($($o.W)x$($o.H)) title='$($p.MainWindowTitle)'"
}
function Click([int]$x, [int]$y) {
    $h = (Get-GameWindow).MainWindowHandle; [GW.Native]::SetForegroundWindow($h) | Out-Null
    $o = Get-ClientOrigin $h
    [GW.Native]::SetCursorPos($o.X + $x, $o.Y + $y) | Out-Null; Start-Sleep -Milliseconds 150
    [GW.Native]::mouse_event(0x2, 0, 0, 0, 0); Start-Sleep -Milliseconds 80; [GW.Native]::mouse_event(0x4, 0, 0, 0, 0)
}
function Drag([int]$x1, [int]$y1, [int]$x2, [int]$y2) {
    $h = (Get-GameWindow).MainWindowHandle; [GW.Native]::SetForegroundWindow($h) | Out-Null
    $o = Get-ClientOrigin $h
    [GW.Native]::SetCursorPos($o.X + $x1, $o.Y + $y1) | Out-Null; Start-Sleep -Milliseconds 300
    [GW.Native]::mouse_event(0x2, 0, 0, 0, 0)
    for ($i = 1; $i -le 15; $i++) {
        [GW.Native]::SetCursorPos($o.X + $x1 + ($x2 - $x1) * $i / 15, $o.Y + $y1 + ($y2 - $y1) * $i / 15) | Out-Null; Start-Sleep -Milliseconds 30
    }
    Start-Sleep -Milliseconds 200; [GW.Native]::mouse_event(0x4, 0, 0, 0, 0)
}
function Keys([string]$k) {
    $h = (Get-GameWindow).MainWindowHandle; [GW.Native]::SetForegroundWindow($h) | Out-Null; Start-Sleep -Milliseconds 150
    [System.Windows.Forms.SendKeys]::SendWait($k)
}
function Vk([byte]$vk) {
    $h = (Get-GameWindow).MainWindowHandle; [GW.Native]::SetForegroundWindow($h) | Out-Null; Start-Sleep -Milliseconds 100
    [GW.Native]::keybd_event($vk, 0, 0, 0); Start-Sleep -Milliseconds 60; [GW.Native]::keybd_event($vk, 0, 2, 0)
}
