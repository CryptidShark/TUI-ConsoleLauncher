package ohi.andre.consolelauncher.managers.notifications;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationRepository {

    private static final Map<String, NotificationService.Notification> sCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 100;

    public static String store(NotificationService.Notification notification) {
        if (notification == null) return null;

        String pkgName = notification.pkg != null ? notification.pkg : "unknown";
        String id = "NOTIF::" + pkgName + "::" + System.currentTimeMillis() + "::" + Math.abs(notification.hashCode());

        if (sCache.size() >= MAX_CACHE_SIZE) {
            Iterator<String> it = sCache.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }

        sCache.put(id, notification);
        return id;
    }

    public static NotificationService.Notification get(String id) {
        if (id == null) return null;
        return sCache.get(id);
    }

    public static String extractPackageFromId(String id) {
        if (id == null || !id.startsWith("NOTIF::")) return null;
        String[] parts = id.split("::");
        if (parts.length >= 2) {
            return parts[1];
        }
        return null;
    }
}
