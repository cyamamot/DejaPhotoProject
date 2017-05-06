package g25.com.dejaphoto;

import android.app.Activity;
import android.app.WallpaperManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

/**
 * Created by dillonliu on 5/6/17.
 */

public class WallpaperChanger {
    private WallpaperManager myWallpaperManager;

    private Cursor cc;
    private int cursorPointer;
    private Uri[] mUrls;
    private Activity activity;

    // constructor passes in activity to get context and stuff
    public WallpaperChanger(Activity activity){
        this.activity = activity;
    }

    // http://stackoverflow.com/questions/25828808/issue-converting-uri-to-bitmap-2014
    // calls wallpapermanager to set wallpaper to specified image
    private void setWallpaper(Uri uri){
        try {
            myWallpaperManager = WallpaperManager.getInstance(activity.getApplicationContext());
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            myWallpaperManager.setBitmap(bitmap);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // http://stackoverflow.com/questions/6855399/how-to-implement-image-gallery-in-gridview-in-android
    public void cursorStuff(){

        cc = activity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                null);

        cc.moveToFirst();
        mUrls = new Uri[cc.getCount()];
        String[] strUrls = new String[cc.getCount()];
        String[] mNames = new String[cc.getCount()];
        for (int i = 0; i < cc.getCount(); i++) {
            // cc.getString(1) is the path to image file
            cc.moveToPosition(i);
            mUrls[i] = Uri.parse("file://" + cc.getString(1));
            strUrls[i] = cc.getString(1);
            mNames[i] = cc.getString(3);
            Log.e("mNames[i]",mNames[i]+":"+cc.getColumnCount()+ " : " +cc.getString(1));
            //Log.e("uri", mUrls[i].toString());
        }

        cursorPointer = 0;
        setWallpaper(mUrls[cursorPointer]);
    }

    public void next(){
        cursorPointer++;
        setWallpaper(mUrls[cursorPointer]);
    }
}
