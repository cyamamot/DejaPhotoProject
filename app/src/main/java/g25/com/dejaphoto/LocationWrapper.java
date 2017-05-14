package g25.com.dejaphoto;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

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
    boolean locationPermissionGiven = false;
    static final int TWO_MINUTES = 1000 * 60 * 2;

    // constructor takes in context for access to context and stuff
    // minTime is minimum time interval between location updates, in milliseconds
    // minDistance is minimum distance between location updates, in meter
    public LocationWrapper(Context context, long minTime, float minDistance) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = LocationManager.NETWORK_PROVIDER;

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                // when location changes, we set the currentUserLocation field
                // but first we have to check if newly returned location is better/more accurate than the last one
                if(isBetterLocation(location, currentUserLocation)) {
                    setCurrentUserLocation(location);

                    //DEBUG Log location
                    Log.e("Location Test", "User Location Changed");
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
            locationPermissionGiven = true;
        }
    }

    // call this getter method from our background service to get the user's current location
    public Location getCurrentUserLocation(){
        if (locationPermissionGiven){
            return currentUserLocation;
        }
        else {
            //null  to indicate no location
            return null;
        }
    }

    // setter method that sets our user's current location
    private void setCurrentUserLocation(Location location){
        currentUserLocation = location;
    }

    // if user turned off permission, then turned it on later, this let's us tell our wrapper class
    public void locationPermissionOn(){
        locationPermissionGiven = true;
    }

    // if we need to stop our app from tracking location
    public void stopLocationTracking(){
        // Remove the listener you previously added
        locationManager.removeUpdates(locationListener);
    }


    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    // android api included this for us to check which provider we trust more but we probably won't need to check for this
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
