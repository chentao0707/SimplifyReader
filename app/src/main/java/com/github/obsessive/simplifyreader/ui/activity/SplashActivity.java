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

package com.github.obsessive.simplifyreader.ui.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.obsessive.library.eventbus.EventCenter;
import com.github.obsessive.library.netstatus.NetUtils;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.presenter.Presenter;
import com.github.obsessive.simplifyreader.presenter.impl.SplashPresenterImpl;
import com.github.obsessive.simplifyreader.ui.activity.base.BaseActivity;
import com.github.obsessive.simplifyreader.view.SplashView;
import com.umeng.analytics.AnalyticsConfig;

import butterknife.InjectView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/3/9.
 * Description:
 */
public class SplashActivity extends BaseActivity implements SplashView {

    @InjectView(R.id.splash_image)
    ImageView mSplashImage;

    @InjectView(R.id.splash_version_name)
    TextView mVersionName;

    @InjectView(R.id.splash_copyright)
    TextView mCopyright;

    private Presenter mSplashPresenter = null;

    @Override
    protected void getBundleExtras(Bundle extras) {

    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_splash;
    }

    @Override
    protected void onEventComming(EventCenter eventCenter) {

    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected void initViewsAndEvents() {
        mSplashPresenter = new SplashPresenterImpl(this, this);
        mSplashPresenter.initialized();
    }

    @Override
    protected void onNetworkConnected(NetUtils.NetType type) {

    }

    @Override
    protected void onNetworkDisConnected() {

    }

    @Override
    protected boolean isApplyKitKatTranslucency() {
        return false;
    }

    @Override
    protected boolean isApplyStatusBarTranslucency() {
        return true;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    protected boolean toggleOverridePendingTransition() {
        return false;
    }

    @Override
    protected TransitionMode getOverridePendingTransitionMode() {
        return null;
    }

    @Override
    public void animateBackgroundImage(Animation animation) {
        mSplashImage.startAnimation(animation);
    }

    @Override
    public void initializeViews(String versionName, String copyright, int backgroundResId) {
        mCopyright.setText(copyright);
        mVersionName.setText(versionName);
        mSplashImage.setImageResource(backgroundResId);
    }

    @Override
    public void initializeUmengConfig() {
        AnalyticsConfig.setAppkey(this, "55018d77fd98c5901e000a09");
        AnalyticsConfig.setChannel("SimplifyReader");
    }

    @Override
    public void navigateToHomePage() {
        readyGoThenKill(HomeActivity.class);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
