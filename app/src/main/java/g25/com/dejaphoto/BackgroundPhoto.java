package g25.com.dejaphoto;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ArrayList;

/**
 * Wrapper class for Photos that encapsulate Location, Date/Time, Karma, Release Status. Also
 * has methods that check for existence of Location and Date and return boolean indicating existence
 * of these optional EXIF data.
 *
 * ***USAGE***
 *
 * Constructor: Call constructor, pass in path to file. Class will handle getting all relevant info.
 *
 * Set to Wallpaper: getUri() return the Uri of the photo, which can be used by WallpaperManager
 *  to set the Wallpaper.
 *
 * Get Location Data: getLocation() returns a Location object or NULL. Location has methods to
 *  compare to other locations and get the distance.
 *  Use hasLocation() to determine if Location exists instead of making another null check.
 *
 * Get Timestamp: getDate() returns a Date object or NULL. Date has methods to compare to other
 *  Dates.
 *  Use hasDate() to determine if  Date exists instead of null checking.
 *
 * Get/Set Karma: hasKarma() will indicate with boolean if picture has karma.
 *  giveKarma() will give the photo karma.
 *
 * Get/Set Released: isReleased() will indicate with boolean if picture is released.
 *  release() will release the picture, setting the boolean to true, CANNOT BE UNDONE.
 *
 * Points/Sorting Algorithm: handled by SortingAlgorithm field, uses the class's sort() method,
 *  putting in here allows us to determine points when photo are initialized, thereby not needing
 *  a second pass through the array. Variable is declared static since only one is needed for all
 *  photos. Any modification to the algorithm can be down in it's own class.
 *
 *  Created by tim on 5/8/2017
 */

//GPS calculation ideas referenced from http://stackoverflow.com/questions/9868158/get-gps-location-of-a-photo
// and http://android-er.blogspot.in/2010/01/convert-exif-gps-info-to-degree-format.html



public class BackgroundPhoto {

    ExifInterface exifData;
    Uri uri;
    GregorianCalendar dateCalendar;
    Location location;
    boolean karma;
    boolean released;
    boolean hasLocation;
    boolean hasDate;
    boolean hasEXIF;
    String name;
    int points = 0;
    int numberofKarmas = 0;
    ArrayList<String> listOfKarmaers;
    Context context;
    static final String KARMA_INDICATOR = "DJP_KARMA";
    static final String RELEASED_INDICATOR = "DJP_RELEASED";
    static SharedPreferences settings;
    static SharedPreferences.Editor settingsEditor;
    int karmaCount;
    String customLocation;

    /**
     * Constructor for the photo and passes in the path to it and the context
     */
    public BackgroundPhoto(String path, Context context){
        if (path == null && context == null) return;
        setContext(context);
        Uri uriInput = Uri.parse("file://" + path);
        setUri(uriInput);
        setExifData();
        parseLocationFromExif();
        parseDateFromExif();
        initializeSettings();
        parseKarmaAndReleased();
        name = uri.getLastPathSegment();
        karmaCount = 0;
        customLocation = "default";
    }

    // constructor for creating arraylist of photo metadata
    public BackgroundPhoto(String name, int karma, String customLocation){
        this.name = name;
        karmaCount = karma;
        this.customLocation = customLocation;
    }

    /**
     * Converts lat and lng from a string indicating degrees and seconds into a double that
     * that can be used to make location object.
     */
    private void parseLocationFromExif() {
        if (exifData == null) {
            this.hasLocation = false;
            this.hasDate = false;
            this.hasEXIF = false;
            return;
        }

        this.hasEXIF = true;

        //get lat
        String lat = exifData.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String latDirection = exifData.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        //get lng
        String lng = exifData.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String lngDirection = exifData.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

        //parse by calling helper method
        double latitude, longitude;
        try {
            latitude = formatLatLng(lat, latDirection);
            longitude = formatLatLng(lng, lngDirection);
        }//coordinates were null, indicate no geotag for photo
        catch (NullPointerException e) {
            e.printStackTrace();
            this.hasLocation = false;
            return;
        }

        //Debug Log
        Log.i("EXIF Coords", lat + latDirection + " " + lng + lngDirection);
        Log.i("EXIF Coords Parsed", Double.toString(latitude) + Double.toString(longitude));

        //set location field
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        setLocation(location);
        this.hasLocation = true;
    }


    /**
     * Parses the string output of exif date into a Calendar object that can return a Date
     * object.
     */
    void parseDateFromExif(){
        if(exifData == null){
            this.hasDate = false;
            return;
        }

        //get the dateCalendar
        String dateTimeStr = exifData.getAttribute(ExifInterface.TAG_DATETIME);

        //null check
        if(dateTimeStr == null) {
            this.hasDate = false;
            Log.i("EXIF DATE", "NO DATE EXISTS");
            return;
        }
        else{
            this.hasDate = true;
            Log.i("EXIF DATE", dateTimeStr);
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

        Log.i("EXIF DATE Parsed", Integer.toString(year) + " " + Integer.toString(month) + " "
                + Integer.toString(date));
        Log.i("EXIF DATE Parsed", Integer.toString(hourOfDay) + " " + Integer.toString(minute) +
                " " + Integer.toString(second));

    }


    /**
     * Converts the GPS lat or lng from string format to a double (minutes), calls on helper
     * method formatLatLng() to format the string output (ex. 51/8 43/3 33/1) into total number of
     * degrees.
     *
     * method taken from:
     * http://android-er.blogspot.in/2010/01/convert-exif-gps-info-to-degree-format.html
     *
     * coordinate is the String format given by ExifInterface
     * direction is the Direction given by ExifInterface
     * Returns a converted Double usable by Location class.
     */
    public double formatLatLng(String coordinate, String direction){
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

    /**
     * Checks if the photo has been given karma/has been released
     */
    private void parseKarmaAndReleased(){
        initializeSettings();
        String karmaStr = uri.toString() + KARMA_INDICATOR;
        String releaseStr = uri.toString() + RELEASED_INDICATOR;


        //karma
        if(settings.getBoolean(karmaStr, false)){
            Log.i("Parse Karma", "Karma Detected");
            giveKarma();
        }

        //release
        if(settings.getBoolean(releaseStr, false)){
            Log.i("Parse Released", "Release Detected");
            release();
        }

    }


    /**
     * Attempts to get ExifData object from photo and set the corresponding field, marks
     * boolean indicating success accordingly.
     */
    private void setExifData(){
        try {
            this.exifData = new ExifInterface(uri.getPath());
            Log.i("EXIF from Path", "SUCCESS");
        }
        catch (IOException e){
            e.printStackTrace();
            Log.i("EXIF from Path", "FAILED");
            this.exifData = null;
            this.hasLocation = false;
            this.hasDate = false;
            this.hasEXIF = false;
        }
    }

    /**
     * Initialize the settings for the photo
     */
    private void initializeSettings(){
        if(this.settings != null){
            return;
        }
        if (context == null) { return; }
        settings = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        settingsEditor = settings.edit();
    }


    /**
     * Gives karma to the photo if it doesn't have karma already
     */
    public void giveKarma(){

        // if the photo already has karma, return
        if(hasKarma()){
            return;
        }

        initializeSettings();
        if (uri == null) {
            this.karma = true;
            return;
        }
        String uriStr = uri.toString() + KARMA_INDICATOR;
        settingsEditor.putBoolean(uriStr, true);
        settingsEditor.commit();
        numberofKarmas += 1;
        //listOfKarmaers.add(id);
        this.karma = true;

        Log.i("Give Karma", "Karma Given");

    }

    /**
     * Releases the photo from being shown as a wallpaper
     */
    public void release(){
        initializeSettings();
        if (uri == null) {
            this.released = true;
            return;
        }
        String uriStr = uri.toString() + RELEASED_INDICATOR;
        settingsEditor.putBoolean(uriStr, true);
        settingsEditor.commit();
        this.released = true;

        Log.i("Release Photo", "Photo Released");

    }


    /**
     * Getter method for the location
     */
    public Location getLocation(){
        if(!hasLocation){ return null; }
        return this.location;
    }

    /**
     * Setter method for the location
     */
    private void setLocation(Location loc){
        if(loc == null){ return; }
        this.location = loc;
    }

    /**
     * Getter method to check if there's a location
     */
    public boolean hasLocation(){
        return this.hasLocation;
    }

    /**
     * Getter method for the URI path
     */
    public String getPath(){
        return this.uri.getPath();
    }

    /**
     * Getter method for the URI
     */
    public Uri getUri(){
        return this.uri;
    }

    /**
     * Getter method for the date
     */
    public Date getDate(){
        if(!hasDate) { return null; }
        return this.dateCalendar.getTime();
    }

    /**
     * Getter method for if the photo has a date
     */
    public boolean hasDate(){
        return this.hasDate;
    }

    /**
     * Getter method for if the photo has karma
     */
    public boolean hasKarma(){
        return this.karma;
    }

    /**
     * Getter method for if photo has been released
     */
    public boolean isReleased(){
        return this.released;
    }

    /**
     * Getter method for the points
     */
    public int getPoints(){
        return this.points;
    }

    /**
     * Setter method for the points
     */
    public void setPoints(int points) {this.points = points;}

    /**
     * Setter method for the context
     */
    private void setContext(Context context){
        if(context == null){ return; }
        this.context = context;
    }

    /**
     * Setter method for the photo's URI
     */
    private void setUri(Uri input){
        this.uri = input;
    }

    public boolean hasEXIF(){
        return this.hasEXIF;
    }

    // should strip .extensions from the photo name. e.g. photo.jpg -> photo
    // use this to pass photo into database
    public String parseName(){
        int index = name.indexOf(".");
        String parsedName = name.substring(0, index);
        return parsedName;
    }

    public String getName(){
        return name;
    }

    public int getKarma(){
        return karmaCount;
    }

    public String getCustomLocation(){
        return customLocation;
    }

    public boolean getReleased() { return released; }
}


