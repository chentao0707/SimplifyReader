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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.domob.android.ads.DomobAdManager.ErrorCode;
import cn.domob.android.ads.DomobSceneInfo;
import cn.domob.android.ads.DomobVideoInterstitialAd;
import cn.domob.android.ads.DomobVideoInterstitialAdListener;

import com.baseproject.image.DiskLruCache;
import com.baseproject.image.ImageCache;
import com.baseproject.image.ImageResizer;
import com.baseproject.image.Utils;
import com.baseproject.network.YoukuAsyncTask;
import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.mobisage.android.MobiSageAdProductPlacement;
import com.mobisage.android.MobiSageAdProductPlacementListener;
import com.mobisage.android.MobiSageManager;
import com.punchbox.ads.AdRequest;
import com.punchbox.ads.InterstitialAd;
import com.punchbox.exception.PBException;
import com.punchbox.listener.AdListener;
import com.youku.player.ad.AdForward;
import com.youku.player.ad.AdVender;
import com.youku.player.base.GoplayException;
import com.youku.player.base.YoukuBasePlayerActivity;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.AdvInfo;
import com.youku.player.goplay.IGetAdvCallBack;
import com.youku.player.goplay.PlayerCustomInfoManager;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.PlayerCustomErrorInfo;
import com.youku.player.module.PlayerCustomInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.module.VideoUrlInfo.Source;
import com.youku.player.ui.R;
import com.youku.player.ui.interf.IGetVideoAdvService;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.ui.interf.IPlayerCustomCallback;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;
//domob ad
//mobisage
//punchbox

public class PluginFullScreenPauseAD extends PluginOverlay implements
		DetailMessage {

	LayoutInflater mLayoutInflater;
	View containerView;
	TextView endPage;
	YoukuBasePlayerActivity mActivity;
	IMediaPlayerDelegate mediaPlayerDelegate;
	private ImageView closeBtn;
	private ImageView adImageView;

	private int mAdType = AdVender.YOUKU;
	private int mAdForward = AdForward.BROWSER;
	// punchbox ad
	private LinearLayout mPunchboxContainer;
	private com.punchbox.ads.InterstitialAd mPunchboxAd = null;
	private AdPunchBoxListener mPunchBoxListener = null;

	// mobisage ad
	private LinearLayout mMobisageContainer;
	MobiSageAdProductPlacement mMobisageAd = null;
	private AdMobisageListener mMobisageListener;
	LayoutParams mParams;

	//domob ad
	private RelativeLayout mDomobContainer;
	private DomobVideoInterstitialAd mDomobAd = null;
    private AdDomobListener mDomobListener = null;

	private static Handler mHandler = new Handler();

	public PluginFullScreenPauseAD(YoukuBasePlayerActivity context,
			IMediaPlayerDelegate mediaPlayerDelegate) {
		super(context, mediaPlayerDelegate);
		this.mediaPlayerDelegate = mediaPlayerDelegate;
		mActivity = context;
		mLayoutInflater = LayoutInflater.from(context);
		init(context);
	}

	private void init(Context context) {
		containerView = mLayoutInflater.inflate(
				R.layout.yp_plugin_player_popup_ad, null);
		addView(containerView);
		findView();
	}

	private void findView() {
		closeBtn = (ImageView) containerView
				.findViewById(R.id.btn_close_pausead);
		adImageView = (ImageView) containerView
				.findViewById(R.id.plugin_pause_ad_image);
		mPunchboxContainer = (LinearLayout) containerView
				.findViewById(R.id.custom_fulladd);

		closeBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				PluginFullScreenPauseAD.this.setVisible(false);
			}
		});
		adImageView.setOnClickListener(null);

		// init mobisage view
		mMobisageContainer = (LinearLayout) containerView
				.findViewById(R.id.ad_mobisage);

		mDomobContainer = (RelativeLayout) containerView
				.findViewById(R.id.ad_domobContainer);

		containerView.setVisibility(View.GONE);
	}

	// private void showLoading() {
	// myEndLoading.setVisibility(View.VISIBLE);
	// myEndLoading.startAnimation();
	// }
	//
	// private void hideLoading() {
	// myEndLoading.stopAnimation();
	// myEndLoading.setVisibility(View.GONE);
	// }

	// private OnClickListener clickListener = new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// switch (v.getId()) {
	// case R.id.btn_close: {
	// PluginFullScreenPauseAD.this.setVisible(false);
	// break;
	// }
	// default:
	// break;
	// }
	// };
	// };
	private String TAG = "PluginFullScreenPauseAD";
	protected VideoAdvInfo pauseADVideoAdvInfo = null;

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
		if (!firstLoaded) {
			firstLoaded = true;
		}
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dismissPauseAD();
			}
		});
	}

	@Override
	public void onLoadedListener() {
		// pauseADcanceled = false;
	}

	@Override
	public void onLoadingListener() {
		// pauseADcanceled = true;
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
			dismissPauseAD();
		}
	}

	@Override
	public void onPluginAdded() {
		super.onPluginAdded();
		containerView.setVisibility(View.GONE);
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

	private boolean pauseADcanceled = false;

	private boolean isVideoNoAdv() {

		VideoUrlInfo videoInfo = mMediaPlayerDelegate.videoInfo;
		boolean notFromYouku = videoInfo.mSource != Source.YOUKU;

		if (notFromYouku) {
			Logger.d("PlayFlow", "PluginImageAD->isVideoNoAdv = true");
			return true;
		}

		boolean isWifi = Util.isWifi();
		boolean isLocalVideo = isLocalVideo(videoInfo);

		if (!isWifi && isLocalVideo) {
			Logger.d("PlayFlow", "PluginImageAD->isVideoNoAdv = true");
			return true;
		}

		Logger.d("PlayFlow", "PluginImageAD->isVideoNoAdv = false");
		return false;
	}

	private boolean isLocalVideo(VideoUrlInfo videoInfo) {
		if (videoInfo == null) {
			return false;
		}
		return videoInfo.playType.equals(StaticsUtil.PLAY_TYPE_LOCAL);
	}

	/**
	 * 开始获取暂停广�?
	 */
	public void showPauseAD() {

		Logger.e("PlayFlow", "暂停广告showPauseAD id:"
				+ mMediaPlayerDelegate.videoInfo.getVid());

		if (mMediaPlayerDelegate.isADShowing) {
			return;
		}
		if (isVideoNoAdv() || Profile.from == Profile.PHONE_BROWSER) {
			return;
		}

		final boolean isOfflineAd = isLocalVideo(mMediaPlayerDelegate.videoInfo);
		// 只有youku请求离线广告
		if (isOfflineAd && !MediaPlayerConfiguration.getInstance().showOfflineAd())
			return;
		pauseADcanceled = false;
//		IGetVideoAdvService getVideoAdvService = new GetVideoAdvService();

		PlayerCustomInfoManager customInfoManager = new PlayerCustomInfoManager();
		customInfoManager.getPlayerCustomInfo(mMediaPlayerDelegate.videoInfo.getVid(), new IPlayerCustomCallback(){

			@Override
			public void onSuccess(PlayerCustomInfo playerCustomInfo) {
				// TODO Auto-generated method stub
				Logger.d("PlayFlow","pause ad, get player custom info atm: " + playerCustomInfo.getAtm());
				String atm = playerCustomInfo.getAtm();
				String token = playerCustomInfo.getToken();
				
				getPauseAd(atm,isOfflineAd);
			}

			@Override
			public void onError(PlayerCustomErrorInfo errorInfo) {
				// TODO Auto-generated method stub
				int errorCode = errorInfo.getErrorCode();
				Logger.e("PlayFlow","pause ad, verify client_id:" + errorCode+ " des: " + errorInfo.getDescription());
				getPauseAd("",isOfflineAd);
			}

			@Override
			public void onFailed(GoplayException e) {
				// TODO Auto-generated method stub
				Logger.e("PlayFlow","pause ad, get atm error:" + e.getErrorInfo());
				getPauseAd("",isOfflineAd);
			}
			
		});

	}
	
	private void getPauseAd(String atm,boolean isOfflineAd){
		IGetVideoAdvService getVideoAdvService = com.youku.player.util.RemoteInterface.getVideoAdvService;
		if (!TextUtils.isEmpty(mMediaPlayerDelegate.videoInfo.getVid())) {
			getVideoAdvService.getVideoAdv(atm,false,
					mMediaPlayerDelegate.videoInfo.getVid(), mActivity,
					mMediaPlayerDelegate.isFullScreen, isOfflineAd,
					new IGetAdvCallBack() {

						@Override
						public void onSuccess(VideoAdvInfo videoAdvInfo) {
							pauseADVideoAdvInfo = videoAdvInfo;
							if (videoAdvInfo != null) {
								for (AdvInfo advInfo : videoAdvInfo.VAL) {
									if ("2".equals(advInfo.VT)) {
										DisposableStatsUtils
												.disposePausedVC(advInfo);
										videoAdvInfo.VAL.remove(advInfo);
									}
								}
							}
							if (pauseADVideoAdvInfo != null) {
								int size = pauseADVideoAdvInfo.VAL.size();
								if (size == 0) {
									mADURL = "";
									Logger.d("PlayFlow", "暂停广告VC:为空");
								}
								for (int i = 0; i < size; i++) {
									mADURL = pauseADVideoAdvInfo.VAL.get(i).RS;
									mADClickURL = pauseADVideoAdvInfo.VAL
											.get(i).CU;
									mAdForward = pauseADVideoAdvInfo.VAL.get(i).CUF;
									mAdType = pauseADVideoAdvInfo.VAL.get(i).SDKID;
									if (mAdType == AdVender.YOUKU
											&& mADURL != null
											&& !mADURL.equals("")) {
										DisposableStatsUtils
												.disposePausedSUS(pauseADVideoAdvInfo.VAL
														.get(i));
										DisposableStatsUtils
												.disposePausedVC(pauseADVideoAdvInfo.VAL
														.get(i));
									}
								}
							}
							// 用于测试 假设每次都能取到图片
							// mADURL =
							// "http://g4.ykimg.com/11270F1F46509C3F5716DA0123193CA669B69C-09D5-BA6A-22B6-2EE5F6CD4A55";
							Logger.d("PlayFlow", "暂停广告地址 imageURL--->" + mADURL);
							if (mAdType == AdVender.YOUKU
									&& (mADURL == null || mADURL.equals("")))
								return;
							showADImage();
						}

						@Override
						public void onFailed(GoplayException e) {
							disposeAdLoss(URLContainer.AD_LOSS_STEP2);
						}
					});
		}
	}

	private String mADURL;
	private String mADClickURL;

	/**
	 * 获取暂停广告信息去加�?
	 */
	protected void showADImage() {
		if ((mediaPlayerDelegate != null && !mediaPlayerDelegate.isFullScreen)
				|| mActivity.isFinishing()) {
			disposeAdLoss(URLContainer.AD_LOSS_STEP1);
			return;
		}
		// 显示暂停广告
		AdvInfo advInfo = null;
		try {
			// 显示广告的时候是使用
			advInfo = getAdvInfo();
			if (advInfo == null) {
				Logger.e("PlayFlow", "暂停广告显示 SUS:为空");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.d("DisposableHttpTask", "暂停广告显示 SUS为空");
		} finally {
			if (advInfo != null) {
				Logger.d("PlayFlow", "暂停广告SDK --->" + mAdType);
				switch (mAdType) {
				case AdVender.YOUKU:
					loadImage(adImageView, mADURL, advInfo);
					break;
				case AdVender.PUNCHBOX:
					startPunchBoxAd();
					break;
				case AdVender.ADSAGE:
					startMobisageAd();
					break;
				case AdVender.DOMOB:
					startDomobAd();
					break;
				}
				mActivity.releaseInvestigate();
			}
		}
	}

	public void loadImage(final ImageView imageView, final String url,
			final AdvInfo advInfo) {
		setAdType(AdVender.YOUKU);
		new YoukuAsyncTask<Void, Void, Bitmap>() {
			private boolean isLoaded = false;

			@Override
			protected Bitmap doInBackground(Void... Void) {
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						if (!isLoaded) {
							disposeAdLoss(URLContainer.AD_LOSS_STEP4);
						}
						cancel(true);
					}
				}, 30000);
				Bitmap bitmap = loadImageFromUrl(url);
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap d) {
				if (d == null) {
					return;
				}
				isLoaded = true;
				if (!mediaPlayerDelegate.isFullScreen) {
					disposeAdLoss(URLContainer.AD_LOSS_STEP3);
					return;
				}
				if (!pauseADcanceled && isContainerViewHide()) {
					showADImageWhenLoaded();
					Logger.e("PlayFlow", "暂停广告加载成功");
				} else {
					disposeAdLoss(URLContainer.AD_LOSS_STEP3);
				}
				removeAllAd();
				imageView.setImageBitmap(d);
			}
		}.execute();
	}

	private boolean isContainerViewHide() {
		return containerView.getVisibility() != View.VISIBLE;
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
			if (bitDrawable == null) {
				return null;
			}
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
	 * 暂停广告获取成功 去显�?
	 */
	private void showADImageWhenLoaded() {
		if (null != mADClickURL && TextUtils.getTrimmedLength(mADClickURL) > 0) {
			adImageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Logger.e("PlayFlow", "点击:" + mADClickURL);
					AdvInfo advInfo = getAdvInfo();
					// 用户点击跳转发送CUM
					DisposableStatsUtils.disposeCUM(advInfo);
					dismissPauseAD();
					if (mADClickURL.endsWith(".apk")
							&& IMediaPlayerDelegate.mIDownloadApk != null
							&& mediaPlayerDelegate != null && !Util.isWifi()) {
						creatSelectDownloadDialog(mActivity);
						return;
					}
					new AdvClickProcessor().processAdvClick(mActivity,
							mADClickURL, mAdForward);
				}
			});
		} else {
			adImageView.setOnClickListener(null);
		}
		setVisible(true);
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
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create();
		alertDialog.setCancelable(false);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}

	/**
	 * 获取广告信息
	 * 
	 * @return
	 */
	private AdvInfo getAdvInfo() {
		try {
			return pauseADVideoAdvInfo.VAL.get(0);
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 不显示暂停广告蒙�?
	 */
	public void dismissPauseAD() {
		pauseADcanceled = true;
		if (containerView.getVisibility() == View.VISIBLE) {
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					containerView.setVisibility(View.GONE);
					if (mPunchboxAd != null) {
						mPunchboxAd.dismiss();
						mPunchboxContainer.removeAllViews();
						mPunchboxAd = null;
					}

					if (mMobisageAd != null) {
						mMobisageAd.destoryAdView();
						mMobisageContainer.removeAllViews();
						mMobisageAd = null;
					}
					if (mDomobAd != null) {
						mDomobAd.closeVideoInterstitialAd();
						mDomobContainer.removeAllViews();
						mDomobAd = null;
					}
				}
			});
			if (mAdType == AdVender.YOUKU) {
				AdvInfo advInfo = getAdvInfo();
				DisposableStatsUtils.disposePausedSUE(advInfo);
			}
		}
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

	private void disposeAdLoss(int step) {
		DisposableStatsUtils.disposeAdLoss(mActivity, step,
				SessionUnitil.playEvent_session, URLContainer.AD_LOSS_MP);
	}

	private void setAdType(int adType) {
		mAdType = adType;
		switch (adType) {
		case AdVender.PUNCHBOX: {
			closeBtn.setVisibility(View.GONE);
			adImageView.setVisibility(View.GONE);
			mMobisageContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.GONE);
			mPunchboxContainer.setVisibility(View.VISIBLE);
			break;
		}

		case AdVender.ADSAGE: {
			closeBtn.setVisibility(View.GONE);
			adImageView.setVisibility(View.GONE);
			mPunchboxContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.GONE);
			mMobisageContainer.setVisibility(View.VISIBLE);
			break;
		}

		case AdVender.DOMOB: {
			closeBtn.setVisibility(View.GONE);
			adImageView.setVisibility(View.GONE);
			mPunchboxContainer.setVisibility(View.GONE);
			mMobisageContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.VISIBLE);
			break;
		}
		default: {
			mPunchboxContainer.setVisibility(View.GONE);
			mMobisageContainer.setVisibility(View.GONE);
			mDomobContainer.setVisibility(View.GONE);
			adImageView.setVisibility(View.VISIBLE);
			closeBtn.setVisibility(View.VISIBLE);
			break;
		}
		}
	}

	private void removeAllAd() {
		if (mPunchboxAd != null) {
			mPunchboxAd.dismiss();
			mPunchboxContainer.removeAllViews();
			mPunchboxAd = null;
		}

		if (mMobisageAd != null) {
			mMobisageAd.destoryAdView();
			mMobisageContainer.removeAllViews();
			mMobisageAd = null;
		}
		if (mDomobAd != null) {
			mDomobAd.closeVideoInterstitialAd();
			mDomobContainer.removeAllViews();
			mDomobAd = null;
		}
	}

	public void release() {
		if (mPunchboxAd != null) {
			mPunchboxAd.destroy();
			mPunchboxAd = null;
		}
		if (mMobisageAd != null) {
			mMobisageAd.destoryAdView();
			mMobisageAd = null;
		}
		if (mDomobAd != null) {
			mDomobAd.closeVideoInterstitialAd();
			mDomobContainer.removeAllViews();
			mDomobAd = null;
		}
		PluginImageAD.isMobiSageSDKInit = false;
		PluginImageAD.releasePunchboxSdk();
	}

	// PunchBox ad related
	private void startPunchBoxAd() {
		setAdType(AdVender.PUNCHBOX);
		PluginImageAD.initPunchboxSdk();
		removeAllAd();
		mPunchboxAd = new InterstitialAd(mActivity);
		mPunchboxAd.setCloseMode(1);
		if (mPunchBoxListener == null) {
			mPunchBoxListener = new AdPunchBoxListener();
		}
		mPunchboxAd.setAdListener(mPunchBoxListener);
		mPunchboxAd.donotReloadAfterClose();
		AdRequest adRequest = new AdRequest();
		adRequest.setOrientation(2);
		mPunchboxAd.loadAd(adRequest);
		Logger.d("PlayFlow", "start to show punchbox pause ad");
		AdvInfo advInfo = getAdvInfo();
		DisposableStatsUtils.disposePausedSUS(advInfo);
	}

	private class AdPunchBoxListener implements AdListener {

		@Override
		public void onDismissScreen() {
			// dismissPauseAD();
		}

		@Override
		public void onFailedToReceiveAd(PBException arg0) {
			disposeAdLoss(URLContainer.AD_LOSS_STEP4);
		}

		@Override
		public void onPresentScreen() {
			if (mediaPlayerDelegate != null && mediaPlayerDelegate.isFullScreen
					&& mAdType == AdVender.PUNCHBOX && !pauseADcanceled) {
				setVisible(true);
				setVisibility(View.VISIBLE);
			} else {
				disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			}
		}

		@Override
		public void onReceiveAd() {
			try {
				if (mPunchboxAd != null && mPunchboxAd.isReady()
						&& mAdType == AdVender.PUNCHBOX) {
					mPunchboxContainer.removeAllViews();
					mPunchboxAd.show(mPunchboxContainer, null);
				} else {
					disposeAdLoss(URLContainer.AD_LOSS_STEP3);
				}
			} catch (Exception e) {
				// 当设置的scale不在范围内，或者isReady()属性为false
				e.printStackTrace();
			}
		}
	}

	// mobisage ad
	private void startMobisageAd() {
		setAdType(AdVender.ADSAGE);
		removeAllAd();
		if (!PluginImageAD.isMobiSageSDKInit) {
			MobiSageManager.getInstance().setPublisherID(mActivity,
					AdVender.MobiSage_ID);
			PluginImageAD.isMobiSageSDKInit = true;
		}
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay()
				.getMetrics(displayMetrics);
		mMobisageAd = new MobiSageAdProductPlacement(mActivity, true);

		if (mMobisageListener == null) {
			mMobisageListener = new AdMobisageListener();
		}
		mMobisageAd.setMobiSageAdProductPlacementListener(mMobisageListener);
		if (mParams == null) {
			mParams = new LayoutParams((int) displayMetrics.density * 300,
					(int) displayMetrics.density * 250);

		}
		mMobisageContainer.addView(mMobisageAd, mParams);
		AdvInfo advInfo = getAdvInfo();
		DisposableStatsUtils.disposePausedSUS(advInfo);
	}

	private class AdMobisageListener implements
			MobiSageAdProductPlacementListener {

		@Override
		public void onMobiSageProductPlacementClick(
				MobiSageAdProductPlacement arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMobiSageProductPlacementClose(
				MobiSageAdProductPlacement arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onMobiSageProductPlacementError(
				MobiSageAdProductPlacement arg0) {
			disposeAdLoss(URLContainer.AD_LOSS_STEP4);
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
					if (mediaPlayerDelegate != null
							&& mediaPlayerDelegate.isFullScreen
							&& mAdType == AdVender.ADSAGE && !pauseADcanceled) {
						setVisible(true);
						setVisibility(View.VISIBLE);
					} else {
						disposeAdLoss(URLContainer.AD_LOSS_STEP3);
					}
				}
			});

		}

	}

	// Domob ad
	private void startDomobAd() {
		setAdType(AdVender.DOMOB);
		removeAllAd();
		mDomobAd = new DomobVideoInterstitialAd(mActivity, AdVender.Domob_ID,
				AdVender.Domob_PauseAd_ID, 1200, 1000);
		if (mDomobListener == null) {
			mDomobListener = new AdDomobListener();
		}
		mDomobAd.setVideoInterstitialAdListener(mDomobListener);
		mDomobAd.loadVideoInterstitialAd();
		Logger.d("PlayFlow", "start to show Domob pause ad");
		AdvInfo advInfo = getAdvInfo();
		DisposableStatsUtils.disposePausedSUS(advInfo);
	}
		
		private class AdDomobListener implements DomobVideoInterstitialAdListener {

			@Override
		public void onVideoInterstitialAdReady() {
			if (mDomobAd != null && !pauseADcanceled
					&& mAdType == AdVender.DOMOB) {
				mDomobAd.showVideoInterstitialAd(getSceneInfo());
			} else {
				disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			}
		}

			@Override
		public void onVideoInterstitialAdPresent() {
			if (mediaPlayerDelegate != null && mediaPlayerDelegate.isFullScreen
					&& mAdType == AdVender.DOMOB && !pauseADcanceled) {
				setVisible(true);
				setVisibility(View.VISIBLE);
			} else {
				disposeAdLoss(URLContainer.AD_LOSS_STEP3);
			}
		}

			@Override
			public void onVideoInterstitialAdLeaveApplication() {
			}

			@Override
			public void onVideoInterstitialAdFailed(ErrorCode code) {
				disposeAdLoss(URLContainer.AD_LOSS_STEP4);
			}

			@Override
			public void onVideoInterstitialAdDismiss() {
			}

			@Override
			public void onVideoInterstitialAdClicked() {
				dismissPauseAD();
			}

			@Override
			public void onLandingPageOpen() {
			}

			@Override
			public void onLandingPageClose() {
			}
		}

		private DomobSceneInfo getSceneInfo() {
			DomobSceneInfo sceneInfo = new DomobSceneInfo();
			sceneInfo.setContext(mActivity);
			sceneInfo.setCoordinatesX(0);
			sceneInfo.setCoordinatesY(0);
			sceneInfo.setParentViewGroup(mDomobContainer);
			sceneInfo.setSceneId("sceneId");
			int frameWidth = mDomobContainer.getLayoutParams().width;
			int frameHeight = mDomobContainer.getLayoutParams().height;
			sceneInfo.setSecureAreaWidth(frameWidth);
			sceneInfo.setSecureAreaHeight(frameHeight);
			return sceneInfo;
		}
}