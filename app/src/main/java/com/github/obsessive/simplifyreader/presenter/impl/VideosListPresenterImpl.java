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

package com.github.obsessive.simplifyreader.presenter.impl;

import android.content.Context;

import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.bean.ResponseVideosListEntity;
import com.github.obsessive.simplifyreader.bean.VideosListEntity;
import com.github.obsessive.simplifyreader.common.Constants;
import com.github.obsessive.simplifyreader.interactor.CommonListInteractor;
import com.github.obsessive.simplifyreader.interactor.impl.VideosListInteractorImpl;
import com.github.obsessive.simplifyreader.listeners.BaseMultiLoadedListener;
import com.github.obsessive.simplifyreader.presenter.VideosListPresenter;
import com.github.obsessive.simplifyreader.view.VideosListView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/4/9.
 * Description:
 */
public class VideosListPresenterImpl implements VideosListPresenter, BaseMultiLoadedListener<ResponseVideosListEntity> {

    private Context mContext = null;
    private VideosListView mVideosListView = null;
    private CommonListInteractor mCommonListInteractor = null;

    public VideosListPresenterImpl(Context context, VideosListView videosListView) {
        mContext = context;
        mVideosListView = videosListView;
        mCommonListInteractor = new VideosListInteractorImpl(this);
    }

    @Override
    public void onSuccess(int event_tag, ResponseVideosListEntity data) {
        mVideosListView.hideLoading();
        if (event_tag == Constants.EVENT_REFRESH_DATA) {
            mVideosListView.refreshListData(data);
        } else if (event_tag == Constants.EVENT_LOAD_MORE_DATA) {
            mVideosListView.addMoreListData(data);
        }
    }

    @Override
    public void onError(String msg) {
        mVideosListView.hideLoading();
        mVideosListView.showError(msg);
    }

    @Override
    public void onException(String msg) {
        mVideosListView.hideLoading();
        mVideosListView.showError(msg);
    }

    @Override
    public void loadListData(String requestTag, int event_tag, String keywords, int page, boolean isSwipeRefresh) {
        mVideosListView.hideLoading();
        if (!isSwipeRefresh) {
            mVideosListView.showLoading(mContext.getString(R.string.common_loading_message));
        }
        mCommonListInteractor.getCommonListData(requestTag, event_tag, keywords, page);
    }

    @Override
    public void onItemClickListener(int position, VideosListEntity entity) {
        mVideosListView.navigateToNewsDetail(position, entity);
    }
}