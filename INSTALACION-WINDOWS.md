# MF Logistica - Instalacion en Windows

Esta guia describe la instalacion local en Windows. El lanzador abre la pantalla de acceso en `http://localhost:8080/login`.

## Requisitos minimos

Instala las siguientes aplicaciones:

1. **Windows 10 64 bits o posterior**.
2. **Docker Desktop for Windows** con WSL 2. En sus ajustes, activa **Settings > General > Start Docker Desktop when you sign in** para que Docker vuelva a iniciarse al entrar en Windows.
3. Al menos **4 GB de RAM disponible** para Docker.
4. El puerto **8080** disponible. PostgreSQL usa internamente `5432` dentro de Docker y no publica el puerto al equipo.

No necesitas instalar Java ni Maven.

## Primera instalacion

1. Instala Docker Desktop.
2. Abre Docker Desktop y espera al estado **Engine running**.
3. Recibe el paquete desde el desarrollador. El paquete es un archivo ZIP con el nombre `MF-Logistica-installer.zip`; dentro deben encontrarse `docker-compose.yml`, `Dockerfile`, `pom.xml`, la carpeta `src/`, `.env.example`, `start-local.bat`, `stop-local.bat` y `INSTALACION-WINDOWS.md`.
4. Haz clic derecho en `MF-Logistica-installer.zip` y selecciona **Extraer todo**.
5. Coloca la carpeta extraida en una ubicacion permanente sin espacios raros, por ejemplo:

```text
C:\MF_Logistica
```

6. Copia `.env.example` y llama a la copia `.env`.
7. Edita `.env` con valores propios:

```dotenv
POSTGRES_DB=mflogistica
POSTGRES_USER=mflogistica
POSTGRES_PASSWORD=CLAVE_POSTGRES_SEGURA
APP_PORT=8080
APP_ADMIN_USERNAME=mfadmin
APP_ADMIN_PASSWORD=CLAVE_ADMIN_MINIMO_16_CARACTERES
APP_ADMIN_FULL_NAME=Nombre de la administradora
```

La contraseña administrativa debe tener como minimo 16 caracteres.

8. Doble clic en `start-local.bat`.

El boton de inicio:

- Comprueba Docker.
- Comprueba que existe `.env`.
- Inicia PostgreSQL y la aplicacion (si ya estaban iniciados, no crea una segunda instancia).
- Reconstruye la aplicacion si cambio el codigo.
- Conserva el volumen PostgreSQL y los datos existentes.
- Abre `http://localhost:8080/login`.

## Crear botones faciles en el Escritorio

En el Explorador de archivos, abre `C:\MF_Logistica`. Haz clic derecho en `start-local.bat` y selecciona **Mostrar más opciones > Enviar a > Escritorio (crear acceso directo)**. Cambia el nombre a **Abrir MF Logistica**. Repite con `stop-local.bat` y nombra el acceso directo **Cerrar MF Logistica**.

Tambien puedes crear un favorito en el navegador para `http://localhost:8080/login`.

## Si ejecutas dos veces

Puedes hacer doble clic en **Abrir MF Logistica** varias veces. Docker Compose conserva un solo contenedor por servicio y no crea una segunda copia ni borra la base de datos. El navegador puede abrir otra pestaña.

## Que pasa al cerrar el navegador o reiniciar Windows

Cerrar la pestaña o el navegador **no detiene la aplicacion**. El servicio sigue ejecutandose en segundo plano. Al reiniciar Windows, Docker Desktop arranca al iniciar sesion (si activaste el ajuste indicado arriba) y Docker vuelve a iniciar los contenedores, siempre que no hubieras usado antes **Cerrar MF Logistica**. Si la detuviste manualmente, haz clic en **Abrir MF Logistica**. El navegador no se abre automaticamente al reiniciar: usa el favorito o el acceso directo.

## Cerrar la aplicacion

Haz doble clic en **Cerrar MF Logistica** en el Escritorio. Esto detiene la aplicacion y la base de datos, pero **conserva todas las facturas**. Para abrirla de nuevo, haz doble clic en **Abrir MF Logistica**.

El navegador puede tardar unos segundos. Si sigue sin cargar, abre PowerShell dentro de la carpeta y ejecuta:

```powershell
docker compose ps
docker compose logs --tail=200 app
```

## Si la aplicacion se detiene

Los contenedores tienen reinicio automatico dentro de Docker con `restart: unless-stopped`.

- Si la aplicacion falla, Docker intenta levantarla otra vez.
- Si Docker Desktop se reinicia, los contenedores vuelven a iniciarse.
- Si Docker Desktop estaba cerrado, abre Docker Desktop y ejecuta `start-local.bat`.
- Si detuviste la aplicacion con `stop-local.bat`, ejecuta `start-local.bat` para volver a iniciarla.

## Inicio manual

```powershell
cd C:\MF_Logistica
docker compose up -d --build --remove-orphans
```

## Parar sin borrar datos

```powershell
docker compose stop
```

Para eliminar contenedores sin borrar datos:

```powershell
docker compose down
```

No uses `docker compose down -v`. `-v` elimina permanentemente el volumen PostgreSQL.

## Actualizar

1. Haz una copia de seguridad.
2. Mantén el archivo `.env`.
3. Conserva la carpeta original o reemplaza los archivos de codigo sin borrar el volumen Docker.
4. Ejecuta `start-local.bat`.

El lanzador reemplaza el contenedor de la aplicacion y conserva PostgreSQL y sus datos.

## Copia de seguridad

Desde la carpeta del proyecto:

```powershell
docker exec mf_logistica_postgres pg_dump -U mflogistica -d mflogistica -F c -f /tmp/mflogistica.dump
docker cp mf_logistica_postgres:/tmp/mflogistica.dump .\mflogistica-backup.dump
```

Los nombres deben coincidir con `.env`.

## Restaurar una copia

```powershell
docker compose stop app
docker cp .\mflogistica-backup.dump mf_logistica_postgres:/tmp/mflogistica.dump
docker exec mf_logistica_postgres pg_restore --clean --if-exists -U mflogistica -d mflogistica -F c /tmp/mflogistica.dump
docker compose start app
```

## Reinicio completo de prueba

Solo para una base vacia deliberada:

```powershell
docker compose down -v
start-local.bat
```

Este comando borra todas las facturas. No lo uses sin copia de seguridad.

## Diagnostico

Los botones de inicio y parada guardan los eventos, errores y una captura de los registros recientes de la aplicacion en:

```text
C:\MF_Logistica\logs\mf-logistica.log
```

Para abrir el archivo, haz doble clic en `logs\mf-logistica.log` o ejecuta:

```powershell
notepad C:\MF_Logistica\logs\mf-logistica.log
```

Si la aplicacion sigue ejecutandose y quieres guardar inmediatamente sus mensajes mas recientes:

```powershell
cd C:\MF_Logistica
docker compose logs --no-color --timestamps --tail=300 app postgres >> logs\mf-logistica.log 2>&1
```

Adjunta `mf-logistica.log` al pedir ayuda. El archivo puede contener informacion operativa; revisalo antes de compartirlo y no compartas `.env`.

```powershell
docker compose ps
docker compose logs --tail=200 app
docker compose logs --tail=100 postgres
docker compose restart app
```
