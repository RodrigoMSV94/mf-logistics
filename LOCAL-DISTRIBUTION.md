# Instalacion local para Windows y macOS

Esta guia entrega una ejecucion sencilla en `http://localhost:8080/login`. La aplicacion y PostgreSQL se ejecutan en Docker Desktop. PostgreSQL y sus datos permanecen en un volumen Docker local; reinstalar o actualizar la aplicacion no elimina ese volumen.

Para instrucciones de uso de un clic, acceso directo de escritorio, reinicio automatico y parada segura, consulta [INSTALACION-WINDOWS.md](INSTALACION-WINDOWS.md) o [INSTALACION-MAC.md](INSTALACION-MAC.md).

## 1. Requisitos

Instala Docker Desktop en Windows o macOS y mantenlo ejecutandose.

- Windows: habilita WSL 2 durante la instalacion.
- macOS: usa Docker Desktop para Apple Silicon o Intel segun el procesador.
- Puertos disponibles: `8080` para la aplicacion y `5432` no expuesto al exterior.

No es necesario instalar Java ni Maven en el equipo de la usuaria.

## 2. Primera entrega

### Desde el equipo de desarrollo

1. Crea una carpeta de entrega limpia:

```powershell
mkdir release-mf-logistica
```

2. Copia dentro al menos estos elementos:

```text
.env.example
docker-compose.yml
Dockerfile
pom.xml
src/
start-local.bat
start-local.command
stop-local.bat
stop-local.command
```

3. En esa carpeta copia `.env.example` a `.env` y cambia todas las claves:

```dotenv
POSTGRES_DB=mflogistica
POSTGRES_USER=mflogistica
POSTGRES_PASSWORD=CLAVE_POSTGRES_LARGA_Y_UNICA
APP_PORT=8080
APP_ADMIN_USERNAME=mfadmin
APP_ADMIN_PASSWORD=CLAVE_ADMIN_LARGA_DE_16_O_MAS
APP_ADMIN_FULL_NAME=Nombre de la administradora
```

No uses el archivo `.env` para entregar la misma contraseña a varias instalaciones si no tienes una clave compartida. Crea una `.env` distinta por equipo.

4. Comprueba el paquete desde una carpeta temporal o la propia maquina de desarrollo:

```powershell
docker compose up -d --build
```

Abre `http://localhost:8080/login`, inicia sesion y confirma que el listado de facturas esta vacio.

5. Comprueba y comprime la entrega, excluyendo datos locales:

```powershell
docker compose down
```

Comprime los archivos fuente sin el volumen Docker, `target/`, `.git/`, `.env` si contiene datos secretos o mantenlo cifrado y entregalo por un canal seguro.

### En Windows

1. Instala Docker Desktop e inicia Docker.
2. Descomprime el paquete.
3. Copia `.env.example` como `.env` y configura sus claves.
4. Ejecuta `start-local.bat`.
5. Abre `http://localhost:8080/login`.
6. Usa `stop-local.bat` para detener sin borrar la base de datos.

### En macOS

1. Instala Docker Desktop e inicia Docker.
2. Descomprime el paquete.
3. Copia `.env.example` como `.env` y configura sus claves.
4. Haz ejecutable el lanzador una sola vez:

```bash
chmod +x start-local.command
chmod +x stop-local.command
```

5. Doble clic en `start-local.command`.
6. Si macOS pide permiso, autoriza el archivo desde Ajustes del Sistema > Privacidad y seguridad.
7. Usa `stop-local.command` para detener sin borrar la base de datos.

## 3. Estado inicial

En una base nueva:

- Las facturas empiezan vacias.
- Se crea la cuenta administradora configurada en `.env`.
- Se crean los datos fiscales por defecto para ser revisados en **Datos fiscales**.
- Liquibase crea las tablas necesarias.
- El reinicio de datos de muestra esta desactivado.

No ejecutes `APP_LOCAL_DATA_RESET_ENABLED=true` en la instalacion de la usuaria.

## 4. Actualizar sin perder datos

### Con el paquete fuente actualizado

Entrega una carpeta nueva o reemplaza `pom.xml`, `Dockerfile`, `docker-compose.yml` y `src/`. Conserva en el equipo la `.env` existente y no borres el volumen Docker.

Luego ejecuta:

**Windows**

```powershell
docker compose up -d --build
```

**macOS/Linux**

```bash
docker compose up -d --build
```

No uses `docker compose down -v`: el sufijo `-v` elimina el volumen PostgreSQL y todas las facturas.

Los lanzadores escriben un log local en `logs/mf-logistica.log`, con eventos de inicio/parada y registros recientes de Docker. Revísalo antes de compartirlo y nunca compartas `.env`.

### Con una imagen ya construida

Si prefieres entregar menos archivos, exporta la imagen desde tu equipo:

```powershell
docker build -t mf-logistica:VERSION .
docker save -o mf-logistica-VERSION.tar mf-logistica:VERSION
docker compose config
```

El paquete debe contener ademas `.env`, `docker-compose.yml` y una entrada en Compose que use esa imagen. Importa e inicia en la maquina de la usuaria:

```powershell
docker load -i mf-logistica-VERSION.tar
docker compose up -d
```

El volumen PostgreSQL sigue siendo el mismo si no se elimina.

## 5. Copias de seguridad

Antes de una actualizacion importante, crea una copia y verifica que puede restaurarse:

```powershell
docker exec mf_logistica_postgres pg_dump -U mflogistica -d mflogistica -F c -f /tmp/mflogistica.dump
docker cp mf_logistica_postgres:/tmp/mflogistica.dump ./mflogistica-backup.dump
```

El nombre de usuario y la base deben coincidir con `.env`.

Para restaurar, detén primero la aplicación:

```powershell
docker compose stop app
docker cp ./mflogistica-backup.dump mf_logistica_postgres:/tmp/mflogistica.dump
docker exec mf_logistica_postgres pg_restore --clean --if-exists -U mflogistica -d mflogistica -F c /tmp/mflogistica.dump
docker compose start app
```

## 6. Inicio, parada y diagnostico

Iniciar:

```powershell
docker compose up -d
```

Parar sin borrar datos:

```powershell
docker compose stop
```

Eliminar contenedores sin eliminar datos:

```powershell
docker compose down
```

Ver estado:

```powershell
docker compose ps
```

Ver errores:

```powershell
docker compose logs --tail=200 app
docker compose logs --tail=100 postgres
```

Reiniciar la aplicacion:

```powershell
docker compose restart app
```

Si `localhost:8080` no responde, confirma primero que Docker Desktop esta iniciado y espera que el contenedor de PostgreSQL quede sano.

## 7. Reinicio completo solo para pruebas

Este procedimiento borra todas las facturas y datos guardados. No lo uses en produccion local sin una copia de seguridad.

```powershell
docker compose down -v
docker compose up -d --build
```
