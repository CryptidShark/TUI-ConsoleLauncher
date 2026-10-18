package ohi.andre.consolelauncher.managers;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Build;
import android.app.role.RoleManager;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.tuils.Tuils;

public class TuiWidgetManager {

    private static final int HOST_ID = 1042;
    public static final int REQUEST_PICK_APPWIDGET = 10430;
    public static final int REQUEST_CREATE_APPWIDGET = 10440;
    private static final String PREFS_NAME = "persistent_widgets";
    private static final String WIDGET_IDS_KEY = "active_ids";
    private static final String WIDGET_IDS_ORDER_KEY = "active_ids_ordered";

    private final AppWidgetHost mAppWidgetHost;
    private final AppWidgetManager mAppWidgetManager;
    private final Context mContext;
    private int mPendingWidgetId = -1;
    private final List<Integer> mActiveWidgetIds = new ArrayList<>();

    public TuiWidgetManager(Context context) {
        this.mContext = context;
        this.mAppWidgetManager = AppWidgetManager.getInstance(context);
        this.mAppWidgetHost = new AppWidgetHost(context, HOST_ID);
    }

    public void startListening() {
        mAppWidgetHost.startListening();
    }

    public void stopListening() {
        mAppWidgetHost.stopListening();
    }

    public void pickWidget(LauncherActivity activity) {
        try {
            mAppWidgetHost.startListening();
        } catch (Exception ignore) {}

        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        mPendingWidgetId = appWidgetId;
        try {
            Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
            pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            pickIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            activity.startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
        } catch (Exception e) {
            deleteWidgetId(appWidgetId);
            mPendingWidgetId = -1;
            Tuils.sendOutput(activity, "Error opening widget picker: " + e.getMessage());
        }
    }

    public void configureWidget(LauncherActivity activity, Intent data) {
        Bundle extras = data != null ? data.getExtras() : null;
        int appWidgetId = extras != null ? extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
        if (appWidgetId == -1) appWidgetId = mPendingWidgetId;
        if (appWidgetId == -1) {
            Tuils.sendOutput(activity, "The widget picker returned no widget ID. Run `widget add` again.");
            return;
        }

        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo == null) {
            cancelPendingWidget(data);
            Tuils.sendOutput(activity, "Selected widget provider is no longer available.");
            return;
        }

        // ACTION_APPWIDGET_PICK returns a widget ID already associated with the
        // selected provider. Rebinding it can fail on Android and strand widgets
        // without a configuration activity, so proceed directly to setup.
        continueWidgetSetup(activity, appWidgetId, appWidgetInfo);
    }

    private void continueWidgetSetup(LauncherActivity activity, int appWidgetId, AppWidgetProviderInfo info) {
        if (info.configure == null) {
            createWidget(activity, appWidgetId);
            return;
        }
        mPendingWidgetId = appWidgetId;
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        intent.setComponent(info.configure);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        try {
            activity.startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } catch (Exception e) {
            deleteWidgetId(appWidgetId);
            mPendingWidgetId = -1;
            Tuils.sendOutput(activity, "Error configuring widget: " + e.getMessage());
        }
    }

    public void cancelPendingWidget(Intent data) {
        int id = data != null ? data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mPendingWidgetId) : mPendingWidgetId;
        if (id != -1) deleteWidgetId(id);
        mPendingWidgetId = -1;
    }

    public void deleteWidgetId(int appWidgetId) {
        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
        mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().remove("height_" + appWidgetId).apply();
        if (mPendingWidgetId == appWidgetId) mPendingWidgetId = -1;
    }

    public void createWidgetFromIntent(LauncherActivity activity, Intent data) {
        int appWidgetId = -1;
        if (data != null && data.getExtras() != null) {
            appWidgetId = data.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        }
        
        if (appWidgetId == -1) {
            appWidgetId = mPendingWidgetId;
        }

        if (appWidgetId != -1) {
            createWidget(activity, appWidgetId);
        }
    }

    public void updateWidgetOptions(AppWidgetHostView hostView, int appWidgetId, AppWidgetProviderInfo appWidgetInfo) {
        if (hostView == null || appWidgetInfo == null) return;
        try {
            int widthDp = getAvailableWidgetWidthDp();
            int minHeightDp = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getInt("height_" + appWidgetId, appWidgetInfo.minHeight > 0 ? Math.max(40, appWidgetInfo.minHeight) : 100);
            Bundle currentOptions = mAppWidgetManager.getAppWidgetOptions(appWidgetId);
            if (currentOptions != null
                    && currentOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, -1) == widthDp
                    && currentOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, -1) == widthDp
                    && currentOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, -1) == minHeightDp
                    && currentOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, -1) == minHeightDp) return;

            Bundle options = new Bundle();
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, minHeightDp);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHeightDp);

            hostView.updateAppWidgetSize(options, widthDp, minHeightDp, widthDp, minHeightDp);
            mAppWidgetManager.updateAppWidgetOptions(appWidgetId, options);
        } catch (Exception e) {
            Tuils.log("Error updating widget options", e);
        }
    }

    /** Refreshes provider size options after rotation or a launcher window resize. */
    public void updateActiveWidgetSizes() {
        if (!(mContext instanceof LauncherActivity)) return;
        ViewGroup container = ((LauncherActivity) mContext).findViewById(ohi.andre.consolelauncher.R.id.persistent_container);
        if (container == null || container.getWidth() <= 0) return;
        for (int i = 0; i < container.getChildCount(); i++) {
            updateWidgetOptionsInTree(container.getChildAt(i));
        }
    }

    private void updateWidgetOptionsInTree(View view) {
        if (view instanceof AppWidgetHostView) {
            AppWidgetHostView hostView = (AppWidgetHostView) view;
            int id = hostView.getAppWidgetId();
            AppWidgetProviderInfo info = mAppWidgetManager.getAppWidgetInfo(id);
            if (info != null) updateWidgetOptions(hostView, id, info);
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                updateWidgetOptionsInTree(group.getChildAt(i));
            }
        }
    }

    public void createWidget(LauncherActivity activity, int appWidgetId) {
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo == null) {
            Tuils.sendOutput(activity, "Widget provider not found or uninstalled for ID " + appWidgetId + ". Removing stale widget.");
            deleteWidgetId(appWidgetId);
            mActiveWidgetIds.remove(Integer.valueOf(appWidgetId));
            saveState(null);
            return;
        }

        try {
            Tuils.sendOutput(activity, "Inflating widget: " + (appWidgetInfo.label != null ? appWidgetInfo.label : "External"));
            AppWidgetHostView hostView = mAppWidgetHost.createView(activity, appWidgetId, appWidgetInfo);
            hostView.setAppWidget(appWidgetId, appWidgetInfo);
            
            updateWidgetOptions(hostView, appWidgetId, appWidgetInfo);

            hostView.postDelayed(() -> {
                try {
                    updateWidgetOptions(hostView, appWidgetId, appWidgetInfo);
                    hostView.updateAppWidget(null);
                } catch (Exception ignore) {}
            }, 500);
            
            hostView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            activity.onPersistentOutput(hostView);
            if (!mActiveWidgetIds.contains(appWidgetId)) {
                mActiveWidgetIds.add(appWidgetId);
                saveState(null);
            }
        } catch (Exception e) {
            Tuils.sendOutput(activity, "Error rendering widget ID " + appWidgetId + ": " + e.getMessage());
            // Create fallback error view instead of crashing
            TextView errorView = new TextView(activity);
            errorView.setText("[ WIDGET_ID: " + appWidgetId + " ] ERROR_RENDERING: " + e.getMessage());
            errorView.setPadding(16, 16, 16, 16);
            activity.onPersistentOutput(errorView);
            if (!mActiveWidgetIds.contains(appWidgetId)) mActiveWidgetIds.add(appWidgetId);
            saveState(null);
        }
        
        mPendingWidgetId = -1;
    }

    public boolean moveWidget(LauncherActivity activity, int widgetId, boolean up) {
        int index = mActiveWidgetIds.indexOf(widgetId);
        if (index == -1) return false;

        int targetIndex = up ? index - 1 : index + 1;
        if (targetIndex < 0 || targetIndex >= mActiveWidgetIds.size()) return false;

        Collections.swap(mActiveWidgetIds, index, targetIndex);
        saveState(null);
        if (activity != null) {
            activity.refreshPersistentContainer();
        }
        return true;
    }

    public boolean setWidgetHeight(LauncherActivity activity, int widgetId, int heightDp) {
        if (!mActiveWidgetIds.contains(widgetId)) return false;
        try {
            if (heightDp < 40 || heightDp > 2000) return false;
            AppWidgetProviderInfo info = mAppWidgetManager.getAppWidgetInfo(widgetId);
            if (info == null) return false;
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putInt("height_" + widgetId, heightDp).apply();
            Bundle options = new Bundle();
            int widthDp = getAvailableWidgetWidthDp();
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp);
            mAppWidgetManager.updateAppWidgetOptions(widgetId, options);
            if (activity != null) {
                activity.refreshPersistentContainer();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns the actual width available to a widget inside the launcher's inset card. */
    private int getAvailableWidgetWidthDp() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        float density = metrics.density > 0 ? metrics.density : 1f;
        int availableWidthPx = metrics.widthPixels;
        if (mContext instanceof LauncherActivity) {
            ViewGroup container = ((LauncherActivity) mContext).findViewById(ohi.andre.consolelauncher.R.id.persistent_container);
            if (container != null && container.getWidth() > 0) {
                availableWidthPx = container.getWidth() - container.getPaddingLeft() - container.getPaddingRight();
            }
        }
        // The host view sits inside a bordered card with small horizontal insets.
        int horizontalInsetsPx = Math.round(16f * density);
        return Math.max(120, Math.round(Math.max(0, availableWidthPx - horizontalInsetsPx) / density));
    }

    public List<Integer> getActiveWidgetIds() {
        return new ArrayList<>(mActiveWidgetIds);
    }

    public boolean removeWidget(int appWidgetId) {
        if (!mActiveWidgetIds.contains(appWidgetId)) return false;
        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
        mActiveWidgetIds.remove(Integer.valueOf(appWidgetId));
        mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().remove("height_" + appWidgetId).apply();
        saveState(null);
        return true;
    }

    public void clearAllWidgets() {
        for (Integer id : mActiveWidgetIds) {
            mAppWidgetHost.deleteAppWidgetId(id);
            mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().remove("height_" + id).apply();
        }
        mActiveWidgetIds.clear();
        saveState(null);
    }

    public boolean isDefaultLauncher() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RoleManager roleManager = mContext.getSystemService(RoleManager.class);
                return roleManager != null && roleManager.isRoleHeld(RoleManager.ROLE_HOME);
            }
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            final ResolveInfo res = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return res != null && mContext.getPackageName().equals(res.activityInfo.packageName);
        } catch (Exception e) {
            return false;
        }
    }

    public void saveState(Bundle outState) {
        if (outState != null) {
            outState.putInt("mPendingWidgetId", mPendingWidgetId);
            outState.putIntegerArrayList("mActiveWidgetIds", (ArrayList<Integer>) mActiveWidgetIds);
        }
        
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = new HashSet<>();
        StringBuilder orderedIds = new StringBuilder();
        for (Integer id : mActiveWidgetIds) {
            set.add(id.toString());
            if (orderedIds.length() > 0) orderedIds.append(',');
            orderedIds.append(id);
        }
        prefs.edit().putStringSet(WIDGET_IDS_KEY, set).putString(WIDGET_IDS_ORDER_KEY, orderedIds.toString()).apply();
    }

    public void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPendingWidgetId = savedInstanceState.getInt("mPendingWidgetId", -1);
            ArrayList<Integer> ids = savedInstanceState.getIntegerArrayList("mActiveWidgetIds");
            if (ids != null) {
                mActiveWidgetIds.clear();
                mActiveWidgetIds.addAll(ids);
            }
        } else {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String orderedIds = prefs.getString(WIDGET_IDS_ORDER_KEY, null);
            if (orderedIds != null) {
                mActiveWidgetIds.clear();
                for (String id : orderedIds.split(",")) {
                    try { mActiveWidgetIds.add(Integer.parseInt(id)); } catch (NumberFormatException ignored) {}
                }
            } else {
                Set<String> set = prefs.getStringSet(WIDGET_IDS_KEY, null);
                if (set == null) return;
                mActiveWidgetIds.clear();
                for (String s : set) {
                    try { mActiveWidgetIds.add(Integer.parseInt(s)); } catch (NumberFormatException ignored) {}
                }
                saveState(null);
            }
        }

        if (!mActiveWidgetIds.isEmpty()) {
            final LauncherActivity activity = (LauncherActivity) mContext;
            activity.runOnUiThread(() -> {
                for (final Integer id : mActiveWidgetIds) {
                    createWidget(activity, id);
                }
            });
        }
    }
}
