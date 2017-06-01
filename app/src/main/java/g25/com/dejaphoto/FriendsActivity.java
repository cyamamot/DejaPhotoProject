package g25.com.dejaphoto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
    }

    // onClick method to go back to the Home Page
    public void backToHome() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
    }
}
