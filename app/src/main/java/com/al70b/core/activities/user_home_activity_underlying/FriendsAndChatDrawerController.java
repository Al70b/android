package com.al70b.core.activities.user_home_activity_underlying;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

/**
 * Created by Naseem on 6/24/2016.
 */
public interface FriendsAndChatDrawerController {

    /*
        Summary: get main drawer layout of the navigation drawer
        Returns: ViewGroup object representing the drawer layout
      */
    ViewGroup getDrawerLayout();

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
