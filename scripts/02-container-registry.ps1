# =============================================
# Paso 2 — Crear Azure Container Registry
# Almacena las imagenes Docker del backend
# =============================================
$ACR_NAME = "finanzasacr"
$RESOURCE_GROUP = "rg-finanzas"

Write-Host "Creando Container Registry: $ACR_NAME..." -ForegroundColor Cyan

az acr create --name $ACR_NAME --resource-group $RESOURCE_GROUP --sku Basic --admin-enabled true

Write-Host ""
Write-Host "Credenciales del ACR (guardar para GitHub Secrets):" -ForegroundColor Yellow
Write-Host ""

$server = az acr show --name $ACR_NAME --resource-group $RESOURCE_GROUP --query loginServer -o tsv
$username = az acr credential show --name $ACR_NAME --resource-group $RESOURCE_GROUP --query username -o tsv
$password = az acr credential show --name $ACR_NAME --resource-group $RESOURCE_GROUP --query "passwords[0].value" -o tsv

Write-Host "  ACR_LOGIN_SERVER = $server" -ForegroundColor Green
Write-Host "  ACR_USERNAME     = $username" -ForegroundColor Green
Write-Host "  ACR_PASSWORD     = $password" -ForegroundColor Green
Write-Host ""
Write-Host "Guarda estos 3 valores! Los necesitas para GitHub Secrets." -ForegroundColor Yellow
