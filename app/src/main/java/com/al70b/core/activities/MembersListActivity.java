package com.al70b.core.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.adapters.MembersRecycleViewAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.LoadMoreRecyclerView;
import com.al70b.core.fragments.UserAdvancedSearchFragment;
import com.al70b.core.fragments.UserBasicSearchFragment;
import com.al70b.core.misc.Utils;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;
import com.al70b.core.server_methods.RequestsInterface.ResponseCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MembersListActivity extends Activity {

    public static final String DATA_SOURCE = "com.al70b.MembersListActivity.DATA_SOURCE";
    private static final String TAG = "MembersListActivity";
    private static final int RESULTS_PER_PAGE = 10;
    private static final int PROFILE_VISIT_RESULT = 1;

    private CurrentUser currentUser;

    // string representing the source of the data (basic\advanced search)
    private String dataSource;

    // page #page from server
    private int page = 1;

    private List<OtherUser> listOfMembers;
    //private MembersListAdapter membersListAdapter;

    private LinearLayout layoutLoading;
    //private LoadMoreListView loadMoreListView;
    private LoadMoreRecyclerView loadMoreRecyclerView;
    private LinearLayout layoutFailedToLoad;

    private LoadMoreRecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    // wrapper for the method to call on loading more members
    private Callable<ServerResponse<Pair<Boolean, List<OtherUser>>>> methodToCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_list);

        // get intent
        Intent intent = getIntent();

        if (intent == null) {
            Log.e(TAG, "Intent is null");
            return;
        }

        dataSource = intent.getStringExtra(MembersListActivity.DATA_SOURCE);
        currentUser = ((MyApplication) getApplication()).getCurrentUser();

        // relate widgets
        loadMoreRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.list_view_userMembersA);
        TextView tvEmptyList = (TextView) findViewById(R.id.tv_members_list_empty_list);
        layoutFailedToLoad = (LinearLayout) findViewById(R.id.layout_userMembersA_failed_loading);
        layoutLoading = (LinearLayout) findViewById(R.id.layout_userMembersA_loading);

        loadMoreRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new GridLayoutManager(this, 2);
        mLayoutManager.offsetChildrenHorizontal((int)Utils.convertDpToPixel(5, this));
        mLayoutManager.offsetChildrenVertical((int)Utils.convertDpToPixel(8, this));
        loadMoreRecyclerView.setLayoutManager(mLayoutManager);

        // create list for received members from server, and an membersListAdapter
        listOfMembers = new ArrayList<>();

        // specify an adapter (see also next example)
        mAdapter = new MembersRecycleViewAdapter(this, listOfMembers, currentUser);
        loadMoreRecyclerView.setAdapter(mAdapter);
        loadMoreRecyclerView.setOnLoadMoreListener(new LoadMoreRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                // Do the work to load more items at the end of list here
                new LoadMoreMembersTask(dataSource).execute();
            }
        });

        /*loadMoreRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                OtherUser otherUser = (OtherUser) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(MembersListActivity.this, MemberProfileActivity.class);

                intent.putExtra(MemberProfileActivity.CURRENT_USER, currentUser);
                intent.putExtra(MemberProfileActivity.OTHER_USER, otherUser);
                intent.putExtra(MemberProfileActivity.POSITION, i);
                startActivityForResult(intent, PROFILE_VISIT_RESULT);
            }
        });*/
        //loadMoreRecyclerView.setEmptyView(tvEmptyList);

        layoutFailedToLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
            }
        });
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

    @Override
    public void onStart() {
        super.onStart();

        // the task for loading more members in the list
        new LoadMoreMembersTask(dataSource).execute();

        ((MyApplication) getApplication()).setAppVisible();
    }

    @Override
    public void onStop() {
        super.onStop();

        ((MyApplication) getApplication()).setAppInvisible();
    }



    private class LoadMoreMembersTask extends AsyncTask<Void, Void, Pair<Boolean, String>> {

        private boolean isNoMoreResult;

        public LoadMoreMembersTask(String dataSource) {
            if(methodToCall == null) {
                methodToCall = initMethodToCall(dataSource, getIntent().getExtras());
            }
        }

        @Override
        protected Pair<Boolean, String> doInBackground(Void... params) {
            if (isCancelled() || isNoMoreResult) {
                return null;
            }

            Pair<Boolean,String> result = new Pair<Boolean,String>();

            try {
                final ServerResponse<Pair<Boolean, List<OtherUser>>> sr = methodToCall.call();

                // if request returned successfully
                if (sr.isSuccess()) {
                    // next time take second page and so on
                    page++;

                    if (sr.getResult().second != null && sr.getResult().second.size() > 0) {
                        // add items to list of items
                        listOfMembers.addAll(sr.getResult().second);

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

            //mAdapter.setDoneLoading();

            // result is surely not null, check if request succeeded
            if (result.first) {
                loadMoreRecyclerView.setVisibility(View.VISIBLE);
                layoutLoading.setVisibility(View.GONE);

                if(layoutFailedToLoad.getVisibility() == View.VISIBLE) {
                    layoutFailedToLoad.setVisibility(View.GONE);
                }

                // This needs to be before notifyDataSetChanged
                loadMoreRecyclerView.onLoadMoreComplete();

                // We need notify the membersListAdapter that the data have been changed
                mAdapter.notifyDataSetChanged();

                loadMoreRecyclerView.setNoMoreLoading(isNoMoreResult);
            } else {
                // failed to fulfill request

                if (listOfMembers.size() == 0) {
                    // something went wrong with the connection
                    layoutFailedToLoad.setVisibility(View.VISIBLE);
                    layoutLoading.setVisibility(View.GONE);
                    ((TextView)layoutFailedToLoad.findViewById(R.id.text_view_userMembersA_failed))
                            .setText(result.second);
                    loadMoreRecyclerView.setVisibility(View.GONE);
                } else {
                    // there is data in the list so just show a toast message
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.error_server_connection_falied),
                            Toast.LENGTH_SHORT).show();
                }


                loadMoreRecyclerView.onLoadMoreComplete();
            }

            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            // Notify the loading more operation has finished
            loadMoreRecyclerView.onLoadMoreComplete();
        }

        ///////////////////////     METHOD WRAPPER  /////////////////////
        private Callable<ServerResponse<Pair<Boolean, List<OtherUser>>>> initMethodToCall(String source, final Bundle bundle) {
            if (bundle == null) {
                return null;
            }

            if (source.compareTo(UserBasicSearchFragment.DISPLAY_DATA_TOKEN) == 0) {
                return new Callable<ServerResponse<Pair<Boolean, List<OtherUser>>>>() {
                    @Override
                    public ServerResponse<Pair<Boolean, List<OtherUser>>> call() throws ServerResponseFailedException {
                        return new RequestsInterface(getApplicationContext())
                                .getUsers(currentUser.getUserID(), currentUser.getAccessToken(),
                                        bundle.getInt(UserBasicSearchFragment.GENDER),
                                        bundle.getInt(UserBasicSearchFragment.AGE_FROM),
                                        bundle.getInt(UserBasicSearchFragment.AGE_TO),
                                        bundle.getBoolean(UserBasicSearchFragment.PICTURES_ONLY),
                                        bundle.getBoolean(UserBasicSearchFragment.ONLINE_ONLY),
                                        bundle.getBoolean(UserBasicSearchFragment.CLOSE_BY_ONLY),
                                        page, RESULTS_PER_PAGE,
                                        new ResponseCallback<Object>() {
                                            @Override
                                            public void call() {
                                                loadMoreRecyclerView.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mAdapter.notifyDataSetChanged();
                                                        //loadMoreRecyclerView.invalidateViews();
                                                    }
                                                });
                                            }
                                        }
                                );
                    }
                };
            } else if (source.compareTo(UserAdvancedSearchFragment.DISPLAY_DATA_TOKEN) == 0) {
                return new Callable<ServerResponse<Pair<Boolean, List<OtherUser>>>>() {
                    @Override
                    public ServerResponse<Pair<Boolean, List<OtherUser>>> call() throws ServerResponseFailedException {

                        return new RequestsInterface(getApplicationContext())
                                .getUsersAdvanced(currentUser,
                                        bundle.getIntegerArrayList(UserAdvancedSearchFragment.GENDER),
                                        bundle.getInt(UserAdvancedSearchFragment.AGE_FROM),
                                        bundle.getInt(UserAdvancedSearchFragment.AGE_TO),
                                        bundle.getString(UserAdvancedSearchFragment.COUNTRY),
                                        bundle.getInt(UserAdvancedSearchFragment.HEIGHT_FROM),
                                        bundle.getInt(UserAdvancedSearchFragment.HEIGHT_TO),
                                        bundle.getStringArrayList(UserAdvancedSearchFragment.EDUCATION),
                                        bundle.getStringArrayList(UserAdvancedSearchFragment.RELIGION),
                                        bundle.getStringArrayList(UserAdvancedSearchFragment.ALCOHOL),
                                        bundle.getStringArrayList(UserAdvancedSearchFragment.SMOKING),
                                        bundle.getBoolean(UserAdvancedSearchFragment.PICTURES_ONLY),
                                        bundle.getBoolean(UserAdvancedSearchFragment.ONLINE_ONLY),
                                        page, RESULTS_PER_PAGE,
                                        new ResponseCallback<Object>() {
                                            @Override
                                            public void call() {
                                                loadMoreRecyclerView.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mAdapter.notifyDataSetChanged();
                                                        //loadMoreListView.invalidateViews();
                                                    }
                                                });
                                            }
                                        });
                    }
                };
            }

            return null;
        }
        ///////////////////////     END OF METHOD WRAPPER  /////////////////////
    }
}
