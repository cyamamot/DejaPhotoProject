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
    static int transitionDelay;
    private AlarmManager alarmMgr;
    private PendingIntent pendingIntent;
    private static WallpaperChanger wallpaperChanger;

    public DejaPhotoService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        //initial initialization
        if (intent.getAction() == "INITIALIZE") {
            getSharedPrefs();
            initializeAlarm();
            Log.e("ChangeWallpaperReceiver", "INITIALIZED");
        }

        //make sure wallpaper changer is not null
        initializeWallpaperChanger();

        //see what action is requested
        if(intent.getAction() == "NEXT")
        {
            wallpaperChanger.next();
        }
        else if(intent.getAction() == "PREV")
        {
            wallpaperChanger.previous();
        }
        else if(intent.getAction() == "RELEASE")
        {
            wallpaperChanger.release();
        }
        else if(intent.getAction() == "KARMA")
        {
            wallpaperChanger.karma();
        }
        else{
            wallpaperChanger.next();
        }

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
        //Intent that holds the class that will receive broadcasts and perform wallpaper change.
        Intent serviceIntent = new Intent(getApplicationContext(), DejaPhotoService.class);

        //Manages the countdown and sending the intent to the receiver once the countdown is over.
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //Intent wrapper needed for technical reasons.
        pendingIntent = PendingIntent.getService(getApplicationContext(), 1, serviceIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        //configures the AlarmManager object to send the intent on a repeating countdown.
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * transitionDelay, pendingIntent);
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
