# =============================================
# Paso 1 — Crear Resource Group
# Es la "carpeta" que agrupa todos los recursos de Azure
# =============================================
$RESOURCE_GROUP = "rg-finanzas"
$LOCATION = "eastus"

Write-Host "Creando Resource Group: $RESOURCE_GROUP en $LOCATION..." -ForegroundColor Cyan

az group create --name $RESOURCE_GROUP --location $LOCATION

Write-Host ""
Write-Host "Resource Group creado exitosamente." -ForegroundColor Green
