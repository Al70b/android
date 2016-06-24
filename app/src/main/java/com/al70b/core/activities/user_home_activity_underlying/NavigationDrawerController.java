package com.al70b.core.activities.user_home_activity_underlying;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

/**
 * Created by nasee on 6/24/2016.
 */
public interface NavigationDrawerController {

    int getSelectedItem();

    Fragment getVisibleFragment();

    void navigateTo(int position);

    //void navigateTo(Fragment fragment);

    ViewGroup getDrawerLayout();

    void updateProfilePicture(Bitmap bitmap);

    // do some cleanup when stopping

    boolean activityStart();

    boolean activityPauseCleanup();

    boolean activityStopCleanup();

    boolean activityDestroyCleanup();
}
