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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baseproject.utils.UIUtils;
import com.youku.player.base.GoplayException;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.service.DisposableHttpTask;
import com.youku.player.ui.R;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DetailUtil;

public class PluginInvestigate extends PluginOverlay implements DetailMessage {
	LayoutInflater mLayoutInflater;
	View containerView;
	YoukuBasePlayerActivity mActivity;
	IMediaPlayerDelegate mediaPlayerDelegate;
	LinearLayout mInvestigate;
	RelativeLayout mAnimator;
	LinearLayout mClose;
	TextView mText;
	LinearLayout mArrowRight;

	private AdvInfo mAdvInfo;
	private InvestCountDownTimer mTimer;
	private int mSavedCount = DEFAULT_DURATION;
	private String mName;
	private String mClickURL;
	private boolean isOpen = false;
	private boolean isHide = false;

	private static final int DEFAULT_DURATION = 120; // s
	private static final String DEFAULT_NAME = "参与调研";
	private static final int MARGIN_BOTTOM_VERTICAL = 50; // DP
	private static final int MARGIN_BOTTOM_HORIZONTAL = 90; // DP
	private static final int MARGIN_BOTTOM_VERTICAL_PAD = 130; // DP
	private static final int MARGIN_BOTTOM_HORIZONTAL_PAD = 160; // DP

	public PluginInvestigate(YoukuBasePlayerActivity context,
			IMediaPlayerDelegate mediaPlayerDelegate) {
		super(context, mediaPlayerDelegate);
		this.mediaPlayerDelegate = mediaPlayerDelegate;
		mActivity = context;
		mLayoutInflater = LayoutInflater.from(context);
		init(context);
	}

	private void init(Context context) {
		containerView = mLayoutInflater.inflate(
				R.layout.yp_player_investigate_youku, null);
		addView(containerView);
		mAnimator = (RelativeLayout) containerView
				.findViewById(R.id.yp_investigate_holder);
		mInvestigate = (LinearLayout) containerView
				.findViewById(R.id.yp_investigate);
		mClose = (LinearLayout) containerView
				.findViewById(R.id.investigate_close);
		mClose.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				close();
			}
		});
		mText = (TextView) containerView.findViewById(R.id.yp_investigate_text);
		mText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				release();
				try {
					Intent intent = new Intent("com.youku.player.api.AD_PROCESS");
					intent.putExtra("url", mClickURL);
					mActivity.startActivity(intent);
				} catch (Exception e) {
				}
			}
		});
		mArrowRight = (LinearLayout) containerView
				.findViewById(R.id.yp_investigate_arrowright);
		mArrowRight.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				release();
				try {
					Intent intent = new Intent("com.youku.player.api.AD_PROCESS");
					intent.putExtra("url", mClickURL);
					mActivity.startActivity(intent);
				} catch (Exception e) {
				}
			}
		});
		containerView.setVisibility(View.GONE);
	}

	@Override
	public void onBufferingUpdateListener(int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCompletionListener() {
		// release();
	}

	@Override
	public boolean onErrorListener(int what, int extra) {
		// release();
		return false;
	}

	@Override
	public void OnPreparedListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnSeekCompleteListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnVideoSizeChangedListener(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnTimeoutListener() {
	}

	@Override
	public void OnCurrentPositionChangeListener(int currentPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoadedListener() {
		// containerView.setVisibility(View.GONE);
	}

	@Override
	public void onLoadingListener() {
		// hide();
	}

	@Override
	public void onNotifyChangeVideoQuality() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRealVideoStarted() {
		// containerView.setVisibility(View.VISIBLE);

	}

	@Override
	public void onUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClearUpDownFav() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFavor() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnFavor() {
		// TODO Auto-generated method stub

	}

	@Override
	public void newVideo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVolumnUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVolumnDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMute(boolean mute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPause() {
		hide();
	}

	@Override
	public void onVideoChange() {
	}

	@Override
	public void onVideoInfoGetting() {
		hide();
	}

	@Override
	public void onVideoInfoGetted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRealVideoStart() {
	}

	@Override
	public void onPlayNoRightVideo(GoplayException e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayReleateNoRightVideo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onADplaying() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onVideoInfoGetFail(boolean needRetry) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVisible(boolean visible) {
		/*
		 * if (visible && !mediaPlayerDelegate.isPause &&
		 * !mediaPlayerDelegate.isComplete) {
		 * //containerView.setVisibility(View.VISIBLE); } else {
		 * containerView.setVisibility(View.GONE); }
		 */
	}

	@Override
	public void back() {
		// TODO Auto-generated method stub

	}

	public void updateLayout() {
		if (isOpen) {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			mActivity.getWindowManager().getDefaultDisplay()
					.getMetrics(displayMetrics);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mInvestigate
					.getLayoutParams();
			if (!mMediaPlayerDelegate.isFullScreen) {
				if (!UIUtils.isTablet(mActivity)) {
					params.bottomMargin = (int) (displayMetrics.density * MARGIN_BOTTOM_VERTICAL);
				} else {
					params.bottomMargin = (int) (displayMetrics.density * MARGIN_BOTTOM_VERTICAL_PAD);
				}
			} else {
				if (!UIUtils.isTablet(mActivity)) {
					params.bottomMargin = (int) (displayMetrics.density * MARGIN_BOTTOM_HORIZONTAL);
				} else {
					params.bottomMargin = (int) (displayMetrics.density * MARGIN_BOTTOM_HORIZONTAL_PAD);
				}
			}
			params.rightMargin = 0;
			mInvestigate.setLayoutParams(params);
			mInvestigate.requestLayout();
		}
	}

	public synchronized void updateLayoutWithVirtualKey(boolean isVirtualKeyDisplay) {
		if (isOpen) {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			mActivity.getWindowManager().getDefaultDisplay()
					.getMetrics(displayMetrics);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mInvestigate
					.getLayoutParams();
			params.rightMargin = 0;
			if (!mMediaPlayerDelegate.isFullScreen) {
				if (!UIUtils.isTablet(mActivity)) {
					params.bottomMargin = (int) (displayMetrics.density * MARGIN_BOTTOM_VERTICAL);
				} else {
					params.bottomMargin = (int) (displayMetrics.density * MARGIN_BOTTOM_VERTICAL_PAD);
				}
			} else {
				if (!UIUtils.isTablet(mActivity)) {
					params.bottomMargin = (int) (displayMetrics.density * MARGIN_BOTTOM_HORIZONTAL);
					if (isVirtualKeyDisplay) {
						params.rightMargin = DetailUtil
								.getFullScreentNavigationBarHeight(mActivity);
					}
				} else {
					params.bottomMargin = (int) (displayMetrics.density * MARGIN_BOTTOM_HORIZONTAL_PAD);
				}
			}
			mInvestigate.setLayoutParams(params);
			mInvestigate.requestLayout();
		}
	}

	public int getNavigationBarHeight(Activity context) {
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
		    return (resources.getDimensionPixelSize(resourceId));
		}
		return 0;
	}

	public void show() {
		if (!isHide && isOpen) {
			if (mMediaPlayerDelegate != null
					&& !mMediaPlayerDelegate.isAdvShowFinished()) {
				return;
			}

			if (mActivity!= null && mActivity.isImageADShowing) {
				return;
			}

			if (containerView.getVisibility() == View.VISIBLE) {
				return;
			}
			updateLayout();
			mActivity.updatePlugin(PLUGIN_SHOW_INVESTIGATE);

			Animation inAnimation = AnimationUtils.loadAnimation(mActivity,
					R.anim.yp_slide_in_from_right);
			mAnimator.startAnimation(inAnimation);
			containerView.setVisibility(View.VISIBLE);
			startTimer();
			disposeVC();
		}
	}

	public void hide() {
		isHide = true;
		if (containerView.getVisibility() == View.VISIBLE) {
			containerView.setVisibility(View.GONE);
			pauseTimer();
		}
	}

	public void unHide() {
		isHide = false;
		show();
	}

	private void close() {
		Animation outAnimation = AnimationUtils.loadAnimation(mActivity,
				R.anim.yp_slide_out_to_right);
		mAnimator.startAnimation(outAnimation);
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		containerView.setVisibility(View.GONE);
		mSavedCount = 0;
		mAdvInfo = null;
		isOpen = false;
	}

	public void release() {
		if (isOpen) {
			isOpen = false;
			if (containerView.getVisibility() == View.VISIBLE) {
				containerView.setVisibility(View.GONE);
			}
			mSavedCount = 0;
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
			mAdvInfo = null;
		}
	}

	public void start(VideoAdvInfo videoAdvInfo) {
		if (mMediaPlayerDelegate != null && videoAdvInfo != null) {
			int size = videoAdvInfo.VAL.size();
			if (size == 0) {
				return;
			}
			for (int i = 0; i < size; i++) {
				if (videoAdvInfo.VAL.get(i).VSC != null
						&& !videoAdvInfo.VAL.get(i).VSC.equalsIgnoreCase("")) {
					mAdvInfo = videoAdvInfo.VAL.get(i);
					mClickURL = mAdvInfo.VSC;
					int duration = mAdvInfo.VP;
					mSavedCount = duration > 0 ? duration : DEFAULT_DURATION;
					String name = mAdvInfo.VN;
					if (name != null && !name.equalsIgnoreCase("")) {
						mName = name;
					} else {
						mName = DEFAULT_NAME;
					}
					mText.setText(mName);
					isOpen = true;
					isHide = false;
					return;
				}
			}
		}
		/*
		 * 测试使用
		mName = DEFAULT_NAME;
		mText.setText(mName);
		mSavedCount = DEFAULT_DURATION;
		mClickURL = "http://www.baidu.com";
		isOpen = true;
		*/
	}

	private void disposeVC() {
		if (mAdvInfo != null
				&& mAdvInfo.VT != null
				&& (mAdvInfo.VT.equalsIgnoreCase("1") || mAdvInfo.VT
						.equalsIgnoreCase("2")) && mAdvInfo.VC != null
				&& !mAdvInfo.VC.equalsIgnoreCase("")) {
			new DisposableHttpTask("PlayFlow", mAdvInfo.VC, "VC").start();
			mAdvInfo.VC = "";
		}
	}

	private void pauseTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	private void startTimer() {
		if (mSavedCount > 0 && isOpen) {
			mTimer = new InvestCountDownTimer(mSavedCount * 1000, 1000);
			mTimer.start();
		}
	}

	private class InvestCountDownTimer extends CountDownTimer {

		public InvestCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			mSavedCount = (int) millisInFuture / 1000;
		}

		@Override
		public void onFinish() {
			mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					close();
				}
			});
		}

		@Override
		public void onTick(long millisUntilFinished) {
			float time = (float) millisUntilFinished / 1000;
			int count = Math.round(time);
			if (mSavedCount != count && count > 0) {
				mSavedCount = count;
			}
		}
	}
	
	@Override
	public boolean isShowing() {
		return containerView.getVisibility() == View.VISIBLE;
	}
}
