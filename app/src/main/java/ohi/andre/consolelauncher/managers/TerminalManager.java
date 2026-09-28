package ohi.andre.consolelauncher.managers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.IBinder;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.ThemeEngine;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.managers.xml.options.Theme;
import ohi.andre.consolelauncher.managers.xml.options.Ui;
import ohi.andre.consolelauncher.tuils.PrivateIOReceiver;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.tuils.interfaces.CommandExecuter;

public class TerminalManager {

    private final int SCROLL_DELAY = 200;
    private final int CMD_LIST_SIZE = 40;

    public static final int CATEGORY_INPUT = 10, CATEGORY_OUTPUT = 11, CATEGORY_NO_COLOR = 20;
    public static int NO_COLOR = Integer.MAX_VALUE;

    private long lastEnter;
    private String prefix;
    private String suPrefix;

    private RecyclerView mTerminalList;
    private TerminalAdapter mAdapter;
    private EditText mInputView;
    private TextView mPrefix;
    private boolean suMode;

    private List<String> cmdList = new ArrayList<>(CMD_LIST_SIZE);
    private int howBack = -1;

    private Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAdapter != null && mAdapter.getItemCount() > 0) {
                mTerminalList.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            }
            mInputView.requestFocus();
        }
    };

    private MainPack mainPack;
    private boolean defaultHint = true;
    private int clearCmdsCount = 0;
    private int clearAfterCmds, clearAfterMs, maxLines;

    private Runnable clearRunnable = new Runnable() {
        @Override
        public void run() {
            clear();
            if (mTerminalList != null) mTerminalList.postDelayed(this, clearAfterMs);
        }
    };

    private String inputFormat, outputFormat;
    private int inputColor, outputColor;
    private boolean clickCommands, longClickCommands;

    public Context mContext;
    private CommandExecuter executer;

    public TerminalManager(final View terminalView, EditText inputView, TextView prefixView, ImageView submitView, final ImageView backView, ImageButton nextView, ImageButton deleteView,
                           ImageButton pasteView, final Context context, MainPack mainPack, CommandExecuter executer) {
        
        if (terminalView == null || inputView == null || prefixView == null)
            throw new UnsupportedOperationException();

        this.mContext = context;
        this.executer = executer;
        this.mainPack = mainPack;

        this.clickCommands = XMLPrefsManager.getBoolean(Behavior.click_commands);
        this.longClickCommands = XMLPrefsManager.getBoolean(Behavior.long_click_commands);
        this.clearAfterMs = XMLPrefsManager.getInt(Behavior.clear_after_seconds) * 1000;
        this.clearAfterCmds = XMLPrefsManager.getInt(Behavior.clear_after_cmds);
        this.maxLines = XMLPrefsManager.getInt(Behavior.max_lines);

        inputFormat = XMLPrefsManager.get(Behavior.input_format);
        outputFormat = XMLPrefsManager.get(Behavior.output_format);
        inputColor = XMLPrefsManager.getColor(Theme.input_color);
        outputColor = XMLPrefsManager.getColor(Theme.output_color);

        prefix = XMLPrefsManager.get(Ui.input_prefix);
        suPrefix = XMLPrefsManager.get(Ui.input_root_prefix);

        String presetName = XMLPrefsManager.get(Behavior.theme_preset);
        ThemeEngine.Preset preset;
        try {
            preset = ThemeEngine.Preset.valueOf(presetName);
        } catch (Exception e) {
            preset = ThemeEngine.Preset.CLASSIC_TERMINAL;
        }
        ThemeEngine.DesignTokens designTokens = ThemeEngine.getPreset(preset);

        int ioSize = XMLPrefsManager.getInt(Ui.input_output_size);

        prefixView.setTypeface(Tuils.getTypeface(context));
        prefixView.setTextColor(designTokens.primary);
        prefixView.setTextSize(ioSize);
        prefixView.setText(prefix.endsWith(Tuils.SPACE) ? prefix : prefix + Tuils.SPACE);
        this.mPrefix = prefixView;

        int toolbarColor = XMLPrefsManager.getColor(Theme.toolbar_color);

        if (submitView != null) {
            submitView.setColorFilter(designTokens.primary, PorterDuff.Mode.SRC_IN);
            submitView.setOnClickListener(v -> onNewInput());
        }

        if (backView != null) {
            backView.setColorFilter(toolbarColor, PorterDuff.Mode.SRC_IN);
            backView.setBackgroundColor(0);
            backView.setOnClickListener(v -> onBackPressed());
        }

        if (nextView != null) {
            nextView.setColorFilter(toolbarColor, PorterDuff.Mode.SRC_IN);
            nextView.setBackgroundColor(0);
            nextView.setOnClickListener(v -> onNextPressed());
        }

        if (pasteView != null) {
            pasteView.setColorFilter(toolbarColor, PorterDuff.Mode.SRC_IN);
            pasteView.setBackgroundColor(0);
            pasteView.setOnClickListener(v -> {
                String text = Tuils.getTextFromClipboard(context);
                if(text != null && text.length() > 0) {
                    setInput(getInput() + text);
                }
            });
        }

        if (deleteView != null) {
            deleteView.setColorFilter(toolbarColor, PorterDuff.Mode.SRC_IN);
            deleteView.setBackgroundColor(0);
            deleteView.setOnClickListener(v -> setInput(Tuils.EMPTYSTRING));
        }

        this.mTerminalList = (RecyclerView) terminalView;
        this.mAdapter = new TerminalAdapter(context, ioSize);
        this.mTerminalList.setLayoutManager(new LinearLayoutManager(context));
        this.mTerminalList.setAdapter(mAdapter);

        if(clearAfterMs > 0) this.mTerminalList.postDelayed(clearRunnable, clearAfterMs);

        this.mInputView = inputView;
        this.mInputView.setTextSize(ioSize);
        this.mInputView.setTextColor(designTokens.text);
        this.mInputView.setTypeface(Tuils.getTypeface(context));
        this.mInputView.setHint(Tuils.getHint(mainPack.currentDirectory.getAbsolutePath()));
        this.mInputView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        Tuils.setCursorDrawableColor(this.mInputView, designTokens.cursor);
        this.mInputView.setHighlightColor(Color.TRANSPARENT);
        this.mInputView.setOnEditorActionListener((v1, actionId, event) -> {
            if(!mInputView.hasFocus()) mInputView.requestFocus();

            if(actionId == KeyEvent.ACTION_DOWN) {
                if(lastEnter == 0) {
                    lastEnter = System.currentTimeMillis();
                } else {
                    long difference = System.currentTimeMillis() - lastEnter;
                    lastEnter = System.currentTimeMillis();
                    if(difference < 350) {
                        return true;
                    }
                }
            }

            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.ACTION_DOWN) {
                onNewInput();
            }

            return true;
        });
    }

    private void setupNewInput() {
        mInputView.setText(Tuils.EMPTYSTRING);
        if(defaultHint) {
            mInputView.setHint(Tuils.getHint(mainPack.currentDirectory.getAbsolutePath()));
        }
        requestInputFocus();
    }

    private boolean onNewInput() {
        if (mInputView == null) return false;

        CharSequence input = mInputView.getText();
        String cmd = input.toString().trim();

        Object obj = null;
        try {
            obj = ((Spannable) input).getSpans(0, input.length(), AppsManager.LaunchInfo.class)[0];
        } catch (Exception e) {}

        if(input.length() > 0) {
            clearCmdsCount++;
            if(clearCmdsCount != 0 && clearAfterCmds > 0 && clearCmdsCount % clearAfterCmds == 0) clear();

            writeToView(input, CATEGORY_INPUT);

            if(cmdList.size() == CMD_LIST_SIZE) {
                cmdList.remove(0);
            }
            cmdList.add(cmdList.size(), cmd);
            howBack = -1;
        }

        executer.execute(cmd, obj);
        setupNewInput();
        return true;
    }

    public void setOutput(CharSequence output, int type) {
        if (output == null || output.length() == 0) return;
        writeToView(output, type);
    }

    public void setOutput(int color, CharSequence output) {
        if(output == null || output.length() == 0) return;

        if(color == TerminalManager.NO_COLOR) {
            color = XMLPrefsManager.getColor(Theme.output_color);
        }

        SpannableString si = new SpannableString(output);
        si.setSpan(new ForegroundColorSpan(color), 0, output.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        writeToView(si);
    }

    public void setOutput(View view) {
        if (view == null) return;
        mAdapter.add(new TerminalItem(view));
        scrollToEnd();
    }

    public String getTerminalText() {
        // This is a bit tricky now with RecyclerView, maybe just join all text items
        return "Terminal history...";
    }

    public void onBackPressed() {
        if(cmdList.size() > 0) {
            if(howBack == -1) howBack = cmdList.size();
            else if(howBack == 0) return;
            howBack--;
            setInput(cmdList.get(howBack));
        }
    }

    public void onNextPressed() {
        if(howBack != -1 && howBack < cmdList.size()) {
            howBack++;
            String input = (howBack == cmdList.size()) ? Tuils.EMPTYSTRING : cmdList.get(howBack);
            setInput(input);
        }
    }

    public static final String FORMAT_INPUT = "%i";
    public static final String FORMAT_OUTPUT = "%o";
    public static final String FORMAT_PREFIX = "%p";
    public static final String FORMAT_NEWLINE = "%n";

    private void writeToView(CharSequence text, int type) {
        text = getFinalText(text, type);
        writeToView(text);
    }

    private void writeToView(final CharSequence text) {
        mTerminalList.post(() -> {
            mAdapter.add(new TerminalItem(text));
            scrollToEnd();
        });
    }

    private CharSequence getFinalText(CharSequence t, int type) {
        CharSequence s;
        switch (type) {
            case CATEGORY_INPUT:
                boolean su = t.toString().startsWith("su ") || suMode;
                SpannableString si = Tuils.span(inputFormat, inputColor);
                
                s = TimeManager.instance.replace(si);
                s = TextUtils.replace(s,
                        new String[] {FORMAT_INPUT, FORMAT_PREFIX, FORMAT_NEWLINE, FORMAT_INPUT.toUpperCase(), FORMAT_PREFIX.toUpperCase(), FORMAT_NEWLINE.toUpperCase()},
                        new CharSequence[] {t, su ? suPrefix : prefix, Tuils.NEWLINE, t, su ? suPrefix : prefix, Tuils.NEWLINE});
                break;
            case CATEGORY_OUTPUT:
                SpannableString so = Tuils.span(outputFormat, outputColor);
                s = TextUtils.replace(so,
                        new String[] {FORMAT_OUTPUT, FORMAT_NEWLINE, FORMAT_OUTPUT.toUpperCase(), FORMAT_NEWLINE.toUpperCase()},
                        new CharSequence[] {t, Tuils.NEWLINE, t, Tuils.NEWLINE});
                break;
            case CATEGORY_NO_COLOR:
                s = t;
                break;
            default:
                return null;
        }
        return s;
    }

    public void simulateEnter() {
        onNewInput();
    }

    public String getInput() {
        return mInputView.getText().toString();
    }

    public void setInput(String input, Object obj) {
        SpannableString spannable = new SpannableString(input);
        if(obj != null) {
            spannable.setSpan(obj, 0, input.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mInputView.setText(spannable);
        focusInputEnd();
    }

    public void setInput(String input) {
        setInput(input, null);
    }

    public void setHint(String hint) {
        defaultHint = false;
        if(mInputView != null) mInputView.setHint(hint);
    }

    public void setDefaultHint() {
        defaultHint = true;
        if(mInputView != null) mInputView.setHint(Tuils.getHint(mainPack.currentDirectory.getAbsolutePath()));
    }

    public void focusInputEnd() {
        mInputView.setSelection(getInput().length());
    }

    public void scrollToEnd() {
        mTerminalList.postDelayed(mScrollRunnable, SCROLL_DELAY);
    }

    public void requestInputFocus() {
        mInputView.requestFocus();
    }

    public IBinder getInputWindowToken() {
        return mInputView.getWindowToken();
    }

    public View getInputView() {
        return mInputView;
    }

    public void clear() {
        mTerminalList.post(() -> {
            mAdapter.clear();
            cmdList.clear();
            clearCmdsCount = 0;
        });
    }

    public void onRoot() {
        ((Activity) mContext).runOnUiThread(() -> {
            suMode = true;
            mPrefix.setText(suPrefix.endsWith(Tuils.SPACE) ? suPrefix : suPrefix + Tuils.SPACE);
        });
    }

    public void onStandard() {
        ((Activity) mContext).runOnUiThread(() -> {
            suMode = false;
            mPrefix.setText(prefix.endsWith(Tuils.SPACE) ? prefix : prefix + Tuils.SPACE);
        });
    }
}
