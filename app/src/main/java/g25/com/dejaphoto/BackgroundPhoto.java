package g25.com.dejaphoto;


import android.provider.MediaStore;
import java.util.Date;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.media.ExifInterface;
import android.os.Parcelable;
import android.net.Uri;


/**
 * Created by angelazhang on 5/8/17.
 */

//GPS calculation ideas referenced from http://stackoverflow.com/questions/9868158/get-gps-location-of-a-photo

public class BackgroundPhoto {
    MediaStore.Images image;
    ExifInterface exifData;
    double latitude;
    double longitude;
    Date date;
    boolean karma;
    boolean released;

    public BackgroundPhoto(Context context){
        String[] columns = { MediaStore.Images.ImageColumns.LATITUDE,
                MediaStore.Images.ImageColumns.LONGITUDE,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_TAKEN
        };

        final String orderBy = MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC";

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN);

        //mUrls[i] = Uri.parse("file://" + cc.getString(1));

        int count = cursor.getCount();
        Double latitude, longitude;
        String filePath;
        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);
            latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE));
            longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE));
            filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            Log.i("Image>>",filePath);
            Log.i("Latitude>>", latitude+"");
            Log.i("Longitude>>",longitude+"");

        }


    }
}

