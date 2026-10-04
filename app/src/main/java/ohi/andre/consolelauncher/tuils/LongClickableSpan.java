package ohi.andre.consolelauncher.tuils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import ohi.andre.consolelauncher.MainManager;
import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.managers.notifications.NotificationManager;
import ohi.andre.consolelauncher.managers.notifications.NotificationRepository;
import ohi.andre.consolelauncher.managers.notifications.NotificationService;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Notifications;

/**
 * Created by francescoandreuzzi on 22/10/2017.
 */

public class LongClickableSpan extends ClickableSpan {

    public static int longPressVibrateDuration = -1;

    private Object clickO, longClickO;
    private String longIntentKey;

    private static boolean set = false, showMenu;
    private static boolean showExcludeApp, showExcludeNotification, showReply;

    public LongClickableSpan(Object clickAction, Object longClickAction) {
        this.clickO = clickAction;
        this.longClickO = longClickAction;
        this.longIntentKey = null;
    }

    public LongClickableSpan(Object clickAction) {
        this.clickO = clickAction;
        this.longClickO = null;
        this.longIntentKey = null;
    }

    public LongClickableSpan(Object clickAction, Object longClickAction, String longIntentKey) {
        this.clickO = clickAction;
        this.longClickO = longClickAction;
        this.longIntentKey = longIntentKey;
    }

    public LongClickableSpan(Object clickAction, String longIntentKey) {
        this.clickO = clickAction;
        this.longClickO = null;
        this.longIntentKey = longIntentKey;
    }

    public LongClickableSpan(String longIntentKey) {
        this.clickO = null;
        this.longClickO = null;
        this.longIntentKey = longIntentKey;
    }

    public void updateDrawState(TextPaint ds) {}

    @Override
    public void onClick(View widget) {
        Context context = widget != null ? widget.getContext() : null;
        if (clickO instanceof NotificationService.Notification) {
            openNotificationApp(context, (NotificationService.Notification) clickO);
        } else if (clickO instanceof String && ((String) clickO).startsWith("NOTIF::")) {
            String notifId = (String) clickO;
            NotificationService.Notification n = NotificationRepository.get(notifId);
            if (n != null) {
                openNotificationApp(context, n);
            } else {
                String pkg = NotificationRepository.extractPackageFromId(notifId);
                if (pkg != null) {
                    openAppByPackage(context, pkg);
                }
            }
        } else if (clickO instanceof PendingIntent) {
            openPendingIntent(context, (PendingIntent) clickO);
        } else {
            execute(widget, clickO);
        }
    }

    private static void openPendingIntent(Context context, PendingIntent pi) {
        if (pi == null || context == null) return;
        boolean sent = false;
        try {
            if (context instanceof Activity) {
                ((Activity) context).startIntentSender(pi.getIntentSender(), null, 0, 0, 0);
                sent = true;
            } else {
                Intent fillInIntent = new Intent();
                fillInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                pi.send(context, 0, fillInIntent);
                sent = true;
            }
        } catch (Exception e) {
            Tuils.log(e);
        }
        if (!sent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && pi.getCreatorPackage() != null) {
            openAppByPackage(context, pi.getCreatorPackage());
        }
    }

    private static void openNotificationApp(Context context, NotificationService.Notification n) {
        if (n == null || context == null) return;

        Activity activity = null;
        Context curr = context;
        while (curr instanceof ContextWrapper) {
            if (curr instanceof Activity) {
                activity = (Activity) curr;
                break;
            }
            curr = ((ContextWrapper) curr).getBaseContext();
        }

        Context launchContext = activity != null ? activity : context;
        boolean opened = false;

        // 1. Primary: Trigger the specific PendingIntent
        if (n.pendingIntent != null) {
            try {
                if (activity != null) {
                    try {
                        activity.startIntentSender(
                                n.pendingIntent.getIntentSender(),
                                null,
                                0,
                                0,
                                0
                        );
                        opened = true;
                    } catch (Exception e1) {
                        Intent fillInIntent = new Intent();
                        fillInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        n.pendingIntent.send(launchContext, 0, fillInIntent);
                        opened = true;
                    }
                } else {
                    Intent fillInIntent = new Intent();
                    fillInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    n.pendingIntent.send(launchContext, 0, fillInIntent);
                    opened = true;
                }
            } catch (Exception e) {
                Tuils.log(e);
            }
        }

        // 2. Fallback: Open application by package name
        if (!opened && n.pkg != null) {
            openAppByPackage(launchContext, n.pkg);
        }
    }

    private static void openAppByPackage(Context context, String pkg) {
        if (context == null || pkg == null) return;
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(pkg);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(launchIntent);
            } else {
                Tuils.sendOutput(context, "App not found for package: " + pkg);
            }
        } catch (Exception e) {
            Tuils.log(e);
        }
    }

    public void onLongClick(View widget) {
        if(execute(widget, longClickO, longIntentKey) && longPressVibrateDuration > 0) ((Vibrator) widget.getContext().getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(longPressVibrateDuration);
    }

    private static boolean execute(View v, Object o) {
        return execute(v, o, null);
    }

    private static boolean execute(final View v, Object o, String intentKey) {
        if(o == null) return false;

        if (o instanceof String && ((String) o).startsWith("NOTIF::")) {
            NotificationService.Notification n = NotificationRepository.get((String) o);
            if (n != null) {
                o = n;
            }
        }

        if(!set) {
            set = true;

            showExcludeApp = XMLPrefsManager.getBoolean(Notifications.notification_popup_exclude_app);
            showExcludeNotification = XMLPrefsManager.getBoolean(Notifications.notification_popup_exclude_notification);
            showReply = XMLPrefsManager.getBoolean(Notifications.notification_popup_reply);

            showMenu = (showExcludeApp && showExcludeNotification) || (showExcludeApp && showReply) || (showExcludeNotification && showReply);
        }

        if(o instanceof String) {
            Intent intent = new Intent(intentKey != null ? intentKey : MainManager.ACTION_EXEC);
            intent.putExtra(PrivateIOReceiver.TEXT, (String) o);

            if(intentKey == null || intentKey.equals(MainManager.ACTION_EXEC)) {
                intent.putExtra(MainManager.NEED_WRITE_INPUT, false);
                intent.putExtra(MainManager.CMD_COUNT, MainManager.commandCount);
            }

            LocalBroadcastManager.getInstance(v.getContext().getApplicationContext()).sendBroadcast(intent);
        } else if(o instanceof PendingIntent) {
            PendingIntent pi = (PendingIntent) o;

            boolean sent = false;
            try {
                pi.send();
                sent = true;
            } catch (Exception e) {
                Tuils.log(e);
            }
            if (!sent && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && pi.getCreatorPackage() != null) {
                try {
                    Intent launchIntent = v.getContext().getPackageManager().getLaunchIntentForPackage(pi.getCreatorPackage());
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        v.getContext().startActivity(launchIntent);
                    }
                } catch (Exception e) {
                    Tuils.log(e);
                }
            }
        } else if(o instanceof Uri) {
            Intent i = new Intent(Intent.ACTION_VIEW, (Uri) o);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                v.getContext().startActivity(i);
            } catch (Exception e) {
                Tuils.sendOutput(Color.RED, v.getContext(), e.toString());
            }
        } else if(o instanceof NotificationService.Notification) {
            final NotificationService.Notification n = (NotificationService.Notification) o;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if(showMenu) {
                    PopupMenu menu = new PopupMenu(v.getContext().getApplicationContext(), v);
                    menu.getMenuInflater().inflate(R.menu.notification_menu, menu.getMenu());

                    menu.getMenu().findItem(R.id.exclude_app).setVisible(showExcludeApp);
                    menu.getMenu().findItem(R.id.exclude_notification).setVisible(showExcludeNotification);
                    menu.getMenu().findItem(R.id.reply_notification).setVisible(showReply);

                    menu.setOnMenuItemClickListener(item -> {
                        int id = item.getItemId();

                        if (id == R.id.exclude_app) {
                            NotificationManager.setState(n.pkg, false);
                        } else if (id == R.id.exclude_notification) {
                            Tuils.log(n.text);
                            NotificationManager.addFilter(n.text, -1);
                        } else if (id == R.id.reply_notification) {
                            Intent intent = new Intent(PrivateIOReceiver.ACTION_INPUT);
                            intent.putExtra(PrivateIOReceiver.TEXT, "reply -to " + n.pkg + Tuils.SPACE);

                            LocalBroadcastManager.getInstance(v.getContext().getApplicationContext()).sendBroadcast(intent);
                        } else {
                            return false;
                        }

                        return true;
                    });

                    menu.show();
                } else {
                    if(showReply) {
                        Intent intent = new Intent(PrivateIOReceiver.ACTION_INPUT);
                        intent.putExtra(PrivateIOReceiver.TEXT, "reply -to " + n.pkg + Tuils.SPACE);

                        LocalBroadcastManager.getInstance(v.getContext().getApplicationContext()).sendBroadcast(intent);
                    }
                    else if(showExcludeNotification) NotificationManager.addFilter(n.text, -1);
                    else if(showExcludeApp) NotificationManager.setState(n.pkg, false);
                }
            }
        }

        return true;
    }
}

