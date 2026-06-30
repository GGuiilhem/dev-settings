$ErrorActionPreference = "Stop"

$packageName = "com.gguilhem.devsettings"
$permission = "android.permission.WRITE_SECURE_SETTINGS"

function Fail($message) {
    Write-Host "Erro: $message" -ForegroundColor Red
    exit 1
}

$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adbCommand) {
    Fail "adb nao foi encontrado no PATH. Instale o Android Platform Tools e tente novamente."
}

$devicesOutput = & adb devices
$authorizedDevices = $devicesOutput | Select-String -Pattern "\sdevice$"
if (-not $authorizedDevices) {
    Fail "nenhum celular autorizado foi encontrado. Ative Depuracao USB, conecte o aparelho e aceite a autorizacao."
}

Write-Host "Concedendo $permission para $packageName..."
& adb shell pm grant $packageName $permission

if ($LASTEXITCODE -ne 0) {
    Fail "nao foi possivel conceder a permissao."
}

Write-Host "Permissao concedida com sucesso." -ForegroundColor Green
