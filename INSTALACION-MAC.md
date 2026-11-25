# MF Logistica - Instalacion en macOS

Esta guia describe la instalacion local en macOS. El lanzador abre la pantalla de acceso en `http://localhost:8080/login`.

## Que significa el paquete

El paquete no existe automaticamente. Es el archivo que debes crear en tu equipo de desarrollo y enviar a la usuaria. Debe contener el codigo fuente, Docker, la configuracion de ejemplo y el lanzador.

### Crear el paquete desde macOS

1. Abre Terminal.
2. Entra en la carpeta del proyecto:

```bash
cd /ruta/al/repositorio/mf-logistics
```

3. Crea el paquete sin datos locales ni archivos Git:

```bash
mkdir -p release/MF_Logistica
cp docker-compose.yml Dockerfile pom.xml .env.example start-local.command stop-local.command INSTALACION-MAC.md release/MF_Logistica/
cp -R src release/MF_Logistica/
cd release
zip -r MF-Logistica-installer.zip MF_Logistica
```

El archivo generado sera `release/MF-Logistica-installer.zip`.

Si estás usando Git Bash en Windows y `zip` no existe, desde la carpeta `release` usa el siguiente comando alternativo:

```bash
tar -a -c -f MF-Logistica-installer.zip MF_Logistica
```

También puedes volver a la raíz del proyecto y crear el ZIP desde PowerShell usando `tar.exe`. Este comando incluye archivos ocultos como `.env.example`:

```powershell
tar.exe -a -c -f release\MF-Logistica-installer.zip -C release MF_Logistica
```

### Crear el paquete desde Windows

Abre PowerShell en la carpeta del proyecto y ejecuta:

```powershell
$release = "release\MF_Logistica"
New-Item -ItemType Directory -Force $release
Copy-Item docker-compose.yml,Dockerfile,pom.xml,.env.example,start-local.command,stop-local.command,INSTALACION-MAC.md $release
Copy-Item -Recurse src $release
tar.exe -a -c -f release\MF-Logistica-installer.zip -C release MF_Logistica
```

Envía `MF-Logistica-installer.zip` por un canal seguro. No envíes `.env` ni respaldos con datos reales dentro del paquete inicial.

## Requisitos minimos

Instala las siguientes aplicaciones:

1. **macOS soportado por Docker Desktop**.
2. **Docker Desktop** para Apple Silicon o Intel segun el procesador. En sus ajustes, activa **Settings > General > Start Docker Desktop when you sign in** para que Docker vuelva a iniciarse al entrar en tu Mac.
3. Al menos **4 GB de RAM disponible** para Docker.
4. El puerto **8080** disponible. PostgreSQL usa internamente `5432` dentro de Docker y no publica el puerto al equipo.

No necesitas instalar Java ni Maven.

## Primera instalacion

1. Instala Docker Desktop.
2. Abre Docker Desktop y espera a que el motor este iniciado.
3. Recibe el paquete desde el desarrollador. El paquete es un archivo ZIP con el nombre `MF-Logistica-installer.zip`; dentro deben encontrarse `docker-compose.yml`, `Dockerfile`, `pom.xml`, la carpeta `src/`, `.env.example`, `start-local.command`, `stop-local.command` y `INSTALACION-MAC.md`.
4. Haz doble clic en `MF-Logistica-installer.zip` en Finder para descomprimirlo.
5. Coloca la carpeta resultante en una ubicacion permanente, por ejemplo:

```text
~/Aplicaciones/MF_Logistica
```

No ejecutes el programa directamente desde la carpeta temporal de Descargas; despues de descomprimir, conserva la carpeta completa.

6. Abre Terminal y comprueba los archivos ocultos de la carpeta:

```bash
cd ~/Aplicaciones/MF_Logistica
ls -la
```

Los nombres que empiezan por punto suelen estar ocultos en Finder. También puedes mostrarlos en Finder con **⌘ + Mayús + .**. Si `ls -la` muestra `.env.example`, crea la configuración con:

```bash
cp .env.example .env
```

Si `.env.example` no aparece incluso con `ls -la`, crea `.env` directamente:

```bash
cat > .env <<'EOF'
POSTGRES_DB=mflogistica
POSTGRES_USER=mflogistica
POSTGRES_PASSWORD=CAMBIA_POR_UNA_CLAVE_POSTGRES_SEGURA
APP_PORT=8080
APP_ADMIN_USERNAME=mfadmin
APP_ADMIN_PASSWORD=CAMBIA_POR_UNA_CLAVE_ADMIN_DE_16_CARACTERES
APP_ADMIN_FULL_NAME=Nombre de la administradora
EOF
```

7. Abre el archivo para editarlo y reemplaza los valores de ejemplo por claves propias:

```bash
open -e .env
```

La contraseña administrativa debe tener como minimo 16 caracteres.

8. Activa el permiso de ejecucion de los dos botones una sola vez:

```bash
cd ~/Aplicaciones/MF_Logistica
chmod +x start-local.command
chmod +x stop-local.command
```

9. Haz doble clic en `start-local.command`.

El boton de inicio:

- Comprueba Docker.
- Comprueba que existe `.env`.
- Inicia PostgreSQL y la aplicacion (si ya estaban iniciados, no crea una segunda instancia).
- Reconstruye la aplicacion si cambio el codigo.
- Conserva el volumen PostgreSQL y los datos existentes.
- Abre `http://localhost:8080/login`.

Si macOS bloquea el archivo, abre **Ajustes del Sistema > Privacidad y seguridad** y autoriza `start-local.command`.

## Crear botones faciles en el Escritorio

En Finder, abre `~/Aplicaciones/MF_Logistica`, selecciona `start-local.command` y pulsa **⌘ + L** para crear un alias. Arrastra el alias al Escritorio y cambia su nombre a **Abrir MF Logistica**. Repite con `stop-local.command` y nombra el alias **Cerrar MF Logistica**.

Si prefieres, crea un favorito en el navegador para `http://localhost:8080/login`.

## Si ejecutas dos veces

Puedes hacer doble clic en **Abrir MF Logistica** varias veces. Docker Compose conserva un solo contenedor por servicio y no crea una segunda copia ni borra la base de datos. El navegador puede abrir otra pestaña.

## Que pasa al cerrar el navegador o reiniciar el Mac

Cerrar la pestaña o el navegador **no detiene la aplicacion**. El servicio sigue ejecutandose en segundo plano. Al reiniciar el Mac, Docker Desktop arranca al iniciar sesion (si activaste el ajuste indicado arriba) y Docker vuelve a iniciar los contenedores, siempre que no hubieras usado antes **Cerrar MF Logistica**. Si la detuviste manualmente, haz clic en **Abrir MF Logistica**. El navegador no se abre automaticamente al reiniciar: usa el favorito o el acceso directo.

## Cerrar la aplicacion

Haz doble clic en **Cerrar MF Logistica** en el Escritorio. Esto detiene la aplicacion y la base de datos, pero **conserva todas las facturas**. Para abrirla de nuevo, haz doble clic en **Abrir MF Logistica**.

El navegador puede tardar unos segundos. Si sigue sin cargar, abre Terminal dentro de la carpeta y ejecuta:

```bash
docker compose ps
docker compose logs --tail=200 app
```

## Si la aplicacion se detiene

Los contenedores tienen reinicio automatico dentro de Docker con `restart: unless-stopped`.

- Si la aplicacion falla, Docker intenta levantarla otra vez.
- Si Docker Desktop se reinicia, los contenedores vuelven a iniciarse.
- Si Docker Desktop estaba cerrado, abre Docker Desktop y ejecuta `start-local.command`.
- Si detuviste la aplicacion con `stop-local.command`, ejecuta `start-local.command` para volver a iniciarla.

## Inicio manual

```bash
cd ~/Aplicaciones/MF_Logistica
docker compose up -d --build --remove-orphans
```

## Parar sin borrar datos

```bash
docker compose stop
```

Para eliminar contenedores sin borrar datos:

```bash
docker compose down
```

No uses `docker compose down -v`. `-v` elimina permanentemente el volumen PostgreSQL.

## Actualizar

1. Haz una copia de seguridad.
2. Conserva el archivo `.env`.
3. Conserva la carpeta original o reemplaza los archivos de codigo sin borrar el volumen Docker.
4. Ejecuta `start-local.command`.

El lanzador reemplaza el contenedor de la aplicacion y conserva PostgreSQL y sus datos.

## Copia de seguridad

Desde la carpeta del proyecto:

```bash
docker exec mf_logistica_postgres pg_dump -U mflogistica -d mflogistica -F c -f /tmp/mflogistica.dump
docker cp mf_logistica_postgres:/tmp/mflogistica.dump ./mflogistica-backup.dump
```

Los nombres deben coincidir con `.env`.

## Restaurar una copia

```bash
docker compose stop app
docker cp ./mflogistica-backup.dump mf_logistica_postgres:/tmp/mflogistica.dump
docker exec mf_logistica_postgres pg_restore --clean --if-exists -U mflogistica -d mflogistica -F c /tmp/mflogistica.dump
docker compose start app
```

## Reinicio completo de prueba

Solo para una base vacia deliberada:

```bash
docker compose down -v
./start-local.command
```

Este comando borra todas las facturas. No lo uses sin copia de seguridad.

## Diagnostico

Los botones de inicio y parada guardan los eventos, errores y una captura de los registros recientes de la aplicacion en:

```text
~/Aplicaciones/MF_Logistica/logs/mf-logistica.log
```

Para abrir el archivo, haz doble clic en `logs/mf-logistica.log` o ejecuta:

```bash
open -e ~/Aplicaciones/MF_Logistica/logs/mf-logistica.log
```

Si la aplicacion sigue ejecutandose y quieres guardar inmediatamente sus mensajes mas recientes:

```bash
cd ~/Aplicaciones/MF_Logistica
docker compose logs --no-color --timestamps --tail=300 app postgres >> logs/mf-logistica.log 2>&1
```

Adjunta `mf-logistica.log` al pedir ayuda. El archivo puede contener informacion operativa; revisalo antes de compartirlo y no compartas `.env`.

```bash
docker compose ps
docker compose logs --tail=200 app
docker compose logs --tail=100 postgres
docker compose restart app
```
