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
    private static final int POINT_INC = 5; //point value that photos are incremented by
    private static final int MIN_IN_ONE_HOUR = 60; //number of minutes in one hour
    private static final int MIN_IN_TWO_HOUR = 120; //number of minutes in two hours
    private static final int RELEASED_POINT = -1; //point value for a released photo
    private static final double WITHIN_DISTANCE = 304.8; //1000 feet expressed in meters


    Context context;
    LocationWrapper loc;
    Location currentL;

    /**
     * Default Constructor
     */
    public SortingAlgorithm(){}

    /**
     * Constructor for the Sorting Algorithm
     */
    public SortingAlgorithm (Context context){
        this.context = context;
        loc = new LocationWrapper(context, MIN_TIME, MIN_DISTANCE);
    }

    /**
     * Used to rank the photos according to location/time/karma/released
     * Check if location is 1000ft from current location & location boolean is true
     * Check if time is 2hrs from current time & time boolean is true
     * Check if photo has karma
     * Check if released, set point value to -1
     */
    public int assignPoints(BackgroundPhoto photo){

        //get info from photo
        Date date = photo.getDate();
        Location location = photo.getLocation();
        boolean released = photo.isReleased();
        boolean karma = photo.hasKarma();

        int points = 0;

        if(released){
            photo.setPoints(RELEASED_POINT);
            return RELEASED_POINT;
        }

        if (loc != null) {
            currentL = loc.getCurrentUserLocation();
        }
        if(currentL != null && location != null) {
            float distance = location.distanceTo(currentL); //distance in meters
            if (abs(distance) <= WITHIN_DISTANCE) {
                points += POINT_INC;
                Log.d("SortingAlg", "Within 1000 Feet");
            }else{
                Log.d("SortingAlg", "Not Within 1000 Feet");
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
                if (abs(difference) <= MIN_IN_TWO_HOUR) {
                    Log.d("SortingAlg", "Within 2 Hours");
                    points += POINT_INC;
                }else{
                    Log.d("SortingAlg", "Not Within 2 Hours");
                }
            }
        }

        if (karma){
            points += POINT_INC;
        }

        photo.setPoints(points);
        Log.d("SortingAlg", ((Integer)photo.getPoints()).toString());

        return points;
    }

    /**
     * Used to calculate the number of minutes in the string
     * Returns the number of minutes
     */
    public static int toMins(String s) {
        String[] hourMin = s.split(":");
        int hour = Integer.parseInt(hourMin[0]);
        int mins = Integer.parseInt(hourMin[1]);
        int hoursInMins = hour * MIN_IN_ONE_HOUR;
        return hoursInMins + mins;
    }
}
