package g25.com.dejaphoto;

import android.location.Location;
import android.content.Context;
import android.util.Log;



import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;


/**
 * Created by angelazhang on 5/8/17.
 */

public class SortingAlgorithm {

    Context act;
    Location location;
    Date date;
    boolean karma;
    boolean released;
    public SortingAlgorithm (){}

        //if location is 1000ft from current location & location boolean is true
        //if time is 2hrs from current time & time boolean is true
        //if photo has karma
        //if released, set point value to -1
        int addPoints (BackgroundPhoto photo){

            //act = activity;
            location = photo.getLocation();
            date = photo.getDate();
            karma = photo.hasKarma();
            released = photo.isReleased();

            int points = 0;

            if(released){
                return -1;
            }

            /*LocationWrapper loc = new LocationWrapper(act, 1, 1);
            try {
                TimeUnit.SECONDS.sleep(10);
            }catch(Exception e){
                Log.e("TimeUnit", "exception");
            }

            Location currentL = loc.getCurrentUserLocation();
            if (currentL == null){
                Log.d("fuck", "why");
            }

            float distance = 1000;
            try {
                distance = location.distanceTo(currentL); //distance in meters
            }catch(Exception e){
                Log.e("SortingAlg", "No Location");
            }
            if (distance <= 304.8){
                points += 5;
            }*/

            Date currentDate = new Date();
            SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
            if (simpleDateformat.format(currentDate).equals(simpleDateformat.format(date))) {
                Log.d("SortingAlg", "Same Day of Week");
                long difference = date.getTime() - currentDate.getTime();
                if (difference <= 7200000) {

                    points += 5;
                }

            }

            if (karma){
                points += 5;
            }

            String p = ((Integer)points).toString();
            Log.d("SortingAlg", p);
            return points;
        }
}
