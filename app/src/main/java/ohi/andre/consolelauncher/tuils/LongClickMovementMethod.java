package ohi.andre.consolelauncher.tuils;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/** Handles taps and long presses on the launcher’s custom clickable spans. */
public class LongClickMovementMethod extends LinkMovementMethod {

    private int longClickDuration;
    private int lastLine = -1;
    private boolean trackingTouch;
    private LongClickableSpan activeSpan;

    private abstract class WasActivatedRunnable implements Runnable {
        boolean wasActivated;

        @Override public void run() {
            wasActivated = true;
        }
    }

    private WasActivatedRunnable runnable;

    @Override
    public boolean onTouchEvent(final TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL) {
            if (!trackingTouch) return super.onTouchEvent(widget, buffer, event);
            cancelTouch(widget);
            return true;
        }

        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE
                && action != MotionEvent.ACTION_UP) {
            return super.onTouchEvent(widget, buffer, event);
        }

        Layout layout = widget.getLayout();
        if (layout == null) return super.onTouchEvent(widget, buffer, event);

        int x = (int) event.getX() - widget.getTotalPaddingLeft() + widget.getScrollX();
        int y = (int) event.getY() - widget.getTotalPaddingTop() + widget.getScrollY();
        int line = layout.getLineForVertical(y);
        LongClickableSpan touchedSpan = findSpanAt(buffer, layout, line, x, y);

        if (action == MotionEvent.ACTION_DOWN) {
            if (touchedSpan == null) return super.onTouchEvent(widget, buffer, event);

            trackingTouch = true;
            activeSpan = touchedSpan;
            runnable = new WasActivatedRunnable() {
                @Override public void run() {
                    super.run();
                    touchedSpan.onLongClick(widget);
                }
            };
            if (longClickDuration > 0) widget.postDelayed(runnable, longClickDuration);
            lastLine = line;
            return true;
        }

        if (!trackingTouch) return super.onTouchEvent(widget, buffer, event);

        if (action == MotionEvent.ACTION_MOVE) {
            if (line != lastLine || touchedSpan != activeSpan) {
                if (runnable != null) widget.removeCallbacks(runnable);
                runnable = null;
            }
            lastLine = line;
            return true;
        }

        // ACTION_UP: only click if the gesture began and ended over a span.
        if (runnable != null) {
            widget.removeCallbacks(runnable);
            if (!runnable.wasActivated && touchedSpan == activeSpan) {
                activeSpan.onClick(widget);
            }
        }
        cancelTouch(widget);
        return true;
    }

    private LongClickableSpan findSpanAt(Spannable buffer, Layout layout, int line, int x, int y) {
        if (y < 0 || y > layout.getHeight()
                || x < layout.getLineLeft(line) || x > layout.getLineRight(line)) return null;
        int offset = layout.getOffsetForHorizontal(line, x);
        LongClickableSpan[] spans = buffer.getSpans(offset, offset, LongClickableSpan.class);
        return spans.length == 0 ? null : spans[0];
    }

    private void cancelTouch(TextView widget) {
        if (runnable != null) widget.removeCallbacks(runnable);
        runnable = null;
        trackingTouch = false;
        activeSpan = null;
        lastLine = -1;
    }

    private static LongClickMovementMethod sInstance;

    public static MovementMethod getInstance(int longClickDuration) {
        if (sInstance == null) sInstance = new LongClickMovementMethod();
        sInstance.longClickDuration = longClickDuration;
        return sInstance;
    }

    public static MovementMethod getInstance() {
        return getInstance(-1);
    }
}
