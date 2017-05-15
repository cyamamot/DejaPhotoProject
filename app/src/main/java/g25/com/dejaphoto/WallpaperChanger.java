package g25.com.dejaphoto;

import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
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
    private Cursor cursor;
    private int prevCursor;
    private ArrayList<BackgroundPhoto> prevList; //TODO use the wrapper
    private PriorityQueue<BackgroundPhoto> queue;
    private Context context;
    private SortingAlgorithm sorter;
    private int albumSize;

    /**
     * Constructor passes in activity to get context and stuff
     */
    public WallpaperChanger(Context context) {
        this.context = context;
    }

    /**
     * Calls WallpaperManager to set wallpaper to specified image
     * http://stackoverflow.com/questions/25828808/issue-converting-uri-to-bitmap-2014
     */
    private void setWallpaper(BackgroundPhoto photoWrapper) {
        Uri uri = photoWrapper.getUri();
        try {
            myWallpaperManager = WallpaperManager.getInstance(context);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            //Bitmap b = addLocationtoBitmap(bitmap);

            myWallpaperManager.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("POINTS", Integer.toString(photoWrapper.getPoints()));
    }
    /*
    // Method to add location to wallpaper
    private Bitmap addLocationtoBitmap(Bitmap bitmap) {

        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are immutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        // trying to set text on the bitmap
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color
        textPaint.setColor(Color.BLUE);
        // text shadow
        textPaint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
        textPaint.setTextSize(14);

        Canvas canvas = new Canvas(bitmap);
        String location = "cse 110 is so much fun";

        // draw text to the Canvas center
        textPaint.setTextAlign(CENTER);

        Rect bounds = new Rect();
        textPaint.getTextBounds(location, 0, location.length(), bounds);
        int x = (canvas.getWidth()/2) - (bounds.width()/2);
        int y = (canvas.getHeight()/2);

        canvas.drawText(location, x, y, textPaint);

        return bitmap;
    }
    */

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

        //fill queue
        populateQueue();

        Toast.makeText(context, "Initialized", Toast.LENGTH_SHORT).show();
    }

    /**
     * Fill the queue with photos that will appear as the wallpaper
     */
    private void populateQueue() {

        //cursor to get images from content provider
        cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN);

        //initialize cursor and stuff to track location of cursor
        try { //in case gallery is empty
            albumSize = cursor.getCount();
            cursor.moveToFirst();
        } catch (NullPointerException e) {
            e.printStackTrace();
            albumSize = 0;
        }

        //initialize previous photo list and cursor
        prevList = new ArrayList<BackgroundPhoto>();
        prevCursor = 0;

        //String[] strUrls = new String[albumSize];
        String[] mNames = new String[albumSize];

        //loop through all images and assign points, put into queue
        PhotoCompare comparator = new PhotoCompare();
        queue = new PriorityQueue<BackgroundPhoto>(albumSize, comparator);
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
        Toast.makeText(context, "Queue Size : " + queue.size(), Toast.LENGTH_LONG).show();
    }


    /**
     * Sets wallpaper to next photo in album; if we reach the end, we go back to the first photo
     */
    public void next() {

        Toast.makeText(context, "NEXT", Toast.LENGTH_SHORT).show();
        BackgroundPhoto nextPhoto = null;

        if (queue.isEmpty()) {
            populateQueue();
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
            Log.e("Location", "No Date Stamp Available for this Photo");
        }

        String comments = nextPhoto.exifData.getAttribute(ExifInterface.TAG_USER_COMMENT);
        if (comments != null) {
            Log.e("Printing Comments", comments);
        } else {
            Log.e("Printing Comments", "No Comments");
        }

        setWallpaper(nextPhoto);
        setLocation();
    }


    /**
     * Sets wallpaper to previous photo in album.
     */
    public void previous() {

        Toast.makeText(context, "PREV", Toast.LENGTH_SHORT).show();
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
        setLocation();
    }


    /**
     * Set the current wallpaper released boolean to true
     */
    public void release() {

        Toast.makeText(context, "RELEASED", Toast.LENGTH_SHORT).show();
        //always in some position in the prevList, just set release bool and remove from list
        prevList.get(prevCursor).release();
        prevList.remove(prevCursor);
        next();

    }


    /**
     * Set current wallpaper karma boolean to true
     */
    public void karma() {
        Toast.makeText(context, "KARMA SET <3", Toast.LENGTH_SHORT).show();
        prevList.get(prevCursor).giveKarma();
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
        BackgroundPhoto curr = prevList.get(prevCursor);
        Location currPhotoLocation = curr.getLocation();
        Geocoder geocoder;
        List<Address> addresses;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.nav_widget);
        ComponentName thisWidget = new ComponentName(context, NavWidget.class);


        remoteViews.setTextViewText(R.id.location_textview, curr.locationString);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);


        geocoder = new Geocoder(context, Locale.getDefault());
        if (curr.hasLocation()) {
            try {
                Log.e("Setting Location", "setting location");
                addresses = geocoder.getFromLocation(currPhotoLocation.getLatitude(), currPhotoLocation.getLongitude(), 1);
                String address = addresses.get(0).getAddressLine(0);
                //String city = addresses.get(0).getLocality();
                //String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                //String postalCode = addresses.get(0).getPostalCode();
                //String knownName = addresses.get(0).getFeatureName();
                String output = address + ", " + country;
                remoteViews.setTextViewText(R.id.location_textview, output);
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);
            } catch (IOException e) {

                remoteViews.setTextViewText(R.id.location_textview, "No Location");
                appWidgetManager.updateAppWidget(thisWidget, remoteViews);

                Log.e("Setting Location", "No Location Available");
            }
        }
    }
}

