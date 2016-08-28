package com.al70b.core.activities.user_home_activity_underlying;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.activities.Dialogs.PromptUserForProfilePictureDialog;
import com.al70b.core.activities.FriendsListActivity;
import com.al70b.core.activities.FriendsRequestsListActivity;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.activities.UsersListActivity;
import com.al70b.core.adapters.NavigationDrawerAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.fragments.UserAdvancedSearchFragment;
import com.al70b.core.fragments.UserBasicSearchFragment;
import com.al70b.core.fragments.UserCloseAccountFragment;
import com.al70b.core.fragments.UserConversationsFragment;
import com.al70b.core.fragments.UserDataFragment;
import com.al70b.core.fragments.UserSettingsFragment;
import com.al70b.core.misc.AppConstants;
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
public class NavigationDrawer implements NavigationDrawerController{

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
    private LinearLayout navDrawerLayout;
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
        BASIC_SEARCH(4),
        ADVANCED_SEARCH(5);

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
        navDrawerLayout = (LinearLayout) root.findViewById(R.id.layout_navigation_drawer);

        // Drawer Header
        ViewGroup navHeader = (ViewGroup) navDrawerLayout.findViewById(R.id.layout_navigation_drawer_header);
        CircleImageView cmUserProfilePicture = (CircleImageView) navHeader.findViewById(R.id.circle_image_view_drawer_profile_image);

        // Drawer Main List
        navDrawerList = (ListView) navDrawerLayout.findViewById(R.id.list_navigation_drawer);

        // Drawer Footer
        //ViewGroup navFooter = (ViewGroup) navDrawerLayout.findViewById(R.id.layout_navigation_drawer_footer);

        // profile picture can be clicked, click activates changing the picture dialog
        cmUserProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to user's fragment
                selectItem(NavigationDrawerItems.PROFILE.index);

                if (currentShownFragment instanceof UserDataFragment) {
                    ((UserDataFragment)currentShownFragment).goToMyPictures();
                }
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
                currentUser.isMale()?
                        R.drawable.ic_action_profile_male:
                        R.drawable.ic_action_profile_female
                , currentUser.isMale()?
                R.drawable.ic_action_profile_male:
                R.drawable.ic_action_profile_female);

        // Conversations
        navDrawerItems[NavigationDrawerItems.CONVERSATIONS.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.CONVERSATIONS.index],
                R.drawable.ic_action_messages, R.drawable.ic_action_messages);

        // Friends
        navDrawerItems[NavigationDrawerItems.FRIENDS.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.FRIENDS.index],
                R.drawable.ic_action_friends, R.drawable.ic_action_friends);

        // Friends Requests
        navDrawerItems[NavigationDrawerItems.FRIENDS_REQUESTS.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.FRIENDS_REQUESTS.index],
                R.drawable.ic_action_friends_requests, R.drawable.ic_action_friends_requests);

        // Basic Search
        navDrawerItems[NavigationDrawerItems.BASIC_SEARCH.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.BASIC_SEARCH.index],
                R.drawable.ic_action_basic_search, R.drawable.ic_action_basic_search);

        // Advanced Search
        navDrawerItems[NavigationDrawerItems.ADVANCED_SEARCH.index] = new NavDrawerItem(
                drawerValues[NavigationDrawerItems.ADVANCED_SEARCH.index],
                R.drawable.ic_action_advanced_search, R.drawable.ic_action_advanced_search);

        return navDrawerItems;
    }


    private static Fragment[] fragments = new Fragment[7];
    private void selectItem(int position) {

        // get fragment
        Fragment fragment = fragments[position];

        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = new UserDataFragment();
                    break;
                case 1:
                    fragment = new UserConversationsFragment();
                    break;
                case 2:
                    // Friends activity
                    Intent intentFriends = new Intent(activity, FriendsListActivity.class);
                    intentFriends.putExtra(AppConstants.CURRENT_USER, currentUser);
                    activity.startActivity(intentFriends);
                    break;
                case 3:
                    // Friends Requests activity
                    Intent intentFriendRequests = new Intent(activity, FriendsRequestsListActivity.class);
                    intentFriendRequests.putExtra(AppConstants.CURRENT_USER, currentUser);
                    intentFriendRequests.putExtra(FriendsRequestsListActivity.NUMBER_OF_FRIENDS_REQUESTS,
                            currentUser.getNumOfFriendsRequests());
                    activity.startActivity(intentFriendRequests);
                    break;
                case 4:
                    fragment = new UserBasicSearchFragment();
                    break;
                case 5:
                    fragment = new UserAdvancedSearchFragment();
                    break;
                case 6: // section: Others
                    return;
                default:
                    return;
            }

            fragments[position] = fragment;

            if(fragment == null) {
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
                .replace(R.id.content_frame, fragment, position + "_" + UserHomeActivity.TAG_EXIT)
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

    private void promptUserForProfilePictureUpdate(){
        SharedPreferences sharedPref = activity.getSharedPreferences(
                AppConstants.SHARED_PREF_FILE,
                Context.MODE_PRIVATE);
        boolean dontAsk = sharedPref.getBoolean(
                AppConstants.DONT_ASK_FOR_PROFILE_PICTURE, false);

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
        int friendRequestsVisibility, messagesVisibility;

        private boolean hasChanged = false;

        public FetchRequestsAndMessagesTask() {
        }

        @Override
        public void run() {
            try {
                RequestsInterface requestsInterface = new RequestsInterface(
                        activity.getApplicationContext());
                ServerResponse<Pair<Integer, Integer>> sr = requestsInterface.getUserStats(currentUser);

                if (sr.isSuccess()) {
                    Pair<Integer, Integer> pair = sr.getResult();

                    StringBuilder sbFriendsRequests = new StringBuilder();
                    StringBuilder sbMessages = new StringBuilder();

                    if (pair.first != null && pair.first > 0) {
                        if (pair.first > 99) {
                            sbFriendsRequests.append("99+");
                        } else {
                            sbFriendsRequests.append(pair.first);
                        }

                        if(currentUser.getNumOfFriendsRequests() != pair.first) {
                            hasChanged = true;
                        } else {
                            hasChanged = false;
                        }

                        friendRequestsVisibility = View.VISIBLE;
                        currentUser.setNumOfFriendsRequests(pair.first);

                        NavDrawerItem item = navDrawerItems[NavigationDrawerItems.FRIENDS_REQUESTS.index];
                        item.setSubtext(sbFriendsRequests.toString());
                    } else {
                        friendRequestsVisibility = View.INVISIBLE;
                        sbFriendsRequests.append(0);

                        if(pair.first == 0) {
                            hasChanged = false;
                        } else {
                            hasChanged = true;
                        }
                    }

                    if(hasChanged) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                navigationDrawerAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    if (pair.second != null && pair.second > 0) {
                        if (pair.second > 99)
                            sbMessages.append("99+");
                        else
                            sbMessages.append(pair.second);
                    } else {
                        messagesVisibility = View.INVISIBLE;
                        sbMessages.append(0);
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
