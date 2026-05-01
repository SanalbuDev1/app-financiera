# =============================================
# Paso 7 — Crear Azure Database for PostgreSQL Flexible Server
# Tier Burstable B1ms — GRATIS 12 meses con cuenta free de Azure
# Incluye: 750 hrs/mes, 32 GB storage, 32 GB backup
# =============================================
$RESOURCE_GROUP = "rg-finanzas"
$LOCATION = "canadacentral"
$PG_SERVER = "finanzas-pgserver"
$PG_DB = "finanzas_db"
$PG_ADMIN = "finanzasadmin"

# Solicitar password al usuario (no hardcodear)
$PG_PASSWORD = Read-Host -Prompt "Ingresa el password para el admin de PostgreSQL (min 8 chars, mayuscula, minuscula, numero)" -AsSecureString
$PG_PASSWORD_PLAIN = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($PG_PASSWORD))

Write-Host ""
Write-Host "Creando Azure PostgreSQL Flexible Server: $PG_SERVER..." -ForegroundColor Cyan
Write-Host "Tier: Burstable B1ms (gratis 12 meses)" -ForegroundColor Yellow
Write-Host "Esto puede tardar 3-5 minutos..." -ForegroundColor Yellow
Write-Host ""

# Crear el servidor PostgreSQL Flexible (Burstable B1ms = free tier 12 meses)
az postgres flexible-server create `
  --name $PG_SERVER `
  --resource-group $RESOURCE_GROUP `
  --location $LOCATION `
  --admin-user $PG_ADMIN `
  --admin-password $PG_PASSWORD_PLAIN `
  --sku-name Standard_B1ms `
  --tier Burstable `
  --storage-size 32 `
  --version 16 `
  --yes

# Verificar que el servidor se creó correctamente
$serverExists = az postgres flexible-server show --name $PG_SERVER --resource-group $RESOURCE_GROUP --query "name" -o tsv 2>$null
if (-not $serverExists) {
    Write-Host ""
    Write-Host "ERROR: El servidor no se pudo crear. Verifica la region y vuelve a intentar." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Servidor creado exitosamente." -ForegroundColor Green
Write-Host ""
Write-Host "Creando base de datos: $PG_DB..." -ForegroundColor Cyan

# Crear la base de datos
az postgres flexible-server db create `
  --resource-group $RESOURCE_GROUP `
  --server-name $PG_SERVER `
  --database-name $PG_DB

Write-Host ""
Write-Host "Configurando reglas de firewall..." -ForegroundColor Cyan

# Permitir acceso desde servicios de Azure (Container Apps)
az postgres flexible-server firewall-rule create `
  --resource-group $RESOURCE_GROUP `
  --name $PG_SERVER `
  --rule-name AllowAzureServices `
  --start-ip-address 0.0.0.0 `
  --end-ip-address 0.0.0.0

# Permitir acceso desde tu IP actual (para ejecutar init.sql)
$MY_IP = (Invoke-RestMethod -Uri "https://api.ipify.org")
Write-Host "Tu IP actual: $MY_IP" -ForegroundColor Yellow

az postgres flexible-server firewall-rule create `
  --resource-group $RESOURCE_GROUP `
  --name $PG_SERVER `
  --rule-name AllowMyIP `
  --start-ip-address $MY_IP `
  --end-ip-address $MY_IP

Write-Host ""
Write-Host "Ejecutando schema SQL..." -ForegroundColor Cyan

# Obtener el FQDN del servidor
$PG_FQDN = az postgres flexible-server show `
  --name $PG_SERVER `
  --resource-group $RESOURCE_GROUP `
  --query fullyQualifiedDomainName -o tsv

# Ejecutar init.sql (requiere psql instalado)
if (Get-Command psql -ErrorAction SilentlyContinue) {
    $env:PGPASSWORD = $PG_PASSWORD_PLAIN
    psql -h $PG_FQDN -U $PG_ADMIN -d $PG_DB -f src/main/resources/db/init.sql
    $env:PGPASSWORD = $null
    Write-Host "Schema ejecutado exitosamente." -ForegroundColor Green
} else {
    Write-Host "AVISO: psql no encontrado. Ejecuta el schema manualmente:" -ForegroundColor Yellow
    Write-Host "  psql -h $PG_FQDN -U $PG_ADMIN -d $PG_DB -f src/main/resources/db/init.sql" -ForegroundColor White
    Write-Host "  O usa Azure Data Studio / pgAdmin para ejecutar init.sql" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " Azure PostgreSQL creado exitosamente!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Servidor: $PG_FQDN" -ForegroundColor White
Write-Host "Base de datos: $PG_DB" -ForegroundColor White
Write-Host "Usuario: $PG_ADMIN" -ForegroundColor White
Write-Host ""
Write-Host "--- Valores para GitHub Secrets ---" -ForegroundColor Yellow
Write-Host "SPRING_R2DBC_URL      = r2dbc:postgresql://${PG_FQDN}:5432/${PG_DB}?sslmode=require" -ForegroundColor White
Write-Host "SPRING_R2DBC_USERNAME = $PG_ADMIN" -ForegroundColor White
Write-Host "SPRING_R2DBC_PASSWORD = (el password que ingresaste)" -ForegroundColor White
