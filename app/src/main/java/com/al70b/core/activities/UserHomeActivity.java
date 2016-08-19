package com.al70b.core.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.user_home_activity_underlying.FriendsAndChatDrawer;
import com.al70b.core.activities.user_home_activity_underlying.NavigationDrawer;
import com.al70b.core.activities.user_home_activity_underlying.NavigationDrawerController;
import com.al70b.core.fragments.BackPressedFragment;
import com.al70b.core.fragments.UserConversationsFragment;
import com.al70b.core.fragments.UserDataFragment;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.KEYS;
import com.al70b.core.notifications.GcmModule;
import com.al70b.core.objects.CurrentUser;

/**
 * Created by Naseem on 5/7/2015.1
 */
public class UserHomeActivity extends FragmentActivity {

    // Static Declarations
    public static final String TAG_EXIT = "EXIT";   // used for identifying when to exit the application
    public static final String KEY_LAST_ITEM_SELECTED = "com.al70b.core.activities.UserHomeActivity.selectedItem";
    public static final String KEY_CURRENT_USER = "com.al70b.core.activities.UserHomeActivity.currentUser";
    private static final String TAG = "UserHomeActivity";

    /////// Current Activity Declarations ///////

    // activity title
    private CharSequence title;

    // current currentUser
    private CurrentUser currentUser;

    // drawer layout, drawer toggle, and both list views
    private DrawerLayout drawerLayout;
    private CustomActionBarDrawerToggle mDrawerToggle;
    private NavigationDrawerController navigationDrawerController;
    private FriendsAndChatDrawer friendsAndChatDrawerController;

    public boolean toUserData;

    // boolean representing the the application is logging out of this user account
    private boolean logginOut;

    private MenuItem chatItem;                                  // menu item that is highlighted when message received

    // when currentUser has just logged in
    private boolean hasJustLoggedIn = true;
    private boolean backPressed;

    private SparseArray<Integer> unreadMessagesUsersIDs = new SparseArray<>();

    /**
     * Logout this current user:
     *   - Delete currentUser's data from the shared pref
     *   -
     */
    public void logout() {
        logginOut = true;

        friendsAndChatDrawerController.logout();

        SharedPreferences sharedPref = getSharedPreferences(AppConstants.SHARED_PREF_FILE,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(KEYS.SHARED_PREFERENCES.USER_ID);
        editor.remove(KEYS.SHARED_PREFERENCES.ACCESS_TOKEN);
        editor.remove(KEYS.SHARED_PREFERENCES.USERNAME);
        editor.remove(KEYS.SHARED_PREFERENCES.NAME);
        editor.remove("DONT_ASK");
        editor.apply();

        new GcmModule(this, currentUser).deleteRegistrationIdFromBackend();

        MyApplication myApp = ((MyApplication)getApplication());
        if (myApp != null) {
            myApp.setCurrentUser(null);
        }

        // show a toast appropriate logout message
        Toast.makeText(this, getString(R.string.logout_message),
                Toast.LENGTH_SHORT).show();

        // start the ScreenSlideHome activity
        Intent intent = new Intent(this, ScreenSlideHomeActivity.class);
        startActivity(intent);

        // close this activity
        finish();
    }

    public void restartApplication() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            Log.d("SavedInstanceState", "is not Null");
        } else {
            Log.d("SavedInstanceState", "is Null");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        /*        P R E P A R E     U S E R ' S     D A T A        */
        // if activity is reloaded, restore user from savedInstanceState
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_CURRENT_USER)) {
            currentUser = (CurrentUser) savedInstanceState.getSerializable(KEY_CURRENT_USER);
        }

        // get passed bundle to get currentUser object
        Bundle bundle = getIntent().getExtras();

        // check if currentUser login is valid, otherwise jump to guest mode
        if (bundle == null) {
            // something went wrong with loading the bundle
            Toast.makeText(this, "FATAL ERROR!! BUNDLE WAS NOT SET, LOGGING OUT", Toast.LENGTH_SHORT).show();
            //logout();
            return;
        }

        // get currentUser object from intent if still empty
        if (currentUser == null) {

            currentUser = (CurrentUser) bundle.getSerializable(KEYS.SHARED_PREFERENCES.USER);

            // if still no user
            if (currentUser == null) {
                Toast.makeText(this, "FATAL ERROR!! NO USER, LOGGING OUT", Toast.LENGTH_SHORT).show();
                logout();
                return;
            }
        }

        /*        P R E P A R E     D R A W E R     L A Y O U T        */
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.parseColor("#33000000"));
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.END);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow_right, GravityCompat.START);

        /*        I N I T I A L I Z E     N A V I G A T I O N     D R A W E R        */
        navigationDrawerController = new NavigationDrawer(this, drawerLayout, currentUser);

        /*        I N I T I A L I Z E     F R I E N D S     D R A W E R        */
        friendsAndChatDrawerController = new FriendsAndChatDrawer(this, drawerLayout, currentUser);


        if (getActionBar() != null) {
            // enable ActionBar app icon to behave as action to toggle navigation drawer
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayShowTitleEnabled(true);
            getActionBar().setDisplayUseLogoEnabled(false);
            getActionBar().setIcon(R.drawable.ic_drawer);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new CustomActionBarDrawerToggle(this, drawerLayout,
                navigationDrawerController.getDrawerLayout(),
                friendsAndChatDrawerController.getDrawerLayout(),
                R.drawable.ic_drawer
                , R.string.open_drawer, R.string.close_drawer);

        // set drawer listener to the activity drawer layout
        drawerLayout.setDrawerListener(mDrawerToggle);


        int selectedItem;
        if (savedInstanceState != null) {
            selectedItem = savedInstanceState.getInt(KEY_LAST_ITEM_SELECTED, 1);
        } else {
            // choose the fragment to be shown at first
            selectedItem = 1;
        }

        navigationDrawerController.navigateTo(selectedItem);
    }

    public void takeMeTo(Fragment fragment) {
        takeMeTo(fragment, null);
    }

    public void takeMeTo(Fragment fragment, Bundle bundle) {
        navigationDrawerController.navigateTo(fragment, bundle);
    }
    @Override
    public void onStart() {
        super.onStart();

        // start currentUser statistics job
        navigationDrawerController.activityStart();
        friendsAndChatDrawerController.activityStart();

        if (hasJustLoggedIn) {
            // open drawer on start so currentUser sees friend requests and messages
            drawerLayout.openDrawer(navigationDrawerController.getDrawerLayout());

            hasJustLoggedIn = false;
        }

        if (toUserData) {
            // if the activity was started after taken a picture or picking a picture
            // go back to the currentUser's picture fragment
            //selectItem(1);
            toUserData = false;
            ((UserDataFragment)navigationDrawerController.getVisibleFragment())
                    .goToMyPictures();
        }

        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onStop() {
        super.onStop();

        navigationDrawerController.activityStop();
        friendsAndChatDrawerController.activityStop();

        ((MyApplication) getApplication()).setAppInvisible();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putInt(KEY_LAST_ITEM_SELECTED, navigationDrawerController.getSelectedItem());
        bundle.putSerializable(KEY_CURRENT_USER, currentUser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_home, menu);

        chatItem = menu.findItem(R.id.action_friends);

        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                // The action bar home/up action should open or close the drawer.
                if (drawerLayout.isDrawerOpen(friendsAndChatDrawerController.getDrawerLayout())) {
                    drawerLayout.closeDrawer(friendsAndChatDrawerController.getDrawerLayout());
                }
                // the other situations are handled here
                mDrawerToggle.onOptionsItemSelected(item);
                return true;
            case R.id.action_friends:
                if (drawerLayout.isDrawerOpen(navigationDrawerController.getDrawerLayout())) {
                    drawerLayout.closeDrawer(navigationDrawerController.getDrawerLayout());
                } else if (drawerLayout.isDrawerOpen(friendsAndChatDrawerController.getDrawerLayout())) {
                    drawerLayout.closeDrawer(friendsAndChatDrawerController.getDrawerLayout());

                    return true;
                }
                drawerLayout.openDrawer(friendsAndChatDrawerController.getDrawerLayout());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence t) {
        this.title = t;

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        Fragment currentShownFragment = navigationDrawerController.getVisibleFragment();

        // if a drawer is open probably currentUser meant to close it
        if (drawerLayout.isDrawerOpen(navigationDrawerController.getDrawerLayout()))
            drawerLayout.closeDrawer(navigationDrawerController.getDrawerLayout());
        else if (drawerLayout.isDrawerOpen(friendsAndChatDrawerController.getDrawerLayout()))
            drawerLayout.closeDrawer(friendsAndChatDrawerController.getDrawerLayout());
        else {
            if (currentShownFragment.getTag().compareTo(UserConversationsFragment.INTERNAL_CONVERSATION_TAG) == 0) {
                boolean handled = ((BackPressedFragment) currentShownFragment).onBackPressed();

                if (!handled)
                    super.onBackPressed();
            } else if (currentShownFragment.getTag().contains(TAG_EXIT)) {
                // if no drawer is open
                if (!backPressed) {
                    // tell the currentUser to press again back to exit, prepare back pressed boolean
                    Toast.makeText(this, getResources().getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();
                    backPressed = true;

                    // the currentUser should press back in 3 seconds for otherwise
                    // backPressed is back to false
                    /*handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            backPressed = false;
                        }
                    }, 3 * 1000);*/
                } else {
                    // currentUser pressed back twice, close the application
                    super.onBackPressed();
                }
            } else {
                // fragment that doesn't require exit from application
                super.onBackPressed();
            }
        }
    }

    public void navigateTo(int position) {
        navigationDrawerController.navigateTo(position);
    }

    public Fragment getVisibleFragment() {
        return navigationDrawerController.getVisibleFragment();
    }

    public void updateProfilePictureInNavigationDrawer(int res) {
        navigationDrawerController.updateProfilePicture(res);
    }

    public void updateProfilePictureInNavigationDrawer(String path) {
        navigationDrawerController.updateProfilePicture(path);
    }

    public void closeDrawers() {
        if (drawerLayout.isDrawerOpen(navigationDrawerController.getDrawerLayout()))
            drawerLayout.closeDrawer(navigationDrawerController.getDrawerLayout());

        if (drawerLayout.isDrawerOpen(friendsAndChatDrawerController.getDrawerLayout()))
            drawerLayout.closeDrawer(friendsAndChatDrawerController.getDrawerLayout());
    }

    public boolean isNavigationDrawerOpen() {
        return drawerLayout.isDrawerOpen(navigationDrawerController.getDrawerLayout());
    }

    public boolean isFriendsDrawerOpen() {
        return drawerLayout.isDrawerOpen(friendsAndChatDrawerController.getDrawerLayout());
    }

    public void lockDrawers(boolean flag) {
        if (flag)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        else
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
/*
    private void notifyFriendsDrawerMessageReceived(final int otherUserID) {
        for (FriendsDrawerItem item : onlineFriends) {
            if (item.id == otherUserID) {

                // get current shown fragment
                UserConversationsInternalFragment fragment
                        = (UserConversationsInternalFragment) getSupportFragmentManager()
                        .findFragmentByTag(UserConversationsFragment.INTERNAL_CONVERSATION_TAG);

                // in case open conversation is not with the same currentUser
                if (fragment != null && fragment.isVisible() && otherUserID == fragment.otherUserID()) {
                    // new message is with current conversation
                    item.isMessageUnread = false;
                    notifyUser = false;

                    if (chatItem != null)
                        chatItem.setIcon(R.drawable.ic_action_group);
                } else {
                    item.isMessageUnread = true;
                    notifyUser = true;  // notify currentUser with message and chatItem Highlight

                    if (chatItem != null)
                        chatItem.setIcon(R.drawable.ic_action_group_chat);

                    unreadMessagesUsersIDs.put(otherUserID, otherUserID);
                }

                // set conversation at top of the list either ways
                onlineFriends.remove(item);
                onlineFriends.add(0, item);

                break;
            }
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                friendsDrawerAdapter.notifyDataSetChanged();
            }
        });

    }
*/

    private class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {

        private Activity activity;
        private ViewGroup navDrawerLayout, chatDrawerLayout;
        private ActionBar actionBar;

        /* host Activity */
        /* DrawerLayout object */
        /* nav drawer image to replace 'Up' caret */
        /* "open drawer" description for accessibility */
        /* "close drawer" description for accessibility */
        private CustomActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
                                            ViewGroup navDrawerLayout, ViewGroup chatDrawerLayout,
                                            int navDrawerIcon, int openDrawerStr, int closeDrawerStr) {
            super(activity, drawerLayout, navDrawerIcon, openDrawerStr, closeDrawerStr);
            this.activity = activity;
            this.navDrawerLayout = navDrawerLayout;
            this.chatDrawerLayout = chatDrawerLayout;
            this.actionBar = activity.getActionBar();
        }

        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            if (actionBar == null)
                return;

            actionBar.setTitle(title);
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(drawerLayout.getWindowToken(), 0);

            // change activity title only when activity is on navigation drawer
            if (drawerLayout.isDrawerOpen(navDrawerLayout)) {
                actionBar.setTitle(getResources().getString(R.string.choose_from_list));
            }

            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item != null && item.getItemId() == android.R.id.home) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
            return false;
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        navigationDrawerController.activityPause();
    }

    @Override
    public void onDestroy() {
        navigationDrawerController.activityDestroy();
        friendsAndChatDrawerController.activityDestroy();

        super.onDestroy();
    }

}
