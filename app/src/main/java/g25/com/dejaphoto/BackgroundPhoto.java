package g25.com.dejaphoto;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import android.location.Location;
import android.util.Log;
import android.media.ExifInterface;
import android.net.Uri;

/**
 * Created by angelazhang on 5/8/17.
 */

//GPS calculation ideas referenced from http://stackoverflow.com/questions/9868158/get-gps-location-of-a-photo
   // and http://android-er.blogspot.in/2010/01/convert-exif-gps-info-to-degree-format.html

    //ANGELA IF YOU'RE READING THIS SORRY WE HAD TO MODIFY UR CODE TO MAKE IT WORK WITH THE WALLPAPER CHANGER
    //ALL THE CURSOR STUFF IN HANDLED IN WALLPAPER CHANGER SO WE REMOVED IT

public class BackgroundPhoto {

    ExifInterface exifData;
    Uri uri;
    GregorianCalendar dateCalendar;
    Location location;
    boolean karma;
    boolean released;
    boolean hasLocation;
    boolean hasDate;


    public BackgroundPhoto(Uri uriInput){
        setUri(uriInput);
        setExifData();
        parseLocationFromExif();
        parseDateFromExif();
    }


    public BackgroundPhoto(String path){
        Uri uriInput = Uri.parse("file://" + path);
        setUri(uriInput);
        setExifData();
        parseLocationFromExif();
        parseDateFromExif();
    }


    /**
     * Converts lat and lng from degrees and seconds into a double that can
     * be used to make a location.
     */
    private void parseLocationFromExif(){
        if (exifData == null){
            this.hasLocation = false;
            this.hasDate = false;
            return;
        }
        //get lat
        String lat = exifData.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String latDirection = exifData.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        //get lng
        String lng = exifData.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String lngDirection = exifData.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

        //parse by calling helper method
        double latitude, longitude;
        try{
            latitude = formatLatLng(lat, latDirection);
            longitude = formatLatLng(lng, lngDirection);
        }//coordinates were null, indicate no geotag for photo
        catch(NullPointerException e){
            e.printStackTrace();
            this.hasLocation = false;
            return;
        }

        //set location field
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        setLocation(location);
        this.hasLocation = true;
    }


    private void parseDateFromExif(){
        if(exifData == null){
            this.hasDate = false;
            return;
        }

        //get the dateCalendar
        String dateTimeStr = exifData.getAttribute(ExifInterface.TAG_DATETIME);

        //null check
        if(dateTimeStr == null) {
            this.hasDate = false;
            return;
        }
        else{
            this.hasDate = true;
            Log.e("EXIF DATE", "EXIF INDICATED DATE");
        }

        //parse string into ints
        String[] dateTime = dateTimeStr.split(" ", 2);
        String[] yearMonthDay = dateTime[0].split(":", 3);
        String[] hourMinSec = dateTime[1].split(":", 3);
        int year = Integer.valueOf(yearMonthDay[0]);
        int month = Integer.valueOf(yearMonthDay[1]);
        int date = Integer.valueOf(yearMonthDay[2]);
        int hourOfDay = Integer.valueOf(hourMinSec[0]);
        int minute = Integer.valueOf(hourMinSec[1]);
        int second = Integer.valueOf(hourMinSec[2]);

        //set Calendar object;
        this.dateCalendar = new GregorianCalendar();
        this.dateCalendar.set(year, month, date, hourOfDay, minute, second);

    }


    /**
     * Converts the GPS lat or lng from string format to a double (minutes), calls on helper
     * method formatLatLng() to format the string output (ex. 51/8 43/3 33/1) into total number of
     * degrees.
     * method taken from:
     *  http://android-er.blogspot.in/2010/01/convert-exif-gps-info-to-degree-format.html
     * @param coordinate - String format given by ExifInterface
     * @param direction - Direction given by ExifInterface
     * @return - Converted Double usable by Location class.
     */
    private double formatLatLng(String coordinate, String direction){
        //redundant null checks since android not specific about what happens during failure
        if(coordinate == null || direction == null){
            throw new NullPointerException("Coordinates were NULL");
        }

        double converted;
        //get degrees minutes and seconds into their own strings
        String[] DegMinSec = coordinate.split(",", 3);

        //parse degree fraction from fraction string to double
        String[] deg = DegMinSec[0].split("/", 2);
        Double deg0 = Double.valueOf(deg[0]);
        Double deg1 = Double.valueOf(deg[1]);
        Double degFinal = deg0 / deg1;

        //parse minute fraction from fraction string to double
        String[] min = DegMinSec[1].split("/", 2);
        Double min0 = Double.valueOf(min[0]);
        Double min1 = Double.valueOf(min[1]);
        Double minFinal = min0 / min1;

        //parse second fraction from fraction string to double
        String[] sec = DegMinSec[2].split("/", 2);
        Double sec0 = Double.valueOf(sec[0]);
        Double sec1 = Double.valueOf(sec[1]);
        Double secFinal = sec0 / sec1;

        //convert everything to degrees
        converted = (degFinal + minFinal/60 + secFinal/3600);

        //invert based on direction
        if(direction.equals("S") || direction.equals("W")){
            converted = 0 - converted;
        }
        return converted;
    }


    private void setUri(Uri input){
        this.uri = input;
    }


    private void setExifData(){
        try {
            this.exifData = new ExifInterface(uri.getPath());
        }
        catch (IOException e){
            e.printStackTrace();
            Log.e("Path from Exif", "FAILED");
            this.exifData = null;
            this.hasLocation = false;
            this.hasDate = false;
        }
    }


    private void setLocation(Location loc){
        this.location = loc;
    }


    public void giveKarma(){
        this.karma = true;
    }


    public void release(){
        this.released = true;
    }


    public Location getLocation(){
        if(!hasLocation){
            return null;
        }
        return this.location;
    }

    public boolean hasLocation(){
        return this.hasLocation;
    }

    public String getPath(){
        return this.uri.getPath();
    }

    public Uri getUri(){
       return this.uri;
    }

    public Date getDate(){
        if(!hasDate){
            return null;
        }
        return this.dateCalendar.getTime();
    }

    public boolean hasDate(){
        return this.hasDate;
    }
    public boolean hasKarma(){
        return this.karma;
    }

    public boolean isReleased(){
        return this.released;
    }

}

