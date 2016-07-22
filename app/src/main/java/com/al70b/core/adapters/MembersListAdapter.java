package com.al70b.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.Dialogs.SendMessageDialog;
import com.al70b.core.misc.StorageOperations;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.FriendButtonHandler;
import com.al70b.core.objects.OtherUser;
import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 8/3/2015.
 */
public class MembersListAdapter extends ArrayAdapter<OtherUser> {

    private Context context;
    private int layout;
    private List<OtherUser> data;
    private CurrentUser currentUser;

    public MembersListAdapter(Context context, int layout, List<OtherUser> data, CurrentUser currentUser) {
        super(context, layout, data);
        this.context = context;
        this.layout = layout;
        this.data = data;
        this.currentUser = currentUser;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        MemberItemHolder holder;

        if (row == null) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layout, parent, false);

            holder = new MemberItemHolder();
            holder.profilePicture = (CircleImageView) row.findViewById(R.id.circle_image_member_item_profile);
            int size = (int) (context.getApplicationContext().getResources().getDimension(R.dimen.medium_member_profile_picture));
            holder.profilePicture.setLayoutParams(new LinearLayout.LayoutParams(size, size));

            holder.status = (CircleImageView) row.findViewById(R.id.circle_image_member_item_status);
            holder.name = (TextView) row.findViewById(R.id.text_view_member_item_name);
            holder.age = (TextView) row.findViewById(R.id.text_view_member_item_age);
            holder.address = (TextView) row.findViewById(R.id.text_view_member_item_address);
            holder.imgViewFriendRequest = (ImageView) row.findViewById(R.id.image_view_members_item_add);
            holder.imgViewMessage = (ImageView) row.findViewById(R.id.image_view_members_item_send);
            holder.pb = (ProgressBar) row.findViewById(R.id.progress_bar_sending_friend_request);
            row.setTag(holder);
        } else {
            holder = (MemberItemHolder) row.getTag();
        }

        final OtherUser otherUser = data.get(position);
        final OtherUser.FriendStatus friendStatus = otherUser.getFriendStatus();

        holder.name.setText(otherUser.getName());
        holder.age.setText(context.getApplicationContext().getString(R.string.age_of, calculateAge(otherUser.getDateOfBirth())));
        holder.address.setText(otherUser.getAddress().toString());

        Glide.with(context).load(otherUser.getProfilePicture().getThumbnailFullPath())
                .asBitmap()
                .placeholder(R.drawable.avatar)
                .into(holder.profilePicture);

        holder.status.setImageResource(otherUser.getOnlineStatus().getResourceID());

        holder.imgViewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessageDialog alert = new SendMessageDialog(context, otherUser);
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            }
        });

        holder.imgViewFriendRequest.setImageResource(friendStatus.getDrawableResourceID());

        final MemberItemHolder tempHolder = holder;
        holder.imgViewFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FriendButtonHandler friendHandler = new FriendButtonHandler();

                tempHolder.pb.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        friendHandler.handle((Activity) context, currentUser, otherUser);

                        tempHolder.imgViewFriendRequest.post(new Runnable() {
                            @Override
                            public void run() {
                                tempHolder.pb.setVisibility(View.INVISIBLE);
                                tempHolder.imgViewFriendRequest
                                        .setImageResource(otherUser.getFriendStatus().getDrawableResourceID());
                            }
                        });
                    }
                }).start();
            }
        });

        holder.imgViewFriendRequest.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(context.getApplicationContext(), otherUser.getFriendStatus().getStringResourceID(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        return row;
    }

    // calculate user's age to display in the result
    private int calculateAge(Calendar c) {
        return Calendar.getInstance().get(Calendar.YEAR) - c.get(Calendar.YEAR);
    }

    private class MemberItemHolder {
        TextView name, age, address;
        CircleImageView profilePicture, status;
        ImageView imgViewMessage, imgViewFriendRequest;
        ProgressBar pb;
    }


}
