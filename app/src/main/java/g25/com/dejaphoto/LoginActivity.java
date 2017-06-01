package g25.com.dejaphoto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    // DELETE WHEN WE IMPLEMENT GOOGLE SIGN_IN
    // onClick method to go to Home Page
    public void toHomePage(View v) {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
    }
}
