package ohi.andre.consolelauncher.search;

import android.content.Context;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ohi.andre.consolelauncher.commands.CommandTuils;
import ohi.andre.consolelauncher.managers.AliasManager;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class UniversalSearchEngine {

    public static final int SCORE_EXACT = 1000;
    public static final int SCORE_PREFIX = 800;
    public static final int SCORE_ALIAS = 700;
    public static final int SCORE_RECENT = 500;
    public static final int SCORE_FREQUENCY = 300;
    public static final int SCORE_FUZZY = 100;

    public static List<SearchResult> search(Context context, String query, AppsManager appsManager, AliasManager aliasManager) {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty() || context == null) {
            return results;
        }

        String q = query.trim().toLowerCase(Locale.ROOT);
        String unspacedQuery = Tuils.removeSpaces(q);

        // 1. Search Applications
        if (appsManager != null) {
            List<AppsManager.LaunchInfo> apps = appsManager.shownApps();
            if (apps != null) {
                for (AppsManager.LaunchInfo app : apps) {
                    if (app == null || app.publicLabel == null) continue;

                    String label = app.publicLabel.toLowerCase(Locale.ROOT);
                    String unspacedLabel = app.unspacedLowercaseLabel != null ? app.unspacedLowercaseLabel : Tuils.removeSpaces(label);
                    String pkg = app.componentName != null ? app.componentName.getPackageName().toLowerCase(Locale.ROOT) : "";

                    int score = 0;
                    if (unspacedLabel.equals(unspacedQuery)) {
                        score = SCORE_EXACT;
                    } else if (unspacedLabel.startsWith(unspacedQuery)) {
                        score = SCORE_PREFIX;
                    } else if (pkg.contains(unspacedQuery)) {
                        score = SCORE_FREQUENCY;
                    } else {
                        int fuzzyDistance = calculateLevenshteinDistance(unspacedQuery, unspacedLabel);
                        if (fuzzyDistance <= 2 && unspacedQuery.length() >= 3) {
                            score = SCORE_FUZZY;
                        }
                    }

                    if (score > 0) {
                        results.add(new SearchResult(
                                app.publicLabel,
                                pkg,
                                SearchResult.Category.APPLICATION,
                                score,
                                new SearchAction.OpenAppAction(app)
                        ));
                    }
                }
            }
        }

        // 2. Search Aliases
        if (aliasManager != null) {
            List<AliasManager.Alias> aliases = aliasManager.getAliases(true);
            if (aliases != null) {
                for (AliasManager.Alias alias : aliases) {
                    if (alias == null || alias.name == null) continue;

                    String aliasName = alias.name.toLowerCase(Locale.ROOT);
                    if (aliasName.equals(q)) {
                        results.add(new SearchResult(
                                alias.name,
                                "Alias -> " + alias.value,
                                SearchResult.Category.ALIAS,
                                SCORE_ALIAS,
                                new SearchAction.ExecuteAliasAction(alias.name, alias.value)
                        ));
                    } else if (aliasName.startsWith(q)) {
                        results.add(new SearchResult(
                                alias.name,
                                "Alias -> " + alias.value,
                                SearchResult.Category.ALIAS,
                                SCORE_PREFIX,
                                new SearchAction.ExecuteAliasAction(alias.name, alias.value)
                        ));
                    }
                }
            }
        }

        // 3. System & Settings shortcuts
        if ("wifi".startsWith(q) || "network".startsWith(q)) {
            results.add(new SearchResult(
                    "Wi-Fi Settings",
                    "Open Android Wi-Fi preferences",
                    SearchResult.Category.SETTINGS,
                    SCORE_PREFIX,
                    new SearchAction.OpenSettingsAction(Settings.ACTION_WIFI_SETTINGS)
            ));
        }

        if ("bluetooth".startsWith(q) || "bt".startsWith(q)) {
            results.add(new SearchResult(
                    "Bluetooth Settings",
                    "Open Bluetooth preferences",
                    SearchResult.Category.SETTINGS,
                    SCORE_PREFIX,
                    new SearchAction.OpenSettingsAction(Settings.ACTION_BLUETOOTH_SETTINGS)
            ));
        }

        if ("battery".startsWith(q) || "power".startsWith(q)) {
            results.add(new SearchResult(
                    "Battery Settings",
                    "Open Battery Saver and Stats",
                    SearchResult.Category.SETTINGS,
                    SCORE_PREFIX,
                    new SearchAction.OpenSettingsAction(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            ));
        }

        if ("lock".equals(q)) {
            results.add(new SearchResult(
                    "Lock Screen",
                    "Lock device screen instantly",
                    SearchResult.Category.ACTION,
                    SCORE_EXACT,
                    new SearchAction.LockDeviceAction()
            ));
        }

        Collections.sort(results);
        return results;
    }

    public static int calculateLevenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;

        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[len1][len2];
    }
}
