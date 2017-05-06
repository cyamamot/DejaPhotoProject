package g25.com.dejaphoto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    // handles all wallpaper changes
    private WallpaperChanger wallpaperChanger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        wallpaperChanger = new WallpaperChanger(this);
    }

    // http://stackoverflow.com/questions/6855399/how-to-implement-image-gallery-in-gridview-in-android
    public void cursorStuff(View view){
        wallpaperChanger.cursorStuff();
    }

    public void next(View view){
        wallpaperChanger.next();
    }
}
