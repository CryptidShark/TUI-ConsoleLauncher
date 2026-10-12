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
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
    public static final int REQUEST_BIND_APPWIDGET = 10450;
    private static final String PREFS_NAME = "persistent_widgets";
    private static final String WIDGET_IDS_KEY = "active_ids";

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
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pickIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        
        try {
            activity.startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
        } catch (Exception e) {
            Tuils.sendOutput(activity, "Error opening widget picker: " + e.getMessage());
        }
    }

    public void configureWidget(LauncherActivity activity, Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras != null ? extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) : -1;
        if (appWidgetId == -1) return;

        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo == null) return;

        if (appWidgetInfo.configure != null) {
            mPendingWidgetId = appWidgetId;
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            
            activity.startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            boolean bound = false;
            try {
                bound = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, appWidgetInfo.provider);
            } catch (SecurityException e) {
                Tuils.log("SecurityException binding widget", e);
            }

            if (bound) {
                createWidget(activity, appWidgetId);
            } else {
                Tuils.sendOutput(activity, "System permission required for widget binding...");
                mPendingWidgetId = appWidgetId;
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, appWidgetInfo.provider);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, Process.myUserHandle());
                intent.putExtra("appWidgetHostId", HOST_ID);

                activity.startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
            }
        }
    }

    public void deleteWidgetId(int appWidgetId) {
        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
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
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            float density = metrics.density > 0 ? metrics.density : 1.0f;

            int widthDp = (int) (metrics.widthPixels / density);
            int minHeightDp = appWidgetInfo.minHeight > 0 ? (int) (appWidgetInfo.minHeight / density) : 100;
            if (minHeightDp < 40) minHeightDp = 100;

            Bundle options = new Bundle();
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, minHeightDp);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHeightDp * 3);

            hostView.updateAppWidgetSize(options, widthDp, minHeightDp, widthDp, minHeightDp * 3);
            mAppWidgetManager.updateAppWidgetOptions(appWidgetId, options);
        } catch (Exception e) {
            Tuils.log("Error updating widget options", e);
        }
    }

    public void createWidget(LauncherActivity activity, int appWidgetId) {
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo == null) {
            Tuils.sendOutput(activity, "Widget provider not found or uninstalled for ID " + appWidgetId + ". Removing stale widget.");
            mAppWidgetHost.deleteAppWidgetId(appWidgetId);
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
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            int heightPx = Tuils.dpToPx(mContext, heightDp);
            
            AppWidgetProviderInfo info = mAppWidgetManager.getAppWidgetInfo(widgetId);
            if (info != null) {
                Bundle options = new Bundle();
                options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp);
                options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp);
                mAppWidgetManager.updateAppWidgetOptions(widgetId, options);
            }
            if (activity != null) {
                activity.refreshPersistentContainer();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Integer> getActiveWidgetIds() {
        return mActiveWidgetIds;
    }

    public void removeWidget(int appWidgetId) {
        mAppWidgetHost.deleteAppWidgetId(appWidgetId);
        mActiveWidgetIds.remove(Integer.valueOf(appWidgetId));
        saveState(null);
    }

    public void clearAllWidgets() {
        for (Integer id : mActiveWidgetIds) {
            mAppWidgetHost.deleteAppWidgetId(id);
        }
        mActiveWidgetIds.clear();
        saveState(null);
    }

    public boolean isDefaultLauncher() {
        try {
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
        for (Integer id : mActiveWidgetIds) set.add(id.toString());
        prefs.edit().putStringSet(WIDGET_IDS_KEY, set).apply();
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
            Set<String> set = prefs.getStringSet(WIDGET_IDS_KEY, null);
            if (set != null) {
                mActiveWidgetIds.clear();
                for (String s : set) mActiveWidgetIds.add(Integer.parseInt(s));
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
