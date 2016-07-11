package com.al70b.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.objects.FriendsDrawerItem;
import com.al70b.core.objects.User;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 6/20/2016.
 */

public class FriendsAndChatDrawerAdapter extends ArrayAdapter<FriendsDrawerItem>
        implements Filterable {

    Context context;
    int layout;
    List<FriendsDrawerItem> data;
    //FriendsFilter friendsFilter;

    public FriendsAndChatDrawerAdapter(Context context, int layout, List<FriendsDrawerItem> data) {
        super(context, layout, data);
        this.context = context;
        this.layout = layout;
        this.data = data;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DrawerItemHolder holder;

        if (row == null) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layout, parent, false);

            holder = new DrawerItemHolder();
            holder.profilePicture = (CircleImageView) row.findViewById(R.id.left_drawer_profile_picture);
            holder.txtName = (TextView) row.findViewById(R.id.text_view_drawer_left_list_name);
            holder.txtStatusMessage = (TextView) row.findViewById(R.id.text_view_drawer_left_list_status_message);
            holder.friendStatus = (CircleImageView) row.findViewById(R.id.left_drawer_status);

            row.setTag(holder);
        } else {
            holder = (DrawerItemHolder) row.getTag();
        }


        FriendsDrawerItem friendsDrawerItem = data.get(position);
        holder.txtName.setText(friendsDrawerItem.name);
        holder.txtStatusMessage.setText(friendsDrawerItem.statusMessage);

        // set background color to highlighted
        if (friendsDrawerItem.unreadMessage)
            row.setBackgroundColor(context.getResources().getColor(R.color.highlighted_yellow));
        else
            row.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));

        Glide.with(context)
                .load(friendsDrawerItem.profilePicture)
                .asBitmap()
                .fitCenter()
                .centerCrop()
                .placeholder(R.drawable.avatar)
                .into(holder.profilePicture);

        User.OnlineStatus status = new User.OnlineStatus(friendsDrawerItem.status);
        holder.friendStatus.setImageResource(status.getResourceID());

        return row;
    }

    @Override
    public int getCount() {
        return data.size();
    }

   /* @Override
    public Filter getFilter() {
        if (friendsFilter == null)
            friendsFilter = new FriendsFilter();

        return friendsFilter;
    }*/

    public List<FriendsDrawerItem> getData() {
        return data;
    }

    public void setData(List<FriendsDrawerItem> newData) {
        this.data = newData;
    }
/*
    @Override
    public void notifyDataSetChanged() {
        if (onlineFriends.size() == 0) {
            txtViewFriendEmpty.setVisibility(View.VISIBLE);
            searchFriendEditText.setVisibility(View.GONE);
        } else {
            txtViewFriendEmpty.setVisibility(View.GONE);
            searchFriendEditText.setVisibility(View.VISIBLE);
        }

        super.notifyDataSetChanged();
    }
*/
    class DrawerItemHolder {
        CircleImageView profilePicture, friendStatus;
        TextView txtName, txtStatusMessage;
    }
/*
    private class FriendsFilter extends Filter {

        public FriendsFilter() {
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            data = (List<FriendsDrawerItem>) results.values;
            notifyDataSetChanged();

        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = onlineFriends;
                results.count = onlineFriends.size();
            } else {
                List<FriendsDrawerItem> mList = new ArrayList<>();

                for (FriendsDrawerItem item : data) {
                    if (item.name.toUpperCase().contains(constraint.toString().toUpperCase()))
                        mList.add(item);
                }

                results.values = mList;
                results.count = mList.size();
            }

            return results;
        }
    }

    // handle thisUser's change status
    public void changeStatus(View view) {
        final CircleImageView civ = ((CircleImageView) view);

        // get the new clicked status
        statusList.updateStatus(civ.getTag().toString());

        final StatusOption so = statusList.getCurrentStatus().getStatus();

        // hide status list
        statusList.hideGradually();

        cometChat.setStatus(so, new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {
                thisUser.setOnlineStatus(so);
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
                statusList.backtrack();
                Toast.makeText(getApplicationContext(), getString(R.string.error_changing_status), Toast.LENGTH_LONG).show();
            }
        });
    }*/

    /*
    populating online friends list
 *//*
    private void populateFriendsList(JSONObject onlineUsers) {
        try {
            if (null != onlineFriends && null != friendsDrawerAdapter) {
                Iterator<String> keys = onlineUsers.keys();

                onlineFriends.clear();
                while (keys.hasNext()) {
                    JSONObject user = onlineUsers.getJSONObject(keys.next());
                    String username = user.getString("n");

                    long timestamp;
                    if (user.isNull("timestamp"))
                        timestamp = 0;
                    else
                        timestamp = user.getLong("timestamp");

                    String thumbnailPath = ServerConstants.SERVER_FULL_URL + user.getString("a");

                    final FriendsDrawerItem item = new FriendsDrawerItem(user.getInt("id"), timestamp, username, thumbnailPath, user.getString                                      ("s"), user.getString("m"));

                    if (item.status.compareTo("offline") == 0)
                        continue;

                    onlineFriends.add(item);
                    if (unreadMessagesUsersIDs.indexOfKey(user.getInt("id")) > -1) {
                        item.unreadMessage = true;
                    }
                }
                Collections.sort(onlineFriends);
                friendsDrawerAdapter.notifyDataSetChanged();
            }
        } catch (JSONException ex) {

        }

        if (layoutChatContacts.getVisibility() == View.GONE) {
            layoutChatFailed.setVisibility(View.GONE);
            layoutChatContacts.setVisibility(View.VISIBLE);
        }
    }*/
}
