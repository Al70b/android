package com.al70b.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.objects.OtherUser;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 5/28/2016.
 */
public class FriendsListAdapter extends ArrayAdapter<OtherUser> {

    private Context context;

    private List<OtherUser> friendsList;

    private int layout;

    public FriendsListAdapter(Context context, int layout, List<OtherUser> friendsList) {
        super(context, layout, friendsList);

        this.friendsList = friendsList;
        this.context = context;
        this.layout = layout;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        FriendItemHolder holder;

        if (row == null) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layout, parent, false);

            holder = new FriendItemHolder();

            holder.profilePicture = (CircleImageView) row.findViewById(R.id.list_item_friend_profile_image);
            holder.name = (TextView) row.findViewById(R.id.tv_list_item_friend_name);
            holder.address = (TextView) row.findViewById(R.id.tv_list_item_friend_info);
            holder.status = (CircleImageView) row.findViewById(R.id.list_item_friend_profile_status);
            holder.more = (ImageButton) row.findViewById(R.id.img_btn_list_item_friend_more);
            row.setTag(holder);
        } else {
            holder = (FriendItemHolder) row.getTag();
        }

        final OtherUser otherUser = friendsList.get(position);
        final OtherUser.FriendStatus friendStatus = otherUser.getFriendStatus();

        holder.name.setText(otherUser.getName());

        holder.status.setImageResource(otherUser.getOnlineStatus().getResourceID());

        holder.address.setText(otherUser.getAddress().toString());

        Glide.with(context).load(otherUser.getProfilePictureThumbnailPath())
                .asBitmap()
                .placeholder(R.drawable.avatar)
                .into(holder.profilePicture);

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //updateFriendsCounter();
                Toast.makeText(context, "This is more and more", Toast.LENGTH_SHORT).show();
            }
        });

        return row;
    }

    private class FriendItemHolder {
        TextView name, address;
        CircleImageView profilePicture;
        ImageButton more;
        CircleImageView status;
    }
}
