package com.al70b.core.activities.user_home_activity_underlying;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.Dialogs.PromptUserForProfilePictureDialog;
import com.al70b.core.activities.SettingsActivity;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.adapters.NavigationDrawerAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.fragments.UserConversationsFragment;
import com.al70b.core.fragments.UserDataFragment;
import com.al70b.core.fragments.UserFriendListFragment;
import com.al70b.core.fragments.UserFriendRequestsFragment;
import com.al70b.core.fragments.UserSearchFragment;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.KEYS;
import com.al70b.core.misc.Utils;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.NavDrawerItem;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.bumptech.glide.Glide;

import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 6/20/2016.
 */
public class NavigationDrawer implements NavigationDrawerController {

    private static String TAG = "NavigationDrawer";

    private UserHomeActivity activity;
    private DrawerLayout root;
    private CurrentUser currentUser;

    public NavigationDrawer(UserHomeActivity activity, DrawerLayout root,
                            CurrentUser currentUser) {
        this.activity = activity;
        this.root = root;
        this.currentUser = currentUser;

        init();
    }

    // ui
    private RelativeLayout navDrawerLayout;
    private ListView navDrawerList;

    // navigation drawer items array
    private NavDrawerItem[] navDrawerItems;
    private NavigationDrawerAdapter navigationDrawerAdapter;

    // track current item & visible fragment
    private int selectedItem = -1;
    private Fragment currentShownFragment;

    // declare timer task running every 10 seconds
    private Timer fetchRequestsAndMessagesTimer;
    private static final int RATE_TO_FETCH_REQUESTS = 10 * 1000; // in milliseconds

    private enum NavigationDrawerItems {
        PROFILE(0),
        CONVERSATIONS(1),
        FRIENDS(2),
        FRIENDS_REQUESTS(3),
        SEARCH(4);

        NavigationDrawerItems(int index) {
            this.index = index;
        }

        int index;

        public int getIndex() {
            return index;
        }
    }

    private void init() {
        /*          N A V I G A T I O N      D R A W E R          */

        // relate navigation drawer components to xml elements
        // this is the main layout of the drawer
        navDrawerLayout = (RelativeLayout) root.findViewById(R.id.layout_navigation_drawer);

        int width, height;
        width = activity.getResources().getDisplayMetrics().widthPixels;
        width -= (int)Utils.convertDpToPixel(15, activity);
        height = activity.getResources().getDisplayMetrics().heightPixels;
        DrawerLayout.LayoutParams lp = new DrawerLayout.LayoutParams(width, height);
        lp.gravity = Gravity.START;
        navDrawerLayout.setLayoutParams(lp);

        // Drawer Header
        ViewGroup navHeader = (ViewGroup) navDrawerLayout.findViewById(R.id.layout_drawer_navigation_header);
        CircleImageView cmUserProfilePicture = (CircleImageView) navHeader.findViewById(R.id.circle_image_view_drawer_profile_image);
        final ImageButton imgBtnSettings = (ImageButton) navHeader.findViewById(R.id.img_btn_drawer_profile_settings);

        imgBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, SettingsActivity.class);
                activity.startActivity(intent);
            }
        });

        imgBtnSettings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(activity, activity.getString(R.string.settings), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imgBtnSettings.setOnTouchListener(new View.OnTouchListener() {
            private Rect rect;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    imgBtnSettings.setColorFilter(Color.argb(60, 0, 0, 0));
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    imgBtnSettings.setColorFilter(Color.argb(0, 0, 0, 0));
                }
                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    if(!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
                        imgBtnSettings.setColorFilter(Color.argb(0, 0, 0, 0));
                    }
                }
                return false;
            }
        });

        // Drawer Main List
        navDrawerList = (ListView) navDrawerLayout.findViewById(R.id.list_navigation_drawer);

        // Drawer Footer
        ViewGroup navFooter = (ViewGroup) navDrawerLayout.findViewById(R.id.layout_drawer_navigation_footer);

        // profile picture can be clicked, click activates changing the picture dialog
        cmUserProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToUsersPictures();
            }
        });

        // if profile picture set for this user
        if (currentUser.isProfilePictureSet()) {
            // set it to circle image view
            Glide.with(activity.getApplicationContext())
                    .load(currentUser.getProfilePictureThumbnailPath())
                    .asBitmap()
                    .fitCenter()
                    .placeholder(R.drawable.avatar)
                    .into(cmUserProfilePicture);
        } else {
            // prompt user to add a profile picture
            promptUserForProfilePictureUpdate();
        }

        // create & set the adapter for the navigation drawer list view
        navigationDrawerAdapter = new NavigationDrawerAdapter(activity,
                buildNavigationDrawerItems());
        navDrawerList.setAdapter(navigationDrawerAdapter);

        // and set the listener for items click event
        navDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        navDrawerList.setOnItemLongClickListener(null);
    }

    // build navigation drawer items depending on the array of drawerItems
    private NavDrawerItem[] buildNavigationDrawerItems() {
        String[] drawerValues = activity.getResources()
                .getStringArray(R.array.navigation_drawer_values);

        navDrawerItems = new NavDrawerItem[drawerValues.length];

        // User Account
        navDrawerItems[NavigationDrawerItems.PROFILE.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.PROFILE.index],
                currentUser.isMale() ?
                        R.drawable.ic_action_user :
                        R.drawable.ic_action_user
                , currentUser.isMale() ?
                R.drawable.ic_action_user :
                R.drawable.ic_action_user);

        // Conversations
        navDrawerItems[NavigationDrawerItems.CONVERSATIONS.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.CONVERSATIONS.index],
                R.drawable.ic_action_messages, R.drawable.ic_action_messages);

        // Friends
        navDrawerItems[NavigationDrawerItems.FRIENDS.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.FRIENDS.index],
                R.drawable.ic_action_users, R.drawable.ic_action_users);

        // Friends Requests
        navDrawerItems[NavigationDrawerItems.FRIENDS_REQUESTS.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.FRIENDS_REQUESTS.index],
                R.drawable.ic_action_user_add, R.drawable.ic_action_user_add);

        // Search
        navDrawerItems[NavigationDrawerItems.SEARCH.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.SEARCH.index],
                R.drawable.ic_action_search, R.drawable.ic_action_search);
        return navDrawerItems;
    }

    private static Fragment[] fragments = new Fragment[7];

    private void selectItem(int position) {
        String fragmentTag = position + "_" + UserHomeActivity.TAG_EXIT;

        // get fragment
        Fragment fragment = activity.getSupportFragmentManager()
                .findFragmentByTag(fragmentTag);

        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = new UserDataFragment();
                    break;
                case 1:
                    fragment = new UserConversationsFragment();
                    break;
                case 2:
                    fragment = new UserFriendListFragment();
                    break;
                case 3:
                    fragment = new UserFriendRequestsFragment();
                    break;
                case 4:
                    fragment = new UserSearchFragment();
                    break;
                default:
                    return;
            }

            //fragments[position] = fragment;

            if (fragment == null) {
                return;
            }
        }

        // get item, highlight it
        NavDrawerItem item = navDrawerItems[position];
        item.setHighlighted(true);
        navDrawerList.setItemChecked(position, true);
        navDrawerList.setSelection(position);

        // update the title
        activity.setTitle(item.getTitle());

        // and show relevant page
        currentShownFragment = fragment;

        // insert the fragment by replacing any existing fragment
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment, fragmentTag)
                .commit();

        // close the drawer
        root.closeDrawer(navDrawerLayout);
    }

    private void logout() {
        // remove timed job
        fetchRequestsAndMessagesTimer.cancel();

        // remove saved data before logging out
        activity.logout();
    }

    private void promptUserForProfilePictureUpdate() {
        SharedPreferences sharedPref = activity.getSharedPreferences(
                AppConstants.SHARED_PREF_FILE,
                Context.MODE_PRIVATE);
        boolean dontAsk = sharedPref.getBoolean(
                KEYS.SHARED_PREFERENCES.DONT_ASK_FOR_PROFILE_PICTURE_UPLOAD, false);

        if (!dontAsk) {
            PromptUserForProfilePictureDialog dialog = new
                    PromptUserForProfilePictureDialog(activity);
            dialog.show();
        }

    }

    private boolean startTimer() {
        fetchRequestsAndMessagesTimer = new Timer("FetchRequestsAndMessagesTimer", true);
        fetchRequestsAndMessagesTimer.scheduleAtFixedRate(
                new FetchRequestsAndMessagesTask(),
                0,
                RATE_TO_FETCH_REQUESTS);

        return true;
    }

    private class FetchRequestsAndMessagesTask extends TimerTask {

        @Override
        public void run() {
            try {
                RequestsInterface requestsInterface = new RequestsInterface(
                        activity.getApplicationContext());
                ServerResponse<Pair<Integer, Integer>> sr = requestsInterface.getUserStats(currentUser);

                if (sr.isSuccess()) {
                    Pair<Integer, Integer> pair = sr.getResult();

                    StringBuilder sbFriendsRequests = new StringBuilder();
                    if (pair.first > 0) {
                        if (pair.first > 99) {
                            sbFriendsRequests.append("99+");
                        } else {
                            sbFriendsRequests.append(pair.first);
                        }
                    }

                    // Value has changed
                    if (currentUser.getNumOfFriendsRequests() != pair.first) {
                        currentUser.setNumOfFriendsRequests(pair.first);

                        NavDrawerItem item = navDrawerItems[NavigationDrawerItems.FRIENDS_REQUESTS.index];
                        item.setSubtext(sbFriendsRequests.toString());

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                navigationDrawerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            } catch (ServerResponseFailedException ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }


    @Override
    public ViewGroup getDrawerLayout() {
        return navDrawerLayout;
    }

    @Override
    public int getSelectedItem() {
        return selectedItem;
    }

    @Override
    public Fragment getVisibleFragment() {
        return currentShownFragment;
    }

    @Override
    public void navigateTo(int position) {
        selectItem(position);
        selectedItem = position;
        // visible fragment updated in selectItem function
    }

    @Override
    public void navigateTo(int position, Bundle bundle) {

    }

    @Override
    public void navigateTo(Fragment fragment) {
        navigateTo(fragment, null);
    }

    @Override
    public void navigateTo(Fragment fragment, Bundle bundle) {

    }

    @Override
    public void navigateToUsersPictures() {
        // go to user's pictures fragment
        if (currentShownFragment instanceof UserDataFragment) {
            ((UserDataFragment) currentShownFragment).goToMyPictures();
            activity.closeDrawers();
        } else {
            UserDataFragment.raiseGoToMyPicturesFlag();
            selectItem(NavigationDrawerItems.PROFILE.index);
        }
    }

    @Override
    public boolean activityStart() {
        return startTimer();
    }

    @Override
    public boolean activityPause() {
        return true;
    }

    @Override
    public boolean activityStop() {
        if (fetchRequestsAndMessagesTimer != null)
            fetchRequestsAndMessagesTimer.cancel();
        return true;
    }

    @Override
    public boolean activityDestroy() {
        // canceling the thread is done in the stop cleanup
        return true;
    }


    @Override
    public void updateProfilePicture(int res) {
        CircleImageView cmUserProfilePicture = (CircleImageView) navDrawerLayout.findViewById(
                R.id.circle_image_view_drawer_profile_image);
        cmUserProfilePicture.setImageResource(res);
        cmUserProfilePicture.invalidate();
    }

    @Override
    public void updateProfilePicture(String picturePath) {
        CircleImageView cmUserProfilePicture = (CircleImageView) navDrawerLayout.findViewById(
                R.id.circle_image_view_drawer_profile_image);
        Glide.with(activity.getApplicationContext())
                .load(picturePath)
                .asBitmap()
                .fitCenter()
                .placeholder(R.drawable.avatar)
                .into(cmUserProfilePicture);
        cmUserProfilePicture.invalidate();
    }

    @Override
    public void updateProfilePicture(Bitmap bitmap) {
        CircleImageView cmUserProfilePicture = (CircleImageView) navDrawerLayout.findViewById(
                R.id.circle_image_view_drawer_profile_image);
        cmUserProfilePicture.setImageBitmap(bitmap);
        cmUserProfilePicture.invalidate();
    }
}
