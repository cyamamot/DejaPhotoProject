package g25.com.dejaphoto;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Tim on 5/6/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    static WallpaperChanger wallpaperChanger;
    @Override
    public void onReceive(Context context, Intent intent){
        if(wallpaperChanger == null){
            wallpaperChanger = new WallpaperChanger(context);
            wallpaperChanger.initialize();

            Log.e("ALARM_RECEIVER_INIT", "INITIALIZED");
        }
        else
            wallpaperChanger.next();
        Log.e("ALARMRECEIVER", "RECEIVED");
    }
}
