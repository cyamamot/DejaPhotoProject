package g25.com.dejaphoto;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.media.MediaScannerConnection;
import android.support.v4.app.ActivityCompat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.helper.IpLogger;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlbumsActivity extends AppCompatActivity {

    private static final int RC_CODE_PICKER = 2000;
    private static final int RC_CAMERA = 3000;
    private static final int RC_STORAGE = 4000;
    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 5000;

    private IpLogger logger = IpLogger.getInstance();

    private FirebaseWrapper fbWrapper;
    private TextView textView;
    private ArrayList<Image> images = new ArrayList<>();
    static SharedPreferences settings;
    static SharedPreferences.Editor settingsEditor;

    public int numImagesCopied;



    private Uri cameraOutUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        // Set up settings
        settings = this.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        settingsEditor = settings.edit();

        /*
        settingsEditor.putBoolean("use my album", true);
        settingsEditor.commit();
        settingsEditor.putBoolean("use friends album", true);
        settingsEditor.commit();
        settingsEditor.putBoolean("use copied album", true);
        settingsEditor.commit();
        */

        findViewById(R.id.button_pick_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        findViewById(R.id.button_open_camera).setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                final Activity activity = AlbumsActivity.this;

                // Request permissions if they have not been granted yet
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
                }
                // Request writing to external storage permission
                else if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_STORAGE);
                }
                // Request camera permission
                else if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, RC_CAMERA);
                }
                // If the permissions have been granted, open the camera
                else{
                    openCamera();
                }

            }
        });

        // Create new FirebaseWrapper if it has not been created yet
        if(fbWrapper == null){
            fbWrapper = new FirebaseWrapper(this);
        }

        // Set the Settings according to the checkboxes
        CheckBox cb1 = (CheckBox)findViewById(R.id.useDefaultAlbum);
        CheckBox cb2 = (CheckBox)findViewById(R.id.useCopiedAlbum);
        CheckBox cb3 = (CheckBox)findViewById(R.id.useFriendsAlbum);
        cb1.setChecked(settings.getBoolean("use my album", true));
        cb2.setChecked(settings.getBoolean("use copied album", true));
        cb3.setChecked(settings.getBoolean("use friends album", true));

    }

    /*
     * Description: Goes to the DejaPhoto album grid view. Called when the user clicks on the button
     * to view the DejaPhoto album
     */
    public void toGrid(View v) {
        Intent i = new Intent(this, AlbumGridActivity.class);
        i.putExtra("album", "DJP");
        startActivity(i);
    }

    /*
     * Description: Goes to the DejaPhotoCopied album grid view. Called when the user clicks on the
     * button to view the DejaPhotoCopied album
     */
    public void toGrid2(View v) {
        Intent i = new Intent(this, AlbumGridActivity.class);
        i.putExtra("album", "DJPC");
        startActivity(i);
    }

    /*
     * Description: Displays the photos for the user to select and add to the DejaPhotoCopied album.
     * Called when the button is pressed by the user to add photos from the gallery
     */
    public void start() {

        // Create the image picker
        ImagePicker imagePicker = ImagePicker.create(this)
                .folderMode(true) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select"); // image selection title


        imagePicker.multi(); // multi mode (default mode)

        imagePicker.limit(10) // max images can be selected (99 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("DejaPhoto")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(RC_CODE_PICKER); // start image picker activity with request code
    }

    /*
     * Description: Executes when it returns from an activity, specifically when a user used the
     * camera to take a picture or when the user selected photos from the gallery
     */
    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        Log.d("ACTIVITY RESULT ", Integer.toString(requestCode));
        super.onActivityResult(requestCode, resultCode, data);

        // Copy the images that were selected by the user from the gallery
        if (requestCode == RC_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            images = (ArrayList<Image>) ImagePicker.getImages(data);
            copyImages(images);
        }

        // Upload the photo that the user took using the camera
        if(requestCode == RC_CAMERA){
            Log.d("Camera Result", "CAMERA RETURNED");
            updateGallery(null);
            String userId = fbWrapper.getSelfId();
            fbWrapper.uploadPhoto(userId, new BackgroundPhoto(cameraOutUri.getPath(), this));
        }
    }

    /*
     * Description: Copy images over into the DejaPhotoCopied album. Called when the user selects
     * photos from the gallery to copy over.
     */
    public void copyImages(List<Image> images){
        if (images == null) return;

        //check and make folder if needed
        File copyDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), LoginActivity.DJP_COPIED_DIR);
        if(!copyDir.exists()){
            copyDir.mkdirs();
        }

        String userId = fbWrapper.getSelfId();
        OutputStream out;
        InputStream in;

        numImagesCopied = 0;

        // Go through all of the images and upload them
        for(int i = 0; i < images.size(); i++){
            numImagesCopied++;
            Uri originalFile = Uri.fromFile(new File(images.get(i).getPath()));
            String filename = originalFile.getLastPathSegment();
            File newFile = new File(copyDir + File.separator + filename);
            try {
                newFile.createNewFile();
                out = new FileOutputStream(newFile);
                in = new FileInputStream(originalFile.getPath());

                byte[] buffer = new byte[1000];
                int bytesRead = 0;
                while ( ( bytesRead = in.read( buffer, 0, buffer.length ) ) >= 0 ){
                    out.write(buffer, 0, buffer.length);
                }
                //UPLOAD
                fbWrapper.uploadPhoto(userId, new BackgroundPhoto(newFile.getPath(), this));
                updateGallery(Uri.fromFile(newFile));
            }
            catch(IOException e){
                e.printStackTrace();
                Log.e("CopyPic", "Can't create new File");
            }
        }

    }

    /*
     * Description: Opens the camera when the user wants to take a photo. Called when the user presses
     * the button to take a picture to add to DejaPhoto album.
     */
    private void openCamera(){
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), LoginActivity.DJP_DIR + File.separator + getImageName());
        cameraOutUri = Uri.fromFile(file);
        Uri cameraOut = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".fileprovider", file);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOut);
        captureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(captureIntent, RC_CAMERA);
    }

    /*
     * Description: Called to return the image name.
     */
    private String getImageName(){
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        return imageFileName;
    }

    /*
     * Description: Update the gallery with the uri that is passed in.
     */
    private void updateGallery(Uri uri){
        if(uri == null){
            uri = cameraOutUri;
        }

        File file = new File(uri.getPath());
        MediaScannerConnection.scanFile(this,
                new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }



    /*
     * Description: Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE:{
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    logger.d("Both permissions granted");
                    openCamera();
                    //captureImage();
                    return;
                }
                logger.e("Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                break;
            }
            case RC_STORAGE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logger.d("Write External permission granted");
                    Activity activity = AlbumsActivity.this;
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        openCamera();
                    }
                    return;
                }
                logger.e("Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                finish();
            }
            break;
            case RC_CAMERA: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logger.d("Camera permission granted");
                    openCamera();
                    //captureImage();
                    return;
                }
                logger.e("Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                break;
            }
            default: {
                logger.d("Got unexpected permission result: " + requestCode);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    /**
     * Description: Configure settings to use the DejaPhoto album depending on the checkbox
     */
    public void useMyAlbum(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            settingsEditor.putBoolean("use my album", true);
            settingsEditor.commit();
        }
        else {
            settingsEditor.putBoolean("use my album", false);
            settingsEditor.commit();
            CheckBox cb = (CheckBox)findViewById(R.id.useDefaultAlbum);
            cb.setChecked(false);
        }
    }

    /**
     * Description: Configure settings to use the DejaPhotoCopied album depending on the checkbox
     */
    public void useCopiedAlbum(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            settingsEditor.putBoolean("use copied album", true);
            settingsEditor.commit();
        }
        else {
            settingsEditor.putBoolean("use copied album", false);
            settingsEditor.commit();
        }
    }

    /**
     * Description: Configure settings to use the DejaPhotoFriends album depending on the checkbox
     */
    public void useFriendsAlbum(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (checked) {
            settingsEditor.putBoolean("use friends album", true);
            settingsEditor.commit();
        }
        else {
            settingsEditor.putBoolean("use friends album", false);
            settingsEditor.commit();
        }
    }

}
