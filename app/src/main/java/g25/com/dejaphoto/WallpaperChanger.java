package g25.com.dejaphoto;

import android.app.WallpaperManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by dillonliu on 5/6/17.
 */

public class WallpaperChanger {

    private WallpaperManager myWallpaperManager;
    private Cursor cursor;
    private int cursorLocation;
    private BackgroundPhoto[] photoWrappers; //TODO use the wrapper
    private Uri[] mUrls;
    private Context context;
    private int albumSize;

    // constructor passes in activity to get context and stuff
    public WallpaperChanger(Context context){
        this.context = context;
    }

    // http://stackoverflow.com/questions/25828808/issue-converting-uri-to-bitmap-2014

    // calls wallpapermanager to set wallpaper to specified image
    private void setWallpaper(BackgroundPhoto photo){
        Uri uri = photo.getUri();
        try {
            myWallpaperManager = WallpaperManager.getInstance(context);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
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

        //create cursor to traverse photos provided by content provider
        cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN);

        //keep track of where cursor is in album
        cursor.moveToFirst();
        albumSize = cursor.getCount();
        mUrls = new Uri[albumSize];
        String[] strUrls = new String[albumSize];
        String[] mNames = new String[albumSize];

        for (int i = 0; i < albumSize; i++) {
            // cursor.getString(1) is the path to image file
            cursor.moveToPosition(i);

            //functionality moved to wrapper class, call getUri()
            //mUrls[i] = Uri.parse("file://" + cursor.getString(1));

            photoWrappers[i] = new BackgroundPhoto(Uri.parse(cursor.getString(1)));
            strUrls[i] = cursor.getString(1);
            mNames[i] = cursor.getString(3);

            Log.e("mNames[i]",mNames[i]+":"+ cursor.getColumnCount()+ " : " + cursor.getString(1));
            //Log.e("uri", mUrls[i].toString());
        }

        cursorLocation = -1;

        Toast.makeText(context, "Initialized",
                Toast.LENGTH_LONG).show();
    }


    /**
     * Sets wallpaper to next photo in album; if we reach the end, we go back to the first photo
     */
    public void next(){
        cursorLocation++;
        if(cursorLocation >= albumSize) {
            cursorLocation = 0;
        }
        setWallpaper(photoWrappers[cursorLocation]);
    }
}
