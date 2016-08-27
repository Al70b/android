package com.al70b.core.extended_widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.al70b.R;

import java.util.List;

/**
 * Created by Naseem on 8/26/2016.
 */
public class LoadMoreRecyclerView extends RecyclerView {
    private static final String TAG = "LoadMoreRecyclerView";

    public LoadMoreRecyclerView(Context context) {
        super(context);
        init();
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    // Listener to process load more items when user reaches the end of the list
    private OnLoadMoreListener mOnLoadMoreListener;

    // To know if no more items to load
    private boolean mNoMoreLoading;

    // To know if the recycler view is loading more items
    private boolean mIsLoadingMore = false;

    // Threshold for when to load more
    private int visibleThreshold = 2;

    public void init() {
        addOnScrollListener(new OnLoadMoreScrollListener());
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (!(adapter instanceof LoadMoreRecyclerView.Adapter)) {
            throw new IllegalArgumentException("Adapter must extend " + LoadMoreRecyclerView.Adapter.class.getSimpleName());
        }

        super.setAdapter(adapter);
    }

    @Override
    public void setLayoutManager(LayoutManager layoutManager) {
        if (!(layoutManager instanceof GridLayoutManager)
                && !(layoutManager instanceof LinearLayoutManager)) {
            throw new IllegalArgumentException(
                    "Layout manager for LoadMoreRecyclerView should be either GridLayoutManager or LinearLayoutManager");
        }

        super.setLayoutManager(layoutManager);

        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager glm = (GridLayoutManager) layoutManager;
            glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    LoadMoreRecyclerView.Adapter adapter = (LoadMoreRecyclerView.Adapter) getAdapter();
                    if (adapter != null) {
                        switch (adapter.getItemViewType(position)) {
                            case LoadMoreRecyclerView.Adapter.VIEW_TYPE_LOADING:
                                return glm.getSpanCount(); //number of columns of the grid
                            default:
                                return 1;
                        }
                    }
                    return 1;
                }
            });
        }
    }

    private class OnLoadMoreScrollListener extends OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            LayoutManager layoutManager = recyclerView.getLayoutManager();
            int lastVisibleItem;
            int totalItemCount;

            if (layoutManager != null) {
                totalItemCount = layoutManager.getItemCount();

                // get last visible item position
                if (layoutManager instanceof GridLayoutManager) {
                    lastVisibleItem = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                } else {
                    // otherwise it should be LinearLayoutManager
                    lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                }

                if (!mIsLoadingMore && totalItemCount <= (lastVisibleItem + visibleThreshold)
                        && !mNoMoreLoading && mOnLoadMoreListener != null) {
                    // load more data
                    mIsLoadingMore = true;

                    LoadMoreRecyclerView.Adapter adapter = (LoadMoreRecyclerView.Adapter) getAdapter();
                    adapter.setIsLoading();
                    mOnLoadMoreListener.onLoadMore();
                }
            }
        }
    }


    public int getVisibleThreshold() {
        return visibleThreshold;
    }

    public void setVisibleThreshold(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    /**
     * Register a callback to be invoked when this list reaches the end (last
     * item be visible)
     *
     * @param onLoadMoreListener The callback to run.
     */

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    /**
     * Notify the loading more operation has finished
     */
    public void onLoadMoreComplete() {
        mIsLoadingMore = false;

        ((LoadMoreRecyclerView.Adapter) getAdapter()).setDoneLoading();

        if (emptyView != null) {
            if(getAdapter().getItemCount() == 0) {
                // empty
                emptyView.setVisibility(View.VISIBLE);
                this.setVisibility(View.GONE);
            } else {
                // not empty
                emptyView.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
            }
        }

    }

    public void setNoMoreLoading(boolean noMore) {
        mNoMoreLoading = noMore;
    }


    private View emptyView;

    /**
     * Interface definition for a callback to be invoked when list reaches the
     * last item (the user load more items in the list)
     */
    public interface OnLoadMoreListener {
        /**
         * Called when the list reaches the last item (the last item is visible
         * to the user)
         */
        void onLoadMore();
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


    public void setEmptyView(View view) {
        emptyView = view;
    }

    public View getEmptyView() {
        return emptyView;
    }


    public static abstract class Adapter<VH extends RecyclerView.ViewHolder>
            extends RecyclerView.Adapter{

        public static final int VIEW_TYPE_LOADING = 1;

        private Context context;
        private List dataSet;

        public static class LoadingViewHolder extends RecyclerView.ViewHolder {
            ProgressBar pb;

            public LoadingViewHolder(View row) {
                super(row);
                pb = (ProgressBar) row.findViewById(R.id.list_item_loading_progress_bar);
                pb.getIndeterminateDrawable().setColorFilter(0xFFFF0000,
                        android.graphics.PorterDuff.Mode.MULTIPLY);
            }
        }

        private boolean isLoading = false;

        public Adapter(Context context, List dataSet) {
            this.context = context;
            this.dataSet = dataSet;
        }

        @Override
        public int getItemViewType(int position) {
            if (dataSet.get(position) == null) {
                return VIEW_TYPE_LOADING;
            }

            return super.getItemViewType(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder vh = null;

            switch (viewType) {
                case VIEW_TYPE_LOADING:
                    View v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_loading, parent, false);
                    vh = new LoadingViewHolder(v);
                    break;
            }

            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            }
        }

        public void setIsLoading() {
            isLoading = true;
            dataSet.add((Object)null);
            notifyItemInserted(dataSet.size());
        }

        public void setDoneLoading() {
            isLoading = false;
            dataSet.remove(null);
            notifyItemRemoved(dataSet.size() - 1);
        }

        public Object getItemAtPosition(int position) {
            return dataSet.get(position);
        }
    }


}
