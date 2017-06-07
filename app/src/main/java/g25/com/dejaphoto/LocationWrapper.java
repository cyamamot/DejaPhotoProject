package g25.com.dejaphoto;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static g25.com.dejaphoto.DejaPhotoService.wallpaperChanger;

/**
 * Created by dillonliu on 5/9/17.
 */

public class LocationWrapper {

    // Acquire a reference to the system Location Manager
    LocationManager locationManager;
    // this field will store the last location that locationManager returned
    Location currentUserLocation;
    // our location listener that our location manager uses
    LocationListener locationListener;
    String locationProvider;
    //whether user has location permission turned on; if not this class can't do a whole lot
    static final int TWO_MINUTES = 1000 * 60 * 2;
    Context context;

    // used for testing on map
    GoogleMap mMap;

    /**
     * Constructor takes in context for access to context, time, and distance
     * minTime is minimum time interval between location updates, in milliseconds
     * minDistance is minimum distance between location updates, in meter
     */
    public LocationWrapper(Context context, long minTime, float minDistance) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = LocationManager.GPS_PROVIDER;
        this.context = context;

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                // when location changes, we set the currentUserLocation field
                // but first we have to check if newly returned location is better/more accurate than the last one
                //sendResetIntent();

                setCurrentUserLocation(location);
                if(wallpaperChanger != null) {
                    wallpaperChanger.initialize();
                }
                else {
                    initWallpaperChanger();
                }
                Log.e("Location Test", "User Location Changed: " + location.toString());

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        //get Initial User position
        currentUserLocation = locationManager.getLastKnownLocation(locationProvider);
        Log.e("LocationWrapper", "Getting Last Known Location");
        if(currentUserLocation == null){
            Log.e("LocationWrapper", "Location is Null");
        }

        //check if user granted us location permission, if yes locationManager to get updates
        if (ContextCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Register the listener with the Location Manager,
            // receive location updates with passed in minTime and minDistance
            locationManager.requestLocationUpdates(locationProvider, minTime, minDistance,
                    locationListener);
        }
    }

    private void initWallpaperChanger() {
        wallpaperChanger = new WallpaperChanger(context);
        wallpaperChanger.initialize();
    }

    /**
     * Getter method for our background service to get the user's current location
     */
    public Location getCurrentUserLocation(){
            Log.e("LocationWrapper", "Returning User Location");
            return currentUserLocation;
    }

    /**
     * Setter method that sets our user's current location
     */
    private void setCurrentUserLocation(Location location){
        currentUserLocation = location;
    }


    /**
     * Test constructor that we will pass into MapsActivity to display markers on map
     * minTime is minimum time interval between location updates, in milliseconds
     * minDistance is minimum distance between location updates, in meter
     */
    public LocationWrapper(Context context, long minTime, float minDistance, GoogleMap gMap) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = LocationManager.GPS_PROVIDER;
        mMap = gMap;

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                // when location changes, we set the currentUserLocation field
                // but first we have to check if newly returned location is better/more accurate than the last one
                if(true) {
                    setCurrentUserLocation(location);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("updated_path"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

                    //DEBUG Log location
                    Log.e("Location Test", "User Location Changed: " + location.toString());
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        //get Initial User position
        currentUserLocation = locationManager.getLastKnownLocation(locationProvider);

        //check if user granted us location permission, if yes locationManager to get updates
        if (ContextCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Register the listener with the Location Manager,
            // receive location updates with passed in minTime and minDistance
            locationManager.requestLocationUpdates(locationProvider, minTime, minDistance,
                    locationListener);
        }
    }
}
