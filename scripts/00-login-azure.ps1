# =============================================
# Paso 0 — Login en Azure
# Ejecutar ANTES de cualquier otro script
# =============================================
Write-Host "Iniciando login en Azure..." -ForegroundColor Cyan
Write-Host "Se abrira el navegador para autenticarte." -ForegroundColor Yellow
Write-Host ""

az login --scope https://management.core.windows.net//.default

Write-Host ""
Write-Host "Login exitoso. Cuenta activa:" -ForegroundColor Green
az account show --query "{Nombre: name, ID: id, Tenant: tenantId}" -o table
