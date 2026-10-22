#Requires -Version 7.0
param([string]$ProjectRoot=(Get-Location).Path,[Parameter(Mandatory)][string]$SigningKey,
    [string]$AndroidSdk=$env:ANDROID_HOME,[switch]$SkipBuild,
    [string]$WorkDirectory=(Join-Path ([IO.Path]::GetTempPath()) 'KIGTTS/plugin-package-work'))
$ErrorActionPreference = 'Stop'
$projectRoot=[IO.Path]::GetFullPath($ProjectRoot)
$sdkRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$config=Get-Content -LiteralPath (Join-Path $projectRoot 'plugin-build.json') -Raw | ConvertFrom-Json
$Modules=@($config.module)
if(-not $AndroidSdk){throw 'Pass -AndroidSdk or set ANDROID_HOME'}
$AndroidSdk=[IO.Path]::GetFullPath($AndroidSdk)
Set-Location -LiteralPath $projectRoot
if (-not $SkipBuild) {
    $buildTasks = @('assembleDebug','collectPluginDependencies')
    $gradle=Join-Path $projectRoot $(if($IsWindows){'gradlew.bat'}else{'gradlew'})
    & $gradle --console=plain @buildTasks
    if ($LASTEXITCODE -ne 0) { throw 'Plugin compilation failed' }
}
$utf8 = [Text.UTF8Encoding]::new($false)
$sdkApi = [int]([regex]::Match([IO.File]::ReadAllText((Join-Path $sdkRoot 'plugin-api/src/main/kotlin/com/kigtts/plugin/api/Plugin.kt')), 'const val PLUGIN_API_VERSION = (\d+)').Groups[1].Value)
if ($sdkApi -le 0) { throw 'Plugin SDK version missing' }
$SigningKey=[IO.Path]::GetFullPath($SigningKey)
if(-not (Test-Path -LiteralPath $SigningKey -PathType Leaf)){throw 'Publisher signing key is missing'}
$rsa=[Security.Cryptography.RSA]::Create(3072)
$consumed=0
$rsa.ImportPkcs8PrivateKey([IO.File]::ReadAllBytes($SigningKey),[ref]$consumed)
$outputRoot = Join-Path $projectRoot 'build/plugin-packages'
[IO.Directory]::CreateDirectory($outputRoot) | Out-Null
function Expand-PluginAar([string]$Aar, [string]$Payload, [string]$Scratch, [string]$Prefix, [string]$NativeAbi = '') {
    $archive = [IO.Compression.ZipFile]::OpenRead($Aar)
    try {
        $jars = @()
        foreach ($entry in $archive.Entries) {
            if ($entry.FullName -eq 'classes.jar') {
                $jar = Join-Path $Scratch "$Prefix-classes.jar"
                [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $jar, $true)
                $jars += $jar
            }
            if ($entry.FullName -match '^jni/([^/]+)/([^/]+\.so)$') {
                if ($NativeAbi -and $Matches[1] -ne $NativeAbi) { continue }
                $target = Join-Path $Payload "lib/$($Matches[1])/$($Matches[2])"
                [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($target)) | Out-Null
                if (Test-Path -LiteralPath $target) {
                    throw "Duplicate native library: $($entry.FullName)"
                }
                [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $target, $false)
            }
        }
        return $jars
    } finally { $archive.Dispose() }
}
foreach ($module in $Modules) {
    $moduleRoot = $projectRoot
    $spec = Get-Content -LiteralPath (Join-Path $moduleRoot 'plugin-spec.json') -Raw -Encoding utf8 | ConvertFrom-Json -AsHashtable
    # Every module is compiled against this SDK; do not label fresh bytecode with an older API.
    $spec.apiVersion = $sdkApi
    $scratch = [IO.Path]::GetFullPath((Join-Path $WorkDirectory "$module/$([Guid]::NewGuid())"))
    $nativeAbi = if ($module -eq 'piper-tts') { 'arm64-v8a' } else { '' }
    $payload = Join-Path $scratch 'payload'
    [IO.Directory]::CreateDirectory((Join-Path $payload 'code')) | Out-Null
    $jars = @(Expand-PluginAar (Join-Path $moduleRoot "build/outputs/aar/$module-debug.aar") $payload $scratch $module $nativeAbi)
    foreach($extra in @($config.extraAars)) {
        $jars += @(Expand-PluginAar (Join-Path $moduleRoot $extra) $payload $scratch 'extra' $nativeAbi)
    }
    foreach ($dependency in @(Get-ChildItem -LiteralPath (Join-Path $moduleRoot 'build/plugin-dependencies') -ErrorAction SilentlyContinue | Where-Object { $_.Name -match '^(debug-ui|onnxruntime-android|xz-|tts-support|commons-compress|commons-io|commons-codec|commons-lang3|luaj-jse)' })) {
        if ($dependency.Extension -eq '.aar') { $jars += @(Expand-PluginAar $dependency.FullName $payload $scratch $dependency.BaseName $nativeAbi) }
        elseif ($dependency.Extension -eq '.jar') { $jars += $dependency.FullName }
    }
    $assetsRoot = Join-Path $moduleRoot 'src/main/assets'
    if (Test-Path -LiteralPath $assetsRoot) {
        [IO.Directory]::CreateDirectory((Join-Path $payload 'assets')) | Out-Null
        Copy-Item -Path (Join-Path $assetsRoot '*') -Destination (Join-Path $payload 'assets') -Recurse
    }
    if ($module -eq 'piper-tts') {
        [IO.Directory]::CreateDirectory((Join-Path $payload 'lib/arm64-v8a')) | Out-Null
        Copy-Item -Path (Join-Path $moduleRoot 'src/main/jniLibs/arm64-v8a/*.so') -Destination (Join-Path $payload 'lib/arm64-v8a')
        # Keep only complete ABIs when extracting dependencies; the ported bridge is arm64.
    }
    & (Join-Path $AndroidSdk $(if($IsWindows){'build-tools/36.0.0/d8.bat'}else{'build-tools/36.0.0/d8'})) --min-api 26 --lib (Join-Path $AndroidSdk 'platforms/android-36/android.jar') --output (Join-Path $payload 'code') @jars
    if ($LASTEXITCODE -ne 0) { throw "DEX compilation failed: $module" }
    $licenses = Join-Path $payload 'assets/licenses'
    [IO.Directory]::CreateDirectory($licenses) | Out-Null
    $licenseName = 'PLUGIN-LICENSE.txt'
    Copy-Item -LiteralPath (Join-Path $moduleRoot 'LICENSE') -Destination (Join-Path $licenses $licenseName)
    Copy-Item -LiteralPath (Join-Path $moduleRoot 'NOTICE.md') -Destination (Join-Path $licenses 'NOTICE.md')
    if ($module -eq 'sensevoice-asr') {
        Copy-Item -LiteralPath (Join-Path $moduleRoot 'licenses/sherpa-onnx.txt') -Destination $licenses
        Copy-Item -LiteralPath (Join-Path $moduleRoot 'licenses/onnxruntime.txt') -Destination $licenses
    }
    if (Test-Path -LiteralPath (Join-Path $moduleRoot 'licenses')) {
        Copy-Item -Path (Join-Path $moduleRoot 'licenses/*') -Destination $licenses -Recurse
    }
    $spec['abis'] = @(Get-ChildItem -LiteralPath (Join-Path $payload 'lib') -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Name)
    $hashes = [ordered]@{}
    Get-ChildItem -LiteralPath $payload -File -Recurse | Sort-Object FullName | ForEach-Object {
        $relative = [IO.Path]::GetRelativePath($payload, $_.FullName).Replace('\', '/')
        $hashes[$relative] = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
    }
    $spec['files'] = $hashes
    if ($spec.icon) {
        $icon = $spec.icon
        if ([bool]$icon.symbol -eq [bool]$icon.path) { throw 'Choose one package icon: symbol or path' }
        if ($icon.symbol -and $icon.symbol -cnotmatch '^[a-z][a-z0-9_]{0,79}$') { throw 'Invalid Material Symbol name' }
        if ($icon.path) {
            if ($icon.path -notmatch '^assets/[^\\:]+\.(svg|png|jpe?g|webp)$' -or
                @($icon.path -split '/' | Where-Object { $_ -in @('', '.', '..') }).Count -gt 0 -or
                -not $hashes.Contains($icon.path)) { throw 'Icon must be a signed SVG/PNG/JPEG/WebP asset inside the package' }
            $iconFile = Get-Item -LiteralPath (Join-Path $payload $icon.path)
            if ($iconFile.Length -lt 1 -or $iconFile.Length -gt 1048576) { throw 'Package icon must be at most 1 MiB' }
        }
    }
    $raw = $utf8.GetBytes(($spec | ConvertTo-Json -Depth 12 -Compress))
    [IO.File]::WriteAllBytes((Join-Path $payload 'plugin.json'), $raw)
    $signatureRoot = Join-Path $payload 'signature'
    [IO.Directory]::CreateDirectory($signatureRoot) | Out-Null
    [IO.File]::WriteAllBytes((Join-Path $signatureRoot 'public.der'), $rsa.ExportSubjectPublicKeyInfo())
    [IO.File]::WriteAllBytes((Join-Path $signatureRoot 'manifest.sig'), $rsa.SignData($raw, [Security.Cryptography.HashAlgorithmName]::SHA256, [Security.Cryptography.RSASignaturePadding]::Pkcs1))
    $result = Join-Path $outputRoot "$($spec.id)-$($spec.version).kigplugin"
    if (Test-Path -LiteralPath $result) { throw 'Output version already exists; keep immutable releases or choose a fresh build directory' }
    [IO.Compression.ZipFile]::CreateFromDirectory($payload, $result, [IO.Compression.CompressionLevel]::Optimal, $false)
    Write-Output "$result ($((Get-Item -LiteralPath $result).Length) bytes)"
}
$rsa.Dispose()
