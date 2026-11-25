@echo off
setlocal
cd /d "%~dp0"
if not exist logs mkdir logs
set "LOG_FILE=logs\mf-logistica.log"
echo [%date% %time%] Solicitud de parada.>>"%LOG_FILE%"

docker compose version >nul 2>&1
if errorlevel 1 (
  echo [%date% %time%] ERROR: Docker Compose no esta disponible.>>"%LOG_FILE%"
  echo Docker Desktop no esta disponible.
  pause
  exit /b 1
)

docker info >nul 2>>"%LOG_FILE%"
if errorlevel 1 (
  echo [%date% %time%] ERROR: El motor Docker no esta iniciado.>>"%LOG_FILE%"
  echo Inicia Docker Desktop antes de detener MF Logistica.
  pause
  exit /b 1
)

docker compose logs --no-color --timestamps --tail=200 app postgres >>"%LOG_FILE%" 2>&1
docker compose stop >>"%LOG_FILE%" 2>&1
if errorlevel 1 (
  echo [%date% %time%] ERROR: No se pudo detener MF Logistica.>>"%LOG_FILE%"
  docker compose ps --all >>"%LOG_FILE%" 2>&1
  echo No se pudo detener MF Logistica.
  echo Revisa el archivo logs\mf-logistica.log.
  pause
  exit /b 1
)

echo [%date% %time%] MF Logistica detenida; los datos se conservaron.>>"%LOG_FILE%"
echo MF Logistica esta detenida. Los datos se conservaron.
pause
