package g25.com.dejaphoto;

import android.location.Location;

import java.util.Date;

/**
 * Created by angelazhang on 5/8/17.
 */

public class SortingAlgorithm {

    Location userLocation;
    public SortingAlgorithm (){

    }
        Location current;
        //if location is 1000ft from current location & location boolean is true
        //if time is 2hrs from current time & time boolean is true
        //if photo has karma
        //if released, set point value to 0
        int sort(Location location, Date date, boolean karma, boolean released){
            int points = 0;
            //LocationWrapper loc = new LocationWrapper()
           if(released){
               return -1;
           }

           return points;
        }
}
