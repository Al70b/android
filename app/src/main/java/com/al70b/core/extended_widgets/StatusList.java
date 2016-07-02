package com.al70b.core.extended_widgets;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.al70b.R;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.User.OnlineStatus;
import com.inscripts.keys.StatusOption;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 5/16/2015.
 */
public class StatusList extends LinearLayout {

    private final int LEFT = 0, CENTER = 1, RIGHT = 2;
    private final int CURRENT_STATUS_INDEX = RIGHT;
    private final OnlineStatus[] STATUES = new OnlineStatus[3];
    private OnlineStatus currentStatus;

    private CircleImageView imageRightSet, imageCenter, imageLeft;
    private Animation goVisibleAnimation, goInvisibleAnimation;

    private boolean listExpanded;
    private Pair<Integer, Integer> lastUpdate = new Pair<Integer, Integer>(-1, -1);

    public StatusList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    public StatusList(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public StatusList(Context context) {
        super(context);
        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.status_list, this, true);

        imageRightSet = (CircleImageView) v.findViewById(R.id.status_list_right);
        imageCenter = (CircleImageView) v.findViewById(R.id.status_list_center);
        imageLeft = (CircleImageView) v.findViewById(R.id.status_list_left);

        imageRightSet.setTag("right");
        imageCenter.setTag("center");
        imageLeft.setTag("left");
        STATUES[LEFT] = new OnlineStatus(StatusOption.OFFLINE);
        STATUES[CENTER] = new OnlineStatus(StatusOption.BUSY);
        STATUES[RIGHT] = (currentStatus = new OnlineStatus(StatusOption.AVAILABLE));

        // create animation
        goVisibleAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.go_visible);
        goInvisibleAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.go_invisible);

        imageRightSet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                if (isListExpanded()) {
                    // hide status list
                    hideGradually();

                    // remove all post delayed for the count starts from zero again
                    handler.removeCallbacksAndMessages(null);
                } else if (isEnabled()) {
                    // show status list
                    showGradually();

                    // create a delayed job for closing the status list if it
                    // is still open after 5 seconds
                    handler.postDelayed(new Runnable() {

                        public void run() {
                            // check if after 5 seconds the currentUser didn't choose a status and close
                            // status list if he didn't
                            if (isListExpanded())
                                hideGradually();
                        }
                    }, 5 * 1000);
                }
            }
        });
    }


    public void showGradually() {
        imageCenter.setVisibility(View.VISIBLE);
        imageCenter.startAnimation(goVisibleAnimation);

        imageLeft.setVisibility(View.VISIBLE);
        imageLeft.startAnimation(goVisibleAnimation);

        listExpanded = true;
    }

    public void hideGradually() {
        imageCenter.startAnimation(goInvisibleAnimation);
        imageCenter.setVisibility(View.INVISIBLE);

        imageLeft.startAnimation(goInvisibleAnimation);
        imageLeft.setVisibility(View.INVISIBLE);
        listExpanded = false;
    }

    public boolean isListExpanded() {
        return listExpanded;
    }


    public void updateStatus(String s) {
        OnlineStatus status;
        if (s.compareTo("left") == 0) {
            status = STATUES[LEFT];
            STATUES[LEFT] = currentStatus;
            lastUpdate.first = LEFT;
        } else if (s.compareTo("center") == 0) {
            status = STATUES[CENTER];
            STATUES[CENTER] = currentStatus;
            lastUpdate.first = CENTER;
        } else {
            status = STATUES[RIGHT];
            lastUpdate.first = RIGHT;
        }

        currentStatus = status;
        STATUES[CURRENT_STATUS_INDEX] = currentStatus;
        lastUpdate.second = CURRENT_STATUS_INDEX;

        updateStatusListViews();
    }

    public void backtrack() {
        if (lastUpdate.first != -1 && lastUpdate.second != -1) {
            swap(lastUpdate.first, lastUpdate.second);
            updateStatusListViews();
            lastUpdate.first = -1;
            lastUpdate.second = -1;
        }
    }

    public OnlineStatus getCurrentStatus() {
        return currentStatus;
    }

    public CircleImageView getVisibleStatusView() {
        return imageRightSet;
    }

    public boolean updateStatusListViews() {
        imageRightSet.setImageResource(STATUES[CURRENT_STATUS_INDEX].getResourceID());
        imageCenter.setImageResource(STATUES[CENTER].getResourceID());
        imageLeft.setImageResource(STATUES[LEFT].getResourceID());

        return true;
    }

    private void swap(int idx1, int idx2) {
        OnlineStatus temp = STATUES[idx2].duplicate();
        STATUES[idx2] = STATUES[idx1];
        STATUES[idx1] = temp;

        if (idx1 == CURRENT_STATUS_INDEX)
            currentStatus = STATUES[idx1];

        if (idx2 == CURRENT_STATUS_INDEX)
            currentStatus = STATUES[idx2];
    }




}
