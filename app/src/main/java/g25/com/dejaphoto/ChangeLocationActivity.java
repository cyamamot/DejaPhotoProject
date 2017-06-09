package g25.com.dejaphoto;

/**
 * Created by angelazhang on 6/8/17.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ChangeLocationActivity extends Activity {

    BackgroundPhoto curr;
    private FirebaseWrapper fbWrapper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeloc);
        fbWrapper = new FirebaseWrapper(this);


        // Get intent data
        Intent i = getIntent();

        // Selected image id
        int position = i.getExtras().getInt("id");
        String album = i.getExtras().getString("album");

        GridImageAdapter imageAdapter = new GridImageAdapter(this, album);

        curr = imageAdapter.itemList.get(position);
        String currName = curr.getCustomLocation();

        TextView tv = (TextView)findViewById(R.id.imageCustomLocation);
        tv.setText(currName);
    }

    /**
     * Description: Save the custom location that the user entered.
     */
    public void saveChange(View view) {
        //change settings
        EditText name = (EditText) findViewById(R.id.editText_cL);
        curr.setCustomLocation(name.getText().toString());

        fbWrapper.addPhotoMetadata(curr);
        Log.d("change location name", curr.getName() + "-->" + curr.getCustomLocation());

        Intent i = new Intent(this, AlbumsActivity.class);
        startActivity(i);
    }
}