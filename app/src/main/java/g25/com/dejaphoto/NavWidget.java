package g25.com.dejaphoto;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
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
import android.widget.Button;
import android.content.ComponentName;

/**
 * Implementation of App Widget functionality.
 * Source: //http://stackoverflow.com/questions/23220757/android-widget-onclick-listener-for-several-buttons
 */
public class NavWidget extends AppWidgetProvider {

    //static WallpaperChanger receiver;
    //static WallpaperChanger wallpaperChanger;
    private static final String NEXT = "NEXT";
    private static final String PREV = "PREV";
    private static final String RELEASE = "RELEASE";
    private static final String KARMA = "KARMA";


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
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Enter relevant functionality for when the first widget is created


        ComponentName thisWidget = new ComponentName(context, NavWidget.class);
        // There may be multiple widgets active, so update all of them
        //Iterate through all of our widgets
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds){


            //Get RemoteView object
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.nav_widget);


            remoteViews.setOnClickPendingIntent(R.id.next, getPendingSelfIntent(context, NEXT));
            remoteViews.setOnClickPendingIntent(R.id.prev, getPendingSelfIntent(context, PREV));
            remoteViews.setOnClickPendingIntent(R.id.release, getPendingSelfIntent(context, RELEASE));
            remoteViews.setOnClickPendingIntent(R.id.karma, getPendingSelfIntent(context,KARMA));


            appWidgetManager.updateAppWidget(thisWidget, remoteViews);

            //specify the action that should occur when the Button is tapped
            //Intent intent = new Intent(context, NavWidget.class);
            //intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);

            //This is the same action sent by the system when the widget needs to be updated automatically
            // PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        }
    }

    //Use PendingIntent to request manual update when the update button is clicked
    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, DejaPhotoService.class);
        intent.setAction(action);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
// onClick action is here
        Intent sendToService = new Intent(context, DejaPhotoService.class);
        if (NEXT.equals(intent.getAction())) {
            sendToService.setAction(NEXT);
            Toast.makeText(context, "NEXT", Toast.LENGTH_SHORT).show();
            Log.w("Widget", "Clicked NEXT");
        } else if (PREV.equals(intent.getAction())) {
            sendToService.setAction(PREV);
            Toast.makeText(context, "PREV", Toast.LENGTH_SHORT).show();
            Log.w("Widget", "Clicked PREV");
        } else if (RELEASE.equals(intent.getAction())) {
            sendToService.setAction(RELEASE);
            Toast.makeText(context, "RELEASE", Toast.LENGTH_SHORT).show();
            Log.w("Widget", "Clicked RELEASE");
        }
        else if (KARMA.equals(intent.getAction())){
            sendToService.setAction(KARMA);
            Toast.makeText(context, "KARMA", Toast.LENGTH_SHORT).show();
            Log.w("Widget", "Clicked KARMA");}
    }




}

