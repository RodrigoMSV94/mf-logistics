#!/bin/sh
cd "$(dirname "$0")" || exit 1

mkdir -p logs
LOG_FILE="logs/mf-logistica.log"

log() {
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S %z')" "$1" | tee -a "$LOG_FILE"
}

log "Solicitud de parada."

if ! docker compose version >/dev/null 2>&1; then
  log "ERROR: Docker Compose no está disponible."
  osascript -e 'display dialog "Docker Desktop no está disponible. Inícialo antes de detener MF Logistica." buttons {"Cerrar"} default button "Cerrar" with icon caution'
  exit 1
fi

if ! docker info >/dev/null 2>>"$LOG_FILE"; then
  log "ERROR: El motor de Docker no está iniciado."
  osascript -e 'display dialog "Docker Desktop no está iniciado. Abre Docker Desktop para poder detener MF Logistica." buttons {"Cerrar"} default button "Cerrar" with icon caution'
  exit 1
fi

docker compose logs --no-color --timestamps --tail=200 app postgres >>"$LOG_FILE" 2>&1
if docker compose stop >>"$LOG_FILE" 2>&1; then
  log "MF Logistica detenida; los datos se conservaron."
  osascript -e 'display dialog "MF Logistica está detenida. Las facturas y los datos se conservaron. Haz doble clic en start-local.command para volver a abrirla." buttons {"Aceptar"} default button "Aceptar" with icon note'
else
  log "ERROR: No se pudo detener MF Logistica."
  docker compose ps --all >>"$LOG_FILE" 2>&1
  osascript -e 'display dialog "No se pudo detener MF Logistica. Revisa el archivo logs/mf-logistica.log." buttons {"Cerrar"} default button "Cerrar" with icon caution'
  exit 1
fi
