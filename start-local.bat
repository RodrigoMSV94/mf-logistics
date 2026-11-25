@echo off
setlocal
cd /d "%~dp0"
if not exist logs mkdir logs
set "LOG_FILE=logs\mf-logistica.log"
echo [%date% %time%] Solicitud de inicio.>>"%LOG_FILE%"

docker compose version >nul 2>&1
if errorlevel 1 (
  echo [%date% %time%] ERROR: Docker Compose no esta disponible.>>"%LOG_FILE%"
  echo Docker Desktop no esta disponible. Instala Docker Desktop e inicia Docker Desktop.
  pause
  exit /b 1
)

if not exist ".env" (
  echo [%date% %time%] ERROR: Falta el archivo .env.>>"%LOG_FILE%"
  echo Falta el archivo .env.
  echo Copia .env.example como .env y configura las claves antes de ejecutar esta aplicacion.
  pause
  exit /b 1
)

docker info >nul 2>>"%LOG_FILE%"
if errorlevel 1 (
  echo [%date% %time%] ERROR: El motor Docker no esta iniciado.>>"%LOG_FILE%"
  echo Docker Desktop esta instalado, pero el motor Docker no esta iniciado.
  echo Abre Docker Desktop, espera a que muestre "Engine running" y vuelve a ejecutar este archivo.
  pause
  exit /b 1
)

echo Iniciando MF Logistica...
echo [%date% %time%] Iniciando servicios Docker.>>"%LOG_FILE%"
docker compose up -d --build --remove-orphans >>"%LOG_FILE%" 2>&1
if errorlevel 1 (
  echo [%date% %time%] ERROR: No se pudieron iniciar los servicios.>>"%LOG_FILE%"
  docker compose ps --all >>"%LOG_FILE%" 2>&1
  docker compose logs --no-color --timestamps --tail=200 app postgres >>"%LOG_FILE%" 2>&1
  echo No se pudo iniciar MF Logistica.
  echo Revisa el archivo logs\mf-logistica.log.
  pause
  exit /b 1
)

echo Esperando a que PostgreSQL y la aplicacion esten disponibles...
set /a ATTEMPT=0
:wait_for_app
curl.exe --fail --silent --output NUL http://localhost:8080/login
if not errorlevel 1 goto app_ready
set /a ATTEMPT+=1
if %ATTEMPT% GEQ 30 goto app_timeout
timeout /t 2 /nobreak >nul
goto wait_for_app

:app_timeout
echo [%date% %time%] ERROR: La aplicacion no respondio despues de 60 segundos.>>"%LOG_FILE%"
docker compose ps --all >>"%LOG_FILE%" 2>&1
docker compose logs --no-color --timestamps --tail=200 app postgres >>"%LOG_FILE%" 2>&1
echo MF Logistica no respondio. Revisa logs\mf-logistica.log.
pause
exit /b 1

:app_ready
docker compose ps --all >>"%LOG_FILE%" 2>&1
docker compose logs --no-color --timestamps --tail=100 app postgres >>"%LOG_FILE%" 2>&1
echo [%date% %time%] Aplicacion disponible en http://localhost:8080/login.>>"%LOG_FILE%"

docker compose ps
start "" "http://localhost:8080/login"
