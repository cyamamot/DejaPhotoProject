package g25.com.dejaphoto;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Implementation of App Widget functionality.
 * Source: //http://stackoverflow.com/questions/23220757/android-widget-onclick-listener-for-several-buttons
 */
public class NavWidget extends AppWidgetProvider {

    //static WallpaperChanger receiver;
    //static WallpaperChanger wallpaperChanger;
    static final String NEXT = "NEXT";
    static final String PREV = "PREV";
    static final String RELEASE = "RELEASE";
    static final String KARMA = "KARMA";


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.nav_widget);
        //views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Log.i("epdateAppWidget", "widget updated");

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

            Log.i("updating id : ", String.valueOf(widgetId));
        }
    }

    /**
     * Description: Use PendingIntent to request manual update when the update button is clicked
     */
    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, DejaPhotoService.class);
        intent.setAction(action);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // create the intent to go to the DejaPhotoService class
        Intent sendToService = new Intent(context, DejaPhotoService.class);

        // set the action for the intent
        if (NEXT.equals(intent.getAction())) {
            sendToService.setAction(NEXT);
            Toast.makeText(context, "NEXT", Toast.LENGTH_SHORT).show();
            Log.v("Widget", "Clicked NEXT");
        } else if (PREV.equals(intent.getAction())) {
            sendToService.setAction(PREV);
            Toast.makeText(context, "PREV", Toast.LENGTH_SHORT).show();
            Log.v("Widget", "Clicked PREV");
        } else if (RELEASE.equals(intent.getAction())) {
            sendToService.setAction(RELEASE);
            Toast.makeText(context, "RELEASE", Toast.LENGTH_SHORT).show();
            Log.v("Widget", "Clicked RELEASE");
        } else if (KARMA.equals(intent.getAction())){
            sendToService.setAction(KARMA);
            Toast.makeText(context, "KARMA", Toast.LENGTH_SHORT).show();
            Log.v("Widget", "Clicked KARMA");}
    }
}

