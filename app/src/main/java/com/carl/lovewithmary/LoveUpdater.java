package com.carl.lovewithmary;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import java.util.Calendar;

public final class LoveUpdater {
    public static final String COUNTER_CHANNEL_ID = "love_counter_v2";
    public static final String REMINDER_CHANNEL_ID = "love_reminders_v2";
    public static final int COUNTER_NOTIFICATION_ID = 214;

    public static final String ACTION_REFRESH = "com.carl.lovewithmary.ACTION_REFRESH";
    public static final String ACTION_REMINDERS = "com.carl.lovewithmary.ACTION_REMINDERS";

    private static final int REFRESH_REQUEST_CODE = 215;
    private static final int REMINDER_REQUEST_CODE = 216;

    private LoveUpdater() {}

    public static void refreshEverything(Context context) {
        AppState.ensureMigration(context);
        updateAllWidgets(context);

        if (AppState.masterNotificationsEnabled(context) && AppState.persistentCounterEnabled(context)) {
            showOrUpdateCounterNotification(context);
        } else {
            cancelCounterNotification(context);
        }

        scheduleAlarms(context);
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, LoveWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        for (int id : ids) {
            manager.updateAppWidget(id, LoveWidgetProvider.buildRemoteViews(context, id));
        }
    }

    public static void createChannels(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        NotificationChannel counter = new NotificationChannel(
                COUNTER_CHANNEL_ID,
                "Daily love counter",
                NotificationManager.IMPORTANCE_LOW
        );
        counter.setDescription("Keeps your current Days of Love count visible in notifications.");
        counter.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        counter.setShowBadge(false);
        manager.createNotificationChannel(counter);

        NotificationChannel reminders = new NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Love reminders",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        reminders.setDescription("Anniversary, monthsary, milestone and gift-plan reminders.");
        reminders.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(reminders);
    }

    public static boolean canPostNotifications(Context context) {
        return Build.VERSION.SDK_INT < 33 ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void showOrUpdateCounterNotification(Context context) {
        if (!AppState.hasStartDate(context) || !canPostNotifications(context)) return;
        createChannels(context);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon_large);
        String title = AppState.counterText(context);
        String detail = AppState.nextAnniversaryNotificationLine(context);

        Notification notification = new Notification.Builder(context, COUNTER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_heart)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(detail)
                .setStyle(new Notification.BigTextStyle().bigText(detail))
                .setContentIntent(contentIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_STATUS)
                .build();

        manager.notify(COUNTER_NOTIFICATION_ID, notification);
    }

    public static void cancelCounterNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancel(COUNTER_NOTIFICATION_ID);
    }

    public static void cancelAllNotifications(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.cancelAll();
    }

    public static PendingIntent openAppPendingIntent(Context context, int requestCode) {
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                context,
                requestCode,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    public static void scheduleAlarms(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Widget and persistent counter refresh shortly after midnight.
        PendingIntent refreshIntent = broadcastPendingIntent(context, ACTION_REFRESH, REFRESH_REQUEST_CODE);
        long refreshAt = nextTimeMillis(0, 5);
        alarmManager.cancel(refreshIntent);
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                refreshAt,
                AlarmManager.INTERVAL_DAY,
                refreshIntent
        );

        // Reminder check at the user's preferred daytime hour.
        PendingIntent reminderIntent = broadcastPendingIntent(context, ACTION_REMINDERS, REMINDER_REQUEST_CODE);
        long reminderAt = nextTimeMillis(AppState.reminderHour(context), 0);
        alarmManager.cancel(reminderIntent);
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                reminderAt,
                AlarmManager.INTERVAL_DAY,
                reminderIntent
        );
    }

    private static PendingIntent broadcastPendingIntent(Context context, String action, int requestCode) {
        Intent intent = new Intent(context, DailyUpdateReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static long nextTimeMillis(int hour, int minute) {
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, hour);
        next.set(Calendar.MINUTE, minute);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }
        return next.getTimeInMillis();
    }
}
