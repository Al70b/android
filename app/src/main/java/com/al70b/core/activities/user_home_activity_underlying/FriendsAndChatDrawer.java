package com.al70b.core.activities.user_home_activity_underlying;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.FriendConversationActivity;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.adapters.FriendsAndChatDrawerAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.StatusList;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.EndMessage;
import com.al70b.core.objects.FriendsDrawerItem;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.User;
import com.al70b.core.server_methods.RequestsInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 6/20/2016.
 */
public class FriendsAndChatDrawer implements FriendsAndChatDrawerController {

    private static String TAG = "FriendsAndChatDrawer";

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

    // declare widgets
    private TextView statusTextView;
    private EditText searchFriendEditText, statusEditText;
    private StatusList statusList;
    private LinearLayout layoutChatFailed;
    private LinearLayout layoutChatConnected;
    private TextView txtViewChatServiceResponse;
    private ProgressBar chatConnectionProgress;
    private ImageButton imgBtnSetStatusMessage, imgBtnSettings;

    private boolean loggingout = false;

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
        statusTextView = (TextView) drawerHeader.findViewById(R.id.tv_friends_drawer_header_status);
        statusEditText = (EditText) drawerHeader.findViewById(R.id.et_friends_drawer_header_status);


        // create new list for online friends, and adapter
        final List<FriendsDrawerItem> onlineFriendsList = new ArrayList<>();
        friendsAndChatDrawerAdapter = new FriendsAndChatDrawerAdapter(activity,
                R.layout.list_item_chat_contacts, onlineFriendsList);
        searchFriendEditText.setVisibility(View.INVISIBLE);

        // set the adapter for the friends drawer list view
        chatListView.setAdapter(friendsAndChatDrawerAdapter);
        chatListView.setEmptyView(layoutChatConnected.findViewById(
                R.id.tv_friends_and_chat_no_friends_online));

        // initialize Comet chat and chat-events handler
        chatHandler = new ChatHandler(activity.getApplicationContext(), currentUser,
                onlineFriendsList, new MyChatHandlerEvents());

        // update online status and status message
        statusList.updateStatus(currentUser.getOnlineStatus().getStatus());
        statusList.setOnStatusChangeEvent(new StatusList.OnStatusChangeEvent() {
            @Override
            public void onStatusChange(User.OnlineStatus onlineStatus) {
                chatHandler.setStatus(onlineStatus.getStatus());
            }
        });

        statusTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                statusTextView.setVisibility(View.GONE);
                statusEditText.setText(statusTextView.getText().toString());
                statusEditText.setVisibility(View.VISIBLE);
                statusEditText.requestFocus();
                imgBtnSetStatusMessage.setVisibility(View.VISIBLE);

                return true;
            }
        });

        statusEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    statusTextView.setText(currentUser.getStatusMessage());
                    statusTextView.setVisibility(View.VISIBLE);

                    // make editable views disappear
                    statusEditText.setVisibility(View.GONE);
                    imgBtnSetStatusMessage.setVisibility(View.GONE);
                }
            }
        });

        imgBtnSetStatusMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = statusEditText.getText().toString();
                chatHandler.setStatusMessage(message);

                statusTextView.setText(currentUser.getStatusMessage());
                statusTextView.setVisibility(View.VISIBLE);

                // make editable views disappear
                statusEditText.setVisibility(View.GONE);
                imgBtnSetStatusMessage.setVisibility(View.GONE);
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
        chatListView.setOnItemClickListener(new FriendsDrawerItemClickListener());
        //chatListView.setOnItemLongClickListener(new FriendsDrawerItemLongClickListener());

        friendsAndChatDrawerLayout.requestFocus();
    }

    private String getString(int resource) {
        return activity.getResources().getString(resource);
    }

    private class MyChatHandlerEvents extends ChatHandler.ChatHandlerEvents {

        private void enableChatComponents(boolean flag) {
            // header
            searchFriendEditText.setEnabled(flag);

            statusTextView.setEnabled(flag);
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

            statusTextView.setText(getString(R.string.not_connected_status));
            chatConnectionProgress.setVisibility(View.VISIBLE);
            layoutChatConnected.setVisibility(View.GONE);
            txtViewChatServiceResponse.setText(
                    getString(R.string.connecting));
            layoutChatFailed.setOnClickListener(null);
        }

        @Override
        public void onChatConnectionFailed() {
            // show message of failed connection, hide irrelevant views
            txtViewChatServiceResponse.setVisibility(View.VISIBLE);
            txtViewChatServiceResponse.setText(
                    getString(R.string.could_not_connect_to_chat_press_to_try_again));

            statusTextView.setText(getString(R.string.not_connected_status));
            statusEditText.setText(activity.getString(R.string.not_connected_status));
            currentUser.setStatusMessage(getString(R.string.not_connected_status));

            chatConnectionProgress.setVisibility(View.GONE);
            layoutChatConnected.setVisibility(View.GONE);

            // enable onclick for retry
            layoutChatFailed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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

            statusTextView.setText(currentUser.getStatusMessage());
            statusEditText.clearFocus();
            searchFriendEditText.requestFocus();
        }

        @Override
        void onProfileInfoReceived(String status, String statusMessage) {
            statusList.updateStatus(status);

            if (statusTextView != null) {
                statusTextView.setText(statusMessage);
            }
        }

        @Override
        public void onFriendsOnlineListUpdated() {
            if (friendsAndChatDrawerAdapter != null) {
                friendsAndChatDrawerAdapter.notifyDataSetChanged();

                if (friendsAndChatDrawerAdapter.isEmpty()) {
                    if (searchFriendEditText.getText().toString().trim().isEmpty()) {
                        // not doing search, so indeed no friends are online
                        searchFriendEditText.setEnabled(false);
                        searchFriendEditText.setVisibility(View.INVISIBLE);
                    } else {
                        // searching and no friends were found, keep it
                        searchFriendEditText.setEnabled(true);
                        searchFriendEditText.setVisibility(View.VISIBLE);
                    }
                } else {
                    searchFriendEditText.setEnabled(true);
                    searchFriendEditText.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        void onSetStatusMessageResponse(boolean statusChanged, String msg, String result) {
            Log.d(TAG, msg);
            if (statusChanged) {
                currentUser.setStatusMessage(msg);
            } else {
                // failed to change status, restore previous
                statusEditText.setText(currentUser.getStatusMessage());
                Toast.makeText(activity, getString(R.string.status_message_change_failed),
                        Toast.LENGTH_SHORT).show();
            }
            statusEditText.clearFocus();
        }

        @Override
        void onAVChatMessageReceived(int userId, EndMessage msg) {
        }

        @Override
        void onMessageReceived(int userId, EndMessage msg) {
            FriendsDrawerItem item = friendsAndChatDrawerAdapter.getItemByUserID(userId);

            if (item != null) {
                item.isMessageUnread = true;
                friendsAndChatDrawerAdapter.notifyDataSetChanged();
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
        return false;
    }

    @Override
    public void logout() {
        this.loggingout = true;

        if (chatHandler != null) {
            chatHandler.logout();
        }
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
    private class FriendsDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final FriendsDrawerItem item = (FriendsDrawerItem) parent.getItemAtPosition(position);

            // mark message as read
            item.isMessageUnread = false;
            view.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(),
                    android.R.color.transparent));

            // remove from unread messages
            //unreadMessagesUsersIDs.remove(item.id);

            try {
                Bundle bundle = new Bundle();

                OtherUser otherUser = new OtherUser(activity.getApplicationContext(), item.id);
                new RequestsInterface(activity.getApplicationContext())
                        .getOtherUserData(currentUser.getUserID(),
                                currentUser.getAccessToken(),
                                otherUser);
                bundle.putSerializable(FriendConversationActivity.CURRENT_USER, currentUser);
                bundle.putSerializable(FriendConversationActivity.OTHER_USER, otherUser);

                Intent intent = new Intent(activity, FriendConversationActivity.class);
                intent.putExtras(bundle);
                activity.closeDrawers();
                activity.startActivity(intent);
            } catch(ServerResponseFailedException ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }



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
