package com.al70b.core.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.activities.audio_video_call.AVChatActivity;
import com.al70b.core.extended_widgets.pull_load_listview.PullToRefreshListView;
import com.al70b.core.objects.EndMessage;
import com.al70b.core.objects.Message;
import com.al70b.core.server_methods.ServerConstants;
import com.bumptech.glide.Glide;
import com.inscripts.Keyboards.SmileyKeyBoard;
import com.inscripts.cometchat.sdk.AVChat;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.interfaces.Callbacks;
import com.inscripts.interfaces.EmojiClickInterface;
import com.inscripts.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import com.inscripts.custom.EmojiTextView;

/**
 * Created by Naseem on 5/25/2015.
 */
public class UserConversationsInternalFragment extends Fragment
        implements BackPressedFragment, EmojiClickInterface {

    private static final int NUMBER_OF_FETCHED_MESSAGES = 10;
    public int incomingMessagePosition = -1, outgoingMessagePosition = -1;

    // showing one conversation at a time
    private int otherUserID = -1;
    private String otherUserName;
    private Bitmap otherUserBitmap;
    private long messageID = -1;     // to fetch last messages

    private boolean noMoreMessages;

    // Messages Manager
    private final static SparseArray<ArrayList<Message>> messagesManager = new SparseArray<>(15);

    // list of messages to show in the listview
    private ArrayList<Message> mListMessages;

    // list view to hold the messages
    private ListView listView;
    private MessagesListAdapter messagesListAdapter;

    // receiver of newly incoming message
    private BroadcastReceiver receiver;

    private CometChat cometChat;

    private MenuItem itemVideo;

    private StringBuilder msgToServer = new StringBuilder();

    public int otherUserID() {
        return otherUserID;
    }

    private SmileyKeyBoard smiliKeyboard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cometChat = CometChat.getInstance(getActivity().getApplicationContext(), ServerConstants.CONSTANTS.COMET_CHAT_API_KEY);

        otherUserID = getArguments().getInt("ID");
        otherUserName = getArguments().getString("Name");
        final String thumbnailPath = getArguments().getString("Bitmap");

        if (otherUserName == null) {
            // something wrong with passed parameters

        } else if (messagesManager.indexOfKey(otherUserID) < 0) {
            // initialize list
            mListMessages = new ArrayList<>();
            messagesManager.put(otherUserID, mListMessages);
        } else {
            mListMessages = messagesManager.get(otherUserID);
        }

        // get other user's thumbnail
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    otherUserBitmap = Glide.with(getActivity()).load(thumbnailPath).
                            asBitmap().
                            into(-1, -1).
                            get();
                } catch (Exception ex) {
                }
            }
        }).start();

        getActivity().setTitle(getString(R.string.chat_with, otherUserName));

        // in order to add video menu items
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.activity_abs_user_conversation, container, false);

        listView = (ListView) viewGroup.findViewById(R.id.listview_user_messages_conversation);
        final EditText etMessage = (EditText) viewGroup.findViewById(R.id.et_user_messages_message);
        final ImageButton ibSend = (ImageButton) viewGroup.findViewById(R.id.image_button_user_messages_send);
        final ImageButton emojiButton = (ImageButton) viewGroup.findViewById(R.id.image_button_user_messages_emoji);


        smiliKeyboard = new SmileyKeyBoard();
        //smiliKeyboard.enable(this, this, R.id.layout_for_emoticons, etMessage);
        final LinearLayout chatFooter = (LinearLayout) viewGroup.findViewById(R.id.layout_bottom_area);
        smiliKeyboard.checkKeyboardHeight(chatFooter);
        smiliKeyboard.enableFooterView(etMessage);

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smiliKeyboard.showKeyboard(chatFooter);
            }
        });

        //Will automatically set size according to the soft keyboard size
       /* popup.setSizeForSoftKeyboard();

        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        smiliKeyboard.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });


        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (etMessage == null || emojicon == null) {
                    return;
                }

                int start = etMessage.getSelectionStart();
                int end = etMessage.getSelectionEnd();

                if (start < 0) {
                    etMessage.append(emojicon.getEmoji());
                } else {
                    etMessage.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                etMessage.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if (!popup.isShowing()) {

                    //If keyboard is visible, simply show the emoji popup
                    if (popup.isKeyBoardOpen()) {
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else {
                        etMessage.setFocusableInTouchMode(true);
                        etMessage.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else {
                    popup.dismiss();
                }
            }
        });*/

        // can't click on empty message
        ibSend.setEnabled(false);

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    ibSend.setEnabled(true);
                    ibSend.setImageResource(R.drawable.ic_action_send_red);
                } else {
                    ibSend.setEnabled(false);
                    ibSend.setImageResource(R.drawable.ic_action_send);
                }
            }
        });

        etMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    // scroll to the latest message sent
                    listView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listView.setSelection(listView.getAdapter().getCount() - 1);
                        }
                    }, 800);
                }
            }
        });

        etMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if (popup.isShowing()) {
                //     emojiButton.callOnClick();
                // }
            }
        });

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(etMessage.getText().toString());

                // empty edit text from message
                etMessage.setText("");
            }
        });


        messagesListAdapter = new MessagesListAdapter(getActivity(), mListMessages);

        // create & set created adapter to list view
        listView.setAdapter(messagesListAdapter);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // get sender id
                int senderID = intent.getIntExtra("user_id", -1);
                // get message
                EndMessage message = (EndMessage) intent.getSerializableExtra("message");

                if (-1 != senderID && senderID == otherUserID) {
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
                            message.setProfilePictureBitmap(otherUserBitmap);
                            message.setProfilePictureVisible();

                            // add new message
                            mListMessages.add(message);
                            break;
                        // end of regular message

                        case Message.Type.CALL_REJECTED:
                        case Message.Type.CALL_ACCEPTED:
                        case Message.Type.NO_ANSWER:
                        case Message.Type.INCOMING_BUSY_TONE:
                            if (outgoingMessagePosition >= 0) {
                                mListMessages.remove(outgoingMessagePosition);
                                mListMessages.add(outgoingMessagePosition, message);

                                outgoingMessagePosition = -1;
                                itemVideo.setEnabled(true);

                                int type = message.getMessageType();
                                if (type == Message.Type.CALL_ACCEPTED) {
                                    Intent intent2 = new Intent(getActivity(), AVChatActivity.class);
                                    intent2.putExtra("userID", String.valueOf(otherUserID));
                                    startActivity(intent2);
                                } else if (type == Message.Type.NO_ANSWER) {

                                }

                            }
                            break;
                        case Message.Type.CANCEL_CALL:   // user canceled his request
                            if (incomingMessagePosition >= 0) {
                                mListMessages.remove(incomingMessagePosition);
                                mListMessages.add(incomingMessagePosition, message);

                                incomingMessagePosition = -1;
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
                    listView.setSelection(messagesListAdapter.getCount() - 1);
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
        };

        // Set a listener to be invoked when the list should be refreshed.
        ((PullToRefreshListView) listView).setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {

            public void onRefresh() {
                // Do work to refresh the list here.
                new PullToRefreshDataTask().execute();
            }
        });


        //listView.addFooterView(lay);
        return viewGroup;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = getActivity().isChangingConfigurations();

        if (!drawerOpen) { // if drawer is closed add the content menu item
            menu.add(Menu.NONE, R.id.menu_item_user_conversation_video, 1, R.string.video_call)
                    .setIcon(R.drawable.ic_action_video)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            itemVideo = menu.findItem(R.id.menu_item_user_conversation_video);

            menu.add(Menu.NONE, R.id.menu_item_user_conversations_clear_history, Menu.NONE, getString(R.string.clear_history))
                    .setIcon(R.drawable.ic_action_discard)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                // implementation is in the activity
                return false;
            case R.id.action_friends:
                // implementation is in the activity
                return false;
            case R.id.menu_item_user_conversation_video:
                long dateTime = System.currentTimeMillis() / 1000;

                final Message message = new Message(-1, getString(R.string.video_chat_request_was_sent), dateTime, Message.Type.CALL_SENT);


                // disable video chat button when a video chat is sent
                item.setEnabled(false);

                AVChat.getAVChatInstance(getActivity().getApplicationContext()).sendAVChatRequest(String.valueOf(otherUserID), new Callbacks() {
                    @Override
                    public void successCallback(JSONObject jsonObject) {
                        mListMessages.add(message);
                        messagesListAdapter.notifyDataSetChanged();
                        listView.setSelection(messagesListAdapter.getCount() - 1);
                        outgoingMessagePosition = messagesListAdapter.getCount() - 1;
                    }

                    @Override
                    public void failCallback(JSONObject jsonObject) {
                        Logger.debug(jsonObject.toString());

                        Toast.makeText(getActivity(), getString(R.string.error_video_call), Toast.LENGTH_SHORT).show();
                        // enable item for if user wants to try again
                        item.setEnabled(true);
                    }
                });

                return true;

            case R.id.menu_item_user_conversations_clear_history:
                if (otherUserID == -1)
                    return true;

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
        getActivity().registerReceiver(receiver, new IntentFilter("NEW_SINGLE_MESSAGE"));

        if (mListMessages.isEmpty()) {
            /*// fetch messages
            ((PullToRefreshListView) listView).onRefresh();

            // scroll to the latest message sent
            listView.setSelection(mListMessages.size() - 1);*/
        } else {
            // scroll to the latest message sent
            listView.setSelection(mListMessages.size() - 1);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // unregister messages receiver
        getActivity().unregisterReceiver(receiver);
    }


    private void sendMessage(String messageText) {
        long date = System.currentTimeMillis() / 1000;

        final Message msg = new Message(-1, messageText, date, Message.Type.REGULAR);

        mListMessages.add(msg);
        messagesListAdapter.notifyDataSetChanged();

        if (listView.canScrollVertically(listView.getBottom()))
            // scroll to the latest message sent
            listView.setSelection(listView.getAdapter().getCount() - 1);

        cometChat.sendMessage(String.valueOf(otherUserID), messageText, new Callbacks() {
            @Override
            public void successCallback(JSONObject jsonObject) {

                msg.status = Message.Status.SENT;
            }

            @Override
            public void failCallback(JSONObject jsonObject) {
                msg.status = Message.Status.FAILED_TO_SEND;
            }
        });
    }

    @Override
    public boolean onBackPressed() {

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove(this);
        trans.commit();
        manager.popBackStack();

        return true;
    }

    private void changeEmojiKeyboardIcon(ImageButton iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
    }

    @Override
    public void getClickedEmoji(int i) {
        smiliKeyboard.getClickedEmoji(i);
    }

    /**
     * this class implements the "load more messages" functionality
     */
    private class PullToRefreshDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            if (noMoreMessages)
                return null;

            if (isCancelled()) {
                return null;
            }

            if (mListMessages.size() > 0) {

            }

            cometChat.getChatHistory((long) otherUserID, messageID, new Callbacks() {
                @Override
                public void successCallback(JSONObject jsonObject) {
                    Log.d("History", jsonObject.toString());

                    boolean checkForMore = true;

                    ArrayList<Message> list = new ArrayList<Message>();
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("history");

                        if (messageID == -1)
                            messageID = Long.MAX_VALUE;


                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject temp = jsonArray.getJSONObject(i);

                            int id, senderID, messageType;
                            long dateTime;
                            String message;

                            id = temp.getInt("id");
                            senderID = temp.getInt("from");
                            message = temp.getString("message");
                            dateTime = temp.getLong("sent");
                            messageType = temp.getInt("message_type");

                            // if message is either of these two, just ignore
                            if (!(messageType == Message.Type.REGULAR || messageType == Message.Type.INCOMING_CALL))
                                continue;

                            Message msg;

                            if (id < messageID)
                                messageID = id;

                            if (checkForMore) {
                                checkForMore = false;

                                // check if that's it with messages history
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        cometChat.getChatHistory((long) otherUserID, messageID, new Callbacks() {
                                            @Override
                                            public void successCallback(JSONObject jsonObject) {
                                                try {
                                                    JSONArray jsonArray = jsonObject.getJSONArray("history");
                                                    if (jsonArray.length() == 0) {
                                                        noMoreMessages = true;

                                                        listView.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ((PullToRefreshListView) listView).setNoMore(true);
                                                                messagesListAdapter.notifyDataSetChanged();
                                                            }
                                                        });

                                                    }
                                                } catch (JSONException ex) {
                                                }
                                            }

                                            @Override
                                            public void failCallback(JSONObject jsonObject) {

                                            }
                                        });
                                    }
                                }).start();
                            }

                            if (senderID == otherUserID) {
                                // message from other user to current user
                                msg = new EndMessage(id, message, dateTime, messageType, otherUserBitmap);
                            } else {
                                // the other way around
                                msg = new Message(id, message, dateTime, messageType);
                            }

                            // message is fetched from server, thus it is inactive & fetched

                            //if(!(msg.getMessageType() == Message.INCOMING_CALL && (System.nanoTime() - msg.getDateTime() > 10000)))
                            msg.setMessageInactive();
                            msg.setMessageFetched();
                            msg.status = Message.Status.SENT;

                            if (i > 0 && msg instanceof EndMessage) {
                                Message previousMessage;

                                if (list.size() == 0) {
                                    previousMessage = mListMessages.get(mListMessages.size() - 1);
                                } else {
                                    // already there is a message
                                    previousMessage = list.get(list.size() - 1);
                                }

                                if (previousMessage.isUserMessage())
                                    ((EndMessage) msg).setProfilePictureVisible();
                                else if (!msg.isUserMessage() && messageType == Message.Type.REGULAR) {
                                    // previous message is an end message
                                    ((EndMessage) previousMessage).setProfilePictureInvisible();
                                    ((EndMessage) previousMessage).removeProfilePictureBitmap();
                                    ((EndMessage) msg).setProfilePictureVisible();
                                }
                            }

                            list.add(msg);
                        }

                        mListMessages.addAll(0, list);
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                messagesListAdapter.notifyDataSetChanged();
                            }
                        });

                    } catch (JSONException ex) {
                    }
                }

                @Override
                public void failCallback(JSONObject jsonObject) {
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            messagesListAdapter.notifyDataSetChanged();
            listView.setSelection(messagesListAdapter.getCount() - 1);

            // Call onLoadMoreComplete when the LoadMore task, has finished
            ((PullToRefreshListView) listView).onRefreshComplete();

            super.onPostExecute(result);
        }
    }

    public class MessagesListAdapter extends ArrayAdapter<Message> {
        private Context context;
        private List<Message> data;

        public MessagesListAdapter(Context context, List<Message> data) {
            super(context, 0, data);
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get message at index 'position'
            final Message message = data.get(position);

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            if (message.getMessageType() == Message.Type.REGULAR)
                // this is a regular message
                convertView = inflateRegularMessage(message, inflater, convertView, parent);
            else
                // message is of a video call type
                convertView = inflateVideoCallMessage(message, inflater, convertView, parent, position);

            return convertView;
        }

        private View inflateRegularMessage(Message message, LayoutInflater inflater, View convertView, ViewGroup parent) {
            // create new holder
            MessageItemHolder holder = new MessageItemHolder();

            // inflate the right layout depending on the message source
            if (message.isUserMessage()) {
                // this is a user message
                convertView = inflater.inflate(R.layout.list_item_conversation_content_user, parent, false);
                holder.message = (EmojiTextView) convertView.findViewById(R.id.emoji_tv_list_item_messages_message_user);
                holder.dateTime = (TextView) convertView.findViewById(R.id.text_view_list_item_messages_date_user);
            } else {
                // other end message
                if (((EndMessage) message).isProfilePictureVisible())
                    convertView = inflater.inflate(R.layout.list_item_conversation_content_member_first, parent, false);
                else
                    convertView = inflater.inflate(R.layout.list_item_conversation_content_member, parent, false);
                holder.profilePicture = (CircleImageView) convertView.findViewById(R.id.circle_image_list_item_messages_profile_picture_member);
                holder.message = (EmojiTextView) convertView.findViewById(R.id.emoji_tv_list_item_messages_last_member);
                holder.dateTime = (TextView) convertView.findViewById(R.id.text_view_list_item_messages_date_member);
            }

            // set message
            holder.message.setText(message.getMessage());

            // set date and time
            holder.dateTime.setText(message.getDateTimeString());

            // if this is an end message, handle showing profile picture
            if (!message.isUserMessage()) {
                EndMessage endMessage = (EndMessage) message;

                // in case need to hide profile picture
                if (endMessage.isProfilePictureVisible()) {
                    // show the profile picture in the circle image view
                    holder.profilePicture.setVisibility(View.VISIBLE);
                    holder.profilePicture.setImageBitmap(endMessage.getProfilePictureBitmap());
                } else {
                    // hide the profile picture circle image
                    holder.profilePicture.setVisibility(View.INVISIBLE);
                }
            }

            // add some functionality to the text views
            // show time when message clicked
            holder.message.setOnClickListener(new ClickListener(holder.dateTime));

            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        private View inflateVideoCallMessage(final Message message, LayoutInflater inflater, View convertView, ViewGroup parent, int position) {

            int type = message.getMessageType();

            if (type == Message.Type.CALL_ACCEPTED || type == Message.Type.CALL_REJECTED || type == Message.Type.CALL_SENT
                    || type == Message.Type.NO_ANSWER || type == Message.Type.INCOMING_BUSY_TONE) {
                // outgoing video call
                final OutgoingVideoMessageItemHolder holder = new OutgoingVideoMessageItemHolder();

                convertView = inflater.inflate(R.layout.list_item_conversation_content_user_video, parent, false);
                holder.header = (TextView) convertView.findViewById(R.id.text_view_list_item_messages_video_user);
                holder.cancel = (Button) convertView.findViewById(R.id.btn_list_item_messages_user_video_cancel);

                handleOutgoingVideoCall(holder, message);
            } else {
                // incoming video call
                final IncomingVideoMessageItemHolder holder = new IncomingVideoMessageItemHolder();

                incomingMessagePosition = position;

                convertView = inflater.inflate(R.layout.list_item_conversation_content_video, parent, false);
                holder.name = (TextView) convertView.findViewById(R.id.text_view_list_item_messages_video_name);
                holder.accept = (Button) convertView.findViewById(R.id.btn_list_item_messages_video_accept);
                holder.reject = (Button) convertView.findViewById(R.id.btn_list_item_messages_video_reject);

                handleIncomingVideoCall(holder, message);
            }

            return convertView;
        }

        private void handleOutgoingVideoCall(final OutgoingVideoMessageItemHolder holder, final Message message) {

            boolean cancelBtnVisible = false;
            String messageStr = "";

            switch (message.getMessageType()) {
                case Message.Type.CALL_ACCEPTED:
                    // other user accepted your video call request
                    messageStr = context.getString(R.string.end_user_accepted_video_call);
                    break;
                case Message.Type.CALL_REJECTED:
                    // other user rejected your video call request
                    messageStr = context.getString(R.string.end_user_rejected_video_call);
                    break;
                case Message.Type.CALL_SENT:
                    // you canceled your video call request
                    if (message.isMessageActive()) {
                        cancelBtnVisible = true;
                        messageStr = message.getMessage();
                        holder.cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AVChat.getAVChatInstance(context).cancelAVChatRequest(String.valueOf(otherUserID), new Callbacks() {
                                    @Override
                                    public void successCallback(JSONObject response) {
                                        message.setMessage(context.getString(R.string.video_call_was_canceled));
                                        holder.header.setText(message.getMessage());
                                        holder.cancel.setVisibility(View.GONE);

                                        message.setMessageInactive();
                                        itemVideo.setEnabled(true);
                                    }

                                    @Override
                                    public void failCallback(JSONObject response) {
                                        message.setMessageInactive();
                                        itemVideo.setEnabled(true);
                                    }
                                });
                            }
                        });
                    } else {
                        if (message.isMessageFetched())
                            messageStr = context.getString(R.string.user_sent_you_video_call);
                        else
                            messageStr = context.getString(R.string.video_call_was_canceled);
                    }
                    break;
                case Message.Type.NO_ANSWER:
                    // other user does not answer
                    messageStr = context.getString(R.string.video_call_no_answer);
                    break;
                case Message.Type.INCOMING_BUSY_TONE:
                    // other user seems to be busy
                    messageStr = context.getString(R.string.video_call_other_user_busy);
                    break;
                case Message.Type.CANCEL_CALL:
                    messageStr = message.getMessage();
                    break;
            }

            if (cancelBtnVisible)
                holder.cancel.setVisibility(View.VISIBLE);
            else
                holder.cancel.setVisibility(View.GONE);

            holder.header.setText(messageStr);

        }

        private void handleIncomingVideoCall(final IncomingVideoMessageItemHolder holder, final Message message) {

            boolean btnsVisible = false;
            String messageStr = "";

            switch (message.getMessageType()) {
                case Message.Type.INCOMING_CALL:
                    // incoming video call request
                    if (message.isMessageActive()) {
                        // message is active
                        btnsVisible = true;
                        messageStr = getString(R.string.video_chat_call_recieved, otherUserName);
                        holder.accept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AVChat.getAVChatInstance(context).acceptAVChatRequest(String.valueOf(otherUserID), new Callbacks() {
                                    @Override
                                    public void successCallback(JSONObject response) {
                                        message.setMessage(context.getString(R.string.you_accepted_video_call));
                                        holder.accept.setVisibility(View.GONE);
                                        holder.reject.setVisibility(View.GONE);

                                        holder.name.setText(message.getMessage());

                                        message.setMessageInactive();

                                        // open video activity
                                        Intent intent = new Intent(getActivity(), AVChatActivity.class);
                                        intent.putExtra("userID", String.valueOf(otherUserID));
                                        startActivity(intent);
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
                                AVChat.getAVChatInstance(context).rejectAVChatRequest("", String.valueOf(otherUserID), new Callbacks() {
                                    @Override
                                    public void successCallback(JSONObject response) {
                                        message.setMessage(context.getString(R.string.you_rejected_video_call));
                                        holder.accept.setVisibility(View.GONE);
                                        holder.reject.setVisibility(View.GONE);
                                        holder.name.setText(message.getMessage());

                                        itemVideo.setEnabled(true);
                                        message.setMessageInactive();
                                    }

                                    @Override
                                    public void failCallback(JSONObject response) {
                                        holder.accept.setVisibility(View.GONE);
                                        holder.reject.setVisibility(View.GONE);
                                        holder.name.setText(context.getString(R.string.error_video_call));

                                        itemVideo.setEnabled(true);
                                        message.setMessageInactive();
                                    }
                                });
                            }
                        });
                    } else {
                        if (message.isMessageFetched())
                            messageStr = context.getString(R.string.video_chat_call_recieved_fetched, otherUserName);
                        else
                            // message is inactive
                            messageStr = message.getMessage();
                    }
                    break;

                case Message.Type.OUTGOING_BUSY_TONE:
                    // you're having a video call and another user requests video call
                    messageStr = context.getString(R.string.user_sent_you_video_call);
                    break;

                case Message.Type.CANCEL_CALL:
                    messageStr = message.getMessage();
                    break;

            }

            if (btnsVisible) {
                holder.accept.setVisibility(View.VISIBLE);
                holder.reject.setVisibility(View.VISIBLE);
            } else {
                holder.accept.setVisibility(View.GONE);
                holder.reject.setVisibility(View.GONE);

            }

            holder.name.setText(messageStr);
        }

        private class MessageItemHolder {
            EmojiTextView message;
            TextView dateTime;
            CircleImageView profilePicture;
        }

        private class IncomingVideoMessageItemHolder {
            TextView name, dateTime;
            Button accept, reject;
        }

        private class OutgoingVideoMessageItemHolder {
            TextView header;
            Button cancel;
        }

        private class ClickListener implements View.OnClickListener {
            // holder for the related datetime text view
            private TextView dateTime;

            public ClickListener(TextView dateTime) {
                this.dateTime = dateTime;
            }

            public void onClick(View v) {
                // show\hide text view according to user clicks
                if (dateTime.getVisibility() == View.VISIBLE)
                    dateTime.setVisibility(View.GONE);
                else {
                    dateTime.setVisibility(View.VISIBLE);

                    if (!listView.canScrollVertically(listView.getBottom()))
                        // scroll to the latest message sent
                        listView.setSelection(listView.getAdapter().getCount() - 1);
                }
            }
        }
    }


}
