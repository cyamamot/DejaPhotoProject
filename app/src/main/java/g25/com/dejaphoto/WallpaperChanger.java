package g25.com.dejaphoto;

import android.app.WallpaperManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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


    // constructor passes in activity to get context and stuff
    public WallpaperChanger(Context context){
        this.context = context;
        this.sorter = new SortingAlgorithm(context);
    }

    // http://stackoverflow.com/questions/25828808/issue-converting-uri-to-bitmap-2014

    // calls wallpapermanager to set wallpaper to specified image
    private void setWallpaper(BackgroundPhoto photoWrapper){
        Uri uri = photoWrapper.getUri();
        try {
            myWallpaperManager = WallpaperManager.getInstance(context);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

            // trying to set text on the bitmap
            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLUE);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawText("hi its me cse110 sucks", 300, 300, textPaint);

            myWallpaperManager.setBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("POINTS", Integer.toString(photoWrapper.getPoints()));
    }


    // http://stackoverflow.com/questions/6855399/how-to-implement-image-gallery-in-gridview-in-android
    // cursor gets the photos from media store and we use it to point to each photo in album
    public void initialize(){

        //prevents re-initialization when service restarts
        if(queue != null && queue.size() != 0){
            return;
        }

        //fill queue
        populateQueue();

        Toast.makeText(context, "Initialized",
                Toast.LENGTH_LONG).show();
    }

    private void populateQueue() {

        //cursor to get images from content provider
        cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN);

        //initialize cursor and stuff to track location of cursor
        try { //in case gallery is empty
            albumSize = cursor.getCount();
            cursor.moveToFirst();
        }
        catch(NullPointerException e){
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
            if(!curr.isReleased()) {
                sorter.assignPoints(curr);
                queue.add(curr);
            }
            //DEBUG log messages
            //strUrls[i] = path;
            mNames[i] = cursor.getString(3);
            Log.e("mNames[i]",mNames[i]+":"+ cursor.getColumnCount()+ " : " + cursor.getString(1));
            Log.e("QUEUE SIZE", Integer.toString(queue.size()));
        }
        Toast.makeText(context, "Queue Size" + queue.size(), Toast.LENGTH_LONG).show();
    }


    /**
     * Sets wallpaper to next photo in album; if we reach the end, we go back to the first photo
     */
    public void next(){
        BackgroundPhoto nextPhoto = null;

        if(queue.isEmpty()){
            populateQueue();
            Log.e("Next Test", "Queue Empty");
            Toast.makeText(context, "Restarting from Beginning, Queue Empty,", Toast.LENGTH_LONG).show();
        }

        //case: not in previous photo list
        if(prevCursor == 0) {
            nextPhoto = queue.remove();
            //add photo to front of the prevList
            if(nextPhoto != null){
                prevList.add(0, nextPhoto);
            }
            //maintain list size of 10
            if(prevList.size() > 11){
                prevList.remove(11);
            }
        }
        //case: in previous photo list
        else{
            //move "forward" in the previous list
            prevCursor--;
            nextPhoto = prevList.get(prevCursor);
        }


        //DEBUG CHECK LOCATION
        if(nextPhoto.hasLocation()) {
            Location location = nextPhoto.getLocation();
            Log.e("Location Latitude", Double.toString(location.getLatitude()));
            Log.e("Location Longitude", Double.toString(location.getLongitude()));
        }
        else{
            Log.e("Location", "No Location Geotag Available for this Photo");
        }

        //DEBUG CHECK DATE
        if(nextPhoto.hasDate()){
            Date date = nextPhoto.getDate();
            Log.e("Date", date.toString());
        }
        else{
            Log.e("Location", "No Date Stamp Available for this Photo");
        }

        setWallpaper(nextPhoto);
        String comments = nextPhoto.exifData.getAttribute(ExifInterface.TAG_USER_COMMENT);
        if( comments != null){
            Log.e("Printing Comments", comments);
            Toast.makeText(context, comments, Toast.LENGTH_LONG);
        }
        else{
            Log.e("Printing Comments", "No Comments");
        }
    }


    /**
     * Sets wallpaper to previous photo in album.
     */
    public void previous(){

<<<<<<< HEAD
        if(cursorLocation <= 0) {
            Log.d("Debug", "I am here");
            cursorLocation = albumSize - 1;
        }
=======
        //not at end
        if(prevCursor < 10 && prevList.size() > prevCursor + 1) {
            prevCursor++;
            setWallpaper(prevList.get(prevCursor));
>>>>>>> 3ebc5fdba4fb3c3b6bc263e51c63a3a94be3eb51

            //DEBUG log
            Log.e("Previous Test", "Showing Previous Photo at" + prevCursor + "position");
        }
        //at end
        else{
            Toast.makeText(context, "No More Previous Photos!", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * sets current wallpaper released boolean to true
     */
    public void release()
    {
        //always in some position in the prevList, just set release bool and remove from list
        prevList.get(prevCursor).release();
        prevList.remove(prevCursor);
        next();

    }

    /**
     * set current wallpaper karma boolean to true
     */
    public void karma() {
        prevList.get(prevCursor).giveKarma();
    }


    class PhotoCompare implements Comparator<BackgroundPhoto>{
        @Override
        public int compare(BackgroundPhoto first, BackgroundPhoto second){
           if(first.getPoints() < second.getPoints()){
                return 1;
           }

           else if(first.getPoints() > second.getPoints()){
               return -1;
           }

           else{
              return 1;
           }

        }
    }
}
