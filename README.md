# Cythernoir Launcher

**Cythernoir Launcher** es mi fork de T-UI Console Launcher: una pantalla de inicio para Android que combina una terminal interactiva con accesos a aplicaciones, tarjetas informativas y controles del sistema. El proyecto conserva la base de T-UI y añade su propia identidad, ajustes y funciones.

El nombre de la aplicación, su pantalla “Acerca de” y la documentación usan ahora **Cythernoir Launcher**, conservando el crédito al proyecto base.

## Qué puedes hacer

- **Usar Android desde una interfaz de terminal:** abrir aplicaciones, buscar contenido y ejecutar comandos con sugerencias y aliases.
- **Personalizar la pantalla:** elegir entre los temas incluidos, ajustar preferencias de interfaz y fijar tarjetas de información.
- **Añadir widgets Android:** usar `widget add`, `widget list`, `widget move`, `widget height`, `widget remove` y `widget clear` para administrar widgets compatibles.
- **Controlar música:** reproducir canciones locales con el reproductor integrado o controlar sesiones de otros reproductores desde la tarjeta `visual music`.
- **Consultar información rápida:** fijar tarjetas como batería, sistema, notas, accesos directos y música.
- **Usar herramientas del launcher:** gestionar notas, notificaciones, contactos, RSS, ubicación, temas, perfiles y ajustes mediante comandos.
- **Buscar y diagnosticar:** usar `palette [consulta]` para encontrar acciones, `privacy` para revisar permisos y `doctor` para localizar problemas de configuración.
- **Cambiar de contexto:** aplicar workspaces con conjuntos de tarjetas para desarrollo, privacidad, uso general o juegos.
- **Ampliar los comandos disponibles:** instalar BusyBox opcionalmente con `bbman -install`. La descarga requiere conexión a Internet y el instalador verifica el archivo con SHA-256.

Para ver los comandos disponibles, abre el launcher y escribe `help`. Para comenzar, prueba `apps -ls`, `search`, `settings` o `visual battery -fixed`.

## Música y tarjeta dinámica

`visual music -fixed` fija el reproductor visual en la pantalla del launcher. Puede mostrar el título y artista de una sesión multimedia activa, y ofrecer controles de reproducir/pausar y anterior/siguiente. Para controlar otras aplicaciones de música, concede a T-UI el permiso **Acceso a notificaciones** en los ajustes de Android.

La animación ASCII de la tarjeta es decorativa: no mide el audio ni muestra un espectro real. El reproductor integrado de Cythernoir es independiente de esa tarjeta y reproduce canciones accesibles al sistema.

## Widgets Android

1. Establece Cythernoir/T-UI como aplicación de inicio predeterminada si Android o el fabricante lo requiere para alojar widgets.
2. Ejecuta `widget add` y selecciona un widget en el selector de Android.
3. Completa cualquier pantalla de configuración que solicite la aplicación del widget.
4. Administra los widgets con `widget list`, `widget move [ID] up|down`, `widget height [ID] [dp]`, `widget remove [ID]` y `widget clear`.

La compatibilidad y el aspecto dependen también de cada proveedor de widgets y de la versión de Android.

## Temas incluidos

Los presets definidos por la aplicación son `CLASSIC_TERMINAL`, `MODERN_TERMINAL`, `CYBER_TERMINAL`, `NEO_TERMINAL`, `AMBER_TERMINAL`, `NORD_TERMINAL`, `DRACULA_TERMINAL`, `AMOLED_TERMINAL` y `WATCHDOGS_CYBER`. La disponibilidad de opciones puede depender de la versión instalada. Consulta `theme` y `help` dentro de la aplicación.

## Requisitos y compilación

- Android mínimo: **API 24**.
- SDK de compilación y destino: **API 37**.
- Java: **17**.
- Gradle Wrapper: **9.4.1**; Android Gradle Plugin: **8.13.2**.

Con Android SDK y Java configurados, puedes compilar las variantes de depuración:

```powershell
.\gradlew.bat :app:assembleFdroidDebug :app:assemblePlaystoreDebug
```

Las variantes disponibles son `fdroid` y `playstore`. Algunas funciones requieren permisos de Android; el launcher los solicita o los explica cuando se usan.

## Guía

Consulta la [guía de usuario en español](docs/USER_GUIDE.md) para conocer la instalación, comandos y ajustes. La [matriz de compatibilidad](docs/COMPATIBILITY.md) reúne los límites y pasos de validación para distintos dispositivos.

## Créditos

Cythernoir Launcher parte del proyecto de código abierto [T-UI Console Launcher](https://github.com/Andre1299/TUI-ConsoleLauncher), de Francesco Andreuzzi, y conserva dependencias y componentes de sus autores originales. Este repositorio es un fork independiente; las funciones y cambios de Cythernoir se mantienen aquí.
