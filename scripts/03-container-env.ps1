# =============================================
# Paso 3 — Crear Container Apps Environment
# Es el "entorno" donde correra el backend
# =============================================
$CONTAINER_ENV = "finanzas-env"
$RESOURCE_GROUP = "rg-finanzas"
$LOCATION = "eastus"

Write-Host "Creando Container Apps Environment: $CONTAINER_ENV..." -ForegroundColor Cyan
Write-Host "Esto puede tardar 1-2 minutos..." -ForegroundColor Yellow

az containerapp env create --name $CONTAINER_ENV --resource-group $RESOURCE_GROUP --location $LOCATION

Write-Host ""
Write-Host "Container Apps Environment creado exitosamente." -ForegroundColor Green
