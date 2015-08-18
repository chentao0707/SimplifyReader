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

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.obsessive.library.adapter.ListViewDataAdapter;
import com.github.obsessive.library.adapter.MultiItemRowListAdapter;
import com.github.obsessive.library.adapter.ViewHolderBase;
import com.github.obsessive.library.adapter.ViewHolderCreator;
import com.github.obsessive.library.eventbus.EventCenter;
import com.github.obsessive.library.netstatus.NetUtils;
import com.github.obsessive.library.utils.CommonUtils;
import com.github.obsessive.library.widgets.XSwipeRefreshLayout;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.api.ApiConstants;
import com.github.obsessive.simplifyreader.bean.ResponseVideosListEntity;
import com.github.obsessive.simplifyreader.bean.VideosListEntity;
import com.github.obsessive.simplifyreader.common.Constants;
import com.github.obsessive.simplifyreader.common.OnCommonPageSelectedListener;
import com.github.obsessive.simplifyreader.presenter.VideosListPresenter;
import com.github.obsessive.simplifyreader.presenter.impl.VideosListPresenterImpl;
import com.github.obsessive.simplifyreader.ui.activity.PlayerActivity;
import com.github.obsessive.simplifyreader.ui.activity.base.BaseFragment;
import com.github.obsessive.simplifyreader.utils.UriHelper;
import com.github.obsessive.simplifyreader.view.VideosListView;
import com.github.obsessive.simplifyreader.widgets.LoadMoreListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/4/9.
 * Description:
 */
public class VideosListFragment extends BaseFragment implements VideosListView, OnCommonPageSelectedListener, LoadMoreListView.OnLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.fragment_videos_list_swipe_layout)
    XSwipeRefreshLayout mSwipeRefreshLayout;

    @InjectView(R.id.fragment_videos_list_list_view)
    LoadMoreListView mListView;

    /**
     * this variable must be initialized.
     */
    private static String mCurrentVideosCategory = null;
    /**
     * the page number
     */
    private int mCurrentPage = 1;

    private VideosListPresenter mVideosListPresenter = null;

    private MultiItemRowListAdapter mMultiItemRowListAdapter = null;
    private ListViewDataAdapter<VideosListEntity> mListViewAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentVideosCategory = getResources().getStringArray(R.array.videos_category_list)[0];
    }

    @Override
    protected void onFirstUserVisible() {
        mCurrentPage = 1;
        mVideosListPresenter = new VideosListPresenterImpl(mContext, this);

        if (NetUtils.isNetworkConnected(mContext)) {
            if (null != mSwipeRefreshLayout) {
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mVideosListPresenter.loadListData(TAG_LOG, Constants.EVENT_REFRESH_DATA, mCurrentVideosCategory,
                                mCurrentPage, false);
                    }
                }, ApiConstants.Integers.PAGE_LAZY_LOAD_DELAY_TIME_MS);
            }
        } else {
            toggleNetworkError(true, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVideosListPresenter.loadListData(TAG_LOG, Constants.EVENT_REFRESH_DATA, mCurrentVideosCategory,
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
        mListViewAdapter = new ListViewDataAdapter<VideosListEntity>(new ViewHolderCreator<VideosListEntity>() {
            @Override
            public ViewHolderBase<VideosListEntity> createViewHolder(int position) {
                return new ViewHolderBase<VideosListEntity>() {

                    TextView mItemTitle;
                    ImageView mItemImage;
                    ImageButton mItemPlay;

                    @Override
                    public View createView(LayoutInflater layoutInflater) {
                        View convertView = layoutInflater.inflate(R.layout.list_item_videos_card, null);

                        mItemTitle = ButterKnife.findById(convertView, R.id.list_item_videos_card_title);
                        mItemImage = ButterKnife.findById(convertView, R.id.list_item_videos_card_image);
                        mItemPlay = ButterKnife.findById(convertView, R.id.list_item_videos_card_play);

                        return convertView;
                    }

                    @Override
                    public void showData(final int position, VideosListEntity itemData) {
                        if (null != itemData) {
                            if (!CommonUtils.isEmpty(itemData.getTitle())) {
                                mItemTitle.setText(CommonUtils.decodeUnicodeStr(itemData.getTitle()));
                            }

                            if (!CommonUtils.isEmpty(itemData.getThumbnail_v2())) {
                                ImageLoader.getInstance().displayImage(itemData.getThumbnail_v2(), mItemImage);
                            }

                            mItemPlay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (null != mListViewAdapter) {
                                        if (position >= 0 && position < mListViewAdapter.getDataList().size()) {
                                            mVideosListPresenter.onItemClickListener(position, mListViewAdapter.getDataList().get(position));
                                        }
                                    }
                                }
                            });
                        }
                    }
                };
            }
        });

        mMultiItemRowListAdapter = new MultiItemRowListAdapter(mContext, mListViewAdapter, 1, 0);

        mListView.setAdapter(mMultiItemRowListAdapter);
        mListView.setOnLoadMoreListener(this);

        mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.gplus_color_1),
                getResources().getColor(R.color.gplus_color_2),
                getResources().getColor(R.color.gplus_color_3),
                getResources().getColor(R.color.gplus_color_4));
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_videos_list;
    }

    @Override
    protected void onEventComming(EventCenter eventCenter) {

    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public void onPageSelected(int position, String category) {
        mCurrentVideosCategory = category;
    }

    @Override
    public void showError(String msg) {
        if (null != mSwipeRefreshLayout) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        toggleShowError(true, msg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideosListPresenter.loadListData(TAG_LOG, Constants.EVENT_REFRESH_DATA, mCurrentVideosCategory,
                        mCurrentPage, false);
            }
        });
    }

    @Override
    public void onRefresh() {
        mCurrentPage = 1;
        mVideosListPresenter.loadListData(TAG_LOG, Constants.EVENT_REFRESH_DATA, mCurrentVideosCategory, mCurrentPage,
                true);
    }

    @Override
    public void onLoadMore() {
        mCurrentPage++;
        mVideosListPresenter.loadListData(TAG_LOG, Constants.EVENT_LOAD_MORE_DATA, mCurrentVideosCategory, mCurrentPage, true);
    }

    @Override
    public void refreshListData(ResponseVideosListEntity responseVideosListEntity) {
        if (null != mSwipeRefreshLayout) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        if (null != responseVideosListEntity && null != responseVideosListEntity.getVideos() && !responseVideosListEntity.getVideos().isEmpty()) {
            if (null != mListViewAdapter) {
                mListViewAdapter.getDataList().clear();
                mListViewAdapter.getDataList().addAll(responseVideosListEntity.getVideos());
                mListViewAdapter.notifyDataSetChanged();
            }

            if (UriHelper.getInstance().calculateTotalPages(responseVideosListEntity.getTotal()) > mCurrentPage) {
                mListView.setCanLoadMore(true);
            } else {
                mListView.setCanLoadMore(false);
            }
        }
    }

    @Override
    public void addMoreListData(ResponseVideosListEntity responseVideosListEntity) {
        if (null != mListView) {
            mListView.onLoadMoreComplete();
        }

        if (null != responseVideosListEntity && null != responseVideosListEntity.getVideos() && !responseVideosListEntity.getVideos().isEmpty()) {
            if (null != mListViewAdapter) {
                mListViewAdapter.getDataList().addAll(responseVideosListEntity.getVideos());
                mListViewAdapter.notifyDataSetChanged();
            }

            if (UriHelper.getInstance().calculateTotalPages(responseVideosListEntity.getTotal()) > mCurrentPage) {
                mListView.setCanLoadMore(true);
            } else {
                mListView.setCanLoadMore(false);
            }
        }
    }

    @Override
    public void navigateToNewsDetail(int position, VideosListEntity entity) {
        Bundle extras = new Bundle();
        extras.putParcelable(PlayerActivity.INTENT_VIDEO_EXTRAS, entity);
        readyGo(PlayerActivity.class, extras);
    }
}
