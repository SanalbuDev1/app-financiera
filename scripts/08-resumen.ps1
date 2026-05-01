# =============================================
# Resumen — Muestra todos los valores para GitHub Secrets
# Ejecutar al final para verificar que todo esta creado
# =============================================
$RESOURCE_GROUP = "rg-finanzas"
$ACR_NAME = "finanzasacr"
$CONTAINER_APP = "finanzas-app"
$STATIC_WEB_APP = "finanzas-frontend"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Verificando recursos creados" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$acrServer = az acr show --name $ACR_NAME --query loginServer -o tsv 2>$null
$acrUser = az acr credential show --name $ACR_NAME --query username -o tsv 2>$null
$acrPass = az acr credential show --name $ACR_NAME --query "passwords[0].value" -o tsv 2>$null
$backendUrl = az containerapp show --name $CONTAINER_APP --resource-group $RESOURCE_GROUP --query "properties.configuration.ingress.fqdn" -o tsv 2>$null
$frontendUrl = az staticwebapp show --name $STATIC_WEB_APP --resource-group $RESOURCE_GROUP --query "defaultHostname" -o tsv 2>$null

Write-Host "========================================" -ForegroundColor Yellow
Write-Host " GitHub Secrets necesarios:" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "--- Azure ---" -ForegroundColor Cyan
Write-Host "AZURE_CREDENTIALS          = (JSON del paso 06)" -ForegroundColor White
Write-Host "ACR_LOGIN_SERVER           = $acrServer" -ForegroundColor White
Write-Host "ACR_USERNAME               = $acrUser" -ForegroundColor White
Write-Host "ACR_PASSWORD               = $acrPass" -ForegroundColor White
Write-Host "AZURE_RESOURCE_GROUP       = $RESOURCE_GROUP" -ForegroundColor White
Write-Host "AZURE_CONTAINER_APP_NAME   = $CONTAINER_APP" -ForegroundColor White
Write-Host ""
Write-Host "--- Azure PostgreSQL ---" -ForegroundColor Cyan
$pgFqdn = az postgres flexible-server show --name "finanzas-pgserver" --resource-group $RESOURCE_GROUP --query fullyQualifiedDomainName -o tsv 2>$null
Write-Host "SPRING_R2DBC_URL           = r2dbc:postgresql://${pgFqdn}:5432/finanzas_db?sslmode=require" -ForegroundColor White
Write-Host "SPRING_R2DBC_USERNAME      = finanzasadmin" -ForegroundColor White
Write-Host "SPRING_R2DBC_PASSWORD      = (el password que configuraste)" -ForegroundColor White
Write-Host ""
Write-Host "--- App ---" -ForegroundColor Cyan
Write-Host "APP_JWT_SECRET             = (generar con: openssl rand -base64 64)" -ForegroundColor White
Write-Host "APP_CORS_ALLOWED_ORIGINS   = https://$frontendUrl" -ForegroundColor White
Write-Host ""
Write-Host "--- URLs finales ---" -ForegroundColor Cyan
Write-Host "Backend:  https://$backendUrl" -ForegroundColor Green
Write-Host "Frontend: https://$frontendUrl" -ForegroundColor Green
