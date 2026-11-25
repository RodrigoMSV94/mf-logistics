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
- `src/main/resources/templates`: vistas Thymeleaf (`index.html`, `login.html`, `dashboard.html`, `facturas.html`, `datos-fiscales.html`, `security-settings.html`)

## Facturación

Desde el panel se pueden crear y editar facturas, seleccionar clientes guardados y consultar el listado paginado con búsqueda por número, cliente o NIF y filtros de fecha. Cada línea puede marcarse como incluida en la base imponible o fuera de ella; por ejemplo, una penalización externa se suma al total sin modificar el IVA ni el IRPF calculados sobre la base. El listado consulta proyecciones ligeras (sin cargar las líneas de cada factura) para evitar consultas N+1. El número de factura es único y se valida tanto en la aplicación como en PostgreSQL. Los datos del emisor se modifican desde **Datos fiscales**. Cada factura conserva una copia de los datos fiscales vigentes al crearla; los PDFs de facturas nuevas reflejan la configuración nueva, mientras que editar o volver a descargar una factura anterior conserva sus datos originales. Liquibase crea el esquema inicial y los datos de configuración de una base vacía; Hibernate valida el esquema sin modificarlo (`ddl-auto: validate`). Para cualquier cambio futuro del esquema se añadirá un nuevo changeset Liquibase.

El acceso se realiza con nombre de usuario. La cuenta inicial se configura obligatoriamente con `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD` y `APP_ADMIN_FULL_NAME`; no hay credenciales administrativas predeterminadas. La contraseña inicial debe tener al menos 16 caracteres. El inicializador no cambia la contraseña de una cuenta existente. Las contraseñas se almacenan con BCrypt. Un administrador puede cambiarlas desde **Seguridad**; la operación solicita la contraseña actual y termina la sesión al completarse. En cualquier despliegue configura una contraseña segura y única desde el gestor de secretos de la plataforma.

Spring Security exige autenticación y rol de administrador en el panel, mantiene CSRF habilitado y configura cabeceras anti-iframe, MIME sniffing y referrer. En producción, el perfil `prod` confía en los encabezados del proxy inverso de Render para reconocer HTTPS; usa la URL HTTPS de Render y no expongas el contenedor directamente a Internet fuera de ese proxy. Las cookies de sesión de producción son `Secure`, `HttpOnly` y `SameSite=Lax`.

El reinicio opcional de datos de ejemplo está desactivado por defecto. Si se activa con `APP_LOCAL_DATA_RESET_ENABLED=true`, solo elimina/recrea facturas cuando la URL JDBC es loopback (`localhost`, `127.0.0.1` o `::1`); nunca se aplica a una base de datos remota y conserva la cuenta administradora.

## Variables de entorno

- `PORT` (default `8080`)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_ADMIN_USERNAME`
- `APP_ADMIN_PASSWORD`
- `APP_ADMIN_FULL_NAME`
- `APP_LOCAL_DATA_RESET_ENABLED` (default `false`; solo habilitar para pruebas con base de datos local)
- `SPRING_PROFILES_ACTIVE` (`prod` en la imagen Docker)

## Ejecucion local

```powershell
mvn clean package
$env:SPRING_PROFILES_ACTIVE = "local"
mvn spring-boot:run
```

Configura previamente las variables obligatorias de conexión y administrador. Puedes copiar `src/main/resources/application-local.example.yml` a `application-local.yml` en la raíz y activar el perfil `local`; el archivo de la raíz está ignorado por Git y queda fuera del artefacto. La plantilla solo usa referencias a variables de entorno: no guardes contraseñas en archivos versionados.

Para crear una entrega local con base PostgreSQL persistente y actualizaciones sin borrar datos, consulta:

- [INSTALACION-WINDOWS.md](INSTALACION-WINDOWS.md)
- [INSTALACION-MAC.md](INSTALACION-MAC.md)

## Pruebas

```powershell
mvn test
```

## Docker (Render)

```powershell
docker build -t mf-logistica .
docker run --rm -p 8080:8080 `
  -e SPRING_DATASOURCE_URL `
  -e SPRING_DATASOURCE_USERNAME `
  -e SPRING_DATASOURCE_PASSWORD `
  -e APP_ADMIN_USERNAME `
  -e APP_ADMIN_PASSWORD `
  -e APP_ADMIN_FULL_NAME `
  mf-logistica
```

El Dockerfile compila y ejecuta las pruebas Maven con Java 21 y ejecuta la aplicación con Java 21 JRE bajo el perfil `prod`. Este perfil habilita cookies de sesión seguras, desactiva mensajes técnicos de error y configura el pool de conexiones. Las migraciones Liquibase se aplican al iniciar la aplicación.
