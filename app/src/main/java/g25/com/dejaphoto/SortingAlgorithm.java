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
    private static final int MIN_TIME =  1000; //update every second (uses milliseconds)
    private static final int POINT_INC = 5; //point value that photos are incremented by
    private static final int KARMA_INC = 1; //point value karma increments by
    private static final int MIN_IN_ONE_HOUR = 60; //number of minutes in one hour
    private static final int MIN_IN_TWO_HOUR = 120; //number of minutes in two hours
    private static final int RELEASED_POINT = -1; //point value for a released photo
    private static final double WITHIN_DISTANCE = 1000; //1 mile expressed in meters


    Context context;
    static LocationWrapper loc;
    Location currentL;
    Date currentDate;

    /**
     * Default Constructor
     */
    public SortingAlgorithm(){}

    /**
     * Constructor for the Sorting Algorithm
     */
    public SortingAlgorithm (Context context){
        this.context = context;
        setLocationWrapper(new LocationWrapper(context, MIN_TIME, MIN_DISTANCE));
        setCurrentDate(new Date());
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
            Log.e("SortingAlgorithm", "Distance: " + distance);
            if (abs(distance) <= WITHIN_DISTANCE) {
                points += POINT_INC;
                Log.e("SortingAlg", "Within 1 mile");
            }else{
                Log.e("SortingAlg", "Not Within 1 mile");
            }
        }
        else if(currentL == null){
            Log.e("SortingAlg", "Current Location is Null");
        }
        else if(location == null){
            Log.e("SortingAlg", "Pic Location is Null");
        }

        if (currentDate == null){
            Log.e("DEBUG", "currentDate is null");
        }

        if (date == null){
            Log.e("DEBUG", "date is null");
        }

        if(currentDate != null && date != null) {
            SimpleDateFormat week = new SimpleDateFormat("E");
            if (week.format(currentDate).equals(week.format(date))) {
                Log.e("SortingAlg", "Same Day of Week");
                SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
                int currentTime = toMins(hour.format(currentDate));
                int photoTime = toMins(hour.format(date));
                long difference = photoTime - currentTime;
                if (abs(difference) <= MIN_IN_TWO_HOUR) {
                    Log.e("SortingAlg", "Within 2 Hours");
                    points += POINT_INC;
                }else{
                    Log.e("SortingAlg", "Not Within 2 Hours");
                }
            }else{
                Log.e("SortingAlg", "Not Same Day of Week");
            }
        }

        if (karma){
            Log.d("SortingAlg", "Adding karma");
            points += (KARMA_INC * photo.karmaCount);
        }

        photo.setPoints(points);
        Log.e("SortingAlg", ((Integer)photo.getPoints()).toString());

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

    /**
     * Sets current Date, allows for testing purposes
     */
    public void setCurrentDate(Date date){
        this.currentDate = date;
    }


    /**
     * Sets location wrapper, allows for testing purposes
     */
    public void setLocationWrapper(LocationWrapper lw){
        this.loc = lw;
    }
}

