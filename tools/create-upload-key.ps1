param([string]$JavaHome = 'C:\Program Files\Android\Android Studio\jbr')

$ErrorActionPreference = 'Stop'
$releaseDir = Join-Path $PSScriptRoot '..\release'
$keyPath = Join-Path $releaseDir 'solocamera-upload.p12'
$settingsPath = Join-Path $releaseDir 'upload-signing.properties'
if ((Test-Path $keyPath) -or (Test-Path $settingsPath)) {
    throw 'An upload key or signing settings already exist. Back them up; do not overwrite them.'
}
New-Item -ItemType Directory -Path $releaseDir -Force | Out-Null
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$password = [Convert]::ToHexString($bytes)
$keytool = Join-Path $JavaHome 'bin\keytool.exe'
& $keytool -genkeypair -noprompt -keystore $keyPath -storetype PKCS12 -storepass $password -keypass $password -alias solocamera-upload -keyalg RSA -keysize 4096 -validity 10000 -dname 'CN=Solo Camera Upload Key, O=Solo Camera'
if ($LASTEXITCODE -ne 0) { throw 'keytool failed to create the upload key.' }
$forwardPath = $keyPath.Replace('\', '/')
@("SOLO_UPLOAD_KEYSTORE=$forwardPath", "SOLO_UPLOAD_STORE_PASSWORD=$password", 'SOLO_UPLOAD_KEY_ALIAS=solocamera-upload', "SOLO_UPLOAD_KEY_PASSWORD=$password") | Set-Content -Path $settingsPath -Encoding ascii
Write-Output "Upload key created at $keyPath. Back up the .p12 file and upload-signing.properties securely; neither belongs in Git."
