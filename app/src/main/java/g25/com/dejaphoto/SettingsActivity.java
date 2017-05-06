package g25.com.dejaphoto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "DejaPhotoPreferences";
    private boolean useCustomAlbum;
    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;

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

        //initialize fields
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        settingsEditor = settings.edit();
        useCustomAlbum = settings.getBoolean("useCustomAlbum", false);
    }

    public void selectDefaultAlbum(View view){
        settingsEditor.putBoolean("useCustomAlbum", false);
        settingsEditor.commit();
    }

    public void selectCustomAlbum(View view){
        settingsEditor.putBoolean("useCustomAlbum", true);
        settingsEditor.commit();
    }
}

