package com.al70b.core.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.objects.FriendsDrawerItem;
import com.al70b.core.objects.User;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
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
    FriendsFilter friendsFilter;

    public FriendsAndChatDrawerAdapter(Context context, int layout, List<FriendsDrawerItem> data) {
        super(context, layout, data);
        this.context = context;
        this.layout = layout;
        this.data = data;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DrawerItemHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layout, parent, false);

            holder = new DrawerItemHolder();
            holder.profilePicture = (CircleImageView) convertView.findViewById(R.id.left_drawer_profile_picture);
            holder.txtName = (TextView) convertView.findViewById(R.id.text_view_drawer_left_list_name);
            holder.txtStatusMessage = (TextView) convertView.findViewById(R.id.text_view_drawer_left_list_status_message);
            holder.friendStatus = (CircleImageView) convertView.findViewById(R.id.left_drawer_status);

            convertView.setTag(holder);
        } else {
            holder = (DrawerItemHolder) convertView.getTag();
        }

        FriendsDrawerItem friendsDrawerItem = data.get(position);
        holder.txtName.setText(friendsDrawerItem.name);
        holder.txtStatusMessage.setText(friendsDrawerItem.statusMessage);

        // set background color to highlighted
        if (friendsDrawerItem.isMessageUnread) {
            convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.highlighted_yellow));
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        Glide.with(context)
                .load(friendsDrawerItem.profilePicture)
                .asBitmap()
                .fitCenter()
                .centerCrop()
                .placeholder(R.drawable.avatar)
                .into(holder.profilePicture);

        User.OnlineStatus status = new User.OnlineStatus(friendsDrawerItem.status);
        holder.friendStatus.setImageResource(status.getResourceID());

        return convertView;
    }

    @Override
    public int getCount() {
        return data.size();
    }

   @Override
    public Filter getFilter() {
        if (friendsFilter == null)
            friendsFilter = new FriendsFilter();

        return friendsFilter;
    }

    /*
    *   Returns the relevant Friends drawer item, if not exists returns null
    * */
    public FriendsDrawerItem getItemByUserID(int userID) {
        for(FriendsDrawerItem item : data) {
            if(item.id == userID) {
                return item;
            }
        }

        return null;
    }

    public List<FriendsDrawerItem> getData() {
        return data;
    }

    public void setData(List<FriendsDrawerItem> newData) {
        this.data = newData;
    }

    class DrawerItemHolder {
        CircleImageView profilePicture, friendStatus;
        TextView txtName, txtStatusMessage;
    }

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
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = data;
                results.count = data.size();
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
}
