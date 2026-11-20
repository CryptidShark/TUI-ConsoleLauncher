package ohi.andre.consolelauncher.widgets.hud;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.net.Inet4Address;
import java.util.Locale;

import ohi.andre.consolelauncher.R;

/**
 * Low-cost system telemetry card. Polling happens only while attached to the
 * visible launcher; battery and network state also refresh from system events.
 */
public class SysTelemetryWidget extends HudCardView {
    private static final long REFRESH_INTERVAL_MS = 5000L;
    private static final long STORAGE_REFRESH_INTERVAL_MS = 30000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final HudGaugeView batteryGauge;
    private final HudGaugeView memoryGauge;
    private final HudGaugeView storageGauge;
    private final TextView networkView;
    private final ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    private final ActivityManager activityManager;
    private final ConnectivityManager connectivityManager;
    private final Runnable refreshRunnable = new Runnable() {
        @Override public void run() {
            if (!isAttachedToWindow()) return;
            refreshMemory();
            refreshTraffic();
            if (System.currentTimeMillis() - lastStorageRefresh >= STORAGE_REFRESH_INTERVAL_MS) {
                refreshStorage();
            }
            handler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };

    private long lastStorageRefresh;
    private long previousRx = TrafficStats.getTotalRxBytes();
    private long previousTx = TrafficStats.getTotalTxBytes();
    private long previousTrafficTime = System.currentTimeMillis();
    private boolean receiverRegistered;
    private boolean networkCallbackRegistered;

    private final BroadcastReceiver stateReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (level >= 0 && scale > 0) updateBattery(Math.round(level * 100f / scale));
            } else {
                refreshNetwork();
            }
        }
    };

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override public void onAvailable(Network network) {
            handler.post(SysTelemetryWidget.this::refreshNetwork);
        }
        @Override public void onLost(Network network) {
            handler.post(SysTelemetryWidget.this::refreshNetwork);
        }
        @Override public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            handler.post(SysTelemetryWidget.this::refreshNetwork);
        }
    };

    public SysTelemetryWidget(Context context) {
        super(context);
        setTitle("SYS STATUS");
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        LinearLayout gauges = new LinearLayout(context);
        gauges.setOrientation(LinearLayout.HORIZONTAL);
        gauges.setGravity(Gravity.CENTER);
        batteryGauge = addGauge(gauges, "BAT", ContextCompat.getColor(context, R.color.hud_green));
        memoryGauge = addGauge(gauges, "RAM", ContextCompat.getColor(context, R.color.hud_pink));
        storageGauge = addGauge(gauges, "DISK", ContextCompat.getColor(context, R.color.hud_purple));
        addView(gauges, new LayoutParams(LayoutParams.MATCH_PARENT, dp(82)));

        networkView = new TextView(context);
        networkView.setTextColor(ContextCompat.getColor(context, R.color.hud_cyan));
        networkView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        networkView.setTypeface(android.graphics.Typeface.MONOSPACE);
        networkView.setMaxLines(2);
        networkView.setEllipsize(TextUtils.TruncateAt.END);
        networkView.setPadding(0, dp(5), 0, 0);
        addView(networkView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        refreshBatteryFromSystem();
        refreshMemory();
        refreshStorage();
        refreshNetwork();
    }

    private HudGaugeView addGauge(LinearLayout row, String label, int accent) {
        HudGaugeView gauge = new HudGaugeView(getContext());
        gauge.setValue(label, 0, accent);
        gauge.setColors(ContextCompat.getColor(getContext(), R.color.hud_border),
                ContextCompat.getColor(getContext(), R.color.hud_text_primary),
                ContextCompat.getColor(getContext(), R.color.hud_text_secondary));
        row.addView(gauge, new LinearLayout.LayoutParams(0, dp(78), 1f));
        return gauge;
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            ContextCompat.registerReceiver(getContext(), stateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            receiverRegistered = true;
        }
        if (connectivityManager != null && !networkCallbackRegistered) {
            try {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
                networkCallbackRegistered = true;
            } catch (RuntimeException ignored) { }
        }
        refreshBatteryFromSystem();
        refreshNetwork();
        handler.removeCallbacks(refreshRunnable);
        handler.post(refreshRunnable);
    }

    @Override protected void onDetachedFromWindow() {
        handler.removeCallbacksAndMessages(null);
        if (receiverRegistered) {
            try { getContext().unregisterReceiver(stateReceiver); } catch (IllegalArgumentException ignored) { }
            receiverRegistered = false;
        }
        if (connectivityManager != null && networkCallbackRegistered) {
            try { connectivityManager.unregisterNetworkCallback(networkCallback); } catch (RuntimeException ignored) { }
            networkCallbackRegistered = false;
        }
        super.onDetachedFromWindow();
    }

    private void refreshBatteryFromSystem() {
        Intent status = getContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (status == null) return;
        int level = status.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = status.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level >= 0 && scale > 0) updateBattery(Math.round(level * 100f / scale));
    }

    private void updateBattery(int percentage) {
        int color = percentage <= 20
                ? ContextCompat.getColor(getContext(), R.color.hud_pink)
                : ContextCompat.getColor(getContext(), R.color.hud_green);
        batteryGauge.setValue("BAT", percentage, color);
    }

    private void refreshMemory() {
        if (activityManager == null) return;
        activityManager.getMemoryInfo(memoryInfo);
        long total = memoryInfo.totalMem;
        long used = Math.max(0, total - memoryInfo.availMem);
        int percent = total > 0 ? (int) Math.min(100, used * 100L / total) : 0;
        memoryGauge.setValue("RAM", percent, ContextCompat.getColor(getContext(), R.color.hud_pink));
    }

    private void refreshStorage() {
        try {
            StatFs stats = new StatFs(android.os.Environment.getDataDirectory().getPath());
            long total = stats.getTotalBytes();
            long available = stats.getAvailableBytes();
            int percent = total > 0 ? (int) Math.max(0, Math.min(100, (total - available) * 100L / total)) : 0;
            storageGauge.setValue("DISK", percent, ContextCompat.getColor(getContext(), R.color.hud_purple));
            lastStorageRefresh = System.currentTimeMillis();
        } catch (RuntimeException ignored) { }
    }

    private void refreshNetwork() {
        if (networkView == null) return;
        Network active = connectivityManager == null ? null : connectivityManager.getActiveNetwork();
        NetworkCapabilities caps = active == null ? null : connectivityManager.getNetworkCapabilities(active);
        boolean connected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        String transport;
        if (caps == null) transport = "--";
        else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) transport = getWifiName();
        else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) transport = "MOBILE";
        else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) transport = "ETHERNET";
        else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) transport = "VPN";
        else transport = "NETWORK";
        String ip = getLocalIpv4();
        String state = connected ? "ONLINE" : "OFFLINE";
        networkView.setText("NET  " + state + " · " + transport + "\nIP   " + ip + "  ·  " + trafficText);
    }

    private String trafficText = "RX --  TX --";

    private void refreshTraffic() {
        long now = System.currentTimeMillis();
        long rx = TrafficStats.getTotalRxBytes();
        long tx = TrafficStats.getTotalTxBytes();
        long elapsed = now - previousTrafficTime;
        if (rx >= 0 && tx >= 0 && previousRx >= 0 && previousTx >= 0 && elapsed > 0) {
            double rxRate = Math.max(0, rx - previousRx) * 1000d / elapsed / 1024d;
            double txRate = Math.max(0, tx - previousTx) * 1000d / elapsed / 1024d;
            trafficText = String.format(Locale.ROOT, "RX %.0f  TX %.0f KB/s", rxRate, txRate);
        } else {
            trafficText = "RX n/a  TX n/a";
        }
        previousRx = rx;
        previousTx = tx;
        previousTrafficTime = now;
        refreshNetwork();
    }

    private String getWifiName() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return "Wi-Fi";
        try {
            WifiManager wifi = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi == null ? null : wifi.getConnectionInfo();
            String ssid = info == null ? null : info.getSSID();
            if (ssid == null || ssid.isEmpty() || "<unknown ssid>".equalsIgnoreCase(ssid)) return "Wi-Fi";
            return ssid.replace("\"", "");
        } catch (RuntimeException ignored) {
            return "Wi-Fi";
        }
    }

    private String getLocalIpv4() {
        try {
            Network active = connectivityManager == null ? null : connectivityManager.getActiveNetwork();
            LinkProperties properties = active == null ? null : connectivityManager.getLinkProperties(active);
            if (properties != null) {
                for (LinkAddress linkAddress : properties.getLinkAddresses()) {
                    java.net.InetAddress address = linkAddress.getAddress();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) { }
        return "--";
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
