package g25.com.dejaphoto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class SettingsActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "DejaPhotoPreferences";
    private boolean useCustomAlbum;
    private EditText delaySeconds;
    private TextView delayLabel;
    private int transitionDelay;
    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;
    //private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //initialize fields
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        settingsEditor = settings.edit();
        useCustomAlbum = settings.getBoolean("useCustomAlbum", false);
        transitionDelay = settings.getInt("transitionDelay", 5);
        delayLabel = (TextView)findViewById(R.id.label_transitionDelay);
        delayLabel.setText("Transition Delay: " + transitionDelay);


        //create intent with extras
        Intent intent = new Intent(SettingsActivity.this, DejaPhotoService.class);
        startService(intent);
    }

    public void setDelay(View view){

        //change settings
        delaySeconds = (EditText)findViewById(R.id.editText_transitionDelay);
        transitionDelay = Integer.parseInt(delaySeconds.getText().toString());
        settingsEditor.putInt("transitionDelay", transitionDelay);
        settingsEditor.commit();

        //change label
        delayLabel.setText("Transition Delay: " + transitionDelay);

        //service will restart itself when stopped
        Intent intent = new Intent(SettingsActivity.this, DejaPhotoService.class);
        stopService(intent);

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

