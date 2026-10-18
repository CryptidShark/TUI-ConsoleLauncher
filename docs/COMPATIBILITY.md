# Compatibilidad y verificación

Cythernoir Launcher declara `minSdk 24`, `compileSdk 37` y `targetSdk 37`. El API declarado no equivale a una certificación ni significa que cada fabricante ofrezca el mismo comportamiento.

## Matriz de validación

| Área | Estado en este repositorio | Qué puede variar |
| --- | --- | --- |
| Instalación Android | Mínimo declarado: API 24 | Política de instalación del dispositivo |
| Aplicación de inicio | `LauncherActivity` declara categorías HOME y LAUNCHER | Flujo de selección de Home del fabricante |
| Widgets del sistema | Host Android con selección, configuración, orden, altura y borrado | Permisos del host, proveedor, tamaño de pantalla y launcher predeterminado |
| Tarjeta de música externa | Lee sesiones multimedia activas | Acceso a notificaciones, metadatos y controles que exponga cada reproductor |
| Reproductor integrado | Lee audio local cuando se concede el permiso correspondiente | Acceso a archivos y restricciones de almacenamiento de cada versión |
| BusyBox | Instalador para ABIs con hashes configurados | ABI del dispositivo, red y políticas del fabricante |

## Comprobaciones manuales recomendadas

Antes de publicar un APK, registra el modelo, fabricante, versión de Android y variante instalada. En cada dispositivo, comprueba:

1. Elegir Cythernoir como aplicación de inicio y volver al launcher tras reiniciar.
2. Añadir un widget sin configuración y otro que sí requiera configuración; cancelar ambos flujos y comprobar que no queden IDs huérfanos.
3. Cambiar el orden y altura de widgets, reiniciar la aplicación y confirmar que se restauran.
4. Conceder y retirar Acceso a notificaciones; conectar, pausar, cambiar de pista y detener dos reproductores multimedia.
5. Conceder y denegar acceso a audio; comprobar el comportamiento del reproductor integrado.
6. Cambiar workspaces y tema; exportar/importar un perfil y confirmar las opciones aplicadas.
7. Ejecutar `doctor` y `privacy` con permisos concedidos y denegados.

## Reportar un problema

Incluye el modelo del dispositivo, fabricante, versión de Android, variante (`fdroid` o `playstore`), pasos para reproducirlo y el resultado esperado/obtenido. No incluyas datos privados de notificaciones, contactos o archivos.

Esta matriz describe qué probar; no afirma que los dispositivos de una marca o versión ya hayan sido probados. Registra aquí los resultados reales de cada lanzamiento.
