package ohi.andre.consolelauncher.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ohi.andre.consolelauncher.MainManager;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.tuils.PrivateIOReceiver;
import ohi.andre.consolelauncher.tuils.Tuils;

public interface SearchAction {

    void execute(Context context);

    class OpenAppAction implements SearchAction {
        private final AppsManager.LaunchInfo launchInfo;

        public OpenAppAction(AppsManager.LaunchInfo launchInfo) {
            this.launchInfo = launchInfo;
        }

        @Override
        public void execute(Context context) {
            if (launchInfo != null && context != null) {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(launchInfo.componentName.getPackageName());
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }

        public AppsManager.LaunchInfo getLaunchInfo() {
            return launchInfo;
        }
    }

    class OpenSettingsAction implements SearchAction {
        private final String settingsAction;

        public OpenSettingsAction(String settingsAction) {
            this.settingsAction = settingsAction != null ? settingsAction : Settings.ACTION_SETTINGS;
        }

        @Override
        public void execute(Context context) {
            if (context != null) {
                try {
                    Intent intent = new Intent(settingsAction);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }
    }

    class LockDeviceAction implements SearchAction {
        @Override
        public void execute(Context context) {
            if (context != null) {
                Tuils.sendInput(context, "lock");
            }
        }
    }

    class SwitchWorkspaceAction implements SearchAction {
        private final String workspaceName;

        public SwitchWorkspaceAction(String workspaceName) {
            this.workspaceName = workspaceName;
        }

        @Override
        public void execute(Context context) {
            if (context != null && workspaceName != null) {
                Tuils.sendOutput(context, "Workspace switched to: " + workspaceName);
            }
        }

        public String getWorkspaceName() {
            return workspaceName;
        }
    }

    class ExecuteAliasAction implements SearchAction {
        private final String aliasName;
        private final String aliasValue;

        public ExecuteAliasAction(String aliasName, String aliasValue) {
            this.aliasName = aliasName;
            this.aliasValue = aliasValue;
        }

        @Override
        public void execute(Context context) {
            if (context != null && aliasValue != null) {
                Intent intent = new Intent(MainManager.ACTION_EXEC);
                intent.putExtra(MainManager.CMD, aliasValue);
                intent.putExtra(MainManager.NEED_WRITE_INPUT, true);
                LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
            }
        }
    }
}
