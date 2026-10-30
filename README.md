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
- `src/main/resources/templates`: vistas Thymeleaf (`index.html`, `login.html`, `dashboard.html`, `facturas.html`, `datos-fiscales.html`)

## Facturación

Desde el panel se pueden crear y editar facturas, seleccionar clientes guardados y consultar el listado paginado con búsqueda por número, cliente o NIF y filtros de fecha. El número de factura es único y se valida tanto en la aplicación como en PostgreSQL. Los datos del emisor se modifican desde **Datos fiscales**. Cada factura conserva una copia de los datos fiscales vigentes al crearla; los PDFs de facturas nuevas reflejan la configuración nueva, mientras que editar o volver a descargar una factura anterior conserva sus datos originales. Liquibase crea las tablas de clientes y datos fiscales, inserta sus valores iniciales y migra los datos de facturas/usuarios existentes. Los datos del cliente se guardan en el catálogo al guardar una factura y quedan disponibles en el selector para facturas futuras.

El acceso se realiza con nombre de usuario. Por defecto, el usuario inicial es `mfadmin` y la contraseña de desarrollo es `Admin1234!`; ambos pueden configurarse con `APP_ADMIN_USERNAME` y `APP_ADMIN_PASSWORD`. El inicializador no cambia la contraseña de una cuenta que ya exista. En cualquier despliegue debe configurarse una contraseña segura y única.

Al arrancar con la base de datos PostgreSQL local (`localhost`, `127.0.0.1` o `::1`), se eliminan las facturas y sus líneas y se crea una factura de ejemplo para que el cliente de muestra aparezca en el selector. Se conserva el usuario administrador. Este reinicio no se ejecuta con una base de datos remota; se puede desactivar con `APP_LOCAL_DATA_RESET_ENABLED=false`.

## Variables de entorno

- `PORT` (default `8080`)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_ADMIN_USERNAME` (por defecto `mfadmin`)
- `APP_ADMIN_PASSWORD`
- `APP_ADMIN_FULL_NAME`
- `APP_LOCAL_DATA_RESET_ENABLED` (default `true`; el reinicio de facturas de muestra solo se ejecuta cuando la base de datos es local)

## Ejecucion local

```powershell
mvn clean package
mvn spring-boot:run
```

## Pruebas

```powershell
mvn test
```

## Docker (Render)

```powershell
docker build -t mf-logistica .
docker run --rm -p 8080:8080 mf-logistica
```

El Dockerfile compila con Maven y Java 21 y ejecuta la aplicación con Java 21 JRE. No hay `docker-compose.yml` en el repositorio. Al ejecutar el contenedor, configura las variables de entorno de PostgreSQL y credenciales de administrador; la contraseña predeterminada es solo para desarrollo. Las migraciones Liquibase se aplican al iniciar la aplicación.
