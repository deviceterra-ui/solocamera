param([Parameter(Mandatory=$true)][string]$VideoUri,[Parameter(Mandatory=$true)][long]$DurationMs,[int]$Width=1080,[int]$Height=1920)
$ErrorActionPreference='Stop'
if($DurationMs -lt 3000){throw 'Choose an app-owned video at least three seconds long.'}
Push-Location $PSScriptRoot\..
try {
    $sdk = if($env:ANDROID_HOME){$env:ANDROID_HOME}else{Join-Path $env:LOCALAPPDATA 'Android\Sdk'}
    $adb=Join-Path $sdk 'platform-tools\adb.exe'
    New-Item -ItemType Directory -Force qa | Out-Null
    $clip=@{uri=$VideoUri;name='QA video';duration=$DurationMs;start=0;end=3000;width=$Width;height=$Height;text='SOLO TEST';textSize=70;textStart=300;textEnd=2500;textPlaced=$true;textX=-0.3;textY=0.5;brightness=0;contrast=0;saturation=0;pitch=0.85;fade=300}
    $overlayClip=@{};foreach($key in $clip.Keys){$overlayClip[$key]=$clip[$key]};$overlayClip.text='';$overlayClip.start=1000;$overlayClip.end=2200;$overlayClip.mask=1;$overlayClip.feather=0.2;$overlayClip.animation=1;$overlayClip.cropX=0.1;$overlayClip.fade=0
    $project=@{id='11111111-1111-4111-8111-111111111111';name='Editor QA';height=720;clips=@($clip);overlays=@(@{clip=$overlayClip;at=1000;x=0.45;y=-0.45;scale=0.4;alpha=1})}
    [System.IO.File]::WriteAllText((Join-Path $PWD 'qa\editor-check.json'),($project|ConvertTo-Json -Depth 8),(New-Object System.Text.UTF8Encoding $false))
    & $adb push qa/editor-check.json /data/local/tmp/editor-check.json
    if($LASTEXITCODE -ne 0){throw 'Fixture transfer failed'}
    $result=& $adb shell am instrument -w -e projectFile /data/local/tmp/editor-check.json studio.solocamera.test/studio.solocamera.EditorDeviceChecks
    $result | Write-Output
    if(($result -join "`n") -notmatch 'checks=PASS'){throw 'Device editor checks failed'}
} finally {Pop-Location}
