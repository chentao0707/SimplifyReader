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

import android.support.v4.view.ViewPager;
import android.view.View;

import com.github.obsessive.library.eventbus.EventCenter;
import com.github.obsessive.library.smartlayout.SmartTabLayout;
import com.github.obsessive.library.widgets.XViewPager;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.bean.BaseEntity;
import com.github.obsessive.simplifyreader.presenter.Presenter;
import com.github.obsessive.simplifyreader.presenter.impl.VideosContainerPresenterImpl;
import com.github.obsessive.simplifyreader.ui.activity.base.BaseFragment;
import com.github.obsessive.simplifyreader.ui.adpter.VideosContainerPagerAdapter;
import com.github.obsessive.simplifyreader.view.CommonContainerView;

import java.util.List;

import butterknife.InjectView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/3/12.
 * Description:
 */
public class VideosContainerFragment extends BaseFragment implements CommonContainerView {


    @InjectView(R.id.fragment_videos_pager)
    XViewPager mViewPager;

    @InjectView(R.id.fragment_videos_tab_smart)
    SmartTabLayout mSmartTabLayout;

    private Presenter mVideosContainerPresenter = null;

    @Override
    protected void onFirstUserVisible() {
        mVideosContainerPresenter = new VideosContainerPresenterImpl(mContext, this);
        mVideosContainerPresenter.initialized();
    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected void initViewsAndEvents() {

    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_videos;
    }

    @Override
    protected void onEventComming(EventCenter eventCenter) {

    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public void initializePagerViews(final List<BaseEntity> categoryList) {
        if (null != categoryList && !categoryList.isEmpty()) {
            mViewPager.setOffscreenPageLimit(categoryList.size());
            mViewPager.setAdapter(new VideosContainerPagerAdapter(getSupportFragmentManager(), categoryList));
            mSmartTabLayout.setViewPager(mViewPager);
            mSmartTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    VideosListFragment fragment = (VideosListFragment) mViewPager.getAdapter().instantiateItem(mViewPager, position);
                    fragment.onPageSelected(position, categoryList.get(position).getId());
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }
}
