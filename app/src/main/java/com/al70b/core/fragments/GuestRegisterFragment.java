package com.al70b.core.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.FragmentTransaction;

import com.al70b.R;
import com.al70b.core.fragments.Register.RegisterFragmentIntro;
import com.al70b.core.objects.CurrentUser;


/**
 * Created by Naseem on 5/30/2015.
 */
public class GuestRegisterFragment extends Fragment {


    private static FragmentActivity activity;

    private static CurrentUser user;

    public static void pickFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_register, fragment, fragment.getClass().getName());

        if (addToBackStack)
            transaction.addToBackStack(fragment.getTag());

        transaction.commit();
    }

    public static CurrentUser getRegisteringUser() {
        return user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        activity = getActivity();

        user = new CurrentUser(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_general_register, container, false);


        pickFragment(new RegisterFragmentIntro(), true);
        return viewGroup;
    }


}
