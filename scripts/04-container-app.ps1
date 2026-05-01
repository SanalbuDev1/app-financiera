# =============================================
# Paso 4 — Crear Container App (backend)
# Se crea con imagen placeholder; el pipeline la actualiza despues
# min-replicas 0 = se apaga sin trafico ($0 en idle)
# =============================================
$CONTAINER_APP = "finanzas-app"
$CONTAINER_ENV = "finanzas-env"
$RESOURCE_GROUP = "rg-finanzas"

Write-Host "Creando Container App: $CONTAINER_APP..." -ForegroundColor Cyan

az containerapp create --name $CONTAINER_APP --resource-group $RESOURCE_GROUP --environment $CONTAINER_ENV --image "mcr.microsoft.com/azuredocs/containerapps-helloworld:latest" --target-port 9000 --ingress external --min-replicas 0 --max-replicas 1 --cpu 0.25 --memory 0.5Gi

$backendUrl = az containerapp show --name $CONTAINER_APP --resource-group $RESOURCE_GROUP --query "properties.configuration.ingress.fqdn" -o tsv

Write-Host ""
Write-Host "Container App creada exitosamente." -ForegroundColor Green
Write-Host "  Backend URL: https://$backendUrl" -ForegroundColor Green
Write-Host ""
Write-Host "Guarda esta URL! Es la que usara el frontend para las llamadas API." -ForegroundColor Yellow
