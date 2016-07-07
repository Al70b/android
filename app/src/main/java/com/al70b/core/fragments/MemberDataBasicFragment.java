package com.al70b.core.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.MemberProfileActivity;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.User;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Naseem on 6/18/2015.
 */
public class MemberDataBasicFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_member_data_basic, container, false);

        TextView textViewGender = (TextView) viewGroup.findViewById(R.id.text_view_member_data_basic_genderB);
        TextView textViewBirthDate = (TextView) viewGroup.findViewById(R.id.text_view_member_data_basic_birthdateB);
        TextView textViewCountry = (TextView) viewGroup.findViewById(R.id.text_view_member_data_basic_countryB);
        TextView textViewCity = (TextView) viewGroup.findViewById(R.id.text_view_member_data_basic_cityB);
        CheckBox checkBoxMale = (CheckBox) viewGroup.findViewById(R.id.checkBox_member_data_basic_interestedIn_male);
        CheckBox checkBoxFemale = (CheckBox) viewGroup.findViewById(R.id.checkBox_member_data_basic_interestedIn_female);


        Bundle bundle = getArguments();
        OtherUser otherUser = (OtherUser) bundle.getSerializable(MemberProfileActivity.OTHER_USER);

        if(otherUser != null) {
            String dateOfBirth;
            Calendar c = otherUser.getDateOfBirth();
            dateOfBirth = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.DAY_OF_MONTH);
            textViewGender.setText(otherUser.getGender().toString(getActivity().getApplicationContext()));
            textViewBirthDate.setText(dateOfBirth);
            textViewCountry.setText(otherUser.getAddress().getCountry());
            textViewCity.setText(otherUser.getAddress().getCity() == null ? getString(R.string.not_specified)
                    : otherUser.getAddress().getCity());

            User.Gender interestedInGender = otherUser.getUserInterest().getGenderInterest();
            switch (interestedInGender.getValue()) {
                case User.Gender.MALE:
                    checkBoxMale.setChecked(true);
                    break;
                case User.Gender.FEMALE:
                    checkBoxFemale.setChecked(true);
                    break;
                case User.Gender.BOTH:
                    checkBoxMale.setChecked(true);
                    checkBoxFemale.setChecked(true);
                    break;
            }
        } else {

        }

        return viewGroup;
    }
}