package g25.com.dejaphoto;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.UploadTask.TaskSnapshot;

import java.util.ArrayList;
import java.util.Map;

import static android.R.attr.value;

/**
 * Created by dillonliu on 6/1/17.
 */

public class FirebaseWrapper {
    static FirebaseDatabase database;
    static FirebaseStorage storage;
    static StorageReference storageRef;
    private static final String TAG = "firebaseWrapper";
    private String selfId;
    private String currFriendId;
    private boolean isCurrFriendAFriend = false;
    private ArrayList<String> friendsList;

    public FirebaseWrapper(){
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storage.setMaxUploadRetryTimeMillis(180000);
        storageRef = storage.getReference();

        int hashSelf = FirebaseAuth.getInstance().getCurrentUser().getEmail().hashCode();
        selfId = Integer.toString(hashSelf);

        friendsList = new ArrayList<>();
    }

    // adds a user to the database
    public void addUser(String email){
        DatabaseReference users = database.getReference("users");

        int hash = (email).hashCode();
        String key = Integer.toString(hash);

        users.child(key).child("email").setValue(email);
    }

    // reads users from database
    public void readUsers(){
        DatabaseReference Msg = database.getReference("users");
        // Read from the database
        Msg.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Map<String, Object> users = (Map<String, Object>) dataSnapshot.getValue();
                Log.i(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // hash is the toString'd hash value of a user's email
    // and photo is the photo we want to upload
    public void uploadPhoto(String hash, BackgroundPhoto photo){
        // Create a child reference
        // imagesRef now points to the child which is a user
        // and photos should be stored under each user node
        String path = "images/" + hash + "/" + photo.getName();
        Log.d("FirebaseWrapper", "Uploading photo to this path: " + path);
        StorageReference imagesRef = storageRef.child(path);
        //StorageReference photoRef = storageRef.child(hash + "/" + photo.getName());
        //photoRef.putFil

        // uploads photo
        Log.d("UPLOAD ATTEMPT", "RECEIVED");
        imagesRef.putFile(photo.getUri())
                .addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        return;
                    }
                });
    }

    public void downloadPhoto(String hash){
        // Create a child reference
        // imagesRef now points to the child which is a user
        // and photos should be stored under each user node
        StorageReference imagesRef = storageRef.child(hash);
    }

    // from our list of friends, we call this on each one to check if they also added us
    public boolean isConfirmedFriend(String email1, String email2){
        boolean oneAddedTwo = false;
        boolean twoAddedOne = false;

        int hash1 = (email1).hashCode();
        String key1 = Integer.toString(hash1);

        int hash2 = (email2).hashCode();
        String key2 = Integer.toString(hash2);

        // should return if user2 has added user1
        DatabaseReference friend2Added1 = database.getReference("users").child(key2).child("friends").child(key1);
        // Read from the database
        friend2Added1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                //String value = dataSnapshot.getValue(String.class);
                boolean oneAddedTwo = (boolean) dataSnapshot.getValue();
                if(oneAddedTwo){
                    //addConfirmedFriend(email2);
                }
                Log.i(TAG, "friend2Added1: " + oneAddedTwo);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
            return true;
    }

    // adds a friend to current user's list of friends
    public void addFriend(String email){
        int hash = (email).hashCode();
        String key = Integer.toString(hash);

        DatabaseReference friend = database.getReference("users").child(selfId).child("friends").child(key);
        friend.setValue(email);
        addFriendToList(email);
    }

    public ArrayList<String> getFriends() {

        DatabaseReference ref = database.getReference("users").child(selfId).child("friends");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshot = dataSnapshot.getChildren();
                for(DataSnapshot friend : snapshot){
                    Log.d("DEBUG", "Adding a friend!!!" + friend.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        return friendsList;
    }

    public void addFriendToList(String friend){
        friendsList.add(friend);
        Log.d("DEBUG", "friend list size: " + friendsList.size());
    }

    // checks a bunch of stuff and returns if the friend is a confirmed friend
    public void isFriends(String email){
        final String friendEmail = email;
        int hash = (email).hashCode();
        currFriendId = Integer.toString(hash);
        Log.d("FirebaseWrapper", "current Friend email: " + friendEmail);
        DatabaseReference ref = database.getReference("users").child(currFriendId).child("friends").child(selfId);
        // Read from the database
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                // if we are in friend's friends list, we are friends
                if(dataSnapshot.exists()) {
                    //setCurrFriend(true);
                    friendsList.add(friendEmail);
                    Log.d("FirebaseWrapper", "friend confirmed: " + friendEmail);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void setCurrFriendId(String email){
        int hash = (email).hashCode();
        currFriendId = Integer.toString(hash);
    }

    public void syncFriends(){
        DatabaseReference friends = database.getReference("users").child(selfId).child("friends");
        // Read from the database
        friends.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e("Count " ,""+snapshot.getChildrenCount());
                // here we loop through our entire list of friends
                // for each friend we add the friend to our hashmap, and we set the value listener
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    String friendEmail = postSnapshot.getValue(String.class);
                    //friendsList.put(friendEmail, false);
                    isFriends(friendEmail);
                    Log.e("friend email: ", friendEmail);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public String getSelfId(){
        return this.selfId;
    }
}
