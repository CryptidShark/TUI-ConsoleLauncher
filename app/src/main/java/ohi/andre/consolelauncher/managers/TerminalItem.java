package ohi.andre.consolelauncher.managers;

import android.view.View;

public class TerminalItem {

    public enum Type {
        TEXT,
        CARD,
        WIDGET
    }

    private final Type type;
    private final CharSequence text;
    private final View customView;
    private final int color;

    public TerminalItem(CharSequence text) {
        this(Type.TEXT, text, null, TerminalManager.NO_COLOR);
    }

    public TerminalItem(CharSequence text, int color) {
        this(Type.TEXT, text, null, color);
    }

    public TerminalItem(View view) {
        this(Type.WIDGET, null, view, TerminalManager.NO_COLOR);
    }

    private TerminalItem(Type type, CharSequence text, View customView, int color) {
        this.type = type;
        this.text = text;
        this.customView = customView;
        this.color = color;
    }

    public Type getType() {
        return type;
    }

    public CharSequence getText() {
        return text;
    }

    public View getCustomView() {
        return customView;
    }

    public int getColor() {
        return color;
    }
}
