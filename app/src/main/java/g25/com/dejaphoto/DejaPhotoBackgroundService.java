package g25.com.dejaphoto;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DejaPhotoBackgroundService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    static final String START_AUTO_SWITCH = "g25.com.dejaphoto.action.NEXT_WALLPAPER";
    private static final String ACTION_BAZ = "g25.com.dejaphoto.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "g25.com.dejaphoto.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "g25.com.dejaphoto.extra.PARAM2";

    // handles all wallpaper changes
    private WallpaperChanger wallpaperChanger;

    public DejaPhotoBackgroundService() {
        super("workerThread");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DejaPhotoBackgroundService.class);
        intent.setAction(START_AUTO_SWITCH);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DejaPhotoBackgroundService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (START_AUTO_SWITCH.equals(action)) {
                Bundle extras = intent.getExtras();
                int delaySeconds[] =extras.getIntArray("delaySeconds");
                handleActionFoo(delaySeconds[0]);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.e("ServiceLog", "Service Started");
        Toast.makeText(DejaPhotoBackgroundService.this, "Service Started", Toast.LENGTH_LONG).show();

        // creates our wallpaper handler and sets initial wallpaper
        wallpaperChanger = new WallpaperChanger(this);
        wallpaperChanger.initialGalleryAccess();

        return super.onStartCommand(intent, flags,startId);
    }

    @Override
    public void onDestroy(){

        super.onDestroy();
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(int delaySeconds) {
        synchronized (this) {
            while (true) {
                try {
                    wait(delaySeconds * 1000);
                    wallpaperChanger.next();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
