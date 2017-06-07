package g25.com.dejaphoto;

import android.os.Environment;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.UploadTask.TaskSnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.value;
import static g25.com.dejaphoto.LoginActivity.DJP_FRIENDS_DIR;

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
    private HashMap<String, ArrayList<BackgroundPhoto>> allFriendsPhotos;
    private ArrayList<BackgroundPhoto> currFriendPhotos;

    public FirebaseWrapper(){
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storage.setMaxUploadRetryTimeMillis(180000);
        storageRef = storage.getReference();

        int hashSelf = FirebaseAuth.getInstance().getCurrentUser().getEmail().hashCode();
        selfId = Integer.toString(hashSelf);

        allFriendsPhotos = new HashMap<>();
        friendsList = new ArrayList<>();
        currFriendPhotos = new ArrayList<>();
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

        // every time we upload a photo to storage, we also upload metadata to db
        addPhotoMetadata(photo);
    }

    // gets a list of all the photos from a specific friend
    public void getPhotoListFromFriend(String hashedFriend) {
        final String friendId = hashedFriend;
        DatabaseReference photos = database.getReference("users").child(hashedFriend).child("photos");
        // Read from the database
        photos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e("fbwrapper", "size of this friend's photo list: "+ snapshot.getChildrenCount());
                // here we loop through our entire list of photos
                // for each photo we add the photo to our arraylist, and we set the value listener
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    // now we loop through each field in the photo
                    String parsedName = postSnapshot.getKey();
                    String name = postSnapshot.child("name").getValue().toString();
                    int karma = Integer.parseInt(postSnapshot.child("karma").getValue().toString());
                    String customLocation = postSnapshot.child("customLocation").getValue().toString();

                    // addPhotoToPhotoList(friendId, new BackgroundPhoto(name, karma, customLocation));

                    // once we get the photo's metadata, we can now download it
                    downloadPhoto(friendId, name);

                    Log.d("fbWrapper", "this friend's photo: " + name + " has karma = " + karma + " customLocation = " + customLocation);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    // call this to download photos from all friends
    public void downloadAllFriendsPhotos(){
        StorageReference friendRef;
        String hashCode;
        for(int i = 0; i < friendsList.size(); i++){
            hashCode = Integer.toString(friendsList.get(i).hashCode());
            friendRef = storageRef.child("images").child(hashCode);

            ArrayList<BackgroundPhoto> currFriendPhotos = allFriendsPhotos.get(friendRef);
            for(int j = 0; j < currFriendPhotos.size(); j++) {
                // loop through list of photo id's to download each photo
                downloadPhoto(hashCode, currFriendPhotos.get(j).getName());
            }
        }
    }

    // downloads a photo from the firebase storage, places it in local friends album
    public void downloadPhoto(String hash, String photoName){
        // Create a child reference
        // imagesRef now points to the child which is a user
        // and photos should be stored under each user node
        StorageReference imageRef = storageRef.child("images").child(hash).child(photoName);

        // this is where file is stored; photo should be downloaded to friends' album
        File DJPFriends = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DJP_FRIENDS_DIR);
        File localFriendsPhotoFile = null;
        localFriendsPhotoFile = new File(DJPFriends, photoName);

        imageRef.getFile(localFriendsPhotoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Log.d("fbwrapper download", "photo successfully downloaded");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    // adds a friend to current user's list of friends; updates database and local arraylist of friends
    public void addFriend(String email){
        int hash = (email).hashCode();
        String key = Integer.toString(hash);

        DatabaseReference friend = database.getReference("users").child(selfId).child("friends").child(key);
        friend.setValue(email);
        friendsList.add(email);
        Log.d("DEBUG", "friend list size: " + friendsList.size());
    }

    // checks a bunch of stuff and returns if the friend is a confirmed friend
    // once we confirm a friend, we add/get their list of photos
    public void isFriends(String email){
        final String friendEmail = email;
        int hash = (email).hashCode();
        currFriendId = Integer.toString(hash);
        final String friendHash = currFriendId;
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

                    // gets this friend's list of photos after friend confirmed
                    getPhotoListFromFriend(friendHash);
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
                Log.e("size of friend list: " ,""+snapshot.getChildrenCount());
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

    // adds a photo to the database to store photo's metadata
    // we store each background photo as an object in database
    // we can then retrieve the karma and locationname from its child nodes
    public void addPhotoMetadata(BackgroundPhoto photo){
        // we identify each photo by parsed name in database by removing .jpg
        DatabaseReference photoRef = database.getReference("users").child(selfId).child("photos").child(photo.parseName());

        // we store each field of the photo in database
        photoRef.child("karma").setValue(photo.getKarma());
        photoRef.child("customLocation").setValue(photo.getCustomLocation());
        photoRef.child("name").setValue(photo.getName());
    }

    // adds a photo to the correct friend's arraylist within the hashmap that has each friend's list
    public void addPhotoToPhotoList(String friendId, BackgroundPhoto photo){
        allFriendsPhotos.get(friendId).add(photo);
    }

    public ArrayList<BackgroundPhoto> getCurrFriendPhotos(){
        return currFriendPhotos;
    }

    public ArrayList<String> getFriends(){
        return friendsList;
    }

    public void syncFriendsPhotos(){
        for(int i = 0; i < friendsList.size(); i++){
            getPhotoListFromFriend(friendsList.get(i));
        }
    }
}
