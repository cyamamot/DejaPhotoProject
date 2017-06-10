package g25.com.dejaphoto;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Created by dillonliu on 5/6/17.
 */

public class WallpaperChanger {
    private static final int PREV_LIST_SIZE = 10;

    private WallpaperManager myWallpaperManager;
    private int prevCursor;
    public ArrayList<BackgroundPhoto> prevList; //TODO use the wrapper
    public PriorityQueue<BackgroundPhoto> queue;
    private Context context;
    private SortingAlgorithm sorter;
    private int albumSize;
    private FirebaseWrapper fbWrapper;
    static SharedPreferences settings;
    static SharedPreferences.Editor settingsEditor;
    public PhotoCompare photoCompare;
    private int display_width = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int display_height = Resources.getSystem().getDisplayMetrics().heightPixels;
    private int display_ratio = display_height/display_width;

    /**
     * Constructor passes in activity to get context and stuff
     */
    public WallpaperChanger(Context context) {
        this.context = context;
        this.fbWrapper = new FirebaseWrapper(context);
        settings = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        settingsEditor = settings.edit();
    }

    public WallpaperChanger() {
        photoCompare = new PhotoCompare();
        queue = new PriorityQueue<BackgroundPhoto>(1, photoCompare);
    }



    /**
     * Description: Calls WallpaperManager to set wallpaper to specified image
     * http://stackoverflow.com/questions/25828808/issue-converting-uri-to-bitmap-2014
     */
    private void setWallpaper(BackgroundPhoto photoWrapper) {
        Uri uri = photoWrapper.getUri();

        try {
            myWallpaperManager = WallpaperManager.getInstance(context);
            //int height = Resources.getSystem().getDisplayMetrics().widthPixels;
            //int width = Resources.getSystem().getDisplayMetrics().heightPixels;
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            //Bitmap b = Bitmap.createScaledBitmap(bitmap, width, height, true);
            //Bitmap b = addLocationtoBitmap(bitmap);

            /*int bitmap_ratio = bitmap.getHeight()/bitmap.getWidth();

            // Check if the bitmap width is bigger than the display width
            if(bitmap.getWidth() > display_width) {
                // Set the height/width to the new proportions
                bitmap.setHeight(bitmap_ratio * display_width);
                bitmap.setWidth(display_width);
            }

            // Check if the bitmap height is bigger than the display height
            if(bitmap.getHeight() > display_height) {
                // Set the height/width to the new proportions
                bitmap.setWidth(display_height / bitmap_ratio);
                bitmap.setHeight(display_height);
            }*/

            myWallpaperManager.setBitmap(bitmap);

            //this one resizes it by stretching it out which isn't right
            //myWallpaperManager.setBitmap(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("POINTS", Integer.toString(photoWrapper.getPoints()));
    }

    /**
     * Cursor gets the photos from media store and we use it to point to each photo in
     * http://stackoverflow.com/questions/6855399/how-to-implement-image-gallery-in-gridview-in-android
     */
    public void initialize() {

        //prevents re-initialization when service restarts
        if (queue != null && queue.size() != 0) {
            return;
        }

        this.sorter = new SortingAlgorithm(context);


        //cursor to get images from content provider
        Cursor cursor = initializeCursor("%DejaPhoto/%");
        Cursor copiedCursor = initializeCursor("%DejaPhotoCopied/%");
        Cursor friendCursor = initializeCursor("%DejaPhotoFriends/%");
        boolean useMyAlbum = settings.getBoolean("use my album", true);
        boolean useCopiedAlbum = settings.getBoolean("use copied album", true);
        boolean useFriendsAlbum = settings.getBoolean("use friends album", true);


        PhotoCompare comparator = new PhotoCompare();
        queue = new PriorityQueue<>(1, comparator);
        //fill queue

        if (useMyAlbum) populateQueue(cursor);
        if (useCopiedAlbum) populateQueue(copiedCursor);

        if (useFriendsAlbum) {
            if (sharingOn()) {
                populateQueue(friendCursor);
            }
        }

        // test uploading photo
        //DejaPhotoService.fbWrapper.uploadPhoto("test", queue.peek());

        Log.e("WallpaperChanger", "INITIALIZED");
        next();
    }

    /**
     * Fill the queue with photos that will appear as the wallpaper
     */
    void populateQueue(Cursor cursor) {

        //initialize cursor and stuff to track location of cursor
        try { //in case gallery is empty
            albumSize = cursor.getCount();
            cursor.moveToFirst();
        } catch (NullPointerException e) {
            e.printStackTrace();
            albumSize = 0;
        }

        //initialize previous photo list and cursor
        if(prevList == null){
            prevList = new ArrayList<BackgroundPhoto>();
        }
        prevCursor = 0;

        //String[] strUrls = new String[albumSize];
        String[] mNames = new String[albumSize];

        //loop through all images and assign points, put into queue
        for (int i = 0; i < albumSize; i++) {
            cursor.moveToPosition(i);
            String path = cursor.getString(1);


            BackgroundPhoto curr = new BackgroundPhoto(path, context);
            if (!curr.isReleased()) {
                sorter.assignPoints(curr);
                queue.add(curr);
            }

            //DEBUG log messages
            mNames[i] = cursor.getString(3);
            Log.e("mNames[i]", mNames[i] + ":" + cursor.getColumnCount() + " : " + cursor.getString(1));
            Log.e("QUEUE SIZE", Integer.toString(queue.size()));
        }

        Log.e("WallpaperChanger", "Queue Size: " + Integer.toString(queue.size()));
    }

    private Cursor initializeCursor(String dirQuery){
        String[] whereArgs = new String[]{dirQuery};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media.DATA + " like ? ", whereArgs,
                MediaStore.Images.ImageColumns.DATE_TAKEN);
        return cursor;
    }

    /**
     * Sets wallpaper to next photo in album; if we reach the end, we go back to the first photo
     */
    public void next() {

        Log.e("WallpaperChanger", "NEXT");
        BackgroundPhoto nextPhoto = null;

        if (queue.isEmpty()) {
            initialize();
            Log.e("Next Test", "Queue Empty");
            Toast.makeText(context, "Restarting from Beginning, Queue Empty,", Toast.LENGTH_LONG).show();
        }

        //case: not in previous photo list
        if (prevCursor == 0) {
            nextPhoto = queue.remove();
            //add photo to front of the prevList
            if (nextPhoto != null) {
                prevList.add(0, nextPhoto);
            }
            //maintain list size of 10
            if (prevList.size() > 11) {
                prevList.remove(11);
            }
        }
        //case: in previous photo list
        else {
            //move "forward" in the previous list
            prevCursor--;
            nextPhoto = prevList.get(prevCursor);
        }


        //DEBUG CHECK LOCATION
        if (nextPhoto.hasLocation()) {
            Location location = nextPhoto.getLocation();
            Log.e("Location Latitude", Double.toString(location.getLatitude()));
            Log.e("Location Longitude", Double.toString(location.getLongitude()));
        } else {
            Log.e("Location", "No Location Geotag Available for this Photo");
        }

        //DEBUG CHECK DATE
        if (nextPhoto.hasDate()) {
            Date date = nextPhoto.getDate();
            Log.e("Date", date.toString());
        } else {
            Log.e("Date", "No Date Stamp Available for this Photo");
        }

        String comments = null;
        if(nextPhoto.hasEXIF()){
            comments = nextPhoto.exifData.getAttribute(ExifInterface.TAG_USER_COMMENT);
        }
        if (comments != null) {
            Log.e("Printing Comments", comments);
        } else {
            Log.e("Printing Comments", "No Comments");
        }
        //Toast.makeText(context, "NEXT", Toast.LENGTH_SHORT).show();
        setWallpaper(nextPhoto);
        setLocation();
    }


    /**
     * Sets wallpaper to previous photo in album.
     */
    public void previous() {

        Log.e("WallpaperChanger", "PREV");
        //not at end
        if (prevCursor < 10 && prevList.size() > prevCursor + 1) {
            prevCursor++;
            setWallpaper(prevList.get(prevCursor));
            //DEBUG log
            Log.e("Previous Test", "Showing Previous Photo at" + prevCursor + "position");
        }
        //at end
        else {
            Toast.makeText(context, "No More Previous Photos!", Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(context, "PREVIOUS", Toast.LENGTH_SHORT).show();
        setLocation();
    }


    /**
     * Set the current wallpaper released boolean to true
     */
    public void release() {

        Log.e("WallpaperChanger", "RELEASED");
        Toast.makeText(context, "RELEASED", Toast.LENGTH_SHORT).show();
        //always in some position in the prevList, just set release bool and remove from list
        prevList.get(prevCursor).release();
        fbWrapper.releaseFromDatabase(prevList.get(prevCursor));
        prevList.remove(prevCursor);
        next();

    }


    /**
     * Set current wallpaper karma boolean to true
     */
    public void karma(String id) {
        Log.e("WallpaperChanger", "k1");
        Toast.makeText(context, "KARMA SET <3", Toast.LENGTH_SHORT).show();
        Log.e("WallpaperChanger", "k2");
        prevList.get(prevCursor).giveKarma(id, fbWrapper);
        Log.e("WallpaperChanger", "karma bitch");
    }


    class PhotoCompare implements Comparator<BackgroundPhoto> {

        /**
         * Takes in two BackgroundPhoto objects and compares them
         * Returns 1 if the 1st photo is ranked <= the 2nd photo
         * Returns -1 if the 2nd photo is ranked < the 1st photo
         */

        @Override
        public int compare(BackgroundPhoto first, BackgroundPhoto second) {
            if (first.getPoints() < second.getPoints()) {
                return 1;
            } else if (first.getPoints() > second.getPoints()) {
                return -1;
            } else {
                return 1;
            }

        }
    }

    /**
     * Display the location of the photo above the widget if the photo has a location
     */
    public void setLocation() {
        //String path = cursor.getString(1);
        if(prevList.isEmpty()){
            return;
        }
        BackgroundPhoto curr = prevList.get(prevCursor);
        Location currPhotoLocation = curr.getLocation();
        Geocoder geocoder;
        List<Address> addresses;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.nav_widget);
        ComponentName thisWidget = new ComponentName(context, NavWidget.class);


        //remoteViews.setTextViewText(R.id.location_textview, curr.locationString);
        //appWidgetManager.updateAppWidget(thisWidget, remoteViews);


        geocoder = new Geocoder(context, Locale.getDefault());

        if (!(curr.getCustomLocation().equals("default"))){
            String output = curr.getCustomLocation()+ " --- " + Integer.toString(curr.karmaCount) + " <3";
            remoteViews.setTextViewText(R.id.location_textview, output);
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }else if (curr.hasLocation()) {
            try {
                Log.e("Setting Location", "setting location");
                addresses = geocoder.getFromLocation(currPhotoLocation.getLatitude(), currPhotoLocation.getLongitude(), 1);
                String address = addresses.get(0).getAddressLine(0);
                //String city = addresses.get(0).getLocality();
                //String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                //String postalCode = addresses.get(0).getPostalCode();
                //String knownName = addresses.get(0).getFeatureName();
                String output = address + ", " + country + " --- " + Integer.toString(curr.karmaCount) + " <3";
                remoteViews.setTextViewText(R.id.location_textview, output);
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            } catch (IOException e) {

                remoteViews.setTextViewText(R.id.location_textview, "No Location");
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);

                Log.e("Setting Location", "No Location Available");
            }
            catch(IndexOutOfBoundsException e){
                remoteViews.setTextViewText(R.id.location_textview, "No Location");
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);

                Log.e("Settings Location", "No Location Available");
            }
        }else{
            remoteViews.setTextViewText(R.id.location_textview, "No Location"+ " --- " + Integer.toString(curr.karmaCount) + " <3");
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }
    }

    /**
     * Description: Turns sharing on
     */
    private boolean sharingOn(){
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_NAME, SettingsActivity.MODE_PRIVATE);
        return settings.getBoolean("shareWithFriends", true);
    }

}

