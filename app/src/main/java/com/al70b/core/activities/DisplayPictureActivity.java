package com.al70b.core.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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


    private DisplayPictureRetainedFragment dataFragment;

    private PhotoViewAttacher mAttacher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_picture);

        // get picture object
        final Picture pic = (Picture) getIntent().getSerializableExtra("DisplayPicture.image");

        final ImageView imgView = (ImageView) findViewById(R.id.img_view_displayPictureA);


        // download thumbnail
        /*Glide.with(this).
                load(pic.getThumbnailFullPath()).
                into(imgView);*/

        Button btn = (Button) findViewById(R.id.btn_displayPictureA_close);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish this activity
                finish();
            }
        });

        mAttacher = new PhotoViewAttacher(imgView);

        dataFragment = (DisplayPictureRetainedFragment) getFragmentManager().findFragmentByTag("DisplayPicture.data");

        // if first time created
        if (dataFragment == null) {
            dataFragment = new DisplayPictureRetainedFragment();
            getFragmentManager().beginTransaction()
                    .add(dataFragment, "DisplayPicture.data")
                    .commit();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Bitmap bitmap = Glide.with(getApplicationContext()).
                                load(pic.getPictureFullPath()).
                                asBitmap().
                                into(-1, -1).
                                get();

                        // set image and refresh
                        imgView.post(new Runnable() {
                            @Override
                            public void run() {
                                imgView.setImageBitmap(bitmap);
                                imgView.invalidate();
                                mAttacher.update();
                            }
                        });

                        // save bitmap in retained fragment
                        dataFragment.setData(bitmap);
                    } catch (Exception ex) {
                    }
                }
            }).start();


        } else {
            Bitmap bitmap = dataFragment.getData();

            if (bitmap != null) {
                imgView.setImageBitmap(bitmap);
                mAttacher.update();
            }
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
