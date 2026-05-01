# =============================================
# setup-azure.ps1 — Crear toda la infraestructura en Azure
# Ejecutar UNA SOLA VEZ antes del primer deploy
# Prerequisitos:
#   - Azure CLI instalado (winget install Microsoft.AzureCLI)
#   - Estar logueado: az login
#   - psql instalado (para ejecutar init.sql)
# =============================================

# --- VARIABLES (personalizar) ---
$RESOURCE_GROUP = "rg-finanzas"
$LOCATION = "eastus"
$ACR_NAME = "finanzasacr"                    # Solo letras minúsculas, sin guiones
$CONTAINER_ENV = "finanzas-env"
$CONTAINER_APP = "finanzas-app"
$STATIC_WEB_APP = "finanzas-frontend"
$PG_SERVER = "finanzas-pgserver"
$PG_DB = "finanzas_db"
$PG_ADMIN = "finanzasadmin"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 1/6 — Crear Resource Group" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
az group create --name $RESOURCE_GROUP --location $LOCATION

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 2/6 — Crear Azure Container Registry" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
az acr create `
  --name $ACR_NAME `
  --resource-group $RESOURCE_GROUP `
  --sku Basic `
  --admin-enabled true

# Obtener credenciales del ACR
$ACR_SERVER = az acr show --name $ACR_NAME --query loginServer -o tsv
$ACR_USERNAME = az acr credential show --name $ACR_NAME --query username -o tsv
$ACR_PASSWORD = az acr credential show --name $ACR_NAME --query "passwords[0].value" -o tsv

Write-Host "  ACR Server: $ACR_SERVER" -ForegroundColor Green
Write-Host "  ACR Username: $ACR_USERNAME" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 3/6 — Crear Container Apps Environment" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
az containerapp env create `
  --name $CONTAINER_ENV `
  --resource-group $RESOURCE_GROUP `
  --location $LOCATION

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 4/6 — Crear Container App (backend)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
# Se crea con una imagen placeholder; el pipeline la actualiza después
az containerapp create `
  --name $CONTAINER_APP `
  --resource-group $RESOURCE_GROUP `
  --environment $CONTAINER_ENV `
  --image "mcr.microsoft.com/azuredocs/containerapps-helloworld:latest" `
  --target-port 9000 `
  --ingress external `
  --min-replicas 0 `
  --max-replicas 1 `
  --cpu 0.25 `
  --memory 0.5Gi `
  --registry-server $ACR_SERVER `
  --registry-username $ACR_USERNAME `
  --registry-password $ACR_PASSWORD

$BACKEND_URL = az containerapp show --name $CONTAINER_APP --resource-group $RESOURCE_GROUP --query "properties.configuration.ingress.fqdn" -o tsv
Write-Host "  Backend URL: https://$BACKEND_URL" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 5/6 — Crear Static Web App (frontend)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
az staticwebapp create `
  --name $STATIC_WEB_APP `
  --resource-group $RESOURCE_GROUP `
  --location $LOCATION `
  --sku Free

$FRONTEND_URL = az staticwebapp show --name $STATIC_WEB_APP --resource-group $RESOURCE_GROUP --query "defaultHostname" -o tsv
Write-Host "  Frontend URL: https://$FRONTEND_URL" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 6/7 — Crear Azure PostgreSQL Flexible Server" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tier: Burstable B1ms (gratis 12 meses)" -ForegroundColor Yellow
Write-Host "Esto puede tardar 3-5 minutos..." -ForegroundColor Yellow

# Solicitar password
$PG_PASSWORD = Read-Host -Prompt "Ingresa el password para PostgreSQL (min 8 chars, mayuscula, minuscula, numero)" -AsSecureString
$PG_PASSWORD_PLAIN = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($PG_PASSWORD))

# Crear servidor
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

# Crear base de datos
az postgres flexible-server db create `
  --resource-group $RESOURCE_GROUP `
  --server-name $PG_SERVER `
  --database-name $PG_DB

# Firewall: permitir servicios de Azure
az postgres flexible-server firewall-rule create `
  --resource-group $RESOURCE_GROUP `
  --name $PG_SERVER `
  --rule-name AllowAzureServices `
  --start-ip-address 0.0.0.0 `
  --end-ip-address 0.0.0.0

# Firewall: permitir IP actual
$MY_IP = (Invoke-RestMethod -Uri "https://api.ipify.org")
az postgres flexible-server firewall-rule create `
  --resource-group $RESOURCE_GROUP `
  --name $PG_SERVER `
  --rule-name AllowMyIP `
  --start-ip-address $MY_IP `
  --end-ip-address $MY_IP

$PG_FQDN = az postgres flexible-server show --name $PG_SERVER --resource-group $RESOURCE_GROUP --query fullyQualifiedDomainName -o tsv
Write-Host "  PostgreSQL Server: $PG_FQDN" -ForegroundColor Green

# Ejecutar schema SQL
Write-Host "Ejecutando schema SQL..." -ForegroundColor Cyan
$env:PGPASSWORD = $PG_PASSWORD_PLAIN
psql -h $PG_FQDN -U $PG_ADMIN -d $PG_DB -f src/main/resources/db/init.sql
$env:PGPASSWORD = $null

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 7/7 — Crear Service Principal para GitHub Actions" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
$SUBSCRIPTION_ID = az account show --query id -o tsv
$SP_JSON = az ad sp create-for-rbac `
  --name "sp-finanzas-github" `
  --role contributor `
  --scopes "/subscriptions/$SUBSCRIPTION_ID/resourceGroups/$RESOURCE_GROUP" `
  --sdk-auth

Write-Host ""
Write-Host "========================================" -ForegroundColor Yellow
Write-Host " RESUMEN — Guardar estos valores como GitHub Secrets" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "AZURE_CREDENTIALS = (el JSON del service principal arriba)" -ForegroundColor White
Write-Host "ACR_LOGIN_SERVER = $ACR_SERVER" -ForegroundColor White
Write-Host "ACR_USERNAME = $ACR_USERNAME" -ForegroundColor White
Write-Host "ACR_PASSWORD = $ACR_PASSWORD" -ForegroundColor White
Write-Host "AZURE_RESOURCE_GROUP = $RESOURCE_GROUP" -ForegroundColor White
Write-Host "AZURE_CONTAINER_APP_NAME = $CONTAINER_APP" -ForegroundColor White
Write-Host "APP_CORS_ALLOWED_ORIGINS = https://$FRONTEND_URL" -ForegroundColor White
Write-Host ""
Write-Host "SPRING_R2DBC_URL = r2dbc:postgresql://${PG_FQDN}:5432/${PG_DB}?sslmode=require" -ForegroundColor White
Write-Host "SPRING_R2DBC_USERNAME = $PG_ADMIN" -ForegroundColor White
Write-Host "SPRING_R2DBC_PASSWORD = (el password que ingresaste)" -ForegroundColor White
Write-Host "APP_JWT_SECRET = (generar: openssl rand -base64 64)" -ForegroundColor White
Write-Host ""
Write-Host "Service Principal JSON:" -ForegroundColor Yellow
Write-Host $SP_JSON
Write-Host ""
Write-Host "DONE! Schema SQL ya ejecutado en Azure PostgreSQL." -ForegroundColor Green
Write-Host "  Servidor: $PG_FQDN" -ForegroundColor Green
