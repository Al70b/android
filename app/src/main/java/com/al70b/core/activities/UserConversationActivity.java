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

    public static final String OTHER_USER = "OTHER_USER";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                Log.d(TAG, "Video request for a non friend user is clicked");
                Toast.makeText(UserConversationActivity.this,
                        getString(R.string.this_feature_is_allowed_with_friends_only), Toast.LENGTH_SHORT).show();

                return true;
            case R.id.menu_item_user_conversations_clear_history:
                Log.d(TAG, "Clear history was clicked for a non friend user");
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
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    void sendMessage(String messageText, final MyCallback callback) {
        //              callback.onSuccess(jsonObject);
           // callback.onFail(jsonObject);

    }

    @Override
    void getHistory(long idOfLastMessage, int messagesFetchedLimit, MyCallback callback) {
        long otherUserID = (long) otherUser.getUserID();
        List<Message> list = new ArrayList<>();

    }


}
