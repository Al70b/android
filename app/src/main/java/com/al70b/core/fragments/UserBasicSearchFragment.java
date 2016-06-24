package com.al70b.core.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.MembersListActivity;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.objects.CurrentUser;

/**
 * Created by Naseem on 6/16/2015.
 */
public class UserBasicSearchFragment extends Fragment {

    public static final String DISPLAY_DATA_TOKEN = "BasicSearch";

    private CurrentUser currentUser;

    private boolean withPicturesOnly, onlineOnly, closeByOnly;
    private int from, to, gender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = ((MyApplication)getActivity().getApplication()).getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_members, container, false);
        viewGroup.setFocusable(true);
        viewGroup.requestFocus();

        final CheckBox checkBoxMale = (CheckBox) viewGroup.findViewById(R.id.checkbox_members_male);
        final CheckBox checkBoxFemale = (CheckBox) viewGroup.findViewById(R.id.checkbox_members_female);
        final CheckBox checkBoxPicturesOnly = (CheckBox) viewGroup.findViewById(R.id.checkbox_members_with_pictures);
        final CheckBox checkBoxOnlineOnly = (CheckBox) viewGroup.findViewById(R.id.checkbox_members_online);
        final CheckBox checkBoxCloseBy = (CheckBox) viewGroup.findViewById(R.id.checkbox_members_close_by);
        final EditText editTextFrom = (EditText) viewGroup.findViewById(R.id.edit_text_members_age_from);
        final EditText editTextTo = (EditText) viewGroup.findViewById(R.id.edit_text_members_age_to);
        final Button searchBtn = (Button) viewGroup.findViewById(R.id.button_members_search);


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean male, female;
                male = checkBoxMale.isChecked();
                female = checkBoxFemale.isChecked();

                if (male || female) {
                    // get interested in gender
                    if (male && female)
                        gender = 3;
                    else if (female)
                        gender = 2;
                    else
                        gender = 1;

                    // parse given age, if empty use default
                    if (!editTextFrom.getText().toString().trim().isEmpty())
                        from = Integer.parseInt(editTextFrom.getText().toString());
                    else {
                        from = AppConstants.MIN_MEMBER_AGE;
                    }

                    // parse given age, if empty use default
                    if (!editTextTo.getText().toString().trim().isEmpty())
                        to = Integer.parseInt(editTextTo.getText().toString());
                    else {
                        to = AppConstants.MAX_MEMBER_AGE;
                    }

                    // validate given age
                    if ((from >= AppConstants.MIN_MEMBER_AGE && from <= AppConstants.MAX_MEMBER_AGE)
                            && (to >= AppConstants.MIN_MEMBER_AGE && to <= AppConstants.MAX_MEMBER_AGE)
                            && (from <= to)) {

                        // get other options flags
                        withPicturesOnly = checkBoxPicturesOnly.isChecked();
                        onlineOnly = checkBoxOnlineOnly.isChecked();
                        closeByOnly = checkBoxCloseBy.isChecked();

                        // build intent and pass request
                        Intent intent = new Intent(UserHomeActivity.getUserHomeActivity(), MembersListActivity.class);
                        intent.putExtra(MembersListActivity.DATA_SOURCE, DISPLAY_DATA_TOKEN);

                        // build bundle with data
                        Bundle bundle = new Bundle();
                        bundle.putInt("gender", gender);
                        bundle.putInt("ageFrom", from);
                        bundle.putInt("ageTo", to);
                        bundle.putBoolean("picturesOnly", withPicturesOnly);
                        bundle.putBoolean("onlineOnly", onlineOnly);
                        bundle.putBoolean("closeByOnly", closeByOnly);
                        intent.putExtras(bundle);

                        // start activity
                        startActivity(intent);
                    } else {
                        String msg;

                        if (from > to) {
                            msg = getString(R.string.error_age_is_not_correct);
                        } else {
                            msg = getString(R.string.error_18_and_above);
                        }

                        // in case age wasn't set properly
                        Toast.makeText(getActivity().getApplicationContext(),
                                msg,
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // in case neither male nor female were chosen
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_choose_interest), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

    }


}
