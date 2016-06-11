package com.al70b.core.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;

/**
 * Created by Naseem on 7/7/2015.
 */
public class DisplayPictureRetainedFragment extends Fragment {

    private Bitmap picture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public Bitmap getData() {
        return picture;
    }

    public void setData(Bitmap data) {
        this.picture = data;
    }


}
