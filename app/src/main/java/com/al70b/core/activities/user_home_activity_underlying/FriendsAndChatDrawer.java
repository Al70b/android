package com.al70b.core.activities.user_home_activity_underlying;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.Dialogs.BlockUserDialog;
import com.al70b.core.activities.Dialogs.BlockedUsersListDialog;
import com.al70b.core.activities.Dialogs.QuestionAlert2;
import com.al70b.core.activities.Dialogs.ReportUserDialog;
import com.al70b.core.activities.FriendConversationActivity;
import com.al70b.core.activities.MemberProfileActivity;
import com.al70b.core.activities.UserHomeActivity;
import com.al70b.core.adapters.FriendsAndChatDrawerAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.StatusList;
import com.al70b.core.misc.Utils;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.EndMessage;
import com.al70b.core.objects.FriendsDrawerItem;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.objects.User;
import com.al70b.core.server_methods.RequestsInterface;
import com.inscripts.jsonphp.Block;

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

    private BlockedUsersListDialog blockedUsersListDialog;

    private void init() {

        // relate widgets to xml
        friendsAndChatDrawerLayout = (LinearLayout) root.findViewById(R.id.layout_chat_drawer);

        // Connected Chat Layout
        layoutChatConnected = (LinearLayout) friendsAndChatDrawerLayout.findViewById(R.id.layout_friends_in_chat);
        final ListView chatListView = (ListView) layoutChatConnected.findViewById(R.id.list_view_friends_in_chat);
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

        String[] overflowSettingsList = activity.getResources().getStringArray(R.array.chat_settings_options);
        final ListPopupWindow popupWindow = new ListPopupWindow(activity);

        popupWindow.setAdapter(new ArrayAdapter<String>(activity,
                R.layout.list_item_settings,
                overflowSettingsList));
        popupWindow.setAnchorView(imgBtnSettings);
        popupWindow.setModal(true);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0: // blocked user manager
                        if (chatHandler.getBlockedUsersList().isEmpty()) {
                            Toast.makeText(activity, getString(R.string.no_blocked_users), Toast.LENGTH_SHORT).show();
                        } else {
                            blockedUsersListDialog = new BlockedUsersListDialog(activity,
                                    currentUser,
                                    chatHandler);
                            blockedUsersListDialog.show();
                        }
                        break;
                }

                popupWindow.dismiss();
            }
        });

        imgBtnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.show();
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
        chatListView.setOnItemLongClickListener(new FriendsDrawerItemLongClickListener());

        friendsAndChatDrawerLayout.requestFocus();
    }

    private String getString(int resource) {
        return activity.getResources().getString(resource);
    }

    private String getString(int resource, String... strings) {
        return activity.getResources().getString(resource, strings);
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

        @Override
        void onBlockUserResponse(boolean isBlocked, OtherUser otherUser) {
            Toast.makeText(activity, getString(R.string.user_has_been_blocked, otherUser.getName()),
                    Toast.LENGTH_SHORT).show();

            if (blockedUsersListDialog != null && blockedUsersListDialog.isShowing()) {
                blockedUsersListDialog.notifyAdapter();
            }

            friendsAndChatDrawerAdapter.remove(friendsAndChatDrawerAdapter.getItemByUserID((int) otherUser.getUserID()));
            friendsAndChatDrawerAdapter.notifyDataSetChanged();
        }

        @Override
        void onUnBlockUserResponse(boolean isUnBlocked, OtherUser otherUser) {
            Toast.makeText(activity, getString(R.string.unblock_user, otherUser.getName()),
                    Toast.LENGTH_SHORT).show();

            if (blockedUsersListDialog != null && blockedUsersListDialog.isShowing()) {
                if (chatHandler.getBlockedUsersList().isEmpty()) {
                    blockedUsersListDialog.dismiss();
                } else {
                    blockedUsersListDialog.notifyAdapter();
                }
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
            } catch (ServerResponseFailedException ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }

    private class FriendsDrawerItemLongClickListener implements ListView.OnItemLongClickListener {

        private final String[] chatOptionsList;

        public FriendsDrawerItemLongClickListener() {
            chatOptionsList = activity.getResources().getStringArray(R.array.friend_item_commands);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final FriendsDrawerItem fdi = friendsAndChatDrawerAdapter.getItem(position);
            final int userID = fdi.id;

            final ListPopupWindow chatPopupWindow = new ListPopupWindow(activity);
            chatPopupWindow.setAdapter(new ArrayAdapter<String>(activity,
                    R.layout.list_item_settings,
                    chatOptionsList));
            chatPopupWindow.setAnchorView(view);
            chatPopupWindow.setModal(true);
            chatPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    OtherUser otherUser = new OtherUser(activity, userID);
                    try {
                        ServerResponse<OtherUser> sr = new RequestsInterface(activity)
                                .getOtherUserData(currentUser.getUserID(), currentUser.getAccessToken(),
                                        otherUser);

                        if (sr.isSuccess()) {
                            Log.d(TAG, "Successfully fetched user: " + sr.getResult().getName());
                        } else {
                            Log.e(TAG, sr.getErrorMsg());
                            Toast.makeText(activity, sr.getErrorMsg(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (ServerResponseFailedException ex) {
                        Log.e(TAG, ex.toString());
                        Toast.makeText(activity, ex.toString(), Toast.LENGTH_SHORT).show();
                    }

                    switch (position) {
                        case 0:
                            Intent intent = new Intent(activity, MemberProfileActivity.class);
                            intent.putExtra(MemberProfileActivity.CURRENT_USER, currentUser);
                            intent.putExtra(MemberProfileActivity.OTHER_USER, otherUser);
                            intent.putExtra(MemberProfileActivity.POSITION, position);
                            activity.startActivity(intent);
                            break;
                        case 1:
                            // block user
                            BlockUserDialog blockUserDialog = new BlockUserDialog(activity, currentUser, otherUser,
                                    chatHandler);
                            blockUserDialog.show();
                            break;
                        case 2:
                            // report user
                            ReportUserDialog reportUserDialog = new ReportUserDialog(activity, currentUser, otherUser);
                            reportUserDialog.show();
                            break;
                    }

                    chatPopupWindow.dismiss();
                }
            });
            chatPopupWindow.show();

            return true;
        }
    }
}
