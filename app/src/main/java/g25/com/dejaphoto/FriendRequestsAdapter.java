package g25.com.dejaphoto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by bigmak712 on 6/5/17.
 */

public class FriendRequestsAdapter extends ArrayAdapter<String>{
    public FriendRequestsAdapter(Context context, List<String> friendRequests) {
        super(context, android.R.layout.simple_list_item_1, friendRequests);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String request = getItem(position);
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_friend_request, parent, false);
        }

        TextView tvFriendRequest = (TextView)convertView.findViewById(R.id.tvFriendRequest);
        //Button btAccept = (Button)convertView.findViewById(R.id.btAccept);
        //Button btDelete = (Button)convertView.findViewById(R.id.btDelete);

        tvFriendRequest.setText(request);

        return convertView;
    }

}
