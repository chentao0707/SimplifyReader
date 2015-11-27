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
import android.util.Log;
import android.view.animation.Animation;

import com.github.obsessive.simplifyreader.interactor.SplashInteractor;
import com.github.obsessive.simplifyreader.interactor.impl.SplashInteractorImpl;
import com.github.obsessive.simplifyreader.presenter.Presenter;
import com.github.obsessive.simplifyreader.view.SplashView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/3/10.
 * Description:
 */
public class SplashPresenterImpl implements Presenter {
    private String TAG="SplashPresenterImpl";
    private Context mContext = null;
    private SplashView mSplashView = null;
    private SplashInteractor mSplashInteractor = null;

    public SplashPresenterImpl(Context context,SplashView splashView) {
        if (null == splashView) {
            throw new IllegalArgumentException("Constructor's parameters must not be Null");
        }

        mContext = context;
        mSplashView = splashView;
        mSplashInteractor = new SplashInteractorImpl();
    }

    @Override
    public void initialized() {
        mSplashView.initializeViews(mSplashInteractor.getVersionName(mContext),
                mSplashInteractor.getCopyright(mContext),
                mSplashInteractor.getBackgroundImageResID());

        Animation animation = mSplashInteractor.getBackgroundImageAnimation(mContext);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mSplashView.navigateToHomePage();
                Log.d(TAG, "onAnimationEnd: ");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mSplashView.animateBackgroundImage(animation);
    }
}
