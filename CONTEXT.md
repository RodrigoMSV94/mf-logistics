# 📦 ARCHITECTURE & CONTEXT SPECIFICATION: MF LOGÍSTICA

## 1. Visión General del Proyecto
Sistema web modular para una empresa de logística y reparto en España (foco actual en Girona y Barcelona). El sistema consta de:
1. **Landing Page Pública:** Informativa y orientada a la conversión vía WhatsApp.
2. **Portal de Login:** Exclusivo para socios/dueños del negocio.
3. **Panel Administrador:** Gestión mínima e inmediata de generación y descarga de facturas en PDF.

## 2. Stack Tecnológico & Arquitectura
* **Lenguaje:** Java 21 (LTS)
* **Framework Principal objetivo:** Spring Boot 4
* **Versión declarada actualmente en `pom.xml`:** Spring Boot 3.3.5 (no asumir que el proyecto ya migró a Boot 4)
* **Patrón de Arquitectura:** Clean Architecture + MVC (Model-View-Controller)
* **Principios de Diseño:** SOLID, Clean Code, DRY, KISS, YAGNI
* **Módulos Spring:**
  * `spring-boot-starter-web` (MVC Controller)
  * `spring-boot-starter-thymeleaf` (Motor de vistas)
  * `spring-boot-starter-data-jpa` (Persistencia)
  * `spring-boot-starter-security` (Autenticación y autorización)
  * `spring-boot-starter-validation` (Validación de DTOs)
* **Base de Datos:** PostgreSQL (Desarrollo local + Producción en **Neon DB**)
* **Migraciones:** Liquibase para evolucionar el esquema y versionar datos iniciales
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

La estructura anterior es una vista resumida. El proyecto también incluye modelos y persistencia de `Client` y `FiscalData`, sus puertos/servicios/adaptadores, `FiscalDataController` y las vistas `facturas.html` y `datos-fiscales.html`. La factura se compone de líneas persistidas y de los campos `issuerName`, `issuerNif`, `issuerAddress`, `issuerPhone`, `issuerEmail` e `issuerIban`, que forman su snapshot fiscal.

## 4. Principios de Código (SOLID & Clean Architecture)
1. **Single Responsibility (SRP):** Los controladores solo manejan peticiones HTTP/Navegación. La lógica de negocio reside en `application/service`. La generación física de PDF está desacoplada en `infrastructure/pdf`.
2. **Open/Closed & Interface Segregation (OCP/ISP):** El servicio de PDF e infraestructura implementan interfaces/puertos (`PdfGeneratorPort`), permitiendo cambiar la librería sin afectar la lógica de negocio.
3. **Dependency Inversion (DIP):** El núcleo de la aplicación depende de abstracciones (puertos), no de implementaciones concretas de PostgreSQL o PDF.

### Criterios obligatorios para cada cambio
Aplicar Java 21 y Spring Boot 4 como estándares objetivo, manteniendo explícito que la versión declarada del proyecto sigue siendo Spring Boot 3.3.5 hasta que se solicite y complete una migración. Cada cambio funcional debe incluir pruebas unitarias y de integración que cubran el comportamiento solicitado; verificar exactamente las superficies afectadas (por ejemplo, plantilla renderizada, persistencia/migración o flujo HTTP), y ejecutar la suite completa cuando sea viable. Las migraciones de base de datos deben ser versionadas con Liquibase y proteger los datos existentes. Estas reglas también están en `AGENTS.md` para futuras sesiones.

## 5. Modelos de Datos

### Entidad `User`
* `id` (Long, PK)
* `username` (String, Unique; principal de autenticación)
* `password` (String, BCrypt)
* `fullName` (String)
* `role` (String, default: "ROLE_ADMIN")

El usuario administrador inicial es `mfadmin` (configurable con `APP_ADMIN_USERNAME`). La migración renombra la columna histórica `email` a `username` y migra la cuenta inicial existente preservando contraseña y rol.

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
* `irpfRate` (BigDecimal, 1.0 para este perfil de facturación)
* `totalRetentions` (BigDecimal, importe negativo retenido en la factura)
* `totalAmount` (BigDecimal)
* `lines` (líneas de factura con descripción, cantidad y precio unitario)
* `issuerSnapshot` (copia del emisor al emitir: nombre, NIF, dirección, teléfono, correo e IBAN)

El dashboard calcula los importes desde las líneas de factura: base imponible, IVA fijo del 21%, retención IRPF fija del 1% y total (`base + IVA - retención`). Los tipos fijos se aplican también en el servicio de aplicación; no dependen de valores enviados por el navegador.

### Entidad `Client`
* `nif` (String, PK)
* `name` (String, Not Null)
* `address` (String)

El cliente de ejemplo SAÚL GUERRERO LÓPEZ (`73025558Y`) se incorpora en `src/main/resources/db/changelog/db.changelog-master.xml`. Liquibase crea la tabla y siembra el cliente de forma idempotente; las facturas guardadas sincronizan sus datos en el catálogo de clientes.

### Datos fiscales del emisor y snapshot de factura
`FiscalData` contiene nombre/razón social, NIF, dirección, teléfono, correo e IBAN. La configuración actual se edita en `/dashboard/datos-fiscales`. Al crear una factura, la aplicación lee la configuración vigente y la copia en columnas de la factura; el PDF siempre se genera desde ese snapshot. Si posteriormente se modifica la configuración, las facturas nuevas y sus PDFs usan los valores nuevos, mientras que las facturas existentes —incluidas las editadas o descargadas de nuevo— conservan sus datos fiscales originales. Liquibase inicializa el perfil fiscal y rellena los snapshots de las facturas históricas con los datos del emisor previos a esta funcionalidad.

El catálogo de clientes se guarda en PostgreSQL, identificado por NIF. Al guardar una factura se sincronizan los datos del cliente con el catálogo, que alimenta el selector de clientes guardados. Si se edita un cliente desde una factura y se conserva el NIF, el catálogo se actualiza con esos datos; las facturas previas mantienen su propia copia de los datos del cliente.

## 6. Mapeo de Rutas MVC

| Método | Ruta | Controlador | Descripción | Permisos |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/` | `PublicController` | Renderiza `index.html` (Landing) | Público |
| `GET` | `/login` | `AuthController` | Renderiza `login.html` | Público |
| `POST` | `/login` | `Security` | Autenticación de administradores | Público |
| `GET` | `/dashboard` | `DashboardController` | Formulario para crear factura | `ROLE_ADMIN` |
| `GET` | `/dashboard/facturas` | `DashboardController` | Lista, filtra y pagina facturas | `ROLE_ADMIN` |
| `GET` | `/dashboard/facturas/{id}/editar` | `DashboardController` | Abre una factura para edición | `ROLE_ADMIN` |
| `GET/POST` | `/dashboard/datos-fiscales` | `FiscalDataController` | Consulta y actualiza datos del emisor para nuevas facturas | `ROLE_ADMIN` |
| `POST` | `/dashboard/invoices` | `DashboardController` | Crea o actualiza una factura y redirige al listado con descarga del PDF | `ROLE_ADMIN` |
| `GET` | `/dashboard/invoices/{id}/pdf` | `DashboardController` | Descarga el PDF generado desde la factura y su snapshot fiscal | `ROLE_ADMIN` |

## 7. Configuración y despliegue (Render + Neon DB)

La aplicación lee la conexión desde `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`. El servidor usa `PORT` (por defecto `8080`). Liquibase aplica `classpath:db/changelog/db.changelog-master.xml`; Hibernate está configurado con `ddl-auto: update`.

El administrador inicial se configura con `APP_ADMIN_USERNAME` (default `mfadmin`), `APP_ADMIN_PASSWORD` (default `Admin1234!`) y `APP_ADMIN_FULL_NAME` (default `Administrador MF`). El inicializador crea la cuenta si no existe, pero no restablece una contraseña existente. Configurar una contraseña segura en todos los despliegues.

`APP_LOCAL_DATA_RESET_ENABLED` está activo por defecto, pero el reinicio de facturas de muestra solo se ejecuta cuando la URL JDBC apunta a loopback (`localhost`, `127.0.0.1` o `::1`). Se puede desactivar con `APP_LOCAL_DATA_RESET_ENABLED=false`; no habilitar ese comportamiento contra una base de datos remota.

El `Dockerfile` usa una etapa Maven con Java 21 para empaquetar y una imagen Java 21 JRE para ejecutar en el puerto 8080. No existe un archivo Docker Compose en el repositorio. No requiere cambios para la funcionalidad fiscal/autenticación: utiliza Java 21, compatible con la versión declarada actualmente en Maven.

Las imágenes de contenedor ejecutan el empaquetado con `-DskipTests`; las pruebas deben ejecutarse antes de construir/publicar la imagen.