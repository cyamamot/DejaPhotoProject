package g25.com.dejaphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by angelazhang on 6/7/17.
 */

public class GridImageAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<BackgroundPhoto> itemList = new ArrayList<BackgroundPhoto>();


    /**
     * Description: Constructor for the grid image adapter
     */
    public GridImageAdapter(Context c, String album) {
        mContext = c;
        getImages(album);
    }

    /**
     * Description: Add a path to a new photo to the array list of photos
     */
    void add(BackgroundPhoto path){
        itemList.add(path);
    }

    /**
     * Description: Returns the size of itemList
     */
    public int getCount() {
        return itemList.size();
    }

    /**
     * Description: Returns the item at that position
     */
    public Object getItem(int position) { return itemList.get(position); }

    /**
     * Description: Implemented abstract method from Adapter (not used)
     */
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Description: Create a new ImageView for each item referenced by the Adapter
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(220, 220));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        Bitmap bm = decodeSampledBitmapFromUri(itemList.get(position).getUri().getPath(), 220, 220);

        imageView.setImageBitmap(bm);
        Log.d("getView", "grid of images set");
        return imageView;
    }

    /**
     * Description: Returns a sample decoded bitmap
     */
    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        Log.d("decodeSampled", "decoded");
        return bm;
    }


    /**
     * Description: Calculate the sample size of the passed in options
     */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        Log.d("grid size", Integer.toString(inSampleSize));
        return inSampleSize;
    }

    /**
     * Description: Get images from the album and add them to itemList
     */
    private void getImages(String album) {
        File dir;
        if (album.equals("DJP")) {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), LoginActivity.DJP_DIR);
        }else{
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), LoginActivity.DJP_COPIED_DIR);

        }
        File[] files = dir.listFiles();
        for (File file : files){
            BackgroundPhoto curr = new BackgroundPhoto(file.getAbsolutePath(), mContext);
            add(curr);
        }
        Log.d("getImages", Integer.toString(itemList.size()));

    }
}
