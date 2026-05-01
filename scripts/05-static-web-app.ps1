# =============================================
# Paso 5 — Crear Static Web App (frontend Angular)
# Tier Free = $0/mes
# =============================================
$STATIC_WEB_APP = "finanzas-frontend"
$RESOURCE_GROUP = "rg-finanzas"
$LOCATION = "eastus2"

Write-Host "Creando Static Web App: $STATIC_WEB_APP..." -ForegroundColor Cyan

az staticwebapp create --name $STATIC_WEB_APP --resource-group $RESOURCE_GROUP --location $LOCATION --sku Free

$frontendUrl = az staticwebapp show --name $STATIC_WEB_APP --resource-group $RESOURCE_GROUP --query "defaultHostname" -o tsv

Write-Host ""
Write-Host "Static Web App creada exitosamente." -ForegroundColor Green
Write-Host "  Frontend URL: https://$frontendUrl" -ForegroundColor Green
Write-Host ""
Write-Host "Guarda esta URL! Es el dominio de tu frontend." -ForegroundColor Yellow
