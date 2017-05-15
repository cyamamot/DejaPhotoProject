package g25.com.dejaphoto;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class DejaPhotoService extends Service {
    static final String INIT = "INITIALIZE";
    static final String NEXT = "NEXT";
    static final String PREV = "PREV";
    static final String RELEASE = "RELEASE";
    static final String KARMA = "KARMA";
    static int transitionDelay;
    AlarmManager alarmChangeWallpaper;
    AlarmManager alarmRecalculatePoints;
    PendingIntent pendingChangingWallpaperIntent;
    PendingIntent pendingCalcIntent;
    static WallpaperChanger wallpaperChanger;

    public DejaPhotoService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        initializeWallpaperChanger();

        //initial initialization
        if (intent == null || intent.getAction() == INIT) {
            getSharedPrefs();
            initializeAlarm();
            Log.e("ChangeWallpaperReceiver", "INITIALIZED");
        }

        //see what action is requested
        else if(intent.getAction() == NEXT)
        {
            wallpaperChanger.next();
            Log.e("ChangeWallpaperReceiver", "NEXT");
        }

        // go to the previous wallpaper
        else if(intent.getAction() == PREV)
        {
            wallpaperChanger.previous();
            Log.e("ChangeWallpaperReceiver", "PREV");
        }

        // release the current photo that is set as the wallpaper
        else if(intent.getAction() == RELEASE)
        {
            wallpaperChanger.release();
            Log.e("ChangeWallpaperReceiver", "RELEASE");
        }

        // give karma to the current wallpaper photo
        else if(intent.getAction() == KARMA)
        {
            wallpaperChanger.karma();
            Log.e("ChangeWallpaperReceiver", "KARMA");
        }
        else{
            wallpaperChanger.next();
            Log.e("ChangeWallpaperReceiver", "NEXT");
        }


        //wallpaperChanger.setLocation();


        //DEBUG MESSAGES
        Log.e("ServiceLog", "Service Called");

        //START_STICKY tells android to restart service if killed somehow
        return START_STICKY;
    }


    /**
     * Since wallpaperChanger field is static, this ensures it is only initialized if it is null.
     */
    private void initializeWallpaperChanger() {
        if(wallpaperChanger == null) {
            wallpaperChanger = new WallpaperChanger(this);
            wallpaperChanger.initialize();
        }
    }


    /**
     * Initializes the objects and intents necessary for this service to repeat our tasks.
     * Objects are private fields. AlarmManager is set to repeat the intent we give it by
     * transition delay times a constant (Milliseconds). Currently AlarmManager calls this
     * service back with a intent indicating the action to take.
     */
    private void initializeAlarm() {
        if(alarmChangeWallpaper != null || alarmRecalculatePoints != null){
            return;
        }
        //Change Pictures Every Interval
        Intent changeWallpaperIntent = new Intent(getApplicationContext(), DejaPhotoService.class);
        changeWallpaperIntent.setAction(NavWidget.NEXT);

        //Manages the countdown and sending the intent to the receiver once the countdown is over.
        alarmChangeWallpaper = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //Intent wrapper needed for technical reasons.
        pendingChangingWallpaperIntent = PendingIntent.getService(getApplicationContext(), 1,
                changeWallpaperIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //configures the AlarmManager object to send the intent on a repeating countdown.
        alarmChangeWallpaper.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * transitionDelay, pendingChangingWallpaperIntent);


        //Recalculate Points every Hour
        Intent calcIntent = new Intent(getApplicationContext(), DejaPhotoService.class);
        calcIntent.setAction(INIT);
        alarmRecalculatePoints = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        pendingCalcIntent = PendingIntent.getService(getApplicationContext(), 2,
                calcIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmRecalculatePoints.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
        3600000, pendingCalcIntent);

    }


    /**
     * Initializes the SharedPreferences and Editor and gets transition time.
     */
    private void getSharedPrefs() {
        SharedPreferences settings = getSharedPreferences("DejaPhotoPreferences", MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        transitionDelay = settings.getInt("transitionDelay", 5);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
