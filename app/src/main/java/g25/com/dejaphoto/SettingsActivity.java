package g25.com.dejaphoto;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        //initialize fields
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        settingsEditor = settings.edit();
        useCustomAlbum = settings.getBoolean("useCustomAlbum", false);

        // creates our wallpaper handler and sets initial wallpaper
        wallpaperChanger = new WallpaperChanger(this);
        wallpaperChanger.cursorStuff();
    }

    public void next(View view){
        wallpaperChanger.next();
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

