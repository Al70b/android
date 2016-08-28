package com.al70b.core.adapters;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.objects.NavDrawerItem;

import org.w3c.dom.Text;

/**
 * Created by Naseem on 6/20/2016.
 */
public class NavigationDrawerAdapter extends ArrayAdapter<NavDrawerItem> {

    private UserHomeActivity activity;
    private LayoutInflater inflater;
    private NavDrawerItem[] data;

    private NavDrawerItem highlightedItem;

    public NavigationDrawerAdapter(UserHomeActivity activity, NavDrawerItem[] data) {
        super(activity, -1, data);
        this.activity = activity;
        this.data = data;
        inflater = activity.getLayoutInflater();

        // inner use
        highlightedItem = null;
        itemsArrayHolder = new DrawerItemHolder[data.length];
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public NavDrawerItem getItem(int position) {
        return data[position];
    }

    private DrawerItemHolder[] itemsArrayHolder;

    @Override
    public View getView(int position, View row, ViewGroup parent) {

        DrawerItemHolder holder;

        NavDrawerItem item = data[position];

        if (row == null) {
            holder = new DrawerItemHolder();

            if (item.isSection()) {
                row = inflater.inflate(R.layout.list_item_navigation_drawer_list_section, parent, false);
                holder.txtTitle = (TextView) row.findViewById(R.id.text_view_drawer_list_section);
                row.setOnClickListener(null);
            } else {
                row = inflater.inflate(R.layout.list_item_navigation_drawer, parent, false);
                holder.imgIcon = (ImageView) row.findViewById(R.id.image_view_navigation_drawer_list);
                holder.txtTitle = (TextView) row.findViewById(R.id.text_view_drawer_list);
                holder.txtSubtext = (TextView) row.findViewById(R.id.text_view_drawer_list_subtext);
            }

            row.setOnLongClickListener(null);
            row.setLongClickable(false);
            itemsArrayHolder[position] = holder;
        } else {
            holder = itemsArrayHolder[position];
        }

        if (item.isSection()) {
            holder.txtTitle.setText(item.getTitle());
        } else {
            updateItemStyle(holder, item);
        }

        if(item.hasSubtext()) {
            holder.txtSubtext.setText(item.getSubtext());
            holder.txtSubtext.setVisibility(View.VISIBLE);
        }

        return row;
    }

    private void updateItemStyle(DrawerItemHolder holder, NavDrawerItem navDrawerItem) {
        holder.txtTitle.setText(navDrawerItem.getTitle());

        if (navDrawerItem.isHighlighted()) {
            if(highlightedItem != null) {
                // set previous highlighted to false
                highlightedItem.setHighlighted(false);
            }

            // this is the new highlighted item
            highlightedItem = navDrawerItem;

            // update color and icon
            if(holder.txtTitle != null) {
                holder.txtTitle.setTextColor(getColor(R.color.white));
                holder.txtSubtext.setTextColor(getColor(R.color.white));
            }

            if(holder.imgIcon != null) {
                holder.imgIcon.setImageResource(navDrawerItem.getIconFocused());
            }
        } else {
            if (holder.txtTitle != null) {
                holder.txtTitle.setTextColor(getColorStateList(R.color.selector_list_item_text_color));
                holder.txtSubtext.setTextColor(getColor(R.color.selector_list_item_text_color));
            }

            if(holder.imgIcon != null) {
                // create states
                StateListDrawable states = new StateListDrawable();
                states.addState(new int[]{android.R.attr.state_pressed},
                        getDrawable(navDrawerItem.getIconFocused()));
                states.addState(new int[]{android.R.attr.state_focused},
                        getDrawable(navDrawerItem.getIconFocused()));
                states.addState(new int[]{}, getDrawable(navDrawerItem.getIcon()));

                holder.imgIcon.setImageDrawable(states);
            }
        }
    }

    private int getColor(int color) {
        return ContextCompat.getColor(activity.getApplicationContext(), color);
    }

    private ColorStateList getColorStateList(int color) {
        return ContextCompat.getColorStateList(activity.getApplicationContext(), color);
    }

    private Drawable getDrawable(int drawable) {
        return ContextCompat.getDrawable(activity.getApplicationContext(), drawable);
    }

    class DrawerItemHolder {
        ImageView imgIcon;
        TextView txtTitle, txtSubtext;
    }
}
