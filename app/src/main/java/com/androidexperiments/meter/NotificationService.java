package com.androidexperiments.meter;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Service receiving notifications from other apps
 */
public class NotificationService extends NotificationListenerService {
    public static final String NOTIFICATION_UPDATE = "com.androidexperiments.utilitywallpaper.NOTIFICATION_UPDATE";
    public static boolean permissionsGranted = false;
    public static int numNotifications = 0;

    private String TAG = this.getClass().getSimpleName();

    public interface NotificationKey {
        String ACTION = "notification_event_action";
        String APPLICATION_PACKAGE_NAME = "notification_event_packagename";
        String APPLICATION_PACKAGES = "notification_event_packages";
    }

    public interface NotificationAction {
        String NOTIFICATION_POSTED = "onNotificationPosted";
        String NOTIFICATION_REMOVED = "onNotificationRemoved";
    }

    /**
     * onBind is called when the service permissions are activated in settings
     */
    @Override
    public IBinder onBind(Intent intent) {
        permissionsGranted = true;
        return super.onBind(intent);
    }

    /**
     * onUnbind is called when permissions are removed from the service
     */
    @Override
    public boolean onUnbind(Intent intent) {
        permissionsGranted = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        numNotifications = getActiveNotifications().length;
    }

    /**
     * Called when new notifications are posted to the system
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Intent intent = new  Intent(NOTIFICATION_UPDATE);
        intent.putExtra(NotificationKey.ACTION, NotificationAction.NOTIFICATION_POSTED);
        intent.putExtra(NotificationKey.APPLICATION_PACKAGE_NAME, sbn.getPackageName());

        // List the notifications in a string array
        StatusBarNotification[] activeNotifications = NotificationService.this.getActiveNotifications();
        String packages[] = new String[activeNotifications.length];
        int i = 0;
        for (StatusBarNotification nf : activeNotifications) {
            packages[i++] = nf.getPackageName();
        }
        intent.putExtra(NotificationKey.APPLICATION_PACKAGES, packages);

        // Broadcast the intent
        sendBroadcast(intent);

        // Store the number of notifications
        numNotifications = packages.length;
    }

    /**
     * Called when notifications are removed
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Intent intent = new  Intent(NOTIFICATION_UPDATE);
        intent.putExtra(NotificationKey.ACTION, NotificationAction.NOTIFICATION_REMOVED);
        intent.putExtra(NotificationKey.APPLICATION_PACKAGE_NAME,sbn.getPackageName());

        // List the notifications in a string array
        StatusBarNotification[] activeNotifications = NotificationService.this.getActiveNotifications();
        String packages[] = new String[activeNotifications.length];
        int i = 0;
        for (StatusBarNotification nf : activeNotifications) {
            packages[i++] = nf.getPackageName();
        }
        intent.putExtra(NotificationKey.APPLICATION_PACKAGES, packages);

        // Broadcast the intent
        sendBroadcast(intent);

        numNotifications = packages.length;
    }
}
