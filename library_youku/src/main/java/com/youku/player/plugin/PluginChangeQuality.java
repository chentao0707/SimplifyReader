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

package com.youku.player.plugin;

import android.text.Html;
import android.view.View;

import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.Profile;
import com.youku.player.ui.interf.IMediaPlayerDelegate;

public class PluginChangeQuality extends PluginPayTip {
	private int mNextQuality;
	private boolean isClosed;
	private boolean isLoading;

	public PluginChangeQuality(YoukuBasePlayerActivity context,
			IMediaPlayerDelegate mediaPlayerDelegate) {
		super(context, mediaPlayerDelegate);
	}

	@Override
	public void onNotifyChangeVideoQuality() {
		if (!MediaPlayerConfiguration.getInstance().showChangeQualityTip()
				|| (mMediaPlayerDelegate != null && mMediaPlayerDelegate.isADShowing))
			return;
		showChangeQualityTip();
	}

	private void showChangeQualityTip() {
		if (!mActivity.isFinishing())
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					isLoading = true;
					if (mMediaPlayerDelegate != null
							&& mMediaPlayerDelegate.videoInfo != null
							&& mContainerView.getVisibility() != View.VISIBLE
							&& mActivity.canShowPluginChangeQuality()
							&& !isClosed && !isHide) {
						mArrowButton.setVisibility(View.GONE);
						int quality = mMediaPlayerDelegate.videoInfo
								.getCurrentQuality();
						if (quality == Profile.VIDEO_QUALITY_SD)
							return;
						String str = "";
						switch (quality) {
						case Profile.VIDEO_QUALITY_HD:
							mNextQuality = Profile.VIDEO_QUALITY_SD;
							str = "标清模式";
							break;
						case Profile.VIDEO_QUALITY_HD2:
							mNextQuality = Profile.VIDEO_QUALITY_HD;
							str = "高清模式";
							break;
						case Profile.VIDEO_QUALITY_HD3:
							mNextQuality = Profile.VIDEO_QUALITY_HD2;
							str = "超清模式";
							break;
						}
						mTipTextView.setText(Html
								.fromHtml("您当前的网络状况不佳<br>建议<font color=#15a4ff>点击切换</font>为"
										+ str));
						show();
					}
				}
			});
	}

	@Override
	protected void onOkClick() {
		if (mMediaPlayerDelegate != null) {
			mMediaPlayerDelegate.pluginManager.onLoading();
			mMediaPlayerDelegate.changeVideoQuality(mNextQuality);
		}
	}

	@Override
	public void onRealVideoStart() {
		if (isShowing())
			close(null);
		isClosed = false;
		isLoading = false;
	}

	@Override
	public void onLoadedListener() {
		if (isShowing())
			close(null);
		isClosed = false;
		isLoading = false;
	}

	@Override
	public boolean isShowing() {
		return mContainerView.getVisibility() == View.VISIBLE;
	}

	@Override
	protected void onCloseClick() {
		isClosed = true;
	}

	@Override
	public void onRelease() {
		super.onRelease();
		isClosed = false;
		isLoading = false;
	}

	@Override
	public boolean onErrorListener(int what, int extra) {
		isClosed = false;
		return super.onErrorListener(what, extra);
	}

	public void hide() {
		isHide = true;
		if (isShowing())
			close(null);
	}

	public void unHide() {
		isHide = false;
		if (isLoading)
			showChangeQualityTip();
	}
}
