package g25.com.dejaphoto;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.abs;


/**
 * Created by angelazhang on 5/8/17.
 */

public class SortingAlgorithm {


    private static final int MIN_DISTANCE = 152; //update every 500 feet (uses meters)
    private static final int MIN_TIME = 1 * 60 * 60 * 1000; //update every hour (uses milliseconds)

    Context context;
    LocationWrapper loc;

    /**
     * Constructor for the Sorting Algorithm
     */
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
                photo.setPoints(-1);
                return -1;
            }

            Location currentL = loc.getCurrentUserLocation();
            if(currentL != null && location != null) {
                float distance = location.distanceTo(currentL); //distance in meters
                if (abs(distance) <= 304.8) {
                    points += 5;
                }
            }


            Date currentDate = new Date();
            if(currentDate != null && date != null) {
                SimpleDateFormat week = new SimpleDateFormat("E");
                if (week.format(currentDate).equals(week.format(date))) {
                    Log.d("SortingAlg", "Same Day of Week");
                    SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
                    int currentTime = toMins(hour.format(currentDate));
                    int photoTime = toMins(hour.format(date));
                    long difference = photoTime - currentTime;
                    if (abs(difference) <= 120) {
                        points += 5;
                    }
                }
            }

            if (karma){
                points += 5;
            }

            photo.setPoints(points);
            Log.d("SortingAlg", ((Integer)photo.getPoints()).toString());

            return points;
        }

    private static int toMins(String s) {
        String[] hourMin = s.split(":");
        int hour = Integer.parseInt(hourMin[0]);
        int mins = Integer.parseInt(hourMin[1]);
        int hoursInMins = hour * 60;
        return hoursInMins + mins;
    }
}
