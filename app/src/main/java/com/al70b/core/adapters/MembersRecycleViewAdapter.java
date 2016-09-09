package com.al70b.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.Dialogs.DisplayPictureDialog;
import com.al70b.core.activities.Dialogs.SendMessageDialog;
import com.al70b.core.extended_widgets.LoadMoreRecyclerView;
import com.al70b.core.misc.Utils;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.FriendButtonHandler;
import com.al70b.core.objects.OtherUser;
import com.bumptech.glide.Glide;
import com.al70b.core.extended_widgets.LoadMoreRecyclerView.OnItemClickListener;

import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by Naseem on 8/3/2015.
 */
public class MembersRecycleViewAdapter extends LoadMoreRecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_ITEM = 0;

    private Context context;
    private List<OtherUser> data;
    private CurrentUser currentUser;
    private OnItemClickListener onItemClickListener;

    private boolean isLoading = false;

    public MembersRecycleViewAdapter(Context context, List<OtherUser> data, CurrentUser currentUser) {
        super(context, data);
        this.context = context;
        this.data = data;
        this.currentUser = currentUser;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder
            implements LoadMoreRecyclerView.OnClickListener {
        TextView name, age, address;
        ImageView profilePicture, status;
        ImageView imgViewMessage, imgViewFriendRequest;
        ProgressBar pb;

        public ItemViewHolder(View row) {
            super(row);

            profilePicture = (ImageView) row.findViewById(R.id.imv_view_member_item_profile);
            status = (CircleImageView) row.findViewById(R.id.circle_image_member_item_status);
            name = (TextView) row.findViewById(R.id.text_view_member_item_name);
            age = (TextView) row.findViewById(R.id.text_view_member_item_age);
            address = (TextView) row.findViewById(R.id.text_view_member_item_address);
            imgViewFriendRequest = (ImageView) row.findViewById(R.id.image_view_members_item_add);
            imgViewMessage = (ImageView) row.findViewById(R.id.image_view_members_item_send);
            pb = (ProgressBar) row.findViewById(R.id.progress_bar_sending_friend_request);

            row.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }



    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        int type = super.getItemViewType(position);

        if(type < 0) {
            type = VIEW_TYPE_ITEM;
        }
        return type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        RecyclerView.ViewHolder vh = super.onCreateViewHolder(parent, viewType);

        if(vh != null) {
            return vh;
        }

        View v;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_item_member, parent, false);
                vh = new ItemViewHolder(v);
                ItemViewHolder itemViewHolder = (ItemViewHolder)vh;

                int x = context.getResources().getDisplayMetrics().widthPixels;
                x = (x / 2);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(x, x);
                itemViewHolder.profilePicture.setLayoutParams(lp);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if(holder instanceof ItemViewHolder) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder)holder;
            final OtherUser otherUser = data.get(position);
            final OtherUser.FriendStatus friendStatus = otherUser.getFriendStatus();

            itemViewHolder.name.setText(otherUser.getName());
            itemViewHolder.age.setText(context.getApplicationContext().getString(R.string.age_of, calculateAge(otherUser.getDateOfBirth())));
            itemViewHolder.address.setText(otherUser.getAddress().toString());

            Glide.with(context).load(otherUser.getProfilePictureThumbnailPath())
                    .asBitmap()
                    .placeholder(R.drawable.avatar)
                    .into(itemViewHolder.profilePicture);

            /*itemViewHolder.profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!otherUser.isProfilePictureSet()) {
                        return;
                    }

                    DisplayPictureDialog dialog = new DisplayPictureDialog(context,
                            otherUser.getProfilePictureThumbnailPath());
                    dialog.show();
                }
            });*/

            itemViewHolder.status.setImageResource(otherUser.getOnlineStatus().getResourceID());

            itemViewHolder.imgViewMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendMessageDialog alert = new SendMessageDialog(context, otherUser);
                    alert.setCanceledOnTouchOutside(false);
                    alert.show();
                }
            });

            itemViewHolder.imgViewFriendRequest.setImageResource(friendStatus.getDrawableResourceID());

            itemViewHolder.imgViewFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final FriendButtonHandler friendHandler = new FriendButtonHandler();

                    itemViewHolder.pb.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            friendHandler.handle((Activity) context, currentUser, otherUser);

                            itemViewHolder.imgViewFriendRequest.post(new Runnable() {
                                @Override
                                public void run() {
                                    itemViewHolder.pb.setVisibility(View.INVISIBLE);
                                    itemViewHolder.imgViewFriendRequest
                                            .setImageResource(otherUser.getFriendStatus().getDrawableResourceID());
                                }
                            });
                        }
                    }).start();
                }
            });

            itemViewHolder.imgViewFriendRequest.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(context.getApplicationContext(), otherUser.getFriendStatus().getStringResourceID(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // calculate user's age to display in the result
    private int calculateAge(Calendar c) {
        return Calendar.getInstance().get(Calendar.YEAR) - c.get(Calendar.YEAR);
    }
}
