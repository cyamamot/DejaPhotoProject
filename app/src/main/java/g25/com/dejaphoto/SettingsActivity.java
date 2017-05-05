package g25.com.dejaphoto;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
    private WallpaperManager myWallpaperManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        int v = 50;

        myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());

        accessImageGallery();


    }

    // accesses android's image gallery
    private void accessImageGallery() {
        // http://stackoverflow.com/questions/29196227/android-studio-display-images-from-my-android-gallery
        // access the images gallery by sending intent to gallery and starting gallery activity
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 2);
    }

    // http://stackoverflow.com/questions/29196227/android-studio-display-images-from-my-android-gallery
    // after calling startActivityForResult, once we have chosen an image, we then return this image data
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // data represents the image we chose and uri is used to make bitmap
        if (requestCode == 2 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            setWallpaper(selectedImage);
        }
    }

    // http://stackoverflow.com/questions/25828808/issue-converting-uri-to-bitmap-2014
    // calls wallpapermanager to set wallpaper to specified image
    private void setWallpaper(Uri uri){
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            myWallpaperManager.setBitmap(bitmap);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
