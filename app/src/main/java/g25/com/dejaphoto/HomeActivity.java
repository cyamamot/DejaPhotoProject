package g25.com.dejaphoto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    // onClick method to go to the Friends Page
    public void toFriends() {
        Intent i = new Intent(this, FriendsActivity.class);
        startActivity(i);
    }

    // onClick method to go to the Albums Page
    public void toViewAlbums() {
        Intent i = new Intent(this, AlbumsActivity.class);
        startActivity(i);
    }

    // onClick method to go to the Settings Page
    public void toSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }
}
