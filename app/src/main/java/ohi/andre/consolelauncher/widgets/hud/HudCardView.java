package ohi.andre.consolelauncher.widgets.hud;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import ohi.andre.consolelauncher.R;

/** Lightweight terminal card with a caption cut into its top border. */
public class HudCardView extends LinearLayout {
    private final Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titleCoverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF frame = new RectF();
    private String title = "TUI";
    private int surfaceColor;
    private int borderColor;

    public HudCardView(Context context) {
        this(context, null);
    }

    public HudCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        surfaceColor = ContextCompat.getColor(context, R.color.hud_surface);
        borderColor = ContextCompat.getColor(context, R.color.hud_border);
        setOrientation(VERTICAL);
        setWillNotDraw(false);
        setPadding(dp(16), dp(20), dp(16), dp(14));
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(dp(1));
        framePaint.setColor(borderColor);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(surfaceColor);
        titleCoverPaint.setStyle(Paint.Style.FILL);
        titleCoverPaint.setColor(surfaceColor);
        titlePaint.setTypeface(Typeface.MONOSPACE);
        titlePaint.setTextSize(sp(11));
        titlePaint.setFakeBoldText(true);
        titlePaint.setColor(ContextCompat.getColor(context, R.color.hud_purple));
    }

    public void setTitle(String title) {
        this.title = title == null ? "TUI" : title.trim().toUpperCase(java.util.Locale.ROOT);
        invalidate();
    }

    public void setFrameColors(int surface, int border, int caption) {
        surfaceColor = surface;
        borderColor = border;
        framePaint.setColor(border);
        fillPaint.setColor(surface);
        titleCoverPaint.setColor(surface);
        titlePaint.setColor(caption);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = dp(1);
        frame.set(inset, dp(7), getWidth() - inset, getHeight() - inset);
        float radius = dp(10);
        canvas.drawRoundRect(frame, radius, radius, fillPaint);
        canvas.drawRoundRect(frame, radius, radius, framePaint);

        float titleX = dp(16);
        float baseline = dp(11);
        float titleWidth = titlePaint.measureText(title);
        canvas.drawRect(titleX - dp(5), 0, titleX + titleWidth + dp(5), dp(15), titleCoverPaint);
        canvas.drawText(title, titleX, baseline, titlePaint);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
