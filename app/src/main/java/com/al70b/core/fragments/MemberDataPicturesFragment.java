package com.al70b.core.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.al70b.R;
import com.al70b.core.activities.DisplayPictureActivity;
import com.al70b.core.activities.MemberProfileActivity;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Picture;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 6/18/2015.
 */
public class MemberDataPicturesFragment extends Fragment {

    // grid view and image adapter;
    private ImageAdapter adapter;

    private List<Picture> listOfPictures;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        OtherUser otherUser = (OtherUser) bundle.getSerializable(MemberProfileActivity.OTHER_USER);

        if (otherUser != null) {
            // get user photos
            listOfPictures = otherUser.getPicturesList();
        } else
            listOfPictures = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_member_data_pictures, container, false);
        GridView gridView = (GridView) viewGroup.findViewById(R.id.grid_view_member_pictures);

        // create adapter and set it to grid view
        adapter = new ImageAdapter(getActivity(), listOfPictures);

        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Picture pic = (Picture) adapter.getItem(i);

                Intent intent = new Intent(getActivity(), DisplayPictureActivity.class);
                intent.putExtra("DisplayPicture.image", pic);
                startActivity(intent);
            }
        });


        return viewGroup;
    }


    private class ImageAdapter extends BaseAdapter {
        private Context context;
        private List<Picture> listOfPictures;
        private int dp110;

        public ImageAdapter(Context context, List<Picture> listOfPictures) {
            this.listOfPictures = listOfPictures;
            this.context = context.getApplicationContext();
            dp110 = (int) (110 * context.getResources().getDisplayMetrics().density);
        }

        public int getCount() {
            return listOfPictures.size();
        }

        public Object getItem(int position) {
            return listOfPictures.get(position);
        }

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

            Glide.with(getActivity())
                    .load(listOfPictures.get(position).getThumbnailFullPath())
                    .override(dp110, dp110)
                    .centerCrop()
                    .placeholder(R.drawable.avatar)
                    .into(imageView);

            return imageView;
        }
    }
}
