package ohi.andre.consolelauncher.search;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.CommandGroup;
import ohi.andre.consolelauncher.managers.AliasManager;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.managers.WorkspaceManager;
import ohi.andre.consolelauncher.tuils.Tuils;

public class UniversalSearchEngine {

    public static final int SCORE_EXACT = 1000;
    public static final int SCORE_PREFIX = 800;
    public static final int SCORE_TOKEN_PREFIX = 750;
    public static final int SCORE_ALIAS = 700;
    public static final int SCORE_CONTAINS = 600;
    public static final int SCORE_RECENT = 500;
    public static final int SCORE_FREQUENCY = 300;
    public static final int SCORE_FUZZY = 200;

    public static List<SearchResult> search(Context context, String query, AppsManager appsManager, AliasManager aliasManager) {
        return search(context, query, appsManager, aliasManager, null);
    }

    public static List<SearchResult> search(Context context, String query, AppsManager appsManager, AliasManager aliasManager, CommandGroup commandGroup) {
        List<SearchResult> results = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        if (query == null || query.trim().isEmpty() || context == null) {
            return results;
        }

        String q = query.trim().toLowerCase(Locale.ROOT);
        String unspacedQuery = Tuils.removeSpaces(q);

        // 1. Search CLI Commands
        if (commandGroup != null) {
            String[] cmdNames = commandGroup.getCommandNames();
            if (cmdNames != null) {
                for (String cmdName : cmdNames) {
                    if (cmdName == null) continue;
                    int score = calculateMatchScore(q, cmdName.toLowerCase(Locale.ROOT));
                    if (score > 0) {
                        String key = "cmd:" + cmdName.toLowerCase(Locale.ROOT);
                        if (!seenKeys.contains(key)) {
                            seenKeys.add(key);
                            results.add(new SearchResult(
                                    cmdName,
                                    "CLI Command",
                                    SearchResult.Category.COMMAND,
                                    score + 50,
                                    new SearchAction.ExecuteCommandAction(cmdName)
                            ));
                        }
                    }
                }
            }
        }

        // 2. Search Applications & App Shortcuts
        if (appsManager != null) {
            List<AppsManager.LaunchInfo> apps = appsManager.shownApps();
            if (apps != null) {
                for (AppsManager.LaunchInfo app : apps) {
                    if (app == null || app.publicLabel == null) continue;

                    String label = app.publicLabel.toLowerCase(Locale.ROOT);
                    String unspacedLabel = app.unspacedLowercaseLabel != null ? app.unspacedLowercaseLabel : Tuils.removeSpaces(label);
                    String pkg = app.componentName != null ? app.componentName.getPackageName().toLowerCase(Locale.ROOT) : "";

                    int baseScore = calculateMatchScore(unspacedQuery, unspacedLabel);
                    if (baseScore == 0) {
                        baseScore = calculateMatchScore(q, label);
                    }
                    if (baseScore == 0 && pkg.contains(unspacedQuery)) {
                        baseScore = SCORE_FREQUENCY;
                    }

                    if (baseScore > 0) {
                        int frequencyBoost = Math.min(app.launchedTimes * 15, 200);
                        int finalScore = baseScore + frequencyBoost;

                        String key = "app:" + pkg;
                        if (!seenKeys.contains(key)) {
                            seenKeys.add(key);
                            results.add(new SearchResult(
                                    app.publicLabel,
                                    pkg,
                                    SearchResult.Category.APPLICATION,
                                    finalScore,
                                    new SearchAction.OpenAppAction(app)
                            ));
                        }
                    }

                    // Search App Shortcuts (Android 7.1+ / N_MR1+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && app.shortcuts != null) {
                        for (ShortcutInfo shortcut : app.shortcuts) {
                            if (shortcut == null) continue;
                            CharSequence sLabel = shortcut.getShortLabel();
                            if (sLabel == null) sLabel = shortcut.getLongLabel();
                            if (sLabel == null) continue;

                            String scText = sLabel.toString().toLowerCase(Locale.ROOT);
                            int scScore = calculateMatchScore(q, scText);
                            if (scScore > 0) {
                                String key = "shortcut:" + pkg + ":" + shortcut.getId();
                                if (!seenKeys.contains(key)) {
                                    seenKeys.add(key);
                                    results.add(new SearchResult(
                                            sLabel.toString(),
                                            "Shortcut (" + app.publicLabel + ")",
                                            SearchResult.Category.ACTION,
                                            scScore - 50,
                                            new SearchAction.LaunchShortcutAction(pkg, shortcut.getId())
                                    ));
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Search Aliases
        if (aliasManager != null) {
            List<AliasManager.Alias> aliases = aliasManager.getAliases(true);
            if (aliases != null) {
                for (AliasManager.Alias alias : aliases) {
                    if (alias == null || alias.name == null) continue;

                    String aliasName = alias.name.toLowerCase(Locale.ROOT);
                    int score = calculateMatchScore(q, aliasName);
                    if (score > 0) {
                        String key = "alias:" + aliasName;
                        if (!seenKeys.contains(key)) {
                            seenKeys.add(key);
                            results.add(new SearchResult(
                                    alias.name,
                                    "Alias -> " + alias.value,
                                    SearchResult.Category.ALIAS,
                                    score + 30,
                                    new SearchAction.ExecuteAliasAction(alias.name, alias.value)
                            ));
                        }
                    }
                }
            }
        }

        // Workspace presets
        for (String workspace : WorkspaceManager.getAvailableWorkspaces()) {
            int score = calculateMatchScore(q, workspace.toLowerCase(Locale.ROOT));
            if (score > 0) {
                String key = "workspace:" + workspace.toLowerCase(Locale.ROOT);
                if (seenKeys.add(key)) {
                    results.add(new SearchResult(workspace, "Switch launcher workspace",
                            SearchResult.Category.ACTION, score,
                            new SearchAction.SwitchWorkspaceAction(workspace)));
                }
            }
        }

        // 4. System & Settings shortcuts
        if ("wifi".startsWith(q) || "network".startsWith(q) || "internet".startsWith(q)) {
            String key = "settings:wifi";
            if (!seenKeys.contains(key)) {
                seenKeys.add(key);
                results.add(new SearchResult(
                        "Wi-Fi Settings",
                        "Open Android Wi-Fi preferences",
                        SearchResult.Category.SETTINGS,
                        SCORE_PREFIX,
                        new SearchAction.OpenSettingsAction(Settings.ACTION_WIFI_SETTINGS)
                ));
            }
        }

        if ("bluetooth".startsWith(q) || "bt".startsWith(q)) {
            String key = "settings:bluetooth";
            if (!seenKeys.contains(key)) {
                seenKeys.add(key);
                results.add(new SearchResult(
                        "Bluetooth Settings",
                        "Open Bluetooth preferences",
                        SearchResult.Category.SETTINGS,
                        SCORE_PREFIX,
                        new SearchAction.OpenSettingsAction(Settings.ACTION_BLUETOOTH_SETTINGS)
                ));
            }
        }

        if ("battery".startsWith(q) || "power".startsWith(q)) {
            String key = "settings:battery";
            if (!seenKeys.contains(key)) {
                seenKeys.add(key);
                results.add(new SearchResult(
                        "Battery Settings",
                        "Open Battery Saver and Stats",
                        SearchResult.Category.SETTINGS,
                        SCORE_PREFIX,
                        new SearchAction.OpenSettingsAction(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                ));
            }
        }

        if ("lock".equals(q)) {
            String key = "action:lock";
            if (!seenKeys.contains(key)) {
                seenKeys.add(key);
                results.add(new SearchResult(
                        "Lock Screen",
                        "Lock device screen instantly",
                        SearchResult.Category.ACTION,
                        SCORE_EXACT,
                        new SearchAction.LockDeviceAction()
                ));
            }
        }

        Collections.sort(results);
        return results;
    }

    public static int calculateMatchScore(String query, String target) {
        if (query == null || target == null || query.isEmpty() || target.isEmpty()) return 0;

        String q = query.toLowerCase(Locale.ROOT);
        String t = target.toLowerCase(Locale.ROOT);

        if (t.equals(q)) {
            return SCORE_EXACT;
        }

        if (t.startsWith(q)) {
            return SCORE_PREFIX;
        }

        // Multi-token prefix matching
        String[] qTokens = q.split("\\s+");
        String[] tTokens = t.split("\\s+");

        boolean allTokensMatchPrefix = true;
        for (String qTok : qTokens) {
            boolean tokFound = false;
            for (String tTok : tTokens) {
                if (tTok.startsWith(qTok)) {
                    tokFound = true;
                    break;
                }
            }
            if (!tokFound) {
                allTokensMatchPrefix = false;
                break;
            }
        }

        if (allTokensMatchPrefix && qTokens.length > 0) {
            return SCORE_TOKEN_PREFIX;
        }

        if (t.contains(q)) {
            return SCORE_CONTAINS;
        }

        // Fuzzy Levenshtein Distance
        int fuzzyDistance = calculateLevenshteinDistance(q, t);
        if (fuzzyDistance <= 2 && q.length() >= 3) {
            return SCORE_FUZZY - (fuzzyDistance * 40);
        }

        return 0;
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
