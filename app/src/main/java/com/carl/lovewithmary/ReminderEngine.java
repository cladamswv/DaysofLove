package com.carl.lovewithmary;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.time.LocalDate;

public final class ReminderEngine {
    private static final long[] MILESTONES = new long[]{100, 250, 500, 750, 1000, 1500, 2000, 2500, 3000, 4000, 5000};

    private ReminderEngine() {}

    public static void checkAndSend(Context context) {
        AppState.ensureMigration(context);
        if (!AppState.masterNotificationsEnabled(context) || !AppState.hasStartDate(context)) return;
        if (!LoveUpdater.canPostNotifications(context)) return;

        LoveUpdater.createChannels(context);
        checkAnniversary(context);
        checkGiftPlan(context);
        checkMonthsary(context);
        checkMilestone(context);
    }

    private static void checkAnniversary(Context context) {
        if (!AppState.anniversaryNotificationsEnabled(context)) return;
        long days = AppState.daysUntilNextAnniversary(context);
        if (days < 0 || days > Integer.MAX_VALUE) return;
        int offset = (int) days;
        boolean explicitOffset = AppState.anniversaryOffsetEnabled(context, offset);
        boolean dailyFinalWeek = AppState.anniversaryFinalWeekDailyEnabled(context) && offset >= 1 && offset <= 7;
        if (!explicitOffset && !dailyFinalWeek) return;

        int anniversaryNumber = AppState.nextAnniversaryNumber(context);
        String eventKey = LocalDate.now() + "|" + anniversaryNumber + "|" + offset;
        if (!AppState.shouldSendEvent(context, "anniversary", eventKey)) return;

        String partner = AppState.partnerDisplayName(context);
        String title;
        String text;
        if (offset == 0) {
            title = "Happy " + AppState.ordinal(anniversaryNumber) + " anniversary ❤️";
            text = "Celebrate another year of love with " + partner + ".";
        } else if (offset == 1) {
            title = "Your anniversary is tomorrow 💕";
            text = "Tomorrow is your " + AppState.ordinal(anniversaryNumber) + " anniversary with " + partner + ".";
        } else {
            title = offset + " days until your anniversary ❤️";
            text = "Your " + AppState.ordinal(anniversaryNumber) + " anniversary with " + partner + " is getting close.";
        }
        if (AppState.giftNotificationsEnabled(context) && (offset == 7 || offset == 3)) {
            text = text + " Gift handled? 🎁";
        }
        send(context, "anniversary", 301, title, text);
        AppState.markEventSent(context, "anniversary", eventKey);
    }

    private static void checkGiftPlan(Context context) {
        if (!AppState.giftNotificationsEnabled(context)) return;
        long days = AppState.daysUntilNextAnniversary(context);
        if (days != 7 && days != 3) return;
        // If an anniversary reminder is already scheduled for this same day, fold the gift nudge into it.
        if (AppState.anniversaryNotificationsEnabled(context) &&
                (AppState.anniversaryOffsetEnabled(context, (int) days) || AppState.anniversaryFinalWeekDailyEnabled(context))) {
            return;
        }

        String eventKey = LocalDate.now() + "|gift|" + days;
        if (!AppState.shouldSendEvent(context, "gift", eventKey)) return;

        String partner = AppState.partnerDisplayName(context);
        String title = "Gift handled? 🎁";
        String text = days == 7
                ? "Your anniversary with " + partner + " is one week away."
                : "Three days to go until your anniversary with " + partner + ".";
        send(context, "gift", 302, title, text);
        AppState.markEventSent(context, "gift", eventKey);
    }

    private static void checkMonthsary(Context context) {
        if (!AppState.monthsaryNotificationsEnabled(context)) return;
        AppState.MonthsaryInfo info = AppState.nextMonthsary(context);
        if (info == null) return;

        // On 12, 24, 36... months, the anniversary reminder is the cleaner celebration.
        if (info.monthNumber % 12 == 0) return;

        long days = AppState.daysUntilNextMonthsary(context);
        if (days < 0 || days > Integer.MAX_VALUE) return;
        int offset = (int) days;
        if (!AppState.monthsaryOffsetEnabled(context, offset)) return;

        String eventKey = LocalDate.now() + "|" + info.monthNumber + "|" + offset;
        if (!AppState.shouldSendEvent(context, "monthsary", eventKey)) return;

        String partner = AppState.partnerDisplayName(context);
        String title;
        String text;
        if (offset == 0) {
            title = "Happy " + AppState.ordinal(info.monthNumber) + " monthsary 💗";
            text = "Another month of love with " + partner + ".";
        } else {
            title = "Your monthsary is tomorrow 💕";
            text = "Tomorrow marks " + info.monthNumber + " months with " + partner + ".";
        }
        send(context, "monthsary", 303, title, text);
        AppState.markEventSent(context, "monthsary", eventKey);
    }

    private static void checkMilestone(Context context) {
        if (!AppState.milestoneNotificationsEnabled(context)) return;
        long days = AppState.daysOfLove(context);
        boolean milestone = false;
        for (long value : MILESTONES) {
            if (days == value) {
                milestone = true;
                break;
            }
        }
        if (!milestone && days > 5000 && days % 1000 == 0) milestone = true;
        if (!milestone) return;

        String eventKey = LocalDate.now() + "|" + days;
        if (!AppState.shouldSendEvent(context, "milestone", eventKey)) return;

        String partner = AppState.partnerDisplayName(context);
        send(
                context,
                "milestone",
                304,
                days + " days of love! 💖",
                "A beautiful milestone with " + partner + "."
        );
        AppState.markEventSent(context, "milestone", eventKey);
    }

    private static void send(Context context, String tag, int id, String title, String text) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon_large);
        Notification notification = new Notification.Builder(context, LoveUpdater.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_heart)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(LoveUpdater.openAppPendingIntent(context, id))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_REMINDER)
                .build();

        manager.notify(tag, id, notification);
    }
}
