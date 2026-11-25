# PostgreSQL de producción en Neon

Guía para usar Neon Free con la aplicación alojada en Render. Neon Free es apropiado para empezar con poco tráfico si se acepta que el cómputo puede suspenderse y se mantienen copias externas de los datos. Los precios, límites y regiones pueden cambiar; confírmalos en la consola antes de crear el proyecto.

## Qué guarda la base de datos

El modelo persistente de la aplicación incluye:

| Tabla | Contenido |
|---|---|
| `users` | Usuario, hash de contraseña, nombre y rol |
| `invoices` | Número, cliente, fecha, importes y snapshot fiscal del emisor |
| `invoice_lines` | Conceptos e importes de cada factura, incluido el indicador `taxable` |
| `clients` | Clientes guardados para reutilizarlos |
| `fiscal_data` | Datos fiscales actuales del emisor |
| `databasechangelog` y `databasechangeloglock` | Control interno de Liquibase |

Las facturas guardan una copia de los datos fiscales del emisor para conservar el histórico del PDF. Es información comercial y personal: limita el acceso a la consola y protege cualquier exportación o backup.

## Crear y preparar Neon

1. Crea una cuenta y un proyecto PostgreSQL en el plan **Free**. Elige Frankfurt (`aws-eu-central-1`) para reducir la latencia hacia un servicio Render en Europa, si Render ofrece esa región para la app.
2. Crea una base y un usuario dedicados a la aplicación. Guarda la contraseña en un gestor de contraseñas.
3. Copia los datos de conexión del panel de Neon. Para esta aplicación de poco tráfico, usa inicialmente la conexión **directa**: es simple y compatible con el arranque de Liquibase. Usa SSL obligatorio. Una conexión agrupada puede evaluarse después si realmente crece la concurrencia.
4. Asegúrate de que la base es nueva y está vacía antes del primer arranque. No uses una base que contenga tablas o datos desconocidos.
5. El changelog inicial crea todas las tablas y datos de configuración en una base vacía; Hibernate valida el esquema sin alterarlo. Revisa los logs del primer arranque y confirma que Liquibase finalizó correctamente.
6. Introduce el host, nombre de base, usuario y contraseña en las variables privadas del servicio Render, no en GitHub ni en el repositorio.

Formato JDBC esperado:

```text
jdbc:postgresql://<host-neon>/<nombre-base>?sslmode=require
```

En Render asigna ese valor a `SPRING_DATASOURCE_URL`; asigna usuario y contraseña a `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

## Suspensión y acceso

- En Free, Neon suspende el cómputo después de unos 5 minutos sin actividad. La documentación indica que suele reactivarse en unos cientos de milisegundos cuando recibe una consulta, pero no es un SLA.
- Una solicitud GET a `/` o `/login` no consulta PostgreSQL directamente. El POST de autenticación busca el usuario y sí puede despertar la base.
- Al arrancar la aplicación, Liquibase y `DataInitializer` acceden a la base; por tanto, un arranque frío de Render también puede reactivar Neon.
- La página pública y UptimeRobot no deben tratarse como una forma fiable de mantener la base siempre activa. Si Neon se suspende, la siguiente operación que necesite PostgreSQL la reactiva.
- Neon Free publica límites de almacenamiento/cómputo y no equivale a capacidad ilimitada. Consulta la cuota vigente en la consola y activa notificaciones disponibles.

## Copias de seguridad: requisito imprescindible

Neon Free no sustituye un backup independiente automático. Para conservar facturas:

1. Programa una exportación lógica periódica con `pg_dump` desde un equipo o tarea segura. Usa una versión de `pg_dump` compatible con la versión mayor de PostgreSQL de Neon.
2. Guarda cada exportación fuera de Neon y fuera del repositorio, cifrada y con acceso restringido. No dejes el único backup en el mismo ordenador que podría fallar.
3. Comprueba que la tarea terminó bien y conserva varias copias, incluyendo una mensual.
4. Prueba periódicamente la restauración de una copia en una base de prueba separada; no restaures sobre producción para probar.
5. Define quién recibe alertas y quién puede recuperar los datos.

Ejemplo de exportación en PowerShell (guardar la contraseña en `PGPASSWORD` de forma temporal y segura, no escribirla literalmente en el comando ni en un archivo versionado):

```powershell
pg_dump --format=custom --no-owner --no-acl `
  --host="<host-neon>" --username="<usuario>" --dbname="<nombre-base>" `
  --file="C:\BackupsSeguros\mf-logistica-$(Get-Date -Format yyyyMMdd).dump"
```

El comando solicitará contraseña si no configuras un método seguro de autenticación. Protege la carpeta de destino y cifra la copia antes de enviarla a almacenamiento externo.

## Antes de emitir facturas reales

- Verifica en la consola que Liquibase aplicó todas las migraciones sin errores.
- Comprueba que existen las cinco tablas de la aplicación y las dos tablas internas de Liquibase.
- Revisa los datos iniciales de cliente y emisor; la configuración actual del changelog incorpora registros iniciales.
- Inicia sesión, crea una factura de prueba claramente identificada, genera el PDF y verifica importes/IVA; después elimina la prueba solo mediante una acción controlada y tras confirmar que no es un documento fiscal emitido.
- Haz una copia y prueba una restauración antes de cargar datos reales.

## Referencias oficiales

- [Neon: precios y cuotas](https://neon.com/pricing)
- [Neon: scale to zero](https://neon.com/docs/introduction/scale-to-zero)
- [Neon: regiones](https://neon.com/docs/introduction/regions)
- [Neon: conexión agrupada](https://neon.com/docs/connect/connection-pooling)
