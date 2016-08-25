package com.al70b.core.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.al70b.core.fragments.guest_home.GuestHome1Fragment;
import com.al70b.core.fragments.guest_home.GuestHome2Fragment;
import com.al70b.core.fragments.guest_home.GuestHome3Fragment;
import com.al70b.core.fragments.guest_home.GuestHome4Fragment;

/**
 * Created by Naseem on 8/25/2016.
 */
public class GuestHomePageAdapter extends FragmentPagerAdapter {

    private final int NUM_OF_PAGES = 4;
    private final Fragment[] fragments = new Fragment[NUM_OF_PAGES];

    public GuestHomePageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        Fragment f = fragments[position];

        if(f == null) {
            switch (position) {
                case 0:
                    f = new GuestHome4Fragment();
                    break;
                case 1:
                    f = new GuestHome3Fragment();
                    break;
                case 2:
                    f = new GuestHome2Fragment();
                    break;
                case 3:
                    f = new GuestHome1Fragment();
                    break;
            }
            fragments[position] = f;
        }

        return f;
    }

    @Override
    public int getCount() {
        return NUM_OF_PAGES;
    }

}
