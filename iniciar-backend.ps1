# Inicia todo el backend de Rincón Sur: Eureka, los 4 servicios y el gateway.
# Uso (desde esta carpeta):  powershell -ExecutionPolicy Bypass -File .\iniciar-backend.ps1
# Agrega -Compilar para recompilar los .jar antes de iniciar.
# Requisitos: Java 21 y MySQL corriendo en localhost:3306 (usuario root sin clave).
param([switch]$Compilar)

$ErrorActionPreference = 'Stop'
$raiz = $PSScriptRoot
$servicios = 'roles-service', 'usuarios-service', 'probadores-service', 'turnos-service'

function Test-Puerto([int]$puerto) {
    [bool](Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue)
}

function Get-Jar([string]$modulo) {
    $jar = Get-ChildItem (Join-Path $raiz "$modulo\target") -Filter "$modulo-*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notlike '*.original' } | Select-Object -First 1
    if ($Compilar -or -not $jar) {
        Write-Host "Compilando $modulo..." -ForegroundColor Cyan
        Push-Location (Join-Path $raiz $modulo)
        try { & .\mvnw.cmd -q -DskipTests package; if ($LASTEXITCODE -ne 0) { throw "Falló la compilación de $modulo" } }
        finally { Pop-Location }
        $jar = Get-ChildItem (Join-Path $raiz "$modulo\target") -Filter "$modulo-*.jar" | Select-Object -First 1
    }
    $jar.FullName
}

function Start-Modulo([string]$modulo, [string]$extra = '') {
    $jar = Get-Jar $modulo
    $dir = Join-Path $raiz $modulo
    # Cada servicio se abre en su propia ventana; se ejecuta desde su carpeta para que usuarios-service lea su .env
    Start-Process powershell -WorkingDirectory $dir -ArgumentList '-NoExit', '-Command',
        "`$Host.UI.RawUI.WindowTitle = '$modulo'; java -jar '$jar' $extra"
    Write-Host "Iniciando $modulo" -ForegroundColor Green
}

if (-not (Test-Puerto 3306)) { Write-Warning 'MySQL no está corriendo en el puerto 3306. Inícialo (por ejemplo desde Laragon) y vuelve a ejecutar.'; exit 1 }
if (-not (Test-Path (Join-Path $raiz 'usuarios-service\.env'))) { Write-Warning 'Falta usuarios-service\.env: el backend funciona, pero no enviará correos. Ver README.md.' }

if (Test-Puerto 8761) { Write-Host 'Eureka ya está corriendo.' } else {
    Start-Modulo 'eureka-server'
    Write-Host 'Esperando a Eureka...'
    while (-not (Test-Puerto 8761)) { Start-Sleep 2 }
}

$puertos = @{ 'roles-service' = 8081; 'usuarios-service' = 8082; 'probadores-service' = 8083; 'turnos-service' = 8084 }
foreach ($s in $servicios) {
    if (Test-Puerto $puertos[$s]) { Write-Warning "El puerto $($puertos[$s]) ya está en uso; no se inicia $s." } else { Start-Modulo $s }
}

# El gateway usa 8080; si está ocupado (por ejemplo por Oracle) usa 8090
$puertoGateway = if (Test-Puerto 8080) { 8090 } else { 8080 }
Start-Modulo 'gateway' "--server.port=$puertoGateway"

Write-Host ''
Write-Host 'Listo. Los servicios tardan entre 30 y 60 segundos en quedar disponibles.' -ForegroundColor Yellow
Write-Host "  Eureka:  http://localhost:8761"
Write-Host "  Gateway: http://localhost:$puertoGateway/swagger-ui.html"
Write-Host '  Servicios: 8081 roles, 8082 usuarios, 8083 probadores, 8084 turnos'
Write-Host 'Para detenerlos, cierra sus ventanas.'
