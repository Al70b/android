package com.al70b.core.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.fragments.DisplayPictureRetainedFragment;
import com.al70b.core.objects.Picture;
import com.bumptech.glide.Glide;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Naseem on 7/11/2015.
 */
public class DisplayPictureActivity extends Activity {

    private static final String TAG = "DisplayPictureActivity";
    public static final String THUMBNAIL_KEY = "THUMBNAIL";
    public static final String FULL_PICTURE_KEY = "FULL_PICTURE";
    public static final String DISPLAY_PICTURE_DATA = "DISPLAY_PICTURE_DATA";


    private DisplayPictureRetainedFragment dataFragment;

    private PhotoViewAttacher mAttacher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_picture);

        // get picture object
        Intent intent = getIntent();

        if(intent == null) {
            return;
        }

        final String thumbnailPath = intent.getStringExtra(THUMBNAIL_KEY);
        final String fullPicturePath = intent.getStringExtra(FULL_PICTURE_KEY);

        final ImageView imgView = (ImageView) findViewById(R.id.img_view_displayPictureA);
        final ProgressBar progressBarLoading = (ProgressBar) findViewById(R.id.progress_bar_loading_full_picture);

        Button btnCloseActivity = (Button) findViewById(R.id.btn_displayPictureA_close);
        btnCloseActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish this activity
                finish();
            }
        });

        mAttacher = new PhotoViewAttacher(imgView);

        dataFragment = (DisplayPictureRetainedFragment) getFragmentManager()
                .findFragmentByTag(DISPLAY_PICTURE_DATA);

        // if first time created
        if (dataFragment == null) {
            dataFragment = new DisplayPictureRetainedFragment();
            getFragmentManager().beginTransaction()
                    .add(dataFragment, DISPLAY_PICTURE_DATA)
                    .commit();

            if(thumbnailPath != null) {
                Glide.with(this)
                        .load(thumbnailPath)
                        .into(imgView);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Bitmap bitmap = Glide.with(getApplicationContext()).
                                load(fullPicturePath)
                                .asBitmap()
                                .into(-1, -1)
                                .get();

                        // set image and refresh
                        imgView.post(new Runnable() {
                            @Override
                            public void run() {
                                imgView.setImageBitmap(bitmap);
                                imgView.invalidate();
                                mAttacher.update();

                                progressBarLoading.setVisibility(View.GONE);
                            }
                        });

                        // save bitmap in retained fragment
                        dataFragment.setData(bitmap);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                }
            }).start();
        } else {
            Bitmap bitmap = dataFragment.getData();

            if (bitmap != null) {
                imgView.setImageBitmap(bitmap);
                mAttacher.update();
            }

            progressBarLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onStop() {
        super.onStop();

        ((MyApplication) getApplication()).setAppInvisible();
    }
}
