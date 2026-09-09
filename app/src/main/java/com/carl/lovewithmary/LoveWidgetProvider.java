package com.carl.lovewithmary;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

public class LoveWidgetProvider extends AppWidgetProvider {

    public static RemoteViews buildRemoteViews(Context context, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_love);
        views.setImageViewBitmap(R.id.widget_background_image, WidgetRenderer.renderBackground(context));
        views.setTextViewText(R.id.widget_days, AppState.counterText(context));
        views.setTextColor(R.id.widget_days, AppState.widgetTextColor(context));
        views.setTextColor(R.id.widget_since, AppState.widgetTextColor(context));
        views.setTextViewText(R.id.widget_since, AppState.sinceText(context));

        int minWidth = 260;
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Bundle options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId);
            minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 260);
        }

        if (minWidth < 220) {
            views.setTextViewTextSize(R.id.widget_days, TypedValue.COMPLEX_UNIT_SP, 16f);
            views.setViewVisibility(R.id.widget_since, View.GONE);
        } else {
            views.setTextViewTextSize(R.id.widget_days, TypedValue.COMPLEX_UNIT_SP, 20f);
            views.setViewVisibility(R.id.widget_since, View.VISIBLE);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        AppState.ensureMigration(context);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context, appWidgetId));
        }
        LoveUpdater.scheduleAlarms(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context, appWidgetId));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        LoveUpdater.scheduleAlarms(context);
    }
}
