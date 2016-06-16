package com.al70b.core.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.AVCall.AVChatActivity;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.StatusList;
import com.al70b.core.fragments.Dialogs.QuestionAlert2;
import com.al70b.core.fragments.Dialogs.ReportUserAlert;
import com.al70b.core.fragments.BackPressedFragment;
import com.al70b.core.fragments.UserAdvancedSearchFragment;
import com.al70b.core.fragments.UserBasicSearchFragment;
import com.al70b.core.fragments.UserConversationsFragment;
import com.al70b.core.fragments.UserConversationsInternalFragment;
import com.al70b.core.fragments.UserDataFragment;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.JSONHelper;
import com.al70b.core.notifications.GcmModule;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.EndMessage;
import com.al70b.core.objects.FriendsDrawerItem;
import com.al70b.core.objects.Message;
import com.al70b.core.objects.NavDrawerItem;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.Picture;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.objects.User;
import com.al70b.core.server_methods.RequestsInterface;
import com.al70b.core.server_methods.ServerConstants;
import com.bumptech.glide.Glide;
import com.inscripts.callbacks.Callbacks;
import com.inscripts.callbacks.SubscribeCallbacks;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.keys.StatusOption;
import com.inscripts.utils.Logger;
import com.inscripts.utils.SessionData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 5/7/2015.1
 */
public class UserHomeActivity extends FragmentActivity {

    public static final String TAG_EXIT = "EXIT";   // used for identifying when to exit the application
    // Static Declarations
    private static final String LAST_ITEM_SELECTED = "com.al70b.core.activities.UserHomeActivity.selectedItem";
    private static final String THIS_USER = "com.al70b.core.activities.UserHomeActivity.thisUser";
    // TODO change this to a more realistic time
    private static final int TIME_TO_REQUEST_USER_STATISTICS = 30 * 1000; // ten seconds for test
    private static UserHomeActivity thisActivity;   // thisUser running activity
    private static CurrentUser thisUser;            // current thisUser
    public boolean toUserData;
    int chatLoginTries = 4;
    /**
     * * Current Activity Declarations ***
     */

    // title
    private CharSequence title;
    // drawer layout, drawer toggle, and both list views
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout navDrawerLayout, chatDrawerLayout;
    private ListView navDrawerList, chatListView;
    // navigation drawer array, adapter, and values
    private NavDrawerItem[] navDrawerItems;
    private NavigationDrawerAdapter navDrawerAdapter;
    private String[] navDrawerValues;
    private int selectedItem = 1;
    // friends drawer adapter and values
    private List<FriendsDrawerItem> onlineFriends;     // list of online friends of the current thisUser
    private FriendsDrawerAdapter friendsDrawerAdapter;
    // hold the current shown fragment
    private Fragment currentShownFragment;
    // Comet Chat object for related chatting matters
    private CometChat cometChat;
    // Requests interface to make requests to server
    private RequestsInterface requestsInterface;
    // handler
    private Handler handler;
    // some widgets that need to be updated every once and a while
    private CircleImageView status, cmUserProfilePicture;       // thisUser status, thisUser profile picture
    private StatusList statusList;                              // status list view
    private TextView txtViewMessages, txtViewFriendsRequests, txtViewFriendEmpty;   // text to display messages and friends requests
    private EditText statusEditText, searchFriendEditText;                            // text to show thisUser's status message
    private MenuItem chatItem;                                  // menu item that is highlighted when message received
    private LinearLayout layoutChatOk, layoutChatFailed;
    private ProgressBar chatConnectionProgress;

    private ImageButton imgBtnSetStatus, btnSettings;
    /**
     * * Current User Declarations ***
     */

    // list of blocked users of this thisUser
    private List<Pair<String, String>> blockedUsersList;
    // when thisUser has just logged in
    private boolean hasJustLoggedIn = true;
    private boolean backPressed;
    // thisUser clicked on status btn
    private boolean statusBtnClicked;
    // thisUser status message in case of cancel changing status message
    private String backupStatusMessage;
    // notify thisUser of receiving messages or friend requests
    private boolean notifyUser = true;
    private SparseArray<Integer> unreadMessagesUsersIDs = new SparseArray<>();
    private Runnable userStatsRunnable;

    public static UserHomeActivity getUserHomeActivity() {
        if (thisActivity == null) {
            thisActivity = new UserHomeActivity();
        }

        return thisActivity;
    }

    public static CurrentUser getCurrentUser() {

        //while (thisUser == null) {
        //    Log.d("WaitingHAHA", "waiting for the thing to happen");

        //}
        Log.d("WaitingHAHA", "The thing happened");


        return thisUser;
    }

    /**
     * Delete thisUser's data from the shared pref
     */
    public static void preLogout(Context c) {
        SharedPreferences sharedPref = c.getSharedPreferences(AppConstants.SHARED_PREF_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(JSONHelper.USER_ID);
        editor.remove(JSONHelper.ACCESS_TOKEN);
        editor.remove(JSONHelper.USERNAME);
        editor.remove(JSONHelper.NAME);
        editor.remove("DONT_ASK");
        editor.apply();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (thisActivity != null)
                    new GcmModule(thisActivity, thisUser).deleteRegistrationIdFromBackend();
            }
        }).start();

        if (getUserHomeActivity().cometChat != null && CometChat.isLoggedIn())
            thisActivity.cometChat.logout(new Callbacks() {
                @Override
                public void successCallback(JSONObject jsonObject) {
                    Logger.debug(jsonObject.toString());
                }

                @Override
                public void failCallback(JSONObject jsonObject) {
                    Logger.error(jsonObject.toString());
                    thisActivity.cometChat.logout(this);
                }
            });

        thisUser = null;

        MyApplication myApp = ((MyApplication)thisActivity.getApplication());
        if (myApp != null)
            myApp.setCurrentUser(null);

        thisActivity = null;
    }

    public void restartApplication() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // if this is not the first time, restore user from savedInstanceState
        if (savedInstanceState != null && savedInstanceState.containsKey(THIS_USER))
            thisUser = (CurrentUser) savedInstanceState.getSerializable(THIS_USER);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // get passed bundle to get thisUser object
        Bundle bundle = getIntent().getExtras();

        // set this to be thisUser home activity for further use
        thisActivity = this;

        // this handler is used later for posted delayed jobs
        handler = new Handler();

        // initialize the requests interface
        requestsInterface = new RequestsInterface(getApplicationContext());

        // check if thisUser login is valid, otherwise jump to guest mode
        if (bundle != null) {

            /*          P R E P A R E     U S E R ' S      D A T A        */
            // get thisUser object from intent
            if (thisUser == null)
                thisUser = (CurrentUser) bundle.getSerializable(JSONHelper.USER);


            /*          P R E P A R E    D R A W E R    L A Y O U T   */
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerLayout.setScrimColor(Color.parseColor("#44000000"));
            drawerLayout.setDrawerShadow(R.drawable.drawer_shadow_right, Gravity.RIGHT);
            drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);

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
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    drawerLayout,         /* DrawerLayout object */
                    R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                    R.string.open_drawer,  /* "open drawer" description for accessibility */
                    R.string.close_drawer  /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {

                    super.onDrawerClosed(view);
                    getActionBar().setTitle(title);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(drawerLayout.getWindowToken(), 0);


                    // change activity title only when activity is on navigation drawer
                    if (drawerLayout.isDrawerOpen(navDrawerLayout)) {
                        getActionBar().setTitle(getResources().getString(R.string.choose_from_list));

                        // set a custom shadow that overlays the src content when the drawer opens
                    } else if (drawerLayout.isDrawerOpen(chatDrawerLayout)) {
                        // set a custom shadow that overlays the src content when the drawer opens
                    }

                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                @Override
                public boolean onOptionsItemSelected(MenuItem item) {
                    if (item != null && item.getItemId() == android.R.id.home) {
                        if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                            drawerLayout.closeDrawer(Gravity.RIGHT);
                        } else {
                            drawerLayout.openDrawer(Gravity.RIGHT);
                        }
                    }
                    return false;
                }
            };

            // set drawer listener to the activity drawer layout
            drawerLayout.setDrawerListener(mDrawerToggle);


            /*          N A V I G A T I O N      D R A W E R          */
            // title of the activity
            title = getTitle();

            // get navigation drawer values from string array
            navDrawerValues = getResources().getStringArray(R.array.navigation_drawer_values);

            // relate navigation drawer components to xml elements
            navDrawerLayout = (LinearLayout) findViewById(R.id.layout_navigation_drawer);
            navDrawerList = (ListView) findViewById(R.id.list_navigation_drawer);

            // create header for navigation drawer and add it to the drawer list
            ViewGroup navHeader = (ViewGroup) getLayoutInflater().inflate(R.layout.navigation_drawer_header, null, false);
            cmUserProfilePicture = (CircleImageView) navHeader.findViewById(R.id.circle_image_view_drawer_profile_image);
            TextView txtViewName = (TextView) navHeader.findViewById(R.id.text_view_drawer_header_name);
            TextView txtViewEmail = (TextView) navHeader.findViewById(R.id.text_view_drawer_header_email);
            txtViewFriendsRequests = (TextView) navHeader.findViewById(R.id.text_view_navigation_header_friends_request);
            txtViewMessages = (TextView) navHeader.findViewById(R.id.text_view_navigation_header_messages);
            ImageView imgViewFriendsRequests = (ImageView) navHeader.findViewById(R.id.img_view_navigation_header_friends_request);
            ImageView imgViewMessages = (ImageView) navHeader.findViewById(R.id.img_view_navigation_header_messages);

            imgViewFriendsRequests.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int num = Integer.parseInt(txtViewFriendsRequests.getText().toString());

                    Intent intent = new Intent(thisActivity, FriendsListActivity.class);
                    intent.putExtra(MembersListActivity.NUMBER_OF_FRIENDS_REQUESTS, num);
                    startActivity(intent);
                }
            });


            imgViewMessages.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedItem = 3;
                    selectItem(selectedItem);
                }
            });

            // profile picture can be clicked, click activates changing the picture dialog
            cmUserProfilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedItem = 2;
                    selectItem(selectedItem);
                    ((UserDataFragment) currentShownFragment).goToUserPictures();
                }
            });

            // if profile picture set for this user
            if (thisUser.isProfilePictureSet()) {
                Glide.with(thisActivity)
                        .load(thisUser.getProfilePicture().getThumbnailFullPath())
                        .asBitmap()
                        .fitCenter()
                        .centerCrop()
                        .placeholder(R.drawable.default_user_photo)
                        .into(cmUserProfilePicture);
            } else {
                SharedPreferences sharedPref = thisActivity.getSharedPreferences(AppConstants.SHARED_PREF_FILE, MODE_PRIVATE);
                boolean dontAsk = sharedPref.getBoolean("DONT_ASK", false);

                if (!dontAsk) {
                    final QuestionAlert2 alert = new QuestionAlert2(this, getString(R.string.question), getString(R.string.you_have_not_uploaded_photo), -1, R.string.yes, R.string.no);
                    alert.show();
                    alert.setCanceledOnTouchOutside(false);

                    alert.setNegativeButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (alert.dontAskMeChecked()) {
                                SharedPreferences sharedPref = thisActivity.getSharedPreferences(AppConstants.SHARED_PREF_FILE, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("DONT_ASK", true);
                                editor.apply();
                            }

                            alert.dismiss();
                        }
                    });
                    alert.setPositiveButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            cmUserProfilePicture.callOnClick();

                            if (alert.dontAskMeChecked()) {
                                SharedPreferences sharedPref = thisActivity.getSharedPreferences(AppConstants.SHARED_PREF_FILE, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("DONT_ASK", true);
                                editor.apply();
                            }

                            alert.dismiss();
                        }
                    });
                }

            }


            // load thisUser's name and email to the appropriate views
            txtViewName.setText(thisUser.getName());
            txtViewEmail.setText(thisUser.getEmail());

            // header itself can't be clicked
            navHeader.setEnabled(false);
            navHeader.setClickable(false);
            navHeader.setOnClickListener(null);

            // add the header that was created to the navigation drawer list view
            navDrawerLayout.addView(navHeader);

            ViewGroup navFooter = (ViewGroup) getLayoutInflater().inflate(R.layout.navigation_drawer_footer, navDrawerLayout, false);
            navFooter.setEnabled(false);
            navFooter.setClickable(false);
            navFooter.setOnClickListener(null);
            navDrawerLayout.addView(navFooter);

            // set the adapter for the navigation drawer list view
            navDrawerAdapter = new NavigationDrawerAdapter(this, buildNavigationDrawerItems());
            navDrawerList.setAdapter(navDrawerAdapter);

            // and set the listener for item click event
            navDrawerList.setOnItemClickListener(new NavigationDrawerItemClickListener());
            navDrawerList.setOnItemLongClickListener(null);



            /*          F R I E N D S      D R A W E R          */
            // relate friends drawer components to xml elements

            chatDrawerLayout = (LinearLayout) findViewById(R.id.layout_chat_drawer);

            // // chat layout
            chatListView = (ListView) findViewById(R.id.list_view_chat);

            layoutChatFailed = (LinearLayout) findViewById(R.id.layout_friends_drawer_failed_connecting);
            txtViewFriendEmpty = (TextView) findViewById(R.id.tv_friends_drawer_message);
            chatConnectionProgress = (ProgressBar) findViewById(R.id.progress_bar_friends_drawer_connecting);

            //  header
            ViewGroup chatDrawerHeader = (ViewGroup) findViewById(R.id.layout_chat_drawer_header);
            searchFriendEditText = (EditText) chatDrawerHeader.findViewById(R.id.et_friends_drawer_header_search);

            //  footer
            final ViewGroup friendsFooter = (ViewGroup) findViewById(R.id.layout_chat_drawer_footer);
            statusEditText = (EditText) friendsFooter.findViewById(R.id.et_friends_drawer_footer_status);
            imgBtnSetStatus = (ImageButton) friendsFooter.findViewById(R.id.img_btn_friends_drawer_footer_set);
            btnSettings = (ImageButton) friendsFooter.findViewById(R.id.img_btn_friends_drawer_footer_settings);
            statusList = (StatusList) friendsFooter.findViewById(R.id.drawer_footer_status_list);

            statusEditText.setText(getString(R.string.not_connected_status));
            imgBtnSetStatus.setVisibility(View.INVISIBLE);

            // initialize Comet chat
            initChat();

            // update status and status message
            String str;
            if (thisUser.getOnlineStatus().getStatus() == StatusOption.BUSY)
                str = "center";
            else if (thisUser.getOnlineStatus().getStatus() == StatusOption.OFFLINE
                    || thisUser.getOnlineStatus().getStatus() == StatusOption.INVISIBLE)
                str = "left";
            else
                str = "right";

            statusList.updateStatus(str);
            statusEditText.setText(getString(R.string.not_connected_status));

            statusEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        imgBtnSetStatus.setVisibility(View.VISIBLE);
                        backupStatusMessage = statusEditText.getText().toString();
                    } else {
                        if (!statusBtnClicked) {
                            statusEditText.setText(backupStatusMessage);
                            statusBtnClicked = false;
                        }
                        imgBtnSetStatus.setVisibility(View.INVISIBLE);
                    }
                }
            });

            imgBtnSetStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    statusBtnClicked = true;
                    String message = statusEditText.getText().toString();
                    cometChat.setStatusMessage(message, new Callbacks() {
                        @Override
                        public void successCallback(JSONObject jsonObject) {
                            Toast.makeText(getApplicationContext(), getString(R.string.status_message_changed_successfully), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failCallback(JSONObject jsonObject) {
                            statusEditText.setText(backupStatusMessage);
                            Toast.makeText(getApplicationContext(), getString(R.string.status_message_change_failed), Toast.LENGTH_SHORT).show();
                        }
                    });

                    statusEditText.clearFocus();
                }
            });

            btnSettings.setOnClickListener(new View.OnClickListener() {

                private AlertDialog ad;

                @Override
                public void onClick(View view) {
                    if (!CometChat.isLoggedIn())
                        return;

                    final String[] ids = new String[blockedUsersList.size()];
                    final String[] names = new String[blockedUsersList.size()];

                    for (int i = 0; i < blockedUsersList.size(); i++) {
                        Pair<String, String> p = blockedUsersList.get(i);
                        ids[i] = p.first;
                        names[i] = p.second;
                    }

                    if (blockedUsersList.size() > 0) {
                        ad = buildAlertDialogWithList(getString(R.string.blocked_users), names,
                                new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(final AdapterView parent, View view, final int position, long x) {
                                        String id = ids[position];
                                        final String name = names[position];

                                        cometChat.unblockUser(id, new Callbacks() {
                                            @Override
                                            public void successCallback(JSONObject jsonObject) {
                                                Toast.makeText(thisActivity, getString(R.string.unblock_user, name), Toast.LENGTH_SHORT).show();
                                                // remove from the list
                                                blockedUsersList.remove(position);

                                                cometChat.getOnlineUsers(new Callbacks() {
                                                    @Override
                                                    public void successCallback(JSONObject jsonObject) {
                                                        populateFriendsList(jsonObject);
                                                    }

                                                    @Override
                                                    public void failCallback(JSONObject jsonObject) {

                                                    }
                                                });

                                                if (blockedUsersList.size() == 0 && ad != null)
                                                    ad.dismiss();
                                            }

                                            @Override
                                            public void failCallback(JSONObject jsonObject) {

                                            }
                                        });
                                    }
                                });

                        ad.show();
                    } else {
                        /// no blocked users
                        Toast.makeText(thisActivity, getString(R.string.no_blocked_users), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btnSettings.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(getApplicationContext(), getString(R.string.blocked_users_list), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            searchFriendEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (i1 > i2)
                        friendsDrawerAdapter.data = onlineFriends;
                    friendsDrawerAdapter.getFilter().filter(charSequence);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            status = statusList.getVisibleStatusView();
            status.setOnClickListener(new View.OnClickListener() {
                // handle the status click, show other status for thisUser to pick from
                @Override
                public void onClick(View view) {
                    if (statusList.isListExpanded()) {
                        // hide status list
                        statusList.hideGradually();

                        // remove all post delayed for the count starts from zero again
                        handler.removeCallbacksAndMessages(null);
                    } else if(statusList.isEnabled()) {
                        // show status list
                        statusList.showGradually();

                        // create a delayed job for closing the status list if it
                        // is still open after 5 seconds
                        handler.postDelayed(new Runnable() {

                            public void run() {
                                // check if after 5 seconds the thisUser didn't choose a status and close
                                // status list if he didn't
                                if (statusList.isListExpanded())
                                    statusList.hideGradually();
                            }
                        }, 5 * 1000);
                    }
                }
            });

            // create new list for online friends
            onlineFriends = new ArrayList<>();

            friendsDrawerAdapter = new FriendsDrawerAdapter(this, R.layout.friends_drawer_list_item, onlineFriends);

            // set the adapter for the friends drawer list view
            chatListView.setAdapter(friendsDrawerAdapter);

            // and set the listener for item click event
            chatListView.setOnItemClickListener(new FriendsDrawerItemClickListener());
            chatListView.setOnItemLongClickListener(new FriendsDrawerItemLongClickListener());

            if (savedInstanceState != null) {
                selectedItem = savedInstanceState.getInt(LAST_ITEM_SELECTED, 1);
                // Highlight the selected item,
                navDrawerList.setItemChecked(selectedItem, true);
                navDrawerList.setSelection(selectedItem);
                // update the title
                setTitle(navDrawerValues[selectedItem - 1]);
            } else {
                selectedItem = 1;

                // choose the fragment to be shown at first
                selectItem(selectedItem);
            }
        } else {
            // something went wrong with loading the bundle
            Toast.makeText(this, "Bundle wasn't set\\\\ FATAL ERROR", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (thisActivity == null)
            thisActivity = this;

        // start thisUser statistics job
        startUserStatsJob();

        if (hasJustLoggedIn) {
            // open drawer on start so thisUser sees friend requests and messages
            drawerLayout.openDrawer(navDrawerLayout);
            hasJustLoggedIn = false;
        }

        if (toUserData) {
            // if the activity was started after taken a picture or picking a picture
            // go back to the thisUser's picture fragment
            selectItem(1);

            toUserData = false;

            ((UserDataFragment) currentShownFragment).goToUserPictures();
        }

        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onStop() {
        super.onStop();

        // remove all jobs
        handler.removeCallbacks(userStatsRunnable);
        handler.removeCallbacks(null);

        ((MyApplication) getApplication()).setAppInvisible();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putInt(LAST_ITEM_SELECTED, selectedItem);
        bundle.putSerializable(THIS_USER, thisUser);
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
                if (drawerLayout.isDrawerOpen(chatDrawerLayout)) {
                    drawerLayout.closeDrawer(chatDrawerLayout);
                }
                // the other situations are handled here
                mDrawerToggle.onOptionsItemSelected(item);
                return true;
            case R.id.action_friends:
                if (drawerLayout.isDrawerOpen(navDrawerLayout)) {
                    drawerLayout.closeDrawer(navDrawerLayout);
                } else if (drawerLayout.isDrawerOpen(chatDrawerLayout)) {
                    drawerLayout.closeDrawer(chatDrawerLayout);

                    return true;
                }
                drawerLayout.openDrawer(chatDrawerLayout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title.toString();
        getActionBar().setTitle(title);
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

        // if a drawer is open probably thisUser meant to close it
        if (drawerLayout.isDrawerOpen(navDrawerLayout))
            drawerLayout.closeDrawer(navDrawerLayout);
        else if (drawerLayout.isDrawerOpen(chatDrawerLayout))
            drawerLayout.closeDrawer(chatDrawerLayout);
        else {
            if (currentShownFragment.getTag().compareTo(UserConversationsFragment.INTERNAL_CONVERSATION_TAG) == 0) {
                boolean handled = ((BackPressedFragment) currentShownFragment).onBackPressed();

                if (!handled)
                    super.onBackPressed();
            } else if (currentShownFragment.getTag().contains(TAG_EXIT)) {
                // if no drawer is open
                if (!backPressed) {
                    // tell the thisUser to press again back to exit, prepare back pressed boolean
                    Toast.makeText(this, getResources().getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();
                    backPressed = true;

                    // the thisUser should press back in 3 seconds for otherwise
                    // backPressed is back to false
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            backPressed = false;
                        }
                    }, 3 * 1000);
                } else {
                    // thisUser pressed back twice, close the application
                    super.onBackPressed();
                }
            } else {
                // fragment that doesn't require exit from application
                super.onBackPressed();
            }
        }
    }

    public void updateProfilePicture(Bitmap bitmap) {
        cmUserProfilePicture.setImageBitmap(bitmap);
        cmUserProfilePicture.invalidate();
    }

    public void updateProfilePicture(int res) {
        cmUserProfilePicture.setImageResource(res);
        cmUserProfilePicture.invalidate();
    }

    // called by fragments in order to keep up with which fragment is currently shown
    public void updateCurrentShownFragment(Fragment f) {
        currentShownFragment = f;
    }

    /**
     * swap fragments in the content view (when navigation)
     */
    private void selectItem(int position) {
        boolean logout = false;

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(position + "_" + TAG_EXIT);

        if (fragment == null) {
            switch (position) {
                case 0: // header
                case 1: // section: General
                    return;
                case 2:
                    fragment = new UserDataFragment();
                    break;
                case 3:
                    fragment = new UserConversationsFragment();
                    break;
                case 4:
                    fragment = new UserBasicSearchFragment();
                    break;
                case 5:
                    fragment = new UserAdvancedSearchFragment();
                    break;
                case 6: // section: Others
                    return;
                case 7:
                    Toast.makeText(thisActivity, "You pressed \"Settings\"", Toast.LENGTH_SHORT).show();
                    return;
                case 8:
                    Toast.makeText(thisActivity, "You pressed \"Close account\"", Toast.LENGTH_SHORT).show();
                    return;
                case 9:
                    logout = true;
                    break;
                default:
                    fragment = new UserDataFragment();
                    break;
            }
        }

        // update current shown fragment
        currentShownFragment = fragment;

        // Highlight the selected item,
        navDrawerList.setItemChecked(position, true);
        navDrawerList.setSelection(position);


        // check if logout was clicked
        if (!logout) {
            // update the title
            setTitle(navDrawerValues[position - 1]);

            // Insert the fragment by replacing any existing fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment, position + "_" + TAG_EXIT)
                    .commit();

            // close the drawer
            drawerLayout.closeDrawer(navDrawerLayout);
        } else {
            // preLogout was clicked

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // remove saved data before logging out
                    preLogout(getUserHomeActivity());
                }
            }).start();

            // show a toast appropriate preLogout message
            Toast.makeText(this, getResources().getString(R.string.logout_message), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, ScreenSlideHomeActivity.class);

            // start the ScreenSlideHome activity
            startActivity(intent);

            // close this activity
            finish();
        }
    }

    // build navigation drawer items depending on the array of drawerItems
    private NavDrawerItem[] buildNavigationDrawerItems() {
        String[] drawerValues = getResources().getStringArray(R.array.navigation_drawer_values);


        navDrawerItems = new NavDrawerItem[drawerValues.length];

        // General header
        navDrawerItems[0] = new NavDrawerItem(drawerValues[0]);

        // Account
        navDrawerItems[1] = new NavDrawerItem(drawerValues[1], R.drawable.ic_action_user, R.drawable.ic_action_user_holo_dark);

        // Conversations
        navDrawerItems[2] = new NavDrawerItem(drawerValues[2], R.drawable.ic_action_messages, R.drawable.ic_action_messages_holo_dark);

        // Members
        navDrawerItems[3] = new NavDrawerItem(drawerValues[3], R.drawable.ic_action_users, R.drawable.ic_action_users_holo_dark);

        // Advanced Search
        navDrawerItems[4] = new NavDrawerItem(drawerValues[4], R.drawable.ic_action_search, R.drawable.ic_action_search_holo_dark);


        // Actions header
        navDrawerItems[5] = new NavDrawerItem(drawerValues[5]);

        // Settings
        navDrawerItems[6] = new NavDrawerItem(drawerValues[6], R.drawable.ic_action_settings, R.drawable.ic_action_exit_holo_dark);

        // Close account
        navDrawerItems[7] = new NavDrawerItem(drawerValues[7], R.drawable.ic_action_exit, R.drawable.ic_action_exit_holo_dark);

        // Logout
        navDrawerItems[8] = new NavDrawerItem(drawerValues[8], R.drawable.ic_action_exit, R.drawable.ic_action_exit_holo_dark);

        return navDrawerItems;
    }

    private AlertDialog userCommandsAlert(String titleStr, String path, String[] list, ListView.OnItemClickListener itemClickListener) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(thisActivity);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.alert_list2, null);
        TextView title = (TextView) convertView.findViewById(R.id.text_view_alert_list2_title);
        CircleImageView imgView = (CircleImageView) convertView.findViewById(R.id.image_view_alert_list2_icon);
        title.setText(titleStr);

        Glide.with(getApplicationContext())
                .load(path)
                .asBitmap()
                .into(imgView);

        alertDialog.setView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.list_view_alert_list2);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(thisActivity, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(itemClickListener);
        final AlertDialog ad = alertDialog.create();
        ad.setCanceledOnTouchOutside(true);
        /*((Button) convertView.findViewById(R.id.btn_alert_list2_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });*/

        return ad;
    }

    private AlertDialog buildAlertDialogWithList(String titleStr, String[] list, ListView.OnItemClickListener itemClickListener) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(thisActivity);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.alert_list, null);
        TextView title = (TextView) convertView.findViewById(R.id.text_view_alert_list_title);

        title.setText(titleStr);

        alertDialog.setView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.list_view_alert_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(thisActivity, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(itemClickListener);


        TextView view = new TextView(getApplicationContext());
        view.setText(getString(R.string.close));
        view.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        lv.addFooterView(view);

        final AlertDialog ad = alertDialog.create();
        ad.setCanceledOnTouchOutside(true);

        return ad;
    }

    /*
        populating online friends list
     */
    private void populateFriendsList(JSONObject onlineUsers) {
        try {
            if (null != onlineFriends && null != friendsDrawerAdapter) {
                Iterator<String> keys = onlineUsers.keys();

                onlineFriends.clear();
                while (keys.hasNext()) {
                    JSONObject user = onlineUsers.getJSONObject(keys.next());
                    String username = user.getString("n");

                    long timestamp;
                    if (user.isNull("timestamp"))
                        timestamp = 0;
                    else
                        timestamp = user.getLong("timestamp");

                    String thumbnailPath = ServerConstants.SERVER_FULL_URL + user.getString("a");

                    final FriendsDrawerItem item = new FriendsDrawerItem(user.getInt("id"), timestamp, username, thumbnailPath, user.getString                                      ("s"), user.getString("m"));

                    if (item.status.compareTo("offline") == 0)
                        continue;

                    onlineFriends.add(item);
                    if (unreadMessagesUsersIDs.indexOfKey(user.getInt("id")) > -1) {
                        item.unreadMessage = true;
                    }
                }
                Collections.sort(onlineFriends);
                friendsDrawerAdapter.notifyDataSetChanged();
            }
        } catch (JSONException ex) {

        }

        if (chatListView.getVisibility() == View.GONE) {
            layoutChatFailed.setVisibility(View.GONE);
            chatListView.setVisibility(View.VISIBLE);
        }
    }

    // handle thisUser's change status
    public void changeStatus(View view) {
        final CircleImageView civ = ((CircleImageView) view);

        // get the new clicked status
        statusList.updateStatus(civ.getTag().toString());

        final StatusOption so = statusList.getCurrentStatus().getStatus();

        // hide status list
        statusList.hideGradually();

        cometChat.setStatus(so, new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {
                thisUser.setOnlineStatus(so);
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
                statusList.backtrack();
                Toast.makeText(getApplicationContext(), getString(R.string.error_changing_status), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void closeDrawers() {
        if (drawerLayout.isDrawerOpen(navDrawerLayout))
            drawerLayout.closeDrawer(navDrawerLayout);

        if (drawerLayout.isDrawerOpen(chatDrawerLayout))
            drawerLayout.closeDrawer(chatDrawerLayout);
    }

    public boolean isNavigationDrawerOpen() {
        return drawerLayout.isDrawerOpen(navDrawerLayout);
    }

    public boolean isFriendsDrawerOpen() {
        return drawerLayout.isDrawerOpen(chatDrawerLayout);
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

    private void notifyFriendsDrawerMessageReceived(final int otherUserID) {
        for (FriendsDrawerItem item : onlineFriends) {
            if (item.id == otherUserID) {

                // get current shown fragment
                UserConversationsInternalFragment fragment
                        = (UserConversationsInternalFragment) getSupportFragmentManager()
                        .findFragmentByTag(UserConversationsFragment.INTERNAL_CONVERSATION_TAG);

                // in case open conversation is not with the same thisUser
                if (fragment != null && fragment.isVisible() && otherUserID == fragment.otherUserID()) {
                    // new message is with current conversation
                    item.unreadMessage = false;
                    notifyUser = false;

                    if (chatItem != null)
                        chatItem.setIcon(R.drawable.ic_action_group);
                } else {
                    item.unreadMessage = true;
                    notifyUser = true;  // notify thisUser with message and chatItem Highlight

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

    private void enableChatComponents(boolean flag) {
        // header
        searchFriendEditText.setEnabled(flag);

        // footer
        statusEditText.setEnabled(flag);
        statusList.setEnabled(flag);
        imgBtnSetStatus.setEnabled(flag);
        btnSettings.setEnabled(flag);
    }

    private void initChat() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                // get thisUser id
                final int userID = thisUser.getUserID();

                // disable chat components
                enableChatComponents(false);

                try {
                    Thread.sleep(7000);
                } catch(InterruptedException ex) {}

                // get singleton instance of comet chat and login
                cometChat = CometChat.getInstance(getApplicationContext());
                cometChat.login(ServerConstants.CHAT_URL, String.valueOf(userID), new Callbacks() {

                    final Callbacks thisCallback = this;

                    @Override
                    public void successCallback(JSONObject response) {
                        // if login successful, subscribe
                        cometChat.subscribe(true, new MySubscribeCallbacks());

                        // list of blocked users
                        getBlockedUsers();

                        // get thisUser status and status message
                        getUserInfo(userID);

                        enableChatComponents(true);
                        statusEditText.clearFocus();
                        searchFriendEditText.requestFocus();
                    }

                    @Override
                    public void failCallback(JSONObject response) {
                        txtViewFriendEmpty.setVisibility(View.VISIBLE);
                        txtViewFriendEmpty.setText(getString(R.string.could_not_connect_to_chat));
                        chatConnectionProgress.setVisibility(View.GONE);
                        layoutChatFailed.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chatConnectionProgress.setVisibility(View.VISIBLE);
                                txtViewFriendEmpty.setText(getString(R.string.connecting));
                                layoutChatFailed.setOnClickListener(null);
                                cometChat.login(ServerConstants.CHAT_URL, String.valueOf(userID), thisCallback);

                                enableChatComponents(false);
                            }
                        });

                        layoutChatFailed.setVisibility(View.VISIBLE);
                    }
                });


            }
        }).start();
    }

    private void getBlockedUsers() {
        // create blocked users list
        blockedUsersList = new ArrayList<>();

        cometChat.getBlockedUserList(new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {
                Iterator<String> iterator = jsonObject.keys();

                while (iterator.hasNext()) {
                    try {
                        JSONObject temp = jsonObject.getJSONObject(iterator.next());
                        String id = temp.getString("id");
                        String name = temp.getString("name");

                        blockedUsersList.add(new Pair<String, String>(id, name));
                    } catch (JSONException ex) {
                    }
                }
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
            }
        });
    }


    /*        C  H  A  T      S  E  C  T  I  O  N        */

    private void getUserInfo(final int userID) {
        cometChat.getUserInfo(String.valueOf(userID), new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {
                try {
                    String statusStr = jsonObject.getString("s"); // get online status
                    String message = jsonObject.getString("m");  // get status message

                    // set online status and status message
                    thisUser.setOnlineStatus(statusStr);
                    thisUser.setStatusMessage(message);

                    // update status and status message
                    String str;
                    if (thisUser.getOnlineStatus().getStatus() == StatusOption.BUSY)
                        str = "center";
                    else if (thisUser.getOnlineStatus().getStatus() == StatusOption.OFFLINE
                            || thisUser.getOnlineStatus().getStatus() == StatusOption.INVISIBLE)
                        str = "left";
                    else
                        str = "right";

                    if (statusList != null)
                        statusList.updateStatus(str);

                    if (statusEditText != null)
                        statusEditText.setText(thisUser.getStatusMessage());
                } catch (JSONException ex) {
                }
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
            }
        });
    }

    private void startUserStatsJob() {
        // create a runnable that is executed every few seconds
        userStatsRunnable = new Runnable() {

            String friendRequestsNumber, messagesNumber;
            int friendRequestsVisibility, messagesVisibility, chatIcon;
            Pair<Integer, Integer> pair;

            @Override
            public void run() {
                try {
                    ServerResponse<Pair<Integer, Integer>> sr = requestsInterface.getUserStats(thisUser);

                    if (sr.isSuccess()) {
                        pair = sr.getResult();

                        if (pair.first > 0) {
                            if (pair.first >= 99)
                                friendRequestsNumber = "99+";
                            else
                                friendRequestsNumber = String.valueOf(pair.first);
                            friendRequestsVisibility = View.VISIBLE;
                        } else {
                            friendRequestsVisibility = View.INVISIBLE;
                            friendRequestsNumber = "0";
                        }

                        if (pair.second > 0) {
                            if (pair.second >= 99)
                                messagesNumber = "99+";
                            else
                                messagesNumber = String.valueOf(pair.second);

                            if (notifyUser) {
                                messagesVisibility = View.VISIBLE;
                            }
                        } else {
                            messagesVisibility = View.GONE;
                            messagesNumber = "0";
                        }

                        // update messages text view
                        txtViewMessages.post(new Runnable() {
                            @Override
                            public void run() {
                                txtViewMessages.setText(messagesNumber);
                                txtViewMessages.setVisibility(messagesVisibility);
                            }
                        });

                        // update friends requests text view
                        txtViewFriendsRequests.post(new Runnable() {
                            @Override
                            public void run() {
                                txtViewFriendsRequests.setText(friendRequestsNumber);
                                txtViewFriendsRequests.setVisibility(friendRequestsVisibility);
                            }
                        });
                    }
                } catch (ServerResponseFailedException ex) {
                }

                // set a post delayed job to get thisUser stats later after TIME_TO_REQUEST_USER_STATISTICS
                handler.postDelayed(this, TIME_TO_REQUEST_USER_STATISTICS);
            }
        };

        // start thread to start fetching thisUser stats
        new Thread(userStatsRunnable).start();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {

        if (getUserHomeActivity().cometChat != null && CometChat.isLoggedIn())
            thisActivity.cometChat.logout(new Callbacks() {
                @Override
                public void successCallback(JSONObject jsonObject) {
                    Logger.debug(jsonObject.toString());
                }

                @Override
                public void failCallback(JSONObject jsonObject) {
                    Logger.error(jsonObject.toString());
                }
            });

        super.onDestroy();
    }

    // this class implements the click event listener for navigation drawer list
    private class NavigationDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            if (position == 0)
                return;

            //view.setSelected(true);
            selectedItem = position;
            selectItem(position);
        }
    }

    private class FriendsDrawerItemLongClickListener implements ListView.OnItemLongClickListener {

        AlertDialog ad;

        private void dismissDialog() {
            if (ad != null)
                ad.dismiss();
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> var1, View var2, final int position, long var4) {
            final FriendsDrawerItem fdi = friendsDrawerAdapter.getItem(position);
            final int userID = fdi.id;
            final String userName = fdi.name;
            final String profilePicture = fdi.profilePicture.substring(fdi.profilePicture.lastIndexOf('/') + 1);

            String names[] = new String[4];
            names[0] = getString(R.string.visit_user_profile);
            names[1] = getString(R.string.block_user);
            names[2] = getString(R.string.report_a_user);
            names[3] = getString(R.string.close);

            AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    OtherUser otherUser = new OtherUser(thisActivity.getApplicationContext(), userID);
                    otherUser.setName(userName);
                    otherUser.addPhoto(new Picture(1, userID, null, profilePicture, null, true));
                    otherUser.setProfilePicture(1);

                    switch (i) {
                        case 0:
                            // show users page
                            Intent intent = new Intent(thisActivity, MemberProfileActivity.class);

                            intent.putExtra(MemberProfileActivity.CURRENT_USER, thisUser);
                            intent.putExtra(MemberProfileActivity.OTHER_USER, otherUser);
                            intent.putExtra(MemberProfileActivity.POSITION, i);
                            startActivity(intent);

                            ad.dismiss();
                            break;
                        case 1:
                            // block thisUser
                            cometChat.blockUser(String.valueOf(userID), new Callbacks() {
                                @Override
                                public void successCallback(JSONObject jsonObject) {
                                    Toast.makeText(thisActivity, getString(R.string.user_has_been_blocked, userName), Toast.LENGTH_SHORT).show();

                                    blockedUsersList.add(new Pair<String, String>(String.valueOf(userID), fdi.name));

                                    onlineFriends.remove(position - 1);

                                    friendsDrawerAdapter.notifyDataSetChanged();

                                    ad.dismiss();
                                }

                                @Override
                                public void failCallback(JSONObject jsonObject) {

                                }
                            });
                            break;
                        case 2:
                            ad.dismiss();

                            ReportUserAlert alert = new ReportUserAlert(thisActivity, thisUser, otherUser);
                            alert.show();
                            break;
                        case 3:
                            ad.dismiss();
                    }
                }
            };

            ad = userCommandsAlert(userName, fdi.profilePicture, names, clickListener);

            ad.show();
            return true;
        }
    }

    // this class implements the click event listener for friends drawer list
    private class FriendsDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            // close drawer
            closeDrawers();

            final FriendsDrawerItem item = friendsDrawerAdapter.getItem(position);

            selectedItem = 2;
            selectItem(selectedItem);

            UserConversationsInternalFragment fragment = new UserConversationsInternalFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("ID", item.id);
            bundle.putString("Name", item.name);
            bundle.putString("Bitmap", item.profilePicture);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, UserConversationsFragment.INTERNAL_CONVERSATION_TAG)
                    .addToBackStack("s")
                    .commit();

            // mark message as read
            item.unreadMessage = false;
            view.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        requestsInterface.markMessageAsRead(thisUser, item.id);

                        // remove from unread messages
                        unreadMessagesUsersIDs.remove(item.id);

                    } catch (ServerResponseFailedException ex) {
                    }
                }
            }).start();
        }
    }

    public class NavigationDrawerAdapter extends ArrayAdapter<NavDrawerItem> {

        Context context;
        private LayoutInflater inflater;
        NavDrawerItem[] data;

        public NavigationDrawerAdapter(Context context, NavDrawerItem[] data) {
            super(context, -1, data);
            this.context = context;
            this.data = data;
            inflater = ((Activity) context).getLayoutInflater();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            DrawerItemHolder holder;

            final NavDrawerItem item = data[position];

            if (row == null) {

                holder = new DrawerItemHolder();

                if(item.isSection) {
                    row = inflater.inflate(R.layout.navigation_drawer_list_section, parent, false);
                    holder.txtTitle = (TextView) row.findViewById(R.id.text_view_drawer_list_section);

                    row.setOnClickListener(null);
                } else {
                    row = inflater.inflate(R.layout.navigation_drawer_list_item, parent, false);
                    holder.imgIcon = (ImageView) row.findViewById(R.id.image_view_drawer_list);
                    holder.txtTitle = (TextView) row.findViewById(R.id.text_view_drawer_list);
                }

                row.setOnLongClickListener(null);
                row.setLongClickable(false);
                row.setTag(holder);
            } else {
                holder = (DrawerItemHolder) row.getTag();
            }

            if(item.isSection) {
                holder.txtTitle.setText(item.title);
            } else {
                updateItemStyle(position, holder, data[position]);
            }

            return row;
        }

        private void updateItemStyle(int position, DrawerItemHolder holder, NavDrawerItem navDrawerItem) {
            holder.txtTitle.setText(navDrawerItem.title);

            if (position == selectedItem - 1) {
                holder.txtTitle.setTextColor(getResources().getColor(R.color.white));
                holder.imgIcon.setImageResource(navDrawerItem.icon_focused);
            } else {
                holder.txtTitle.setTextColor(getResources().getColorStateList(R.color.selector_list_item_text_color));

                // create states
                StateListDrawable states = new StateListDrawable();
                states.addState(new int[]{android.R.attr.state_pressed}, ContextCompat.getDrawable(context, navDrawerItem.icon_focused));
                states.addState(new int[]{android.R.attr.state_focused}, ContextCompat.getDrawable(context, navDrawerItem.icon_focused));
                states.addState(new int[]{}, ContextCompat.getDrawable(context, navDrawerItem.icon));
                holder.imgIcon.setImageDrawable(states);

            }
        }

        public void updateItemStyle(ListView listView, int position) {
            NavDrawerItem navDrawerItem = data[position - 1];
            NavDrawerItem prevNavDrawerItem = data[selectedItem - 1];
            View row = listView.getChildAt(position);
            View prevRow = listView.getChildAt(selectedItem);

            DrawerItemHolder holder, prevHolder;
            holder = (DrawerItemHolder) row.getTag();
            prevHolder = (DrawerItemHolder) prevRow.getTag();

            holder.txtTitle.setTextColor(getResources().getColor(R.color.white));
            holder.imgIcon.setImageResource(navDrawerItem.icon_focused);

            if (prevHolder != null) {
                prevHolder.txtTitle.setTextColor(getResources().getColorStateList(R.color.selector_list_item_text_color));
                StateListDrawable states = new StateListDrawable();
                states.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(prevNavDrawerItem.icon_focused));
                states.addState(new int[]{android.R.attr.state_focused}, getResources().getDrawable(prevNavDrawerItem.icon_focused));
                states.addState(new int[]{}, getResources().getDrawable(prevNavDrawerItem.icon));
                prevHolder.imgIcon.setImageDrawable(states);
                selectedItem = position;
            }
        }

        class DrawerItemHolder {
            ImageView imgIcon;
            TextView txtTitle;
        }


    }

    public class FriendsDrawerAdapter extends ArrayAdapter<FriendsDrawerItem>
            implements Filterable {

        Context context;
        int layout;
        List<FriendsDrawerItem> data;
        FriendsFilter friendsFilter;

        public FriendsDrawerAdapter(Context context, int layout, List<FriendsDrawerItem> data) {
            super(context, layout, data);
            this.context = context;
            this.layout = layout;
            this.data = data;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            DrawerItemHolder holder;

            if (row == null) {

                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layout, parent, false);

                holder = new DrawerItemHolder();
                holder.profilePicture = (CircleImageView) row.findViewById(R.id.left_drawer_profile_picture);
                holder.txtName = (TextView) row.findViewById(R.id.text_view_drawer_left_list_name);
                holder.txtStatusMessage = (TextView) row.findViewById(R.id.text_view_drawer_left_list_status_message);
                holder.friendStatus = (CircleImageView) row.findViewById(R.id.left_drawer_status);

                row.setTag(holder);
            } else {
                holder = (DrawerItemHolder) row.getTag();
            }


            FriendsDrawerItem friendsDrawerItem = data.get(position);
            holder.txtName.setText(friendsDrawerItem.name);
            holder.txtStatusMessage.setText(friendsDrawerItem.statusMessage);

            // set background color to highlighted
            if (friendsDrawerItem.unreadMessage)
                row.setBackgroundColor(getResources().getColor(R.color.highlighted_yellow));
            else
                row.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            Glide.with(context)
                    .load(friendsDrawerItem.profilePicture)
                    .asBitmap()
                    .fitCenter()
                    .centerCrop()
                    .placeholder(R.drawable.default_user_photo)
                    .into(holder.profilePicture);

            User.OnlineStatus status = new User.OnlineStatus(friendsDrawerItem.status);
            holder.friendStatus.setImageResource(status.getResourceID());

            return row;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Filter getFilter() {
            if (friendsFilter == null)
                friendsFilter = new FriendsFilter();

            return friendsFilter;
        }

        @Override
        public void notifyDataSetChanged() {
            if (onlineFriends.size() == 0) {
                txtViewFriendEmpty.setVisibility(View.VISIBLE);
                searchFriendEditText.setVisibility(View.GONE);
            } else {
                txtViewFriendEmpty.setVisibility(View.GONE);
                searchFriendEditText.setVisibility(View.VISIBLE);
            }

            super.notifyDataSetChanged();
        }

        class DrawerItemHolder {
            CircleImageView profilePicture, friendStatus;
            TextView txtName, txtStatusMessage;
        }

        private class FriendsFilter extends Filter {

            public FriendsFilter() {
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                data = (List<FriendsDrawerItem>) results.values;
                notifyDataSetChanged();

            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    results.values = onlineFriends;
                    results.count = onlineFriends.size();
                } else {
                    List<FriendsDrawerItem> mList = new ArrayList<>();

                    for (FriendsDrawerItem item : data) {
                        if (item.name.toUpperCase().contains(constraint.toString().toUpperCase()))
                            mList.add(item);
                    }

                    results.values = mList;
                    results.count = mList.size();
                }

                return results;
            }
        }
    }

    public class MySubscribeCallbacks implements SubscribeCallbacks {

        @Override
        public void onMessageReceived(JSONObject receivedMessage) {
            try {
                Logger.debug("Message12" + receivedMessage.toString());
                Intent intent = new Intent();
                intent.setAction("NEW_SINGLE_MESSAGE");

					/* Send a broadcast to SingleChatActivity */
                int id;
                String msg;
                long dateTime;
                int otherUserID;

                id = receivedMessage.getInt("id");
                otherUserID = receivedMessage.getInt("from");
                // TODO handle icons
                msg = receivedMessage.getString("message");
                dateTime = receivedMessage.getLong("sent");

                EndMessage message = new EndMessage(id, msg, dateTime, Message.REGULAR);
                intent.putExtra("user_id", otherUserID);
                intent.putExtra("message", message);
                sendBroadcast(intent);

                // notify friends drawer
                notifyFriendsDrawerMessageReceived(otherUserID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(JSONObject errorResponse) {
            Logger.error("Some error: " + errorResponse);
        }

        @Override
        public void gotProfileInfo(JSONObject profileInfo) {
            Logger.debug("Profile info: " + profileInfo);
            JSONObject j = profileInfo;
        }

        @Override
        public void gotOnlineList(JSONObject onlineUsers) {
            try {
                Logger.debug(onlineUsers.toString());
                populateFriendsList(onlineUsers);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAVChatMessageReceived(JSONObject response) {
            Logger.debug("AVChat Received Message: " + response.toString());
            try {
                Intent intent = new Intent();
                intent.setAction("NEW_SINGLE_MESSAGE");

					/* Send a broadcast to SingleChatActivity */
                int id;
                String msg;
                long dateTime;
                int messageType, otherUserID;

                id = response.getInt("id");
                otherUserID = response.getInt("from");
                msg = response.getString("message");
                dateTime = response.getLong("sent");
                messageType = response.getInt("message_type");


                // message with response about video chat
                if (messageType == Message.CALL_REJECTED)
                    msg = getString(R.string.end_user_rejected_video_call);
                else if (messageType == Message.INCOMING_CALL)
                    msg = getString(R.string.end_user_requested_video_call);
                else if (messageType == Message.CALL_ACCEPTED)
                    msg = getString(R.string.end_user_accepted_video_call);
                else if (messageType == Message.OUTGOING_BUSY_TONE)
                    msg = getString(R.string.video_call_other_user_tried_calling_you);
                else if (messageType == Message.CANCEL_CALL)
                    msg = getString(R.string.end_user_canceled_video_call);
                else if (messageType == Message.NO_ANSWER)
                    msg = getString(R.string.video_call_no_answer);
                else if (messageType == Message.INCOMING_BUSY_TONE)
                    msg = getString(R.string.video_call_other_user_busy);

                if (messageType == Message.END_CALL && id != SessionData.getInstance().getId()) {
                    if (SessionData.getInstance().isAvchatCallRunning() && AVChatActivity.thisActivity != null) {
                        AVChatActivity.thisActivity.endCall();
                        Toast.makeText(thisActivity, getString(R.string.end_user_ended_call), Toast.LENGTH_SHORT).show();
                    }

                    return;
                }

                EndMessage message = new EndMessage(id, msg, dateTime, messageType);

                intent.putExtra("user_id", otherUserID);
                intent.putExtra("message", message);

                sendBroadcast(intent);

                // notify friends drawer
                notifyFriendsDrawerMessageReceived(otherUserID);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void gotAnnouncement(JSONObject announcement) {
        }
    }


}
