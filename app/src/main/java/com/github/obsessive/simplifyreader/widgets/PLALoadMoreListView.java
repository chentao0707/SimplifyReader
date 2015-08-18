/*
 * Copyright (c) 2015 [1076559197@qq.com | tchen0707@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.obsessive.simplifyreader.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.obsessive.library.pla.PLAAbsListView;
import com.github.obsessive.library.pla.PLAMultiColumnListView;
import com.github.obsessive.library.progress.CircularProgressBar;
import com.github.obsessive.simplifyreader.R;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/4/3.
 * Description:
 */
public class PLALoadMoreListView extends PLAMultiColumnListView implements PLAAbsListView.OnScrollListener {

    private OnScrollListener mOnScrollListener;
    private LayoutInflater mInflater;

    // footer view
    private RelativeLayout mFooterView;
    private TextView mLabLoadMore;
    private CircularProgressBar mProgressBarLoadMore;

    // Listener to process load more items when user reaches the end of the list
    private OnLoadMoreListener mOnLoadMoreListener;
    // To know if the list is loading more items
    private boolean mIsLoadingMore = false;

    private boolean mCanLoadMore = true;
    private int mCurrentScrollState;

    public PLALoadMoreListView(Context context) {
        super(context);
        init(context);
    }

    public PLALoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PLALoadMoreListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setSelector(R.drawable.transparent);

        // footer
        mFooterView = (RelativeLayout) mInflater.inflate(R.layout.common_load_more_footer, this, false);
        mLabLoadMore = (TextView) mFooterView.findViewById(R.id.common_load_more_footer_msg);
        mProgressBarLoadMore = (CircularProgressBar) mFooterView.findViewById(R.id.common_load_more_footer_progress);

        addFooterView(mFooterView);

        super.setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(PLAAbsListView view, int scrollState) {
        mCurrentScrollState = scrollState;

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(PLAAbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem,
                    visibleItemCount, totalItemCount);
        }

        if (mOnLoadMoreListener != null) {

            if (visibleItemCount == totalItemCount) {
                mProgressBarLoadMore.setVisibility(View.GONE);
                mLabLoadMore.setVisibility(View.GONE);
                return;
            }


            boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

            if (!mIsLoadingMore && loadMore
                    && mCurrentScrollState != SCROLL_STATE_IDLE) {
                if (!mCanLoadMore) {
                    mLabLoadMore.setVisibility(View.VISIBLE);
                    return;
                }
                mProgressBarLoadMore.setVisibility(View.VISIBLE);
                mLabLoadMore.setVisibility(View.GONE);
                mIsLoadingMore = true;
                onLoadMore();
            }


        }
    }

    /**
     * Set the listener that will receive notifications every time the list
     * scrolls.
     *
     * @param listener The scroll listener.
     */
    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
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

    public void setCanLoadMore(boolean canLoadMore) {
        mCanLoadMore = canLoadMore;
        mLabLoadMore.setVisibility(View.GONE);
    }

    public void onLoadMore() {
        if (mOnLoadMoreListener != null) {
            mOnLoadMoreListener.onLoadMore();
        }
    }

    /**
     * Notify the loading more operation has finished
     */
    public void onLoadMoreComplete() {
        mIsLoadingMore = false;
        mProgressBarLoadMore.setVisibility(View.GONE);
    }

    /**
     * Interface definition for a callback to be invoked when list reaches the
     * last item (the user load more items in the list)
     */
    public interface OnLoadMoreListener {
        /**
         * Called when the list reaches the last item (the last item is visible
         * to the user)
         */
        public void onLoadMore();
    }
}
