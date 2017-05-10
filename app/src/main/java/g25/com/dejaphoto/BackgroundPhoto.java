package g25.com.dejaphoto;


import java.io.IOException;
import java.util.Date;

import android.location.Location;
import android.provider.MediaStore;
import android.util.Log;
import android.media.ExifInterface;
import android.net.Uri;
import android.graphics.Bitmap;

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
    Date date;
    Location location;
    boolean karma;
    boolean released;
    boolean noLocation;

    public BackgroundPhoto(Uri uriInput){
        setUri(uriInput);
        setExifData();
        parseLocationFromExif();
        setKarma(false);
        setReleased(false);
    }


    private void setUri(Uri input){
        this.uri = input;
    }

    public Uri getUri(){
        return this.uri;
    }

    public Uri getParsedUri(){
       return this.uri.parse("file://");
    }

    private void setExifData(){
        try {
            this.exifData = new ExifInterface(uri.getPath());
        }
        catch (IOException e){
            e.printStackTrace();
            Log.e("Path from Exif", "FAILED");
            this.exifData = null;
            this.noLocation = true;
        }

    }

    /**
     * Converts lat and lng from degrees and seconds into a double that can
     * be used to make a location.
     */
    private void parseLocationFromExif(){

        if (exifData == null){
            this.location = null;
            return;
        }

        //parse lat
        String lat = exifData.TAG_GPS_LATITUDE;
        String latDirection = exifData.TAG_GPS_LATITUDE_REF;
        double latitude = formatLatLng(lat, latDirection);


        //parse lng
        String lng = exifData.TAG_GPS_LONGITUDE;
        String lngDirection = exifData.TAG_GPS_LONGITUDE_REF;
        double longitude = formatLatLng(lng, lngDirection);

        //set location field
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        this.location = location;

    }

    public void setKarma(boolean input){
        this.karma = input;
    }

    public void setReleased(boolean input){
        this.released = input;
    }


    public Location getLocation(){
        return this.location;
    }

    /**
     * Converts the GPS lat or lng from string format to a double (minutes)
     * method taken from:
     *  http://android-er.blogspot.in/2010/01/convert-exif-gps-info-to-degree-format.html
     * @param coordinate - String format given by ExifInterface
     * @param direction - Direction given by ExifInterface
     * @return - Converted Double usable by Location class.
     */
    private double formatLatLng(String coordinate, String direction){
        double converted;
        String[] DegMinSec = coordinate.split(",", 3);

        String[] deg = DegMinSec[0].split("/", 2);
        Double deg0 = new Double(deg[0]);
        Double deg1 = new Double(deg[1]);
        Double degFinal = deg0 / deg1;

        String[] min = DegMinSec[1].split("/", 2);
        Double min0 = new Double(min[0]);
        Double min1 = new Double(min[1]);
        Double minFinal = min0 / min1;

        String[] sec = DegMinSec[2].split("/", 2);
        Double sec0 = new Double(sec[0]);
        Double sec1 = new Double(sec[1]);
        Double secFinal = sec0 / sec1;

        converted = new Double(degFinal + (minFinal / 60) + (secFinal/3600));

        if(direction.equals("S") || direction.equals("W")){
            converted = 0 - converted;
        }

        return converted;

    }

}

