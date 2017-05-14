package g25.com.dejaphoto;

import android.Manifest.permission;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.util.Log;



import com.google.android.gms.common.api.GoogleApiClient;


public class SettingsActivity extends AppCompatActivity /*implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/{
    private boolean useCustomAlbum;
    private EditText delaySeconds;
    private TextView delayLabel;
    private int transitionDelay;
    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;

    public static final String PREFS_NAME = "DejaPhotoPreferences";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // requests required permissions like read_external storage
        requestPermissions();

        //initialize fields
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        settingsEditor = settings.edit();
        useCustomAlbum = settings.getBoolean("useCustomAlbum", false);
        transitionDelay = settings.getInt("transitionDelay", -1);
        delayLabel = (TextView)findViewById(R.id.label_transitionDelay);
        delayLabel.setText("Transition Delay: " + transitionDelay);


    }

    /**
     * Method only executes if User grants permission to use location.
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        //Launch Service
        Intent intent = new Intent(SettingsActivity.this, DejaPhotoService.class);
        intent.setAction("INITIALIZE");
        startService(intent);

        //DEBUG
        LocationWrapper testLocation = new LocationWrapper(this, 1, 1);
        Location location = testLocation.getCurrentUserLocation();
        if(location == null){
            Log.e("Location Test", "Null Location returned");
        }
        else{
            Log.e("Location Test", location.toString());
        }
    }


    /**
     * Saves all settings to sharedPreferences.
     * @param view - Current View
     */
    public void saveSettings(View view){
        //change settings
        delaySeconds = (EditText)findViewById(R.id.editText_transitionDelay);
        transitionDelay = Integer.parseInt(delaySeconds.getText().toString());
        settingsEditor.putInt("transitionDelay", transitionDelay);
        settingsEditor.commit();

        //change label
        delayLabel.setText("Transition Delay: " + transitionDelay);

        //service will restart itself when stopped
        Intent intent = new Intent(SettingsActivity.this, DejaPhotoService.class);
        stopService(intent);

        Log.e("Settings Save", "Button Clicked");
    }


    public void selectDefaultAlbum(View view){
        settingsEditor.putBoolean("useCustomAlbum", false);
        settingsEditor.commit();
    }


    public void selectCustomAlbum(View view){
        settingsEditor.putBoolean("useCustomAlbum", true);
        settingsEditor.commit();
    }


    /**
     * Creates the pop up dialogues to ask user to permission.
     */
    public void requestPermissions(){

        // request read_external_storage
        if (ContextCompat.checkSelfPermission(this,
                permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //pop up dialogue
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        // request location
        if (ContextCompat.checkSelfPermission(this,
                permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}

