package com.al70b.core.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.al70b.R;
import com.al70b.core.fragments.UserSearchAdvancedFragment;
import com.al70b.core.fragments.UserSearchBasicFragment;

/**
 * Created by Naseem on 8/25/2016.
 */
public class SearchPageAdapter extends FragmentPagerAdapter {

    private final int NUM_OF_PAGES = 2;
    private final String[] tabTitles;

    public SearchPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        tabTitles = context.getResources().getStringArray(R.array.search_options);
    }

    @Override
    public int getCount() {
        return NUM_OF_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new UserSearchBasicFragment();
            case 0:
            default:
                return new UserSearchAdvancedFragment();
        }
    }
}
