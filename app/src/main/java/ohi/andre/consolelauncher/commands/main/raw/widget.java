package ohi.andre.consolelauncher.commands.main.raw;

import android.appwidget.AppWidgetHostView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ohi.andre.consolelauncher.LauncherActivity;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.tuils.Tuils;

public class widget implements CommandAbstraction {

    @Override
    public String exec(ExecutePack pack) throws Exception {
        MainPack info = (MainPack) pack;

        if (pack.args != null && pack.args.length > 0) {
            String argumentText = pack.args[0] == null ? "" : pack.args[0].toString().trim();
            if (argumentText.isEmpty()) return "Usage: widget [add | list | move | height | remove | clear | perm]";
            String[] arguments = argumentText.split("\\s+");
            String arg = arguments[0];
            if (arg.equalsIgnoreCase("add")) {
                if (info.context instanceof LauncherActivity) {
                    if (info.widgetManager == null) {
                        return "Widget support is not ready. Restart T-UI and try again.";
                    }
                    if (!info.widgetManager.isDefaultLauncher()) {
                        Tuils.sendOutput(info.context, "WARNING: T-UI is NOT your default launcher. Some widgets might fail to bind.");
                    }
                    ((LauncherActivity) info.context).runOnUiThread(() -> info.widgetManager.pickWidget((LauncherActivity) info.context));
                    return null;
                }
            } else if (arg.equalsIgnoreCase("perm")) {
                return "Android controls widget binding. Set Cythernoir/T-UI as your Home app, then use `widget add` and approve Android's picker or binding prompt.";
            } else if (arg.equalsIgnoreCase("list")) {
                List<Integer> ids = info.widgetManager.getActiveWidgetIds();
                if (ids.isEmpty()) return "No active widgets.";
                StringBuilder sb = new StringBuilder("Active Widgets:\n");
                for (int id : ids) {
                    sb.append("- ID: ").append(id).append("\n");
                }
                return sb.toString();
            } else if (arg.equalsIgnoreCase("move")) {
                if (arguments.length > 2 && info.context instanceof LauncherActivity) {
                    try {
                        int id = Integer.parseInt(arguments[1]);
                        String dir = arguments[2];
                        boolean up = dir.equalsIgnoreCase("up");
                        boolean moved = info.widgetManager.moveWidget((LauncherActivity) info.context, id, up);
                        return moved ? "Widget " + id + " moved " + (up ? "up" : "down") + "." : "Unable to move widget " + id;
                    } catch (Exception e) {
                        return "Usage: widget move [ID] [up|down]";
                    }
                }
                return "Usage: widget move [ID] [up|down]";
            } else if (arg.equalsIgnoreCase("height") || arg.equalsIgnoreCase("resize")) {
                if (arguments.length > 2 && info.context instanceof LauncherActivity) {
                    try {
                        int id = Integer.parseInt(arguments[1]);
                        int heightDp = Integer.parseInt(arguments[2]);
                        boolean resized = info.widgetManager.setWidgetHeight((LauncherActivity) info.context, id, heightDp);
                        return resized ? "Widget " + id + " height updated to " + heightDp + "dp." : "Unable to resize widget " + id;
                    } catch (Exception e) {
                        return "Usage: widget height [ID] [dp]";
                    }
                }
                return "Usage: widget height [ID] [dp]";
            } else if (arg.equalsIgnoreCase("remove")) {
                if (arguments.length > 1) {
                    try {
                        int id = Integer.parseInt(arguments[1]);
                        if (!info.widgetManager.removeWidget(id)) return "Widget ID " + id + " is not active. Use `widget list` to see saved IDs.";
                        if (info.context instanceof LauncherActivity) {
                            ((LauncherActivity) info.context).refreshPersistentContainer();
                        }
                        return "Widget " + id + " removed.";
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
                        ((LauncherActivity) info.context).updatePersistentAreaVisibility();
                    });
                }
                return "Persistent widgets cleared.";
            }
        }

        return "Usage: widget [add | list | move | height | remove | clear | perm]";
    }

    @Override
    public int[] argType() {
        // PLAIN_TEXT is the full remainder; subcommand operands are split in exec().
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
