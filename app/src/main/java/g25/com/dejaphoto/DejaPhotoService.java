package g25.com.dejaphoto;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DejaPhotoService extends Service {
    static int transitionDelay;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    public DejaPhotoService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        getSharedPrefs();
        initializeAlarm();

        //DEBUG MESSAGES
        Log.e("ServiceLog", "Service Started");
        Toast.makeText(DejaPhotoService.this, "Service Started", Toast.LENGTH_LONG).show();
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    /**
     * Initializes the objects and intents necessary for ChangeWallpaperReceiver to repeat our tasks.
     * Objects are private fields.
     */
    private void initializeAlarm() {
        //Intent that holds the class that will receive broadcasts and perform wallpaper change.
        Intent intentReceiver = new Intent(getApplicationContext(), ChangeWallpaperReceiver.class);

        //Manages the countdown and sending the intent to the receiver once the countdown is over.
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //Intent wrapper needed for technical reasons.
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intentReceiver,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //configures the AlarmManager object to send the intent on a repeating countdown.
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * transitionDelay, alarmIntent);
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

        //Send broadcast to listener to restart the service when app closes
        Intent restartService = new Intent("RestartServiceReceiver");
        sendBroadcast(restartService);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
