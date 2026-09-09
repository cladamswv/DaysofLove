package com.carl.lovewithmary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DailyUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AppState.ensureMigration(context);
        String action = intent == null ? null : intent.getAction();

        if (LoveUpdater.ACTION_REMINDERS.equals(action)) {
            ReminderEngine.checkAndSend(context);
            LoveUpdater.scheduleAlarms(context);
            return;
        }

        LoveUpdater.refreshEverything(context);
        if (action == null ||
                Intent.ACTION_DATE_CHANGED.equals(action) ||
                Intent.ACTION_TIME_SET.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            ReminderEngine.checkAndSend(context);
        }
    }
}
