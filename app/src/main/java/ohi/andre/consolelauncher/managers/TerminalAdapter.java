package ohi.andre.consolelauncher.managers;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.tuils.LongClickMovementMethod;
import ohi.andre.consolelauncher.tuils.LongClickableSpan;
import ohi.andre.consolelauncher.tuils.Tuils;

public class TerminalAdapter extends RecyclerView.Adapter<TerminalAdapter.ViewHolder> {

    private static final List<TerminalItem> sHistoryCache = new ArrayList<>();
    private static final int MAX_HISTORY_ITEMS = 150;

    private final List<TerminalItem> items = new ArrayList<>();
    private final Context context;
    private final int fontSize;
    private ThemeEngine.DesignTokens theme;

    public TerminalAdapter(Context context, int fontSize) {
        this.context = context;
        this.fontSize = fontSize;
        updateTheme();
        
        // Restore recent text messages from static history cache when Activity recreates
        for (TerminalItem cachedItem : sHistoryCache) {
            this.items.add(cachedItem);
        }
    }

    public void updateTheme() {
        String presetName = XMLPrefsManager.get(Behavior.theme_preset);
        ThemeEngine.Preset preset;
        try {
            preset = ThemeEngine.Preset.valueOf(presetName);
        } catch (Exception e) {
            preset = ThemeEngine.Preset.CLASSIC_TERMINAL;
        }
        this.theme = ThemeEngine.getPreset(preset);
        notifyDataSetChanged();
    }

    public void add(TerminalItem item) {
        items.add(item);
        if (item.getType() == TerminalItem.Type.TEXT) {
            if (sHistoryCache.size() >= MAX_HISTORY_ITEMS) {
                sHistoryCache.remove(0);
            }
            sHistoryCache.add(item);
        }
        notifyItemInserted(items.size() - 1);
    }

    public void clear() {
        int size = items.size();
        items.clear();
        sHistoryCache.clear();
        notifyItemRangeRemoved(0, size);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType().ordinal();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = viewType == TerminalItem.Type.TEXT.ordinal() ? R.layout.terminal_item_text : R.layout.terminal_item_card;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TerminalItem item = items.get(position);
        if (item.getType() == TerminalItem.Type.TEXT) {
            TextView textView = (TextView) holder.itemView;
            textView.setTextSize(fontSize);
            textView.setTypeface(Tuils.getTypeface(context));
            textView.setText(item.getText());
            
            if (item.getColor() != TerminalManager.NO_COLOR) {
                textView.setTextColor(item.getColor());
            } else {
                textView.setTextColor(theme.text);
            }

            textView.setMovementMethod(LongClickMovementMethod.getInstance(XMLPrefsManager.getInt(Behavior.long_click_duration)));

            if (item.getText() instanceof Spanned) {
                Spanned spanned = (Spanned) item.getText();
                LongClickableSpan[] spans = spanned.getSpans(0, spanned.length(), LongClickableSpan.class);
                if (spans != null && spans.length > 0) {
                    final LongClickableSpan span = spans[0];
                    textView.setOnClickListener(v -> {
                        v.animate().alpha(0.4f).setDuration(80).withEndAction(() -> {
                            v.animate().alpha(1.0f).setDuration(80).start();
                            span.onClick(v);
                        }).start();
                    });
                    textView.setOnLongClickListener(v -> {
                        v.animate().alpha(0.4f).setDuration(80).withEndAction(() -> {
                            v.animate().alpha(1.0f).setDuration(80).start();
                            span.onLongClick(v);
                        }).start();
                        return true;
                    });
                } else {
                    textView.setOnClickListener(null);
                    textView.setOnLongClickListener(null);
                    textView.setClickable(false);
                }
            } else {
                textView.setOnClickListener(null);
                textView.setOnLongClickListener(null);
                textView.setClickable(false);
            }
        } else {
            FrameLayout container = holder.itemView.findViewById(R.id.card_container);
            container.removeAllViews();
            
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(theme.surface);
            gd.setCornerRadius(Tuils.dpToPx(context, (int) theme.borderRadius > 0 ? (int) theme.borderRadius : 6));
            gd.setStroke((int) Tuils.dpToPx(context, (int) theme.borderWidth > 0 ? (int) theme.borderWidth : 1), theme.border);
            container.setBackground(gd);

            View customView = item.getCustomView();
            if (customView != null) {
                if (customView.getParent() != null) {
                    ((ViewGroup) customView.getParent()).removeView(customView);
                }

                if (customView instanceof AppWidgetHostView) {
                    AppWidgetHostView hostView = (AppWidgetHostView) customView;
                    AppWidgetProviderInfo info = hostView.getAppWidgetInfo();
                    
                    LinearLayout wrapper = new LinearLayout(context);
                    wrapper.setOrientation(LinearLayout.VERTICAL);
                    wrapper.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    String titleText = "EXTERNAL_MODULE";
                    if (info != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            titleText = info.loadLabel(context.getPackageManager());
                        } else {
                            titleText = info.label;
                        }
                    }

                    TextView header = new TextView(context);
                    header.setText("● [ " + (titleText != null ? titleText.toUpperCase() : "EXTERNAL_MODULE") + " ]");
                    header.setTextColor(theme.primary);
                    header.setTextSize(fontSize * 0.7f);
                    header.setTypeface(Tuils.getTypeface(context));
                    header.setPadding(Tuils.dpToPx(context, 12), Tuils.dpToPx(context, 8), Tuils.dpToPx(context, 12), Tuils.dpToPx(context, 6));
                    wrapper.addView(header);

                    View line = new View(context);
                    line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Tuils.dpToPx(context, 1)));
                    line.setBackgroundColor(theme.border);
                    line.setAlpha(0.35f);
                    wrapper.addView(line);

                    hostView.setPadding(0, 0, 0, 0);
                    LinearLayout.LayoutParams hostParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    hostParams.setMargins(Tuils.dpToPx(context, 4), Tuils.dpToPx(context, 4), Tuils.dpToPx(context, 4), Tuils.dpToPx(context, 4));
                    hostView.setLayoutParams(hostParams);
                    wrapper.addView(hostView);
                    
                    TextView footer = new TextView(context);
                    footer.setText("<< MODULE_ID: " + hostView.getAppWidgetId() + " // STATUS: ONLINE");
                    footer.setTextColor(theme.textMuted);
                    footer.setTextSize(fontSize * 0.6f);
                    footer.setTypeface(Tuils.getTypeface(context));
                    footer.setPadding(Tuils.dpToPx(context, 12), Tuils.dpToPx(context, 2), Tuils.dpToPx(context, 12), Tuils.dpToPx(context, 8));
                    wrapper.addView(footer);

                    container.addView(wrapper);
                } else {
                    container.addView(customView);
                    applyThemeRecursively(customView);
                }
            }
        }

        setAnimation(holder.itemView, position);
    }

    private void applyThemeRecursively(View view) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(theme.text);
            ((TextView) view).setTypeface(Tuils.getTypeface(context));
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyThemeRecursively(group.getChildAt(i));
            }
        }
    }

    private int lastPosition = -1;
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final int type;

        ViewHolder(View view, int type) {
            super(view);
            this.type = type;
        }
    }
}
