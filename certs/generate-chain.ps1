param(
    [string]$StudentId = "REPLACE_WITH_STUDENT_ID",
    [string]$OutDir = ".\certs\out",
    [string]$RootPass = "RootPass_ChangeMe_123!",
    [string]$IntermediatePass = "IntPass_ChangeMe_123!",
    [string]$ServerPass = "ServerPass_ChangeMe_123!"
)

$ErrorActionPreference = "Stop"

if ($StudentId -eq "REPLACE_WITH_STUDENT_ID") {
    Write-Host "Сначала подставь свой номер студенческого билета в параметр -StudentId" -ForegroundColor Red
    exit 1
}

$keytool = "keytool"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$rootAlias = "rbpoRoot$StudentId"
$intAlias = "rbpoInt$StudentId"
$serverAlias = "rbpoSrv$StudentId"

$rootJks = Join-Path $OutDir "rbpo-root-$StudentId.jks"
$rootCrt = Join-Path $OutDir "rbpo-root-$StudentId.crt"

$intJks = Join-Path $OutDir "rbpo-int-$StudentId.jks"
$intCsr = Join-Path $OutDir "rbpo-int-$StudentId.csr"
$intCrt = Join-Path $OutDir "rbpo-int-$StudentId.crt"

$serverJks = Join-Path $OutDir "rbpo-server-$StudentId.jks"
$serverCsr = Join-Path $OutDir "rbpo-server-$StudentId.csr"
$serverCrt = Join-Path $OutDir "rbpo-server-$StudentId.crt"

# ROOT CA
& $keytool -genkeypair -alias $rootAlias -keyalg RSA -keysize 4096 -validity 3650 `
  -keystore $rootJks -storepass $RootPass -keypass $RootPass `
  -dname "CN=RBPO Root CA $StudentId, OU=RBPO-$StudentId, O=MTUCI-RBPO-$StudentId, L=Moscow, ST=Moscow, C=RU" `
  -ext "BC=ca:true" `
  -ext "KU=digitalSignature,keyCertSign"

& $keytool -exportcert -rfc -alias $rootAlias -keystore $rootJks -storepass $RootPass -file $rootCrt

# INTERMEDIATE CA
& $keytool -genkeypair -alias $intAlias -keyalg RSA -keysize 2048 -validity 1825 `
  -keystore $intJks -storepass $IntermediatePass -keypass $IntermediatePass `
  -dname "CN=RBPO Intermediate CA $StudentId, OU=RBPO-$StudentId, O=MTUCI-RBPO-$StudentId, L=Moscow, ST=Moscow, C=RU"

& $keytool -certreq -alias $intAlias -keystore $intJks -storepass $IntermediatePass -file $intCsr

# ROOT подписывает INTERMEDIATE
& $keytool -gencert -alias $rootAlias -keystore $rootJks -storepass $RootPass `
  -infile $intCsr -outfile $intCrt -validity 1825 `
  -ext "BC=ca:true,pathlen:0" `
  -ext "KU=digitalSignature,keyCertSign"

# Собираем цепочку в intermediate.jks
& $keytool -importcert -alias $rootAlias -keystore $intJks -storepass $IntermediatePass -file $rootCrt -noprompt
& $keytool -importcert -alias $intAlias -keystore $intJks -storepass $IntermediatePass -file $intCrt -noprompt

# SERVER
& $keytool -genkeypair -alias $serverAlias -keyalg RSA -keysize 2048 -validity 365 `
  -keystore $serverJks -storepass $ServerPass -keypass $ServerPass `
  -dname "CN=localhost, OU=RBPO-$StudentId, O=MTUCI-RBPO-$StudentId, L=Moscow, ST=Moscow, C=RU" `
  -ext "SAN=DNS:localhost"

& $keytool -certreq -alias $serverAlias -keystore $serverJks -storepass $ServerPass -file $serverCsr

# INTERMEDIATE подписывает SERVER
& $keytool -gencert -alias $intAlias -keystore $intJks -storepass $IntermediatePass `
  -infile $serverCsr -outfile $serverCrt -validity 365 `
  -ext "KU=digitalSignature,keyEncipherment" `
  -ext "EKU=serverAuth,clientAuth" `
  -ext "SAN=DNS:localhost"

# Собираем полную цепочку в server.jks
& $keytool -importcert -alias $rootAlias -keystore $serverJks -storepass $ServerPass -file $rootCrt -noprompt
& $keytool -importcert -alias $intAlias -keystore $serverJks -storepass $ServerPass -file $intCrt -noprompt
& $keytool -importcert -alias $serverAlias -keystore $serverJks -storepass $ServerPass -file $serverCrt -noprompt

Write-Host ""
Write-Host "Готово." -ForegroundColor Green
Write-Host "Корневой сертификат: $rootCrt"
Write-Host "Промежуточный сертификат: $intCrt"
Write-Host "Серверный keystore: $serverJks"
Write-Host "Проверь цепочку командой:"
Write-Host "keytool -list -v -keystore `"$serverJks`" -storepass $ServerPass"