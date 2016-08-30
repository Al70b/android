package com.al70b.core.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.adapters.GuestHomePageAdapter;
import com.al70b.core.extended_widgets.page_indicators.CirclePageIndicator;

public class GuestHomeActivity extends FragmentActivity {

    private ViewPager mViewPager;
    private GuestHomePageAdapter mPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_home);

        // user is not logged in
        mViewPager = (ViewPager) findViewById(R.id.view_pager_guestHomeA);
        mPagerAdapter = new GuestHomePageAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        mViewPager.setAdapter(mPagerAdapter);

        CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.circle_page_ind_guestHomeA);
        circlePageIndicator.setViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MyApplication) getApplication()).setAppVisible();

        mViewPager.setCurrentItem(mPagerAdapter.getCount() - 1, true);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MyApplication) getApplication()).setAppInvisible();
    }


    public void onClickRegisterButton(View view) {
        Intent intent = new Intent(GuestHomeActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onClickLoginButton(View view) {
        Intent intent = new Intent(GuestHomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
