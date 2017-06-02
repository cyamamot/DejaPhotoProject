package g25.com.dejaphoto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    // onClick method to go to the Friends Page
    public void toFriends(View v) {
        Intent i = new Intent(this, FriendsActivity.class);
        startActivity(i);
    }

    // onClick method to go to the Albums Page
    public void toViewAlbums(View v) {
        Intent i = new Intent(this, AlbumsActivity.class);
        startActivity(i);
    }

    // onClick method to go to the Settings Page
    public void toSettings(View v) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    // signs user out of google/firebase account
    public void signOut(View v) {
        // Firebase sign out
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        goToHomeActivity();
    }

    // when user signs out, we return to login screen
    public void goToHomeActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


}
