package com.al70b.core.activities.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.fragments.UserDataFragment;
import com.al70b.core.fragments.UserDataPicturesFragment;
import com.al70b.core.misc.AppConstants;

/**
 * Created by Naseem on 6/25/2016.
 */
public class PromptUserForProfilePictureDialog extends Dialog {

    private UserHomeActivity activity;
    private WindowManager.LayoutParams lp;

    public PromptUserForProfilePictureDialog(UserHomeActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.dialog_prompt_user_for_profile_picture);
        final CheckBox chkBoxDont = (CheckBox) findViewById(R.id.chk_box_dialog_question_dont_ask_me);
        Button btnPositive = (Button) findViewById(R.id.btn_dialog_question_positive);
        Button btnNegative = (Button) findViewById(R.id.btn_dialog_question_negative);

        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to pictures fragment in user data fragment
                activity.navigateTo(1);
                Fragment f = activity.getVisibleFragment();

                if(f instanceof UserDataFragment) {
                    ((UserDataFragment) f).goToUserPictures();
                }

                if (chkBoxDont.isChecked()) {
                    SharedPreferences sharedPref = activity.getSharedPreferences(
                            AppConstants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(AppConstants.DONT_ASK_FOR_PROFILE_PICTURE, true);
                    editor.apply();
                }

                dismiss();
            }
        });

        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkBoxDont.isChecked()) {
                    SharedPreferences sharedPref = activity.getSharedPreferences(
                            AppConstants.SHARED_PREF_FILE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(AppConstants.DONT_ASK_FOR_PROFILE_PICTURE, true);
                    editor.apply();
                }

                dismiss();
            }
        });

        lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.horizontalMargin = (int) (12 / Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public void show() {
        super.show();

        getWindow().setAttributes(lp);
    }
}
