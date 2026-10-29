# MF Logistica

Aplicacion web MVC para landing publica, acceso interno de socios y panel de facturacion con generacion de PDF.

## Stack

- Java 21
- Spring Boot 3.3.5
- Spring MVC + Thymeleaf
- Spring Security
- Spring Data JPA
- PostgreSQL (Neon compatible)
- OpenPDF

## Estructura

- `src/main/java/com/mflogistica/domain`: modelos y excepciones de negocio
- `src/main/java/com/mflogistica/application`: DTOs, puertos y servicios de casos de uso
- `src/main/java/com/mflogistica/infrastructure`: persistencia, seguridad y adaptadores externos
- `src/main/java/com/mflogistica/web`: controladores MVC y manejo global de errores
- `src/main/resources/templates`: vistas Thymeleaf (`index.html`, `login.html`, `dashboard.html`)

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
