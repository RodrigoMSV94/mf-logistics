# Monitorizar Render con UptimeRobot

Esta guía configura un monitor HTTP para avisar si la app deja de responder y, si se deja activo con frecuencia, para reducir la probabilidad de que Render Free la duerma.

## URL del monitor

En UptimeRobot crea un monitor **HTTP(s)** con esta URL:

```text
https://<nombre-del-servicio>.onrender.com/
```

Sustituye el host por el dominio `onrender.com` que Render muestra en el servicio. Si más adelante configuras un dominio propio, puedes usar `https://<tu-dominio>/`.

Usa `/` (página pública) y no `/dashboard`: no requiere autenticación y no hace una consulta directa a PostgreSQL. Este proyecto no incluye Spring Actuator ni una ruta `/health`; no configures esa ruta porque responderá como inexistente.

## Configuración

1. En UptimeRobot selecciona **Add New Monitor** y el tipo **HTTP(s)**.
2. Pon un nombre reconocible, por ejemplo `MF Logística - Render`.
3. Pega la URL pública indicada arriba.
4. Usa el intervalo mínimo que permita tu plan (habitualmente 5 minutos en el plan gratuito).
5. Configura avisos por correo y deja que UptimeRobot considere no disponible una respuesta HTTP de error o timeout.
6. Guarda y comprueba que la primera verificación responda correctamente.

## Efectos y límites

- Render Free duerme una app después de 15 minutos sin tráfico. Las peticiones cada 5 minutos normalmente evitan ese periodo de inactividad y mantienen la app despierta, consumiendo horas gratuitas mientras el monitor esté activo. La cuota de Render es compartida por los servicios Free del workspace; 5 minutos todo el mes equivalen aproximadamente a 720 horas.
- Si no necesitas la app siempre despierta, desactiva el monitor cuando no quieras mantenerla caliente y acepta que la primera visita después de dormir tarde alrededor de un minuto.
- La petición normal a `/` no consulta PostgreSQL, así que **no garantiza mantener Neon activo**. La base Free puede suspenderse de todas formas y reactivarse cuando la app la necesite.
- Si Render se duerme y luego UptimeRobot lo despierta, la app ejecuta su inicialización; ese arranque accede a PostgreSQL y puede reactivar Neon.
- El monitor avisa de respuestas fallidas, pero no impide mantenimiento del proveedor, agotamiento de cuotas, fallos de despliegue ni pérdida de datos. No sustituye backups.

## Comprobación tras desplegar

Abre la URL manualmente en una ventana privada y confirma que muestra la página pública sin iniciar sesión. Después revisa en UptimeRobot que el monitor está **Up**. Prueba el login por separado; no guardes contraseñas en la configuración del monitor.

Consulta la [documentación de UptimeRobot](https://uptimerobot.com/help/) para ver los tipos de monitor, intervalos y opciones de notificación disponibles en tu plan.
