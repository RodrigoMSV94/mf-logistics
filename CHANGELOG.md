# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]
- feat: añade gestión de datos fiscales y genera cada PDF desde el snapshot fiscal de su factura, conservando los datos históricos.
- feat: cambia el inicio de sesión a nombre de usuario y migra cuentas existentes preservando contraseña y rol.
- feat: incorpora la opción Datos fiscales y un botón de cierre de sesión destacado en las pantallas administrativas.
- security: exige secretos de conexión y administrador por entorno, limita a 16 caracteres mínimos la contraseña inicial y añade perfil de producción para sesiones/cabeceras.
- build: ejecuta la suite de pruebas durante la construcción de la imagen Docker.
- perf: usa proyecciones paginadas en listados de facturas para evitar cargar líneas innecesariamente.
- docs: actualiza la documentación de autenticación, clientes, facturación, snapshots fiscales y ejecución con Docker.

## [3.3.5] - 29/10/2026
- feat: creación del proyecto y primera versión de la web.
