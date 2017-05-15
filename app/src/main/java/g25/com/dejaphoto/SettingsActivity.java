package g25.com.dejaphoto;

import android.Manifest.permission;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity /*implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/{
    boolean useCustomAlbum;
    EditText delaySeconds;
    TextView delayLabel;
    int transitionDelay;
    SharedPreferences settings;
    SharedPreferences.Editor settingsEditor;

    static final String PREFS_NAME = "DejaPhotoPreferences";
    static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 2;

    // used for testing
    private Button mapsButton;
    private boolean debug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // requests required permissions like read_external storage
        if(!hasPermissions()){

            requestPermissionLocation();
        }

        //initialize fields
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        settingsEditor = settings.edit();
        useCustomAlbum = settings.getBoolean("useCustomAlbum", false);
        transitionDelay = settings.getInt("transitionDelay", 1);
        delayLabel = (TextView)findViewById(R.id.label_transitionDelay);
        delayLabel.setText("Transition Delay (Minutes): " + transitionDelay);

        // set the edit text transition delay
        EditText etTransitionDelay = (EditText)findViewById(R.id.editText_transitionDelay);
        etTransitionDelay.setText(String.valueOf(transitionDelay));
        etTransitionDelay.setSelection(String.valueOf(transitionDelay).length());

        Log.i("ActivitySettings", String.valueOf(useCustomAlbum) + ", " + String.valueOf(transitionDelay));
        if(!debug){
            mapsButton = (Button) findViewById(R.id.btn_testMap);
            mapsButton.setVisibility(View.GONE);
        }
    }

    /**
     * Method only executes if User grants permission to use location.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                requestPermissionStorage();
                break;
            }
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //Launch Service
                    launchService();

                    //DEBUG
                    LocationWrapper testLocation = new LocationWrapper(this, 1, 1);
                    Location location = testLocation.getCurrentUserLocation();
                    if (location == null) {
                        Log.e("Location Test", "Null Location returned");
                    } else {
                        Log.e("Location Test", location.toString());
                    }

                }
            }
        }

    }

    /**
     * Starts the DejaPhoto service to change the wallpaper
     */
    private void launchService() {
        Intent intent = new Intent(SettingsActivity.this, DejaPhotoService.class);
        intent.setAction(DejaPhotoService.INIT);
        startService(intent);
    }


    /**
     * Saves all settings to sharedPreferences.
     */
    public void saveSettings(View view){
        //change settings
        delaySeconds = (EditText)findViewById(R.id.editText_transitionDelay);
        transitionDelay = Integer.parseInt(delaySeconds.getText().toString());

        // check for a time greater than 0
        if(transitionDelay > 0) {
            settingsEditor.putInt("transitionDelay", transitionDelay);
            settingsEditor.commit();

            //change label
            delayLabel.setText("Transition Delay: " + transitionDelay);

            //service will restart itself when stopped
            launchService();
        Log.e("Settings Save", "Button Clicked");
        }
    }


    /**
     * Saves the user's preference to use the default album
     */
    public void selectDefaultAlbum(View view){
        settingsEditor.putBoolean("useCustomAlbum", false);
        settingsEditor.commit();

        Log.e("Album Selected", "Default Album");
    }

    /**
     * Saves the user's preference to use the custom album
     */
    public void selectCustomAlbum(View view){
        settingsEditor.putBoolean("useCustomAlbum", true);
        settingsEditor.commit();

        Log.e("Album Selected", "Custom Album");
    }


    /**
     * Creates the pop up dialogues to ask user to permission.
     */
    public boolean requestPermissionLocation(){
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
        return true;
    }
    public void requestPermissionStorage(){
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

    }

    /**
     * Returns whether or not the user has granted permissions
     */
    public boolean hasPermissions(){
        //If we have both permissions, we return true
        if (ContextCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    /**
     * Used for testing LocationWrapper and is the onClick to the MapsActivity
     */
    public void testMap(View view){
        if(debug) {
            Intent intent = new Intent(this, TestMapsActivity.class);
            startActivity(intent);
        }
    }
}

