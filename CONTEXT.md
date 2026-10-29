# 📦 ARCHITECTURE & CONTEXT SPECIFICATION: MF LOGÍSTICA

## 1. Visión General del Proyecto
Sistema web modular para una empresa de logística y reparto en España (foco actual en Girona y Barcelona). El sistema consta de:
1. **Landing Page Pública:** Informativa y orientada a la conversión vía WhatsApp.
2. **Portal de Login:** Exclusivo para socios/dueños del negocio.
3. **Panel Administrador:** Gestión mínima e inmediata de generación y descarga de facturas en PDF.

## 2. Stack Tecnológico & Arquitectura
* **Lenguaje:** Java 21 (LTS)
* **Framework Principal:** Spring Boot 3.x / 4.x (Spring Framework 6/7)
* **Patrón de Arquitectura:** Clean Architecture + MVC (Model-View-Controller)
* **Principios de Diseño:** SOLID, Clean Code, DRY, KISS, YAGNI
* **Módulos Spring:**
  * `spring-boot-starter-web` (MVC Controller)
  * `spring-boot-starter-thymeleaf` (Motor de vistas)
  * `spring-boot-starter-data-jpa` (Persistencia)
  * `spring-boot-starter-security` (Autenticación y autorización)
  * `spring-boot-starter-validation` (Validación de DTOs)
* **Base de Datos:** PostgreSQL (Desarrollo local + Producción en **Neon DB**)
* **Generación de PDF:** `OpenPDF` (`com.github.librepdf:openpdf`)
* **Frontend UI:** Thymeleaf + Tailwind CSS (vía CDN) + Google Material Symbols
* **Despliegue Target:** Render (Web Service vía Dockerfile)

## 3. Estructura de Proyecto (Clean Architecture + MVC)

```
mf-logistica/
├── Dockerfile
├── pom.xml
├── CONTEXT.md
├── docs/
│   └── desing-html/         # Maquetas visuales de Google Stitch
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── mflogistica/
        │           ├── MfLogisticaApplication.java
        │           │
        │           ├── domain/                      # CAPA DE DOMINIO (Core)
        │           │   ├── model/
        │           │   │   ├── User.java
        │           │   │   └── Invoice.java
        │           │   └── exception/
        │           │       └── BusinessException.java
        │           │
        │           ├── application/                 # CAPA DE APLICACIÓN (Casos de Uso / Servicios)
        │           │   ├── dto/
        │           │   │   ├── InvoiceRequestDto.java
        │           │   │   └── InvoiceResponseDto.java
        │           │   ├── port/                    # Inversión de Dependencias (DIP)
        │           │   │   ├── InvoiceRepositoryPort.java
        │           │   │   └── PdfGeneratorPort.java
        │           │   └── service/
        │           │       ├── InvoiceService.java
        │           │       └── AuthService.java
        │           │
        │           ├── infrastructure/              # CAPA DE INFRAESTRUCTURA (Detalles externos)
        │           │   ├── config/
        │           │   │   ├── SecurityConfig.java
        │           │   │   └── DataInitializer.java
        │           │   ├── persistence/
        │           │   │   ├── entity/
        │           │   │   │   ├── UserEntity.java
        │           │   │   │   └── InvoiceEntity.java
        │           │   │   ├── mapper/              # Mapeadores Entity <-> Domain
        │           │   │   │   └── InvoiceMapper.java
        │           │   │   └── repository/
        │           │   │       ├── SpringDataUserRepository.java
        │           │   │       └── SpringDataInvoiceRepository.java
        │           │   └── pdf/
        │           │       └── OpenPdfGeneratorAdapter.java
        │           │
        │           └── web/                         # CAPA PRESENTACIÓN (MVC Controllers)
        │               ├── controller/
        │               │   ├── PublicController.java
        │               │   ├── AuthController.java
        │               │   └── DashboardController.java
        │               └── advice/
        │                   └── GlobalExceptionHandler.java
        │
        └── resources/
            ├── application.yml
            └── templates/                            # Vistas Thymeleaf basadas en docs/desing-html
                ├── index.html
                ├── login.html
                └── dashboard.html
```

## 4. Principios de Código (SOLID & Clean Architecture)
1. **Single Responsibility (SRP):** Los controladores solo manejan peticiones HTTP/Navegación. La lógica de negocio reside en `application/service`. La generación física de PDF está desacoplada en `infrastructure/pdf`.
2. **Open/Closed & Interface Segregation (OCP/ISP):** El servicio de PDF e infraestructura implementan interfaces/puertos (`PdfGeneratorPort`), permitiendo cambiar la librería sin afectar la lógica de negocio.
3. **Dependency Inversion (DIP):** El núcleo de la aplicación depende de abstracciones (puertos), no de implementaciones concretas de PostgreSQL o PDF.

## 5. Modelos de Datos

### Entidad `User`
* `id` (Long, PK)
* `email` (String, Unique)
* `password` (String, BCrypt)
* `fullName` (String)
* `role` (String, default: "ROLE_ADMIN")

### Entidad `Invoice`
* `id` (Long, PK)
* `invoiceNumber` (String, Unique) -> Formato: `FAC-YYYY-XXXX`
* `clientName` (String, Not Null)
* `clientNif` (String, Not Null)
* `clientAddress` (String)
* `issueDate` (LocalDate)
* `subtotal` (BigDecimal)
* `taxRate` (BigDecimal, default 21.0)
* `taxAmount` (BigDecimal)
* `totalAmount` (BigDecimal)
* `concept` (String)

## 6. Mapeo de Rutas MVC

| Método | Ruta | Controlador | Descripción | Permisos |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/` | `PublicController` | Renderiza `index.html` (Landing) | Público |
| `GET` | `/login` | `AuthController` | Renderiza `login.html` | Público |
| `POST` | `/login` | `Security` | Autenticación de administradores | Público |
| `GET` | `/dashboard` | `DashboardController` | Renderiza `dashboard.html` | `ROLE_ADMIN` |
| `POST` | `/dashboard/invoices` | `DashboardController` | Procesa creación y guarda factura | `ROLE_ADMIN` |
| `GET` | `/dashboard/invoices/{id}/pdf` | `DashboardController` | Descarga el archivo PDF directo | `ROLE_ADMIN` |

## 7. Configuración de Entornos (Render + Neon DB)

### `application.yml`
```yaml
server:
  port: ${PORT:8080}

spring:
  application:
    name: mf-logistica
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/mflogistica}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          lob:
            non_contextual_creation: true
```
eof
```

---

### Prompt definitivo para Copilot en IntelliJ

Copia y pega este prompt exacto en el chat de **GitHub Copilot**:

```text
Actúa como un Arquitecto de Software y Desarrollador Principal experto en Java 21, Spring Boot y Clean Architecture.

Necesito que construyas la estructura completa del proyecto desde cero respetando estrictamente la documentación técnica definida en el archivo `CONTEXT.md` que está en la raíz del proyecto.

### REQUERIMIENTOS CLAVE:
1. Lee minuciosamente `CONTEXT.md` para entender la arquitectura por capas (domain, application, infrastructure, web).
2. Explora la carpeta `docs/desing-html` (y todas sus subcarpetas) para extraer los HTML/Tailwind CSS diseñados en Google Stitch. Adapta esas maquetas como plantillas Thymeleaf dentro de `src/main/resources/templates/` (`index.html`, `login.html`, `dashboard.html`).
3. Sigue principios SOLID y Clean Code: desacopla persistencia y servicios externos mediante interfaces/puertos (`PdfGeneratorPort`, `InvoiceRepositoryPort`).

### TAREAS A EJECUTAR DE FORMA ORDENADA:
1. `pom.xml`: Genera el archivo con dependencias para Java 21, Spring Boot (Web, Security, Data JPA, Validation, Thymeleaf), PostgreSQL, Lombok y OpenPDF.
2. Dominio y Aplicación:
   - Clases de Dominio (`User`, `Invoice`) y DTOs con validaciones.
   - Puertos e interfaces de servicios.
   - `InvoiceService.java`: Lógica para calcular IVA, totales y coordinar la creación y emisión de facturas.
3. Infraestructura:
   - Entidades JPA, Repositorios Spring Data y Mapeadores.
   - `OpenPdfGeneratorAdapter.java`: Implementación física de generación de PDF con la paleta de colores corporativa (negro, beige, rosa) indicada en el diseño.
   - `SecurityConfig.java`: Autenticación por formulario y usuario inicial en memoria/BD.
4. Capa Web (MVC):
   - `PublicController.java`, `AuthController.java` y `DashboardController.java`.
   - Control de excepciones global (`GlobalExceptionHandler.java`).
5. Configuración y Despliegue:
   - `application.yml` parametrizado con variables de entorno para Neon PostgreSQL.
   - `Dockerfile` multi-stage para Java 21 listo para desplegar en Render.

Comienza generando los archivos paso a paso.
```

¿Quieres que hagamos algún ajuste en los controladores o en la entidad de facturación antes de que se lo envíes a Copilot?