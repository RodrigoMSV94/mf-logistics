# Despliegue de MF Logística en Render

Guía para desplegar la aplicación web en Render Free desde GitHub, manteniendo PostgreSQL en Neon. Los nombres de opciones y precios de Render pueden cambiar; compruébalos en el panel antes de crear el servicio.

## Antes del primer despliegue

1. El repositorio contiene un changelog inicial para crear todas las tablas y datos de configuración en una base vacía. Hibernate usa `ddl-auto: validate`: comprueba que el esquema coincida, pero no lo altera. Ejecuta las pruebas y verifica el primer arranque en la base nueva. Para cambios futuros, añade changesets nuevos; no edites el changelog inicial después de haberlo aplicado en producción.
2. Ejecuta las pruebas Maven y confirma que la aplicación arranca con Java 21.
3. Comprueba los datos iniciales que introduce Liquibase (cliente y datos fiscales). Actualízalos en una migración o desde la aplicación antes de emitir facturas.
4. No subas contraseñas, `.env`, volcados de base de datos ni datos reales a GitHub.
5. Activa MFA en GitHub y Render y limita quién puede fusionar cambios a `master`.

## Crear el servicio web

1. Sube el repositorio a GitHub y crea un servicio **New > Web Service** en Render conectado a ese repositorio.
2. Configura:
   - **Branch:** `master` (o la rama que el equipo haya decidido usar como producción).
   - **Root Directory:** raíz del repositorio.
   - **Runtime:** Docker.
   - **Dockerfile Path:** `./Dockerfile`.
   - **Region:** la misma región europea que se elija para la base, si está disponible.
   - **Plan:** Free, aceptando que Render duerme el servicio tras 15 minutos sin tráfico y que el primer arranque puede tardar alrededor de un minuto.
3. En **Environment**, añade las variables de la sección siguiente. Usa los valores de conexión de Neon; no crees una base PostgreSQL Free en Render, porque caduca.
4. En **Settings > Health Checks**, puedes dejar el chequeo TCP predeterminado. Si configuras un chequeo HTTP, utiliza `/`, una ruta pública que responde sin exigir inicio de sesión.
5. Crea el servicio y revisa los logs del primer deploy. Un fallo de Liquibase o de conexión a Neon debe resolverse antes de continuar.

## Variables de entorno de Render

Configura las variables como secretos/valores del servicio, no en el código:

| Variable | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOST_NEON/BASE?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Usuario PostgreSQL de Neon |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña PostgreSQL de Neon |
| `APP_ADMIN_USERNAME` | Usuario administrador único |
| `APP_ADMIN_PASSWORD` | Contraseña aleatoria, única y de al menos 16 caracteres |
| `APP_ADMIN_FULL_NAME` | Nombre que se mostrará para la cuenta |
| `APP_LOCAL_DATA_RESET_ENABLED` | `false` |

Render proporciona `PORT`; la aplicación ya lo utiliza y no hace falta definirlo manualmente. Copia el host, base, usuario y contraseña del panel de Neon. No pegues la URL de conexión completa en una variable de contraseña ni publiques credenciales en tickets o capturas.

`DataInitializer` crea el usuario administrador únicamente si no existe; no actualiza la contraseña de un usuario existente al cambiar la variable. No asumas que basta con cambiar `APP_ADMIN_PASSWORD` para rotar una contraseña ya creada.

Después de iniciar sesión puedes cambiarla desde **Seguridad**. Se exige la contraseña actual, una nueva de al menos 16 caracteres y confirmación; al guardarla se invalida la sesión actual. El perfil `prod` confía en los encabezados HTTPS de Render para que Spring pueda reconocer la conexión protegida. Mantén el servicio detrás del proxy TLS de Render, usa siempre la URL `https://` y no publiques el contenedor directamente.

No uses valores de `.env.example` en producción. Genera contraseñas únicas y aleatorias para el administrador y PostgreSQL, guárdalas como variables secretas en Render y rótalas si se exponen. No las incluyas en logs, capturas, issues ni commits.

## Publicar actualizaciones desde un PR

1. Trabaja en una rama distinta de `master`.
2. Abre un Pull Request hacia `master`; revisa los cambios y espera a que las pruebas/acciones CI terminen correctamente.
3. Fusiona el PR solo después de comprobar que no incluye secretos, archivos de configuración locales, bases de datos ni artefactos de distribución.
4. Render debe estar configurado con **Auto-Deploy** para la rama de producción. Al fusionar, el nuevo commit llega a `master` y Render inicia el build y deploy.
5. Abrir un PR no actualiza producción por sí solo. Los entornos de preview de PR son opcionales y deben configurarse aparte; no los conectes a la base de producción.
6. Después del deploy, comprueba los logs, abre la página pública y realiza una prueba controlada de inicio de sesión y PDF. No uses una factura real como prueba.

Configura protección de rama en GitHub y una acción CI que ejecute `mvn test` como requisito antes de permitir la fusión. Si el primer deploy ya contiene datos reales, realiza una copia de seguridad antes de cualquier actualización de esquema.

## Qué archivos se despliegan

El `Dockerfile` solo copia `pom.xml` y `src/` para compilar la aplicación; no necesita los scripts locales, documentos de instalación, ni el paquete ZIP de `release/`. `.dockerignore` excluye esos archivos del contexto Docker y evita enviar configuración local o `.env` al builder. Mantén documentación útil en Git, pero no la empaquetes dentro del contenedor.

No borres `src/`, migraciones Liquibase, `pom.xml` o `Dockerfile`: son necesarios para construir la app. No subas la carpeta `release/` como sustituto del código fuente.

## Límites operativos de Render Free

- El servicio duerme tras 15 minutos sin tráfico entrante; al volver a recibir una petición se inicia y puede tardar aproximadamente un minuto.
- El filesystem del contenedor es efímero. La aplicación no debe guardar ahí facturas, archivos permanentes ni copias de la base.
- Render ofrece 750 horas gratuitas mensuales por workspace; los servicios Free comparten la cuota. Un monitor que mantenga esta app despierta todo el mes puede consumir unas 720 horas.
- El servicio y la base de datos son componentes distintos: mantener la app despierta no mantiene activo el cómputo Free de Neon.
- Un plan gratuito no ofrece garantía de disponibilidad. No confundir un monitor verde con un SLA ni con una copia de seguridad.

## Referencias oficiales

- [Render: Free services and limitations](https://render.com/docs/free)
- [Render: Docker deployments](https://render.com/docs/docker)
- [Render: Auto-deploy](https://render.com/docs/deploys)
- [Render: Health checks](https://render.com/docs/health-checks)
