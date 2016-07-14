package com.al70b.core.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.al70b.R;

/**
 * Created by Naseem on 7/11/2016.
 */
public class FriendsRequestsAdapter2 extends RecyclerView.Adapter<FriendsRequestsAdapter2.ViewHolder> {

    private String[] mDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FriendsRequestsAdapter2(String[] myDataSet) {
        this.mDataSet = myDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FriendsRequestsAdapter2.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_friend_request, parent, false);
        TextView vv = (TextView) v.findViewById(R.id.info_text);
        ViewHolder vh = new ViewHolder(vv);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataSet[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.length;
    }
}

