package com.al70b.core.extended_widgets;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.al70b.R;
import com.al70b.core.objects.User;
import com.al70b.core.objects.User.OnlineStatus;
import com.inscripts.enums.StatusOption;


import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 5/16/2015.
 */
public class StatusList extends LinearLayout{

    private static final int TIME_TO_AUTO_COLLAPSE = 5 * 1000; // 5 seconds

    private OnlineStatus currentStatus;
    private Map<StatusOption, OnlineStatus> STATUES_MAPPED_SET;

    // This is the fixed circle image view
    private CircleImageView fixedCircleImageView;

    private Animation goVisibleAnimation, goInvisibleAnimation;

    private boolean listExpanded;

    private Handler handler;

    // declare events
    private OnStatusListEnabledChangeEvent onStatusListEnabledChangeEvent;
    private OnStatusChangeEvent onStatusChangeEvent;

    public StatusList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public StatusList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusList(Context context) {
        super(context);
        init();
    }


    private void init() {
        setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        setPadding(2, 2, 2, 2);

        STATUES_MAPPED_SET = new HashMap<>();

        handler = new Handler();

        buildOnlineStatusSet();
        createImageViewsFromSet();

        // create animation
        goVisibleAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.go_visible);
        goInvisibleAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.go_invisible);

        fixedCircleImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    }, TIME_TO_AUTO_COLLAPSE);
                }
            }
        });
    }


    private void buildOnlineStatusSet() {
        StatusOption[] statusOptions = StatusOption.values();

        for (StatusOption s : statusOptions) {
            STATUES_MAPPED_SET.put(s, new OnlineStatus(s));
        }
    }

    private void createImageViewsFromSet() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        // Go over each status and create image view, add it to this layout
        for (OnlineStatus status : STATUES_MAPPED_SET.values()) {
            CircleImageView v = (CircleImageView) inflater.inflate(
                    R.layout.status_widget, this, false);
            v.setTag(status.getStatus());
            v.setImageResource(status.getResourceID());
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateStatus((StatusOption)v.getTag());
                    hideGradually();
                }
            });
            v.setVisibility(View.INVISIBLE);

            // add view to this layout
            addView(v);

            // set first view as the fixed circle image
            if (fixedCircleImageView == null) {
                fixedCircleImageView = v;
                currentStatus = status;
                v.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showGradually() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);

            if (v != fixedCircleImageView) {
                v.setVisibility(View.VISIBLE);
                v.startAnimation(goVisibleAnimation);
            }
        }
        listExpanded = true;
    }

    private void hideGradually() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);

            if (v != fixedCircleImageView) {
                v.startAnimation(goInvisibleAnimation);
                v.setVisibility(View.INVISIBLE);
            }
        }
        listExpanded = false;
    }

    public boolean updateStatus(StatusOption so) {
        handler.removeCallbacksAndMessages(null);

        for (int i = 0; i < getChildCount(); i++) {
            CircleImageView v = (CircleImageView)getChildAt(i);

            if (v.getTag() == so) {
                v.setTag(currentStatus.getStatus());
                v.setImageResource(currentStatus.getResourceID());
                break;
            }
        }

        currentStatus = STATUES_MAPPED_SET.get(so);
        fixedCircleImageView.setTag(currentStatus.getStatus());
        fixedCircleImageView.setImageResource(currentStatus.getResourceID());


        if(onStatusChangeEvent != null) {
            onStatusChangeEvent.onStatusChange(currentStatus);
        }

        return true;
    }

    public boolean updateStatus(String s) {
        StatusOption so = User.OnlineStatus.stringToStatusOption(s);
        return so != null && updateStatus(so);
    }

    public OnlineStatus getCurrentStatus() {
        return currentStatus;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        fixedCircleImageView.setEnabled(enabled);
        fixedCircleImageView.setAlpha(enabled? 1f : 0.5f);

        if(onStatusListEnabledChangeEvent != null) {
            onStatusListEnabledChangeEvent.onStatusListEnabledChange(enabled);
        }
    }

    public boolean isListExpanded() {
        return listExpanded;
    }

    public void setOnStatusListEnabledChangeEvent(OnStatusListEnabledChangeEvent e) {
        this.onStatusListEnabledChangeEvent = e;
    }

    public void setOnStatusChangeEvent(OnStatusChangeEvent e) {
        this.onStatusChangeEvent = e;
    }

    public interface OnStatusListEnabledChangeEvent {
        void onStatusListEnabledChange(boolean b);
    }

    public interface OnStatusChangeEvent {
        void onStatusChange(OnlineStatus onlineStatus);
    }
}
