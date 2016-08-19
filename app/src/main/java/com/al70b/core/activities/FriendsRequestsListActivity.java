package com.al70b.core.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.adapters.FriendsRequestsAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.pull_load_listview.LoadMoreListView;
import com.al70b.core.misc.AppConstants;
import com.al70b.core.misc.Enums.FriendRequestAction;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 5/28/2016.
 */
public class FriendsRequestsListActivity extends Activity {

    public static final String NUMBER_OF_FRIENDS_REQUESTS = "com.FriendsRequestsActivity.NumbersOfFriendsRequests";
    private static final String TAG = "FriendsRequestsActivity";
    private static final int RESULTS_PER_PAGE = 10;

    // used for when a user profile is visited and need to get friend status
    private static final int PROFILE_VISIT_RESULT = 1;

    private CurrentUser currentUser;

    // #page from server
    private int page = 1;

    private List<OtherUser> listOfFriendRequests;
    private FriendsRequestsAdapter friendsRequestsAdapter;

    private LinearLayout layoutLoading;
    private LoadMoreListView loadMoreListView;
    private LinearLayout layoutFailedToLoad;

    private int numOfFriendsRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_requests_list);

        // get intent
        Intent intent = getIntent();

        if(intent == null) {
            Log.d(TAG, "Friends list activity did not get an intent");
            finish();
            return;
        }

        numOfFriendsRequests = intent.getIntExtra(NUMBER_OF_FRIENDS_REQUESTS, -1);

        currentUser =  (CurrentUser)intent.getSerializableExtra(AppConstants.CURRENT_USER);

        // relate widgets
        loadMoreListView = (LoadMoreListView) findViewById(R.id.list_view_friendsRequestsA_requests);
        TextView tvEmptyList = (TextView) findViewById(R.id.tv_friendsRequestsA_empty_list);
        layoutLoading = (LinearLayout) findViewById(R.id.layout_friendsRequestsA_loading);
        layoutFailedToLoad = (LinearLayout) findViewById(R.id.layout_friendsRequestsA_failed_loading);

        loadMoreListView.setEmptyView(tvEmptyList);
        listOfFriendRequests = new ArrayList<OtherUser>();
        friendsRequestsAdapter = new FriendsRequestsAdapter(this, R.layout.list_item_friend_request,
                listOfFriendRequests, currentUser, new OnFriendRequestAction() {
            @Override
            public void callback(FriendRequestAction action) {
                switch(action) {
                    case ACCEPTED:
                    case REJECTED:
                        numOfFriendsRequests -= 1;
                        break;
                }

                updateTitle();
            }
        });
        loadMoreListView.setAdapter(friendsRequestsAdapter);
        loadMoreListView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            public void onLoadMore() {
                // Do the work to load more items at the end of list here
                new LoadMoreFriendsRequestsTask().execute();
            }
        });

        layoutFailedToLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadMoreFriendsRequestsTask().execute();
            }
        });


        loadMoreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OtherUser otherUser = (OtherUser) parent.getItemAtPosition(position);
                Intent intent = new Intent(FriendsRequestsListActivity.this, MemberProfileActivity.class);

                intent.putExtra(MemberProfileActivity.CURRENT_USER, currentUser);
                intent.putExtra(MemberProfileActivity.OTHER_USER, otherUser);
                intent.putExtra(MemberProfileActivity.POSITION, position);
                startActivityForResult(intent, PROFILE_VISIT_RESULT);
            }
        });

        // update title with number of friends requests
        updateTitle();
    }


    @Override
    public void onStart() {
        super.onStart();

        new LoadMoreFriendsRequestsTask().execute();
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
        getMenuInflater().inflate(R.menu.menu_friends_requests_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_friends_requests_list_close) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public interface OnFriendRequestAction {
        void callback(FriendRequestAction action);
    }

    private void updateTitle() {
        String title = getString(R.string.friends_requests_str);

        if (numOfFriendsRequests > 0 && numOfFriendsRequests < 1000) {
            title = title.concat("  (" + numOfFriendsRequests + ")");
        } else if (numOfFriendsRequests < 0) {
            title = title.concat("  (...)");
        }

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    private class LoadMoreFriendsRequestsTask extends AsyncTask<Void, Void, Pair<Boolean, String>> {

        private boolean isNoMoreResult;

        public LoadMoreFriendsRequestsTask() {
        }

        @Override
        protected void onPreExecute() {
            if(listOfFriendRequests.isEmpty()) {
                loadMoreListView.setVisibility(View.GONE);
                layoutFailedToLoad.setVisibility(View.GONE);
                layoutLoading.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Pair<Boolean, String> doInBackground(Void... params) {
            if (isCancelled() || isNoMoreResult) {
                return null;
            }

            Pair<Boolean,String> result = new Pair<Boolean,String>();

            try {

                ServerResponse<Pair<Boolean, List<OtherUser>>> sr =
                    new RequestsInterface(getApplicationContext())
                            .getUserPendingReceivedRequests(currentUser, page, RESULTS_PER_PAGE, null);

                // if request returned successfully
                if (sr.isSuccess()) {
                    // next time take second page and so on
                    page++;

                    if (sr.getResult().second != null && sr.getResult().second.size() > 0) {
                        // add items to list of items
                        listOfFriendRequests.addAll(sr.getResult().second);

                        if (sr.getResult().first) {
                            // no more
                            isNoMoreResult = true;
                        }
                    }
                    result.set(true, "Succeeded");
                } else {
                    result.set(false, sr.getErrorMsg());
                }
            } catch (ServerResponseFailedException ex) {
                result.set(false, ex.toString());
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
                result.set(false, ex.toString());
            }

            return result;
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> result) {
            if(result == null) {
                return;
            }

            // result is not null, check if request succeeded
            if (result.first) {
                loadMoreListView.setVisibility(View.VISIBLE);
                layoutLoading.setVisibility(View.GONE);

                if(layoutFailedToLoad.getVisibility() == View.VISIBLE) {
                    layoutFailedToLoad.setVisibility(View.GONE);
                }

                // We need notify the membersListAdapter that the data have been changed
                friendsRequestsAdapter.notifyDataSetChanged();

                loadMoreListView.setNoMore(isNoMoreResult);
                loadMoreListView.onLoadMoreComplete();
            } else {
                // failed to fulfill request

                if (listOfFriendRequests.size() == 0) {
                    // something went wrong with the connection
                    layoutFailedToLoad.setVisibility(View.VISIBLE);
                    layoutLoading.setVisibility(View.GONE);
                    ((TextView)layoutFailedToLoad.findViewById(R.id.text_view_friendsRequestsA_failed))
                            .setText(result.second);
                    loadMoreListView.setVisibility(View.GONE);
                } else {
                    // there is data in the list so just show a toast message
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_server_connection_falied),
                            Toast.LENGTH_SHORT).show();
                }
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            // Notify the loading more operation has finished
            loadMoreListView.onLoadMoreComplete();
        }
    }



}
