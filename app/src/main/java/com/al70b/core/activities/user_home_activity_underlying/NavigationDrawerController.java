package com.al70b.core.activities.user_home_activity_underlying;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

/**
 * Created by Naseem on 6/24/2016.
 */
public interface NavigationDrawerController {

    /*
       Summary: get current selected item position
       Returns: enum:NavigationTarget
      */
    int getSelectedItem();

    /*
       Summary: get current visible fragment
       Returns: Fragment object of the current shown fragment
      */
    Fragment getVisibleFragment();

    /*
       Summary: navigate\show fragment in position 'position'
      */
    void navigateTo(int position);

    void navigateTo(int position, Bundle bundle);

    void navigateTo(Fragment fragment);

    void navigateTo(Fragment fragment, Bundle bundle);

    /*
       Summary: get main drawer layout of the navigation drawer
       Returns: ViewGroup object representing the drawer layout
      */
    ViewGroup getDrawerLayout();


    void updateProfilePicture(Bitmap bitmap);


    /*
       Summary: a callback when activity executes onStart method
       Returns: boolean, if method executed with no errors
      */
    boolean activityStart();

    /*
       Summary: a callback when activity executes onPause method
       Returns: boolean, if method executed with no errors
      */
    boolean activityPause();

    /*
       Summary: a callback when activity executes onStop method
       Returns: boolean, if method executed with no errors
      */
    boolean activityStop();

    /*
       Summary: a callback when activity executes onDestroy method
       Returns: boolean, if method executed with no errors
      */
    boolean activityDestroy();
}
