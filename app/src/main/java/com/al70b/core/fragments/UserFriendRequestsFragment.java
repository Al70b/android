package com.al70b.core.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.al70b.R;
import com.al70b.core.adapters.FriendsRequestsPageAdapter;
import com.al70b.core.extended_widgets.SlidingTabLayout;

/**
 * Created by Naseem on 5/10/2015.
 */
public class UserFriendRequestsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_friend_requests_parent, container, false);

        ViewPager mViewPager = (ViewPager) viewGroup.findViewById(R.id.view_pager_friendsRequestsA);
        FriendsRequestsPageAdapter mPagerAdapter = new FriendsRequestsPageAdapter(
                getActivity().getSupportFragmentManager(), getActivity());
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        mViewPager.setAdapter(mPagerAdapter);


        // bind sliding tab layout with xml tag, and set the previously created view pager
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) viewGroup.findViewById(R.id.sliding_tabs_friendsRequestsA);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setCustomTabView(R.layout.tab, R.id.tv_tab_in_view_pager);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return Color.RED;
            }
        });
        slidingTabLayout.setViewPager(mViewPager);
        return viewGroup;
    }


}
