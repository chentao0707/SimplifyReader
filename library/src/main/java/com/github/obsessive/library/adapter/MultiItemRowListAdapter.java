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

package com.github.obsessive.library.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MultiItemRowListAdapter implements WrapperListAdapter {
    private final ListAdapter mAdapter;
    private final int mItemsPerRow;
    private final int mCellSpacing;
    private final WeakReference<Context> mContextReference;
    private final LinearLayout.LayoutParams mItemLayoutParams;
    private final AbsListView.LayoutParams mRowLayoutParams;

    public MultiItemRowListAdapter(Context context, ListAdapter adapter, int itemsPerRow, int cellSpacing) {
        if (itemsPerRow <= 0) {
            throw new IllegalArgumentException("Number of items per row must be positive");
        }
        mContextReference = new WeakReference<Context>(context);
        mAdapter = adapter;
        mItemsPerRow = itemsPerRow;
        mCellSpacing = cellSpacing;

        mItemLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        mItemLayoutParams.setMargins(cellSpacing, cellSpacing, 0, 0);
        mItemLayoutParams.weight = 1;
        mRowLayoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean isEmpty() {
        return (mAdapter == null || mAdapter.isEmpty());
    }

    public int getItemsPerRow() {
        return mItemsPerRow;
    }

    @Override
    public int getCount() {
        if (mAdapter != null) {
            return (int)Math.ceil(1.0f * mAdapter.getCount() / mItemsPerRow);
        }
        return 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        if (mAdapter != null) {
            return mAdapter.areAllItemsEnabled();
        } else {
            return true;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        if (mAdapter != null) {
            // the cell is enabled if at least one item is enabled
            boolean enabled = false;
            for (int i = 0; i < mItemsPerRow; ++i) {
                int p = position * mItemsPerRow + i;
                if (p < mAdapter.getCount()) {
                    enabled |= mAdapter.isEnabled(p);
                }
            }
            return enabled;
        }
        return true;
    }

    @Override
    public Object getItem(int position) {
        if (mAdapter != null) {
            List<Object> items = new ArrayList<Object>(mItemsPerRow);
            for (int i = 0; i < mItemsPerRow; ++i) {
                int p = position * mItemsPerRow + i;
                if (p < mAdapter.getCount()) {
                    items.add(mAdapter.getItem(p));
                }
            }
            return items;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (mAdapter != null) {
            return position;
        }
        return -1;
    }

    @Override
    public boolean hasStableIds() {
        if (mAdapter != null) {
            return mAdapter.hasStableIds();
        }
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context c = mContextReference.get();
        if (c == null || mAdapter == null) return null;

        LinearLayout view = null;
        if (convertView == null
                || !(convertView instanceof LinearLayout)
                || !((Integer)convertView.getTag()).equals(mItemsPerRow)) {
            // create a linear Layout
            view = new LinearLayout(c);
            view.setPadding(0, 0, mCellSpacing, 0);
            view.setLayoutParams(mRowLayoutParams);
            view.setOrientation(LinearLayout.HORIZONTAL);
            view.setBaselineAligned(false);
            view.setTag(Integer.valueOf(mItemsPerRow));
        } else {
            view = (LinearLayout) convertView;
        }

        for (int i = 0; i < mItemsPerRow; ++i) {
            View subView = i < view.getChildCount() ? view.getChildAt(i) : null;
            int p = position * mItemsPerRow + i;

            View newView = subView;
            if (p < mAdapter.getCount()) {
            	if (subView instanceof PlaceholderView){
            		view.removeView(subView);
            		subView = null;
            	}
                newView = mAdapter.getView(p, subView, view);
            } else if (subView == null || !(subView instanceof PlaceholderView)) {
                newView = new PlaceholderView(c);
            }
            if (newView != subView || i >= view.getChildCount()) {
                if (i < view.getChildCount()) {
                    view.removeView(subView);
                }
                newView.setLayoutParams(mItemLayoutParams);
                view.addView(newView, i);
            }
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        if (mAdapter != null) {
            return mAdapter.getItemViewType(position);
        }

        return -1;
    }

    @Override
    public int getViewTypeCount() {
        if (mAdapter != null) {
            return mAdapter.getViewTypeCount();
        }
        return 1;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(observer);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(observer);
        }
    }

    @Override
    public ListAdapter getWrappedAdapter() {
        return mAdapter;
    }

    public static class PlaceholderView extends View {

        public PlaceholderView(Context context) {
            super(context);
        }

    }
}