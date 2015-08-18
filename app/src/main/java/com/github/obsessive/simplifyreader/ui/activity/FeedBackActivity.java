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
import android.view.View;

import com.github.obsessive.library.eventbus.EventCenter;
import com.github.obsessive.library.netstatus.NetUtils;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.ui.activity.base.BaseSwipeBackActivity;
import com.umeng.fb.fragment.FeedbackFragment;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    15/7/22
 * Description:
 */
public class FeedBackActivity extends BaseSwipeBackActivity {

    private String mConversationId = null;
    private FeedbackFragment mFeedbackFragment;

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        mFeedbackFragment.refresh();
    }

    @Override
    protected boolean isApplyKitKatTranslucency() {
        return true;
    }

    @Override
    protected void getBundleExtras(Bundle extras) {
        mConversationId = extras.getString(FeedbackFragment.BUNDLE_KEY_CONVERSATION_ID);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.common_fragment_placeholder;
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
        mFeedbackFragment = FeedbackFragment.newInstance(mConversationId);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.common_fragment_placeholder_container, mFeedbackFragment)
                .commit();
    }

    @Override
    protected void onNetworkConnected(NetUtils.NetType type) {

    }

    @Override
    protected void onNetworkDisConnected() {

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
        return true;
    }

    @Override
    protected TransitionMode getOverridePendingTransitionMode() {
        return TransitionMode.RIGHT;
    }
}
