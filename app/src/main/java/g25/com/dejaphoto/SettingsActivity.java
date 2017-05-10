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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;


public class SettingsActivity extends AppCompatActivity /*implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/{
    public static final String PREFS_NAME = "DejaPhotoPreferences";
    private boolean useCustomAlbum;
    private EditText delaySeconds;
    private TextView delayLabel;
    private int transitionDelay;
    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;
    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    //private Intent intent;
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
        transitionDelay = settings.getInt("transitionDelay", 5);
        delayLabel = (TextView)findViewById(R.id.label_transitionDelay);
        delayLabel.setText("Transition Delay: " + transitionDelay);


        //create intent with extras
        Intent intent = new Intent(SettingsActivity.this, DejaPhotoService.class);
        startService(intent);

        //buildGoogleApiClient();
    }

    /*protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }*/

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    /*@Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.format("%s: %f", mLatitudeLabel,
                    mLastLocation.getLatitude()));
            mLongitudeText.setText(String.format("%s: %f", mLongitudeLabel,
                    mLastLocation.getLongitude()));
        } else {
            Toast.makeText(this, "f this", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        //Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        //Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }*/

    public void setDelay(View view){

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

    }
    public void selectDefaultAlbum(View view){
        settingsEditor.putBoolean("useCustomAlbum", false);
        settingsEditor.commit();
    }

    public void selectCustomAlbum(View view){
        settingsEditor.putBoolean("useCustomAlbum", true);
        settingsEditor.commit();
    }

    // we call this to ask user for all permissions
    public void requestPermissions(){
        // request read_external_storage
        if (ContextCompat.checkSelfPermission(this,
                permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
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

