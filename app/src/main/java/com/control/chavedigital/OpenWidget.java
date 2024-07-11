package com.control.chavedigital;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class OpenWidget extends AppWidgetProvider {
    // Update widgets
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Get views from widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.open_widget);

        // Creates openIntent
        Intent openIntent = new Intent(context, OpenBdReceiver.class);
        PendingIntent openPendingIntent =
                PendingIntent.getBroadcast(context, 1, openIntent, PendingIntent.FLAG_IMMUTABLE);

        // Adds intent to widget onCLick
        views.setOnClickPendingIntent(R.id.widget_abrir, openPendingIntent);

        // Updates the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // When widget is updated
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Updates all widgets
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}