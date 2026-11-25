# Opciones de despliegue económico para MF Logística

**Fecha de revisión de planes y precios publicados: 30/10/2026**

Este documento compara opciones para un negocio local que empieza desde cero. La aplicación es un monolito Java 21 / Spring Boot MVC, con Thymeleaf, PostgreSQL, JPA y generación de PDF bajo demanda. El volumen previsto es pequeño: una persona o pocas personas, facturas y clientes, consultas ocasionales y PDFs. No necesita Kubernetes ni servicios distribuidos.

## Recomendación

### Equilibrio entre coste y operación sencilla: Railway Hobby

Para empezar con una aplicación accesible para el negocio y sin administrar un servidor Linux, la opción más sencilla es **Railway Hobby con la aplicación y PostgreSQL en el mismo proyecto**, en dos servicios, y una copia de seguridad cifrada fuera de Railway.

- La suscripción Hobby cuesta **5 USD al mes e incluye 5 USD de uso**. Si el consumo mensual supera ese crédito, se cobra la diferencia. No es un precio garantizado de 5 USD: depende de memoria, CPU, tráfico y almacenamiento.
- El plan permite hasta 48 GB por servicio y 5 GB por volumen persistente, más que suficiente para el tamaño inicial de esta aplicación.
- Tanto la aplicación como PostgreSQL pueden permanecer encendidos; no dependen de que alguien visite la web para reactivarse. El almacenamiento de PostgreSQL debe ser un volumen persistente, no el filesystem efímero del contenedor.
- La estimación razonable para un despliegue pequeño es empezar alrededor de **5–10 USD/mes**, pero se debe comprobar en la consola de Railway con el consumo real. Aplicar alertas y límites de gasto.
- No tiene el SLA contractual que Railway reserva a Enterprise. Una sola instancia de la aplicación y una sola base de datos siguen siendo puntos únicos de fallo.

Usaría esta alternativa si el tiempo de administración también tiene coste. Si el consumo habitual excede el presupuesto, valorar un VPS europeo de bajo coste administrado por una persona técnica.

### Coste mínimo absoluto: Oracle Cloud Always Free

Se puede intentar desplegar aplicación y PostgreSQL en una VM Linux Always Free, usando Docker Compose y discos persistentes. Oracle publica hasta 2 OCPU y 12 GB de memoria para la cuota mensual A1 Always Free, según disponibilidad de la cuenta y región.

**No es la primera recomendación para guardar las únicas copias de facturas de un negocio.** La capacidad Always Free puede no estar disponible en la región elegida y Oracle se reserva reclamar instancias que considere inactivas. No hay SLA de disponibilidad para este uso gratuito. Además, hay que encargarse de actualizaciones del sistema, firewall, certificados TLS, persistencia, monitorización y copias de seguridad externas.

Puede servir para una prueba o si se acepta ese trabajo operativo y se verifica periódicamente que la instancia y los backups funcionan.

### Solo para pruebas o demostración: Render Free + Neon Free

No usaría esta combinación como producción del negocio:

- Render Free suspende el servicio web tras 15 minutos sin tráfico; volver a entrar puede tardar aproximadamente un minuto. El filesystem del contenedor es efímero.
- La base de datos PostgreSQL Free de Render expira a los 30 días y no incluye backups.
- Neon Free es permanente, pero tiene límites: 1 GB de almacenamiento por proyecto, 100 CU-horas de cómputo mensuales, suspensión tras cinco minutos de inactividad y un único snapshot manual. Si se agotan los recursos o la cuenta tiene una incidencia, la aplicación puede quedar indisponible.
- La combinación de dos proveedores implica más configuración y diagnóstico sin eliminar el riesgo de suspensión o pérdida de datos.

Es adecuada para validar el despliegue o enseñar una demo, no para depender de ella al emitir facturas.

### Railway Free y Railway + Neon Free

Railway Free ofrece **1 USD de crédito mensual**, con límites de 0,5 GB de RAM por servicio, una réplica y 500 MB de volumen. No presupuestaría que mantiene una aplicación Spring Boot y PostgreSQL disponibles de forma fiable todo el mes.

Railway Hobby + Neon Free reduce el coste de la base de datos, pero conserva los límites de suspensión y cómputo de Neon Free. Solo la elegiría si se acepta que el primer acceso tras inactividad pueda ser más lento y se mantienen backups independientes. Para mayor simplicidad, evitaría repartir aplicación y datos entre proveedores al principio.

## Alternativa autogestionada: VPS europeo

Un VPS en un centro de datos de la UE puede ser la opción más económica con servicio siempre activo si alguien puede administrarlo. En una sola máquina se pueden ejecutar la aplicación y PostgreSQL con Docker Compose, disco persistente, firewall que solo exponga HTTPS/SSH y backups cifrados fuera de la máquina.

El precio exacto depende de proveedor, región, IVA, disco y política actual; conviene comparar el coste final en la página del proveedor antes de contratar. No debe elegirse únicamente por el precio del VPS: una VM única puede fallar y toda la seguridad, parches, recuperación y observabilidad quedan bajo responsabilidad del negocio.

No alojaría la base de datos en el ordenador de casa como primera opción. Cortes eléctricos, Internet doméstico, IP dinámica o CGNAT, router y exposición de la red convierten una solución aparentemente gratuita en una fuente de interrupciones y riesgo. Si se usa solo para desarrollo, no publicar PostgreSQL en Internet.

## Base de datos y copias de seguridad

Para un negocio que emite facturas, la prioridad no es conseguir PostgreSQL gratis: es **no perder la base de datos**. Ningún plan gratuito consultado garantiza continuidad, restauración o soporte adecuados para este caso.

1. En Railway, crear un servicio PostgreSQL con volumen persistente. Guardar la URL/usuario/contraseña como variables o secretos del servicio de aplicación; no publicar el puerto de PostgreSQL.
2. Programar un `pg_dump` diario, cifrado, enviado a almacenamiento fuera del proveedor principal. Mantener una política de retención sencilla (por ejemplo, copias diarias recientes y copias mensuales) y vigilar errores/espacio.
3. Probar periódicamente la restauración en una base separada. Una tarea que solo crea archivos de backup, pero nunca se restaura, no demuestra que la recuperación funcione.
4. No usar la base de datos gratuita de Render para datos operativos: tiene caducidad de 30 días y carece de backups.
5. Si se usa Neon Free, tener backups externos propios; su cuota actual incluye solo un snapshot manual y un historial corto.
6. Para crecer o reducir el riesgo operativo, pasar PostgreSQL a un plan administrado con backups/PITR y soporte, o contratar un servicio con almacenamiento y recuperación incluidos.

## Puesta en marcha desde cero

El despliegue debe apuntar a una base PostgreSQL **nueva y vacía**, no a una copia con facturas anteriores. No se realizará traslado ni conversión de datos históricos.

Configurar al menos:

- `SPRING_PROFILES_ACTIVE=prod`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_ADMIN_USERNAME`
- `APP_ADMIN_PASSWORD` (mínimo 16 caracteres según el inicializador)
- `APP_ADMIN_FULL_NAME`
- `APP_LOCAL_DATA_RESET_ENABLED=false`

La aplicación aplica Liquibase al arrancar y JPA crea/actualiza las entidades configuradas. En una base nueva se inicializa el catálogo fiscal y los datos de ejemplo definidos por la aplicación; comprobar el primer arranque y emitir/descargar una factura de prueba antes de usarla con clientes.

No reutilizar el usuario/contraseña de desarrollo, no versionar secretos y no activar el reinicio local de ejemplos en producción. Configurar el dominio con HTTPS, habilitar MFA en las cuentas del proveedor si está disponible y restringir quién puede acceder al panel.

## Comparativa breve

| Opción | Coste publicado/esperado | Disponibilidad | Esfuerzo | ¿La usaría con facturas reales? |
|---|---:|---|---|---|
| Railway Hobby + PostgreSQL persistente + backup externo | 5 USD base; estimar 5–10 USD/mes al iniciar, sujeto al consumo | Aplicación siempre encendida; sin alta disponibilidad/SLA contractual en Hobby | Bajo | **Sí, para empezar**, siempre que los backups se verifiquen y se acepte el riesgo de una sola instancia |
| Oracle Cloud Always Free + app y PostgreSQL propios | 0 USD si hay capacidad Always Free | Sin SLA; posible falta de capacidad/reclamación por inactividad | Alto | Solo si se administra bien el servidor y hay backups externos; no es mi recomendación de menor riesgo |
| Render Free + Neon Free | 0 USD dentro de cuotas | Servicio dormido, DB con límites; Render Postgres Free expira | Bajo/medio | **No**; demo/desarrollo |
| Railway Free | 0 USD hasta agotar 1 USD/mes incluido | Límites estrechos de RAM/volumen y uso | Bajo | **No** para depender a diario |
| PC/servidor en casa | Coste eléctrico + equipo/conexión | Depende de luz, router, ISP y red local | Alto | No como primera opción |

## Fuentes oficiales consultadas

- [Railway: planes y precios](https://railway.com/pricing)
- [Railway Docs: planes, créditos y límites](https://docs.railway.com/reference/pricing/plans)
- [Render: límites de servicios gratuitos](https://render.com/docs/free)
- [Neon: precios y cuotas](https://neon.com/pricing)
- [Neon Docs: planes](https://neon.com/docs/introduction/plans)
- [Oracle Cloud: recursos Always Free e inactividad](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm)

Los proveedores pueden cambiar precios, cuotas, regiones y condiciones. Confirmar las páginas oficiales y la estimación del panel antes de contratar. “Gratis” no significa SLA ni recuperación garantizada.
