package com.al70b.core.activities.user_home_activity_underlying;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.AVCall.AVChatActivity;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.objects.EndMessage;
import com.al70b.core.objects.Message;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.ServerConstants;
import com.inscripts.callbacks.Callbacks;
import com.inscripts.callbacks.SubscribeCallbacks;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.keys.StatusOption;
import com.inscripts.utils.Logger;
import com.inscripts.utils.SessionData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Naseem on 6/20/2016.
 */
public class ChatHandler {
    /*private void getUserInfo(final int userID) {
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

    public class MySubscribeCallbacks implements SubscribeCallbacks {

        @Override
        public void onMessageReceived(JSONObject receivedMessage) {
            try {
                Logger.debug("Message12" + receivedMessage.toString());
                Intent intent = new Intent();
                intent.setAction("NEW_SINGLE_MESSAGE");

					/* Send a broadcast to SingleChatActivity */
            /*    int id;
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
         /*       int id;
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

                        layoutChatFailed.setOnClickListener(null);
                        layoutChatFailed.setVisibility(View.GONE);
                        layoutChatContacts.setVisibility(View.VISIBLE);
                        enableChatComponents(true);

                        statusEditText.clearFocus();
                        searchFriendEditText.requestFocus();
                    }

                    @Override
                    public void failCallback(JSONObject response) {
                        txtViewFriendEmpty.setVisibility(View.VISIBLE);
                        txtViewFriendEmpty.setText(getString(R.string.could_not_connect_to_chat));
                        chatConnectionProgress.setVisibility(View.GONE);
                        layoutChatContacts.setVisibility(View.GONE);
                        layoutChatFailed.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chatConnectionProgress.setVisibility(View.VISIBLE);
                                layoutChatContacts.setVisibility(View.GONE);
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

    public static CometChat instance() {
        return null;
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
    }*/


       /*        C  H  A  T      S  E  C  T  I  O  N        */


}
