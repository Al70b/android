package com.al70b.core.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.al70b.R;
import com.al70b.core.objects.Picture;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Naseem on 8/13/2016.
 */
public class UserImageAdapter extends BaseAdapter {
    private final String TAG = "UserImageAdapter";

    private Context context;
    private List<Picture> listOfPictures;
    private int dp110;

    public UserImageAdapter(Context context, List<Picture> listOfPictures) {
        this.listOfPictures = listOfPictures;
        this.context = context.getApplicationContext();
        dp110 = (int) (110 * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getCount() {
        return listOfPictures.size();
    }

    @Override
    public Object getItem(int position) {
        return listOfPictures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.avatar);
            imageView.setLayoutParams(new GridView.LayoutParams(dp110, dp110));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        Glide.with(context)
                .load(listOfPictures.get(position).getThumbnailFullPath())
                .fitCenter()
                .centerCrop()
                .placeholder(R.drawable.avatar)
                .into(imageView);

        return imageView;
    }

    @Override
    public void notifyDataSetChanged() {
        Log.d(TAG, listOfPictures.size() + "");
        super.notifyDataSetChanged();
    }
}
