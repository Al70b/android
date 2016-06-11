package com.al70b.core.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Naseem on 4/27/2015.
 */
public class WelcomeFragment extends Fragment {

    private static final int NUMBER_OF_PICTURES_TO_SHOW = 10;
    private static final int DEFAULT_DURATION = 10; // 250 milliseconds
    private static final int DEFAULT_INTERVAL = 5; // pixels to jump
    private static LinearLayout layout;
    private static int dpToPx9;
    private static int dpTopx80;
    private static Activity thisActivity;
    private HorizontalScrollView horizontalScrollView;
    private Handler handler;

    public static void addImageViewToLayout(final Bitmap bitmap) {
        layout.post(new Runnable() {
            @Override
            public void run() {
                layout.addView(createImageView(bitmap));
            }
        });
    }

    private static ImageView createImageView(Bitmap bitmap) {
        ImageView imgView = new ImageView(thisActivity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpTopx80, dpTopx80);
        params.leftMargin = dpToPx9;
        params.rightMargin = dpToPx9 / 3;
        params.topMargin = dpToPx9;
        params.bottomMargin = dpToPx9;
        imgView.setLayoutParams(params);
        imgView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgView.setImageBitmap(bitmap);
        return imgView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        thisActivity = getActivity();

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_welcome, container, false);
        layout = (LinearLayout) viewGroup.findViewById(R.id.layout_fragment_welcome_images2);
        horizontalScrollView = (HorizontalScrollView) viewGroup.findViewById(R.id.horizontal_scrollview_fragment_welcome);
        dpToPx9 = (int) (9 * getActivity().getResources().getDisplayMetrics().density);
        dpTopx80 = (int) (80 * getActivity().getResources().getDisplayMetrics().density);
        TextView tvTitle = (TextView) viewGroup.findViewById(R.id.textViewWelcomeMessage);
        TextView tvDescription = (TextView) viewGroup.findViewById(R.id.textViewWelcomeMessage2);
        TextView tvExtra = (TextView) viewGroup.findViewById(R.id.text_view_fragment_welcome_users_photos);

        Typeface typefaceReg = Typeface.createFromAsset(getActivity().getAssets(), "fonts/DroidNaskh-Regular.ttf");
        Typeface typefaceBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/DroidNaskh-Bold.ttf");

        tvTitle.setTypeface(typefaceBold);
        tvDescription.setTypeface(typefaceReg);
        tvExtra.setTypeface(typefaceReg);

        //horizontalScrollView.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadImages();
            }
        }).start();


        return viewGroup;
    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);

        handler = new Handler();


        //new Thread(new DirectedRunnable(DEFAULT_INTERVAL)).start();
    }

    @Override
    public void onStop() {
        super.onStop();

        // remove all callbacks
        handler.removeCallbacks(null);
    }

    public void downloadImages() {
        final RequestsInterface requestsInterface = new RequestsInterface(getActivity().getApplicationContext());

        try {
            ServerResponse<List<String>> sr = requestsInterface.getUsersStatic(NUMBER_OF_PICTURES_TO_SHOW);

            if (sr.isSuccess()) {
                final List<String> list = sr.getResult();

                for (String s : list) {
                    try {
                        addImageViewToLayout(Glide.with(getActivity()).load(s).
                                asBitmap().
                                into(-1, -1).
                                get());

                        horizontalScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                horizontalScrollView.invalidate();
                            }
                        });
                    } catch (Exception ex) {
                    }
                }
            } else {

            }
        } catch (ServerResponseFailedException ex) {
        }
    }

    private class DirectedRunnable implements Runnable {
        int direction;

        public DirectedRunnable(int direction) {
            this.direction = direction;
        }

        @Override
        public void run() {
            if (horizontalScrollView.canScrollHorizontally(direction))
                something(false);
            else {
                something(true);
            }
        }

        public void something(final boolean changeDirection) {
            horizontalScrollView.postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                    }

                    if (!changeDirection)
                        horizontalScrollView.scrollBy(direction, 0);
                    else
                        handler.postDelayed(new DirectedRunnable(-1 * direction), DEFAULT_DURATION);

                    horizontalScrollView.postDelayed(this, DEFAULT_DURATION);
                }
            });
        }
    }


}

