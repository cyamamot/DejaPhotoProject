package g25.com.dejaphoto;

import android.content.Context;
import android.location.Location;
import android.util.Log;


import java.util.Comparator;
import java.util.Date;


/**
 * Created by angelazhang on 5/8/17.
 */

public class SortingAlgorithm {

    private static final int MIN_DISTANCE = 152; //update every 500 feet (uses meters)
    private static final int MIN_TIME = 1 * 60 * 60 * 1000; //update every hour (uses milliseconds)

    public SortingAlgorithm(){}
    Context context;
    LocationWrapper loc;

    public SortingAlgorithm (Context context){
        this.context = context;
        loc = new LocationWrapper(context, MIN_TIME, MIN_DISTANCE);
    }
        //if location is 1000ft from current location & location boolean is true
        //if time is 2hrs from current time & time boolean is true
        //if photo has karma
        //if released, set point value to -1
        int assignPoints(BackgroundPhoto photo){

            //get info from photo
            Date date = photo.getDate();
            Location location = photo.getLocation();
            boolean released = photo.isReleased();
            boolean karma = photo.hasKarma();

            int points = 0;

            if(released){
                return -1;
            }

            if (context == null){
                Log.e("fuck", "fml");
            }

            Location currentL = loc.getCurrentUserLocation();
            if(currentL != null && location != null) {
                float distance = location.distanceTo(currentL); //distance in meters
                if (distance <= 304.8) {
                    points += 5;
                }
            }

            Date currentDate = new Date();
            if(currentDate != null && date != null) {
                long difference = date.getTime() - currentDate.getTime();
                if (difference <= 7200000) {
                    points += 5;
                }
            }

            if (karma){
                points += 5;
            }

            photo.setPoints(points);

            return points;
        }
}
