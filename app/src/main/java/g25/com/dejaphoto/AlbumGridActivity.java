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

        Activity activity = AlbumGridActivity.this;
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new GridImageAdapter(activity));

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(AlbumGridActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
