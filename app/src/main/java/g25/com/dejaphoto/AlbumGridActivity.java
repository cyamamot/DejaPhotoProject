package g25.com.dejaphoto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/**
 * Created by angelazhang on 6/7/17.
 */

public class AlbumGridActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        // Get the album from the intent
        Intent intent = getIntent();
        final String album = intent.getExtras().getString("album");

        // Set up the adapter to display the album photos
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new GridImageAdapter(this, album));

        // Create onClickListener to go to the ChangeLocation Activity when a photo is clicked
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v, int position, long id){
                // Send intent to SingleViewActivity
                Intent i = new Intent(getApplicationContext(), ChangeLocationActivity.class);
                // Pass image index
                i.putExtra("id", position);
                i.putExtra("album", album);
                startActivity(i);
            }
        });
    }

}
