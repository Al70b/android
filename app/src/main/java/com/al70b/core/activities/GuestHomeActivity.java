package com.al70b.core.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.extended_widgets.circle_page_indicator.CirclePageIndicator;
import com.al70b.core.fragments.guest_home.GuestHome1Fragment;
import com.al70b.core.fragments.guest_home.GuestHome2Fragment;
import com.al70b.core.fragments.guest_home.GuestHome3Fragment;
import com.al70b.core.fragments.guest_home.GuestHome4Fragment;

public class GuestHomeActivity extends FragmentActivity {

    // number of pages
    private static final int NUM_OF_PAGES = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_home);

        // user is not logged in
        ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager_guestHomeA);
        mViewPager.setOffscreenPageLimit(NUM_OF_PAGES);
        GuestHomePageAdapter mPagerAdapter = new GuestHomePageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(NUM_OF_PAGES - 1, true);

        CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.circle_page_ind_guestHomeA);
        circlePageIndicator.setViewPager(mViewPager);
    }

/*
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
    }*/

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


    public void onClickRegisterButton(View view) {
        Toast.makeText(this, "Register was clicked", Toast.LENGTH_SHORT).show();
    }
    public void onClickLoginButton(View view) {
        Toast.makeText(this, "Login was clicked", Toast.LENGTH_SHORT).show();
    }


    public class GuestHomePageAdapter extends FragmentPagerAdapter {

        public GuestHomePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f;

            switch (position) {
                case 0:
                    f = new GuestHome1Fragment();
                    break;
                case 1:
                    f = new GuestHome2Fragment();
                    break;
                case 2:
                    f = new GuestHome3Fragment();
                    break;
                case 3:
                    f = new GuestHome4Fragment();
                    break;
                default:
                    f = new GuestHome1Fragment();
            }
            return f;
        }

        @Override
        public int getCount() {
            return NUM_OF_PAGES;
        }
    }
}
