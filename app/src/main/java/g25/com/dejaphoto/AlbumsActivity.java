package g25.com.dejaphoto;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.camera.CameraModule;
import com.esafirm.imagepicker.features.camera.ImmediateCameraModule;
import com.esafirm.imagepicker.features.camera.OnImageReadyListener;
import com.esafirm.imagepicker.model.Image;

public class AlbumsActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RC_CODE_PICKER = 2000;
    private static final int RC_CAMERA = 3000;

    private TextView textView;
    private ArrayList<Image> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);

        textView = (TextView) findViewById(R.id.text_view);

        findViewById(R.id.button_pick_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        findViewById(R.id.button_open_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera(view);
            }
        });

    }

    public void start() {
        //final boolean returnAfterCapture = ((Switch) findViewById(R.id.ef_switch_return_after_capture)).isChecked();
        //final boolean isSingleMode = ((Switch) findViewById(R.id.ef_switch_single)).isChecked();
        //final boolean useCustomImageLoader = ((Switch) findViewById(R.id.ef_switch_imageloader)).isChecked();

        ImagePicker imagePicker = ImagePicker.create(this)
                //.theme(R.style.ImagePickerTheme)
                //.returnAfterFirst(returnAfterCapture) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                .folderMode(true) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select"); // image selection title

        /*if (useCustomImageLoader) {
            imagePicker.imageLoader(new GrayscaleImageLoader());
        }

        if (isSingleMode) {
            imagePicker.single();
        } else {*/
            imagePicker.multi(); // multi mode (default mode)
        //}

        imagePicker.limit(10) // max images can be selected (99 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(RC_CODE_PICKER); // start image picker activity with request code
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (requestCode == RC_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            images = (ArrayList<Image>) ImagePicker.getImages(data);
            copyImages(images);
            return;
        }

        if(requestCode == RC_CAMERA && resultCode == RESULT_OK && data != null){
            Uri pic = data.getData();
            Log.d("Camera Result", data.getData().toString());
            return;
        }

        /*if (requestCode == RC_CAMERA && resultCode == RESULT_OK) {
            getCameraModule().getImage(this, data, new OnImageReadyListener() {
                @Override
                public void onImageReady(List<Image> resultImages) {
                    images = (ArrayList<Image>) resultImages;
                    printImages(images);
                }
            });
        }*/
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void printImages(List<Image> images) {
        if (images == null) return;

        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0, l = images.size(); i < l; i++) {
            stringBuffer.append(images.get(i).getPath()).append("\n");
        }
        textView.setText(stringBuffer.toString());
    }


    private void copyImages(List<Image> images){
        if (images == null) return;

        //check and make folder if needed
        File copyDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), LoginActivity.DJP_COPIED_DIR);
        if(!copyDir.exists()){
            copyDir.mkdirs();
        }
        OutputStream out;
        InputStream in;
        for(int i = 0; i < images.size(); i++){
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

            }
            catch(IOException e){
                e.printStackTrace();
                Log.e("CopyPic", "Can't create new File");
            }
        }

    }

    private void openCamera(View view){
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), LoginActivity.DJP_DIR + File.separator + getImageName());
        Uri outPutfileUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".fileprovider", file);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outPutfileUri);
        captureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(captureIntent, RC_CAMERA);

    }

    private String getImageName(){
        String mCurrentPhotoPath;
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        return imageFileName;
    }


}
