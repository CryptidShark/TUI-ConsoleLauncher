package ohi.andre.consolelauncher.widgets.hud;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import ohi.andre.consolelauncher.R;

/** Compact, accessible circular HUD meter. */
public class HudGaugeView extends View {
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint percentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private int value;
    private int accentColor;
    private int trackColor;
    private int textColor;
    private int mutedColor;
    private String label = "SYSTEM";

    public HudGaugeView(Context context) {
        this(context, null);
    }

    public HudGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        setMinimumWidth(Math.round(68 * density));
        setMinimumHeight(Math.round(72 * density));
        trackColor = ContextCompat.getColor(context, R.color.hud_border);
        textColor = ContextCompat.getColor(context, R.color.hud_text_primary);
        mutedColor = ContextCompat.getColor(context, R.color.hud_text_secondary);
        accentColor = ContextCompat.getColor(context, R.color.hud_cyan);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(3 * density);
        trackPaint.setColor(trackColor);
        valuePaint.setStyle(Paint.Style.STROKE);
        valuePaint.setStrokeCap(Paint.Cap.ROUND);
        valuePaint.setStrokeWidth(3 * density);
        valuePaint.setColor(accentColor);
        percentPaint.setTypeface(Typeface.create("monospace", Typeface.BOLD));
        percentPaint.setTextAlign(Paint.Align.CENTER);
        percentPaint.setTextSize(15 * getResources().getDisplayMetrics().scaledDensity);
        percentPaint.setColor(textColor);
        labelPaint.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(8 * getResources().getDisplayMetrics().scaledDensity);
        labelPaint.setColor(mutedColor);
        setContentDescription(label + " 0 percent");
    }

    public void setValue(String label, int value, int accentColor) {
        this.label = label == null ? "SYSTEM" : label.toUpperCase(java.util.Locale.ROOT);
        this.value = Math.max(0, Math.min(100, value));
        this.accentColor = accentColor;
        valuePaint.setColor(accentColor);
        setContentDescription(this.label + " " + this.value + " percent");
        invalidate();
    }

    public void setColors(int track, int text, int muted) {
        trackColor = track;
        textColor = text;
        mutedColor = muted;
        trackPaint.setColor(track);
        percentPaint.setColor(text);
        labelPaint.setColor(muted);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float size = Math.min(getWidth(), getHeight() - 12 * density);
        float stroke = 3 * density;
        float left = (getWidth() - size) / 2f + stroke;
        float top = stroke;
        arcBounds.set(left, top, left + size - 2 * stroke, top + size - 2 * stroke);
        canvas.drawArc(arcBounds, 135, 270, false, trackPaint);
        canvas.drawArc(arcBounds, 135, 270 * (value / 100f), false, valuePaint);

        float centerX = getWidth() / 2f;
        float centerY = arcBounds.centerY();
        Paint.FontMetrics metrics = percentPaint.getFontMetrics();
        canvas.drawText(value + "%", centerX, centerY - (metrics.ascent + metrics.descent) / 2f, percentPaint);
        canvas.drawText(label, centerX, getHeight() - 2 * density, labelPaint);
    }
}
