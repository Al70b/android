package com.al70b.core.activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.fragments.GuestLoginFragment;
import com.al70b.core.fragments.GuestRegisterFragment;
import com.al70b.core.fragments.GuestWelcomeFragment;

import java.util.ArrayList;
import java.util.List;

public class ScreenSlideHomeActivity extends FragmentActivity {

    // number of pages
    private static final int NUM_OF_PAGES = 3;

    // initial fragment position
    private int INITIAL_POSITION = NUM_OF_PAGES - 1;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mViewPager;

    // pager adapter which provides the pages to the View Pager widget
    private ScreenSlidePagerAdapter mPagerAdapter;


    public void goToLogin(String email) {
        mViewPager.setCurrentItem(getResources().getInteger(R.integer.centerScreenSlide));
        GuestLoginFragment guestLoginFragment = (GuestLoginFragment) mPagerAdapter.getItem(getResources().getInteger(R.integer.centerScreenSlide));
        guestLoginFragment.setEmailEditText(email);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_home);

        // user is not logged in
        mViewPager = (ViewPager) findViewById(R.id.view_pager_guest_home);
        mViewPager.setOffscreenPageLimit(NUM_OF_PAGES);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(INITIAL_POSITION, true);
    }


    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == getResources().getInteger(R.integer.rightMostScreenSlide)) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();

        } else {
            // Otherwise, select the previous step.
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.guest_mode, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {

            case R.id.action_bar_login:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
                mViewPager.setCurrentItem(getResources().getInteger(R.integer.centerScreenSlide), true);
                return true;

            case R.id.action_bar_register:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
                mViewPager.setCurrentItem(getResources().getInteger(R.integer.leftMostScreenSlide), true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MyApplication) getApplication()).setAppInvisible();
    }

    public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<Fragment>();
            fragments.add(new GuestRegisterFragment());
            fragments.add(new GuestLoginFragment());
            fragments.add(new GuestWelcomeFragment());
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return NUM_OF_PAGES;
        }
    }
}
