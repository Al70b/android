package com.al70b.core.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.al70b.R;
import com.al70b.core.adapters.FriendsRequestsAdapter2;

public class UsersListActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.Adapter mAdapter;
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        String[] s = new String[]{"Hello", "There", "Nice", "Keep going", "Okay"};
        mAdapter = new FriendsRequestsAdapter2(s);
        mRecyclerView.setAdapter(mAdapter);
    }


}