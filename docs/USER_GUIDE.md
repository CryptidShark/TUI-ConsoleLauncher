# Cythernoir Launcher — guía de usuario

Cythernoir Launcher es un fork independiente de T-UI Console Launcher. La pantalla de inicio combina una terminal de comandos con accesos a aplicaciones, sugerencias y tarjetas fijadas. Esta guía describe funciones presentes en el código de este repositorio.

## Primer inicio

1. Instala y abre la aplicación **Cythernoir Launcher**.
2. Para alojar widgets, selecciónala como aplicación de inicio predeterminada si Android lo solicita.
3. Ejecuta `doctor` para revisar requisitos y `help` para ver los comandos disponibles.
4. Usa `privacy` para consultar permisos opcionales y abrir sus ajustes de Android.

Los permisos dependen de las funciones utilizadas. Puedes negar los permisos que no necesites.

## Comandos para empezar

| Comando | Función |
| --- | --- |
| `help` / `help [comando]` | Lista comandos y muestra ayuda disponible |
| `apps -ls` | Consulta aplicaciones instaladas |
| `palette [consulta]` | Busca aplicaciones, comandos, aliases, accesos directos y workspaces; toca un resultado para ejecutarlo |
| `alias` | Administra aliases |
| `search -gg [consulta]` | Busca en Google; también existen opciones para YouTube, Play Store, DuckDuckGo y URL |
| `settings` | Abre los ajustes del launcher |
| `doctor` | Revisa la configuración del launcher |
| `privacy` | Muestra permisos y accesos opcionales |
| `history list` / `history find <texto>` | Muestra o busca comandos recientes |

La sintaxis completa depende de cada comando; consulta `help [comando]`.

## Gestos rápidos

Configura los gestos desde la terminal sin abrir el teclado después de cada toque:

```text
config -set swipe_up_cmd search
config -set swipe_down_cmd notifications
config -set swipe_left_cmd workspace next
config -set swipe_right_cmd workspace prev
config -set double_tap_lock false
config -set double_tap_cmd time
```

El doble toque conserva el bloqueo de pantalla si `double_tap_lock` está activo. Desactiva ese ajuste con `config -set double_tap_lock false` si quieres que el doble toque ejecute solo el comando asignado.

## Tarjetas y apariencia

Fija las tarjetas que quieras en la parte superior del historial:

```text
visual battery -fixed
visual system -fixed
visual notes -fixed
visual shortcuts -fixed
visual music -fixed
visual clear
```

Los ajustes muestran los 15 temas disponibles. Al elegir uno se actualiza la paleta completa de la consola, sugerencias, indicadores y tarjetas. Para probarlo sin cambiar el tema actual:

```text
theme preview dracula
theme preview windows
theme preview catppuccin-latte
```

La tarjeta de vista previa incluye una acción para aplicar el tema. También se puede aplicar directamente con `theme -preset dracula` o cambiar un color individual con `theme -set <opción> <color>`.

Escribe un comando y deja un espacio para ver las acciones disponibles en la fila de sugerencias. Esto incluye `widget `, `dock `, `visual `, `workspace `, `overlay ` y `history `; los comandos con parámetros como `config`, `theme` y `notes` sugieren sus opciones.

### Workspaces

Un workspace aplica un conjunto de tarjetas fijadas:

```text
workspace list
workspace set Developer
workspace set Privacy
workspace set web_dev
workspace set authorized_lab
workspace set Custom
```

`Web Dev` fija accesos de desarrollo, telemetría y el estado de la integración Termux. `Authorized Lab` fija notas, telemetría y Termux para flujos de laboratorio autorizados. Los otros perfiles cubren uso general, privacidad, ciberestética y música. `Custom` conserva las tarjetas elegidas manualmente; los perfiles predefinidos sustituyen el conjunto de tarjetas ancladas.

## Música

El reproductor integrado admite canciones locales y ofrece comandos como `music ls`, `music play`, `music next`, `music previous`, `music info` y `music stop`. El acceso a la biblioteca de audio se administra desde los ajustes de Android; `doctor` y `privacy` muestran su estado.

La tarjeta `visual music -fixed` detecta sesiones multimedia externas mediante **Acceso a notificaciones**. Muestra metadatos y envía controles de reproducción a la sesión seleccionada. La animación ASCII es decorativa y no analiza el sonido.

## Widgets Android

```text
widget add
widget list
widget move [ID] up
widget height [ID] [dp]
widget remove [ID]
widget clear
```

`widget ` muestra estas opciones en las sugerencias. Primero ejecuta `widget list` para obtener el ID que se usa en `move`, `height` y `remove`. Si la fila de sugerencias está oculta, habilítala con `config -set show_suggestions true`.

El selector y las pantallas de configuración pertenecen a Android y a cada aplicación proveedora. La compatibilidad, el enlace y las dimensiones pueden variar según el fabricante y la versión del sistema. Si agregar falla, establece el launcher como aplicación de inicio predeterminada y revisa `doctor`.

## Perfiles y restauración

Exporta la configuración del launcher a un archivo JSON y guarda una copia fuera del dispositivo:

```text
config -export_profile cythernoir.json
config -import_profile cythernoir.json
```

El perfil incluye ajustes de comportamiento, interfaz, colores, sugerencias, comandos, workspace y Dock. La vista previa valida los tipos y valores antes de aplicar cambios. Cada importación guarda una copia previa con nombre `profile_before_import_<marca_de_tiempo>.json`; los datos privados de otras aplicaciones y los widgets Android externos no se respaldan.

## Dock lateral

```text
alias add w WhatsApp
dock add w
dock icon w 📱
dock side left
dock size compact
dock enable
```

También puedes cambiar el borde y el tamaño desde `settings`. El tamaño admite `compact`, `normal` o `large`; el borde, `left` o `right`. Ejecuta `dock list` para revisar el estado y `dock icon w reset` para volver al alias de texto.

## Historial de comandos

`history list` muestra los comandos guardados, `history find <texto>` filtra coincidencias y `history run <número>` coloca uno en la línea de entrada para que puedas revisarlo antes de ejecutarlo. Las teclas/flechas de anterior y siguiente siguen recorriendo los comandos de la sesión. El modo incógnito no guarda ni muestra historial persistente.

## Herramientas opcionales

`bbman -install` descarga BusyBox para las arquitecturas admitidas y verifica el SHA-256 antes de instalarlo. Requiere Internet. Para eliminarlo, ejecuta `bbman -remove`.

Otros comandos ofrecen acceso a notas, notificaciones, contactos, RSS, ubicación, controles de volumen, brillo, Wi-Fi, Bluetooth y funciones de Android. Algunos requieren permisos o dependen de restricciones del sistema.

## Compatibilidad y ayuda

El proyecto declara Android mínimo API 24 y compila/destina a API 37. Consulta [COMPATIBILITY.md](COMPATIBILITY.md) para la matriz de comprobación y las diferencias conocidas entre dispositivos. No se garantiza que todos los widgets o reproductores de terceros funcionen igual en cada dispositivo.

Para conocer la versión del código, revisa `versionName` en `app/build.gradle`. Los cambios del fork se publican en este repositorio. Cythernoir conserva atribución al proyecto base T-UI Console Launcher y a sus autores originales.
