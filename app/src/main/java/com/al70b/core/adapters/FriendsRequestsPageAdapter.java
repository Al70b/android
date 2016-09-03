package com.al70b.core.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.al70b.R;
import com.al70b.core.fragments.UserReceivedFriendRequestsFragment;
import com.al70b.core.fragments.UserSentFriendRequestsFragment;

/**
 * Created by Naseem on 8/25/2016.
 */
public class FriendsRequestsPageAdapter extends FragmentPagerAdapter {

    private final int NUM_OF_PAGES = 2;
    private final String[] tabTitles;
    private final Fragment[] fragments = new Fragment[NUM_OF_PAGES];

    public FriendsRequestsPageAdapter(FragmentManager fm, Context context) {
        super(fm);

        tabTitles = context.getResources().getStringArray(R.array.friends_requests);
    }

    @Override
    public Fragment getItem(int position) {

        Fragment f = fragments[position];

        if(f == null) {
            switch (position) {
                case 0:
                    f = new UserSentFriendRequestsFragment();
                    break;
                case 1:
                    f = new UserReceivedFriendRequestsFragment();
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

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

}