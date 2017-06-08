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

public class ChangeLocationActivity extends Activity {

    BackgroundPhoto curr;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeloc);

        // Get intent data
        Intent i = getIntent();

        // Selected image id
        int position = i.getExtras().getInt("id");
        String album = i.getExtras().getString("album");

        GridImageAdapter imageAdapter = new GridImageAdapter(this, album);

        curr = imageAdapter.itemList.get(position);
        String currName = curr.getCustomLocation();

        if (!currName.equals("default")){
            EditText name = (EditText)findViewById(R.id.editText_cL);
            name.setText(currName);
        }

    }

    public void saveChange(View view) {
        //change settings
        EditText name = (EditText) findViewById(R.id.editText_cL);
        curr.setCustomLocation(name.getText().toString());
    }
}