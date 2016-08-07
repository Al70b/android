package com.al70b.core.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.pull_load_listview.LoadMoreListView;
import com.al70b.core.fragments.Items.ConversationItem;
import com.al70b.core.misc.StorageOperations;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.EndMessage;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.al70b.core.server_methods.ServerConstants;
import com.bumptech.glide.Glide;
import com.inscripts.cometchat.sdk.CometChat;
import com.inscripts.interfaces.Callbacks;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Naseem on 5/10/2015.
 */
public class UserConversationsFragment extends Fragment {

    // tag for log & back button functionality
    public static final String INTERNAL_CONVERSATION_TAG = "InternalConversation";
    // number of conversations to load per page
    private static final int CONVERSATION_PER_PAGE = 10;
    // list of conversation items
    private static List<ConversationItem> listOfItems;
    // Current user
    private CurrentUser user;
    private BroadcastReceiver receiver;
    // List widget showing the conversations
    private LoadMoreListView listView;
    private boolean listAlreadyCreated;

    private boolean isInitialized, serverError;

    private int pagesCounter = 1;

    private Handler handler;

    private RequestsInterface requestsInterface;
    private ServerResponse<ConversationItem[]> sr;

    private LinearLayout layoutLoading, layoutLoadingFailed;
    private TextView textViewFailed;

    private ConversationsListAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = ((MyApplication)getActivity().getApplication()).getCurrentUser();

        requestsInterface = new RequestsInterface(getActivity().getBaseContext());

        if (savedInstanceState != null) {
            listAlreadyCreated = savedInstanceState.getBoolean("listAlreadyCreated");
        }

        if (!listAlreadyCreated) {
            listOfItems = new ArrayList<>();
            listAlreadyCreated = true;
        }

        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_messages, container, false);

        // get the list view from xml
        listView = (LoadMoreListView) viewGroup.findViewById(R.id.list_view_user_conversations);
        layoutLoading = (LinearLayout) viewGroup.findViewById(R.id.layout_load_conversations);
        layoutLoadingFailed = (LinearLayout) viewGroup.findViewById(R.id.layout_failed_loading_conversations);
        textViewFailed = (TextView) viewGroup.findViewById(R.id.text_view_user_conversations_message);

        adapter = new ConversationsListAdapter(getActivity(),
                R.layout.list_item_conversation, listOfItems);
        listView.setAdapter(adapter);

        // set a listener to be invoked when the list reaches the end
        listView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            public void onLoadMore() {
                // Do the work to load more items at the end of list here
                new LoadDataTask().execute();
            }
        });

        layoutLoadingFailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutLoadingFailed.setVisibility(View.GONE);
                layoutLoading.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                onStart();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ConversationItem ci = (ConversationItem) listView.getAdapter().getItem(i);

                final int id = ci.userID;
                String name = ci.name;

                /*Fragment f = new UserConversationsInternalFragment();

                Bundle bundle = new Bundle();
                bundle.putInt("ID", id);
                bundle.putString("Name", name);
                bundle.putString("Bitmap", ci.profilePicture);
                f.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, f, INTERNAL_CONVERSATION_TAG)
                                //.addToBackStack("s")
                        .commit();


                // mark message as read
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RequestsInterface rq = new RequestsInterface(getActivity());

                        try {
                            ServerResponse<String> sr = rq.markMessageAsRead(user, id);

                        } catch (ServerResponseFailedException ex) {

                        }
                    }
                }).start();*/
            }

        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // get sender id
                final int senderID = intent.getIntExtra("user_id", 0);
                final EndMessage message = (EndMessage) intent.getSerializableExtra("message");


                /* TODO handle when a video call is sent */

                for (ConversationItem item : listOfItems) {
                    if (item.userID == senderID) {
                        listOfItems.remove(item);

                        item.lastMessage = message.getMessage();
                        item.lastMessageDate = message.getDateTimeString();

                        // yellow color for newly received message
                        item.highlight = true;
                        listOfItems.add(0, item);

                        adapter.notifyDataSetChanged();
                        // done handling
                        return;
                    }
                }

                // no previous conversation was found
                CometChat.getInstance(getActivity().getApplicationContext(),
                        ServerConstants.CONSTANTS.COMET_CHAT_API_KEY)
                        .getUserInfo(String.valueOf(senderID), new Callbacks() {
                    @Override
                    public void successCallback(JSONObject jsonObject) {

                        String name, photo;

                        try {
                            name = jsonObject.getString("n");
                            photo = jsonObject.getString("a");
                            ConversationItem item = new ConversationItem(message.getMessageID(), senderID,
                                    name, message.getMessage(), message.getDateTimeString(), photo);

                            // yellow color for newly received message
                            item.highlight = true;
                            listOfItems.add(0, item);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException ex) {

                        }
                    }

                    @Override
                    public void failCallback(JSONObject jsonObject) {

                    }
                });


                // Toast.makeText(getActivity(), "Conversations message received", Toast.LENGTH_SHORT).show();
            }
        };
        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

        getActivity().registerReceiver(receiver, new IntentFilter("NEW_SINGLE_MESSAGE"));
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (!listAlreadyCreated || (listOfItems != null && listOfItems.size() == 0)) {
                    init();     // need to initialize
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listOfItems.size() == 0) {
                            // show the list view and hide others
                            textViewFailed.setText(getString(R.string.empty_conversations));
                            layoutLoadingFailed.setVisibility(View.VISIBLE);
                            layoutLoading.setVisibility(View.GONE);
                            listView.setVisibility(View.GONE);
                        } else {
                            // show the list view and hide others
                            layoutLoadingFailed.setVisibility(View.GONE);
                            layoutLoading.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }).start();
    }

    public void onStop() {
        super.onStop();

        getActivity().unregisterReceiver(receiver);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("listAlreadyCreated", listAlreadyCreated);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        listOfItems = null;
    }

    private void init() {
        try {
            // get first page
            sr = requestsInterface.getConversations(user, (pagesCounter = 1), CONVERSATION_PER_PAGE);

            if (sr.isSuccess()) {       // if request returned successfully
                // next time take second page and so on
                pagesCounter++;

                // add items to list of items
                listOfItems.addAll(0, Arrays.asList(sr.getResult()));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (ServerResponseFailedException ex) {
            handler.post(new Runnable() {
                public void run() {
                    // something went wrong with the connection
                    layoutLoadingFailed.setVisibility(View.VISIBLE);
                    textViewFailed.setText(getString(R.string.failed_loading_conversations));
                    layoutLoading.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);

                    serverError = true;
                }
            });
        }
    }

    // this function is called always with a new thread
    private void loadMoreConversations() {
        try {
            sr = requestsInterface.getConversations(user, pagesCounter, CONVERSATION_PER_PAGE);

            if (sr.isSuccess()) {       // if request returned successfully
                pagesCounter++;
                listOfItems.addAll(0, Arrays.asList(sr.getResult()));

                handler.post(new Runnable() {
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (ServerResponseFailedException ex) {
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(), "Could not connect to the server and load more messages", Toast.LENGTH_SHORT).show();

                    serverError = true;
                }
            });
        }
    }

    public class ConversationsListAdapter extends ArrayAdapter<ConversationItem> {

        Context context;
        int layout;
        List<ConversationItem> data;
        StorageOperations so;

        public ConversationsListAdapter(Context context, int layout, List<ConversationItem> data) {
            super(context, layout, data);
            this.context = context;
            this.layout = layout;
            this.data = data;
            this.so = new StorageOperations(context);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            ConversationItemHolder holder;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layout, parent, false);

                holder = new ConversationItemHolder();
                holder.profilePicture = (CircleImageView) row.findViewById(R.id.circle_image_list_item_messages_profile_picture);
                holder.name = (TextView) row.findViewById(R.id.text_view_list_item_messages_name);
                holder.lastMessage = (TextView) row.findViewById(R.id.text_view_list_item_messages_last);
                holder.lastMessageDate = (TextView) row.findViewById(R.id.text_view_list_item_messages_date);

                row.setTag(holder);
            } else {
                holder = (ConversationItemHolder) row.getTag();
            }

            ConversationItem conItem = data.get(position);


            // set background color to highlighted
            if (conItem.highlight)
                row.setBackgroundColor(getResources().getColor(R.color.highlighted_yellow));
            else
                row.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            holder.name.setText(conItem.name);
            holder.lastMessage.setText(conItem.lastMessage);
            holder.lastMessageDate.setText(conItem.lastMessageDate);

            if (conItem.profilePicture != null && conItem.profilePicture.endsWith("default-user-photo.png"))
                conItem.profilePicture = null;

            Glide.with(context)
                    .load(conItem.profilePicture)
                    .asBitmap()
                    .fitCenter()
                    .centerCrop()
                    .placeholder(R.drawable.avatar)
                    .override(110, 110)
                    .into(holder.profilePicture);


            return row;
        }

        class ConversationItemHolder {
            TextView name, lastMessage, lastMessageDate;
            CircleImageView profilePicture;

        }
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            if (isCancelled()) {
                return null;
            }

            // load more conversations
            loadMoreConversations();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!serverError) {

                // We need notify the adapter that the data have been changed
                ((BaseAdapter) ((HeaderViewListAdapter) listView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();

                // Call onLoadMoreComplete when the LoadMore task, has finished
                listView.onLoadMoreComplete();
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            if (!serverError)
                // Notify the loading more operation has finished
                listView.onLoadMoreComplete();
        }
    }


}
