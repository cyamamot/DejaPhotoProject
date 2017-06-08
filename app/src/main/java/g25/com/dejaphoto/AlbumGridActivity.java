package g25.com.dejaphoto;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.widget.Toast;

/**
 * Created by angelazhang on 6/7/17.
 */

public class AlbumGridActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        Intent intent = getIntent();

        Activity activity = AlbumGridActivity.this;
        GridView gridview = (GridView) findViewById(R.id.gridview);
        final String album = intent.getExtras().getString("album");
        gridview.setAdapter(new GridImageAdapter(activity, album));

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
