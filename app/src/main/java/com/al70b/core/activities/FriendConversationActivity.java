package com.al70b.core.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.audio_video_call.AVChatActivity;
import com.al70b.core.objects.EndMessage;
import com.al70b.core.objects.Message;
import com.al70b.core.server_methods.ServerConstants;
import com.inscripts.cometchat.sdk.AVChat;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.interfaces.Callbacks;
import com.inscripts.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 7/27/2016.
 */
public class FriendConversationActivity extends AbstractUserConversationActivity {

    private static final String TAG = "FriendConversationA";

    public static final String NEW_SINGLE_MESSAGE = "NEW_SINGLE_MESSAGE";
    public static final String OTHER_USER = "OTHER_USER";

    // receiver of newly incoming message
    private BroadcastReceiver receiver;

    private CometChat cometChat;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cometChat = CometChat.getInstance(this,
                ServerConstants.CONSTANTS.COMET_CHAT_API_KEY);

        receiver = new SingleChatMessagesReceiver();
    }


    private class SingleChatMessagesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get sender id
            int senderID = intent.getIntExtra("user_id", -1);
            // get message
            EndMessage message = (EndMessage) intent.getSerializableExtra("message");

            if (senderID != -1 && senderID == otherUser.getUserID()) {
                if (duplicateMessages(message))
                    return;

                switch (message.getMessageType()) {
                    case Message.Type.REGULAR:
                        int idx;
                        if ((idx = messagesListAdapter.getCount() - 1) >= 0) {
                            Message lastMessage = messagesListAdapter.getItem(idx);
                            if (!lastMessage.isUserMessage()) {
                                // previous message is also end message
                                // remove bitmap and hide
                                ((EndMessage) lastMessage).setProfilePictureInvisible();
                                ((EndMessage) lastMessage).removeProfilePictureBitmap();
                            }
                        }

                        // this is an end message, show the profile picture
                        message.setProfilePictureVisible();

                        // add new message
                        mListMessages.add(message);
                        break;
                    // end of regular message

                    case Message.Type.VIDEO_CALL_REJECTED:
                    case Message.Type.VIDEO_CALL_ACCEPTED:
                    case Message.Type.VIDEO_CALL_NO_ANSWER:
                    case Message.Type.VIDEO_CALL_INCOMING_BUSY_TONE:
                    case Message.Type.VIDEO_CALL_CURRENT_USER_CANCELED_CALL:
                        if (messagesListAdapter.outgoingMessagePosition >= 0) {
                            mListMessages.remove(messagesListAdapter.outgoingMessagePosition);
                            mListMessages.add(messagesListAdapter.outgoingMessagePosition, message);

                            messagesListAdapter.outgoingMessagePosition = -1;
                            message.setMessageInactive();
                            enableVideoChat(true);

                            int type = message.getMessageType();
                            if (type == Message.Type.VIDEO_CALL_ACCEPTED) {
                                Intent intent2 = new Intent(FriendConversationActivity.this, AVChatActivity.class);
                                intent2.putExtra("userID", String.valueOf(otherUser.getUserID()));
                                intent2.putExtra("callId", message.callId);
                                startActivity(intent2);
                            }
                        }
                        break;
                    case Message.Type.VIDEO_CALL_END_USER_CANCELED_CALL:   // user canceled his request
                        if (messagesListAdapter.incomingMessagePosition >= 0) {
                            mListMessages.remove(messagesListAdapter.incomingMessagePosition);
                            mListMessages.add(messagesListAdapter.incomingMessagePosition, message);

                            messagesListAdapter.incomingMessagePosition = -1;
                            enableVideoChat(true);
                        }
                        break;
                    case Message.Type.VIDEO_CALL_INCOMING_CALL:
                        // add message to the list of messages
                        mListMessages.add(message);
                        break;
                    case Message.Type.VIDEO_CALL_OUTGOING_BUSY_TONE:
                        mListMessages.add(message);
                        break;
                }


                // notify adapter
                messagesListAdapter.notifyDataSetChanged();
                pulledListView.setSelection(messagesListAdapter.getCount() - 1);
            }
        }

        private boolean duplicateMessages(Message message) {
            int idx = messagesListAdapter.getCount() - 1;

            if (idx >= 0) {
                Message lastMessage = messagesListAdapter.getItem(idx);

                if (message.getDateTime() == lastMessage.getDateTime() &&
                        message.getMessageType() == lastMessage.getMessageType() &&
                        message.getMessageType() != Message.Type.REGULAR)
                    return true; // this is a duplicate probably, ignore it
            }

            return false;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.menu_item_user_conversation_video:
                Log.d(TAG, "Video request was sent to the user for a friend user");
                long dateTime = System.currentTimeMillis() / 1000;
                final Message message = new Message(-1,
                        getString(otherUser.isMale()?
                                    R.string.you_sent_video_chat_request_to_male :
                                    R.string.you_sent_video_chat_request_to_female,
                                otherUser.getName()),
                        dateTime, Message.Type.VIDEO_CALL_SENT);

                // disable video chat button when a video chat is sent
                enableVideoChat(false);

                mListMessages.add(message);
                messagesListAdapter.notifyDataSetChanged();

                pulledListView.setSelection(messagesListAdapter.getCount() - 1);
                messagesListAdapter.outgoingMessagePosition = messagesListAdapter.getCount() - 1;
                message.status = Message.Status.SENDING;

                AVChat.getAVChatInstance(this.getApplicationContext())
                        .sendAVChatRequest(String.valueOf(otherUser.getUserID()), new Callbacks() {
                            @Override
                            public void successCallback(JSONObject jsonObject) {
                                Log.d(TAG, "Successfully sent video chat request. Response: " + jsonObject.toString());
                                message.status = Message.Status.SENT;

                                try {
                                    message.callId = jsonObject.getString("callid");
                                } catch (JSONException e) {
                                    Log.e(TAG, e.toString());
                                }
                            }

                            @Override
                            public void failCallback(JSONObject jsonObject) {
                                Log.d(TAG, "Failed to send video chat request. Response: " + jsonObject.toString());
                                Logger.debug(jsonObject.toString());

                                Toast.makeText(FriendConversationActivity.this,
                                        getString(R.string.error_video_call), Toast.LENGTH_SHORT).show();
                                // enable item for if user wants to try again
                                enableVideoChat(true);
                                message.status = Message.Status.FAILED_TO_SEND;
                            }
                        });
                return true;
            case R.id.menu_item_user_conversations_clear_history:
                Log.d(TAG, "Clear history was clicked for a friend user");
                /*cometChat.deleteHistory(String.valueOf(otherUserID), new Callbacks() {
                    @Override
                    public void successCallback(JSONObject jsonObject) {
                        Log.d("ClearHistoryS", jsonObject.toString());
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.history_was_cleared), Toast.LENGTH_SHORT).show();

                        mListMessages.clear();
                        messagesListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void failCallback(JSONObject jsonObject) {
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_clearing_conversations), Toast.LENGTH_SHORT).show();
                    }
                });*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // register a receiver to receive messages
        registerReceiver(receiver, new IntentFilter(NEW_SINGLE_MESSAGE));
    }

    @Override
    public void onStop() {
        super.onStop();

        // unregister messages receiver
        unregisterReceiver(receiver);
    }

    @Override
    void sendMessage(String messageText, final MyCallback callback) {
        cometChat.sendMessage(String.valueOf(otherUser.getUserID()),
                messageText, new Callbacks() {
                    @Override
                    public void successCallback(JSONObject jsonObject) {
                        Log.d(TAG, "Successfully sent message to user. Response: " + jsonObject.toString());
                        callback.onSuccess(jsonObject);
                    }

                    @Override
                    public void failCallback(JSONObject jsonObject) {
                        Log.d(TAG, "Failed to send message to user. Response: " + jsonObject.toString());
                        callback.onFail(jsonObject);
                    }
                });
    }

    @Override
    List<Message> getHistory(int messagesFetchedLimit) {
        long otherUserID = (long) otherUser.getUserID();
        List<Message> list = new ArrayList<>();

        cometChat.getChatHistory(otherUserID, -1L, new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {
                Log.d(TAG, "Getting history: " + jsonObject.toString());

            }

            @Override
            public void failCallback(JSONObject jsonObject) {
                Log.e(TAG, "Getting history failed: " + jsonObject.toString());
            }
        });

        return list;
    }


}
