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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 7/27/2016.
 */
public class UserConversationActivity extends AbstractUserConversationActivity {

    private static final String TAG = "UserConversationA";

    public static final String NEW_SINGLE_MESSAGE = "NEW_SINGLE_MESSAGE";
    public static final String OTHER_USER = "OTHER_USER";

    // receiver of newly incoming message
    private BroadcastReceiver receiver;

    private CometChat cometChat;

    private MenuItem itemVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cometChat = CometChat.getInstance(this, ServerConstants.CONSTANTS.COMET_CHAT_API_KEY);

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

                    case Message.Type.CALL_REJECTED:
                    case Message.Type.CALL_ACCEPTED:
                    case Message.Type.NO_ANSWER:
                    case Message.Type.INCOMING_BUSY_TONE:
                        if (messagesListAdapter.outgoingMessagePosition >= 0) {
                            mListMessages.remove(messagesListAdapter.outgoingMessagePosition);
                            mListMessages.add(messagesListAdapter.outgoingMessagePosition, message);

                            messagesListAdapter.outgoingMessagePosition = -1;
                            itemVideo.setEnabled(true);

                            int type = message.getMessageType();
                            if (type == Message.Type.CALL_ACCEPTED) {
                                Intent intent2 = new Intent(UserConversationActivity.this, AVChatActivity.class);
                                intent2.putExtra("userID", String.valueOf(otherUser.getUserID()));
                                startActivity(intent2);
                            } else if (type == Message.Type.NO_ANSWER) {

                            }

                        }
                        break;
                    case Message.Type.CANCEL_CALL:   // user canceled his request
                        if (messagesListAdapter.incomingMessagePosition >= 0) {
                            mListMessages.remove(messagesListAdapter.incomingMessagePosition);
                            mListMessages.add(messagesListAdapter.incomingMessagePosition, message);

                            messagesListAdapter.incomingMessagePosition = -1;
                            itemVideo.setEnabled(true);
                        }
                        break;
                    case Message.Type.INCOMING_CALL:
                        // add message to the list of messages
                        mListMessages.add(message);
                        break;
                    case Message.Type.OUTGOING_BUSY_TONE:
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

        itemVideo = menu.findItem(R.id.menu_item_user_conversation_video);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.menu_item_user_conversation_video:
                Log.d(TAG, "Video request was sent to the user");
                long dateTime = System.currentTimeMillis() / 1000;

                final Message message = new Message(-1,
                        getString(R.string.video_chat_request_was_sent),
                        dateTime, Message.Type.CALL_SENT);

                // disable video chat button when a video chat is sent
                item.setEnabled(false);

                AVChat.getAVChatInstance(this.getApplicationContext())
                        .sendAVChatRequest(String.valueOf(otherUser.getUserID()), new Callbacks() {
                            @Override
                            public void successCallback(JSONObject jsonObject) {
                                mListMessages.add(message);
                                messagesListAdapter.notifyDataSetChanged();
                                pulledListView.setSelection(messagesListAdapter.getCount() - 1);
                                messagesListAdapter.outgoingMessagePosition = messagesListAdapter.getCount() - 1;
                                message.status = Message.Status.SENT;
                            }

                            @Override
                            public void failCallback(JSONObject jsonObject) {
                                Logger.debug(jsonObject.toString());

                                Toast.makeText(UserConversationActivity.this,
                                        getString(R.string.error_video_call), Toast.LENGTH_SHORT).show();
                                // enable item for if user wants to try again
                                item.setEnabled(true);
                                message.status = Message.Status.FAILED_TO_SEND;
                            }
                        });

                return true;
            case R.id.menu_item_user_conversations_clear_history:
                Log.d(TAG, "Clear history was clicked");
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
                        callback.onSuccess(jsonObject);
                    }

                    @Override
                    public void failCallback(JSONObject jsonObject) {
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
