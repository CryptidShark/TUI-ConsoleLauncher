package ohi.andre.consolelauncher.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ohi.andre.consolelauncher.MainManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class TuiOverlayService extends Service {

    private WindowManager windowManager;
    private View floatingButton;
    private View miniTerminal;
    private WindowManager.LayoutParams buttonParams;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;

        buttonParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        buttonParams.gravity = Gravity.TOP | Gravity.START;
        buttonParams.x = 100;
        buttonParams.y = 200;

        Button bubble = new Button(this);
        bubble.setText(">_");
        bubble.setTextColor(0xFF00FFD1);
        bubble.setBackgroundColor(0xDD0B0E14);
        bubble.setPadding(12, 6, 12, 6);
        bubble.setOnClickListener(v -> toggleMiniTerminal());

        bubble.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = buttonParams.x;
                        initialY = buttonParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        buttonParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        buttonParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingButton, buttonParams);
                        return true;
                }
                return false;
            }
        });

        floatingButton = bubble;
        try {
            windowManager.addView(floatingButton, buttonParams);
        } catch (Exception e) {
            Tuils.log(e);
        }
    }

    private void toggleMiniTerminal() {
        if (miniTerminal == null) {
            int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                    WindowManager.LayoutParams.TYPE_PHONE;

            WindowManager.LayoutParams terminalParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    type,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
            );
            terminalParams.gravity = Gravity.TOP;
            terminalParams.y = 150;

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(0xEE0B0E14);
            layout.setPadding(24, 24, 24, 24);

            TextView header = new TextView(this);
            header.setText("● [ T-UI QUICK ASSISTANT OVERLAY ]");
            header.setTextColor(0xFF00FFD1);
            layout.addView(header);

            EditText input = new EditText(this);
            input.setHint("Enter command (e.g. search, visual battery)");
            input.setTextColor(0xFFFFFFFF);
            input.setHintTextColor(0x88FFFFFF);
            layout.addView(input);

            Button execBtn = new Button(this);
            execBtn.setText("Execute");
            execBtn.setOnClickListener(v -> {
                String cmd = input.getText().toString().trim();
                if (!cmd.isEmpty()) {
                    Intent intent = new Intent(MainManager.ACTION_EXEC);
                    intent.putExtra(MainManager.CMD, cmd);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                    input.setText("");
                }
            });
            layout.addView(execBtn);

            Button closeBtn = new Button(this);
            closeBtn.setText("Close");
            closeBtn.setOnClickListener(v -> closeMiniTerminal());
            layout.addView(closeBtn);

            miniTerminal = layout;
            windowManager.addView(miniTerminal, terminalParams);
        } else {
            closeMiniTerminal();
        }
    }

    private void closeMiniTerminal() {
        if (miniTerminal != null) {
            try {
                windowManager.removeView(miniTerminal);
            } catch (Exception ignore) {}
            miniTerminal = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeMiniTerminal();
        if (floatingButton != null) {
            try {
                windowManager.removeView(floatingButton);
            } catch (Exception ignore) {}
            floatingButton = null;
        }
    }
}
