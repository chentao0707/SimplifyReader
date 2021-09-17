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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.domob.android.ads.DomobAdManager.ErrorCode;
import cn.domob.android.ads.DomobPreRollAd;
import cn.domob.android.ads.DomobPreRollAdListener;
import cn.domob.android.ads.DomobSceneInfo;

import com.baseproject.image.DiskLruCache;
import com.baseproject.image.ImageCache;
import com.baseproject.image.ImageResizer;
import com.baseproject.image.Utils;
import com.baseproject.network.YoukuAsyncTask;
import com.baseproject.utils.Logger;
import com.baseproject.utils.UIUtils;
import com.baseproject.utils.Util;
//inmobi ad
import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMErrorCode;
import com.inmobi.monetization.IMInterstitial;
import com.inmobi.monetization.IMInterstitial.State;
import com.inmobi.monetization.IMInterstitialListener;
//adsage ad
import com.mobisage.android.MobiSageAdProductPlacement;
import com.mobisage.android.MobiSageAdProductPlacementListener;
import com.mobisage.android.MobiSageManager;
//import com.punchbox.util.PBLog;
import com.punchbox.ads.AdRequest;
import com.punchbox.ads.InterstitialAd;
//punchbox ad
import com.punchbox.exception.PBException;
import com.punchbox.listener.AdListener;
import com.youku.player.Track;
import com.youku.player.ad.AdForward;
import com.youku.player.ad.AdVender;
import com.youku.player.base.GoplayException;
import com.youku.player.base.Plantform;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.module.VideoUrlInfo.Source;
import com.youku.player.ui.R;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DetailUtil;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;

public class PluginImageAD extends PluginOverlay implements DetailMessage {

	LayoutInflater mLayoutInflater;
	View containerView;
	TextView endPage;
	YoukuBasePlayerActivity mActivity;
	IMediaPlayerDelegate mediaPlayerDelegate;
	private View closeBtn;
	private ImageView adImageView;
	private static Handler mHandler = new Handler();

	private int mAdType = AdVender.YOUKU;
	private int mAdForward = AdForward.BROWSER;

	public boolean isOnClick = false;
	AdvInfo mAdvInfo = null;

	private boolean mIsStartToShow = false;

	private static final int TIME_OUT = 10000;// ms
	private RelativeLayout mBtnCloseWrap = null;
    // timer
	private static final int COUNTDOWN_DEFAULT = 5;// s
	private LinearLayout mTimerWrap;
	private TextView mTimerText;
	private AdCountDownTimer mTimer;
	private int mSavedCount;
	//html5 ad
	private LinearLayout mWebContainer;
	private WebView mAdWeb = null;
	private ImageAdWebViewClient mWebViewClient = null;

	// inmobi ad
	private IMInterstitial mIMAdInterstitial;
	private InmobiInterstListener mIMAdInListener = null;
	public static boolean isInmobiSDKInit = false;

	// mobisage ad
	private LinearLayout mMobisageContainer;
	MobiSageAdProductPlacement mMobisageAd = null;
	private AdMobisageListener mMobisageListener;
	LayoutParams mParams;
	public static boolean isMobiSageSDKInit = false;

	// PunchBox ad
	private static boolean isPunchBoxInit = false;
	private LinearLayout mPunchboxContainer;
	private com.punchbox.ads.InterstitialAd mPunchboxAd = null;
	private AdPunchBoxListener mPunchBoxListener = null;

	// Domob ad
	private RelativeLayout mDomobContainer;
	private DomobPreRollAd mDomobAd = null;
	private AdDomobPreRollListener mDomobListener = null;
	private int mScreenWidth;
	private int mScreenHeight;
	private boolean isDomobLandingPageOpened = false;

	public PluginImageAD(YoukuBasePlayerActivity context,
			IMediaPlayerDelegate mediaPlayerDelegate) {
		super(context, mediaPlayerDelegate);
		this.mediaPlayerDelegate = mediaPlayerDelegate;
		mActivity = context;
		mLayoutInflater = LayoutInflater.from(context);
		init(context);
	}

	private void init(Context context) {
		containerView = mLayoutInflater.inflate(R.layout.yp_plugin_image_ad,
				null);
		addView(containerView);
		findView();
		mScreenWidth = DetailUtil.getScreenWidth(mActivity);
		mScreenHeight = DetailUtil.getScreenHeight(mActivity);
	}

	public void findView() {
		mBtnCloseWrap = (RelativeLayout) containerView.findViewById(R.id.btn_close_wrap);
		closeBtn = (View) containerView.findViewById(R.id.btn_close);
		adImageView = (ImageView) containerView
				.findViewById(R.id.plugin_full_ad_image);
		mPunchboxContainer = (LinearLayout) containerView
				.findViewById(R.id.ad_punchbox);
		closeBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissImageAD();
				if (mediaPlayerDelegate != null) {
					mediaPlayerDelegate.startPlayAfterImageAD();
				}
			}
		});
		adImageView.setOnClickListener(null);
		mWebContainer = (LinearLayout) containerView
				.findViewById(R.id.plugin_full_ad_webview);
		mTimerWrap = (LinearLayout) containerView
				.findViewById(R.id.image_ad_timer_wrap);
		mTimerText = (TextView) containerView.findViewById(R.id.image_ad_count);

		// init mobisage view
		mMobisageContainer = (LinearLayout) containerView
				.findViewById(R.id.ad_mobisage);
		// init Domob view
		mDomobContainer = (RelativeLayout) containerView
				.findViewById(R.id.ad_domobContainer);
	}

	private String TAG = "PluginImageAD";
	protected VideoAdvInfo mVideoAdvInfo = null;

	@Override
	public void onBufferingUpdateListener(int percent) {

	}

	@Override
	public void onCompletionListener() {

	}

	@Override
	public boolean onErrorListener(int what, int extra) {
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
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dismissImageAD();
			}
		});
	}

	@Override
	public void onLoadedListener() {
	}

	@Override
	public void onLoadingListener() {
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
		firstLoaded = false;
	}

	@Override
	public void onVideoInfoGetting() {
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
			containerView.setVisibility(View.VISIBLE);
		} else {
			containerView.setVisibility(View.INVISIBLE);
			setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onPluginAdded() {
		super.onPluginAdded();
	}

	public ImageResizer getImageWorker() {
		return mActivity.mImageWorker;
	}

	@Override
	public void onNotifyChangeVideoQuality() {

	}

	@Override
	public void onRealVideoStart() {

	}

	@Override
	public void onADplaying() {

	}

	private boolean isVideoNoAdv() {

		VideoUrlInfo videoInfo = mMediaPlayerDelegate.videoInfo;
		boolean notFromYouku = videoInfo.mSource != Source.YOUKU;

		if (notFromYouku) {
			Logger.d("PlayFlow", "PluginImageAD->isVideoNoAdv = true");
			return true;
		}

		boolean isWifi = Util.isWifi();
		boolean isLocalVideo = videoInfo.playType
				.equals(StaticsUtil.PLAY_TYPE_LOCAL);

		if (!isWifi && isLocalVideo) {
			Logger.d("PlayFlow", "PluginImageAD->isVideoNoAdv = true");
			return true;
		}

		Logger.d("PlayFlow", "PluginImageAD->isVideoNoAdv = false");
		return false;
	}

	/**
	 * 开始获取广�?
	 */
	public void showAD(VideoAdvInfo videoAdvInfo) {
		if (!firstLoaded) {
			// return;
		}

		isOnClick = false;
		mVideoAdvInfo = videoAdvInfo;
		if (mMediaPlayerDelegate.isADShowing) {
			// return;
		}

		if (isVideoNoAdv() || Profile.from == Profile.PHONE_BROWSER) {
			return;
		}

		if (mMediaPlayerDelegate != null && mVideoAdvInfo != null) {
			int size = mVideoAdvInfo.VAL.size();
			if (size == 0) {
				// mADURL = "";
				Logger.d("PlayFlow", "全屏广告VC:为空");
				dismissImageAD();
				if (mediaPlayerDelegate != null) {
					mediaPlayerDelegate.startPlayAfterImageAD();
				}
				return;
			}

			for (int i = 0; i < size; i++) {
				if (mVideoAdvInfo.VAL.get(i).AT.equals("76")) {
					mAdvInfo = mVideoAdvInfo.VAL.get(i);
					int duration = mVideoAdvInfo.VAL.get(i).AL;
					mSavedCount = duration;
					if (mVideoAdvInfo.VAL.get(i).SDKID == AdVender.YOUKU
							&& mVideoAdvInfo.VAL.get(i).RS != null
							&& !mVideoAdvInfo.VAL.get(i).RS.equals("")) {
						mADURL = mAdvInfo.RS;
						mADClickURL = mAdvInfo.CU;
						mAdForward = mAdvInfo.CUF;
						if (mAdvInfo.RST != null
								&& mAdvInfo.RST.equalsIgnoreCase("html")) {
							setAdType(AdVender.YOUKU_HTML);
						} else {
							setAdType(AdVender.YOUKU);
						}

						mTimerText.setText(String.valueOf(mSavedCount));
						showADImage();
						return;
					}
					if (mVideoAdvInfo.VAL.get(i).SDKID == AdVender.INMOBI) {
						setAdType(AdVender.INMOBI);
						showADImage();
						return;
					}
					if (mVideoAdvInfo.VAL.get(i).SDKID == AdVender.ADSAGE) {
						setAdType(AdVender.ADSAGE);
						showADImage();
						return;
					}
					if (mVideoAdvInfo.VAL.get(i).SDKID == AdVender.DOMOB) {
						setAdType(AdVender.DOMOB);
						showADImage();
						return;
					}
					if (mVideoAdvInfo.VAL.get(i).SDKID == AdVender.PUNCHBOX) {
						setAdType(AdVender.PUNCHBOX);
						showADImage();
						return;
					}
				}
			}
		}
		dismissImageAD();
		if (mediaPlayerDelegate != null) {
			mediaPlayerDelegate.startPlayAfterImageAD();
		}
	}

	private String mADURL;
	private String mADClickURL;

	/**
	 * 获取广告信息去加�?
	 */
	protected void showADImage() {
		AdvInfo advInfo = null;
		try {
			// 显示广告的时候是使用
			advInfo = getAdvInfo();
			if (advInfo == null) {
				Logger.e("PlayFlow", "全屏广告显示 SUS:为空");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.d("DisposableHttpTask", "全屏广告显示 SUS为空");
		} finally {
			if (advInfo != null) {
				mIsStartToShow = true;
				if (mAdType == AdVender.YOUKU) {
					loadImage(adImageView, mADURL, advInfo);
				} else if (mAdType == AdVender.INMOBI) {
					startInmobiAd();
				} else if (mAdType == AdVender.ADSAGE) {
					startMobisageAd();
				} else if (mAdType == AdVender.YOUKU_HTML) {
					startYoukuHtml5Ad();
				} else if (mAdType == AdVender.DOMOB) {
					startDomobAd();
				} else if (mAdType == AdVender.PUNCHBOX) {
					startPunchBoxAd();
				}
			}
		}

	}

	public void loadImage(final ImageView imageView, final String url,
			final AdvInfo advInfo) {

		new YoukuAsyncTask<Void, Void, Bitmap>() {
			private boolean isLoaded = false;

			@Override
			protected Bitmap doInBackground(Void... Void) {
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						if (!isLoaded) {
							mActivity.isImageADShowing = false;
							if (mediaPlayerDelegate != null
									&& !mediaPlayerDelegate.isPause) {
								if (StaticsUtil.PLAY_TYPE_LOCAL
										.equals(mediaPlayerDelegate.videoInfo
												.getPlayType())
										&& mediaPlayerDelegate.pluginManager != null
										&& mActivity != null) {
									if (mActivity != null)
										mActivity.runOnUiThread(new Runnable() {

											@Override
											public void run() {
												mediaPlayerDelegate.pluginManager
														.onVideoInfoGetted();
												mediaPlayerDelegate.pluginManager
														.onChangeVideo();
											}
										});
								}
								mediaPlayerDelegate.startPlayAfterImageAD();
							}
							disposeAdLoss(URLContainer.AD_LOSS_STEP4);
						}
						cancel(true);
					}
				}, TIME_OUT);
				Bitmap bitmap = loadImageFromUrl(url);
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap d) {
				if (d == null) {
					return;
				}
				isLoaded = true;

				Logger.e("PlayFlow", "全屏广告加载成功");
				mActivity.updatePlugin(DetailMessage.PLUGIN_SHOW_IMAGE_AD);
				imageView.setImageBitmap(d);
				showADImageWhenLoaded();
			}
		}.execute();
	}

	private Bitmap loadImageFromUrl(String url) {
		URL u;
		InputStream i = null;
		Bitmap d = null;
		DiskLruCache cache = null;

		try {
			u = new URL(url);
			d = getImageFromCache(url);
			if (d != null) {
				return d;
			}
			i = (InputStream) u.getContent();
		} catch (Exception e) {
		}
		if (mActivity != null && mActivity.mImageWorker != null
				&& mActivity.mImageWorker.getImageCache() != null) {
			cache = mActivity.mImageWorker.getImageCache().getDiskCache();
		}
		if (cache == null) {
			return null;
		}
		final String fileName = Utils.urlToFileName(url);
		final File cacheFile = new File(cache.createFilePath(fileName));
		BufferedOutputStream out = null;
		try {
			BitmapDrawable bitDrawable;
			bitDrawable = (BitmapDrawable) BitmapDrawable.createFromStream(i,
					"src");
			if (bitDrawable == null)
				return null;
			d = bitDrawable.getBitmap();
			if (d != null) {
				addImageToCache(url, d);
			}
			out = new BufferedOutputStream(new FileOutputStream(cacheFile),
					Utils.IO_BUFFER_SIZE);

			d.compress(CompressFormat.PNG, 85, out);
			out.flush();
		} catch (final IOException e) {
		} catch (OutOfMemoryError e) {
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (final IOException e) {
					Logger.e(TAG, "Error in downloadBitmap - " + e);
				}
			}
		}
		return d;
	}

	private Bitmap getImageFromCache(String url) {
		Bitmap d = null;
		ImageCache cache;
		if (mActivity != null && mActivity.mImageWorker != null) {
			cache = mActivity.mImageWorker.getImageCache();
		} else {
			return d;
		}
		if (cache != null) {
			d = cache.getBitmapFromMemCache(url);
			if (d == null) {
				d = cache.getBitmapFromDiskCache(url);
			}
		}
		return d;
	}

	private void addImageToCache(String url, Bitmap bitmap) {
		ImageCache cache = null;
		if (mActivity != null && mActivity.mImageWorker != null) {
			cache = mActivity.mImageWorker.getImageCache();
		}
		if (cache != null) {
			cache.addBitmapToCache(url, bitmap);
		}
	}

	/**
	 * 全屏广告获取成功 去显�?
	 */
	private void showADImageWhenLoaded() {
		if (null != mADClickURL && TextUtils.getTrimmedLength(mADClickURL) > 0) {
			adImageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Logger.e("PlayFlow", "点击:" + mADClickURL);
					if (isOnClick) {
						return;
					}
					AdvInfo advInfo = getAdvInfo();
					// 用户点击跳转发送CUM
					DisposableStatsUtils.disposeCUM(advInfo);
					isOnClick = true;
					if (mADClickURL.endsWith(".apk")
							&& IMediaPlayerDelegate.mIDownloadApk != null
							&& mediaPlayerDelegate != null) {
						if (!Util.isWifi()) {
							creatSelectDownloadDialog(mActivity);
							return;
						}
						dismissImageAD();
						mediaPlayerDelegate.pluginManager.onLoading();
						mediaPlayerDelegate.startPlayAfterImageAD();
					} else if (mediaPlayerDelegate != null) {
						dismissImageAD();
						mediaPlayerDelegate.pluginManager.onLoaded();
					}
					new AdvClickProcessor().processAdvClick(mActivity,
							mADClickURL, mAdForward);
				}
			});
		} else {
			adImageView.setOnClickListener(null);
		}
		if (mediaPlayerDelegate != null
				&& StaticsUtil.PLAY_TYPE_LOCAL.equals(mediaPlayerDelegate.videoInfo.getPlayType())
				&& mediaPlayerDelegate.pluginManager != null) {
			mediaPlayerDelegate.pluginManager.onVideoInfoGetted();
			mediaPlayerDelegate.pluginManager.onChangeVideo();
		}
		if (mActivity.isFinishing()) {
			disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			return;
		}
		if (Profile.PLANTFORM == Plantform.YOUKU && isLand()) {
			// youku客户端播放器不再挤压，横屏view尺寸需要重新初始化
			mActivity.updatePlugin(PLUGIN_SHOW_IMAGE_AD);
		}
		if (UIUtils.hasKitKat()) {
			mActivity.setPluginHolderPaddingZero();
		}
		mActivity.isImageADShowing = true;
		Track.onImageAdStart();
		setVisible(true);
		setVisibility(View.VISIBLE);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				startTimer();
			}
		}, 400);
	}

	/**
	 * 获取广告信息
	 * 
	 * @return
	 */
	private AdvInfo getAdvInfo() {
		try {
			return mAdvInfo;
			// return mVideoAdvInfo.VAL.get(0);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 不显示全屏广告蒙�?
	 */
	public void dismissImageAD() {
		destroyInmobiAd();
		if (containerView.getVisibility() == View.VISIBLE) {
			mActivity.updatePlugin(PLUGIN_SHOW_NOT_SET);
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					containerView.setVisibility(View.INVISIBLE);
				}
			});
			Track.onImageAdEnd();
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
			if (mAdWeb != null) {
				mWebContainer.removeAllViews();
				mAdWeb.destroy();
				mAdWeb = null;
			}
			if (mAdType == AdVender.YOUKU || mAdType == AdVender.YOUKU_HTML) {
				AdvInfo advInfo = getAdvInfo();
				DisposableStatsUtils.disposeSUE(advInfo);
			}
		}

		if (mPunchboxAd != null) {
			mPunchboxAd.destroy();
			mPunchboxContainer.removeAllViews();
			mPunchboxAd = null;
		}
		if (mMobisageAd != null) {
			mMobisageAd.destoryAdView();
			mMobisageContainer.removeAllViews();
			mMobisageAd = null;
		}
		if (mDomobAd != null) {
			mDomobAd.closePreRollAd();
			mDomobContainer.removeAllViews();
			mDomobAd = null;
		}
		mWebViewClient = null;
		mSavedCount = 0;
		mActivity.isImageADShowing = false;
		mIsStartToShow = false;
		mAdvInfo = null;
	}

	public void destroyInmobiAd() {
		if (mIMAdInterstitial != null) {
			mIMAdInterstitial.destroy();
			mIMAdInterstitial = null;
			mIMAdInListener = null;
		}
	}

	public void setMobisageAdInvisible() {
		if (mMobisageAd != null) {
			mMobisageContainer.setVisibility(View.INVISIBLE);
		}
	}

	public void release() {
		if (mActivity.isImageADShowing) {
			Track.onImageAdEnd();
			if (mAdType == AdVender.YOUKU || mAdType == AdVender.YOUKU_HTML) {
				AdvInfo advInfo = getAdvInfo();
				DisposableStatsUtils.disposeSUE(advInfo);
			}
		}
		if (mIMAdInterstitial != null) {
			mIMAdInterstitial.destroy();
			mIMAdInterstitial = null;
			mIMAdInListener = null;
		}
		if (mMobisageAd != null) {
			mMobisageAd.destoryAdView();
			mMobisageContainer.removeAllViews();
			mMobisageAd = null;
		}
		if (mDomobAd != null) {
			mDomobAd.closePreRollAd();
			mDomobContainer.removeAllViews();
			mDomobAd = null;
		}
		if (mPunchboxAd != null) {
			mPunchboxAd.destroy();
			mPunchboxContainer.removeAllViews();
			mPunchboxAd = null;
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mAdWeb != null) {
			mWebContainer.removeAllViews();
			mAdWeb.destroy();
			mAdWeb = null;
		}
		mSavedCount = 0;
		mActivity.isImageADShowing = false;
		mIsStartToShow = false;
		mAdvInfo = null;
		isInmobiSDKInit = false;
		isMobiSageSDKInit = false;
		releasePunchboxSdk();
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
		pauseTimer();
	}

	boolean firstLoaded = false;

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

	public void setAdType(int adType) {
		mAdType = adType;
		switch (adType) {
		case AdVender.INMOBI: {
			mBtnCloseWrap.setVisibility(View.GONE);
			closeBtn.setVisibility(View.GONE);
			adImageView.setVisibility(View.GONE);
			mMobisageContainer.setVisibility(View.GONE);
			mTimerWrap.setVisibility(View.GONE);
			mWebContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.GONE);
			mPunchboxContainer.setVisibility(View.GONE);
			// mInmobiAd.setVisibility(View.VISIBLE);
			break;
		}
		case AdVender.ADSAGE: {
			mBtnCloseWrap.setVisibility(View.GONE);
			closeBtn.setVisibility(View.GONE);
			adImageView.setVisibility(View.GONE);
			mTimerWrap.setVisibility(View.GONE);
			mWebContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.GONE);
			mPunchboxContainer.setVisibility(View.GONE);
			mMobisageContainer.setVisibility(View.VISIBLE);
			// mInmobiAd.setVisibility(View.VISIBLE);
			break;
		}
		case AdVender.YOUKU_HTML: {
			mMobisageContainer.setVisibility(View.GONE);
			mPunchboxContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.GONE);
			adImageView.setVisibility(View.GONE);
			mBtnCloseWrap.setVisibility(View.VISIBLE);
			closeBtn.setVisibility(View.VISIBLE);
			mWebContainer.setVisibility(View.VISIBLE);
			if (mSavedCount > 0) {
				mTimerWrap.setVisibility(View.VISIBLE);
			} else {
				mTimerWrap.setVisibility(View.GONE);
			}
			break;
		}
		case AdVender.DOMOB: {
			mBtnCloseWrap.setVisibility(View.GONE);
			closeBtn.setVisibility(View.GONE);
			adImageView.setVisibility(View.GONE);
			mTimerWrap.setVisibility(View.GONE);
			mWebContainer.setVisibility(View.GONE);
			mMobisageContainer.setVisibility(View.GONE);
			mPunchboxContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.VISIBLE);
			break;
		}
		case AdVender.PUNCHBOX: {
			mBtnCloseWrap.setVisibility(View.GONE);
			adImageView.setVisibility(View.GONE);
			mTimerWrap.setVisibility(View.GONE);
			mWebContainer.setVisibility(View.GONE);
			mMobisageContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.GONE);
			mPunchboxContainer.setVisibility(View.VISIBLE);
			break;
		}
		default: {
			mMobisageContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.GONE);
			mPunchboxContainer.setVisibility(View.GONE);
			mWebContainer.setVisibility(View.GONE);
			mBtnCloseWrap.setVisibility(View.VISIBLE);
			closeBtn.setVisibility(View.VISIBLE);
			adImageView.setVisibility(View.VISIBLE);
			if (mSavedCount > 0) {
				mTimerWrap.setVisibility(View.VISIBLE);
			} else {
				mTimerWrap.setVisibility(View.GONE);
			}
			break;
		}
		}
	}

	// 当在2G/3G网络下，让用户选择是否下载
	private void creatSelectDownloadDialog(Activity activity) {
		Dialog alertDialog = new AlertDialog.Builder(activity)
				.setMessage("您当前处于非WiFi网络环境，请确定是否继续下载?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new AdvClickProcessor().processAdvClick(mActivity,
								mADClickURL, mAdForward);
						dismissImageAD();
						if (mediaPlayerDelegate != null) {
							mediaPlayerDelegate.pluginManager.onLoading();
							mediaPlayerDelegate.startPlayAfterImageAD();
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismissImageAD();
						if (mediaPlayerDelegate != null) {
							mediaPlayerDelegate.pluginManager.onLoading();
							mediaPlayerDelegate.startPlayAfterImageAD();
						}
					}
				}).create();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	public int getAdType() {
		return mAdType;
	}

	public boolean isOnclick() {
		return isOnClick;
	}

	public boolean isSaveOnOrientChange() {
		if (mAdType == AdVender.YOUKU || mAdType == AdVender.YOUKU_HTML) {
			return true;
		}
		return false;
	}

	public boolean isStartToShow() {
		return mIsStartToShow;
	}

	// youku html5 ad
	private void startYoukuHtml5Ad() {
		if (mAdWeb != null) {
			mWebContainer.removeAllViews();
			mAdWeb.destroy();
		}
		if (mActivity == null || mActivity.isFinishing()) {
			disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			return;
		}
		mAdWeb = new WebView(mActivity);
		mAdWeb.getSettings().setJavaScriptEnabled(true);
		//mAdWeb.getSettings().setUseWideViewPort(true);
		mAdWeb.getSettings().setLoadWithOverviewMode(true);
		if (mWebViewClient == null) {
			mWebViewClient = new ImageAdWebViewClient();
		}
		mWebViewClient.isGetFeedBack = false;
		mAdWeb.setWebViewClient(mWebViewClient);
		mAdWeb.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mTimer != null) {
					mTimer.cancel();
					mTimer = null;
					mSavedCount = 0;
					mTimerWrap.setVisibility(View.GONE);
				}
				return false;
			}
		});
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mAdWeb.setLayoutParams(params);
		mWebContainer.addView(mAdWeb);
		try {
			mAdWeb.loadUrl(mADURL);
		} catch (Exception e) {

		}

		Logger.d("PlayFlow", "start to show youku html5 ad");
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mIsStartToShow && mWebViewClient != null
						&& !mWebViewClient.isGetFeedBack) {
					dismissImageAD();
					if (mediaPlayerDelegate != null
							&& !mediaPlayerDelegate.isPause) {
						mediaPlayerDelegate.startPlayAfterImageAD();
					}
				}
			}
		}, TIME_OUT);
	}

	private class ImageAdWebViewClient extends WebViewClient {
        private boolean isGetFeedBack = false;
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if (isGetFeedBack) {
				return;
			}
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mIsStartToShow && mediaPlayerDelegate != null
							&& mAdType == AdVender.YOUKU_HTML) {
						if (Profile.PLANTFORM == Plantform.YOUKU && isLand()) {
							// youku客户端播放器不再挤压，横屏view尺寸需要重新初始化
							mActivity.updatePlugin(PLUGIN_SHOW_IMAGE_AD);
						}
						if (UIUtils.hasKitKat()) {
							mActivity.setPluginHolderPaddingZero();
						}
						setVisible(true);
						setVisibility(View.VISIBLE);
						mActivity.isImageADShowing = true;
						isGetFeedBack = true;
						Track.onImageAdStart();
						startTimer();
					} else {
						disposeAdLoss(URLContainer.AD_LOSS_STEP3);
					}
				}
			});
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			mActivity.isImageADShowing = false;
			mIsStartToShow = false;
			isGetFeedBack = true;
			disposeAdLoss(URLContainer.AD_LOSS_STEP4);
			if (mediaPlayerDelegate != null) {
				mediaPlayerDelegate.startPlayAfterImageAD();
			}
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Logger.e("PlayFlow", "点击:" + url);
			AdvInfo advInfo = getAdvInfo();
			// 用户点击跳转发送CUM
			if (!isOnClick) {
				DisposableStatsUtils.disposeCUM(advInfo);
			}
			isOnClick = true;
			if (url.endsWith(".apk")
					&& IMediaPlayerDelegate.mIDownloadApk != null
					&& mediaPlayerDelegate != null) {
				if (!Util.isWifi()) {
					creatSelectDownloadDialog(mActivity);
					return true;
				}
				dismissImageAD();
				mediaPlayerDelegate.pluginManager.onLoading();
				mediaPlayerDelegate.startPlayAfterImageAD();
			} else if (mediaPlayerDelegate != null) {
				dismissImageAD();
				mediaPlayerDelegate.pluginManager.onLoaded();
			}
			new AdvClickProcessor().processAdvClick(mActivity, url, mAdForward);

			return true;
		}
		
	}
	// Inmobi ad related
	private void startInmobiAd() {
		if (mediaPlayerDelegate != null && mediaPlayerDelegate.isFullScreen) {
			if (mActivity == null || mActivity.isFinishing()) {
				disposeAdLoss(URLContainer.AD_LOSS_STEP3);
				return;
			}
			if (!isInmobiSDKInit) {
				InMobi.initialize(mActivity, AdVender.Inmobi_ID);
				isInmobiSDKInit = true;
			}
			mIMAdInterstitial = new IMInterstitial(mActivity,
					AdVender.Inmobi_Interstitial_ID);

			if (mIMAdInListener == null) {
				mIMAdInListener = new InmobiInterstListener();
			}
			mIMAdInListener.isGetFeedBack = false;
			mIMAdInterstitial.setIMInterstitialListener(mIMAdInListener);
			Logger.d("PlayFlow", "start to show inmobi ad");
			AdvInfo advInfo = getAdvInfo();
			DisposableStatsUtils.disposeSUS(advInfo);
			mIMAdInterstitial.loadInterstitial();

			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mIsStartToShow && mIMAdInListener != null
							&& !mIMAdInListener.isGetFeedBack) {
						dismissImageAD();
						if (mediaPlayerDelegate != null
								&& !mediaPlayerDelegate.isPause) {
							mediaPlayerDelegate.startPlayAfterImageAD();
						}
					}
				}
			}, TIME_OUT);

		} else {
			dismissImageAD();
			if (mediaPlayerDelegate != null) {
				mediaPlayerDelegate.startPlayAfterImageAD();
			}
		}
	}

	private class InmobiInterstListener implements IMInterstitialListener {
		public boolean isGetFeedBack = false;

		@Override
		public void onDismissInterstitialScreen(IMInterstitial arg0) {
			if (mActivity.isImageADShowing && !mActivity.onPause) {
				mActivity.isImageADShowing = false;
				mIsStartToShow = false;
				isGetFeedBack = true;
				Track.onImageAdEnd();
				// mActivity.updatePlugin(PLUGIN_SHOW_NOT_SET);
				if (mediaPlayerDelegate != null) {
					if (mediaPlayerDelegate.pluginManager != null) {
						mediaPlayerDelegate.pluginManager.onLoaded();
					}
					mediaPlayerDelegate.startPlayAfterImageAD();
				}
				;
			}
		}

		@Override
		public void onInterstitialFailed(IMInterstitial arg0, IMErrorCode arg1) {
			mActivity.isImageADShowing = false;
			mIsStartToShow = false;
			isGetFeedBack = true;
			disposeAdLoss(URLContainer.AD_LOSS_STEP4);
			if (mediaPlayerDelegate != null) {
				mediaPlayerDelegate.startPlayAfterImageAD();
			}
		}

		@Override
		public void onInterstitialInteraction(IMInterstitial arg0,
				Map<String, String> arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onInterstitialLoaded(IMInterstitial arg0) {
			if (mIsStartToShow && mAdType == AdVender.INMOBI
					&& mIMAdInterstitial != null
					&& mIMAdInterstitial.getState() == State.READY
					&& mediaPlayerDelegate != null
					&& mediaPlayerDelegate.isFullScreen) {
				setVisible(true);
				setVisibility(View.VISIBLE);
				mIMAdInterstitial.show();
				mActivity.isImageADShowing = true;
				isGetFeedBack = true;
				Track.onImageAdStart();
			} else {
				disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			}

		}

		@Override
		public void onLeaveApplication(IMInterstitial arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onShowInterstitialScreen(IMInterstitial arg0) {
			// TODO Auto-generated method stub

		}
	};

	// mobisage ad
	private void startMobisageAd() {
		if (mMobisageAd != null) {
			mMobisageAd.destoryAdView();
			mMobisageContainer.removeAllViews();
		}
		if (mActivity == null || mActivity.isFinishing()) {
			disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			return;
		}
		if (!isMobiSageSDKInit) {
			MobiSageManager.getInstance().setPublisherID(mActivity,
					AdVender.MobiSage_ID);
			isMobiSageSDKInit = true;
		}
		mMobisageAd = new MobiSageAdProductPlacement(mActivity, true);
		if (mMobisageListener == null) {
			mMobisageListener = new AdMobisageListener();
		}
		mMobisageListener.isGetFeedBack = false;
		mMobisageAd.setMobiSageAdProductPlacementListener(mMobisageListener);
		if (mParams == null) {
			mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
		}
		mMobisageAd.setLayoutParams(mParams);
		mMobisageContainer.addView(mMobisageAd, mParams);
		Logger.d("PlayFlow", "start to show adsage ad");
		AdvInfo advInfo = getAdvInfo();
		DisposableStatsUtils.disposeSUS(advInfo);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mIsStartToShow && mMobisageListener != null
						&& !mMobisageListener.isGetFeedBack) {
					dismissImageAD();
					if (mediaPlayerDelegate != null
							&& !mediaPlayerDelegate.isPause) {
						mediaPlayerDelegate.startPlayAfterImageAD();
					}
				}
			}
		}, TIME_OUT);
	}

	private class AdMobisageListener implements
			MobiSageAdProductPlacementListener {
		public boolean isGetFeedBack = false;

		@Override
		public void onMobiSageProductPlacementClick(
				MobiSageAdProductPlacement arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMobiSageProductPlacementClose(
				MobiSageAdProductPlacement arg0) {
			if (mActivity.isImageADShowing) {
				mActivity.isImageADShowing = false;
				mIsStartToShow = false;
				isGetFeedBack = true;
				Track.onImageAdEnd();
				if (mediaPlayerDelegate != null) {
					mediaPlayerDelegate.startPlayAfterImageAD();
				}
			}

		}

		@Override
		public void onMobiSageProductPlacementError(
				MobiSageAdProductPlacement arg0) {
			mActivity.isImageADShowing = false;
			mIsStartToShow = false;
			isGetFeedBack = true;
			disposeAdLoss(URLContainer.AD_LOSS_STEP4);
			if (mediaPlayerDelegate != null) {
				mediaPlayerDelegate.startPlayAfterImageAD();
			}
		}

		@Override
		public void onMobiSageProductPlacementHide(
				MobiSageAdProductPlacement arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMobiSageProductPlacementHideWindow(
				MobiSageAdProductPlacement arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMobiSageProductPlacementPopupWindow(
				MobiSageAdProductPlacement arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMobiSageProductPlacementShow(
				MobiSageAdProductPlacement arg0) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mIsStartToShow && mediaPlayerDelegate != null
							&& mAdType == AdVender.ADSAGE) {
						setVisible(true);
						setVisibility(View.VISIBLE);
						mActivity.isImageADShowing = true;
						isGetFeedBack = true;
						Track.onImageAdStart();
					} else {
						disposeAdLoss(URLContainer.AD_LOSS_STEP3);
					}
				}
			});

		}

	}

	// Domob ad
	private void startDomobAd() {
		if (mDomobAd != null) {
			mDomobAd.closePreRollAd();
			mMobisageContainer.removeAllViews();
		}
		if (mActivity == null || mActivity.isFinishing()) {
			disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			return;
		}
		mDomobAd = new DomobPreRollAd(mActivity, AdVender.Domob_ID,
				AdVender.Domob_PreAd_ID, mScreenWidth, mScreenHeight);
		if (mDomobListener == null) {
			mDomobListener = new AdDomobPreRollListener();
		}
		mDomobListener.isGetFeedBack = false;
		mDomobAd.setProRollAdListener(mDomobListener);
		mDomobAd.setCountdownTotalSeconds(mSavedCount > 0 ? mSavedCount
				: COUNTDOWN_DEFAULT);
		mDomobAd.loadPreRollAd();
		Logger.d("PlayFlow", "start to show Domob image ad");
		AdvInfo advInfo = getAdvInfo();
		DisposableStatsUtils.disposeSUS(advInfo);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mIsStartToShow && mDomobListener != null
						&& !mDomobListener.isGetFeedBack) {
					dismissImageAD();
					if (mediaPlayerDelegate != null
							&& !mediaPlayerDelegate.isPause) {
						mediaPlayerDelegate.startPlayAfterImageAD();
					}
				}
			}
		}, TIME_OUT);
	}

	private class AdDomobPreRollListener implements DomobPreRollAdListener {
		public boolean isGetFeedBack = false;

		@Override
		public void onPreRollAdReady() {
			if (mDomobAd != null && mIsStartToShow && mAdType == AdVender.DOMOB) {
				mDomobAd.showPreRollAd(getSceneInfo());
			} else {
				disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			}
		}

		@Override
		public void onPreRollAdPresent() {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mIsStartToShow && mediaPlayerDelegate != null
							&& mAdType == AdVender.DOMOB) {
						setVisible(true);
						setVisibility(View.VISIBLE);
						mActivity.isImageADShowing = true;
						isGetFeedBack = true;
						Track.onImageAdStart();
					} else {
						disposeAdLoss(URLContainer.AD_LOSS_STEP3);
					}
				}
			});
		}

		@Override
		public void onPreRollAdLeaveApplication() {
		}

		@Override
		public void onPreRollAdFailed(ErrorCode code) {
			mActivity.isImageADShowing = false;
			mIsStartToShow = false;
			isGetFeedBack = true;
			disposeAdLoss(URLContainer.AD_LOSS_STEP4);
			if (mediaPlayerDelegate != null) {
				mediaPlayerDelegate.startPlayAfterImageAD();
			}
		}

		@Override
		public void onPreRollAdDismiss() {
			if (mActivity.isImageADShowing) {
				mActivity.isImageADShowing = false;
				mIsStartToShow = false;
				isGetFeedBack = true;
				Track.onImageAdEnd();
				if (mediaPlayerDelegate != null) {
					mediaPlayerDelegate.startPlayAfterImageAD();
				}
			}
		}

		@Override
		public void onPreRollAdClicked() {
		}

		@Override
		public void onLandingPageOpen() {
			isDomobLandingPageOpened = true;
		}

		@Override
		public void onLandingPageClose() {
			isDomobLandingPageOpened = false;
			if (mActivity.isImageADShowing) {
				mActivity.isImageADShowing = false;
				mIsStartToShow = false;
				isGetFeedBack = true;
				Track.onImageAdEnd();
				mediaPlayerDelegate.pluginManager.onLoading();
				if (mediaPlayerDelegate != null) {
					mediaPlayerDelegate.startPlayAfterImageAD();
				}
			}

		}
	}

	private DomobSceneInfo getSceneInfo() {
		DomobSceneInfo sceneInfo = new DomobSceneInfo();
		sceneInfo.setContext(mActivity);
		sceneInfo.setCoordinatesX(0);
		sceneInfo.setCoordinatesY(0);
		sceneInfo.setParentViewGroup(mDomobContainer);
		sceneInfo.setSceneId("sceneId");
		int frameWidth = getWidth();
		int frameHeight = getHeight();
		sceneInfo.setSecureAreaWidth(frameWidth);
		sceneInfo.setSecureAreaHeight(frameHeight);
		return sceneInfo;
	}

	public boolean isDomobLandingPageOpened() {
		return isDomobLandingPageOpened;
	}

	private void startPunchBoxAd() {
		if (mPunchboxAd != null) {
			mPunchboxAd.dismiss();
			mPunchboxContainer.removeAllViews();
		}
		initPunchboxSdk();
		if (mActivity == null || mActivity.isFinishing()) {
			disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			return;
		}
		mPunchboxAd = new InterstitialAd(mActivity);
		if (mPunchBoxListener == null) {
			mPunchBoxListener = new AdPunchBoxListener();
		} else {
			mPunchBoxListener.isGetFeedBack = false;
		}
		mPunchboxAd.setAdListener(mPunchBoxListener);
		mPunchboxAd.setCloseMode(2);
		mPunchboxAd.setDisplayTime(mSavedCount > 0 ? mSavedCount
				: COUNTDOWN_DEFAULT);
		mPunchboxAd.donotReloadAfterClose();
		AdRequest adRequest = new AdRequest();
		adRequest.setOrientation(2);
		mPunchboxAd.loadAd(adRequest);

		Logger.d("PlayFlow", "start to show punchbox fullscreen ad");
		AdvInfo advInfo = getAdvInfo();
		DisposableStatsUtils.disposePausedSUS(advInfo);
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mIsStartToShow && mPunchBoxListener != null
						&& !mPunchBoxListener.isGetFeedBack) {
					dismissImageAD();
					if (mediaPlayerDelegate != null
							&& !mediaPlayerDelegate.isPause) {
						mediaPlayerDelegate.startPlayAfterImageAD();
					}
				}
			}
		}, TIME_OUT);
	}

	private class AdPunchBoxListener implements AdListener {
		public boolean isGetFeedBack = false;

		@Override
		public void onDismissScreen() {
			if (mActivity.onPause) {
				isGetFeedBack = true;
				Track.onImageAdEnd();
				return;
			}

			if (mActivity.isImageADShowing) {
				mActivity.isImageADShowing = false;
				mIsStartToShow = false;
				isGetFeedBack = true;
				Track.onImageAdEnd();
				mediaPlayerDelegate.pluginManager.onLoading();
				if (mediaPlayerDelegate != null) {
					mediaPlayerDelegate.startPlayAfterImageAD();
				}
			}
		}

		@Override
		public void onFailedToReceiveAd(PBException arg0) {
			Logger.d("PlayFlow", "punchbox onFailedToReceiveAd");
			mActivity.isImageADShowing = false;
			mIsStartToShow = false;
			isGetFeedBack = true;
			disposeAdLoss(URLContainer.AD_LOSS_STEP4);
			if (mediaPlayerDelegate != null) {
				mediaPlayerDelegate.startPlayAfterImageAD();
			}
		}

		@Override
		public void onPresentScreen() {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (mIsStartToShow && mediaPlayerDelegate != null
							&& mAdType == AdVender.PUNCHBOX) {
						setVisible(true);
						setVisibility(View.VISIBLE);
						mActivity.isImageADShowing = true;
						isGetFeedBack = true;
						Track.onImageAdStart();
					} else {
						disposeAdLoss(URLContainer.AD_LOSS_STEP3);
					}
				}
			});
		}

		@Override
		public void onReceiveAd() {
			try {
				if (mPunchboxAd!=null && mPunchboxAd.isReady()) {
					mPunchboxAd.show(mPunchboxContainer, null);
				}
			} catch (PBException e) {
				disposeAdLoss(URLContainer.AD_LOSS_STEP3);
				// 当设置的scale不在范围内，或者isReady()属性为false
				e.printStackTrace();
			}
		}

	}

	public static void initPunchboxSdk() {
		if (isPunchBoxInit) {
			return;
		}
		//PBLog.LOG_ENABLED = false;
		isPunchBoxInit = true;
	}

	public static void releasePunchboxSdk() {
		isPunchBoxInit = false;
	}

	private void disposeAdLoss(int step) {
		DisposableStatsUtils.disposeAdLoss(mActivity, step,
				SessionUnitil.playEvent_session, URLContainer.AD_LOSS_MF);
	}

	public void pauseTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}

	public void startTimer() {
		if ((mAdType == AdVender.YOUKU || mAdType == AdVender.YOUKU_HTML)
				&& mSavedCount > 0 && mActivity.isImageADShowing) {
			mTimer = new AdCountDownTimer(mSavedCount * 1000, 100);
			mTimer.start();
		}
	}
	private class AdCountDownTimer extends CountDownTimer {

		public AdCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			mSavedCount = (int)millisInFuture/1000;
		}

		@Override
		public void onFinish() {
			dismissImageAD();
			if (mediaPlayerDelegate != null) {
				mediaPlayerDelegate.pluginManager.onLoading();
				mediaPlayerDelegate.startPlayAfterImageAD();
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			synchronized (mTimerText) {
				float time = (float) millisUntilFinished / 1000;
				int count = Math.round(time);
				if (mSavedCount != count && count > 0) {
					mSavedCount = count;
					mTimerText.setText(String.valueOf(mSavedCount));
				}
			}
		}

	}

	private boolean isLand() {
		if (mActivity != null) {
			Display getOrient = mActivity.getWindowManager()
					.getDefaultDisplay();
			return getOrient.getWidth() > getOrient.getHeight();
		}
		return false;
	}
}
