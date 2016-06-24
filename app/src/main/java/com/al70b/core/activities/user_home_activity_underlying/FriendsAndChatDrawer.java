package com.al70b.core.activities.user_home_activity_underlying;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.al70b.core.activities.MemberProfileActivity;
import com.al70b.core.adapters.FriendsAndChatDrawerAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.StatusList;
import com.al70b.core.fragments.Dialogs.ReportUserAlert;
import com.al70b.core.fragments.UserConversationsFragment;
import com.al70b.core.fragments.UserConversationsInternalFragment;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.FriendsDrawerItem;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.Picture;
import com.inscripts.callbacks.Callbacks;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.keys.StatusOption;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 6/20/2016.
 */
public class FriendsAndChatDrawer {

    private static String TAG = "FriendsAndChatDrawer";

    // context is the activity this drawer in
    private Context context;

    // the drawer parent in activity
    private ViewGroup root;
    private ViewGroup friendsAndChatDrawerLayout;

    private CurrentUser currentUser;

    private ChatHandler chatHandler;

    private FriendsAndChatDrawerAdapter friendsAndChatDrawerAdapter;

    public FriendsAndChatDrawer(Context context, ViewGroup root, CurrentUser currentUser) {
        this.context = context;
        this.root = root;
        this.currentUser = currentUser;

        init();
    }

    public ViewGroup getDrawerLayout() {
        return friendsAndChatDrawerLayout;
    }

    private void init() {

        friendsAndChatDrawerLayout = (LinearLayout) root.findViewById(R.id.layout_chat_drawer);

        // chat layout
        ListView chatListView = (ListView) friendsAndChatDrawerLayout.findViewById(R.id.list_view_friends_in_chat);
        EditText searchFriendEditText = (EditText) friendsAndChatDrawerLayout.findViewById(R.id.et_friends_drawer_search);

        // chat connection failed layout
        LinearLayout layoutChatFailed = (LinearLayout) friendsAndChatDrawerLayout.findViewById(R.id.layout_friends_drawer_failed_connecting);
        TextView txtViewFriendEmpty = (TextView) friendsAndChatDrawerLayout.findViewById(R.id.tv_friends_drawer_message);
        ProgressBar chatConnectionProgress = (ProgressBar) friendsAndChatDrawerLayout.findViewById(R.id.progress_bar_friends_drawer_connecting);


        //  header
        ViewGroup drawerHeader = (ViewGroup) friendsAndChatDrawerLayout.findViewById(R.id.layout_chat_drawer_header);
        StatusList statusList = (StatusList) drawerHeader.findViewById(R.id.drawer_header_status_list);
        EditText statusEditText = (EditText) drawerHeader.findViewById(R.id.et_friends_drawer_header_status);

        //  footer
        ViewGroup drawerFooter = (ViewGroup) friendsAndChatDrawerLayout.findViewById(R.id.layout_chat_drawer_footer);
        //ImageButton imgBtnSetStatus = (ImageButton) drawerFooter.findViewById(R.id.img_btn_friends_drawer_footer_set);
        ImageButton btnSettings = (ImageButton) drawerFooter.findViewById(R.id.img_btn_friends_drawer_footer_settings);


        statusEditText.setText(context.getString(R.string.not_connected_status));
        //imgBtnSetStatus.setVisibility(View.INVISIBLE);

        // initialize Comet chat
        chatHandler = new ChatHandler();
        //chatHandler.initChat();

        // update status and status message
        String str;
        if (currentUser.getOnlineStatus().getStatus() == StatusOption.BUSY)
            str = "center";
        else if (currentUser.getOnlineStatus().getStatus() == StatusOption.OFFLINE
                || currentUser.getOnlineStatus().getStatus() == StatusOption.INVISIBLE)
            str = "left";
        else
            str = "right";

        statusList.updateStatus(str);

        statusEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    //imgBtnSetStatus.setVisibility(View.VISIBLE);
                    //backupStatusMessage = statusEditText.getText().toString();
                } else {
                    /*if (!statusBtnClicked) {
                        statusEditText.setText(backupStatusMessage);
                        statusBtnClicked = false;
                    }
                    imgBtnSetStatus.setVisibility(View.INVISIBLE);*/
                }
            }
        });

        /*imgBtnSetStatus.setOnClickListener(new View.OnClickListener() {
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
        });*/

        /*btnSettings.setOnClickListener(new View.OnClickListener() {

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
                    Toast.makeText(context, context.getString(R.string.no_blocked_users), Toast.LENGTH_SHORT).show();
                }
            }
        });*/



        // create new list for online friends
        final List<FriendsDrawerItem> onlineFriends = new ArrayList<>();

        friendsAndChatDrawerAdapter = new FriendsAndChatDrawerAdapter(context, R.layout.list_item_chat_contacts, onlineFriends);

        // set the adapter for the friends drawer list view
        chatListView.setAdapter(friendsAndChatDrawerAdapter);

        btnSettings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(context, context.getString(R.string.blocked_users_list), Toast.LENGTH_SHORT).show();
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
                    friendsAndChatDrawerAdapter.setData(onlineFriends);
                friendsAndChatDrawerAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //status = statusList.getVisibleStatusView();
        /*status.setOnClickListener(new View.OnClickListener() {
            // handle the status click, show other status for currentUser to pick from
            @Override
            public void onClick(View view) {
                if (statusList.isListExpanded()) {
                    // hide status list
                    statusList.hideGradually();

                    // remove all post delayed for the count starts from zero again
                    handler.removeCallbacksAndMessages(null);
                } else if (statusList.isEnabled()) {
                    // show status list
                    statusList.showGradually();

                    // create a delayed job for closing the status list if it
                    // is still open after 5 seconds
                    handler.postDelayed(new Runnable() {

                        public void run() {
                            // check if after 5 seconds the currentUser didn't choose a status and close
                            // status list if he didn't
                            if (statusList.isListExpanded())
                                statusList.hideGradually();
                        }
                    }, 5 * 1000);
                }
            }
        });*/


        // and set the listener for item click event
        //chatListView.setOnItemClickListener(new FriendsDrawerItemClickListener());
        //chatListView.setOnItemLongClickListener(new FriendsDrawerItemLongClickListener());
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
}
