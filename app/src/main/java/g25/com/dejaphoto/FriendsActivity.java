package g25.com.dejaphoto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class FriendsActivity extends AppCompatActivity {

    private FirebaseWrapper fbWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        fbWrapper = new FirebaseWrapper();
    }

    // we click send request to add friend by email to our own self-list
    public void sendRequest(View view){
        EditText email = (EditText) findViewById(R.id.etFriendRequest);
        String emailText = email.getText().toString();
        fbWrapper.addFriend(emailText);
    }
}
