package com.al70b.core.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.adapters.FriendsListAdapter;
import com.al70b.core.adapters.FriendsRequestsAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.pull_load_listview.LoadMoreListView;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListActivity extends Activity {

    public static final String NUMBER_OF_FRIENDS_REQUESTS = "com.MembersListActivity.NumbersOfFriendsRequests";
    private static final int RESULTS_PER_PAGE = 10;
    private static final int PROFILE_VISIT_RESULT = 1;
    private static final String TAG = "FriendsListActivity";

    // this activity
    private static FriendsListActivity thisActivity;

    private RequestsInterface requestsInterface;

    // current page loading (for friends list)
    private int page = 1;

    // this current user
    private CurrentUser currentUser;

    // list of users who sent friend requests
    private List<OtherUser> listOfFriendRequests;

    // list of current user's friends
    private List<OtherUser> listOfFriends;
    private FriendsListAdapter friendsListAdapter;

    private boolean serverError, noMore;

    // widgets
    private LoadMoreListView listViewFriends;
    private ListView listViewFriendsRequests;
    private LinearLayout layoutLoadingFailed;
    private TextView textViewLoadingFailed;
    private TextView tvNoFriendsRequests;
    private TextView tvNoFriends;

    private TextView tvFriendsRequestTitle;
    private TextView tvFriendsTitle;
    private FriendsRequestsAdapter friendsRequestsAdapter;
    private LinearLayout layoutFriendsRequestsLoading;
    private int numOfFriendsRequests;
    private boolean collapsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        thisActivity = this;
        listOfFriendRequests = new ArrayList<OtherUser>();
        listOfFriends = new ArrayList<OtherUser>();

        requestsInterface = new RequestsInterface(thisActivity);

        // get intent
        Intent intent = getIntent();

        if (intent != null) {
            numOfFriendsRequests = getIntent().getIntExtra(NUMBER_OF_FRIENDS_REQUESTS, -1);
        } else {
            Log.d(TAG, "Friends list activity did not get an intent");
            this.finish();
        }

        currentUser = ((MyApplication)getApplication()).getCurrentUser();

        // relate widgets
        listViewFriends = (LoadMoreListView) findViewById(R.id.list_view_friends_activity_friends);
        listViewFriendsRequests = (ListView) findViewById(R.id.list_view_friends_requests_list);
        layoutLoadingFailed = (LinearLayout) findViewById(R.id.layout_friendsA_failed_loading);
        layoutFriendsRequestsLoading = (LinearLayout) findViewById(R.id.layout_friendsA_requests_failed_loading);
        textViewLoadingFailed = (TextView) findViewById(R.id.text_view_friendsA_failed);
        tvNoFriendsRequests = (TextView) findViewById(R.id.tv_friends_no_friends_requests_were_found);
        tvNoFriends = (TextView) findViewById(R.id.tv_friends_no_friends_were_found);
        tvFriendsRequestTitle = (TextView) findViewById(R.id.tv_friends_list_of_friends_requests);
        tvFriendsTitle = (TextView) findViewById(R.id.tv_friends_list_of_friends);
        final ImageButton btnExpand = (ImageButton) findViewById(R.id.img_btn_friends_collapse);

        btnExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!collapsed) {
                    // collapse it
                    listViewFriendsRequests.setVisibility(View.GONE);
                    btnExpand.setImageResource(R.drawable.ic_action_expand);
                    tvNoFriendsRequests.setVisibility(View.GONE);
                } else {
                    // expand it
                    listViewFriendsRequests.setVisibility(View.VISIBLE);
                    btnExpand.setImageResource(R.drawable.ic_action_collapse);

                    if (listOfFriendRequests.size() == 0)
                        tvNoFriendsRequests.setVisibility(View.VISIBLE);
                }
                collapsed = !collapsed;
            }
        });

        btnExpand.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int strID;
                if (!collapsed) {
                    strID = R.string.collapse_friends;
                } else {
                    strID = R.string.expand_friends;
                }
                Toast.makeText(thisActivity, getString(strID), Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        // FRIENDS LIST

        // set empty view when list is empty
        listViewFriends.setEmptyView(tvNoFriends);


        // create list adapter
        friendsListAdapter = new FriendsListAdapter(thisActivity, R.layout.list_view_item_friend, listOfFriends);

        // set adapter for list view
        listViewFriends.setAdapter(friendsListAdapter);

        // force hiding empty view at first loading
        tvNoFriends.setVisibility(View.GONE);

        // set a listener to be invoked when the list reaches the end
        listViewFriends.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            public void onLoadMore() {
                // Do the work to load more items at the end of list here
                new LoadFriendsAsyncTask().execute();
            }
        });

        layoutLoadingFailed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutLoadingFailed.setVisibility(View.GONE);

                getFriendsListFromServer();
            }
        });

        listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(thisActivity, "Item was clicked", Toast.LENGTH_SHORT).show();
                OtherUser otherUser = (OtherUser) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(thisActivity, MemberProfileActivity.class);

                intent.putExtra(MemberProfileActivity.CURRENT_USER, currentUser);
                intent.putExtra(MemberProfileActivity.OTHER_USER, otherUser);
                intent.putExtra(MemberProfileActivity.POSITION, i);
                startActivityForResult(intent, PROFILE_VISIT_RESULT);
            }
        });


        // FRIENDS REQUESTS LIST

        updateFriendsRequestsCounter();

        // set empty view when  list is empty
        listViewFriendsRequests.setEmptyView(tvNoFriendsRequests);

        // create list adapter
        friendsRequestsAdapter = new FriendsRequestsAdapter(thisActivity, R.layout.list_item_friend_request,
                listOfFriendRequests, currentUser);

        // set adapter for list view
        listViewFriendsRequests.setAdapter(friendsRequestsAdapter);

        // force hide at first
        tvNoFriendsRequests.setVisibility(View.GONE);

        loadFriendsRequests();

        listViewFriendsRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(thisActivity, "Item was clicked", Toast.LENGTH_SHORT).show();
            }
        });

        listViewFriendsRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(thisActivity, "Item was clicked", Toast.LENGTH_SHORT).show();
                OtherUser otherUser = (OtherUser) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(thisActivity, MemberProfileActivity.class);

                intent.putExtra(MemberProfileActivity.CURRENT_USER, currentUser);
                intent.putExtra(MemberProfileActivity.OTHER_USER, otherUser);
                intent.putExtra(MemberProfileActivity.POSITION, i);
                startActivityForResult(intent, PROFILE_VISIT_RESULT);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        new LoadFriendsAsyncTask().execute();
        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onStop() {
        super.onStop();

        ((MyApplication) getApplication()).setAppInvisible();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_friends_list_close) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void loadFriendsRequests() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                RequestsInterface ri = new RequestsInterface(getApplicationContext());

                try {
                    ServerResponse<Pair<Boolean, List<OtherUser>>> sr = ri.getUserPendingReceivedRequests(currentUser, page, RESULTS_PER_PAGE, null);

                    if (sr.isSuccess()) {
                        listOfFriendRequests.addAll(sr.getResult().second);


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                friendsRequestsAdapter.notifyDataSetChanged();
                                layoutFriendsRequestsLoading.setVisibility(View.GONE);
                                listViewFriendsRequests.setVisibility(View.VISIBLE);

                                if (numOfFriendsRequests < 0) {
                                    numOfFriendsRequests = friendsRequestsAdapter.getCount();
                                    updateFriendsRequestsCounter();

                                }
                            }
                        });
                    }
                } catch (ServerResponseFailedException ex) {

                }

            }
        }).start();
    }

    private void getFriendsListFromServer() {
        if (noMore)
            return;

        try {
            final ServerResponse<Pair<Boolean, List<OtherUser>>> sr = requestsInterface.getUserFriends(currentUser.getUserID(), currentUser.getAccessToken(), page, RESULTS_PER_PAGE, null);

            if (sr.isSuccess()) {       // if request returned successfully
                // next time take second page and so on
                page++;

                if (sr.getResult().second.size() > 0) {
                    // add items to list of items
                    listOfFriends.addAll(sr.getResult().second);

                    if (sr.getResult().first) {
                        // no more
                        noMore = true;
                        listViewFriends.setNoMore(true);
                    }
                }

                serverError = false;
            } else {
                handleConnectionFailed(sr.getErrorMsg());
            }
        } catch (ServerResponseFailedException ex) {
            handleConnectionFailed(ex.toString());
        }
    }

    private void handleConnectionFailed(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (listOfFriends.size() == 0) {
                    // something went wrong with the connection
                    textViewLoadingFailed.setText(msg);
                    layoutLoadingFailed.setVisibility(View.VISIBLE);
                    listViewFriends.setVisibility(View.GONE);
                } else {
                    // there is data in the list so just show a toast message
                    Toast.makeText(thisActivity, getString(R.string.error_server_connection_falied), Toast.LENGTH_SHORT).show();
                }
                serverError = true;
            }
        });
    }

    private void updateFriendsRequestsCounter() {
        String title = getString(R.string.friends_requests_str);

        if (numOfFriendsRequests > 0)
            title = title.concat("  (" + numOfFriendsRequests + ")");
        else if (numOfFriendsRequests < 0)
            title = title.concat("  (...)");


        tvFriendsRequestTitle.setText(title);
        tvFriendsRequestTitle.invalidate();
    }

    private class LoadFriendsAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            if (isCancelled()) {
                return null;
            }

            // load more of the query result
            getFriendsListFromServer();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!serverError) {

                if (listOfFriends.isEmpty()) {
                    listViewFriends.setVisibility(View.GONE);
                    layoutLoadingFailed.setVisibility(View.GONE);
                    tvNoFriends.setVisibility(View.VISIBLE);
                } else {
                    // We need notify the adapter that the data have been changed
                    friendsListAdapter.notifyDataSetChanged();

                    listViewFriends.setVisibility(View.VISIBLE);
                    layoutLoadingFailed.setVisibility(View.GONE);
                    tvNoFriends.setVisibility(View.GONE);

                    // Call onLoadMoreComplete when the LoadMore task, has finished
                }
                listViewFriends.onLoadMoreComplete();
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            if (!serverError)
                // Notify the loading more operation has finished
                listViewFriends.onLoadMoreComplete();
        }
    }

}
