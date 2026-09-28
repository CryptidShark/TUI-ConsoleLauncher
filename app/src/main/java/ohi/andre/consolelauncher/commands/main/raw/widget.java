package ohi.andre.consolelauncher.commands.main.raw;

import android.appwidget.AppWidgetHostView;
import android.view.ViewGroup;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.tuils.Tuils;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;

public class widget implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;

        if (pack.args.length > 0) {
            String arg = pack.args[0].toString();
            if (arg.equalsIgnoreCase("add")) {
                if (info.context instanceof LauncherActivity) {
                    if (!info.widgetManager.isDefaultLauncher()) {
                        Tuils.sendOutput(info.context, "WARNING: T-UI is NOT your default launcher. Some widgets might fail to bind.");
                    }
                    ((LauncherActivity) info.context).runOnUiThread(() -> info.widgetManager.pickWidget((LauncherActivity) info.context));
                    return null;
                }
            } else if (arg.equalsIgnoreCase("perm")) {
                return "Note: Widgets require 'Bind Widgets' permission. If 'widget add' fails, check your device 'Special App Access' settings.";
            } else if (arg.equalsIgnoreCase("list")) {
                List<Integer> ids = info.widgetManager.getActiveWidgetIds();
                if (ids.isEmpty()) return "No active widgets.";
                StringBuilder sb = new StringBuilder("Active Widgets:\n");
                for (int id : ids) sb.append("- ID: ").append(id).append("\n");
                return sb.toString();
            } else if (arg.equalsIgnoreCase("remove")) {
                if (pack.args.length > 1) {
                    try {
                        int id = Integer.parseInt(pack.args[1].toString());
                        info.widgetManager.removeWidget(id);
                        return "Widget " + id + " removed from system host.";
                    } catch (Exception e) {
                        return "Invalid ID.";
                    }
                }
                return "Usage: widget remove [ID]";
            } else if (arg.equalsIgnoreCase("clear")) {
                info.widgetManager.clearAllWidgets();
                if (info.context instanceof LauncherActivity) {
                    ((LauncherActivity) info.context).runOnUiThread(() -> {
                        ViewGroup pc = ((LauncherActivity) info.context).findViewById(R.id.persistent_container);
                        if (pc != null) {
                            for (int i = 0; i < pc.getChildCount(); i++) {
                                View child = pc.getChildAt(i);
                                if (child instanceof AppWidgetHostView || "widget_container".equals(child.getTag())) {
                                    pc.removeViewAt(i--);
                                }
                            }
                        }
                    });
                }
                return "Persistent widgets cleared.";
            }
        }

        return "Usage: widget [add | list | remove | clear] (Use 'manual' for customization guide)";
    }

    @Override
    public int[] argType() {
        return new int[] {CommandAbstraction.PLAIN_TEXT};
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public int helpRes() {
        return 0;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int indexNotFound) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return null;
    }
}
