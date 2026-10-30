package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.View;
import java.util.Random;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Theme;

public class ChargingMatrixView extends View {
    private final Paint textPaint = new Paint();
    private final Paint boltPaint = new Paint();
    private final Paint glowPaint = new Paint();
    private final Random random = new Random();
    private int[] drops;
    private int fontSize = 32;
    private int columns, rows;
    private final Handler handler = new Handler();

    private final Runnable animator = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, 50);
        }
    };

    public ChargingMatrixView(Context context) {
        super(context);
        setClickable(false);
        setFocusable(false);
        init();
    }

    private void init() {
        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setTextSize(fontSize);
        textPaint.setAntiAlias(true);

        boltPaint.setTypeface(Typeface.DEFAULT_BOLD);
        boltPaint.setTextSize(180);
        boltPaint.setTextAlign(Paint.Align.CENTER);
        boltPaint.setAntiAlias(true);

        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        columns = w / fontSize;
        rows = h / fontSize;
        drops = new int[columns];
        for (int i = 0; i < columns; i++) {
            drops[i] = random.nextInt(rows);
        }
        handler.removeCallbacks(animator);
        handler.post(animator);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int color = 0;
        try {
            color = XMLPrefsManager.getColor(Theme.charging_color);
        } catch (Exception ignore) {}
        if (color == 0 || color == Color.TRANSPARENT) {
            try {
                color = XMLPrefsManager.getColor(Theme.time_color);
            } catch (Exception ignore) {}
        }
        if (color == 0) color = Color.parseColor("#00FFD1");

        // Semi-transparent dark cyber background
        canvas.drawColor(Color.parseColor("#CC04070A"));

        // Matrix binary streams rising from bottom to top
        textPaint.setColor(color);
        textPaint.setAlpha(170);

        for (int i = 0; i < drops.length; i++) {
            int x = i * fontSize;
            int y = drops[i] * fontSize;

            char digit = random.nextBoolean() ? '1' : '0';
            canvas.drawText(String.valueOf(digit), x, y, textPaint);

            // Move drop UP (from bottom to top)
            drops[i]--;
            if (drops[i] < 0) {
                drops[i] = rows;
            }
        }

        // Center large glowing lightning bolt ⚡
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f - 30;

        glowPaint.setColor(color);
        glowPaint.setAlpha(45);
        canvas.drawCircle(cx, cy, 160, glowPaint);

        boltPaint.setColor(color);
        canvas.drawText("⚡", cx, cy + 60, boltPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(animator);
    }
}
