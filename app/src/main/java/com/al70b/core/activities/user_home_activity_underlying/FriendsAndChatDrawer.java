package com.al70b.core.activities.user_home_activity_underlying;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.adapters.FriendsAndChatDrawerAdapter;
import com.al70b.core.extended_widgets.StatusList;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.FriendsDrawerItem;
import com.al70b.core.objects.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Naseem on 6/20/2016.
 */
public class FriendsAndChatDrawer implements FriendsAndChatDrawerController {

    private static String TAG = "FriendsAndChatDrawer";
    private static final long TIME_RETRY_CHAT_LOGIN = 2 * 60 * 1000; // 2 minutes, in milliseconds

    // context is the activity this drawer in
    private UserHomeActivity activity;

    // the drawer parent in activity
    private ViewGroup root;

    private CurrentUser currentUser;

    public FriendsAndChatDrawer(UserHomeActivity activity, ViewGroup root, CurrentUser currentUser) {
        this.activity = activity;
        this.root = root;
        this.currentUser = currentUser;

        init();
    }

    private ChatHandler chatHandler;

    private ViewGroup friendsAndChatDrawerLayout;
    private FriendsAndChatDrawerAdapter friendsAndChatDrawerAdapter;

    private Timer retryChatLoginTimer;

    // declare widgets
    private EditText searchFriendEditText, statusEditText;
    private StatusList statusList;
    private LinearLayout layoutChatFailed;
    private LinearLayout layoutChatConnected;
    private TextView txtViewChatServiceResponse;
    private ProgressBar chatConnectionProgress;
    private ImageButton imgBtnSetStatusMessage, imgBtnSettings;

    private void init() {

        // relate widgets to xml
        friendsAndChatDrawerLayout = (LinearLayout) root.findViewById(R.id.layout_chat_drawer);

        // Connected Chat Layout
        layoutChatConnected = (LinearLayout) friendsAndChatDrawerLayout.findViewById(R.id.layout_friends_in_chat);
        ListView chatListView = (ListView) layoutChatConnected.findViewById(R.id.list_view_friends_in_chat);
        searchFriendEditText = (EditText) layoutChatConnected.findViewById(R.id.et_friends_drawer_search);

        // Disconnected Chat Layout
        layoutChatFailed = (LinearLayout) friendsAndChatDrawerLayout.findViewById(R.id.layout_friends_drawer_failed_connecting);
        txtViewChatServiceResponse = (TextView) friendsAndChatDrawerLayout.findViewById(R.id.tv_friends_drawer_message);
        chatConnectionProgress = (ProgressBar) friendsAndChatDrawerLayout.findViewById(R.id.progress_bar_friends_drawer_connecting);

        //  header
        ViewGroup drawerHeader = (ViewGroup) friendsAndChatDrawerLayout.findViewById(R.id.layout_chat_drawer_header);
        statusList = (StatusList) drawerHeader.findViewById(R.id.drawer_header_status_list);
        imgBtnSettings = (ImageButton) drawerHeader.findViewById(R.id.img_btn_friends_drawer_header_settings);
        imgBtnSetStatusMessage = (ImageButton) drawerHeader.findViewById(R.id.img_btn_friends_drawer_header_set);
        statusEditText = (EditText) drawerHeader.findViewById(R.id.et_friends_drawer_header_status);


        // create new list for online friends, and adapter
        final List<FriendsDrawerItem> onlineFriendsList = new ArrayList<>();
        friendsAndChatDrawerAdapter = new FriendsAndChatDrawerAdapter(activity,
                R.layout.list_item_chat_contacts, onlineFriendsList);

        // set the adapter for the friends drawer list view
        chatListView.setAdapter(friendsAndChatDrawerAdapter);

        // initialize Comet chat and chat-events handler
        chatHandler = new ChatHandler(activity.getApplicationContext(), currentUser,
                onlineFriendsList, new MyChatHandlerEvents());

        // update online status and status message
        statusEditText.setText(getString(R.string.not_connected_status));
        statusList.updateStatus(currentUser.getOnlineStatus().getStatus());
        statusList.setOnStatusChangeEvent(new StatusList.OnStatusChangeEvent() {
            @Override
            public void onStatusChange(User.OnlineStatus onlineStatus) {
                chatHandler.setStatus(onlineStatus.getStatus());
            }
        });

        statusEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    imgBtnSetStatusMessage.setVisibility(View.VISIBLE);
                } else {
                    statusEditText.setText(currentUser.getStatusMessage());
                    imgBtnSetStatusMessage.setVisibility(View.INVISIBLE);
                }
            }
        });

        imgBtnSetStatusMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = statusEditText.getText().toString();
                chatHandler.setStatusMessage(message);
                statusEditText.clearFocus();
            }
        });

        imgBtnSettings.setOnClickListener(new View.OnClickListener() {

            //private AlertDialog ad;

            @Override
            public void onClick(View view) {
                Toast.makeText(activity.getApplicationContext(),
                        "You clicked chat settings",
                        Toast.LENGTH_SHORT).show();
                /*
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
                    Toast.makeText(context, context.getString(R.string.no_blocked_users), Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        imgBtnSettings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(activity, activity.getString(R.string.blocked_users_list),
                        Toast.LENGTH_SHORT).show();
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
                    friendsAndChatDrawerAdapter.setData(onlineFriendsList);
                friendsAndChatDrawerAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // and set the listener for item click event
        //chatListView.setOnItemClickListener(new FriendsDrawerItemClickListener());
        //chatListView.setOnItemLongClickListener(new FriendsDrawerItemLongClickListener());

        friendsAndChatDrawerLayout.requestFocus();

        retryChatLoginTimer = new Timer();
        retryChatLoginTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG + ": Chat Login Timer", "Timer running..");
                if(!chatHandler.isLoggedIn()) {
                    chatHandler.login();
                }
            }
        }, 0, TIME_RETRY_CHAT_LOGIN);
    }

    private String getString(int resource) {
        return activity.getResources().getString(resource);
    }

    private class MyChatHandlerEvents extends ChatHandler.ChatHandlerEvents {

        private void enableChatComponents(boolean flag) {
            // header
            searchFriendEditText.setEnabled(flag);

            // footer
            statusEditText.clearFocus();
            statusEditText.setEnabled(flag);
            statusList.setEnabled(flag);
            imgBtnSetStatusMessage.setEnabled(flag);
            imgBtnSettings.setEnabled(flag);
            imgBtnSetStatusMessage.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onChatConnectionSetup() {
            enableChatComponents(false);
        }

        @Override
        public void onChatConnectionFailed() {
            // show message of failed connection, hide irrelevant views
            txtViewChatServiceResponse.setVisibility(View.VISIBLE);
            txtViewChatServiceResponse.setText(
                    getString(R.string.could_not_connect_to_chat_press_to_try_again));

            statusEditText.setText(activity.getString(R.string.not_connected_status));
            currentUser.setStatusMessage(getString(R.string.not_connected_status));

            chatConnectionProgress.setVisibility(View.GONE);
            layoutChatConnected.setVisibility(View.GONE);

            // enable onclick for retry
            layoutChatFailed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatConnectionProgress.setVisibility(View.VISIBLE);
                    layoutChatConnected.setVisibility(View.GONE);
                    txtViewChatServiceResponse.setText(
                            getString(R.string.connecting));
                    layoutChatFailed.setOnClickListener(null);

                    chatHandler.login();
                }
            });

            layoutChatFailed.setVisibility(View.VISIBLE);
        }

        @Override
        public void onChatConnectionSucceeded() {
            layoutChatFailed.setOnClickListener(null);
            layoutChatFailed.setVisibility(View.GONE);
            layoutChatConnected.setVisibility(View.VISIBLE);
            enableChatComponents(true);

            statusEditText.clearFocus();
            searchFriendEditText.requestFocus();
        }

        @Override
        void onProfileInfoReceived(String status, String statusMessage) {
            statusList.updateStatus(status);

            if (statusEditText != null)
                statusEditText.setText(statusMessage);
        }

        @Override
        public void onFriendsOnlineListUpdated() {
            if (friendsAndChatDrawerAdapter != null) {
                friendsAndChatDrawerAdapter.notifyDataSetChanged();
            }
        }

        @Override
        void onSetStatusMessageResponse(boolean statusChanged, String msg) {
            Log.d(TAG, msg);
            if(statusChanged) {
                Toast.makeText(activity, getString(R.string.status_message_changed_successfully),
                        Toast.LENGTH_SHORT).show();
            } else {
                // failed to change status, restore previous
                statusEditText.setText(currentUser.getStatusMessage()); // TODO: verify message is restored
                Toast.makeText(activity, getString(R.string.status_message_change_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public ViewGroup getDrawerLayout() {
        return friendsAndChatDrawerLayout;
    }

    @Override
    public boolean activityStart() {
        return false;
    }

    @Override
    public boolean activityPause() {
        return false;
    }

    @Override
    public boolean activityStop() {
        return false;
    }

    @Override
    public boolean activityDestroy() {
        if(retryChatLoginTimer != null) {
            retryChatLoginTimer.cancel();
        }

        return true;
    }

    /*private class FriendsDrawerItemLongClickListener implements ListView.OnItemLongClickListener {

        AlertDialog ad;

        private void dismissDialog() {
            if (ad != null)
                ad.dismiss();
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> var1, View var2, final int position, long var4) {
            final FriendsDrawerItem fdi = friendsAndChatDrawerAdapter.getItem(position);
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

                            ReportUserDialog alert = new ReportUserDialog(thisActivity, thisUser, otherUser);
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
    }*/

    // this class implements the click event listener for friends drawer list
    /*private class FriendsDrawerItemClickListener implements ListView.OnItemClickListener {
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
    }*/



/*
    private AlertDialog buildAlertDialogWithList(String titleStr, String[] list, ListView.OnItemClickListener itemClickListener) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(thisActivity);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.dialog_list, null);
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
    }*/
}
