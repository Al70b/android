package com.al70b.core.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.MembersListActivity;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.objects.User;
import com.al70b.core.objects.UserInterest;

/**
 * Created by Naseem on 6/16/2015.
 */
public class UserSearchBasicFragment extends Fragment {

    public static final String DISPLAY_DATA_TOKEN = "BasicSearch";
    public static final String GENDER = "gender";
    public static final String AGE_FROM = "ageFrom";
    public static final String AGE_TO = "ageTo";
    public static final String PICTURES_ONLY = "picturesOnly";
    public static final String ONLINE_ONLY = "onlineOnly";
    public static final String CLOSE_BY_ONLY = "closeByOnly";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_basic_search, container, false);
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


        editTextFrom.setHint(String.valueOf(AppConstants.MIN_MEMBER_AGE));

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int from, to, gender;
                boolean male, female;
                boolean withPicturesOnly, onlineOnly, closeByOnly;

                male = checkBoxMale.isChecked();
                female = checkBoxFemale.isChecked();

                if (male || female) {
                    // get interested in gender
                    if (male && female) {
                        gender = 3;
                    } else if (female) {
                        gender = 2;
                    } else {
                        gender = 1;
                    }

                    // parse given age, if empty use default
                    if (!editTextFrom.getText().toString().trim().isEmpty()) {
                        from = Integer.parseInt(editTextFrom.getText().toString());
                    } else {
                        from = AppConstants.MIN_MEMBER_AGE;
                    }

                    // parse given age, if empty use default
                    if (!editTextTo.getText().toString().trim().isEmpty()) {
                        to = Integer.parseInt(editTextTo.getText().toString());
                    } else {
                        to = AppConstants.MAX_MEMBER_AGE;
                    }

                    // validate given age
                    if ((from >= AppConstants.MIN_MEMBER_AGE) && (to <= AppConstants.MAX_MEMBER_AGE)
                            && (from <= to)) {
                        // get other options flags
                        withPicturesOnly = checkBoxPicturesOnly.isChecked();
                        onlineOnly = checkBoxOnlineOnly.isChecked();
                        closeByOnly = checkBoxCloseBy.isChecked();

                        // insert the fragment by replacing any existing fragment
                        Fragment fragment = new UserMembersSearchListFragment();

                        // build bundle with data
                        Bundle bundle = new Bundle();
                        bundle.putInt(GENDER, gender);
                        bundle.putInt(AGE_FROM, from);
                        bundle.putInt(AGE_TO, to);
                        bundle.putBoolean(PICTURES_ONLY, withPicturesOnly);
                        bundle.putBoolean(ONLINE_ONLY, onlineOnly);
                        bundle.putBoolean(CLOSE_BY_ONLY, closeByOnly);
                        bundle.putString(UserMembersSearchListFragment.DATA_SOURCE, DISPLAY_DATA_TOKEN);

                        fragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .add(R.id.content_frame, fragment, UserMembersSearchListFragment.FRAGMENT_TAG)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        String msg;

                        if (to > AppConstants.MAX_MEMBER_AGE) {
                            msg = getString(R.string.error_greater_than_max_age);
                        } else if (from < AppConstants.MIN_MEMBER_AGE){
                            msg = getString(R.string.error_should_be_older_than_min_age);
                        } else {
                            msg = getString(R.string.error_age_is_not_correct);
                        }

                        // in case age wasn't set properly
                        Toast.makeText(getActivity().getApplicationContext(),
                                msg,
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // in case neither male nor female were chosen
                    Toast.makeText(getActivity(), getString(R.string.error_choose_interest), Toast.LENGTH_SHORT).show();
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
