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

public class FriendsAdapter extends ArrayAdapter<String>{
    public FriendsAdapter(Context context, List<String> friendRequests) {
        super(context, android.R.layout.simple_list_item_1, friendRequests);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String email = getItem(position);

        // Inflate the view if it hasn't already been inflated
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend, parent, false);
        }

        // Set the text to the friend's email at that position
        TextView tvFriendEmail = (TextView)convertView.findViewById(R.id.tvFriendEmail);
        tvFriendEmail.setText(email);

        return convertView;
    }

}
