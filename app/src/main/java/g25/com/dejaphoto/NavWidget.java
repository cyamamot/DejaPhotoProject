package g25.com.dejaphoto;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;

import android.content.Intent;
import android.net.Uri;
import android.app.PendingIntent;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.ImageButton;

/**
 * Implementation of App Widget functionality.
 */
public class NavWidget extends AppWidgetProvider {

    static WallpaperChanger wallpaperChanger;
    ImageButton PREV, NEXT;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.nav_widget);
        //views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        int count = appWidgetIds.length;

        //Iterate through all of our widgets
        for(int i = 0; i < count; i++) {

            int widgetId = appWidgetIds[i];

            //Get RemoteView object, update RemoteViews text view with random number

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.nav_widget);
           // remoteViews.setTextViewText(R.id.textview, number);

            //specify the action that should occur when the Button is tapped
            Intent intent = new Intent(context, NavWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_ENABLED);

            //Use PendingIntent to request manual update when the update button is clicked
            //Then Actions for the intent is set to ACTION_APPWIDGET_UPDATE
            //This is the same action sent by the system when the widget needs to be updated automatically
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.next, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);

        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        wallpaperChanger = new WallpaperChanger(context);

        PREV = (ImageButton)PREV.findViewById(R.id.prev);
        PREV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        NEXT = (ImageButton)NEXT.findViewById(R.id.next);
        NEXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wallpaperChanger.next();
            }
        });


    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

