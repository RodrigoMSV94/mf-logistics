#!/bin/sh
cd "$(dirname "$0")" || exit 1

mkdir -p logs
LOG_FILE="logs/mf-logistica.log"

log() {
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S %z')" "$1" | tee -a "$LOG_FILE"
}

log "Solicitud de inicio."

if ! docker compose version >/dev/null 2>&1; then
  log "ERROR: Docker Compose no está disponible."
  osascript -e 'display dialog "Docker Desktop no está disponible. Instálalo e inícialo antes de abrir MF Logistica." buttons {"Cerrar"} default button "Cerrar" with icon caution'
  exit 1
fi

if [ ! -f ".env" ]; then
  log "ERROR: Falta el archivo .env."
  osascript -e 'display dialog "Falta el archivo .env. Consulta la guía de instalación para crearlo." buttons {"Cerrar"} default button "Cerrar" with icon caution'
  exit 1
fi

if ! docker info >/dev/null 2>>"$LOG_FILE"; then
  log "ERROR: El motor de Docker no está iniciado."
  osascript -e 'display dialog "Docker Desktop está instalado, pero no está iniciado. Abre Docker Desktop y vuelve a intentar. Revisa logs/mf-logistica.log si continúa el problema." buttons {"Cerrar"} default button "Cerrar" with icon caution'
  exit 1
fi

log "Iniciando servicios Docker."
if ! docker compose up -d --build --remove-orphans >>"$LOG_FILE" 2>&1; then
  log "ERROR: No se pudieron iniciar los servicios."
  docker compose ps --all >>"$LOG_FILE" 2>&1
  docker compose logs --no-color --timestamps --tail=200 app postgres >>"$LOG_FILE" 2>&1
  osascript -e 'display dialog "No se pudo iniciar MF Logistica. Revisa el archivo logs/mf-logistica.log dentro de la carpeta de la aplicación." buttons {"Cerrar"} default button "Cerrar" with icon caution'
  exit 1
fi

log "Esperando a que la aplicación responda."
attempt=0
until curl --fail --silent --output /dev/null http://localhost:8080/login; do
  attempt=$((attempt + 1))
  if [ "$attempt" -ge 30 ]; then
    log "ERROR: La aplicación no respondió en http://localhost:8080/login después de 60 segundos."
    docker compose ps --all >>"$LOG_FILE" 2>&1
    docker compose logs --no-color --timestamps --tail=200 app postgres >>"$LOG_FILE" 2>&1
    osascript -e 'display dialog "MF Logistica no respondió. Revisa logs/mf-logistica.log dentro de la carpeta de la aplicación." buttons {"Cerrar"} default button "Cerrar" with icon caution'
    exit 1
  fi
  sleep 2
done

docker compose ps --all >>"$LOG_FILE" 2>&1
docker compose logs --no-color --timestamps --tail=100 app postgres >>"$LOG_FILE" 2>&1
log "Aplicación disponible en http://localhost:8080/login."
open "http://localhost:8080/login"
