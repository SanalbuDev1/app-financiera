# =============================================
# Paso 6 — Crear Service Principal para GitHub Actions
# Permite al pipeline hacer deploy sin tu cuenta personal
# =============================================
$RESOURCE_GROUP = "rg-finanzas"

Write-Host "Creando Service Principal para GitHub Actions..." -ForegroundColor Cyan

$subscriptionId = az account show --query id -o tsv

$spJson = az ad sp create-for-rbac --name "sp-finanzas-github" --role contributor --scopes "/subscriptions/$subscriptionId/resourceGroups/$RESOURCE_GROUP" --sdk-auth

Write-Host ""
Write-Host "Service Principal creado. Este es el JSON para AZURE_CREDENTIALS:" -ForegroundColor Yellow
Write-Host ""
Write-Host $spJson -ForegroundColor Green
Write-Host ""
Write-Host "Copia TODO el JSON de arriba y guardalo como GitHub Secret: AZURE_CREDENTIALS" -ForegroundColor Yellow
