package g25.com.dejaphoto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    private FirebaseWrapper fbWrapper;
    private ListView friendList;
    private ListView friendRequests;
    private FriendRequestsAdapter requestsAdapter;
    private ArrayList<String> requests;
    private ArrayList<String> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        fbWrapper = new FirebaseWrapper();

        friendList = (ListView)findViewById(R.id.lvFriendsList);
        //friendList = fbWrapper.getFriendRequests();

        friendRequests = (ListView)findViewById(R.id.lvRequests);
        requests = new ArrayList<>();
        requestsAdapter = new FriendRequestsAdapter(this, requests);
        friendRequests.setAdapter(requestsAdapter);
        requestsAdapter.addAll(fbWrapper.getFriendRequests());
    }

    // we click send request to add friend by email to our own self-list
    public void sendRequest(View view){
        EditText email = (EditText) findViewById(R.id.etFriendRequest);
        String emailText = email.getText().toString();
        fbWrapper.sendFriendRequest(emailText);
        //fbWrapper.addFriend(emailText);
    }
}
