package com.al70b.core.activities.user_home_activity_underlying;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.audio_video_call.AVChatActivity;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.EndMessage;
import com.al70b.core.objects.FriendsDrawerItem;
import com.al70b.core.objects.Message;
import com.al70b.core.objects.Pair;
import com.al70b.core.server_methods.ServerConstants;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.enums.StatusOption;
import com.inscripts.interfaces.Callbacks;
import com.inscripts.interfaces.SubscribeCallbacks;
import com.inscripts.utils.Logger;
import com.inscripts.utils.SessionData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

/**
 * Created by Naseem on 6/20/2016.
 */
public class ChatHandler {

    private static final String TAG = "ChatHandler";
    private static final long TIME_RETRY_CHAT_LOGIN = 30 * 1000; // 30 seconds, in milliseconds

    private Context context;
    private CurrentUser currentUser;
    private ChatHandlerEvents chatHandlerEvents;
    private List<FriendsDrawerItem> onlineFriendsList;

    public ChatHandler(Context context, CurrentUser currentUser,
                       List<FriendsDrawerItem> onlineFriendsList) {
        this(context, currentUser, onlineFriendsList, null);
    }

    public ChatHandler(Context context, CurrentUser currentUser,
                       List<FriendsDrawerItem> onlineFriendsList, ChatHandlerEvents e) {
        this.context = context;
        this.currentUser = currentUser;

        if(onlineFriendsList == null) {
            this.onlineFriendsList = new ArrayList<>();
        }

        chatHandlerEvents = e;

        init();
    }

    public ChatHandler(Context context, CurrentUser currentUser) {
        this(context, currentUser, null);
    }

    private CometChat cometChatInstance;
    private Timer retryChatLoginTimer;

    private void init() {
        // get singleton instance of comet chat, and login
        cometChatInstance = CometChat.getInstance(context, ServerConstants.COMET_CHAT_API_KEY);

        if(chatHandlerEvents != null) {
            login();
        }


        retryChatLoginTimer = new Timer();
        retryChatLoginTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG + ":Chat Timer", "Timer running..");
                if(!CometChat.isLoggedIn() && chatHandlerEvents != null) {
                    login();
                }
            }
        }, 0, TIME_RETRY_CHAT_LOGIN);
    }

    private String getString(int resource) {
        return context.getResources().getString(resource);
    }

    public void login() {
        if(chatHandlerEvents == null) {
            return;
        }

        // invoke pre chat connection setup
        chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
            @Override
            public void call() {
                chatHandlerEvents.onChatConnectionSetup();
            }
        });

        cometChatInstance.login(ServerConstants.CHAT_URL,
                currentUser.getName(),
                currentUser.getPassword(),
                new Callbacks() {
                    @Override
                    public void successCallback(JSONObject response) {
                        Logger.debug(TAG + ", login succeeded: " + response.toString());
                        // if login successful, subscribe
                        cometChatInstance.subscribe(true, new MySubscribeCallbacks());

                        // list of blocked users
                        getBlockedUsers();

                        chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                            @Override
                            public void call() {
                                chatHandlerEvents.onChatConnectionSucceeded();
                            }
                        });
                    }

                    @Override
                    public void failCallback(JSONObject response) {
                        Logger.debug(TAG + ", login failed: " + response.toString());

                        chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                            @Override
                            public void call() {
                                chatHandlerEvents.onChatConnectionFailed();
                            }
                        });
                    }
                });
    }

    public void setOnChatConnectionEvents(ChatHandlerEvents onChatConnection) {
        this.chatHandlerEvents = onChatConnection;
    }

    public void setStatusMessage(String statusMessage) {
        cometChatInstance.setStatusMessage(statusMessage, new Callbacks() {
            @Override
            public void successCallback(final JSONObject jsonObject) {
                chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                    @Override
                    public void call() {
                        chatHandlerEvents.onSetStatusMessageResponse(true, jsonObject.toString());
                    }
                });
            }

            @Override
            public void failCallback(final JSONObject jsonObject) {
                chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                    @Override
                    public void call() {
                        chatHandlerEvents.onSetStatusMessageResponse(false, jsonObject.toString());
                    }
                });
            }
        });
    }

    public void logout() {
        if(retryChatLoginTimer != null) {
            retryChatLoginTimer.cancel();
        }

        cometChatInstance.logout(new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {
                Log.d(TAG + ": Logout (Y)", jsonObject.toString());
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
                Log.e(TAG + ": Logout (N)", jsonObject.toString());
            }
        });
    }

    public void setStatus(StatusOption so) {
        cometChatInstance.setStatus(so, new Callbacks() {
            @Override
            public void successCallback(final JSONObject jsonObject) {
                chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                    @Override
                    public void call() {
                        chatHandlerEvents.onSetStatusResponse(true, jsonObject.toString());
                    }
                });
            }

            @Override
            public void failCallback(final JSONObject jsonObject) {
                chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                    @Override
                    public void call() {
                        chatHandlerEvents.onSetStatusResponse(false, jsonObject.toString());
                    }
                });
            }
        });
    }

    public boolean isLoggedIn() {
        // TODO: find a better implementation since api is deprecated
        return cometChatInstance.isLoggedIn();
    }

    public static abstract class ChatHandlerEvents {

        private Handler handler;

        public ChatHandlerEvents() {
            handler = new Handler();
        }

        private interface Callable {
            void call();
        }

        private void runWithHandler(final Callable func) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    func.call();
                }
            });
        }

        void onAVChatMessageReceived(int userId, EndMessage msg) {}

        void onMessageReceived(int userId, EndMessage msg) {}

        void onChatConnectionSetup(){}

        void onProfileInfoReceived(String status, String statusMessage){}

        void onFriendsOnlineListUpdated(){}

        void onSetStatusMessageResponse(boolean statusChanged, String msg){}

        void onSetStatusResponse(boolean statusChanged, String msg) {}

        abstract void onChatConnectionFailed();

        abstract void onChatConnectionSucceeded();
    }

    private void getBlockedUsers() {
        // create blocked users list
        final List<Pair> blockedUsersList = new ArrayList<>();

        cometChatInstance.getBlockedUserList(new Callbacks() {
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

    public class MySubscribeCallbacks implements SubscribeCallbacks {

        @Override
        public void onMessageReceived(JSONObject receivedMessage) {
            Logger.debug(TAG + ", Message Received: " + receivedMessage.toString());
            try {
                Intent intent = new Intent();
                intent.setAction("NEW_SINGLE_MESSAGE");

				/* Send a broadcast to SingleChatActivity */
                int id;
                String msg;
                long dateTime;

                id = receivedMessage.getInt("id");
                final int otherUserID = receivedMessage.getInt("from");
                // TODO handle icons
                msg = receivedMessage.getString("message");
                dateTime = receivedMessage.getLong("sent");

                final EndMessage message = new EndMessage(id, msg, dateTime, Message.REGULAR);
                intent.putExtra("user_id", otherUserID);
                intent.putExtra("message", message);
                context.sendBroadcast(intent);

                // notify friends & chat drawer
                chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                    @Override
                    public void call() {
                        chatHandlerEvents.onMessageReceived(otherUserID, message);
                    }
                });
            } catch (Exception e) {
                Logger.error(TAG + ":" + e.getMessage());
            }
        }

        @Override
        public void onError(JSONObject errorResponse) {
            Logger.error(TAG + ":" + errorResponse.toString());
        }

        @Override
        public void gotProfileInfo(JSONObject profileInfo) {
            Logger.debug(TAG + ", Message Received: " + profileInfo.toString());

            try {
                final String status = profileInfo.getString("s"); // get online status
                final String message = profileInfo.getString("m");  // get status message

                // set online status and status message
                currentUser.setOnlineStatus(status);
                currentUser.setStatusMessage(message);

                chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                    @Override
                    public void call() {
                        chatHandlerEvents.onProfileInfoReceived(status, message);
                    }
                });
            } catch (JSONException ex) {
                Logger.error(TAG + ":" + ex.getMessage());
            }
        }

        @Override
        public void gotOnlineList(JSONObject onlineUsers) {
            Logger.debug(TAG + ", Online List Received: " + onlineUsers.toString());
            try {
                if (onlineFriendsList != null) {
                    Iterator<String> keys = onlineUsers.keys();

                    onlineFriendsList.clear();
                    while (keys.hasNext()) {
                        JSONObject user = onlineUsers.getJSONObject(keys.next());
                        String username = user.getString("n");

                        onlineFriendsList
                                .add(new FriendsDrawerItem(user.getInt("id"), user.getLong("t"), username,
                                                            user.getString("a"), user.getString("s"),
                                                            user.getString("m")));
                    }

                    // invoke registered function to update friends and chat drawer
                    chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                        @Override
                        public void call() {
                            chatHandlerEvents.onFriendsOnlineListUpdated();
                        }
                    });
                } else {
                    Logger.error(TAG + ": onlineFriendsList is null");
                }
            } catch (Exception e) {
                Logger.error(TAG + ":" + e.getMessage());
            }
        }

        @Override
        public void onAVChatMessageReceived(JSONObject response) {
            Logger.debug(TAG + ", AVChat Message Received: " + response.toString());
            try {
                Intent intent = new Intent();
                intent.setAction("NEW_SINGLE_MESSAGE");

				/* Send a broadcast to SingleChatActivity */
                int id;
                String msg;
                long dateTime;
                int messageType;

                id = response.getInt("id");
                final int otherUserID = response.getInt("from");
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
                        Toast.makeText(context, getString(R.string.end_user_ended_call), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                final EndMessage message = new EndMessage(id, msg, dateTime, messageType);

                intent.putExtra("user_id", otherUserID);
                intent.putExtra("message", message);

                context.sendBroadcast(intent);

                // notify friends & chat drawer
                chatHandlerEvents.runWithHandler(new ChatHandlerEvents.Callable() {
                    @Override
                    public void call() {
                        chatHandlerEvents.onAVChatMessageReceived(otherUserID, message);
                    }
                });
            } catch (Exception e) {
                Logger.error(TAG + ":" + e.getMessage());
            }
        }

        @Override
        public void onActionMessageReceived(JSONObject jsonObject) {

        }

        @Override
        public void gotAnnouncement(JSONObject announcement) {
            Logger.debug(TAG + ", Announcement Received: " + announcement.toString());
        }
    }
}






