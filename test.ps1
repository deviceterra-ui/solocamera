$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $compiler = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\javac.exe' } else { 'javac' }
    $runtime = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }
    New-Item -ItemType Directory -Force -Path tests\out | Out-Null
    & $compiler -d tests\out app\src\main\java\studio\solocamera\SpeechCleanup.java tests\SpeechCleanupTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Speech cleanup compilation failed' }
    & $runtime -cp tests\out studio.solocamera.SpeechCleanupTest
    if ($LASTEXITCODE -ne 0) { throw 'Speech cleanup tests failed' }
    & $compiler -d tests\out app\src\main\java\studio\solocamera\EditTiming.java tests\EditTimingTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Editor timing compilation failed' }
    & $runtime -cp tests\out studio.solocamera.EditTimingTest
    if ($LASTEXITCODE -ne 0) { throw 'Editor timing tests failed' }

    & $compiler -d tests\out app\src\main\java\studio\solocamera\FrameRatePolicy.java app\src\main\java\studio\solocamera\CreatorLooks.java tests\CreatorSettingsTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Creator settings compilation failed' }
    & $runtime -cp tests\out studio.solocamera.CreatorSettingsTest
    if ($LASTEXITCODE -ne 0) { throw 'Creator settings tests failed' }

    & $compiler -d tests\out app\src\main\java\studio\solocamera\SecureChannel.java app\src\main\java\studio\solocamera\SliderScale.java tests\SecureChannelTest.java tests\SliderScaleTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
    & $runtime -cp tests\out studio.solocamera.SecureChannelTest
    if ($LASTEXITCODE -ne 0) { throw 'Transport tests failed' }
    & $runtime -cp tests\out studio.solocamera.SliderScaleTest
    if ($LASTEXITCODE -ne 0) { throw 'Slider range tests failed' }
    & $compiler -cp app\libs\core-3.5.3.jar -d tests\out app\src\main\java\studio\solocamera\PairingCode.java tests\PairingCodeTest.java
    if ($LASTEXITCODE -ne 0) { throw 'QR test compilation failed' }
    & $runtime -cp 'tests\out;app\libs\core-3.5.3.jar' studio.solocamera.PairingCodeTest
    if ($LASTEXITCODE -ne 0) { throw 'QR tests failed' }
    & $compiler -d tests\out app\src\main\java\studio\solocamera\NetworkAddress.java tests\NetworkAddressTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Network test compilation failed' }
    & $runtime -cp tests\out studio.solocamera.NetworkAddressTest
    if ($LASTEXITCODE -ne 0) { throw 'Network tests failed' }
    & $compiler -d tests\out app\src\main\java\studio\solocamera\WhiteBalanceFine.java tests\WhiteBalanceFineTest.java
    if ($LASTEXITCODE -ne 0) { throw 'White balance test compilation failed' }
    & $runtime -cp tests\out studio.solocamera.WhiteBalanceFineTest
    if ($LASTEXITCODE -ne 0) { throw 'White balance tests failed' }
    & $compiler -d tests\out app\src\main\java\studio\solocamera\VideoQuality.java tests\VideoQualityTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Video quality test compilation failed' }
    & $runtime -cp tests\out studio.solocamera.VideoQualityTest
    if ($LASTEXITCODE -ne 0) { throw 'Video quality tests failed' }
    $previewSdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
    $previewAndroidJar = Join-Path $previewSdk 'platforms\android-35\android.jar'
    $editorJsonJar = $env:EDITOR_JSON_TEST_JAR
    if (-not $editorJsonJar) {
        $editorJsonCache = Join-Path $env:USERPROFILE '.gradle\caches\modules-2\files-2.1\org.json\json'
        if (Test-Path $editorJsonCache) { $editorJsonJar = Get-ChildItem -LiteralPath $editorJsonCache -Recurse -Filter 'json-*.jar' | Select-Object -First 1 -ExpandProperty FullName }
    }
    if ((Test-Path $previewAndroidJar) -and $editorJsonJar -and (Test-Path $editorJsonJar)) {
        & $compiler -cp "$editorJsonJar;$previewAndroidJar" -d tests\out app\src\main\java\studio\solocamera\EditTiming.java app\src\main\java\studio\solocamera\EditorTransitions.java app\src\main\java\studio\solocamera\CreatorLooks.java app\src\main\java\studio\solocamera\EditorTransitions.java app\src\main\java\studio\solocamera\EditorMotion.java app\src\main\java\studio\solocamera\EditorProject.java app\src\main\java\studio\solocamera\EditorEdits.java tests\EditorTracksTest.java
        if ($LASTEXITCODE -ne 0) { throw 'Editor track test compilation failed' }
        & $runtime -cp "tests\out;$editorJsonJar;$previewAndroidJar" studio.solocamera.EditorTracksTest
        if ($LASTEXITCODE -ne 0) { throw 'Independent editor track tests failed' }
    } else { Write-Host 'SKIP independent editor track tests: set EDITOR_JSON_TEST_JAR to a JVM org.json JAR and install Android SDK Platform 35.' }
    if ((Test-Path $previewAndroidJar) -and $editorJsonJar) {
        & $compiler -cp "$editorJsonJar;$previewAndroidJar;tests\out" -d tests\out app\src\main\java\studio\solocamera\PreviewWindow.java app\src\main\java\studio\solocamera\PeerLink.java tests\PeerPreviewTest.java tests\PreviewBackpressureTest.java
        if ($LASTEXITCODE -ne 0) { throw 'Preview transport test compilation failed' }
        & $runtime -cp "tests\out;$editorJsonJar;$previewAndroidJar" studio.solocamera.PeerPreviewTest
        if ($LASTEXITCODE -ne 0) { throw 'Preview transport tests failed' }
        & $runtime -cp "tests\out;$editorJsonJar;$previewAndroidJar" studio.solocamera.PreviewBackpressureTest
        if ($LASTEXITCODE -ne 0) { throw 'Preview backpressure tests failed' }
    } else { Write-Host 'Preview transport test needs Android SDK Platform 35 (set ANDROID_HOME).' }
} finally { Pop-Location }


