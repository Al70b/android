package com.al70b.core.fragments.Register;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.fragments.RegisterFragment;

/**
 * Created by Naseem on 7/1/2015.
 */
public class RegisterFragmentIntro extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_register_intro, container, false);
        Button btnStart = (Button) viewGroup.findViewById(R.id.btn_register_start);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterFragment.pickFragment(new RegisterFragment1(), true);
            }
        });

        TextView tvFree = (TextView) viewGroup.findViewById(R.id.tv_register_intro_free);
        TextView tvTitle = (TextView) viewGroup.findViewById(R.id.tv_register_intro_title);
        TextView tvSubtitle = (TextView) viewGroup.findViewById(R.id.tv_register_intro_subtitle);

        Typeface typefaceReg = Typeface.createFromAsset(getActivity().getAssets(), "fonts/DroidNaskh-Regular.ttf");
        Typeface typefaceBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/DroidNaskh-Bold.ttf");

        tvFree.setTypeface(typefaceBold);
        tvTitle.setTypeface(typefaceReg);
        tvSubtitle.setTypeface(typefaceReg);

        return viewGroup;
    }
}
