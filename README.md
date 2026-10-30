# MF Logistica

Aplicacion web MVC para landing publica, acceso interno de socios y panel de facturacion con generacion de PDF.

## Stack

- Java 21
- Spring Boot 3.3.5
- Spring MVC + Thymeleaf
- Spring Security
- Spring Data JPA
- Liquibase para cambios y datos iniciales versionados
- PostgreSQL (Neon compatible)
- OpenPDF

## Estructura

- `src/main/java/com/mflogistica/domain`: modelos y excepciones de negocio
- `src/main/java/com/mflogistica/application`: DTOs, puertos y servicios de casos de uso
- `src/main/java/com/mflogistica/infrastructure`: persistencia, seguridad y adaptadores externos
- `src/main/java/com/mflogistica/web`: controladores MVC y manejo global de errores
- `src/main/resources/templates`: vistas Thymeleaf (`index.html`, `login.html`, `dashboard.html`, `facturas.html`)

## Facturación

Desde el panel se pueden crear y editar facturas, seleccionar clientes guardados y consultar el listado paginado con búsqueda por número, cliente o NIF y filtros de fecha. El número de factura es único y se valida tanto en la aplicación como en PostgreSQL. Liquibase crea la tabla de clientes e inserta el cliente de ejemplo de forma idempotente.

Al arrancar con la base de datos PostgreSQL local (`localhost`, `127.0.0.1` o `::1`), se eliminan las facturas y sus líneas y se crea una factura de ejemplo para que el cliente de muestra aparezca en el selector. Se conserva el usuario administrador. Este reinicio no se ejecuta con una base de datos remota; se puede desactivar con `APP_LOCAL_DATA_RESET_ENABLED=false`.

## Variables de entorno

- `PORT` (default `8080`)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_ADMIN_EMAIL`
- `APP_ADMIN_PASSWORD`
- `APP_ADMIN_FULL_NAME`

## Ejecucion local

```powershell
mvn clean package
mvn spring-boot:run
```

## Docker (Render)

```powershell
docker build -t mf-logistica .
docker run --rm -p 8080:8080 mf-logistica
```
