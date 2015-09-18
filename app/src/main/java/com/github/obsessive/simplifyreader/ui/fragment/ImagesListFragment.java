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

package com.github.obsessive.simplifyreader.ui.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;

import com.github.obsessive.library.adapter.ListViewDataAdapter;
import com.github.obsessive.library.adapter.ViewHolderBase;
import com.github.obsessive.library.adapter.ViewHolderCreator;
import com.github.obsessive.library.eventbus.EventCenter;
import com.github.obsessive.library.netstatus.NetUtils;
import com.github.obsessive.library.pla.PLAAdapterView;
import com.github.obsessive.library.pla.PLAImageView;
import com.github.obsessive.library.utils.CommonUtils;
import com.github.obsessive.library.widgets.XSwipeRefreshLayout;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.api.ApiConstants;
import com.github.obsessive.simplifyreader.bean.ImagesListEntity;
import com.github.obsessive.simplifyreader.bean.ResponseImagesListEntity;
import com.github.obsessive.simplifyreader.common.Constants;
import com.github.obsessive.simplifyreader.common.OnCommonPageSelectedListener;
import com.github.obsessive.simplifyreader.presenter.ImagesListPresenter;
import com.github.obsessive.simplifyreader.presenter.impl.ImagesListPresenterImpl;
import com.github.obsessive.simplifyreader.ui.activity.ImagesDetailActivity;
import com.github.obsessive.simplifyreader.ui.activity.base.BaseFragment;
import com.github.obsessive.simplifyreader.utils.UriHelper;
import com.github.obsessive.simplifyreader.view.ImagesListView;
import com.github.obsessive.simplifyreader.widgets.PLALoadMoreListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/4/2.
 * Description:
 */
public class ImagesListFragment extends BaseFragment implements ImagesListView, OnCommonPageSelectedListener,
        PLALoadMoreListView.OnLoadMoreListener, PLAAdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.fragment_images_list_swipe_layout)
    XSwipeRefreshLayout mSwipeRefreshLayout;

    @InjectView(R.id.fragment_images_list_list_view)
    PLALoadMoreListView mListView;

    /**
     * this variable must be initialized.
     */
    private static String mCurrentImagesCategory = null;

    /**
     * the page number
     */
    private int mCurrentPage = 0;

    private ImagesListPresenter mImagesListPresenter = null;
    private ListViewDataAdapter<ImagesListEntity> mListViewAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentImagesCategory = getResources().getStringArray(R.array.images_category_list_id)[0];
    }

    @Override
    protected void onFirstUserVisible() {
        mCurrentPage = 0;
        mImagesListPresenter = new ImagesListPresenterImpl(mContext, this);

        if (NetUtils.isNetworkConnected(mContext)) {
            if (null != mSwipeRefreshLayout) {
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mImagesListPresenter.loadListData(TAG_LOG, Constants.EVENT_REFRESH_DATA, mCurrentImagesCategory,
                                mCurrentPage, false);
                    }
                }, ApiConstants.Integers.PAGE_LAZY_LOAD_DELAY_TIME_MS);
            }
        } else {
            toggleNetworkError(true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mImagesListPresenter.loadListData(TAG_LOG, Constants.EVENT_REFRESH_DATA, mCurrentImagesCategory,
                            mCurrentPage, false);
                }
            });
        }
    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected View getLoadingTargetView() {
        return mSwipeRefreshLayout;
    }

    @Override
    protected void initViewsAndEvents() {
        mListViewAdapter = new ListViewDataAdapter<ImagesListEntity>(new ViewHolderCreator<ImagesListEntity>() {
            @Override
            public ViewHolderBase<ImagesListEntity> createViewHolder(int position) {
                return new ViewHolderBase<ImagesListEntity>() {

                    PLAImageView mItemImage;

                    @Override
                    public View createView(LayoutInflater layoutInflater) {
                        View convertView = layoutInflater.inflate(R.layout.list_item_images_list, null);
                        mItemImage = ButterKnife.findById(convertView, R.id.list_item_images_list_image);

                        return convertView;
                    }

                    @Override
                    public void showData(int position, ImagesListEntity itemData) {
                        int width = itemData.getThumbnailWidth();
                        int height = itemData.getThumbnailHeight();

                        String imageUrl = itemData.getThumbnailUrl();

                        if (!CommonUtils.isEmpty(imageUrl)) {
                            ImageLoader.getInstance().displayImage(imageUrl, mItemImage);
                        }

                        mItemImage.setImageWidth(width);
                        mItemImage.setImageHeight(height);
                    }
                };
            }
        });

        mListView.setOnItemClickListener(this);
        mListView.setOnLoadMoreListener(this);
        mListView.setAdapter(mListViewAdapter);

        mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.gplus_color_1),
                getResources().getColor(R.color.gplus_color_2),
                getResources().getColor(R.color.gplus_color_3),
                getResources().getColor(R.color.gplus_color_4));
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_images_list;
    }

    @Override
    protected void onEventComming(EventCenter eventCenter) {

    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public void refreshListData(ResponseImagesListEntity responseImagesListEntity) {
        if (null != mSwipeRefreshLayout) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        if (null != responseImagesListEntity) {
            if (null != mListViewAdapter) {
                mListViewAdapter.getDataList().clear();
                mListViewAdapter.getDataList().addAll(responseImagesListEntity.getImgs());
                mListViewAdapter.notifyDataSetChanged();
            }

            if (null != mListView) {
                if (UriHelper.getInstance().calculateTotalPages(responseImagesListEntity.getTotalNum()) > mCurrentPage) {
                    mListView.setCanLoadMore(true);
                } else {
                    mListView.setCanLoadMore(false);
                }
            }
        }
    }

    @Override
    public void addMoreListData(ResponseImagesListEntity responseImagesListEntity) {
        if (null != mListView) {
            mListView.onLoadMoreComplete();
        }

        if (null != responseImagesListEntity) {
            if (null != mListViewAdapter) {
                mListViewAdapter.getDataList().addAll(responseImagesListEntity.getImgs());
                mListViewAdapter.notifyDataSetChanged();
            }

            if (null != mListView) {
                if (UriHelper.getInstance().calculateTotalPages(responseImagesListEntity.getTotalNum()) > mCurrentPage) {
                    mListView.setCanLoadMore(true);
                } else {
                    mListView.setCanLoadMore(false);
                }
            }
        }
    }

    @Override
    public void navigateToImagesDetail(int position, ImagesListEntity entity, int x, int y, int width, int height) {
        Bundle extras = new Bundle();
        extras.putString(ImagesDetailActivity.INTENT_IMAGE_URL_TAG, entity.getThumbnailUrl());
        extras.putInt(ImagesDetailActivity.INTENT_IMAGE_X_TAG, x);
        extras.putInt(ImagesDetailActivity.INTENT_IMAGE_Y_TAG, y);
        extras.putInt(ImagesDetailActivity.INTENT_IMAGE_W_TAG, width);
        extras.putInt(ImagesDetailActivity.INTENT_IMAGE_H_TAG, height);
        readyGo(ImagesDetailActivity.class, extras);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onPageSelected(int position, String keywords) {
        mCurrentImagesCategory = keywords;
    }

    @Override
    public void onRefresh() {
        mCurrentPage = 0;
        mImagesListPresenter.loadListData(TAG_LOG, Constants.EVENT_REFRESH_DATA, mCurrentImagesCategory, mCurrentPage,
                true);
    }

    @Override
    public void onLoadMore() {
        mCurrentPage++;
        mImagesListPresenter.loadListData(TAG_LOG, Constants.EVENT_LOAD_MORE_DATA, mCurrentImagesCategory, mCurrentPage, true);
    }

    @Override
    public void showError(String msg) {
        if (null != mSwipeRefreshLayout) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        toggleShowError(true, msg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImagesListPresenter.loadListData(TAG_LOG, Constants.EVENT_REFRESH_DATA, mCurrentImagesCategory,
                        mCurrentPage, false);
            }
        });
    }

    @Override
    public void onItemClick(PLAAdapterView<?> parent, View view, int position, long id) {
        Rect frame = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        location[1] += statusBarHeight;

        int width = view.getWidth();
        int height = view.getHeight();

        if (null != mListViewAdapter) {
            if (position >= 0 && position < mListViewAdapter.getDataList().size()) {
                mImagesListPresenter.onItemClickListener(position,
                        mListViewAdapter.getDataList().get(position),
                        location[0],
                        location[1],
                        width,
                        height);
            }
        }
    }
}
