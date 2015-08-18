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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.obsessive.library.eventbus.EventCenter;
import com.github.obsessive.library.netstatus.NetUtils;
import com.github.obsessive.library.utils.CommonUtils;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.bean.VideosListEntity;
import com.github.obsessive.simplifyreader.bean.VideosListUserEntity;
import com.github.obsessive.simplifyreader.common.Constants;
import com.github.obsessive.simplifyreader.presenter.VideosDetailPresenter;
import com.github.obsessive.simplifyreader.presenter.impl.VideosDetailPresenterImpl;
import com.github.obsessive.simplifyreader.utils.ImageLoaderHelper;
import com.github.obsessive.simplifyreader.view.VideosDetailView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.youku.player.base.BasePlayerActivity;
import com.youku.player.base.YoukuPlayer;
import com.youku.player.base.YoukuPlayerView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import me.gujun.android.taggroup.TagGroup;

public class PlayerActivity extends BasePlayerActivity implements VideosDetailView {


    public static final String INTENT_VIDEO_EXTRAS = "INTENT_VIDEO_EXTRAS";

    @InjectView(R.id.full_holder)
    YoukuPlayerView mYoukuPlayerView;

    @InjectView(R.id.player_back)
    ImageButton mBackBtn;

    @InjectView(R.id.player_title)
    TextView mTitle;

    @InjectView(R.id.player_title_bar)
    LinearLayout mTitleBar;

    @InjectView(R.id.player_view_count)
    TextView mViewCount;

    @InjectView(R.id.player_comment_count)
    TextView mCommentCount;

    @InjectView(R.id.player_favor_count)
    TextView mFavorCount;

    @InjectView(R.id.player_publish_time)
    TextView mPublishTime;

    @InjectView(R.id.player_user_name)
    TextView mUserName;

    @InjectView(R.id.player_user_avatar)
    ImageView mUserAvatar;

    @InjectView(R.id.player_tag_group)
    TagGroup mTagGroup;

    private String vid = "";
    private YoukuPlayer youkuPlayer;
    private VideosListEntity mExtras = null;
    private VideosDetailPresenter mVideosDetailPresenter = null;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getIntentData(intent);
        goPlay();
    }

    private void getIntentData(Intent intent) {
        if (intent != null) {
            mExtras = intent.getExtras().getParcelable(INTENT_VIDEO_EXTRAS);
            if (null != mExtras) {
                vid = mExtras.getId();
            }
        }

    }

    @Override
    public void setPadHorizontalLayout() {

    }

    @Override
    public void onInitializationSuccess(YoukuPlayer player) {
        addPlugins();
        youkuPlayer = player;
        goPlay();
    }

    private void goPlay() {
        youkuPlayer.playVideo(vid);
    }

    @Override
    public void onFullscreenListener() {
        mTitleBar.setVisibility(View.GONE);
    }

    @Override
    public void onSmallscreenListener() {
        mTitleBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(new EventCenter(Constants.EVENT_STOP_PLAY_MUSIC));
    }


    @Override
    protected void getBundleExtras(Bundle extras) {

    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_player;
    }

    @Override
    protected void onEventComming(EventCenter eventCenter) {

    }

    @Override
    protected View getLoadingTargetView() {
        return ButterKnife.findById(this, R.id.player_loading_target_view);
    }

    @Override
    protected void initViewsAndEvents() {
        getIntentData(getIntent());
        mYoukuPlayerView.initialize(this);

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (null == mExtras) {
            return;
        }

        if (!CommonUtils.isEmpty(mExtras.getTitle())) {
            mTitle.setText(mExtras.getTitle());
        }

        mViewCount.setText(getResources().getString(R.string.player_view_count) + mExtras.getView_count());
        mCommentCount.setText(getResources().getString(R.string.player_comment_count) + mExtras.getComment_count());
        mFavorCount.setText(getResources().getString(R.string.player_favor_count) + mExtras.getFavorite_count());
        mPublishTime.setText(getResources().getString(R.string.player_publish) + mExtras.getPublished());

        String tagAll = mExtras.getTags();
        String[] tags = tagAll.split(",");
        if (null != tags && tags.length != 0) {
            mTagGroup.setTags(tags);
        }

        mVideosDetailPresenter = new VideosDetailPresenterImpl(this);
        mVideosDetailPresenter.loadVideoUser(TAG_LOG, mExtras.getUser().getId());
    }

    @Override
    protected void onNetworkConnected(NetUtils.NetType type) {

    }

    @Override
    protected void onNetworkDisConnected() {

    }

    @Override
    protected boolean isApplyStatusBarTranslucency() {
        return false;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().post(new EventCenter(Constants.EVENT_START_PLAY_MUSIC));
    }

    @Override
    public void loadUser(VideosListUserEntity entity) {
        if (!CommonUtils.isEmpty(entity.getName())) {
            mUserName.setText(entity.getName());
        }

        if (!CommonUtils.isEmpty(entity.getAvatar_large())) {
            ImageLoader.getInstance().displayImage(entity.getAvatar_large(), mUserAvatar,
                    ImageLoaderHelper.getInstance(PlayerActivity.this).getDisplayOptions(100));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.right_in,R.anim.right_out);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.right_in, R.anim.right_out);
    }

    @Override
    public void showLoading(String msg) {
        toggleShowLoading(true, msg);
    }

    @Override
    public void hideLoading() {
        toggleShowLoading(false, null);
    }

    @Override
    public void showError(String msg) {
        toggleShowError(true, msg, null);
    }

    @Override
    public void showException(String msg) {
    }

    @Override
    public void showNetError() {

    }
}
