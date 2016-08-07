package com.al70b.core.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.activities.MemberProfileActivity;
import com.al70b.core.objects.Characteristics;
import com.al70b.core.objects.OtherUser;

/**
 * Created by Naseem on 6/18/2015.
 */
public class MemberDataAdvancedFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_member_data_advanced, container, false);


        TextView textViewHeight = (TextView) viewGroup.findViewById(R.id.text_view_member_data_advanced_heightB);
        TextView textViewWork = (TextView) viewGroup.findViewById(R.id.text_view_member_data_advanced_workB);
        TextView textViewEducation = (TextView) viewGroup.findViewById(R.id.text_view_member_data_advanced_educationB);
        TextView textViewReligion = (TextView) viewGroup.findViewById(R.id.text_view_member_data_advanced_religionB);
        TextView textViewAlcohol = (TextView) viewGroup.findViewById(R.id.text_view_member_data_advanced_alcoholB);
        TextView textViewSmoking = (TextView) viewGroup.findViewById(R.id.text_view_member_data_advanced_smokingB);
        TextView textViewMore = (TextView) viewGroup.findViewById(R.id.text_view_member_data_advanced_moreB);


        Bundle bundle = getArguments();
        OtherUser otherUser = (OtherUser) bundle.getSerializable(MemberProfileActivity.OTHER_USER);

        if (otherUser != null) {
            Characteristics ch = otherUser.getUserChar();

            if (ch != null) {
                textViewHeight.setText(ch.displayHeight(getResources()).compareTo(getString(R.string.not_specified)) == 0 ?
                        ch.displayHeight(getResources()) :
                        ch.displayHeight(getResources()) + " " + getString(R.string.cm));
                textViewWork.setText(ch.getWork());
                textViewEducation.setText(ch.getEducation());
                textViewReligion.setText(ch.getReligion());
                textViewAlcohol.setText(ch.getAlcohol());
                textViewSmoking.setText(ch.getSmoking());
                textViewMore.setText(ch.getDescription());
            }
        }

        return viewGroup;
    }
}
