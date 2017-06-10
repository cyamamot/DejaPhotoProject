package g25.com.dejaphoto;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
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
import com.google.firebase.database.GenericTypeIndicator;
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
 * USAGE:
 *  Only external method call is syncFriends()!!
 *
 * Internal Flow: syncFriends() gets friend list from firebase, confirms friends with isFriends()
 * isFriends() calls getPhotoListFromFriend() to get shared photo list from friend if confirmed
 *  getPhotoListFromFriend() calls downloadPhoto() to download Photo.
 */

public class FirebaseWrapper {
    static FirebaseDatabase database;
    static FirebaseStorage storage;
    static StorageReference storageRef;
    private static final String TAG = "firebaseWrapper";
    private String selfId;
    private String currFriendId;
    private boolean isCurrFriendAFriend = false;
    public static ArrayList<String> friendsList;
    private HashMap<String, ArrayList<BackgroundPhoto>> allFriendsPhotos;
    private ArrayList<BackgroundPhoto> currFriendPhotos;
    private Context context;

    public boolean uploadPhotoSuccess;
    public boolean isFriend;
    public boolean addEmail;


    /**
     * Description: Constructor for the FirebaseWrapper. Initializes the database, storage, and
     * other data fields.
     */
    public FirebaseWrapper(Context context) {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storage.setMaxUploadRetryTimeMillis(180000);
        storageRef = storage.getReference();
        this.context = context;
        int hashSelf = FirebaseAuth.getInstance().getCurrentUser().getEmail().hashCode();
        selfId = Integer.toString(hashSelf);

        allFriendsPhotos = new HashMap<>();
        friendsList = new ArrayList<>();
        loadFriends();
        currFriendPhotos = new ArrayList<>();

    }

    /**
     * Description: Adds the passed in user to the database
     */
    public void addUser(String email) {
        addEmail = false;
        if (email == "") {
            return;
        }
        addEmail = true;
        DatabaseReference users = database.getReference("users");

        int hash = (email).hashCode();
        String key = Integer.toString(hash);

        Log.e("FW", email);
        users.child(key).child("email").setValue(email);
    }

    /**
     * Description: Reads users from database
     */
    public void readUsers() {
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

    /**
     * Description: Upload a photo along with its metadata. Hash is the toString'd hash value of a
     * user's email and photo is the photo we want to upload
     */
    public void uploadPhoto(String hash, BackgroundPhoto photo) {
        // Create a child reference
        // imagesRef now points to the child which is a user
        // and photos should be stored under each user node
        uploadPhotoSuccess = false;
        String path = "images/" + hash + "/" + photo.getName();
        Log.d("FirebaseWrapper", "Uploading photo to this path: " + path);
        StorageReference imagesRef = storageRef.child(path);

        // uploads photo
        Log.d("UPLOAD ATTEMPT", "RECEIVED");
        imagesRef.putFile(photo.getUri())
                .addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        uploadPhotoSuccess = true;
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

    /**
     * Description: Gets a list of all the photos from a specific friend
     */
    public void getPhotoListFromFriend(String hashedFriend) {
        final String friendId = hashedFriend;
        Log.e("FirebaseWrapper", friendId);
        DatabaseReference isShared = database.getReference("users").child(friendId).child("isSharingOn");
        isShared.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean s = (Boolean) dataSnapshot.getValue();
                Log.e("FirebaseWrapper", "Bool val=" + String.valueOf(s));
                if (s) {
                    DatabaseReference photos = database.getReference("users").child(friendId).child("photos");
                    // Read from the database
                    photos.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            Log.e("fbwrapper", "size of this friend's photo list: " + snapshot.getChildrenCount());
                            // here we loop through our entire list of photos
                            // for each photo we add the photo to our arraylist, and we set the value listener
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                // now we loop through each field in the photo
                                String parsedName = postSnapshot.getKey();
                                Log.e("fbwrapper", parsedName);
                                String name = parsedName + ".jpg";
                                int karma = Integer.parseInt(postSnapshot.child("karmaCount").getValue().toString());
                                String customLocation = "default";
                                try {
                                    customLocation = postSnapshot.child("customLocation").getValue().toString();
                                } catch (Exception e) {
                                }

                                // once we get the photo's metadata, we can now download it
                                downloadPhoto(friendId, name, karma, customLocation);

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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // call this to download photos from all friends
    //CURRENTLY NOT USED BY DO NOT DELETE
    /*public void downloadAllFriendsPhotos(){
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
    }*/

    /**
     * Description: Downloads a photo from the firebase storage, places it in local friends album
     */
    public void downloadPhoto(String hash, String photoName, int karma, String customLoc){

        // checks if user has released a friends' photo; if so, we don't return
        if(photoIsReleased(photoName)){
            return;
        }


        // Create a child reference
        // imagesRef now points to the child which is a user
        // and photos should be stored under each user node
        StorageReference imageRef = storageRef.child("images").child(hash).child(photoName);

        if (hash.equals(selfId)){
            File DJP = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), LoginActivity.DJP_DIR);
            File f = new File(DJP, photoName);
            final Uri uri = Uri.fromFile(f);
            BackgroundPhoto photo = new BackgroundPhoto(uri, karma, customLoc, context);
            return;
        }

        // this is where file is stored; photo should be downloaded to friends' album
        File DJPFriends = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DJP_FRIENDS_DIR);
        File localFriendsPhotoFile = null;
        localFriendsPhotoFile = new File(DJPFriends, photoName);
        final Uri uri = Uri.fromFile(localFriendsPhotoFile);

        BackgroundPhoto photo = new BackgroundPhoto(uri, karma, customLoc, context);
        try {
            imageRef.getFile(localFriendsPhotoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    Log.d("fbwrapper download", "photo successfully downloaded");
                    updateGallery(uri);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }catch(Exception e){}
    }

    /**
     * Description: Update gallery with the passed in Uri
     */
    private void updateGallery(Uri uri){

        File file = new File(uri.getPath());
        MediaScannerConnection.scanFile(context,
                new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }

    /**
     * Description: Adds a friend to current user's list of friends; updates database and local
     * arraylist of friends
     */
    public void addFriend(String email){
        int hash = (email).hashCode();
        String key = Integer.toString(hash);

        DatabaseReference friend = database.getReference("users").child(selfId).child("friends").child(key);
        friend.setValue(email);
        friendsList.add(email);
        Log.d("DEBUG", "friend list size: " + friendsList.size());
    }

    /**
     * Description: Used to load the user's friends into the friendsList
     */
    public void loadFriends(){

        DatabaseReference ref = database.getReference("users").child(selfId).child("friends");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, String>> t = new GenericTypeIndicator<Map<String, String>>(){};
                Map<String, String> f = dataSnapshot.getValue(t);
                if(dataSnapshot.exists()) {
                    for(Map.Entry<String, String> entry : f.entrySet()) {
                        friendsList.add(entry.getValue());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateShare(boolean s){
        Log.e("FW", "yo");
        database.getReference("users").child(selfId).child("isSharingOn").setValue(s);
    }

    /**
     * Description: Goes through checks and returns if the friend is a confirmed friend. Once we
     * confirm a friend, we add/get their list of photos.
     */
    public void isFriends(String email){
        isFriend = false;
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
                    isFriend = true;
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

    /**
     * Description: Set the friends ID
     */
    public void setCurrFriendId(String email){
        int hash = (email).hashCode();
        currFriendId = Integer.toString(hash);
    }

    /**
     * Description: Sync the friends with the database and confirm that each friend is actually a
     * friend (two-way relationship)
     */
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
                    isFriends(friendEmail);
                    Log.e("friend email: ", friendEmail);
                }
                getPhotoListFromFriend(selfId);

            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * Description: Sync the current user with the database
     */
    public void syncCurrentUserPhotos(){

        DatabaseReference currentUserPhotos = database.getReference("users").child(selfId).child("photos");
        // Read from the database
         currentUserPhotos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.e("number of photos " ,""+snapshot.getChildrenCount());
                //Use this method to update current user's photos
                getPhotoListFromFriend(selfId);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * Description: Return the user's ID
     */
    public String getSelfId(){
        return this.selfId;
    }

    /**
     * Description: Adds a photo to the database to store photo's metadata. We store each background
     * photo as an object in database and then we can retrieve the karma and location name from its
     * child nodes.
     */
    public void addPhotoMetadata(BackgroundPhoto photo){
        // we identify each photo by parsed name in database by removing .jpg
        //DatabaseReference photoRef = database.getReference("users").child(selfId).child("photos").child(photo.parseName());
        DatabaseReference ref = database.getReference("users");
        final String photoName = photo.parseName();
        final int pkarma = photo.getKarma();
        final String plocation = photo.getCustomLocation();
        final String pname = photo.getName();
        Log.e("FirebaseWrapper", "1");

        ref.orderByChild("photos/"+photoName+"/name").equalTo(pname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("FirebaseWrapper", "2");
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        Log.e("FirebaseWrapper", issue.getKey());
                        DatabaseReference photoRef = database.getReference("users").child(issue.getKey()).child("photos").child(photoName);
                        photoRef.child("karmaCount").setValue(pkarma);
                        photoRef.child("customLocation").setValue(plocation);
                        photoRef.child("name").setValue(pname);
                    }
                }else{
                    DatabaseReference photoRef = database.getReference("users").child(selfId).child("photos").child(photoName);
                    photoRef.child("karmaCount").setValue(pkarma);
                    photoRef.child("customLocation").setValue(plocation);
                    photoRef.child("name").setValue(pname);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseWrapper", "herro");
            }
        });
    }

    /**
     * Description: Adds a photo to the correct friend's arraylist within the hashmap that has each friend's list
     */
    public void addPhotoToPhotoList(String friendId, BackgroundPhoto photo){
        allFriendsPhotos.get(friendId).add(photo);

    }

    /**
     * Description: Getter method for the current friend's photos
     */
    public ArrayList<BackgroundPhoto> getCurrFriendPhotos(){
        return currFriendPhotos;
    }

    /**
     * Description: Getter method to get the list of friends
     */
    public ArrayList<String> getFriends(){
        return friendsList;
    }

    /**
     * Description: Checks local preferences and if a friend's photo was released by the user, this
     * returns true
     */
    private boolean photoIsReleased(String photoName){
        SharedPreferences settings =
                context.getSharedPreferences(SettingsActivity.PREFS_NAME, SettingsActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        return settings.getBoolean(photoName, false);
    }

    /**
     * Description: Releases the photo from the database
     */
    public void releaseFromDatabase(BackgroundPhoto photo){
        String parseName = photo.parseName();
        String name = photo.getName();

        //remove from text db
        DatabaseReference ref
                = database.getReference().child("users").child(getSelfId()).child("photos").child(parseName);
        ref.removeValue();

        //remove from storage
        storageRef.child("images").child(getSelfId()).child(name).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d(TAG, "onSuccess: deleted file");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d(TAG, "onFailure: did not delete file");
            }
        });

    }

    /**
     * Description: Used for testing whether a friend is added correctly
     */
    public boolean findFriend(String email) {
        for(int i = 0; i < friendsList.size(); i++) {
            if(friendsList.get(i) == email) {
                return true;
            }
        }
        return false;
    }

    public void removeFriend(String email) {
        for(int i = 0; i < friendsList.size(); i++) {
            if(friendsList.get(i) == email) {
                friendsList.remove(i);
            }
        }
    }
}
