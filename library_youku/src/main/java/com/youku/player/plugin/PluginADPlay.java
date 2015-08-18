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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.UIUtils;
import com.baseproject.utils.Util;
import com.youdo.AdApplicationContext;
import com.youdo.AdManager;
import com.youdo.XAdSDKResource;
import com.youdo.ad.interfaces.IAdApplicationContext;
import com.youdo.ad.interfaces.IAdContants;
import com.youdo.ad.interfaces.IAdManager;
import com.youdo.events.IXYDEvent;
import com.youdo.events.IXYDEventListener;
import com.youku.player.ad.AdForward;
import com.youku.player.base.GoplayException;
import com.youku.player.base.Plantform;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.Stat;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.service.DisposableHttpTask;
import com.youku.player.ui.R;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DetailUtil;
import com.youku.player.util.DisposableStatsUtils;

public class PluginADPlay extends PluginOverlay implements DetailMessage {

	LayoutInflater mLayoutInflater;
	View containerView;
	TextView endPage;
//	TextView ad_more;
	YoukuBasePlayerActivity mActivity;
	IMediaPlayerDelegate mediaPlayerDelegate;
	private TextView mCountUpdateTextView;
	private ImageView mSwitchPlayer;
	// youku控件
	private LinearLayout mCountUpdateWrap;
//	private TextView mAdSkip;
	private LinearLayout mAdSkipBlank;

	// 去详情的父view
	private View mSwitchParent;
	protected String TAG = "PluginADPlay";
	private View seekLoadingContainerView;
	private ImageButton play_adButton;

	public static final int ADMORE_BACKGROUND_COLOR_YOUKU = 0xcc292929;
	public static final int ADMORE_BACKGROUND_COLOR_TUDOU = 0xffff6600;

	public static int sAdMoreBackgroundColor = ADMORE_BACKGROUND_COLOR_YOUKU;

	private RelativeLayout mAdPageHolder = null;
	// interactive ad
	private static final int INTERACTIVE_AD_TIMEOUT = 5;//s
	private RelativeLayout mInteractiveAdContainer = null;
	private RelativeLayout mInteractiveAdGoFull;
	private IAdApplicationContext mAdApplicationContext;
	private IAdManager mAdManager = null;
	private org.json.JSONObject mCurrentAdData;
	private InteractiveAdListener mInteractiveAdListener = null;
	private boolean isInteractiveAdShow = false;
	private boolean isInteractiveAdHide = false;
	private String mInteractiveAdVideoRs = null; //互动广告对应视频素材

	public PluginADPlay(YoukuBasePlayerActivity context,
			IMediaPlayerDelegate mediaPlayerDelegate) {
		super(context, mediaPlayerDelegate);
		this.mediaPlayerDelegate = mediaPlayerDelegate;
		mActivity = context;
		mLayoutInflater = LayoutInflater.from(context);
		init(context);
	}

	private void init(Context context) {
		if (Profile.PLANTFORM == Plantform.YOUKU) {
			containerView = mLayoutInflater.inflate(
					R.layout.yp_player_ad_youku, null);
		} else {
			containerView = mLayoutInflater.inflate(
					R.layout.yp_player_ad_tudou, null);
		}
		addView(containerView);
		mCountUpdateTextView = (TextView) containerView
				.findViewById(R.id.my_ad_count);
		if (Profile.PLANTFORM == Plantform.YOUKU) {
			mAdPageHolder = (RelativeLayout) containerView
					.findViewById(R.id.ad_page_holder);
			mInteractiveAdContainer = (RelativeLayout) containerView
					.findViewById(R.id.interactive_ad_container);
			mInteractiveAdGoFull = (RelativeLayout) containerView
					.findViewById(R.id.interactive_ad_gofull_layout);
			mInteractiveAdGoFull.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					isInteractiveAdHide = false;
					if (mAdApplicationContext != null) {
						mAdApplicationContext.show();
					}
					mInteractiveAdGoFull.setVisibility(View.GONE);
					mInteractiveAdContainer.setVisibility(View.VISIBLE);
					if (mAdPageHolder != null) {
						mAdPageHolder.setVisibility(View.GONE);
					}
					mActivity.goFullScreen();
					mActivity.setOrientionDisable();
				}
				
			});
			mCountUpdateWrap = (LinearLayout) containerView
					.findViewById(R.id.my_ad_count_wrap);
			mAdSkipBlank = (LinearLayout) containerView
					.findViewById(R.id.my_ad_blank);
/*			mAdSkip = (TextView) containerView.findViewById(R.id.my_ad_skip);
			mAdSkip.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent();
						intent.setClassName(mActivity.getPackageName(),
								"com.youku.phone.vip.activity.VipProductActivity");
						intent.putExtra("from", 1001);
						intent.putExtra("isVip", false);
						intent.putExtra("video_id",
								mediaPlayerDelegate.videoInfo.getVid());
						intent.putExtra("isFromLocal",
								mediaPlayerDelegate.videoInfo.playType
										.equals(StaticsUtil.PLAY_TYPE_LOCAL));
						intent.putExtra("playlist_id",
								mediaPlayerDelegate.videoInfo.playlistId);
						mActivity.startActivity(intent);
					} catch (Exception e) {

					} finally {
						mActivity.finish();
					}
				}
			});*/
		}
		mSwitchPlayer = (ImageView) containerView
				.findViewById(R.id.gofullscreen);
		mSwitchParent = containerView.findViewById(R.id.gofulllayout);
/*		ad_more = (TextView) containerView.findViewById(R.id.ad_more);
		ad_more.setBackgroundColor(sAdMoreBackgroundColor);*/
		play_adButton = (ImageButton) containerView
				.findViewById(R.id.ib_detail_play_control_ad_play);
		play_adButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Util.hasInternet()
						&& !Util.isWifi()
						&& !PreferenceManager.getDefaultSharedPreferences(
								mActivity).getBoolean("allowONline3G", true)) {
					Toast.makeText(mActivity, "请设置3g/2g允许播放", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				startPlay();
				play_adButton.setVisibility(View.GONE);
			}
		});
		mSwitchParent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mediaPlayerDelegate.isFullScreen) {
					mActivity.goSmall();
					if (Profile.PLANTFORM == Plantform.TUDOU) {
						mSwitchPlayer
								.setImageResource(R.drawable.plugin_ad_gofull_tudou);
					} else {
						mSwitchPlayer
								.setImageResource(R.drawable.plugin_ad_gofull_youku);
					}
				} else {
					mActivity.goFullScreen();
					if (Profile.PLANTFORM == Plantform.TUDOU) {
						mSwitchPlayer
								.setImageResource(R.drawable.plugin_ad_gosmall_tudou);
					} else {
						mSwitchPlayer
								.setImageResource(R.drawable.plugin_ad_gosmall_youku);
					}
				}
			}
		});

		seekLoadingContainerView = containerView
				.findViewById(R.id.seek_loading_bg);
		initSeekLoading();
	}

	private void startPlay() {
		if (null == mMediaPlayerDelegate)
			return;
		if (!mMediaPlayerDelegate.isAdvShowFinished()) {
			((YoukuBasePlayerActivity) mActivity).startPlay();
		} else {
			mMediaPlayerDelegate.start();
		}
	}

	@Override
	public void onBufferingUpdateListener(int percent) {

	}

	@Override
	public void onCompletionListener() {

	}

	@Override
	public boolean onErrorListener(int what, int extra) {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				containerView.setVisibility(View.GONE);
			}
		});
		return false;
	}

	@Override
	public void OnPreparedListener() {

	}

	@Override
	public void OnSeekCompleteListener() {

	}

	@Override
	public void OnVideoSizeChangedListener(int width, int height) {

	}

	@Override
	public void OnTimeoutListener() {

	}

	@Override
	public void OnCurrentPositionChangeListener(int currentPosition) {
	}

	@Override
	public void onLoadedListener() {
		((Activity) mActivity).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				play_adButton.setVisibility(View.GONE);
				hideLoading();
			}
		});
	}

	@Override
	public void onLoadingListener() {
		((Activity) mActivity).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showLoading();
			}
		});
	}

	@Override
	public void onUp() {

	}

	@Override
	public void onDown() {

	}

	@Override
	public void onFavor() {
	}

	@Override
	public void onUnFavor() {
	}

	@Override
	public void newVideo() {
	}

	@Override
	public void onVolumnUp() {
	}

	@Override
	public void onVolumnDown() {
	}

	@Override
	public void onMute(boolean mute) {
	}

	@Override
	public void onVideoChange() {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mCountUpdateTextView.setText("");
				play_adButton.setVisibility(View.GONE);
				mSwitchPlayer.setVisibility(View.GONE);
//				ad_more.setVisibility(View.GONE);
				mSwitchParent.setVisibility(View.GONE);
                if (Profile.PLANTFORM == Plantform.YOUKU) {
//                	mAdSkip.setVisibility(View.GONE);
                	mAdSkipBlank.setVisibility(View.GONE);
                	mCountUpdateWrap.setVisibility(View.GONE);
                }
			}
		});
	}

	boolean isADPluginShowing = false;

	@Override
	public void onVideoInfoGetting() {
		if (isADPluginShowing) {
			/*
			Track.onError(mActivity, mediaPlayerDelegate.nowVid,
					Profile.GUID, mediaPlayerDelegate.videoInfo.playType,
					PlayCode.VIDEO_ADV_RETURN);
					*/
			mActivity.interuptAD();
		}
	}

	@Override
	public void onVideoInfoGetted() {
	}

	@Override
	public void onVideoInfoGetFail(boolean needRetry) {
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			isADPluginShowing = true;
			containerView.setVisibility(View.VISIBLE);
		} else {
			isADPluginShowing = false;
			containerView.setVisibility(View.GONE);
		}
	}

	public void notifyUpdate(int count) {

		if (count <= 0) {
			mCountUpdateTextView.setText("");
			mCountUpdateTextView.setVisibility(View.GONE);
			if (Profile.PLANTFORM == Plantform.YOUKU) {
				mCountUpdateWrap.setVisibility(View.GONE);
			}
			return;
		}
		if (mCountUpdateTextView != null) {
			
			if (Profile.PLANTFORM != Plantform.YOUKU) {
				StringBuilder mytext = new StringBuilder("广告剩余时间");
				mytext.append(count).append("秒");
				mCountUpdateTextView.setText(mytext);
				mCountUpdateTextView.setVisibility(View.VISIBLE);
			} else {
				String str = String.valueOf(count);
				mCountUpdateTextView.setText(str);
				mCountUpdateTextView.setVisibility(View.VISIBLE);
				mCountUpdateWrap.setVisibility(View.VISIBLE);
			}
			
		}

		int visibility = mediaPlayerDelegate.isPlayLocalType() ? View.GONE : View.VISIBLE;
		// TODO:要保持“广告剩余时间”和“全屏”,“详细了解”的同步显示，需要把三者处理显示的时机要一致。
		// 目前onStartPlayAD中没有倒计时的参数，故暂时放在这里处理。这些应该在onStartPlayAD方法中处理。
		mSwitchParent.setVisibility(visibility);
		mSwitchPlayer.setVisibility(visibility);

		if (mediaPlayerDelegate.videoInfo.videoAdvInfo != null) {
			AdvInfo advInfo = getAdvInfo();
			if (advInfo == null) {
				Logger.e("PlayFlow", "PlugiADPlay->notifyUpdate    advInfo = null,   return");
				return;
			}

/*			if (TextUtils.isEmpty(advInfo.CU)) {
				ad_more.setVisibility(View.GONE);
			} else {
				if (AdForward.YOUKU_VIDEO == advInfo.CUF) {
					ad_more.setText(R.string.playersdk_ad_descrip_play_youku);
				} else {
					ad_more.setText(R.string.playersdk_ad_descrip_youku);
				}
				ad_more.setVisibility(View.VISIBLE);
			}*/
		}
	}

	@Override
	public void onPluginAdded() {
		super.onPluginAdded();
		if (mediaPlayerDelegate.isFullScreen) {
			if (Profile.PLANTFORM == Plantform.TUDOU) {
				mSwitchPlayer
						.setImageResource(R.drawable.plugin_ad_gosmall_tudou);
			} else {
				mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gosmall_youku);
			}
		} else {
			if (Profile.PLANTFORM == Plantform.TUDOU) {
				mSwitchPlayer
						.setImageResource(R.drawable.plugin_ad_gofull_tudou);
			} else {
				mSwitchPlayer.setImageResource(R.drawable.plugin_ad_gofull_youku);
			}
		}
		if (mediaPlayerDelegate.videoInfo.videoAdvInfo != null) {
			final VideoAdvInfo adInfo = mediaPlayerDelegate.videoInfo.videoAdvInfo;

/*			ad_more.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (adInfo.VAL.size() <= 0) {
						return;
					}

					AdvInfo advInfo = adInfo.VAL.get(0);
					if (advInfo == null) {
						return;
					}
					String url = advInfo.CU;
					Logger.e("PlayFlow", "点击url-->" + url);

					if (url == null || TextUtils.getTrimmedLength(url) <= 0) {
						return;
					}
					DisposableStatsUtils.disposeCUM(advInfo);
					new AdvClickProcessor().processAdvClick(mActivity, url, advInfo.CUF);
				}
			});*/
		}
		if (UIUtils.hasKitKat()) {
			mActivity.hideSystemUI(this);
		}
		mActivity.setPluginHolderPaddingZero();
	}

	/**
	 * 获取广告信息
	 * 
	 * @return
	 */
	private AdvInfo getAdvInfo() {
		try {
			return mediaPlayerDelegate.videoInfo.videoAdvInfo.VAL.get(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 发送广告统计信息
	 * 
	 * @param stat
	 */
	private void sendStat(Stat stat) {
		new DisposableHttpTask(stat.U).start();
	}

	private void initSeekLoading() {
		if (null == seekLoadingContainerView)
			return;
		playLoadingBar = (SeekBar) seekLoadingContainerView
				.findViewById(R.id.loading_seekbar);
		if (null != playLoadingBar)
			playLoadingBar
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {

						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {

						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							if (fromUser) {
								//Track.setTrackPlayLoading(false);
								return;
							} else {
								seekBar.setProgress(progress);
							}

						}
					});
	}

	private int seekcount = 0;

	public void showLoading() {

		if (null != seekLoadingContainerView) {
			if (seekLoadingContainerView.getVisibility() == View.GONE) {
				seekLoadingContainerView.setVisibility(View.VISIBLE);
				seekcount = 0;
				seekHandler.sendEmptyMessageDelayed(0, 50);

			}
			if (null != mMediaPlayerDelegate
					&& mMediaPlayerDelegate.getCurrentPosition() > 1000) {
				seekendHandler.sendEmptyMessageDelayed(0, 50);
				seekLoadingContainerView.setBackgroundResource(0);
			} else {
				seekLoadingContainerView
						.setBackgroundResource(R.drawable.bg_play);
			}
		}
	}

	public void hideLoading() {
		((Activity) mActivity).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (null != seekLoadingContainerView) {
					seekLoadingContainerView.setVisibility(View.GONE);
					playLoadingBar.setProgress(0);
				}
				if (null != seekHandler)
					seekHandler.removeCallbacksAndMessages(null);
			}
		});
	}

	private Handler seekHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (seekcount < 50) {
				seekcount++;
				playLoadingBar.setProgress(seekcount);
				Thread temp = new Thread(new Runnable() {

					@Override
					public void run() {
						seekHandler.sendEmptyMessageDelayed(0, 50);
					}
				});
				temp.run();
			} else {
				playLoadingBar.setProgress(50);
			}

		}

	};

	private SeekBar playLoadingBar;
	private Handler seekendHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (seekcount < 100) {
				seekcount++;
				playLoadingBar.setProgress(seekcount);
				Thread temp = new Thread(new Runnable() {

					@Override
					public void run() {
						seekHandler.sendEmptyMessageDelayed(0, 10);
					}
				});
				temp.run();
			}

		}

	};

	@Override
	public void onNotifyChangeVideoQuality() {

	}

	@Override
	public void onRealVideoStart() {
	}

	@Override
	public void onADplaying() {
	}

	@Override
	public void onRealVideoStarted() {

	}

	@Override
	public void onStart() {

	}

	@Override
	public void onClearUpDownFav() {

	}

	@Override
	public void onPause() {

	}

	public void showPlayIcon() {
		play_adButton.setVisibility(View.VISIBLE);
	}

	@Override
	public void back() {
	}

	@Override
	public void onPlayNoRightVideo(GoplayException e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayReleateNoRightVideo() {
		// TODO Auto-generated method stub

	}

	public static void setAdMoreBackgroundColor(boolean isTudouPlatform) {
		if (isTudouPlatform) {
			sAdMoreBackgroundColor = ADMORE_BACKGROUND_COLOR_TUDOU;
			return;
		}
		sAdMoreBackgroundColor = ADMORE_BACKGROUND_COLOR_YOUKU;
	}

	public boolean isCountUpdateVisible() {
		if (mCountUpdateTextView != null) {
			return mCountUpdateTextView.getVisibility() == View.VISIBLE ? true
					: false;
		}
		return false;
	}

	public void setSkipVisible(boolean visible) {
/*		if (MediaPlayerConfiguration.getInstance().showSkipAdButton() && mAdSkip != null) {
			mAdSkip.setVisibility(visible ? View.VISIBLE : View.GONE);
			if (mAdSkipBlank != null) {
				mAdSkipBlank.setVisibility(visible ? View.VISIBLE : View.GONE);
			}
		}*/
	}

	/**
	 * 互动广告
	 * 
	 * @return
	 */
	public void startInteractiveAd(String brs, int count) {
		if (brs == null || brs.equalsIgnoreCase("")
				|| mInteractiveAdContainer == null) {
			return;
		}

		if (mAdManager == null) {
			mAdManager = new AdManager();
		}
		mAdManager.setLocation(DetailUtil.getLocation(mActivity));

		mAdApplicationContext = mAdManager.getAdApplicationContext();
		// (REQUIRED) the container which used to host the html5 ad.
		mInteractiveAdContainer.removeAllViews();
		mAdApplicationContext
				.setWMHtml5AdViewContainer(mInteractiveAdContainer);
		// (REQUIRED)
		mAdApplicationContext.setActivity(mActivity);
		setInteractiveAdResource();
		// (REQUIRED)
		setupInteractiveAdData(brs, count);
		mAdApplicationContext.setAdData(mCurrentAdData);
		mAdApplicationContext.setTimeout(INTERACTIVE_AD_TIMEOUT);
		// (REQUIRED) register observer
		if (mInteractiveAdListener == null) {
			mInteractiveAdListener = new InteractiveAdListener();
		}
		mAdApplicationContext.addEventListener(
				IAdApplicationContext.AD_PREPARED, mInteractiveAdListener);
		mAdApplicationContext.addEventListener(IAdApplicationContext.AD_STOPED,
				mInteractiveAdListener);
		mAdApplicationContext.addEventListener(IAdApplicationContext.AD_ERROR,
				mInteractiveAdListener);
		mAdApplicationContext.addEventListener(
				IAdApplicationContext.AD_VIEW_MODE_CHANGE,
				mInteractiveAdListener);
		mAdApplicationContext.addEventListener(
				IAdApplicationContext.VIDEO_PAUSE, mInteractiveAdListener);
		mAdApplicationContext.addEventListener(
				IAdApplicationContext.VIDEO_RESUME, mInteractiveAdListener);
		if (mediaPlayerDelegate.videoInfo != null
				&& mediaPlayerDelegate.videoInfo.getCurrentAdvInfo() != null) {
			mInteractiveAdVideoRs = mediaPlayerDelegate.videoInfo
					.getCurrentAdvInfo().RS;
		}
		try {
			mAdApplicationContext.load();
		} catch (Exception e) {
		}
		isInteractiveAdHide = false;
	}

	public void closeInteractiveAd() {
		if (isInteractiveAdShow) {
			if (mAdApplicationContext != null) {
				mAdApplicationContext.removeAllListeners();
			}
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mInteractiveAdContainer != null) {
						mInteractiveAdContainer.removeAllViews();
						mInteractiveAdContainer.setVisibility(View.GONE);
					}
					if (mAdPageHolder != null) {
						mAdPageHolder.setVisibility(View.VISIBLE);
					}
					mInteractiveAdGoFull.setVisibility(View.GONE);
				}
			});
			isInteractiveAdShow = false;
			isInteractiveAdHide = false;
			if ((mediaPlayerDelegate.videoInfo != null && !StaticsUtil.PLAY_TYPE_LOCAL
					.equals(mediaPlayerDelegate.videoInfo.getPlayType()))) {
				mActivity.setOrientionEnable();
			}
		}
	}

	public void closeInteractiveAdNotIcludeUI() {
		if (isInteractiveAdShow) {
			if (mAdApplicationContext != null) {
				mAdApplicationContext.removeAllListeners();
			}

			isInteractiveAdShow = false;
			isInteractiveAdHide = false;
			if ((mediaPlayerDelegate.videoInfo != null && !StaticsUtil.PLAY_TYPE_LOCAL
					.equals(mediaPlayerDelegate.videoInfo.getPlayType()))) {
				mActivity.setOrientionEnable();
			}
		}
	}

	private class InteractiveAdListener implements IXYDEventListener {

		@Override
		public void run(IXYDEvent arg0) {
			String type = arg0.getType();
			if (type.equals(AdApplicationContext.AD_PREPARED)) {
				if (isInteractiveAdShow) {
					DisposableStatsUtils.disposeSHU(getAdvInfo());
				}
			} else if (type.equals(AdApplicationContext.AD_STOPED)) {
				isInteractiveAdShow = false;
				isInteractiveAdHide = false;
				mAdApplicationContext.removeAllListeners();
				mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mInteractiveAdContainer.removeAllViews();
						mInteractiveAdContainer.setVisibility(View.GONE);
						mInteractiveAdGoFull.setVisibility(View.GONE);
						if (mAdPageHolder != null) {
							mAdPageHolder.setVisibility(View.VISIBLE);
						}
					}
				});

				if ((mediaPlayerDelegate.videoInfo != null && !StaticsUtil.PLAY_TYPE_LOCAL
						.equals(mediaPlayerDelegate.videoInfo.getPlayType()))) {
					mActivity.setOrientionEnable();
				}
				if (mMediaPlayerDelegate != null
						&& mMediaPlayerDelegate.mediaPlayer != null
						&& mediaPlayerDelegate.videoInfo.getCurrentAdvInfo() != null
						&& mInteractiveAdVideoRs
								.equalsIgnoreCase(mediaPlayerDelegate.videoInfo
										.getCurrentAdvInfo().RS)) {
					mediaPlayerDelegate.videoInfo.removePlayedAdv();
					mMediaPlayerDelegate.mediaPlayer.skipCurPreAd();
				}

			} else if (type.equals(AdApplicationContext.AD_ERROR)) {
				Logger.e("PlayFlow", "PlugiADPlay: interactive ad error");
				closeInteractiveAd();
			} else if (type.equals(AdApplicationContext.AD_VIEW_MODE_CHANGE)) {
				String oldViewMode = (String) arg0.getData().get("oldViewMode");
				String newViewMode = (String) arg0.getData().get("newViewMode");

				if (IAdContants.ViewMode.EXPAND.getValue().equals(oldViewMode)
						&& IAdContants.ViewMode.THUMBNAIL.getValue().equals(
								newViewMode)) {
					isInteractiveAdHide = true;
					mAdApplicationContext.hide();
					mActivity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							mInteractiveAdContainer.setVisibility(View.GONE);
							if (mAdPageHolder != null) {
								mAdPageHolder.setVisibility(View.VISIBLE);
							}
							mInteractiveAdGoFull.setVisibility(View.VISIBLE);
						}
					});
					if ((mediaPlayerDelegate.videoInfo != null && !StaticsUtil.PLAY_TYPE_LOCAL
							.equals(mediaPlayerDelegate.videoInfo.getPlayType()))) {
						mActivity.setOrientionEnable();
					}
				}
				if (IAdContants.ViewMode.EXPAND.getValue().equals(newViewMode)) {
					isInteractiveAdHide = false;
				}

			} else if (type.equals(IAdApplicationContext.VIDEO_PAUSE)) {
				if (mMediaPlayerDelegate != null) {
					mMediaPlayerDelegate.pauseByInteractiveAd();
				}
			} else if (type.equals(IAdApplicationContext.VIDEO_RESUME)) {
				if (mMediaPlayerDelegate != null) {
					mMediaPlayerDelegate.startByInteractiveAd();
				}
			}
		}
	}

	private void setupInteractiveAdData(String rs, int count) {
		mCurrentAdData = new org.json.JSONObject();
		try {
			mCurrentAdData.put("BRS", rs);
			mCurrentAdData.put("AL", count);
		} catch (Exception e) {
		}
	}

	private void setInteractiveAdResource() {
		if (mAdApplicationContext == null) {
			return;
		}
		XAdSDKResource resource = new XAdSDKResource();
		resource.ad_mini = R.drawable.xadsdk_ad_mini;
		resource.ad_close = R.drawable.xadsdk_ad_close;
		resource.browser_bkgrnd = R.drawable.xadsdk_browser_bkgrnd;
		resource.browser_leftarrow = R.drawable.xadsdk_browser_leftarrow;
		resource.browser_unleftarrow = R.drawable.xadsdk_browser_unleftarrow;
		resource.browser_rightarrow = R.drawable.xadsdk_browser_rightarrow;
		resource.browser_unrightarrow = R.drawable.xadsdk_browser_unrightarrow;
		resource.browser_refresh = R.drawable.xadsdk_browser_refresh;
		resource.browser_close = R.drawable.xadsdk_browser_close;
		mAdApplicationContext.setXAdSDKResource(resource);
	}

	public boolean isInteractiveAdShow() {
		return isInteractiveAdShow;
	}

	public boolean isInteractiveAdHide() {
		return isInteractiveAdHide;
	}

	public void setInteractiveAdVisible(boolean visible) {
		if (mInteractiveAdContainer == null) {
			return;
		}
		if (visible) {
			if (isInteractiveAdShow) {
				if (!isInteractiveAdHide) {
					mInteractiveAdContainer.setVisibility(View.VISIBLE);
					if (mAdPageHolder != null) {
						mAdPageHolder.setVisibility(View.GONE);
					}
				} else {
					mInteractiveAdGoFull.setVisibility(View.VISIBLE);
				}
				mActivity.goFullScreen();
				mActivity.setOrientionDisable();
			}
		} else {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mInteractiveAdContainer.setVisibility(View.GONE);
					if (mAdPageHolder != null) {
						mAdPageHolder.setVisibility(View.VISIBLE);
					}
					mInteractiveAdGoFull.setVisibility(View.GONE);
				}
			});
			if ((mediaPlayerDelegate.videoInfo != null && !StaticsUtil.PLAY_TYPE_LOCAL
					.equals(mediaPlayerDelegate.videoInfo.getPlayType()))) {
				mActivity.setOrientionEnable();
			}
		}
	}

	public void showInteractiveAd() {
		if (mInteractiveAdContainer == null) {
			return;
		}
		isInteractiveAdShow = true;
		if (mediaPlayerDelegate.isPause || mActivity.onPause) {
			return;
		}
		mActivity.goFullScreen();
		mActivity.setOrientionDisable();
		if (mAdPageHolder != null) {
			mAdPageHolder.setVisibility(View.GONE);
		}
		mInteractiveAdContainer.setVisibility(View.VISIBLE);
	}
}
