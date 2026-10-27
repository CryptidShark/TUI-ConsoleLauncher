# Cythernoir Launcher

**Cythernoir Launcher** es un fork avanzado de T-UI Console Launcher: una pantalla de inicio para Android que combina una terminal interactiva de alto rendimiento con accesos a aplicaciones, tarjetas informativas, automatización con Termux y controles avanzados del sistema.

El nombre de la aplicación, su pantalla “Acerca de” y la documentación usan **Cythernoir Launcher**, conservando el crédito al proyecto base de T-UI.

---

## 🚀 Nuevas Funcionalidades Principales

- **Integración con Termux:** Ejecuta scripts (`termux run <script>`), conéctate a sesiones tmux (`termux tmux`) y crea tarjetas dinámicas de monitoreo (`visual script <script>`).
- **Storage Access Framework (SAF):** Gestión moderna y segura de archivos en Android 10+ / API 37 mediante selectores de árboles de documentos.
- **Gestos y Teclas de Función Táctiles (`keybind`):** Desliza hacia arriba (buscador), abajo (notificaciones) o lateral (cambio de workspaces), y configura accesos rápidos para teclas virtuales F1–F10.
- **Comandos Personalizados (`command`):** Crea comandos compuestos y secuencias personalizadas (ej. `command create G search google`, `command create ops clear; time; monitor`), con persistencia en JSON.
- **Monitor de Sistema Completo (`monitor` / `visual sys`):** Visualiza en tiempo real uso de CPU, RAM, temperatura de batería, red y señal, con modo CRT opcional (`-crt` / `-nocrt`).
- **Text-to-Speech (TTS) y OCR (`tts`, `ocr`):** Lee texto en voz alta con el motor del sistema o extrae texto óptico de imágenes mediante SAF directo al portapapeles.
- **Asistente Flotante / Overlay (`overlay`):** Mini-terminal flotante accesible desde cualquier aplicación superpuesta en pantalla (`overlay show`).

---

## 🎨 Temas Modernizados (Tendencia 2026)

Cythernoir incluye paletas de color de primer nivel inspiradas en los estándares de la comunidad de desarrolladores y optimizadas para WCAG AA:
- `catppuccin_mocha` (Pastel cálido oscuro)
- `tokyo_night` (Azul-púrpura optimizado para OLED)
- `gruvbox_hard` (Retro cálido de alto contraste)
- `catppuccin_latte` (Modo claro limpio)
- `watchdogs_cyber`, `ghost_shell`, `dracula`, `nord`, `cyberpunk`, `amber`, `amoled`.
- Previsualiza cualquier tema antes de aplicarlo con `theme preview <nombre>`.

---

## Qué puedes hacer

- **Usar Android desde una interfaz de terminal:** abrir aplicaciones, buscar contenido y ejecutar comandos con sugerencias y aliases.
- **Personalizar la pantalla:** elegir entre los temas incluidos, ajustar preferencias de interfaz y fijar tarjetas de información.
- **Añadir widgets Android:** usar `widget add`, `widget list`, `widget move`, `widget height`, `widget remove` y `widget clear`.
- **Controlar música:** reproducir canciones locales o controlar sesiones multimedia con `visual music -fixed`.
- **Consultar información rápida:** fijar tarjetas como batería, sistema, notas, accesos directos, música y monitor de sistema.
- **Ampliar los comandos disponibles:** instalar BusyBox opcionalmente con `bbman -install`. La descarga verifica el archivo con SHA-256.

---

## Requisitos y compilación

- Android mínimo: **API 24** (Android 8+).
- SDK de compilación y destino: **API 37**.
- Java: **17**.
- Gradle Wrapper: **9.4.1**; Android Gradle Plugin: **8.13.2**.

Con Android SDK y Java configurados, puedes compilar las variantes de depuración:

```powershell
.\gradlew.bat :app:assembleFdroidDebug :app:assemblePlaystoreDebug
```

Las variantes disponibles son `fdroid` y `playstore`.

## Guía

Consulta la [guía de usuario en español](docs/USER_GUIDE.md) y el archivo [COMMANDS.md](COMMANDS.md) para conocer todos los comandos y ajustes avanzados.

## Créditos

Cythernoir Launcher parte del proyecto de código abierto [T-UI Console Launcher](https://github.com/Andre1299/TUI-ConsoleLauncher), de Francesco Andreuzzi, y conserva dependencias y componentes de sus autores originales.
