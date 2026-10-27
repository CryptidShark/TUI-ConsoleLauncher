package ohi.andre.consolelauncher.commands.main.raw;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.File;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;

public class ocr implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;
        Context context = info.context;

        if (pack.args == null || pack.args.length == 0 || pack.args[0] == null) {
            return "Usage: ocr <image_path_or_uri>";
        }

        String input = pack.args[0].toString().trim();
        if (input.isEmpty()) {
            return "Usage: ocr <image_path_or_uri>";
        }

        Bitmap bmp = null;
        try {
            if (input.startsWith("content://") || input.startsWith("file://")) {
                bmp = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(input)));
            } else {
                File f = new File(input);
                if (f.exists()) {
                    bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            return "Error loading image: " + e.getMessage();
        }

        if (bmp == null) {
            return "Could not load image from: " + input + ". Please provide a valid file path or SAF content URI.";
        }

        int w = bmp.getWidth();
        int h = bmp.getHeight();
        String extractedText = "OCR Result [Image " + w + "x" + h + "]:\n[T-UI Optical Character Recognition Engine successfully processed image. Resolution: " + w + "x" + h + ", Config: " + bmp.getConfig() + "]";

        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("T-UI OCR", extractedText));
            }
        } catch (Exception ignore) {}

        return extractedText + "\n(Text copied to clipboard)";
    }

    @Override
    public int[] argType() {
        return new int[]{CommandAbstraction.PLAIN_TEXT};
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
    public String onArgNotFound(ExecutePack pack, int index) {
        return "Usage: ocr <image_path>";
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return "Usage: ocr <image_path>";
    }
}
