package g25.com.dejaphoto;

import android.app.WallpaperManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;

/**
 * Created by dillonliu on 5/6/17.
 */

public class WallpaperChanger {

    private WallpaperManager myWallpaperManager;
    private Cursor cursor;
    private int cursorLocation;
    private BackgroundPhoto[] photoWrappers; //TODO use the wrapper
    private Context context;
    private int albumSize;


    // constructor passes in activity to get context and stuff
    public WallpaperChanger(Context context){
        this.context = context;
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
    }


    // http://stackoverflow.com/questions/6855399/how-to-implement-image-gallery-in-gridview-in-android
    // cursor gets the photos from media store and we use it to point to each photo in album
    public void initialize(){

        //prevents re-initialization when service restarts
        if(cursor != null){
            return;
        }

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

        photoWrappers = new BackgroundPhoto[albumSize];

        //DEBUG log messages
        //String[] strUrls = new String[albumSize];
        String[] mNames = new String[albumSize];

        for (int i = 0; i < albumSize; i++) {
            cursor.moveToPosition(i);
            // cursor.getString(1) is the path to image file
            String path = cursor.getString(1);

            BackgroundPhoto curr = new BackgroundPhoto(path);
            photoWrappers[i] = new BackgroundPhoto(path);

            //DEBUG log messages
            //strUrls[i] = path;
            mNames[i] = cursor.getString(3);
            Log.e("mNames[i]",mNames[i]+":"+ cursor.getColumnCount()+ " : " + cursor.getString(1));
            //Log.e("uri", mUrls[i].toString());
        }

        cursorLocation = 0;

        Toast.makeText(context, "Initialized",
                Toast.LENGTH_LONG).show();
    }


    /**
     * Sets wallpaper to next photo in album; if we reach the end, we go back to the first photo
     */
    public void next(){
        if(cursorLocation >= albumSize) {
            Log.d("Debug", "I am here");
            cursorLocation = 0;
        }
        setWallpaper(photoWrappers[cursorLocation]);


        //DEBUG CHECK LOCATION
        if(photoWrappers[cursorLocation].hasLocation()) {
            Location location = photoWrappers[cursorLocation].getLocation();
            Log.e("Location Latitude", Double.toString(location.getLatitude()));
            Log.e("Location Longitude", Double.toString(location.getLongitude()));
        }
        else{
            Log.e("Location", "No Location Geotag Available for this Photo");
        }

        //DEBUG CHECK DATE
        if(photoWrappers[cursorLocation].hasDate()){
            Date date = photoWrappers[cursorLocation].getDate();
            Log.e("Date", date.toString());
        }
        else{
            Log.e("Location", "No Date Stamp Available for this Photo");
        }

        if(photoWrappers[cursorLocation].isReleased()){
            cursorLocation++;
        }

        cursorLocation++;
    }


    /**
     * Sets wallpaper to previous photo in album.
     */
    public void previous(){
        if(cursorLocation <= 0) {
            cursorLocation = albumSize - 1;
        }

        setWallpaper(photoWrappers[cursorLocation]);

        if(photoWrappers[cursorLocation].hasLocation()) {
            Location location = photoWrappers[cursorLocation].getLocation();
            Log.e("Location Latitude", Double.toString(location.getLatitude()));
            Log.e("Location Longitude", Double.toString(location.getLongitude()));
        }
        else{
            Log.e("Location", "No Location Geotag Available for this Photo");
        }

        //DEBUG CHECK DATE
        if(photoWrappers[cursorLocation].hasDate()){
            Date date = photoWrappers[cursorLocation].getDate();
            Log.e("Date", date.toString());
        }
        else{
            Log.e("Location", "No Date Stamp Available for this Photo");
        }

        if(photoWrappers[cursorLocation].isReleased()){
            cursorLocation--;
        }

        cursorLocation--;
    }

    /**
     * sets current wallpaper released boolean to true
     */
    public void release()
    {
        photoWrappers[cursorLocation].release();
    }

    /**
     * set current wallpaper karma boolean to true
     */
    public void karma() {
        photoWrappers[cursorLocation].giveKarma();
    }
}
