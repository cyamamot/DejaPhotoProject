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
    //private static WallpaperChanger wallpaperChanger;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    public DejaPhotoService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //initialize fields

        Log.e("onStartCommand Executed", "!!!!!!!!!!!!!!!!!!");

        //get transitionDelay from SharedPrefs
        SharedPreferences settings = getSharedPreferences("DejaPhotoPreferences", MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        transitionDelay = settings.getInt("transitionDelay", 5);


        //TODO REMOVE DEBUG MESSAGES
        Log.e("ServiceLog", "Service Started");
        Toast.makeText(DejaPhotoService.this, "Service Started", Toast.LENGTH_LONG).show();

        Intent intentReceiver = new Intent(getApplicationContext(), AlarmReceiver.class);
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intentReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * transitionDelay, alarmIntent);

       // return START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Intent restartService = new Intent("RestartService");
        sendBroadcast(restartService);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
