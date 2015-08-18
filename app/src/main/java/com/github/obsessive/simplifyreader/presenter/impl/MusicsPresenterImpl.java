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

import com.github.obsessive.simplifyreader.bean.MusicsListEntity;
import com.github.obsessive.simplifyreader.bean.ResponseMusicsListentity;
import com.github.obsessive.simplifyreader.common.Constants;
import com.github.obsessive.simplifyreader.interactor.MusicsInteractor;
import com.github.obsessive.simplifyreader.interactor.impl.MusicsInteracotrImpl;
import com.github.obsessive.simplifyreader.listeners.BaseMultiLoadedListener;
import com.github.obsessive.simplifyreader.presenter.MusicsPresenter;
import com.github.obsessive.simplifyreader.view.MusicsView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/4/16.
 * Description:
 */
public class MusicsPresenterImpl implements MusicsPresenter, BaseMultiLoadedListener<ResponseMusicsListentity> {

    private Context mContext = null;
    private MusicsView mMusicsView = null;
    private MusicsInteractor mMusicsInteractor = null;

    public MusicsPresenterImpl(Context context, MusicsView musicsView) {
        mContext = context;
        mMusicsView = musicsView;
        mMusicsInteractor = new MusicsInteracotrImpl(this);
    }

    @Override
    public void loadListData(String requestTag, String keywords, int event_tag) {
        mMusicsInteractor.getMusicListData(requestTag, keywords, event_tag);
    }

    @Override
    public void onNextClick() {
        mMusicsView.playNextMusic();
    }

    @Override
    public void onPrevClick() {
        mMusicsView.playPrevMusic();
    }

    @Override
    public void onStartPlay() {
        mMusicsView.startPlayMusic();
    }

    @Override
    public void onPausePlay() {
        mMusicsView.pausePlayMusic();
    }

    @Override
    public void onRePlay() {
        mMusicsView.rePlayMusic();
    }

    @Override
    public void seekTo(int position) {
        mMusicsView.seekToPosition(position);
    }

    @Override
    public void onStopPlay() {
        mMusicsView.stopPlayMusic();
    }

    @Override
    public void refreshPageInfo(MusicsListEntity entity, int totalDuration) {
        mMusicsView.refreshPageInfo(entity, totalDuration);
    }

    @Override
    public void refreshProgress(int progress) {
        mMusicsView.refreshPlayProgress(progress);
    }

    @Override
    public void refreshSecondProgress(int progress) {
        mMusicsView.refreshPlaySecondProgress(progress);
    }

    @Override
    public void onSuccess(int event_tag, ResponseMusicsListentity data) {
        if (event_tag == Constants.EVENT_REFRESH_DATA) {
            mMusicsView.refreshMusicsList(data);
        } else if (event_tag == Constants.EVENT_LOAD_MORE_DATA) {
            mMusicsView.addMoreMusicsList(data);
        }

    }

    @Override
    public void onError(String msg) {
        mMusicsView.showError(msg);
    }

    @Override
    public void onException(String msg) {
        mMusicsView.showError(msg);
    }
}
