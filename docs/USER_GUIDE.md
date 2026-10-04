# Cythernoir Launcher — Manual de Usuario

Bienvenido al manual oficial de **Cythernoir Launcher**, una interfaz Android inspirada en consolas TUI (Terminal User Interface) modernas, diseñada para ser ultra veloz, privada, offline-first, minimalista y orientada a desarrolladores y usuarios técnicos.

---

## Índice
1. [¿Qué es Cythernoir?](#1-qué-es-cythernoir)
2. [Instalación](#2-instalación)
3. [Convertirlo en Launcher Predeterminado](#3-convertirlo-en-launcher-predeterminado)
4. [Primeros 10 Minutos (Configuración Inicial)](#4-primeros-10-minutos-configuración-inicial)
5. [Interfaz Principal](#5-interfaz-principal)
6. [Tema Dracula y Personalización](#6-tema-dracula-y-personalización)
7. [Sistema de Gestos](#7-sistema-de-gestos)
8. [Terminal / TUI y Comandos Disponibles](#8-terminal--tui-y-comandos-disponibles)
9. [Aliases y Automatización de Entrada](#9-aliases-y-automatización-de-entrada)
10. [Búsqueda Universal 2.0](#10-búsqueda-universal-20)
11. [Command Palette y Acciones Tipadas](#11-command-palette-y-acciones-tipadas)
12. [App Drawer (Cajón de Aplicaciones)](#12-app-drawer-cajón-de-aplicaciones)
13. [Gestión de Widgets Nativos Android](#13-gestión-de-widgets-nativos-android)
14. [Workspaces y Perfiles](#14-workspaces-y-perfiles)
15. [Notificaciones Interactivas](#15-notificaciones-interactivas)
16. [Media HUD (Control Multimedia)](#16-media-hud-control-multimedia)
17. [Privacy Center (Centro de Privacidad)](#17-privacy-center-centro-de-privacidad)
18. [App Inspector (Inspección Técnica de Apps)](#18-app-inspector-inspección-técnica-de-apps)
19. [Estadísticas de Uso Locales](#19-estadísticas-de-uso-locales)
20. [Motor de Automatización Local (WHEN-DO)](#20-motor-de-automatización-local-when-do)
21. [Copia de Seguridad y Restauración (Backup & Restore)](#21-copia-de-seguridad-y-restauración-backup--restore)
22. [Solución de Problemas Frecuentes](#22-solución-de-problemas-frecuentes)
23. [Privacidad y Comparativa F-Droid vs Play Store](#23-privacidad-y-comparativa-f-droid-vs-play-store)
24. [Configuración Recomendada Developer / Dracula](#24-configuración-recomendada-developer--dracula)
25. [Checklist Inicial](#25-checklist-inicial)

---

## 1. ¿Qué es Cythernoir?
Cythernoir Launcher es un entorno de escritorio completo para dispositivos Android que reemplaza el launcher tradicional por una consola interactiva rica en información, rápida y personalizable. A diferencia de un emulador de terminal Linux tradicional, Cythernoir se comunica directamente con las APIs oficiales de Android para lanzar aplicaciones, gestionar notificaciones, alojar widgets nativos y permitir una navegación híbrida mediante comandos y gestos.

---

## 2. Instalación
1. Descarga el paquete APK de Cythernoir Launcher (versión `Fdroid` o `Playstore`).
2. Abre el archivo descargado e instala la aplicación aceptando los permisos de instalación estándar de Android.
3. Una vez instalada, presiona el botón físico o gesto de **Inicio (Home)** de tu teléfono.

---

## 3. Convertirlo en Launcher Predeterminado
Para disfrutar de la experiencia completa de Cythernoir, debes establecerlo como la pantalla de inicio predeterminada de Android:

1. Al presionar **Home** por primera vez tras la instalación, Android te preguntará qué aplicación deseas usar. Selecciona **Cythernoir Launcher** y presiona **Siempre**.
2. **Método alternativo desde Ajustes de Android:**
   * Ve a `Ajustes del Sistema` $\rightarrow$ `Aplicaciones` $\rightarrow$ `Aplicaciones Predeterminadas` $\rightarrow$ `Aplicación de Inicio` (Home).
   * Selecciona **Cythernoir Launcher**.
3. **Volver al launcher anterior:** Si deseas cambiar temporalmente de launcher, ejecuta en la consola de Cythernoir:
   ```text
   config -set behavior default_launcher false
   ```
   o dirígete a los Ajustes de Aplicaciones Predeterminadas de Android.

---

## 4. Primeros 10 Minutos (Configuración Inicial)
Sigue esta secuencia rápida de 10 minutos para dejar tu launcher listo para un entorno de desarrollo profesional:

1. **Elegir el Tema Dracula:** Escribe `settings` en la consola y pulsa **Enter** (o pulsa el botón **Cyber/Dracula**).
2. **Consultar Gestos:** Escribe `settings gestures` para verificar las acciones asignadas a gestos.
3. **Listar Aplicaciones:** Desliza hacia arriba o escribe `apps -ls` para ver todas tus aplicaciones.
4. **Fijar Tarjetas de Información:** Escribe `visual system -fixed` o `visual battery -fixed` para fijar paneles de monitoreo en el escritorio.
5. **Configurar un Alias:** Escribe `alias -add dev "open Android Studio"` para crear un atajo rápido.
6. **Realizar un Backup:** Escribe `config -get` o realiza una copia de seguridad local.

---

## 5. Interfaz Principal
La pantalla de inicio se compone de tres áreas clave:
* **Consola Terminal (Historial):** Muestra los comandos ejecutados, salidas de texto, tarjetas de métricas del sistema y paneles fijados.
* **Barra de Entrada de Comandos:** Campo de texto interactivo con sugerencias inteligentes en tiempo real.
* **Paneles Modulares HUD:** Tarjetas informativas sobre nivel de batería, uso de RAM, estado de la red Wi-Fi/Bluetooth, control de música y notificaciones.

---

## 6. Tema Dracula y Personalización
Cythernoir incluye 8 esquemas de color predefinidos diseñados para alta legibilidad:

| Preset Tema | Descripción |
| :--- | :--- |
| `DRACULA_TERMINAL` | Paleta oficial Dracula (#282A36 fondo, #F8F8F2 texto, #8BE9FD cian, #FF79C6 rosa, #BD93F9 púrpura) |
| `AMOLED_TERMINAL` | Fondo negro puro (#000000) optimizado para máximo ahorro de batería en pantallas OLED |
| `AMBER_TERMINAL` | Fósforo ámbar retro tipo monitor CRT |
| `NORD_TERMINAL` | Estilo nórdico ártico helado |
| `CYBER_TERMINAL` | Neón magenta y verde matriz estilo Cyberpunk |
| `NEO_TERMINAL` | Cyan y púrpura de contraste medio |
| `MODERN_TERMINAL` | Azul oscuro estilo Tokyo Night |
| `CLASSIC_TERMINAL` | Verde terminal clásico sobre negro |

Para cambiar de tema, ejecuta:
```text
config -set behavior theme_preset DRACULA_TERMINAL
```
o ingresa a la tarjeta visual mediante el comando `settings`.

---

## 7. Sistema de Gestos
Cythernoir detecta vectores táctiles directos en la pantalla de inicio sin interferir con la barra de navegación gestual de Android:

| Gesto | Acción Predeterminada | Comando de Configuración |
| :--- | :--- | :--- |
| **Swipe Up (Deslizar Arriba)** | Muestra el cajón de aplicaciones | `config -set behavior swipe_up_cmd "apps -ls"` |
| **Swipe Down (Deslizar Abajo)** | Despliega notificaciones | `config -set behavior swipe_down_cmd "notifications"` |
| **Double Tap (Doble Toque)** | Bloquea la pantalla o comando | `config -set behavior double_tap_lock true` / `double_tap_cmd` |
| **Long Press (Toque Prolongado)** | Abre opciones contextuales / ajustes | `config -set behavior long_click_duration 500` |

Para visualizar los gestos activos en pantalla, ejecuta:
```text
settings gestures
```

---

## 8. Terminal / TUI y Comandos Disponibles
A continuación se detalla la lista de comandos locales nativos disponibles:

| Comando | Descripción | Ejemplo de Uso |
| :--- | :--- | :--- |
| `help` | Muestra el menú de ayuda y comandos generales | `help` |
| `apps` | Muestra o busca aplicaciones instaladas | `apps -ls` / `apps -h` (ocultas) |
| `battery` | Muestra estado, temperatura y nivel de batería | `battery` |
| `device` | Información del modelo, SO, Kernel y tiempo encendido | `device` |
| `network` | Estado de la red Wi-Fi, IP local y conectividad | `network` |
| `storage` | Análisis de espacio en almacenamiento interno | `storage` |
| `settings` | Despliega la tarjeta gráfica interactiva de ajustes | `settings` / `settings live` / `settings gestures` |
| `theme` | Muestra o modifica la paleta de colores activa | `theme` |
| `visual` | Muestra o fija tarjetas de monitoreo en el terminal | `visual system -fixed` / `visual clear` |
| `alias` | Crea, elimina o lista atajos de comandos | `alias -add dev "open Android Studio"` |
| `clear` | Limpia la pantalla de la consola terminal | `clear` |
| `notifications` | Muestra el panel interactivo de notificaciones | `notifications` |
| `widget` / `bbman` | Administra e infla widgets Android externos | `widget add` / `widget clear` |
| `wifi` | Abre los ajustes de red Wi-Fi de Android | `wifi` |
| `bluetooth` | Abre la configuración de Bluetooth | `bluetooth` |

---

## 9. Aliases y Automatización de Entrada
Un **Alias** permite abreviar comandos extensos en una sola palabra clave.

* **Crear un Alias:**
  ```text
  alias -add dev "open Android Studio"
  alias -add web "open Firefox"
  ```
* **Listar Aliases Activos:**
  ```text
  alias -ls
  ```
* **Eliminar un Alias:**
  ```text
  alias -rm dev
  ```
* **Almacenamiento:** Los aliases se guardan localmente en `alias.txt` y se incluyen automáticamente en las copias de seguridad (`launcher-config.json`).

---

## 10. Búsqueda Universal 2.0
El motor de **Búsqueda Universal 2.0** indexa instantáneamente todo tu dispositivo. Simplemente empieza a escribir en la barra de entrada o abre el buscador.

El sistema de puntuación prioriza los resultados bajo la siguiente jerarquía estricta:
1. **Coincidencia Exacta (Exact Match)**
2. **Coincidencia por Prefijo (Prefix Match)**
3. **Coincidencia por Alias (Alias Match)**
4. **Uso Reciente (Recent Usage)**
5. **Frecuencia de Uso (Frequency)**
6. **Búsqueda Difusa (Fuzzy Match / Levenshtein)**

---

## 11. Command Palette y Acciones Tipadas
El **Command Palette** permite ejecutar acciones del sistema de forma segura mediante **Acciones Tipadas Internas** (evitando la ejecución de comandos de shell inseguros):
* `OpenAppAction`: Abre la aplicación seleccionada.
* `OpenSettingsAction`: Abre la sección exacta de Ajustes de Android.
* `LockDeviceAction`: Bloquea la pantalla de forma segura.
* `SwitchWorkspaceAction`: Cambia el espacio de trabajo activo.
* `ExecuteAliasAction`: Despacha un alias creado por el usuario.

---

## 12. App Drawer (Cajón de Aplicaciones)
Para ver todas tus aplicaciones ordenadas alfabéticamente:
* Desliza hacia arriba en la pantalla principal o escribe:
  ```text
  apps -ls
  ```
* **Ocultar una app:** Para ocultar una app sensible del cajón:
  ```text
  apps -hide NombreDeApp
  ```
* **Mostrar apps ocultas:**
  ```text
  apps -lsh
  ```

---

## 13. Gestión de Widgets Nativos Android
Cythernoir incluye soporte nativo completo para widgets nativos de Android (Google Chrome, Spotify, Clima, Cuidado del Dispositivo, etc.):

1. **Añadir un Widget:** Escribe `widget add` o `bbman add` para abrir el selector oficial de Android.
2. **Redimensionamiento:** El launcher calcula dinámicamente las dimensiones en `dp` (`updateWidgetOptions`) para que el contenido nunca se encasille ni se distorsione.
3. **Eliminar Widgets:** Escribe `widget clear` para eliminar los widgets del escritorio.
4. **Recuperación tras Desinstalación:** Si desinstalas una app cuyo widget tenías en pantalla, Cythernoir detectará la ausencia del proveedor, purgará de forma segura el identificador huérfano y recuperará el control sin experimentar cierres (*crashes*).

---

## 14. Workspaces y Perfiles
Puedes organizar tu entorno en distintos **Espacios de Trabajo (Workspaces)** según la actividad que estés realizando:
* `Minimal`: Solo reloj, búsqueda y aplicaciones esenciales.
* `Developer`: Herramientas de desarrollo, métricas de CPU/RAM, terminal y accesos a IDEs.
* `Privacy`: Modo seguro sin accesos de fondo ni servicios externos.
* `Cyber`: Estilo cargado con monitoreo del sistema, reproductor de medios y widgets.
* `Gaming`: Pantalla despejada orientada al rendimiento.
* `Custom`: Configuración personalizada por el usuario.

---

## 15. Notificaciones Interactivas
El sistema de notificaciones de Cythernoir ofrece interacción directa (*deep linking*):
1. **Visualización:** Escribe `notifications` o desliza hacia abajo.
2. **Navegación Directa:** Al pulsar sobre una notificación (por ejemplo, de WhatsApp, Gmail o Telegram), Cythernoir invoca el `PendingIntent` original del sistema, abriendo **la conversación o correo específico**.
3. **Recuperación Segura:** Si la notificación ya expiró o fue borrada por la app de origen, Cythernoir ejecuta un respaldo seguro abriendo la aplicación principal sin fallar.

---

## 16. Media HUD (Control Multimedia)
Cuando hay una sesión de música o video activa (Spotify, YouTube, VLC), el panel de control multimedia permite:
* Reproducir / Pausar.
* Pista Siguiente / Pista Anterior.
* Visualizar título de la canción y artista en tiempo real.

---

## 17. Privacy Center (Centro de Privacidad)
Escribe `device` o accede a la sección de Privacidad para verificar qué permisos sensibles están activos en tu dispositivo:
* Estado de permiso de Notificaciones (`POST_NOTIFICATIONS`).
* Acceso a Contactos (`READ_CONTACTS`).
* Acceso a Ubicación (`ACCESS_FINE_LOCATION`).

Todo el análisis se realiza 100% de manera local en el teléfono.

---

## 18. App Inspector (Inspección Técnica de Apps)
Permite inspeccionar técnicamente cualquier aplicación instalada:
* Nombre del Paquete (`packageName`).
* Versión y Código de Versión (`versionCode`).
* SDK Objetivo (`targetSdkVersion`) y SDK Mínimo (`minSdkVersion`).

---

## 19. Estadísticas de Uso Locales
Cythernoir registra de forma privada y local el uso de tus aplicaciones para priorizar tus apps más frecuentadas (`MOST USED` y `RECENTLY USED`). Ningún dato de uso sale jamás de tu dispositivo.

---

## 20. Motor de Automatización Local (WHEN-DO)
Permite establecer reglas condicionales locales de automatización:
* `WHEN battery < 20%` $\rightarrow$ `DO open battery saver settings`
* `WHEN time = 08:00` $\rightarrow$ `DO workspace = Developer`

---

## 21. Copia de Seguridad y Restauración (Backup & Restore)
Toda tu configuración (temas, gestos, aliases, espacios de trabajo y accesos fijados) puede exportarse e importarse mediante el archivo local:
```text
launcher-config.json
```
* **Exportar / Guardar:** Escribe `config -get` o copia el archivo de configuración.
* **Restaurar:** Reemplaza el archivo y ejecuta `config -reload` o `restart`.
* **Compatibilidad de Esquema:** El archivo cuenta con control de versión (`schemaVersion`). Si se importa una versión antigua o con campos dañados, el sistema aplicará valores de reserva seguros sin romperse.

---

## 22. Solución de Problemas Frecuentes

| Problema | Causa Probable | Solución |
| :--- | :--- | :--- |
| **El launcher no se establece como predeterminado** | No se asignaron permisos de Inicio en Android | Ve a `Ajustes de Android` $\rightarrow$ `Aplicaciones Predeterminadas` $\rightarrow$ `Inicio` y selecciona **Cythernoir**. |
| **Los gestos no responden** | Conflicto con la barra de navegación gestual | Asegúrate de realizar el gesto en el centro de la pantalla del terminal y no sobre el borde físico del marco del teléfono. |
| **Un widget aparece vacío o no carga** | La app proveedora requiere configuración inicial | Toca el widget o elimina el widget con `widget clear` y vuelve a añadirlo completando el diálogo de configuración de la app. |
| **Una notificación no abre el mensaje específico** | La app de origen canceló el `PendingIntent` | Cythernoir abrirá la aplicación principal como respaldo de seguridad automáticamente. |

---

## 23. Privacidad y Comparativa F-Droid vs Play Store

| Característica | Flavor F-Droid | Flavor Play Store |
| :--- | :--- | :--- |
| **Código Propietario / Google Play Services** | 0% (100% Código Libre/Open Source) | 0% (No utiliza librerías propietarias) |
| **Telemetría / Tracking / Ads** | **NINGUNA** | **NINGUNA** |
| **Funcionamiento Offline** | 100% Local | 100% Local |
| **Target SDK** | SDK 37 (Android 16) | SDK 37 (Android 16) |

---

## 24. Configuración Recomendada Developer / Dracula
Para la mejor experiencia de desarrollo e ingeniería en tu teléfono:
1. **Tema:** `DRACULA_TERMINAL` (`config -set behavior theme_preset DRACULA_TERMINAL`).
2. **Gestos:**
   * Swipe Up $\rightarrow$ `apps -ls`
   * Swipe Down $\rightarrow$ `notifications`
   * Double Tap $\rightarrow$ Bloqueo de pantalla
3. **Paneles Fijados:** `visual system -fixed` y `visual battery -fixed`.
4. **Aliases Recomendados:**
   ```text
   alias -add dev "open Android Studio"
   alias -add term "open Termux"
   alias -add web "open Firefox"
   ```

---

## 25. Checklist Inicial
- [x] Establecer Cythernoir como Launcher Predeterminado.
- [x] Seleccionar el Tema **Dracula** (`settings`).
- [x] Probar el gesto deslizar arriba (**Swipe Up**) para el cajón de aplicaciones.
- [x] Probar el gesto deslizar abajo (**Swipe Down**) para el panel de notificaciones.
- [x] Crear tu primer **Alias** de comando.
- [x] Probar la **Búsqueda Universal** escribiendo el nombre de una app.
- [x] Fijar una tarjeta de monitoreo en consola con `visual system -fixed`.
- [x] Realizar una copia de seguridad local de tu configuración.
