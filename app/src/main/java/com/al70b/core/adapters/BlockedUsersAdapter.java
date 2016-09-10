package com.al70b.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.Dialogs.DisplayPictureDialog;
import com.al70b.core.activities.user_home_activity_underlying.ChatHandler;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 5/28/2016.
 */
public class BlockedUsersAdapter extends ArrayAdapter<OtherUser> {

    private Context context;
    private List<OtherUser> otherUsers;
    private int layout;
    private CurrentUser currentUser;
    private ChatHandler chatHandler;

    public BlockedUsersAdapter(Context context, int layout,
                               List<OtherUser> otherUsers,
                               CurrentUser currentUser,
                               ChatHandler chatHandler) {
        super(context, layout, otherUsers);

        this.otherUsers = otherUsers;
        this.context = context;
        this.layout = layout;
        this.currentUser = currentUser;
        this.chatHandler = chatHandler;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {

        View row = convertView;
        BlockedUserItemHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layout, parent, false);

            holder = new BlockedUserItemHolder();
            holder.profilePicture = (CircleImageView) row.findViewById(R.id.list_item_blocked_user_profile_image);
            holder.name = (TextView) row.findViewById(R.id.tv_list_item_blocked_user_name);
            holder.status = (CircleImageView) row.findViewById(R.id.list_item_blocked_user_status);
            holder.unblock = (Button) row.findViewById(R.id.btn_list_item_blocked_user_unblock);
            row.setTag(holder);
        } else {
            holder = (BlockedUserItemHolder) row.getTag();
        }

        final OtherUser otherUser = otherUsers.get(position);

        holder.name.setText(otherUser.getName());
        holder.status.setImageResource(otherUser.getOnlineStatus().getResourceID());
        Glide.with(context).load(otherUser.getProfilePictureThumbnailPath())
                .asBitmap()
                .placeholder(R.drawable.avatar)
                .into(holder.profilePicture);


        holder.unblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatHandler.unblockUser(otherUser);
            }
        });

        return row;
    }

    private class BlockedUserItemHolder {
        TextView name;
        CircleImageView profilePicture;
        Button unblock;
        CircleImageView status;
    }
}
