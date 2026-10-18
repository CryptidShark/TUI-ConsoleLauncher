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

La sintaxis completa depende de cada comando; consulta `help [comando]`.

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

Los presets visuales incluyen `CLASSIC_TERMINAL`, `MODERN_TERMINAL`, `CYBER_TERMINAL`, `NEO_TERMINAL`, `AMBER_TERMINAL`, `NORD_TERMINAL`, `DRACULA_TERMINAL`, `AMOLED_TERMINAL` y `WATCHDOGS_CYBER`. También se puede cambiar una opción de color desde `theme` o `config`. Ejecuta `help theme` para ver las opciones disponibles.

### Workspaces

Un workspace aplica un conjunto de tarjetas fijadas:

```text
workspace list
workspace set Developer
workspace set Privacy
workspace set Custom
```

`Custom` conserva las tarjetas elegidas manualmente. Los otros perfiles sustituyen el conjunto de tarjetas ancladas.

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

El selector y las pantallas de configuración pertenecen a Android y a cada aplicación proveedora. La compatibilidad, el enlace y las dimensiones pueden variar según el fabricante y la versión del sistema. Si agregar falla, establece el launcher como aplicación de inicio predeterminada y revisa `doctor`.

## Perfiles y restauración

Exporta la configuración del launcher a un archivo JSON y guarda una copia fuera del dispositivo:

```text
config -export_profile cythernoir.json
config -import_profile cythernoir.json
```

El perfil incluye ajustes de comportamiento, interfaz, colores, sugerencias y comandos. No incluye los datos privados de otras aplicaciones ni garantiza el respaldo de widgets externos. Importar un perfil reemplaza las opciones incluidas; haz una exportación antes de importar.

## Herramientas opcionales

`bbman -install` descarga BusyBox para las arquitecturas admitidas y verifica el SHA-256 antes de instalarlo. Requiere Internet. Para eliminarlo, ejecuta `bbman -remove`.

Otros comandos ofrecen acceso a notas, notificaciones, contactos, RSS, ubicación, controles de volumen, brillo, Wi-Fi, Bluetooth y funciones de Android. Algunos requieren permisos o dependen de restricciones del sistema.

## Compatibilidad y ayuda

El proyecto declara Android mínimo API 24 y compila/destina a API 37. Consulta [COMPATIBILITY.md](COMPATIBILITY.md) para la matriz de comprobación y las diferencias conocidas entre dispositivos. No se garantiza que todos los widgets o reproductores de terceros funcionen igual en cada dispositivo.

Para conocer la versión del código, revisa `versionName` en `app/build.gradle`. Los cambios del fork se publican en este repositorio. Cythernoir conserva atribución al proyecto base T-UI Console Launcher y a sus autores originales.
