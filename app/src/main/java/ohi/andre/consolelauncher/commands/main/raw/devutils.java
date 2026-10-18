package ohi.andre.consolelauncher.commands.main.raw;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

import ohi.andre.consolelauncher.BuildConfig;
import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.tuils.Tuils;

/**
 * Created by francescoandreuzzi on 22/08/2017.
 */

public class devutils extends ParamCommand {

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {
        notify {
            @Override
            public String exec(ExecutePack pack) {
                List<String> text = pack.getList();

                String title, txt = null;
                if(text.size() == 0) return null;
                else {
                    title = text.remove(0);
                    if(!text.isEmpty()) txt = Tuils.toPlanString(text, Tuils.SPACE);
                }

                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(pack.context,
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    if (pack.context instanceof Activity) {
                        ActivityCompat.requestPermissions((Activity) pack.context,
                                new String[]{Manifest.permission.POST_NOTIFICATIONS}, LauncherActivity.COMMAND_REQUEST_PERMISSION);
                        return pack.context.getString(R.string.output_waitingpermission);
                    }
                    return "Allow notifications in Android settings to post a test notification.";
                }

                NotificationManagerCompat.from(pack.context).notify(200,
                        new NotificationCompat.Builder(pack.context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(txt)
                            .build());

                return null;
            }

            @Override
            public int[] args() {
                return new int[] {CommandAbstraction.TEXTLIST};
            }
        },
        check_notifications {
            @Override
            public int[] args() {
                return new int[0];
            }

            @Override
            public String exec(ExecutePack pack) {
                return "Notification access: " + NotificationManagerCompat.getEnabledListenerPackages(pack.context).contains(BuildConfig.APPLICATION_ID) + Tuils.NEWLINE + "Notification service running: " + Tuils.notificationServiceIsRunning(pack.context);
            }
        };

        static Param get(String p) {
            p = p.toLowerCase();
            Param[] ps = values();
            for (Param p1 : ps)
                if (p.endsWith(p1.label()))
                    return p1;
            return null;
        }

        static String[] labels() {
            Param[] ps = values();
            String[] ss = new String[ps.length];

            for (int count = 0; count < ps.length; count++) {
                ss[count] = ps[count].label();
            }

            return ss;
        }

        @Override
        public String label() {
            return Tuils.MINUS + name();
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
            return pack.context.getString(R.string.help_devutils);
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
            return null;
        }
    }

    @Override
    protected ohi.andre.consolelauncher.commands.main.Param paramForString(MainPack pack, String param) {
        return Param.get(param);
    }

    @Override
    public int priority() {
        return 2;
    }

    @Override
    public int helpRes() {
        return R.string.help_devutils;
    }

    @Override
    public String[] params() {
        return Param.labels();
    }

    @Override
    protected String doThings(ExecutePack pack) {
        return null;
    }
}
