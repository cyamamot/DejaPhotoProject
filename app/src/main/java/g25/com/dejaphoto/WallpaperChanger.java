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
    private void setWallpaper(BackgroundPhoto photoWrapper){
        Uri uri = photoWrapper.getUri();
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

        //cursor to get images from content provider
        cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN);

        //initialize cursor and stuff to track location of cursor
        cursor.moveToFirst();
        albumSize = cursor.getCount();
        mUrls = new Uri[albumSize];
        photoWrappers = new BackgroundPhoto[albumSize];
        String[] strUrls = new String[albumSize];
        String[] mNames = new String[albumSize];

        for (int i = 0; i < albumSize; i++) {
            // cursor.getString(1) is the path to image file
            cursor.moveToPosition(i);

            String path = cursor.getString(1);

            //functionality moved to wrapper class, call getUri()
            mUrls[i] = Uri.parse("file://" + path);

            photoWrappers[i] = new BackgroundPhoto(path);

            strUrls[i] = path;
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
