package com.al70b.core.activities.user_home_activity_underlying;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
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

    // track current item & visible fragment
    private int selectedItem = -1;
    private Fragment currentShownFragment;

    // declare timer task running every 10 seconds
    private Timer fetchRequestsAndMessagesTimer;
    private static final int RATE_TO_FETCH_REQUESTS = 10 * 1000; // in milliseconds

    private void init() {
        /*          N A V I G A T I O N      D R A W E R          */

        // relate navigation drawer components to xml elements
        // this is the main layout of the drawer
        navDrawerLayout = (LinearLayout) root.findViewById(R.id.layout_navigation_drawer);

        // Drawer Header
        ViewGroup navHeader = (ViewGroup) navDrawerLayout.findViewById(R.id.layout_navigation_drawer_header);
        CircleImageView cmUserProfilePicture = (CircleImageView) navHeader.findViewById(R.id.circle_image_view_drawer_profile_image);
        TextView txtViewName = (TextView) navHeader.findViewById(R.id.text_view_drawer_header_name);
        TextView txtViewEmail = (TextView) navHeader.findViewById(R.id.text_view_drawer_header_email);
        ImageView imgViewFriendsRequests = (ImageView) navHeader.findViewById(R.id.img_view_navigation_header_friends_request);
        ImageView imgViewMessages = (ImageView) navHeader.findViewById(R.id.img_view_navigation_header_messages);

        // Drawer Main List
        navDrawerList = (ListView) navDrawerLayout.findViewById(R.id.list_navigation_drawer);

        // Drawer Footer
        //ViewGroup navFooter = (ViewGroup) navDrawerLayout.findViewById(R.id.layout_navigation_drawer_footer);

        imgViewFriendsRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, UsersListActivity.class);
                intent.putExtra(FriendsListActivity.NUMBER_OF_FRIENDS_REQUESTS,
                        currentUser.getNumOfFriendsRequests());
                activity.startActivity(intent);
            }
        });

        imgViewMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to conversations fragment
                selectItem(2);
            }
        });

        // profile picture can be clicked, click activates changing the picture dialog
        cmUserProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to user's fragment
                selectItem(1);

                if (currentShownFragment instanceof UserDataFragment) {
                    ((UserDataFragment) currentShownFragment).goToUserPictures();
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
                    .centerCrop()
                    .placeholder(R.drawable.avatar)
                    .into(cmUserProfilePicture);
        } else {
            // prompt user to add a profile picture
            promptUserForProfilePictureUpdate();
        }

        // load currentUser's name and email to the appropriate views
        txtViewName.setText(currentUser.getName());
        txtViewEmail.setText(currentUser.getEmail());

        // create & set the adapter for the navigation drawer list view
        NavigationDrawerAdapter navDrawerAdapter = new NavigationDrawerAdapter(activity,
                buildNavigationDrawerItems());
        navDrawerList.setAdapter(navDrawerAdapter);

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

        // General header - Section
        navDrawerItems[0] = new NavDrawerItem(drawerValues[0]);

        // User Account
        navDrawerItems[1] = new NavDrawerItem(drawerValues[1],
                R.drawable.ic_action_user, R.drawable.ic_action_user_holo_dark);

        // Conversations
        navDrawerItems[2] = new NavDrawerItem(drawerValues[2],
                R.drawable.ic_action_messages, R.drawable.ic_action_messages_holo_dark);

        // Members
        navDrawerItems[3] = new NavDrawerItem(drawerValues[3],
                R.drawable.ic_action_users, R.drawable.ic_action_users_holo_dark);

        // Advanced Search
        navDrawerItems[4] = new NavDrawerItem(drawerValues[4],
                R.drawable.ic_action_search, R.drawable.ic_action_search_holo_dark);


        // Actions header - Section
        navDrawerItems[5] = new NavDrawerItem(drawerValues[5]);

        // Settings
        navDrawerItems[6] = new NavDrawerItem(drawerValues[6],
                R.drawable.ic_action_settings, R.drawable.ic_action_exit_holo_dark);

        // Close account
        navDrawerItems[7] = new NavDrawerItem(drawerValues[7],
                R.drawable.ic_action_exit, R.drawable.ic_action_exit_holo_dark);

        // Logout
        navDrawerItems[8] = new NavDrawerItem(drawerValues[8],
                R.drawable.ic_action_exit, R.drawable.ic_action_exit_holo_dark);

        return navDrawerItems;
    }


    private void selectItem(int position) {

        // get fragment
        Fragment fragment = activity.getSupportFragmentManager().findFragmentByTag(
                position + "_" + UserHomeActivity.TAG_EXIT);

        if (fragment == null) {
            switch (position) {
                case 0: // section: General
                    return;
                case 1:
                    fragment = new UserDataFragment();
                    break;
                case 2:
                    fragment = new UserConversationsFragment();
                    break;
                case 3:
                    fragment = new UserBasicSearchFragment();
                    break;
                case 4:
                    fragment = new UserAdvancedSearchFragment();
                    break;
                case 5: // section: Others
                    return;
                case 6:
                    fragment = new UserSettingsFragment();
                    break;
                case 7:
                    fragment = new UserCloseAccountFragment();
                    break;
                case 8:
                    // close the drawer
                    root.closeDrawer(navDrawerLayout);
                    logout();
                default:
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
        TextView txtViewFriendsRequests = (TextView) navDrawerLayout.findViewById(
                R.id.text_view_navigation_header_friends_request);
        TextView txtViewMessages = (TextView) navDrawerLayout.findViewById(
                R.id.text_view_navigation_header_messages);

        fetchRequestsAndMessagesTimer = new Timer("FetchRequestsAndMessagesTimer", true);
        fetchRequestsAndMessagesTimer.scheduleAtFixedRate(
                new FetchRequestsAndMessagesTask(txtViewFriendsRequests, txtViewMessages),
                0,
                RATE_TO_FETCH_REQUESTS);

        return true;
    }

    private class FetchRequestsAndMessagesTask extends TimerTask {
        int friendRequestsVisibility, messagesVisibility;

        private TextView tvFriendsRequests, tvMessages;

        public FetchRequestsAndMessagesTask(TextView tvFriendsRequests, TextView tvMessages) {
            this.tvFriendsRequests = tvFriendsRequests;
            this.tvMessages = tvMessages;
        }

        @Override
        public void run() {
            try {
                RequestsInterface requestsInterface = new RequestsInterface(
                        activity.getApplicationContext());
                ServerResponse<Pair<Integer, Integer>> sr = requestsInterface.getUserStats(currentUser);

                if (sr.isSuccess()) {
                    Pair<Integer, Integer> pair = sr.getResult();

                    final StringBuilder sbFriendsRequests = new StringBuilder();
                    final StringBuilder sbMessages = new StringBuilder();

                    if (pair.first != null && pair.first > 0) {
                        if (pair.first > 99)
                            sbFriendsRequests.append("99+");
                        else
                            sbFriendsRequests.append(pair.first);
                        friendRequestsVisibility = View.VISIBLE;
                    } else {
                        friendRequestsVisibility = View.INVISIBLE;
                        sbFriendsRequests.append(0);
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

                    // update messages text view
                    tvMessages.post(new Runnable() {
                        @Override
                        public void run() {
                            tvMessages.setText(sbMessages.toString());
                            tvMessages.setVisibility(messagesVisibility);
                        }
                    });

                    // update friends requests text view
                    tvFriendsRequests.post(new Runnable() {
                        @Override
                        public void run() {
                            tvFriendsRequests.setText(sbFriendsRequests.toString());
                            tvFriendsRequests.setVisibility(friendRequestsVisibility);
                        }
                    });
                }
            } catch (ServerResponseFailedException ex) {

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
    public void updateProfilePicture(Bitmap bitmap) {
        CircleImageView cmUserProfilePicture = (CircleImageView) navDrawerLayout.findViewById(
                R.id.circle_image_view_drawer_profile_image);
        cmUserProfilePicture.setImageBitmap(bitmap);
        cmUserProfilePicture.invalidate();
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


    public void updateProfilePicture(int res) {
        CircleImageView cmUserProfilePicture = (CircleImageView) navDrawerLayout.findViewById(
                R.id.circle_image_view_drawer_profile_image);
        cmUserProfilePicture.setImageResource(res);
        cmUserProfilePicture.invalidate();
    }
}
