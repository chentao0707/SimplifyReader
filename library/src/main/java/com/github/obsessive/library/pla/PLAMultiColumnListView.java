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

package com.github.obsessive.library.pla;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.github.obsessive.library.R;

/**
 * @author huewu.ynag
 * @date 2012-11-06
 */
public class PLAMultiColumnListView extends PLAListView {

	@SuppressWarnings("unused")
	private static final String TAG = "MultiColumnListView";

	private static final int DEFAULT_COLUMN_NUMBER = 2;

	private int mColumnNumber = 2;
	private Column[] mColumns = null;
	private Column mFixedColumn = null;	//column for footers & headers.
	private ParcelableSparseIntArray mItems = new ParcelableSparseIntArray();

	private int mColumnPaddingLeft = 0;
	private int mColumnPaddingRight = 0;

	public PLAMultiColumnListView(Context context) {
		super(context);
		init(null);
	}

	public PLAMultiColumnListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public PLAMultiColumnListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private Rect mFrameRect = new Rect();

	private void init(AttributeSet attrs) {
		getWindowVisibleDisplayFrame(mFrameRect);

		if( attrs == null ){
			mColumnNumber = DEFAULT_COLUMN_NUMBER; 	//default column number is 2.
		}else{
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PLAMultiColumnListView);

			int landColNumber = a.getInteger(R.styleable.PLAMultiColumnListView_plaLandscapeColumnNumber, -1);
			int defColNumber = a.getInteger(R.styleable.PLAMultiColumnListView_plaColumnNumber, -1);

			if(mFrameRect.width() > mFrameRect.height() && landColNumber != -1 ){
				mColumnNumber = landColNumber;
			}else if(defColNumber != -1){
				mColumnNumber = defColNumber;
			}else{
				mColumnNumber = DEFAULT_COLUMN_NUMBER;
			}

			mColumnPaddingLeft = a.getDimensionPixelSize(R.styleable.PLAMultiColumnListView_plaColumnPaddingLeft, 0);
			mColumnPaddingRight = a.getDimensionPixelSize(R.styleable.PLAMultiColumnListView_plaColumnPaddingRight, 0);
			a.recycle();
		}

		mColumns = new Column[mColumnNumber];
		for( int i = 0; i < mColumnNumber; ++i )
			mColumns[i] = new Column(i);

		mFixedColumn = new FixedColumn();
	}

    public void setColumnPaddingLeft(int columnPaddingLeft) {
        this.mColumnPaddingLeft = columnPaddingLeft;
    }

    public void setColumnPaddingRight(int columnPaddingRight) {
        this.mColumnPaddingRight = columnPaddingRight;
    }

	///////////////////////////////////////////////////////////////////////
	//Override Methods...
	///////////////////////////////////////////////////////////////////////

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		//TODO the adapter status may be changed. what should i do here...
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = (getMeasuredWidth() - mListPadding.left - mListPadding.right - mColumnPaddingLeft - mColumnPaddingRight) / mColumnNumber;

		for( int index = 0; index < mColumnNumber; ++ index ){
			mColumns[index].mColumnWidth = width;
			mColumns[index].mColumnLeft = mListPadding.left + mColumnPaddingLeft + width * index;
		}

		mFixedColumn.mColumnLeft = mListPadding.left;
		mFixedColumn.mColumnWidth = getMeasuredWidth();
	}

	@Override
	protected void onMeasureChild(View child, int position, int widthMeasureSpec, int heightMeasureSpec) {
		if(isFixedView(child))
			child.measure(widthMeasureSpec, heightMeasureSpec);
		else
			child.measure(MeasureSpec.EXACTLY | getColumnWidth(position), heightMeasureSpec);
	}

	@Override
	protected int modifyFlingInitialVelocity(int initialVelocity) {
		return initialVelocity;
	}

	@Override
	protected void onItemAddedToList(int position, boolean flow ) {
		super.onItemAddedToList(position, flow);

		if( isHeaderOrFooterPosition(position) == false){
			Column col = getNextColumn( flow, position );
			mItems.append(position, col.getIndex());
		}
	}

	@Override
	protected void onLayoutSync(int syncPos) {
		for( Column c : mColumns ){
			c.save();
		}
	}

	@Override
	protected void onLayoutSyncFinished(int syncPos) {
		for( Column c : mColumns ){
			c.clear();
		}
	}

	@Override
	protected void onAdjustChildViews(boolean down) {

		int firstItem = getFirstVisiblePosition();
		if(!down && firstItem == 0){
			final int firstColumnTop = mColumns[0].getTop();
			for( Column c : mColumns ){
				final int top = c.getTop();
				//align all column's top to 0's column.
				c.offsetTopAndBottom( firstColumnTop - top );
			}
		}
		super.onAdjustChildViews(down);
	}

	@Override
	protected int getFillChildBottom() {
		//return smallest bottom value.
		//in order to determine fill down or not... (calculate below space)
		int result = Integer.MAX_VALUE;
		for(Column c : mColumns){
			int bottom = c.getBottom();
			result = result > bottom ? bottom : result;
		}
		return result;
	}

	@Override
	protected int getFillChildTop() {
		//find largest column.
        int result = Integer.MIN_VALUE;
        for(Column c : mColumns){
            int top = c.getTop();
            result = Math.max(result, top);
        }
		return result;
	}

	@Override
	protected int getScrollChildBottom() {
		//return largest bottom value.
		//for checking scrolling region...
		int result = Integer.MIN_VALUE;
		for(Column c : mColumns){
			int bottom = c.getBottom();
			result = result < bottom ? bottom : result;
		}
		return result;
	}

	@Override
	protected int getScrollChildTop() {
		//find largest column.
		int result = Integer.MAX_VALUE;
		for(Column c : mColumns){
			int top = c.getTop();
			result = result > top ? top : result;
		}
		return result;
	}

	@Override
	protected int getItemLeft(int pos) {

		if( isHeaderOrFooterPosition(pos) )
			return mFixedColumn.getColumnLeft();

		return getColumnLeft(pos);
	}

	@Override
	protected int getItemTop( int pos ){

		if( isHeaderOrFooterPosition(pos) )
			return mFixedColumn.getBottom();	//footer view should be placed below the last column.

		int colIndex = mItems.get(pos, -1);
		if(colIndex == -1)
			return getFillChildBottom();

		return mColumns[colIndex].getBottom();
	}

	@Override
	protected int getItemBottom( int pos ){

		if( isHeaderOrFooterPosition(pos) )
			return mFixedColumn.getTop();	//header view should be place above the first column item.

		int colIndex = mItems.get(pos, -1);
		if(colIndex == -1)
			return getFillChildTop();

		return mColumns[colIndex].getTop();
	}

	//////////////////////////////////////////////////////////////////////////////
	//Private Methods...
	//////////////////////////////////////////////////////////////////////////////

	//flow If flow is true, align top edge to y. If false, align bottom edge to y.
	private Column getNextColumn(boolean flow, int position) {

		//we already have this item...
		int colIndex = mItems.get(position, -1);
		if( colIndex != -1 ){
			return mColumns[colIndex];
		}

		//adjust position (exclude headers...)
		position = Math.max(0, position - getHeaderViewsCount());

		final int lastVisiblePos = Math.max( 0, position );
		if( lastVisiblePos < mColumnNumber )
			return mColumns[lastVisiblePos];

		if( flow ){
			//find column which has the smallest bottom value.
			return gettBottomColumn();
		}else{
			//find column which has the smallest top value.
			return getTopColumn();
		}
	}

	private boolean isHeaderOrFooterPosition( int pos ){
		int type = mAdapter.getItemViewType(pos);
		return type == ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
	}

	private Column getTopColumn() {
		Column result = mColumns[0];
		for( Column c : mColumns ){
			result = result.getTop() > c.getTop() ? c : result;
		}
		return result;
	}

	private Column gettBottomColumn() {
		Column result = mColumns[0];
		for( Column c : mColumns ){
			result = result.getBottom() > c.getBottom() ? c : result;
		}

		return result;
	}

	private int getColumnLeft(int pos) {
		int colIndex = mItems.get(pos, -1);

		if( colIndex == -1 )
			return 0;

		return mColumns[colIndex].getColumnLeft();
	}

	private int getColumnWidth(int pos) {
		int colIndex = mItems.get(pos, -1 );

		if( colIndex == -1 )
			return 0;

		return mColumns[colIndex].getColumnWidth();
	}

	///////////////////////////////////////////////////////////////
	//Inner Class.
	///////////////////////////////////////////////////////////////

	private class Column {

		private int mIndex;
		private int mColumnWidth;
		private int mColumnLeft;
		private int mSynchedTop = 0;
		private int mSynchedBottom = 0;

		//TODO is it ok to use item position info to identify item??

		public Column(int index) {
			mIndex = index;
		}

		public int getColumnLeft() {
			return mColumnLeft;
		}

		public int getColumnWidth() {
			return mColumnWidth;
		}

		public int getIndex() {
			return mIndex;
		}

		public int getBottom() {
			//find biggest value.
			int bottom = Integer.MIN_VALUE;
			int childCount = getChildCount();

			for( int index = 0; index < childCount; ++index ){
				View v = getChildAt(index);

				if(v.getLeft() != mColumnLeft && isFixedView(v) == false )
					continue;
				bottom = bottom < v.getBottom() ? v.getBottom() : bottom;
			}

			if( bottom == Integer.MIN_VALUE )
				return mSynchedBottom;	//no child for this column..
			return bottom;
		}

		public void offsetTopAndBottom(int offset) {
			if( offset == 0 )
				return;

			//find biggest value.
			int childCount = getChildCount();

			for( int index = 0; index < childCount; ++index ){
				View v = getChildAt(index);

				if(v.getLeft() != mColumnLeft && isFixedView(v) == false )
					continue;

				v.offsetTopAndBottom(offset);
			}
		}

		public int getTop() {
			//find smallest value.
			int top = Integer.MAX_VALUE;
			int childCount = getChildCount();
			for( int index = 0; index < childCount; ++index ){
				View v = getChildAt(index);
                if(v.getLeft() != mColumnLeft && !isFixedView(v))
                    continue;
                top = Math.min(top, v.getTop());
            }

			if( top == Integer.MAX_VALUE )
				return mSynchedTop;	//no child for this column. just return saved sync top..
			return top;
		}

		public void save() {
			mSynchedTop = 0;
			mSynchedBottom = getTop();
		}

		public void clear() {
			mSynchedTop = 0;
			mSynchedBottom = 0;
		}
	}//end of inner class Column

	private class FixedColumn extends Column {

		public FixedColumn() {
			super(Integer.MAX_VALUE);
		}

		@Override
		public int getBottom() {
			return getScrollChildBottom();
		}

		@Override
		public int getTop() {
			return getScrollChildTop();
		}

	}
	
	
	private boolean loadingMoreComplete = true;

    public void onLoadMoreComplete() {
        loadingMoreComplete = true;
    }

    public interface OnLoadMoreListener {
        /**
         * Method to be called when scroll to buttom is requested
         */
        void onLoadMore();
    }

    public OnScrollListener scroller = new OnScrollListener() {
        private int visibleLastIndex = 0;
        private static final int OFFSET = 2;

        @Override
        public void onScrollStateChanged(PLAAbsListView view, int scrollState) {
            int lastIndex = getAdapter().getCount() - OFFSET;
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                    && visibleLastIndex == lastIndex && loadingMoreComplete) {

                loadMoreListener.onLoadMore();
                loadingMoreComplete = false;

            }
        }

        @Override
        public void onScroll(PLAAbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            visibleLastIndex = firstVisibleItem + visibleItemCount - OFFSET;
        }
    };
    OnLoadMoreListener loadMoreListener;

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {

        if (listener != null) {
            this.loadMoreListener = listener;
            this.setOnScrollListener(scroller);
        }
    }//end of class


    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putParcelable("items", mItems);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof  Bundle) {
            Bundle bundle = (Bundle) state;
            mItems = bundle.getParcelable("items");
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }
}//end of class
