package com.al70b.core.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.MemberProfileActivity;
import com.al70b.core.adapters.FriendsListAdapter;
import com.al70b.core.exceptions.ServerResponseFailedException;
import com.al70b.core.extended_widgets.pull_load_listview.LoadMoreListView;
import com.al70b.core.objects.CurrentUser;
import com.al70b.core.objects.OtherUser;
import com.al70b.core.objects.Pair;
import com.al70b.core.objects.ServerResponse;
import com.al70b.core.server_methods.RequestsInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naseem on 5/10/2015.
 */
public class UserFriendListFragment extends Fragment {

    private static final String TAG = "UserFriendListFragment";
    private static final int RESULTS_PER_PAGE = 10;

    // used for when a user profile is visited and need to get friend status
    private static final int PROFILE_VISIT_RESULT = 1;

    private CurrentUser currentUser;

    // #page from server
    private int page;

    private List<OtherUser> listOfFriend;
    private FriendsListAdapter friendsListAdapter;

    private LinearLayout layoutLoading;
    private LoadMoreListView loadMoreListView;
    private LinearLayout layoutFailedToLoad;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = ((MyApplication)getActivity().getApplication()).getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_friends_list, container, false);

        // relate widgets
        loadMoreListView = (LoadMoreListView) viewGroup.findViewById(R.id.list_view_friendsListF_requests);
        TextView tvEmptyList = (TextView) viewGroup.findViewById(R.id.tv_friendsListF_empty_list);
        layoutLoading = (LinearLayout) viewGroup.findViewById(R.id.layout_friendsListF_loading);
        layoutFailedToLoad = (LinearLayout) viewGroup.findViewById(R.id.layout_friendsListF_failed_loading);

        tvEmptyList.setText(getString(R.string.no_friends));
        loadMoreListView.setEmptyView(tvEmptyList);
        listOfFriend = new ArrayList<OtherUser>();
        friendsListAdapter = new FriendsListAdapter(getActivity(), R.layout.list_view_item_friend,
                listOfFriend, currentUser);

        loadMoreListView.setAdapter(friendsListAdapter);
        loadMoreListView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            public void onLoadMore() {
                // Do the work to load more items at the end of list here
                new LoadMoreFriendsTask().execute();
            }
        });

        layoutFailedToLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadMoreFriendsTask().execute();
            }
        });

        loadMoreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OtherUser otherUser = (OtherUser) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), MemberProfileActivity.class);

                intent.putExtra(MemberProfileActivity.CURRENT_USER, currentUser);
                intent.putExtra(MemberProfileActivity.OTHER_USER, otherUser);
                intent.putExtra(MemberProfileActivity.POSITION, position);
                startActivityForResult(intent, PROFILE_VISIT_RESULT);
            }
        });


        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

        page = 1;
        listOfFriend.clear();

        new LoadMoreFriendsTask().execute();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private class LoadMoreFriendsTask extends AsyncTask<Void, Void, Pair<Boolean, String>> {

        private boolean isNoMoreResult;

        public LoadMoreFriendsTask() {
        }

        @Override
        protected void onPreExecute() {
            if (listOfFriend.isEmpty()) {
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

            Pair<Boolean, String> result = new Pair<Boolean, String>();
            try {

                ServerResponse<Pair<Boolean, List<OtherUser>>> sr =
                        new RequestsInterface(getActivity())
                                .getAllFriends(currentUser, page, RESULTS_PER_PAGE, null);

                // if request returned successfully
                if (sr.isSuccess()) {
                    // next time take second page and so on
                    page++;

                    if (sr.getResult().second != null && sr.getResult().second.size() > 0) {
                        // add items to list of items
                        listOfFriend.addAll(sr.getResult().second);

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
            if (result == null) {
                return;
            }

            if(!isAdded()) {
                return;
            }

            // result is not null, check if request succeeded
            if (result.first) {
                loadMoreListView.setVisibility(View.VISIBLE);
                layoutLoading.setVisibility(View.GONE);

                if (layoutFailedToLoad.getVisibility() == View.VISIBLE) {
                    layoutFailedToLoad.setVisibility(View.GONE);
                }

                // We need notify the membersListAdapter that the data have been changed
                friendsListAdapter.notifyDataSetChanged();

                loadMoreListView.setNoMore(isNoMoreResult);
                loadMoreListView.onLoadMoreComplete();
            } else {
                // failed to fulfill request

                if (listOfFriend.size() == 0) {
                    // something went wrong with the connection
                    layoutFailedToLoad.setVisibility(View.VISIBLE);
                    layoutLoading.setVisibility(View.GONE);
                    ((TextView) layoutFailedToLoad.findViewById(R.id.text_view_friendsListF_failed))
                            .setText(result.second);
                    loadMoreListView.setVisibility(View.GONE);
                } else {
                    // there is data in the list so just show a toast message
                    Toast.makeText(getActivity(),
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
