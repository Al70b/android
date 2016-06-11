package com.al70b.core.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

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
        TextView textViewBirthdate = (TextView) viewGroup.findViewById(R.id.text_view_member_data_basic_birthdateB);
        TextView textViewSocial = (TextView) viewGroup.findViewById(R.id.text_view_member_data_basic_socialB);
        TextView textViewCountry = (TextView) viewGroup.findViewById(R.id.text_view_member_data_basic_countryB);
        TextView textViewCity = (TextView) viewGroup.findViewById(R.id.text_view_member_data_basic_cityB);
        TextView textViewInterestedIn = (TextView) viewGroup.findViewById(R.id.text_view_member_data_lookForRelationB);
        CheckBox chkboxMale = (CheckBox) viewGroup.findViewById(R.id.checkBox_member_data_basic_interestedIn_male);
        CheckBox chkBoxFemale = (CheckBox) viewGroup.findViewById(R.id.checkBox_member_data_basic_interestedIn_female);


        Bundle bundle = getArguments();
        OtherUser otherUser = (OtherUser) bundle.getSerializable(MemberProfileActivity.OTHER_USER);

        String dateOfBirth;
        Calendar c = otherUser.getDateOfBirth();
        dateOfBirth = c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.DAY_OF_MONTH);
        textViewGender.setText(otherUser.getGender().toString(getActivity().getApplicationContext()));
        textViewBirthdate.setText(dateOfBirth);
        textViewSocial.setText(otherUser.getSocialStatus());
        textViewCountry.setText(otherUser.getAddress().getCountry());
        textViewCity.setText(otherUser.getAddress().getCity() == null ? getString(R.string.not_specified)
                : otherUser.getAddress().getCity());

        String interestPurposeStr = getString(R.string.not_specified);
        List<String> list = otherUser.getUserInterest().getPurposesOfInterest();
        if (list.size() > 0)
            interestPurposeStr = otherUser.getUserInterest().myListToString();

        textViewInterestedIn.setText(interestPurposeStr);

        User.Gender interestedInGender = otherUser.getUserInterest().getGenderInterest();
        switch (interestedInGender.getValue()) {
            case User.Gender.MALE:
                chkboxMale.setChecked(true);
                break;
            case User.Gender.FEMALE:
                chkBoxFemale.setChecked(true);
                break;
            case User.Gender.BOTH:
                chkboxMale.setChecked(true);
                chkBoxFemale.setChecked(true);
                break;
        }

        return viewGroup;
    }
}