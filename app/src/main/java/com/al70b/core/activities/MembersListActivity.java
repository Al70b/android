package com.al70b.core.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.adapters.MembersListAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.pull_load_listview.LoadMoreListView;
import com.al70b.core.fragments.UserAdvancedSearchFragment;
import com.al70b.core.fragments.UserBasicSearchFragment;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

import java.util.ArrayList;
import java.util.List;

public class MembersListActivity extends Activity {

    public static final String NUMBER_OF_FRIENDS_REQUESTS = "com.MembersListActivity.NumbersOfFriendsRequests";
    public static final String DATA_SOURCE = "com.al70b.MembersListActivity.DATA_SOURCE";
    private static final int RESULTS_PER_PAGE = 10;
    private static final int PROFILE_VISIT_RESULT = 1;
    private static MembersListActivity thisActivity;

    // current page loading
    private int page = 1;
    // this current user
    private CurrentUser currentUser;
    // list of other users as received from server
    private List<OtherUser> listOfUsers;
    private boolean serverError, noMore;
    private String dataSource;

    // widgets
    private LoadMoreListView listView;
    private LinearLayout layoutLoadingFailed;
    private TextView textViewLoadingFailed, tvEmpty;
    private MembersListAdapter adapter;

    private MethodToCall method;

    public static void updateAdapter(int result, int otherUserID) {
        if (thisActivity != null && thisActivity.dataSource != null
                && thisActivity.dataSource.compareTo("Friends Requests") == 0) {
            // it's showing a friend request
            final MembersListAdapter adapter = thisActivity.adapter;
            final List<OtherUser> list = thisActivity.listOfUsers;
            if (adapter != null && list != null) {

                OtherUser temp = null;
                for (OtherUser user : list) {
                    if (user.getUserID() == otherUserID) {
                        temp = user;
                        break;
                    }
                }

                if (temp != null) {
                    list.remove(temp);

                    thisActivity.listView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();

                            if (list.size() == 0)
                                thisActivity.finish();
                        }
                    }, 2000); // start
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_list);

        thisActivity = this;

        // get intent
        Intent intent = getIntent();

        if (intent != null) {
            dataSource = intent.getStringExtra(MembersListActivity.DATA_SOURCE);
            currentUser = ((MyApplication)getApplication()).getCurrentUser();
        }

        // relate widgets
        listView = (LoadMoreListView) findViewById(R.id.list_view_userMembersA);
        tvEmpty = (TextView) findViewById(R.id.tv_members_list_empty_list);
        layoutLoadingFailed = (LinearLayout) findViewById(R.id.layout_userMembersA_failed_loading);
        textViewLoadingFailed = (TextView) findViewById(R.id.text_view_userMembersA_failed);

        // create list for received users from server
        listOfUsers = new ArrayList<>();

        // create list adapter
        adapter = new MembersListAdapter(thisActivity, R.layout.list_view_item_member, listOfUsers, currentUser);

        // set adapter for list view
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

                getUsersListFromServer();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                OtherUser otherUser = (OtherUser) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(thisActivity, MemberProfileActivity.class);

                intent.putExtra(MemberProfileActivity.CURRENT_USER, currentUser);
                intent.putExtra(MemberProfileActivity.OTHER_USER, otherUser);
                intent.putExtra(MemberProfileActivity.POSITION, i);
                startActivityForResult(intent, PROFILE_VISIT_RESULT);
            }
        });

    }

    private void initMethodToCall(String source) {
        if (source.compareTo(UserBasicSearchFragment.DISPLAY_DATA_TOKEN) == 0) {
            method = new MethodToCall() {
                @Override
                public ServerResponse<Pair<Boolean, List<OtherUser>>> call(Bundle bundle) throws ServerResponseFailedException {
                    return new RequestsInterface(thisActivity).getUsers(currentUser.getUserID(), currentUser.getAccessToken(),
                            bundle.getInt("gender"),
                            bundle.getInt("ageFrom"),
                            bundle.getInt("ageTo"),
                            bundle.getBoolean("picturesOnly"),
                            bundle.getBoolean("onlineOnly"),
                            bundle.getBoolean("closeByOnly"),
                            page, RESULTS_PER_PAGE,
                            new RequestsInterface.ResponseCallback() {
                                @Override
                                public void execute() {
                                    listView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                            listView.invalidateViews();
                                        }
                                    });
                                }

                                @Override
                                public void execute(Object object) {

                                }
                            });
                }
            };
        } else if (source.compareTo(UserAdvancedSearchFragment.DISPLAY_DATA_TOKEN) == 0) {
            method = new MethodToCall() {
                @Override
                public ServerResponse<Pair<Boolean, List<OtherUser>>> call(Bundle bundle) throws ServerResponseFailedException {

                    return new RequestsInterface(thisActivity).getUsersAdvanced(currentUser,
                            bundle.getIntegerArrayList("Gender"),
                            (Integer) bundle.getSerializable("AgeFrom"), (Integer) bundle.getSerializable("AgeTo"),
                            bundle.getString("Country"),
                            (Integer) bundle.getSerializable("HeightFrom"), (Integer) bundle.getSerializable("HeightTo"),
                            bundle.getStringArrayList("Education"),
                            bundle.getStringArrayList("Religion"),
                            bundle.getStringArrayList("Alcohol"),
                            bundle.getStringArrayList("Smoking"),
                            bundle.getBoolean("PicturesOnly"),
                            bundle.getBoolean("OnlineOnly"),
                            page, RESULTS_PER_PAGE,
                            new RequestsInterface.ResponseCallback() {
                                @Override
                                public void execute() {
                                    listView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                                @Override
                                public void execute(Object object) {

                                }
                            });
                }
            };
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (dataSource != null) {
            initMethodToCall(dataSource);
        }

        new LoadDataTask().execute();

        ((MyApplication) getApplication()).setAppVisible();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_members_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_members_list_close) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getUsersListFromServer() {
        if (noMore)
            return;

        try {
            final ServerResponse<Pair<Boolean, List<OtherUser>>> sr = invokeAppropriateFunction();

            if (sr.isSuccess()) {       // if request returned successfully
                // next time take second page and so on
                page++;

                if (sr.getResult().second.size() > 0) {
                    // add items to list of items
                    listOfUsers.addAll(sr.getResult().second);

                    if (sr.getResult().first) {
                        // no more
                        noMore = true;
                        listView.setNoMore(true);
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
                if (listOfUsers.size() == 0) {
                    // something went wrong with the connection
                    textViewLoadingFailed.setText(msg);
                    layoutLoadingFailed.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                } else {
                    // there is data in the list so just show a toast message
                    Toast.makeText(thisActivity, getString(R.string.error_server_connection_falied), Toast.LENGTH_SHORT).show();
                }
                serverError = true;
            }
        });
    }

    private ServerResponse<Pair<Boolean, List<OtherUser>>> invokeAppropriateFunction()
            throws ServerResponseFailedException {
        // call appropriate method
        return method.call(getIntent().getExtras());
    }

    @Override
    public void onStop() {
        super.onStop();

        ((MyApplication) getApplication()).setAppInvisible();
    }

    private interface MethodToCall {

        ServerResponse<Pair<Boolean, List<OtherUser>>> call(Bundle bundle) throws ServerResponseFailedException;
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            if (isCancelled()) {
                return null;
            }

            // load more of the query result
            getUsersListFromServer();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!serverError) {

                if (listOfUsers.isEmpty()) {
                    listView.setVisibility(View.GONE);
                    layoutLoadingFailed.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    // We need notify the adapter that the data have been changed
                    adapter.notifyDataSetChanged();

                    listView.setVisibility(View.VISIBLE);
                    layoutLoadingFailed.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.GONE);

                    // Call onLoadMoreComplete when the LoadMore task, has finished
                }
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
