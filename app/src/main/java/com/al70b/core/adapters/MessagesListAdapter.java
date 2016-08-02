package com.al70b.core.adapters;

import android.content.Intent;
import android.support.v7.internal.widget.ActionBarOverlayLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.activities.AbstractUserConversationActivity;
import com.al70b.core.activities.audio_video_call.AVChatActivity;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.EndMessage;
import com.al70b.core.objects.Message;
import com.al70b.core.objects.OtherUser;
import com.bumptech.glide.Glide;
import com.inscripts.cometchat.sdk.AVChat;
import com.inscripts.custom.EmojiTextView;
import com.inscripts.interfaces.Callbacks;

import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 7/27/2016.
 */

public class MessagesListAdapter extends ArrayAdapter<Message> {
    private AbstractUserConversationActivity activity;
    private CurrentUser currentUser;
    private OtherUser otherUser;
    private List<Message> data;

    private LayoutInflater inflater;

    public int incomingMessagePosition;
    public int outgoingMessagePosition;

    public MessagesListAdapter(AbstractUserConversationActivity activity,
                               CurrentUser cu, OtherUser ou, List<Message> data) {
        super(activity, 0, data);
        this.activity = activity;
        this.currentUser = cu;
        this.otherUser = ou;
        this.data = data;

        inflater = activity.getLayoutInflater();

        incomingMessagePosition = -1;
        outgoingMessagePosition = -1;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get message at index 'position'
        Message message = data.get(position);

        if (message.getMessageType() == Message.Type.REGULAR) {
            // this is a regular message
            convertView = getRegularMessage(message, convertView, parent);

            int prevMessagePosition = position - 1;

            // in case there is a previous message
            if (prevMessagePosition >= 0) {
                Message previousMessage = data.get(prevMessagePosition);

                // and the previous message is of different senders -> add some space
                if (previousMessage.getMessageType() == Message.Type.REGULAR &&
                        ((previousMessage.isUserMessage() && !message.isUserMessage()) ||
                                (message.isUserMessage() && !previousMessage.isUserMessage()))) {

                }
            }
        } else {
            // message is of a video call type
            convertView = getVideoCallMessage(message, convertView, parent, position);
        }

        return convertView;
    }

    private View getRegularMessage(Message message, View convertView, ViewGroup parent) {
        // create new holder
        final Holder.RegularMessage holder = new Holder.RegularMessage();

        // inflate the right layout depending on the message source
        if (message.isUserMessage()) {
            // this is a user message
            convertView = inflater.inflate(R.layout.list_item_conversation_content_user, parent, false);
            holder.message = (EmojiTextView) convertView.findViewById(R.id.emoji_tv_list_item_messages_message_user);
            holder.dateTime = (TextView) convertView.findViewById(R.id.text_view_list_item_messages_date_user);
        } else {
            // other end message
            if (((EndMessage) message).isProfilePictureVisible()) {
                convertView = inflater.inflate(R.layout.list_item_conversation_content_member_first, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.list_item_conversation_content_member, parent, false);
            }
            holder.profilePicture = (CircleImageView) convertView.findViewById(R.id.circle_image_list_item_messages_profile_picture_member);
            holder.message = (EmojiTextView) convertView.findViewById(R.id.emoji_tv_list_item_messages_last_member);
            holder.dateTime = (TextView) convertView.findViewById(R.id.text_view_list_item_messages_date_member);
        }

        // set message
        holder.message.setEmojiText(message.getMessage());

        // set date and time
        holder.dateTime.setText(message.getDateTimeString());

        // if this is an end message, handle showing profile picture
        if (!message.isUserMessage()) {
            EndMessage endMessage = (EndMessage) message;

            // in case need to hide profile picture
            if (endMessage.isProfilePictureVisible()) {
                // show the profile picture in the circle image view
                Glide.with(activity.getApplicationContext())
                        .load(otherUser.isProfilePictureSet() ?
                                otherUser.getProfilePicture().getThumbnailFullPath() :
                                "")
                        .placeholder(R.drawable.avatar)
                        .into(holder.profilePicture);
                holder.profilePicture.setVisibility(View.VISIBLE);
            } else {
                // hide the profile picture circle image
                holder.profilePicture.setVisibility(View.INVISIBLE);
            }
        }

        // add some functionality to the text views
        // show time when message clicked
        holder.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.dateTime.getVisibility() == View.VISIBLE)
                    holder.dateTime.setVisibility(View.GONE);
                else {
                    holder.dateTime.setVisibility(View.VISIBLE);

                    /*if (!listView.canScrollVertically(listView.getBottom())) {
                        // scroll to the latest message sent
                        listView.setSelection(listView.getAdapter().getCount() - 1);
                    }*/
                }
            }
        });

        return convertView;
    }

    private View getVideoCallMessage(final Message message, View convertView, ViewGroup parent, int position) {

        int type = message.getMessageType();

        if (type == Message.Type.CALL_ACCEPTED || type == Message.Type.CALL_REJECTED || type == Message.Type.CALL_SENT
                || type == Message.Type.NO_ANSWER || type == Message.Type.INCOMING_BUSY_TONE) {
            // outgoing video call
            final Holder.OutgoingVideoMessage holder = new Holder.OutgoingVideoMessage();

            convertView = inflater.inflate(R.layout.list_item_conversation_content_user_video, parent, false);
            holder.header = (TextView) convertView.findViewById(R.id.text_view_list_item_messages_video_user);
            holder.cancel = (Button) convertView.findViewById(R.id.btn_list_item_messages_user_video_cancel);

            handleOutgoingVideoCall(holder, message);

            // save incoming message to change later on response
            outgoingMessagePosition = position;
        } else {
            // incoming video call
            final Holder.IncomingVideoMessage holder = new Holder.IncomingVideoMessage();
            convertView = inflater.inflate(R.layout.list_item_conversation_content_video, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.text_view_list_item_messages_video_name);
            holder.accept = (Button) convertView.findViewById(R.id.btn_list_item_messages_video_accept);
            holder.reject = (Button) convertView.findViewById(R.id.btn_list_item_messages_video_reject);

            handleIncomingVideoCall(holder, message);

            // save incoming message to change later on response
            incomingMessagePosition = position;
        }

        return convertView;
    }


    private void handleOutgoingVideoCall(final Holder.OutgoingVideoMessage holder, final Message message) {

        boolean cancelBtnVisible = false;
        String messageStr = "";

        switch (message.getMessageType()) {
            case Message.Type.CALL_ACCEPTED:
                // other user accepted your video call request
                messageStr = getString(R.string.end_user_accepted_video_call);
                break;
            case Message.Type.CALL_REJECTED:
                // other user rejected your video call request
                messageStr = getString(R.string.end_user_rejected_video_call);
                break;
            case Message.Type.CALL_SENT:
                // you canceled the video call request you sent
                if (message.isMessageActive()) {
                    cancelBtnVisible = true;
                    messageStr = message.getMessage();
                    holder.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AVChat.getAVChatInstance(activity.getApplicationContext())
                                    .cancelAVChatRequest(String.valueOf(otherUser.getUserID()), new Callbacks() {
                                        @Override
                                        public void successCallback(JSONObject response) {
                                            message.setMessage(getString(R.string.video_call_was_canceled));
                                            holder.header.setText(message.getMessage());
                                            holder.cancel.setVisibility(View.GONE);

                                            message.setMessageInactive();
                                            //itemVideo.setEnabled(true);
                                        }

                                        @Override
                                        public void failCallback(JSONObject response) {
                                            message.setMessageInactive();
                                            //itemVideo.setEnabled(true);
                                        }
                                    });
                        }
                    });
                } else {
                    if (message.isMessageFetched()) {
                        messageStr = getString(R.string.user_sent_you_video_call);
                    } else {
                        messageStr = getString(R.string.video_call_was_canceled);
                    }
                }
                break;
            case Message.Type.NO_ANSWER:
                // other user does not answer
                messageStr = getString(R.string.video_call_no_answer);
                break;
            case Message.Type.INCOMING_BUSY_TONE:
                // other user seems to be busy
                messageStr = getString(R.string.video_call_other_user_busy);
                break;
            case Message.Type.CANCEL_CALL:
                messageStr = message.getMessage();
                break;
        }

        if (cancelBtnVisible) {
            holder.cancel.setVisibility(View.VISIBLE);
        } else {
            holder.cancel.setVisibility(View.GONE);
        }

        holder.header.setText(messageStr);
    }

    private void handleIncomingVideoCall(final Holder.IncomingVideoMessage holder, final Message message) {
        boolean btnVisible = false;
        String messageStr = "";

        switch (message.getMessageType()) {
            case Message.Type.INCOMING_CALL:
                // incoming video call request
                if (message.isMessageActive()) {
                    // message is active
                    btnVisible = true;
                    messageStr = getString(R.string.video_chat_call_recieved, otherUser.getName());
                    holder.accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AVChat.getAVChatInstance(activity.getApplicationContext())
                                    .acceptAVChatRequest(String.valueOf(otherUser.getUserID()), new Callbacks() {
                                        @Override
                                        public void successCallback(JSONObject response) {
                                            message.setMessage(getString(R.string.you_accepted_video_call));
                                            holder.accept.setVisibility(View.GONE);
                                            holder.reject.setVisibility(View.GONE);

                                            holder.name.setText(message.getMessage());

                                            message.setMessageInactive();

                                            // open video activity
                                            Intent intent = new Intent(activity, AVChatActivity.class);
                                            intent.putExtra("userID", String.valueOf(otherUser.getUserID()));
                                            activity.startActivity(intent);
                                        }

                                        @Override
                                        public void failCallback(JSONObject response) {
                                            message.setMessageInactive();
                                        }
                                    });
                        }
                    });

                    holder.reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO: check api and fix
                            AVChat.getAVChatInstance(activity.getApplicationContext())
                                    .rejectAVChatRequest("", String.valueOf(otherUser.getUserID()), new Callbacks() {
                                        @Override
                                        public void successCallback(JSONObject response) {
                                            message.setMessage(getString(R.string.you_rejected_video_call));
                                            holder.accept.setVisibility(View.GONE);
                                            holder.reject.setVisibility(View.GONE);
                                            holder.name.setText(message.getMessage());

                                            //itemVideo.setEnabled(true);
                                            message.setMessageInactive();
                                        }

                                        @Override
                                        public void failCallback(JSONObject response) {
                                            holder.accept.setVisibility(View.GONE);
                                            holder.reject.setVisibility(View.GONE);
                                            holder.name.setText(getString(R.string.error_video_call));

                                            //itemVideo.setEnabled(true);
                                            message.setMessageInactive();
                                        }
                                    });
                        }
                    });
                } else {
                    if (message.isMessageFetched())
                        messageStr = getString(R.string.video_chat_call_recieved_fetched,
                                otherUser.getName());
                    else
                        // message is inactive
                        messageStr = message.getMessage();
                }
                break;

            case Message.Type.OUTGOING_BUSY_TONE:
                // you're having a video call and another user requests video call
                messageStr = getString(R.string.user_sent_you_video_call);
                break;

            case Message.Type.CANCEL_CALL:
                messageStr = message.getMessage();
                break;

        }

        if (btnVisible) {
            holder.accept.setVisibility(View.VISIBLE);
            holder.reject.setVisibility(View.VISIBLE);
        } else {
            holder.accept.setVisibility(View.GONE);
            holder.reject.setVisibility(View.GONE);

        }

        holder.name.setText(messageStr);
    }

    private static class Holder {

        private static class RegularMessage {
            EmojiTextView message;
            TextView dateTime;
            CircleImageView profilePicture;
        }

        private static class IncomingVideoMessage {
            TextView name, dateTime;
            Button accept, reject;
        }

        private static class OutgoingVideoMessage {
            TextView header;
            Button cancel;
        }
    }

    private String getString(int resId) {
        return activity.getString(resId);
    }

    private String getString (int resId, String ... strs) {
        return activity.getString(resId, strs);
    }
}

