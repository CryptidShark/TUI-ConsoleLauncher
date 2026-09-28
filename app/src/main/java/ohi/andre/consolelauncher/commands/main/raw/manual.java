package ohi.andre.consolelauncher.commands.main.raw;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;

public class manual implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) throws Exception {
        String sb = "--- MANUAL DE PERSONALIZACIÓN T-UI ---\n\n" +
                "📂 ARCHIVOS EN: /sdcard/t-ui/\n\n" +
                "🎨 theme.xml (Colores)\n" +
                "- bg_color: Color de fondo (#AARRGGBB para transparencia)\n" +
                "- input_color: Color de lo que escribes\n" +
                "- output_color: Color de la terminal\n" +
                "- accent_color: Color de botones y barras\n\n" +
                "📐 ui.xml (Tamaños y Márgenes)\n" +
                "- input_output_size: Tamaño de letra (ej: 15)\n" +
                "- system_font: 'true' para usar la fuente de tu móvil\n" +
                "- margins: Formato 'Izquierda,Arriba,Derecha,Abajo'\n\n" +
                "🖼️ TRANSPARENCIA (Wallpaper):\n" +
                "1. Escribe: '$ settings live' y pulsa 'Wallpaper'\n" +
                "2. Reinicia con: 'restart'\n" +
                "3. Ajusta: 'config -set bg_color #80000000' (para 50% transparencia)\n" +
                "Nota: Si no ves tu fondo, asegúrate de tener una imagen puesta en Android.\n\n" +
                "🎵 MÚSICA (SimpMusic/Spotify):\n" +
                "- Usa 'visual music -fixed' para el reproductor ASCII\n" +
                "- IMPORTANTE: Debes dar permiso de 'Acceso a Notificaciones' a T-UI en ajustes de Android para que detecte las canciones.\n\n" +
                "🚀 TRUCOS:\n" +
                "- Usa 'visual music -fixed' para el reproductor ASCII\n" +
                "- Si no detecta nada, ACTIVA el 'Acceso a Notificaciones' para T-UI en ajustes de Android.\n\n" +
                "🚀 TRUCOS:\n" +
                "- '$ settings live': Personaliza sin archivos XML\n" +
                "- '$ visual [nombre] -fixed': Ancla módulos arriba\n" +
                "- '$ widget add': Añade widgets reales\n" +
                "- IMPORTANTE: T-UI debe ser el launcher PREDETERMINADO para que los widgets funcionen bien.";

        return sb;
    }

    @Override
    public int[] argType() {
        return new int[0];
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public int helpRes() {
        return 0;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int indexNotFound) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return null;
    }
}
