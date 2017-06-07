package g25.com.dejaphoto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    private FirebaseWrapper fbWrapper;
    private ListView friendList;
    private FriendsAdapter friendsAdapter;
    private ArrayList<String> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        fbWrapper = new FirebaseWrapper(this);

        friends = fbWrapper.getFriends();
        friendList = (ListView)findViewById(R.id.lvFriendsList);
        friendsAdapter = new FriendsAdapter(this, friends);
        friendList.setAdapter(friendsAdapter);
    }

    // we click send request to add friend by email to our own self-list
    public void addFriend(View view){
        EditText email = (EditText) findViewById(R.id.etAddFriend);
        String emailText = email.getText().toString();
        fbWrapper.addFriend(emailText);
        friends = fbWrapper.getFriends();
        friendsAdapter.notifyDataSetChanged();
        email.setText("");
        for(String f : friends){
            Log.d("DEBUG", "friend: " + f);
        }
    }
}
