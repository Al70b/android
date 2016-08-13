package com.al70b.core.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.adapters.MessagesListAdapter;
import com.al70b.core.extended_widgets.pull_load_listview.PullToRefreshListView;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.Message;
import com.al70b.core.objects.OtherUser;
import com.bumptech.glide.Glide;
import com.inscripts.Keyboards.SmileyKeyBoard;
import com.inscripts.interfaces.EmojiClickInterface;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 7/27/2016.
 */
public abstract class AbstractUserConversationActivity extends FragmentActivity
        implements EmojiClickInterface {

    private static final String TAG = "AbstractUserCon";
    private static final int NUMBER_OF_FETCHED_MESSAGES_EACH_TIME = 10;
    public static final String OTHER_USER = "OTHER_USER";
    public static final String CURRENT_USER = "CURRENT_USER";
    private static final int PROFILE_VISIT_RESULT = 135;

    // the current loggedin user
    protected CurrentUser currentUser;

    // the other user chatting with
    protected OtherUser otherUser;

    // list of messages to show in the listview
    protected ArrayList<Message> mListMessages;

    // list view to hold the messages and its adapter
    protected PullToRefreshListView pulledListView;
    protected MessagesListAdapter messagesListAdapter;
    protected ImageButton emojiButton;

    private SmileyKeyBoard smileyKeyBoard;

    private MenuItem itemVideo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abs_user_conversation);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            Log.e(TAG, "No values were passed to the activity");
            return;
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        currentUser = (CurrentUser) bundle.getSerializable(CURRENT_USER);
        assert currentUser != null : "Current user passed to activity cannot be null";

        otherUser = (OtherUser) bundle.getSerializable(OTHER_USER);
        assert otherUser != null : "Other user passed to activity cannot be null!";

        inflateActionBar();

        pulledListView = (PullToRefreshListView) findViewById(R.id.listview_user_messages_conversation);
        final EditText etMessage = (EditText) findViewById(R.id.et_user_messages_message);
        final ImageButton ibSend = (ImageButton) findViewById(R.id.image_button_user_messages_send);
        emojiButton = (ImageButton) findViewById(R.id.image_button_user_messages_emoji);

        smileyKeyBoard = new SmileyKeyBoard();
        smileyKeyBoard.enable(this, this, R.id.layout_for_emoticons, etMessage);
        final LinearLayout chatFooter = (LinearLayout) findViewById(R.id.layout_bottom_area);
        smileyKeyBoard.checkKeyboardHeight(chatFooter);
        smileyKeyBoard.enableFooterView(etMessage);

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SmileyKeyBoard.isKeyboardVisibile()) {
                    SmileyKeyBoard.dismissKeyboard();
                    emojiButton.setImageResource(R.drawable.ic_action_emo_basic);

                    final InputMethodManager inputMethodManager =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    smileyKeyBoard.showKeyboard(chatFooter);
                    emojiButton.setImageResource(R.drawable.ic_action_keyboard);
                }
            }
        });

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
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    // scroll to the latest message sent
                    pulledListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //pulledListView.setSelection(pulledListView.getAdapter().getCount() - 1);
                            pulledListView.smoothScrollToPosition(pulledListView.getAdapter().getCount() - 1);
                        }
                    }, 800);
                }
            }
        });

        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = etMessage.getText().toString();
                long date = System.currentTimeMillis() / 1000;

                final Message msg = new Message(-1, messageText, date, Message.Type.REGULAR);
                msg.status = Message.Status.SENDING;
                mListMessages.add(msg);
                messagesListAdapter.notifyDataSetChanged();

                sendMessage(etMessage.getText().toString(), new MyCallback() {
                    @Override
                    void onSuccess(JSONObject result) {
                        msg.status = Message.Status.SENT;
                        messagesListAdapter.notifyDataSetChanged();

                        if (pulledListView.canScrollVertically(pulledListView.getBottom())) {
                            // scroll to the latest message sent
                            pulledListView.setSelection(mListMessages.size() - 1);
                        }
                    }

                    @Override
                    void onFail(JSONObject result) {
                        msg.status = Message.Status.FAILED_TO_SEND;
                        messagesListAdapter.notifyDataSetChanged();
                    }
                });

                // empty edit text from message
                etMessage.setText("");
            }
        });

        // create list of messages and adapter to bind with the list
        mListMessages = new ArrayList<>();
        messagesListAdapter = new MessagesListAdapter(this,
                currentUser, otherUser, mListMessages);
        pulledListView.setAdapter(messagesListAdapter);

        // Set a listener to be invoked when the list should be refreshed.
        pulledListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                // Do work to refresh the list here.
                new PullToRefreshDataTask().execute();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_conversation, menu);

        itemVideo = menu.findItem(R.id.menu_item_user_conversation_video);

        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    public void enableVideoChat(boolean flag) {
        if (itemVideo != null) {
            itemVideo.setEnabled(flag);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_item_user_conversation_profile:
                Intent intent = new Intent(this, MemberProfileActivity.class);
                intent.putExtra(MemberProfileActivity.CURRENT_USER, currentUser);
                intent.putExtra(MemberProfileActivity.OTHER_USER, otherUser);
                startActivityForResult(intent, PROFILE_VISIT_RESULT);
                return true;
            case R.id.menu_item_user_conversation_video:
                // Specific implementation depending on other user friend status
                return false;
            case R.id.menu_item_user_conversations_clear_history:
                // Specific implementation depending on other user friend status
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void inflateActionBar() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.actionbar_conversation, null);

        TextView textViewTitle = (TextView) layout.findViewById(R.id.actionbar_conversation_txtview_title);
        ImageButton imgBtnBack = (ImageButton) layout.findViewById(R.id.actionbar_conversation_imgbtn_back);
        CircleImageView circleImageProfilePicture = (CircleImageView) layout.findViewById(R.id.actionbar_conversation_circleimg_profile);

        textViewTitle.setText(otherUser.getName());

        circleImageProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!otherUser.isProfilePictureSet()) {
                    return;
                }

                Intent intent = new Intent(AbstractUserConversationActivity.this, DisplayPictureActivity.class);
                intent.putExtra(DisplayPictureActivity.THUMBNAIL_KEY, otherUser.getProfilePictureThumbnailPath());
                intent.putExtra(DisplayPictureActivity.FULL_PICTURE_KEY, otherUser.getProfilePicturePath());
                startActivity(intent);
            }
        });

        imgBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ActionBar actionBar = getActionBar();

        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(layout);
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Build.VERSION.SDK_INT > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "back button was pressed");
        if (SmileyKeyBoard.isKeyboardVisibile()) {
            SmileyKeyBoard.dismissKeyboard();
            emojiButton.setImageResource(R.drawable.ic_action_emo_basic);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void getClickedEmoji(int i) {
        smileyKeyBoard.getClickedEmoji(i);
    }

    @Override
    public void onStart() {
        super.onStart();

        // start fetching history
        pulledListView.onRefresh();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    /**
     * this class implements the "load more messages" functionality
     */
    private class PullToRefreshDataTask extends AsyncTask<Void, Void, List<Message>> {

        @Override
        protected void onPreExecute() {
            if (mListMessages.isEmpty()) {
                Log.d(TAG, "No messages at first, fetch with loading view");
            } else {
                Log.d(TAG, "There are messages already, list view loading");
            }
        }

        @Override
        protected List<Message> doInBackground(Void... params) {
            Log.d(TAG, "Fetching older messages");

            if (isCancelled()) {
                return null;
            }

            List<Message> fetchedMessages = getHistory(NUMBER_OF_FETCHED_MESSAGES_EACH_TIME);
            Log.d(TAG, String.format("Fetched %1$s new messages", String.valueOf(fetchedMessages.size())));

            mListMessages.addAll(fetchedMessages);

            return fetchedMessages;
        }

        @Override
        protected void onPostExecute(List<Message> result) {
            if (result.isEmpty()) {
                pulledListView.setNoMore(true);
            } else {
                messagesListAdapter.notifyDataSetChanged();
                pulledListView.setSelection(messagesListAdapter.getCount() - 1);
                pulledListView.onRefreshComplete();
            }

            super.onPostExecute(result);
        }
    }


    ////////////////  Callback Interface ////////////////

    public abstract class MyCallback {
        abstract void onSuccess(JSONObject result);

        abstract void onFail(JSONObject result);
    }

    ////////////////   Abstract Methods   ////////////////

    /**
     * Send message to the server\chat depending on the implementation
     *
     * @param messageText message to send
     * @param callback    the callback to call on method finish
     */
    abstract void sendMessage(String messageText, MyCallback callback);

    /**
     * Fetch more messages from history
     *
     * @return List of newly fetched messages if exist
     */
    abstract List<Message> getHistory(int messagesFetchedLimit);
}
