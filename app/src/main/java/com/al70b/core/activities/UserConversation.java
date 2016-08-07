package com.al70b.core.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.MyApplication;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 5/25/2015.
 */
public class UserConversation extends ListActivity {

    String name;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new MyMessagesListAdapter(this, loadLastMessages()));

        name = getIntent().getExtras().getString("Name");

        setTitle("محادثه مع " + name);
    }


    @Override
    public void onStart() {
        super.onStart();

        ListView listView = getListView();

        // hide divider from list view
        listView.setDivider(null);
        listView.setDividerHeight(0);

        // items clicking is disabled
        listView.setClickable(false);
        listView.setEnabled(false);

        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onStop() {
        super.onStop();

        ((MyApplication) getApplication()).setAppInvisible();
    }

    public MyMessage[] loadLastMessages() {
        MyMessage[] messagesArr = new MyMessage[6];

        messagesArr[0] = new MyMessage("Waseem", "Hello there", "10.10.2015", R.drawable.feather_edittexth, false);
        messagesArr[1] = new MyMessage("Naseem", "Hey waseem", "11.10.2015", R.drawable.add_your_picture, true);
        messagesArr[2] = new MyMessage("Naseem", "How are you doing, it has been a long time since we've talked. tell me please about your life, how is it goin, is your mom still cooking that delicious thai food? I would die for another plate like the one we had on christmas, say hi to your mom and the rest of the family", "12.10.2015", R.drawable.add_your_picture, true);
        messagesArr[3] = new MyMessage("Waseem", "I am fine.", "13.10.2015", R.drawable.feather_edittexth, false);
        messagesArr[4] = new MyMessage("Waseem", "How are you doing?", "14.10.2015", R.drawable.feather_edittexth, false);
        messagesArr[5] = new MyMessage("Naseem", "I am fine too", "15.10.2015", R.drawable.add_your_picture, true);

        return messagesArr;
    }

    public class MyMessagesListAdapter extends ArrayAdapter<MyMessage> {
        Context context;
        MyMessage[] data;
        MyMessageItemHolder holder;

        public MyMessagesListAdapter(Context context, MyMessage[] data) {
            super(context, 0, data);
            this.context = context;
            this.data = data;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;

            MyMessage myMessage = data[position];
            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();

                // create new holder
                holder = new MyMessageItemHolder();

                // inflate the right layout depending on the message source
                if (myMessage.isUserMessage()) {
                    row = inflater.inflate(R.layout.list_item_conversation_content_user, parent, false);
                    holder.message = (TextView) row.findViewById(R.id.emoji_tv_list_item_messages_message_user);
                    holder.dateTime = (TextView) row.findViewById(R.id.text_view_list_item_messages_date_user);
                } else {
                    row = inflater.inflate(R.layout.list_item_conversation_content_member, parent, false);
                    holder.profilePicture = (CircleImageView) row.findViewById(R.id.circle_image_list_item_messages_profile_picture_member);
                    holder.message = (TextView) row.findViewById(R.id.emoji_tv_list_item_messages_last_member);
                    holder.dateTime = (TextView) row.findViewById(R.id.text_view_list_item_messages_date_member);
                }

                // add some functionality to the widgets
                holder.message.setOnClickListener(new ClickListener(holder.dateTime));

                row.setTag(holder);
            } else {
                holder = (MyMessageItemHolder) row.getTag();
            }

            if (!myMessage.isUserMessage()) {
                holder.profilePicture.setImageResource(myMessage.profilePicture);
            }
            holder.message.setText(myMessage.message);
            holder.dateTime.setText(myMessage.dateTime);

            return row;
        }

        class MyMessageItemHolder {
            TextView message, dateTime;
            CircleImageView profilePicture;

        }

    }

    public class MyMessage {
        boolean userMessage;
        String name, message, dateTime;
        int profilePicture;

        public MyMessage(String name, String message, String dateTime, int profilePicture, boolean userMessage) {
            this.name = name;
            this.message = message;
            this.dateTime = dateTime;
            this.profilePicture = profilePicture;
            this.userMessage = userMessage;
        }

        public boolean isUserMessage() {
            return userMessage;
        }
    }

    private class ClickListener implements View.OnClickListener {

        TextView dateTime;

        public ClickListener(TextView dateTime) {
            this.dateTime = dateTime;
        }

        public void onClick(View v) {
            if (dateTime.getVisibility() == View.VISIBLE)
                dateTime.setVisibility(View.GONE);
            else
                dateTime.setVisibility(View.VISIBLE);
        }
    }
}
