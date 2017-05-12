package g25.com.dejaphoto;

import android.location.Location;
import android.app.Activity;
import android.util.Log;



import java.util.Date;


/**
 * Created by angelazhang on 5/8/17.
 */

public class SortingAlgorithm {

    public SortingAlgorithm(){}
    Activity act;
    public SortingAlgorithm (Activity activity){
        act = activity;
    }
        //if location is 1000ft from current location & location boolean is true
        //if time is 2hrs from current time & time boolean is true
        //if photo has karma
        //if released, set point value to -1
        int addPoints (Location location, Date date, boolean karma, boolean released){
            int points = 0;

            if(released){
                return -1;
            }
            if (act == null){
                Log.e("fuck", "fml");
            }
            LocationWrapper loc = new LocationWrapper(act, 1, 1);
            Location currentL = loc.getCurrentUserLocation();
            float distance = location.distanceTo(currentL); //distance in meters
            if (distance <= 304.8){
                points += 5;
            }

            Date currentDate = new Date();
            long difference = date.getTime() - currentDate.getTime();
            if (difference <= 7200000){
                points += 5;
            }

            if (karma){
                points += 5;
            }

            return points;
        }
}
