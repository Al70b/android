package com.al70b.core.activities.audio_video_call;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.al70b.R;
import com.inscripts.cometchat.sdk.AVChat;
import com.inscripts.interfaces.Callbacks;
import com.inscripts.utils.Logger;

import org.json.JSONObject;

/**
 * Created by Naseem on 6/29/2015.
 */
public class AVChatActivity extends Activity {

    public static AVChatActivity thisActivity;
    private ImageButton imgBtnEndCall, imgBtnAudio, imgBtnVideo, imgBtnSwitchCamera;
    private String friendId;
    private boolean isAudioOn = true, isVideoOn = true, frontCamera = true;
    private AVChat avchat;
    private RelativeLayout container;
    private LinearLayout layoutSettings;
    private String callId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avchat);

        thisActivity = this;

        container = (RelativeLayout) findViewById(R.id.layout_avchat_container);
        layoutSettings = (LinearLayout) findViewById(R.id.layout_avchat_settings);

        imgBtnSwitchCamera = (ImageButton) findViewById(R.id.btn_switch_camera);
        imgBtnEndCall = (ImageButton) findViewById(R.id.btn_end_call);
        imgBtnAudio = (ImageButton) findViewById(R.id.btn_audio_toggle);
        imgBtnVideo = (ImageButton) findViewById(R.id.btn_video_toggle);

        Intent intent = getIntent();
        friendId = intent.getStringExtra("userID");
        callId = intent.getStringExtra("callId");
        avchat = AVChat.getAVChatInstance(this);

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layoutSettings.getVisibility() == View.VISIBLE)
                    layoutSettings.animate()
                            .translationX(-1 * layoutSettings.getWidth())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    layoutSettings.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {

                                }
                            });
                else {
                    layoutSettings.animate()
                            .translationX(0)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {
                                    layoutSettings.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {

                                }
                            });
                }
            }
        });

        avchat.startAVChatCall(callId, container, new Callbacks() {

            @Override
            public void successCallback(JSONObject response) {
            }

            @Override
            public void failCallback(JSONObject response) {
            }
        });


        imgBtnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frontCamera = !frontCamera;
            }
        });
        imgBtnSwitchCamera.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(thisActivity, getString(R.string.switch_camera), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imgBtnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });
        imgBtnEndCall.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(thisActivity, getString(R.string.end_call), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imgBtnAudio.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isAudioOn) {
                    isAudioOn = false;
                    imgBtnAudio.setImageResource(R.drawable.ic_action_mic);
                } else {
                    isAudioOn = true;
                    imgBtnAudio.setImageResource(R.drawable.ic_action_mic_muted);
                }

                avchat.toggleAudio(isAudioOn);
            }
        });
        imgBtnAudio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int str;

                if (isAudioOn)
                    str = R.string.turn_off_audio;
                else
                    str = R.string.turn_on_audio;

                Toast.makeText(thisActivity, getString(str), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imgBtnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideoOn) {
                    isVideoOn = false;
                    imgBtnVideo.setImageResource(R.drawable.ic_action_video);
                } else {
                    isVideoOn = true;
                    imgBtnVideo.setImageResource(R.drawable.ic_action_video_stop);
                }

                avchat.toggleVideo(isVideoOn);
            }
        });
        imgBtnVideo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int str;

                if (isVideoOn)
                    str = R.string.turn_off_video;
                else
                    str = R.string.turn_on_video;

                Toast.makeText(thisActivity, getString(str), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (container != null) {
            AVChat.getAVChatInstance(getApplicationContext()).removeVideoOnRotation(container);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (container != null) {
            AVChat.getAVChatInstance(getApplicationContext()).addVideoOnRotation(container);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    layoutSettings.animate()
                            .translationX(-1 * layoutSettings.getWidth())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    layoutSettings.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {

                                }
                            });
                }
            }, 3000);
        }
    }

    public void endCall() {
        avchat.endAVChatCall(friendId, callId, new Callbacks() {
            @Override
            public void successCallback(JSONObject response) {
                Logger.error("success end callback " + response);
            }

            @Override
            public void failCallback(JSONObject response) {
                Logger.error("fail end callback " + response);
            }
        });

        finish();
    }
}
