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

package com.youku.player.base;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.baseproject.utils.Logger;
import com.baseproject.utils.UIUtils;
import com.baseproject.utils.Util;
import com.youku.player.ApiBaseManager;
import com.youku.player.NewSurfaceView;
import com.youku.player.Track;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.plugin.MediaPlayerObserver;
import com.youku.player.plugin.PluginManager;
import com.youku.player.plugin.PluginOverlay;
import com.youku.player.service.DisposableHttpCookieTask;
import com.youku.player.ui.R;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.ui.widget.FitScaleImageView;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.PlayCode;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.URLContainer;
import com.youku.statistics.PlayerStatistics;
import com.youku.uplayer.MPPErrorCode;

/**
 * 播放器界面,不需要计算播放器高度 xml <com.youku.player.base.YoukuPlayerView
 * android:id="@+id/full_holder" android:layout_width="fill_parent"
 * android:layout_height="wrap_content" >
 * </com.youku.player.base.YoukuPlayerView>
 * 
 * @author longfan
 * @time 2013年5月7日10:28:42
 */

@SuppressLint("InlinedApi")
public class YoukuPlayerView extends PluginOverlay implements DetailMessage,
		MediaPlayerObserver {

	private Context mContext;
	private YoukuBasePlayerActivity mActivity;
	public static String TAG = "YoukuPlayerView";
	int position = -1;
	public NewSurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	YoukuPlayerView fullStub;
	View surfaceBlack;
	TextView playerDebugView;
	PluginManager pluginManager;
	private YoukuPlayer player;
	int fullWidth, fullHeight;// , smallWidth, smallHeight;
	public boolean firstOnloaded = false;
	public boolean realVideoStart = false;
	public static Handler handler = new Handler() {

	};

	public boolean autoPaly = true;// 是否自动播放

	RelativeLayout leftSpace;
	RelativeLayout rightSpace;
	RelativeLayout topSpace;
	RelativeLayout bottomSpace;
	
	RelativeLayout spaceMiddle;

	// 播放器来源
	String from;

	// 是否来自于收藏页
	boolean isFromFav = false;

	// 是否第一次加载成功

	public static final int END_REQUEST = 201;

	public static final int END_PLAY = 202;

	// 分享登录请求
	public static final int LOGIN_REQUEST = 301;

	// 因为切换到3g暂停
	boolean is3GPause = false;
	VideoView cc;

	public YoukuPlayerView(Context context) {
		super(context);
		init(context);
	}

	public YoukuPlayerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public YoukuPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private LayoutInflater inflater;
//	private RelativeLayout padLandBottomInteract;

	/**
	 * 找到播放器界面的layout并初始化
	 * 
	 * @param context
	 */
	private void init(Context context) {
		mContext = context;

		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.yp_player_view, null);
		this.addView(view);
		initLayout();
		initPlayerBlank();
	}

	private FitScaleImageView playback;

	/**
	 * 初始化surface 界面的debug信息
	 */
	private void initLayout() {
		surfaceView = (NewSurfaceView) findViewById(R.id.surface_view);
		playback = (FitScaleImageView) findViewById(R.id.player_back);
		// padLandBottomInteract = (RelativeLayout)
		// findViewById(R.id.player_pad_blow_interact);
		// padLandBottomInteract.setVisibility(View.GONE);
		playerDebugView = (TextView) findViewById(R.id.surface_view_debug);
		surfaceBlack = (View) findViewById(R.id.surface_black);

		spaceMiddle = (RelativeLayout) findViewById(R.id.space_middle);
		
		if (UIUtils.hasKitKat() && hasVirtualButtonBar(mContext))
			addLeftAndRight();
	}

	/**
	 * 带有虚拟键的4.4设备转屏会出现半屏，临时方案是通过给surfaceview左右添加view
	 */
	private void addLeftAndRight() {
		View viewLeft = new View(mContext);
		viewLeft.setId(1999);
		viewLeft.setVisibility(View.INVISIBLE);
		RelativeLayout.LayoutParams leftLayoutParams = new RelativeLayout.LayoutParams(
				0, 0);
		leftLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		viewLeft.setLayoutParams(leftLayoutParams);
		spaceMiddle.addView(viewLeft);

		View viewRight = new View(mContext);
		viewRight.setId(1998);
		viewRight.setVisibility(View.INVISIBLE);
		RelativeLayout.LayoutParams rightLayoutParams = new RelativeLayout.LayoutParams(
				0, 0);
		rightLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		viewRight.setLayoutParams(rightLayoutParams);
		spaceMiddle.addView(viewRight);

		RelativeLayout.LayoutParams surfaceLayoutParams = (RelativeLayout.LayoutParams) surfaceView
				.getLayoutParams();
		surfaceLayoutParams.addRule(RelativeLayout.RIGHT_OF, viewLeft.getId());
		surfaceLayoutParams.addRule(RelativeLayout.LEFT_OF, viewRight.getId());
	}

	private static final boolean DEBUG = false;

	/**
	 * 设置debug信息
	 * 
	 * @param debug
	 *            需要显示到播放器界面上的信息
	 */
	public void setDebugText(final String debug) {
		if (!DEBUG)
			return;
		if (null != playerDebugView) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					playerDebugView.append("\n" + debug);
				}
			});
		}
	}

	/**
	 * 初始化播放画面四周的空白黑色区域
	 */
	private void initPlayerBlank() {
		leftSpace = (RelativeLayout) findViewById(R.id.space_left);
		rightSpace = (RelativeLayout) findViewById(R.id.space_right);
		topSpace = (RelativeLayout) findViewById(R.id.space_top);
		bottomSpace = (RelativeLayout) findViewById(R.id.space_bottom);
	}

	int Adaptation_lastPercent = 0;

	@Override
	public void onBufferingUpdateListener(final int percent) {
		((Activity) mActivity).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (percent == 100 && Adaptation_lastPercent != 100) {
					Adaptation_lastPercent = percent;
					return;
				}
			}

		});
	}

	@Override
	public void onCompletionListener() {
		setDebugText("播放完成onCompletionListener");
		setPlayerBlack();
	}

	@Override
	public boolean onErrorListener(int what, int extra) {
		if ((what == MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR
				|| what == MPPErrorCode.MEDIA_INFO_NETWORK_DISSCONNECTED
				|| what == MPPErrorCode.MEDIA_INFO_NETWORK_CHECK
				|| what == MPPErrorCode.MEDIA_INFO_NETWORK_ERROR || what == MPPErrorCode.MEDIA_INFO_PREPARE_TIMEOUT_ERROR)
				&& null != mMediaPlayerDelegate.videoInfo
				&& !mMediaPlayerDelegate.videoInfo.IsSendVV) {
			if (PlayerUtil
					.isBaiduQvodSource(mMediaPlayerDelegate.videoInfo.mSource)) {
				Track.onError(mContext,
						mMediaPlayerDelegate.videoInfo.getVid(), Profile.GUID,
						StaticsUtil.PLAY_TYPE_NET, PlayCode.VIDEO_LOADING_FAIL,
						mMediaPlayerDelegate.videoInfo.mSource,
						mMediaPlayerDelegate.videoInfo.getCurrentQuality(),
						mMediaPlayerDelegate.videoInfo.getProgress(),
						mMediaPlayerDelegate.isFullScreen);
			} else if (!StaticsUtil.PLAY_TYPE_LOCAL
					.equals(mMediaPlayerDelegate.videoInfo.playType)) {
				Track.onError(mContext,
						mMediaPlayerDelegate.videoInfo.getVid(), Profile.GUID,
						mMediaPlayerDelegate.videoInfo.playType,
						PlayCode.VIDEO_LOADING_FAIL,
						mMediaPlayerDelegate.videoInfo.mSource,
						mMediaPlayerDelegate.videoInfo.getCurrentQuality(),
						mMediaPlayerDelegate.videoInfo.getProgress(),
						mMediaPlayerDelegate.isFullScreen);
			}
		}
		
		if (null != mMediaPlayerDelegate.videoInfo
				&& StaticsUtil.PLAY_TYPE_LOCAL
						.equals(mMediaPlayerDelegate.videoInfo.playType)) {
			if (what == MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR
					|| what == MPPErrorCode.MEDIA_INFO_NETWORK_CHECK
					|| what == MPPErrorCode.MEDIA_INFO_NETWORK_DISSCONNECTED
					|| what == MPPErrorCode.MEDIA_INFO_SEEK_ERROR) {
				Track.onError(mContext,
						mMediaPlayerDelegate.videoInfo.getVid(), Profile.GUID,
						mMediaPlayerDelegate.videoInfo.playType,
						PlayCode.VIDEO_NOT_EXIST,
						mMediaPlayerDelegate.videoInfo.mSource,
						mMediaPlayerDelegate.videoInfo.getCurrentQuality(),
						mMediaPlayerDelegate.videoInfo.getProgress(),
						mMediaPlayerDelegate.isFullScreen);
			}
		}
		
		// 使用系统播放器播放的时候
		if (!PlayerUtil.useUplayer() && what == 1
				&& mMediaPlayerDelegate != null
				&& mMediaPlayerDelegate.videoInfo != null) {
			Track.onError(mContext, mMediaPlayerDelegate.videoInfo.getVid(),
					Profile.GUID, mMediaPlayerDelegate.videoInfo.playType,
					PlayCode.VIDEO_LOADING_FAIL,
					mMediaPlayerDelegate.videoInfo.mSource,
					mMediaPlayerDelegate.videoInfo.getCurrentQuality(),
					mMediaPlayerDelegate.videoInfo.getProgress(),
					mMediaPlayerDelegate.isFullScreen);
		}

		Track.changeVideoQualityOnError(mActivity);
		Track.mIsChangingLanguage = false;
		mMediaPlayerDelegate.onVVEnd();
		return false;
	}

	private final String tag = "YoukuPlayerView";

	@Override
	public void OnPreparedListener() {
		Logger.e(tag, " OnPreparedListener()");
	}

	@Override
	public void OnSeekCompleteListener() {
		setDebugText("seek完成OnSeekCompleteListener");
		Logger.e(TAG, "OnSeekCompleteListener");
	}

	int mVideoWidth;
	int mVideoHeight;

	@Override
	public void OnVideoSizeChangedListener(int width, int height) {
		Logger.e(TAG, "width-->" + width + "height-->" + height);
		if (mVideoHeight == height && mVideoWidth == width) {
			return;
		}
		mVideoWidth = width;
		mVideoHeight = height;
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				resizeMediaPlayer(true);
			}
		});

	}

	@Override
	public void OnTimeoutListener() {
		setDebugText("超时 OnTimeoutListener");
	}

	public static final String PLAY_LOG_URL = "http://v.youku.com/player/wplaylog";
	@Override
	public void OnCurrentPositionChangeListener(int currentPosition) {
		if (mMediaPlayerDelegate != null
				&& mMediaPlayerDelegate.videoInfo != null) {
			if (!mMediaPlayerDelegate.isComplete)
				mMediaPlayerDelegate.videoInfo.setProgress(currentPosition);
			// 付费视频需要在10分钟时候发送统计
			if (mMediaPlayerDelegate.videoInfo.paid
					&& !mMediaPlayerDelegate.videoInfo.paidSended
					&& currentPosition / 1000 == 600) {
				new DisposableHttpCookieTask(PLAY_LOG_URL + "?vid="
						+ mMediaPlayerDelegate.videoInfo.getVid()).start();
				mMediaPlayerDelegate.videoInfo.paidSended = true;
			}
			if (mMediaPlayerDelegate.videoInfo.getLookTen() == 1
					&& currentPosition / 1000 >= 600) {
				mActivity.onPayClick();
			}
		}
		setPlayerBlackGone();
	}

	@Override
	public void onLoadedListener() {
		setDebugText("缓冲完成onLoadedListener");
		if (!firstOnloaded) {
			firstOnloaded = true;
		}
		if (surfaceBlack.getVisibility() == View.VISIBLE) {
			setPlayerBlackGone();
		}
		// Track.onRealVideoFirstLoadEnd();
		Track.onChangVideoQualityEnd(mActivity);
		if (Track.mIsChangingLanguage) {
			Track.mIsChangingLanguage = false;
		}
	}

	@Override
	public void onLoadingListener() {
		setDebugText("缓冲中onLoadingListener");
	}

	int videoSize = IMediaPlayerDelegate.PLAY_100;

	private SharedPreferences sp;

	/**
	 * 调整播放画面的宽高比
	 * 
	 * @param force
	 *            是否强制刷新播放器宽高
	 */
	public void resizeMediaPlayer(boolean force) {
		if (mMediaPlayerDelegate != null) {
			if (mMediaPlayerDelegate.isFullScreen) {
				videoSize = sp.getInt("video_size", 100);
			} else {
				videoSize = 100;
			}
			resizeVideoView(videoSize, force);
		}
	}

	/**
	 * 全屏的时候设置全屏
	 */
	public void setFullscreenBack() {
		this.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		playback.isFullscreen = true;
		this.setBackgroundColor(getResources().getColor(R.color.black));
	}

	int landPlayheight;
	int landPlaywidth;
	int playwidth;
	int playheight;
	int lastFullHeight;
	int lastFullWidth;
	int lastpercent;
	int lastOrientation;

	/**
	 * 重新调整视频的画面
	 * 
	 * @param percent
	 *            画面百分比
	 * @param force
	 *            是否强制刷新
	 */
	public void resizeVideoView(int percent, boolean force) {
		int showWidth = 0, showHeight = 0;
		// 控件的宽高
		fullHeight = playback.getHeight();
		fullWidth = playback.getWidth();
		int orientation = mMediaPlayerDelegate.mediaPlayer == null ? 0
				: mMediaPlayerDelegate.mediaPlayer.getVideoOrientation();
		if (lastpercent == percent && fullWidth == lastFullWidth
				&& fullHeight == lastFullHeight
				&& lastOrientation == orientation && !force) {
			return;
		}

		if (mMediaPlayerDelegate == null) {
			return;
		}
		if (percent == -1) {// 满屏
			if (mMediaPlayerDelegate.isFullScreen) {
				showWidth = fullWidth;
				showHeight = fullHeight;
			} else if (Util.isLandscape(mContext)) {
				showWidth = landPlaywidth;
				showHeight = landPlayheight;
			} else {
				showWidth = playwidth;
				showHeight = playheight;
			}
		} else {
			int resizeScreenWidth = 0, resizeScreenHeight = 0;
			if (percent == 50) {// 50%
				// 控件的一般高
				resizeScreenWidth = fullWidth / 2;
				resizeScreenHeight = fullHeight / 2;
			} else if (percent == 75) {// %75
				resizeScreenWidth = fullWidth * 3 / 4;
				resizeScreenHeight = fullHeight * 3 / 4;
			} else {// %100
				resizeScreenWidth = fullWidth;
				resizeScreenHeight = fullHeight;
			}
			// 以宽度为基准
			showWidth = resizeScreenWidth;
			// 视频的宽高
			int videoHeight = 0;
			int videoWidth = 0;
			if (orientation == 0 || orientation == 3) {
				videoHeight = mVideoHeight;
				videoWidth = mVideoWidth;
			} else {
				videoHeight = mVideoWidth;
				videoWidth = mVideoHeight;
			}

			if (videoWidth == 0)
				return;

			// 成比例的高度
			showHeight = showWidth * videoHeight / videoWidth;

			// 展示的高于预留d
			if (showHeight > resizeScreenHeight) {
				showHeight = resizeScreenHeight;
				showWidth = resizeScreenHeight * videoWidth / videoHeight;
			} else {
			}
		}

		int leftWidth = (fullWidth - showWidth) / 2;
		int topWidth = (fullHeight - showHeight) / 2;
		surfaceView.setDimensions(showWidth, showHeight);
		RelativeLayout leftSpace = (RelativeLayout) findViewById(R.id.space_left);
		RelativeLayout rightSpace = (RelativeLayout) findViewById(R.id.space_right);
		RelativeLayout topSpace = (RelativeLayout) findViewById(R.id.space_top);
		RelativeLayout bottomSpace = (RelativeLayout) findViewById(R.id.space_bottom);
		leftSpace.setVisibility(View.INVISIBLE);
		rightSpace.setVisibility(View.INVISIBLE);
		topSpace.setVisibility(View.INVISIBLE);
		bottomSpace.setVisibility(View.INVISIBLE);
		RelativeLayout.LayoutParams leftPara = (android.widget.RelativeLayout.LayoutParams) leftSpace
				.getLayoutParams();
		leftPara.height = fullHeight;
		leftPara.width = leftWidth;
		// if (leftWidth > 0) {
		// leftPara.rightMargin = 1;
		// }
		leftSpace.setLayoutParams(leftPara);
		leftSpace.requestLayout();
		RelativeLayout.LayoutParams rightPara = (android.widget.RelativeLayout.LayoutParams) rightSpace
				.getLayoutParams();
		rightPara.height = fullHeight;
		rightPara.width = leftWidth;
		// if (leftWidth > 0) {
		// rightPara.leftMargin = 1;
		// }
		rightSpace.setLayoutParams(rightPara);
		rightSpace.requestLayout();
		RelativeLayout.LayoutParams topPara = (android.widget.RelativeLayout.LayoutParams) topSpace
				.getLayoutParams();
		topPara.height = topWidth;
		topPara.width = fullWidth;
		if (topWidth > 0) {
			topPara.bottomMargin = 1;
		}
		topSpace.setLayoutParams(topPara);
		topSpace.requestLayout();
		RelativeLayout.LayoutParams bottomPara = (android.widget.RelativeLayout.LayoutParams) bottomSpace
				.getLayoutParams();
		bottomPara.height = topWidth;
		bottomPara.width = fullWidth;
		// if (topWidth > 0) {
		// bottomPara.topMargin = 1;
		// }
		bottomSpace.setLayoutParams(bottomPara);
		bottomSpace.requestLayout();
		// 解决土豆bug 7721
		++showWidth;
		++showHeight;
		Logger.e("PlayFlow", "changeVideoSize-->" + "showWidth-->" + showWidth
				+ "showHeight-->" + showHeight);
		if (mMediaPlayerDelegate.mediaPlayer != null) {
			mMediaPlayerDelegate.mediaPlayer.changeVideoSize(showWidth,
					showHeight);
		}
		lastFullHeight = fullHeight;
		lastFullWidth = fullWidth;
		lastpercent = percent;
		lastOrientation = orientation;
	}

	int land_height, land_width;
	int port_height, port_width;
	int landMarginLR;
	int landMarginTB;
	int landMarginTop;
	int surfaceMarginTop;
	int getSmallBoderHeight;
	int playTitleHeight;
	private ViewTreeObserver vto;

	int times = 1;

	/**
	 * 当播放器尺寸变化时候调用
	 */
	public void onConfigrationChange() {
		times = 1;
		vto = playback.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(mGlobalLayoutListener);
	}

	OnGlobalLayoutListener mGlobalLayoutListener = new OnGlobalLayoutListener() {

		@Override
		public void onGlobalLayout() {
			resizeMediaPlayer(false);
		}
	};
	/**
	 * pad横屏时候播放器界面占总宽度的比例
	 */
	private final static float WIDTH_RATIO = 0.6625f;

	/**
	 * 设置哼屏幕布局
	 */
	public void setHorizontalLayout()// 设置横屏布局pad
	{
		playback.isFullscreen = false;
		Display getOrient = mActivity.getWindowManager().getDefaultDisplay();
		int playWidth = (int) ((int) getOrient.getWidth() * WIDTH_RATIO);
		this.setLayoutParams(new LinearLayout.LayoutParams(playWidth,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		showBottonInteract();
	}
	
	public void setHorizontalLayout(int height){
		playback.isFullscreen = false;
		this.setLayoutParams(new LinearLayout.LayoutParams(height*16/9,
				LinearLayout.LayoutParams.WRAP_CONTENT));
	}

	/**
	 * 在主客户端pad横屏状态下播放器界面下面的交互区
	 */
	private void showBottonInteract() {
//		padLandBottomInteract.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置竖屏布局
	 */
	public void setVerticalLayout()// 设置竖屏布局
	{
		this.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		playback.isFullscreen = false;
		hideBottonInteract();
	}

	/**
	 * 隐藏pad横屏下的交互区
	 */
	private void hideBottonInteract() {
//		padLandBottomInteract.setVisibility(View.GONE);
	}

	int action_bar_height_port = 0;
	int factor = 1;

	/**
	 * 初始化接口
	 * 
	 * @param mYoukuBaseActivity
	 * @param platformId
	 * @see {@link Plantform}
	 * @param pid
	 *            各平台注册
	 * @param useSystemPlayer
	 *            强制硬解接口，使用这个参数将只能够播放m3u8
	 */
	public void initialize(YoukuBasePlayerActivity mYoukuBaseActivity,
			int platformId, String pid, String verName, String userAgent,
			boolean useSystemPlayer) {

		initialize(mYoukuBaseActivity, platformId, pid, verName, userAgent,
				useSystemPlayer, null, null);
	}
	
//	boolean isTablet = (this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
//	String User_Agent = (isTablet ? "Youku HD;" : "Youku;") + versionName
//			+ ";Android;" + android.os.Build.VERSION.RELEASE + ";"
//			+ android.os.Build.MODEL;
	
	public void initialize(YoukuBasePlayerActivity mYoukuBaseActivity){
		PackageManager pm = mYoukuBaseActivity.getPackageManager();
		String ver = "4.1";
		try {
			ver = pm.getPackageInfo(mYoukuBaseActivity.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean isTablet = (this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		String ua = (isTablet ? "Youku HD;" : "Youku;") + ver
				+ ";Android;" + android.os.Build.VERSION.RELEASE + ";"
				+ android.os.Build.MODEL;
		
		Logger.d(TAG,"initialize(): ua = " + ua);
		
		initialize(mYoukuBaseActivity, 10001, "4e308edfc33936d7", ver, ua, false,-7L,"631l1i1x3fv5vs2dxlj5v8x81jqfs2om");
	}
	
	public void initialize(YoukuBasePlayerActivity mYoukuBaseActivity,String pid){
		PackageManager pm = mYoukuBaseActivity.getPackageManager();
		String ver = "4.1";
		try {
			ver = pm.getPackageInfo(mYoukuBaseActivity.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean isTablet = (this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
		String ua = (isTablet ? "Youku HD;" : "Youku;") + ver
				+ ";Android;" + android.os.Build.VERSION.RELEASE + ";"
				+ android.os.Build.MODEL;
		
		Logger.d(TAG,"initialize(): ua = " + ua);
		
		initialize(mYoukuBaseActivity, 10001, pid, ver, ua, false,-7L,"631l1i1x3fv5vs2dxlj5v8x81jqfs2om");
	}

	/**
	 * 初始化接口
	 * 
	 * @param mYoukuBaseActivity
	 * @param platformId
	 * @see {@link Plantform}
	 * @param pid
	 *            各平台注册
	 * @param useSystemPlayer
	 *            强制硬解接口，使用这个参数将只能够播放m3u8
	 * @param timeStamp
	 *            时间戳
	 * @param secret
	 *            密匙
	 */
	public void initialize(YoukuBasePlayerActivity mYoukuBaseActivity,
			int platformId, String pid, String verName, String userAgent,
			boolean useSystemPlayer, Long timeStamp, String secret) {
//		if(!ApiManager.getInstance().getApiServiceState()) return;
		long begin = SystemClock.elapsedRealtime();
		mActivity = mYoukuBaseActivity;
		Profile.USE_SYSTEM_PLAYER = useSystemPlayer;
		mYoukuBaseActivity.initLayoutView(this);	//MediaPlayerDelegate在这里进行了初始化
		player = new YoukuPlayer(mYoukuBaseActivity);	//-------------此处把MediaPlayerDelegate实例对YoukuPlayer进行了初始化
		Profile.PLANTFORM = platformId;
		Profile.pid = pid;
		Profile.USER_AGENT = userAgent;
		Util.TIME_STAMP = timeStamp;
		Util.SECRET = secret;
		URLContainer.verName = verName;
		URLContainer.getStatisticsParameter();
		MediaPlayerConfiguration.getInstance();
		mYoukuBaseActivity.onInitializationSuccess(player);
		trackPlayerLoad(SystemClock.elapsedRealtime() - begin);
	}
	
	/**
	 * 从播放器初始化到请求视频或广告文件片之前时间
	 * @param duration
	 */
	private void trackPlayerLoad(long duration) {
		long currentTime = System.currentTimeMillis();
		HashMap<String, String> extend = new HashMap<String, String>();
		extend.put("pltype", "playerload");
		extend.put("s", duration + "");
		extend.put("st", (currentTime - duration) + "");
		extend.put("et", currentTime + "");
		AnalyticsWrapper.trackExtendCustomEvent(mContext,
				PlayerStatistics.PALYER_LOAD, PlayerStatistics.PAGE_NAME, null,
				IMediaPlayerDelegate.getUserID(), extend);
	}

	@Override
	public void onUp() {
		Logger.e(TAG, "onUp");
	}

	@Override
	public void onDown() {
		Logger.e(TAG, "onDown");
	}

	@Override
	public void onFavor() {
		Logger.e(TAG, "onFavor");
	}

	@Override
	public void onUnFavor() {
		Logger.e(TAG, "onUnFavor");
	}

	@Override
	public void newVideo() {
		setDebugText("新视频newVideo");
		Logger.e(TAG, "newVideo");
	}

	@Override
	public void onVolumnUp() {
		setDebugText("音量调大onVolumnUp");
		Logger.e(TAG, "onVolumnUp");
	}

	@Override
	public void onVolumnDown() {
		setDebugText("音量调小onVolumnDown");
		Logger.e(TAG, "onVolumnDown");
	}

	@Override
	public void onMute(boolean mute) {
		setDebugText("静音onMute");
		Logger.e(TAG, "onMute");
	}

	@Override
	public void onVideoChange() {
		mActivity.onVideoChange();
		setDebugText("获取信息中onVideoChange");
		Logger.e(TAG, "onVideoChange");
		firstOnloaded = false;
		realVideoStart = false;
	}

	@Override
	public void onVideoInfoGetting() {
		setDebugText("获取信息中onVideoInfoGetting");
		Logger.e(TAG, "onVideoInfoGetting");
		setPlayerBlack();
		realVideoStart = false;
	}

	@Override
	public void onVideoInfoGetted() {
		Logger.e(TAG, "onVideoInfoGetted");
	}

	@Override
	public void onVideoInfoGetFail(boolean needRetry) {
		setDebugText("获取信息失败onVideoInfoGetFail");
		Logger.e(TAG, "onVideoInfoGetFail");
	}

	@Override
	public void setVisible(boolean visible) {
		Logger.e(TAG, "setVisible");
	}

	/**
	 * 播放完成
	 */
	protected void playComplete() {
		Logger.d("PlayFlow", "播放完成");
//		Track.setplayCompleted(true);
		if (mMediaPlayerDelegate != null) {
			mMediaPlayerDelegate.release();
			mMediaPlayerDelegate.videoInfo.setProgress(0);
		}
	}

	@Override
	public void onNotifyChangeVideoQuality() {
		setDebugText("播放清晰度变化onNotifyChangeVideoQuality");
	}

	@Override
	public void onRealVideoStart() {
		setDebugText("正片开始播放 onRealVideoStart");
		realVideoStart = true;
	}

	@Override
	public void onADplaying() {
		setDebugText("广告正在播放 onADplaying");

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

	@Override
	public void back() {
	}

	/**
	 * 设置播放器画面为黑色
	 */
	public void setPlayerBlack() {
		if (surfaceBlack != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					surfaceBlack.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	/**
	 * 去掉播放器的黑色
	 */
	public void setPlayerBlackGone() {
		if (surfaceBlack != null) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					surfaceBlack.setVisibility(View.GONE);
				}
			});
		}
	}

	@Override
	public void onPlayNoRightVideo(GoplayException e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayReleateNoRightVideo() {
		// TODO Auto-generated method stub

	}
	
	@SuppressLint("NewApi")
	public static boolean hasVirtualButtonBar(Context context) {
		if (Build.VERSION.SDK_INT >= 18) {
			return !ViewConfiguration.get(context).hasPermanentMenuKey();
		}else {
			return false;
		}
	}
}
