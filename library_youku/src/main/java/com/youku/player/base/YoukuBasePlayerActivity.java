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


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.baseproject.image.ImageCache;
import com.baseproject.image.ImageFetcher;
import com.baseproject.image.ImageResizer;
import com.baseproject.image.ImageWorker;
import com.baseproject.image.Utils;
import com.baseproject.utils.Logger;
import com.baseproject.utils.UIUtils;
import com.baseproject.utils.Util;
import com.youku.analytics.data.Device;
import com.youku.player.ApiBaseManager;
import com.youku.player.NewSurfaceView;
import com.youku.player.Track;
import com.youku.player.ad.AdType;
import com.youku.player.ad.AdVender;
import com.youku.player.apiservice.ICacheInfo;
import com.youku.player.config.MediaPlayerConfiguration;
import com.youku.player.goplay.Profile;
import com.youku.player.goplay.StaticsUtil;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.PlayVideoInfo;
import com.youku.player.module.VideoCacheInfo;
import com.youku.player.module.VideoHistoryInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.module.VideoUrlInfo.Source;
import com.youku.player.plugin.PluginADPlay;
import com.youku.player.plugin.PluginChangeQuality;
import com.youku.player.plugin.PluginFullScreenPauseAD;
import com.youku.player.plugin.PluginImageAD;
import com.youku.player.plugin.PluginInvestigate;
import com.youku.player.plugin.PluginManager;
import com.youku.player.plugin.PluginOverlay;
import com.youku.player.plugin.PluginPayTip;
import com.youku.player.plugin.PluginSimplePlayer;
import com.youku.player.ui.R;
import com.youku.player.ui.interf.IBaseActivity;
import com.youku.player.ui.interf.IBaseMediaPlayer.OnPlayHeartListener;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.ui.widget.TudouEncryptDialog.OnPositiveClickListener;
import com.youku.player.util.AnalyticsWrapper;
import com.youku.player.util.DetailMessage;
import com.youku.player.util.DeviceOrientationHelper;
import com.youku.player.util.DeviceOrientationHelper.OrientationChangeCallback;
import com.youku.player.util.DisposableStatsUtils;
import com.youku.player.util.MediaPlayerProxyUtil;
import com.youku.player.util.PlayCode;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.PreferenceUtil;
import com.youku.player.util.RemoteInterface;
import com.youku.player.util.SessionUnitil;
import com.youku.player.util.URLContainer;
import com.youku.statistics.OfflineStatistics;
import com.youku.statistics.TaskSendPlayBreak;
import com.youku.uplayer.EGLUtil;
import com.youku.uplayer.MPPErrorCode;
import com.youku.uplayer.OnADCountListener;
import com.youku.uplayer.OnADPlayListener;
import com.youku.uplayer.OnCurrentPositionUpdateListener;
import com.youku.uplayer.OnHwDecodeErrorListener;
import com.youku.uplayer.OnLoadingStatusListener;
import com.youku.uplayer.OnNetworkSpeedListener;
import com.youku.uplayer.OnRealVideoStartListener;
import com.youku.uplayer.OnTimeoutListener;
import com.youku.uplayer.OnVideoIndexUpdateListener;

@SuppressLint("NewApi")
public abstract class YoukuBasePlayerActivity extends IBaseActivity
        implements DetailMessage, OrientationChangeCallback {

    // 挂起时的进度
    int position = 0;
    NewSurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    YoukuPlayerView mYoukuPlayerView;
    PluginManager pluginManager;
    //	IMediaPlayerDelegate mediaPlayerDelegate;
    public boolean autoPaly = true;// 是否自动播放
    PluginFullScreenPauseAD mFullScreenPauseAD;
    protected PluginImageAD mImageAD;
    private PluginInvestigate mInvestigate;
    PluginOverlay mPluginSmallScreenPlay;
    protected PluginPayTip mPaytipPlugin;
    protected PluginChangeQuality mChangeQualityPlugin;
    public Context youkuContext;
    private boolean isSendPlayBreakEvent = false; //是否发送播放中断，只发送一�?


    // 记录onCreate和onDestroy次数，防止多次启动时在onDestroy时将surfaceholder置空
    private static int mCreateTime;

    public IMediaPlayerDelegate getMediaPlayerDelegate() {
        return this.mediaPlayerDelegate;
    }

    int fullWidth, fullHeight;// , smallWidth, smallHeight;
    public static Handler handler = new Handler() {

    };
    //	public String id;// 上页传递id video/show
    public boolean autoPlay = true;// 是否自动播放

    // 是否第一次加载成�?
    private boolean firstLoaded = false;

    public static final int END_REQUEST = 201;

    public static final int END_PLAY = 202;

    // 因为切换�?g暂停
    public boolean is3GPause = false;

    int land_height, land_width;
    int port_height, port_width;
    public ImageResizer mImageWorker;                                //unused
    private static String TAG = "YoukuBaseActivity";
    private static final boolean DEVELOPER_MODE = false;
    //	PlayerApplication mApplication;									//unused
    // 提示用户登录dialog
    private Dialog mAdDialogHint = null;
    // 保存PlayVideo()信息，onActivityResult()使用
    private PlayVideoInfo mSavedPlayVideoInfo = null;
    // 等待登录返回，此时不报错
    private boolean mWaitingLoginResult = false;
    // 登录提示对话框登录请�?
    private static final int LOGIN_REQUEST_DIALOG_CODE = 10999;

    /**
     * 加密视频处理的回调，如果客户端没有调用YoukuPlayer的setIEncryptVideoCallBack设置�?
     * YoukuBasePlayerActivity默认提供一个call调用，用于处理加密视频的回调信息�?
     */
//	public boolean isApiServiceAvailable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d("PlayFlow", "YoukuBasePlayerActivity->onCreate");
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork() // 这里可以替换为detectAll()
                            // 就包括了磁盘读写和网络I/O
                    .penaltyLog() // 打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects() // 探测SQLite数据库操�?
                    .penaltyLog() // 打印logcat
                    .penaltyDeath().build());
        }

        super.onCreate(savedInstanceState);
/*		isApiServiceAvailable = PlayerUiUtile.isYoukuPlayerServiceAvailable(this);
        if(!isApiServiceAvailable){
			return;
		}

		PlayerUiUtile.initialYoukuPlayerService(this);
		mediaPlayerDelegate = RemoteInterface.mediaPlayerDelegate;*/


        if (mCreateTime == 0) {
            // 优酷进入播放器需要重新获取设置的清晰�?
            Profile.getVideoQualityFromSharedPreferences(getApplicationContext());
        }
        ++mCreateTime;
        youkuContext = this;
        mImageWorker = (ImageResizer) getImageWorker(this);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        OfflineStatistics offline = new OfflineStatistics();
        offline.sendVV(this);

        ACTIVE_TIME = PreferenceUtil.getPreference(this, "active_time");
        if (ACTIVE_TIME == null || ACTIVE_TIME.length() == 0) {
            ACTIVE_TIME = String.valueOf(System.currentTimeMillis());
            PreferenceUtil.savePreference(this, "active_time", ACTIVE_TIME);
        }

        // 初始化设备信�?
        Profile.GUID = Device.guid;
        try {
            YoukuBasePlayerActivity.versionName = getPackageManager()
                    .getPackageInfo(getPackageName(),
                            PackageManager.GET_META_DATA).versionName;
        } catch (NameNotFoundException e) {
            YoukuBasePlayerActivity.versionName = "3.1";
            Logger.e(TAG_GLOBAL, e);
        }

        if (TextUtils.isEmpty(com.baseproject.utils.Profile.User_Agent)) {
            String plant = UIUtils.isTablet(this) ? "Youku HD;" : "Youku;";
            com.baseproject.utils.Profile.initProfile("player", plant
                    + versionName + ";Android;"
                    + android.os.Build.VERSION.RELEASE + ";"
                    + android.os.Build.MODEL, getApplicationContext());
        }

//		mApplication = PlayerApplication.getPlayerApplicationInstance();
        flags = getApplicationInfo().flags;
        com.baseproject.utils.Profile.mContext = getApplicationContext();
        if (MediaPlayerProxyUtil.isUplayerSupported()) {                                //-------------------->
            YoukuBasePlayerActivity.isHighEnd = true;
            // 使用软解
            PreferenceUtil.savePreference(this, "isSoftwareDecode", true);
            com.youku.player.goplay.Profile.setVideoType_and_PlayerType(
                    com.youku.player.goplay.Profile.FORMAT_FLV_HD, this);
        } else {
            YoukuBasePlayerActivity.isHighEnd = false;
            com.youku.player.goplay.Profile.setVideoType_and_PlayerType(
                    com.youku.player.goplay.Profile.FORMAT_3GPHD, this);
        }

        IMediaPlayerDelegate.is = getResources().openRawResource(R.raw.aes);        //-------------------------------------->
        orientationHelper = new DeviceOrientationHelper(this, this);
    }

    public ImageWorker getImageWorker(YoukuBasePlayerActivity context) {
        if (null == mImageWorker) {
            final int height = Device.ht;
            final int width = Device.wt;
            final int shortest = height > width ? width : height;
            mImageWorker = new ImageFetcher(context, shortest);
            mImageWorker.setImageCache(ImageCache.findOrCreateCache(context,
                    Utils.IMAGE_CACHE_DIR));
            mImageWorker.setImageFadeIn(true);
        }
        return mImageWorker;
    }

    public void initLayoutView(YoukuPlayerView mYoukuPlayerView) {                                //在YoukuPlayerView初始化时调用
        this.mYoukuPlayerView = mYoukuPlayerView;
        onCreateInitialize();    //其内初始化了MediaPlayerDelegate
        // addPlugins();
    }

    FrameLayout player_holder;
    private PluginOverlay mPluginFullScreenPlay;                        //--------------------->全屏插件
    private PluginADPlay mPluginADPlay;                                //--------------------->广告插件

    /**
     *
     */
    protected void addPlugins() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                player_holder = (FrameLayout) mYoukuPlayerView
                        .findViewById(R.id.player_holder_all);

                mImageAD = new PluginImageAD(                                        //------------------------->
                        YoukuBasePlayerActivity.this, mediaPlayerDelegate);
                mImageAD.setVisibility(View.INVISIBLE);

                mPaytipPlugin = new PluginPayTip(YoukuBasePlayerActivity.this,        //-------------------------->
                        mediaPlayerDelegate);
                mChangeQualityPlugin = new PluginChangeQuality(                        //-------------------------->
                        YoukuBasePlayerActivity.this, mediaPlayerDelegate);

                mFullScreenPauseAD = new PluginFullScreenPauseAD(
                        YoukuBasePlayerActivity.this, mediaPlayerDelegate);            //-------------------------->
                mInvestigate = new PluginInvestigate(
                        YoukuBasePlayerActivity.this, mediaPlayerDelegate);            //-------------------------->

                // 全屏插件
                pluginManager.addPlugin(mPluginFullScreenPlay, player_holder);
                // 小屏播放控制
                if (mPluginSmallScreenPlay == null)
                    mPluginSmallScreenPlay = new PluginSimplePlayer(                //--------------------->
                            YoukuBasePlayerActivity.this, mediaPlayerDelegate);

                // 播放结束�?
                // 广告播放页面
                PluginADPlay.setAdMoreBackgroundColor(Profile.PLANTFORM == Plantform.TUDOU);
                mPluginADPlay = new PluginADPlay(YoukuBasePlayerActivity.this,
                        mediaPlayerDelegate);

                // 特殊的播放页�?
                mYoukuPlayerView.mMediaPlayerDelegate = mediaPlayerDelegate;    //----------------->
                pluginManager.addYoukuPlayerView(mYoukuPlayerView);
                pluginManager.addPlugin(mPluginSmallScreenPlay, player_holder);
                pluginManager.addPlugin(mImageAD, player_holder);
                pluginManager.addPlugin(mPaytipPlugin, player_holder);
                pluginManager.addPlugin(mChangeQualityPlugin, player_holder);
                pluginManager.addPluginAbove(mFullScreenPauseAD, player_holder);
                updatePlugin(PLUGIN_SHOW_NOT_SET);
            }
        });
    }

    private boolean isLand() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        return getOrient.getWidth() > getOrient.getHeight();
    }

    @SuppressWarnings("deprecation")
    private void initPlayAndSurface() {
        surfaceView = mYoukuPlayerView.surfaceView;
        mYoukuPlayerView.mMediaPlayerDelegate = mediaPlayerDelegate;            //初始化YoukuPlayerView中的MediaPlayerDelegate实例
//		mediaPlayerDelegate.mediaPlayer = BaseMediaPlayer.getInstance();		//MediaPlayerDelegate部分初始化
        mediaPlayerDelegate.mediaPlayer = RemoteInterface.baseMediaPlayer;        //MediaPlayerDelegate部分初始化
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(mediaPlayerDelegate.mediaPlayer);                //此处同时把surface holder句柄赋值给了mediaplayer
        if (!isHighEnd)
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public SurfaceHolder getSurfaceHolder() {
        return surfaceHolder;
    }

    // 初始化播放区控件
    public void initPlayerPart() {
        if (mediaPlayerDelegate != null
                && mediaPlayerDelegate.mediaPlayer != null
                && mediaPlayerDelegate.mediaPlayer.isListenerInit())
            return;
        initPlayAndSurface();
        initMediaPlayer();
    }

    private void initMediaPlayer() {                                            //---------->内部使用
//		mediaPlayerDelegate.mediaPlayer = BaseMediaPlayer.getInstance();
        mediaPlayerDelegate.mediaPlayer = RemoteInterface.baseMediaPlayer;
        mediaPlayerDelegate.mediaPlayer
                .setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        if (onPause) {
                            mp.release();
                            return;
                        }
                        if (pluginManager == null)
                            return;
                        pluginManager.onBufferingUpdateListener(percent);
                    }
                });
        mediaPlayerDelegate.mediaPlayer
                .setOnCompletionListener(new OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mediaPlayerDelegate != null)
                            mediaPlayerDelegate.onComplete();
                        if (mYoukuPlayerView != null)
                            mYoukuPlayerView.setPlayerBlack();
                    }
                });
        mediaPlayerDelegate.mediaPlayer
                .setOnErrorListener(new OnErrorListener() {

                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Logger.d("PlayFlow", "播放器出现错误 MediaPlayer onError what=" + what
                                + " !!!");
                        if (mYoukuPlayerView != null)
                            mYoukuPlayerView.setDebugText("出现错误-->onError:"
                                    + what);
                        disposeAdErrorLoss(what);
                        if (isAdPlayError(what)) {
                            Logger.d("PlayFlow", "出现错误:" + what
                                    + " 处理结果:跳过广告播放");
                            return loadingADOverTime();
                        }
                        if (mediaPlayerDelegate != null
                                && !mYoukuPlayerView.realVideoStart) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    detectPlugin();
                                }
                            });
                        }
                        if (!isSendPlayBreakEvent
                                && MediaPlayerConfiguration.getInstance().trackPlayError()
                                && mYoukuPlayerView.realVideoStart
                                && mediaPlayerDelegate != null
                                && mediaPlayerDelegate.videoInfo != null) {
                            final String videoUrl = mediaPlayerDelegate.videoInfo
                                    .getWeburl();
                            final TaskSendPlayBreak task = new TaskSendPlayBreak(videoUrl);
                            task.execute();
                            isSendPlayBreakEvent = true;
                        }
                        if (mYoukuPlayerView.realVideoStart && mediaPlayerDelegate.isLoading)
                            Track.onPlayLoadingEnd();
                        onLoadingFailError();
                        if (pluginManager == null) {
                            Logger.d("PlayFlow", "onError出现错误:" + what + " pluginManager == null  return false");
                            return false;
                        }
                        //Logger.d("PlayFlow", "出现错误:" + what + " 处理结果:去重�?);"
                        int nowPostition = mediaPlayerDelegate.getCurrentPosition();
                        if (nowPostition > 0) {
                            position = nowPostition;
                        }
                        // 系统播放器错误特殊处�?
                        if (what == -38
                                && !MediaPlayerProxyUtil.isUplayerSupported()) {
                            what = MPPErrorCode.MEDIA_INFO_PLAY_UNKNOW_ERROR;
                        }
                        return pluginManager.onError(what, extra);
                    }

                    private boolean isAdPlayError(int what) {
                        return what == MPPErrorCode.MEDIA_INFO_PREPARED_AD_CHECK
                                || (what == MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR && !mediaPlayerDelegate
                                .isAdvShowFinished())
                                || (what == MPPErrorCode.MEDIA_INFO_NETWORK_ERROR && mediaPlayerDelegate.isADShowing)
                                || (what == MPPErrorCode.MEDIA_INFO_NETWORK_CHECK && mediaPlayerDelegate.isADShowing);
                    }

                    //广告损耗埋点使�?
                    private void disposeAdErrorLoss(int what) {
                        if (mediaPlayerDelegate == null
                                || mediaPlayerDelegate.videoInfo == null) {
                            return;
                        }
                        if (what == MPPErrorCode.MEDIA_INFO_DATA_SOURCE_ERROR
                                && !mediaPlayerDelegate.videoInfo.isAdvEmpty()) {
                            DisposableStatsUtils.disposeAdLoss(
                                    YoukuBasePlayerActivity.this,
                                    URLContainer.AD_LOSS_STEP4,
                                    SessionUnitil.playEvent_session,
                                    URLContainer.AD_LOSS_MF);
                        }
                        if (what == MPPErrorCode.MEDIA_INFO_PREPARED_AD_CHECK) {
                            DisposableStatsUtils.disposeAdLoss(
                                    YoukuBasePlayerActivity.this,
                                    URLContainer.AD_LOSS_STEP6,
                                    SessionUnitil.playEvent_session,
                                    URLContainer.AD_LOSS_MF);
                        }
                    }
                });

        mediaPlayerDelegate.mediaPlayer
                .setOnPreparedListener(new OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (pluginManager == null)
                            return;
                        pluginManager.onPrepared();
                    }
                });

        mediaPlayerDelegate.mediaPlayer
                .setOnSeekCompleteListener(new OnSeekCompleteListener() {

                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        if (mediaPlayerDelegate != null) {
                            mediaPlayerDelegate.isLoading = false;
                        }
                        Track.setTrackPlayLoading(true);
                        if (pluginManager == null)
                            return;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                pluginManager.onSeekComplete();
                            }
                        });
                    }
                });
        mediaPlayerDelegate.mediaPlayer
                .setOnVideoSizeChangedListener(new OnVideoSizeChangedListener() {

                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width,
                                                   int height) {
                        if (pluginManager == null)
                            return;
                        pluginManager.onVideoSizeChanged(width, height);
                        Logger.e(TAG, "onVideoSizeChanged-->" + width + height);
                        mediaPlayerDelegate.mediaPlayer.updateWidthAndHeight(
                                width, height);

                    }
                });
        mediaPlayerDelegate.mediaPlayer
                .setOnTimeOutListener(new OnTimeoutListener() {

                    @Override
                    public void onTimeOut() {
                        if (mediaPlayerDelegate == null)
                            return;
                        Logger.d("PlayFlow", "onTimeOut");
                        mediaPlayerDelegate.release();
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Track.pause();
                                onLoadingFailError();
                            }
                        });
                        if (!isSendPlayBreakEvent
                                && MediaPlayerConfiguration.getInstance().trackPlayError()
                                && mYoukuPlayerView.realVideoStart
                                && mediaPlayerDelegate != null
                                && mediaPlayerDelegate.videoInfo != null) {
                            final String videoUrl = mediaPlayerDelegate.videoInfo
                                    .getWeburl();
                            final TaskSendPlayBreak task = new TaskSendPlayBreak(
                                    videoUrl);
                            task.execute();
                            isSendPlayBreakEvent = true;
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (pluginManager == null)
                                    return;
                                pluginManager.onTimeout();
                            }
                        });
                    }

                    @Override
                    public void onNotifyChangeVideoQuality() {
                        if (pluginManager == null)
                            return;
                        Logger.d("PlayFlow", "onNotifyChangeVideoQuality");
                        pluginManager.onNotifyChangeVideoQuality();
                    }
                });
        mediaPlayerDelegate.mediaPlayer
                .setOnCurrentPositionUpdateListener(new OnCurrentPositionUpdateListener() {

                    @Override
                    public void onCurrentPositionUpdate(
                            final int currentPosition) {
                        if (pluginManager == null)
                            return;
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    pluginManager
                                            .onCurrentPositionChange(currentPosition);
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                });

        if (PlayerUtil.useUplayer()) {
            mediaPlayerDelegate.mediaPlayer
                    .setOnADPlayListener(new OnADPlayListener() {

                        @Override
                        public boolean onStartPlayAD(int index) {
                            Logger.d("PlayFlow", "onstartPlayAD");
                            Track.onAdStart();
                            String vid = "";
                            if (mediaPlayerDelegate != null
                                    && mediaPlayerDelegate.videoInfo != null)
                                vid = mediaPlayerDelegate.videoInfo.getVid();
                            Track.trackAdLoad(getApplicationContext(), vid);
                            mYoukuPlayerView.setPlayerBlackGone();
                            mPluginADPlay.setInteractiveAdVisible(false);
                            if (mediaPlayerDelegate != null) {
                                mediaPlayerDelegate.isADShowing = true;
                                mediaPlayerDelegate.isAdStartSended = true;
                                if (mediaPlayerDelegate.videoInfo != null
                                        && mediaPlayerDelegate.videoInfo.videoAdvInfo != null) {
                                    if (mediaPlayerDelegate.videoInfo.videoAdvInfo.SKIP != null
                                            && mediaPlayerDelegate.videoInfo.videoAdvInfo.SKIP
                                            .equals("1")) {
                                        if (null != mPluginADPlay) {
                                            mPluginADPlay.setSkipVisible(true);
                                        }
                                    }
                                    if (mediaPlayerDelegate.videoInfo.videoAdvInfo.VAL
                                            .get(0).RST.equals("hvideo")) {
                                        if (mPluginADPlay.isInteractiveAdShow()) {
                                            mPluginADPlay
                                                    .setInteractiveAdVisible(true);
                                        } else {
                                            String brs = mediaPlayerDelegate.videoInfo.videoAdvInfo.VAL
                                                    .get(0).BRS;
                                            int count = mediaPlayerDelegate.videoInfo.videoAdvInfo.VAL
                                                    .get(0).AL;
                                            mPluginADPlay.startInteractiveAd(
                                                    brs, count);
                                            mPluginADPlay.showInteractiveAd();
                                        }
                                    }
                                }
                            }

                            updatePlugin(PLUGIN_SHOW_AD_PLAY);
                            if (null != pluginManager) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        pluginManager.onLoaded();
                                        if (null != mPluginADPlay) {
                                            mPluginADPlay.setVisible(true);
                                        }
                                    }
                                });
                            }
                            if (mediaPlayerDelegate != null
                                    && mediaPlayerDelegate.videoInfo != null) {
                                AnalyticsWrapper.adPlayStart(
                                        getApplicationContext(),
                                        mediaPlayerDelegate.videoInfo);
                            }
                            try {
                                DisposableStatsUtils
                                        .disposeSUS(mediaPlayerDelegate.videoInfo);
                            } catch (NullPointerException e) {
                                Logger.e("sgh", e.toString());
                            }

                            if (mediaPlayerDelegate.videoInfo
                                    .getCurrentAdvInfo() != null
                                    && (mediaPlayerDelegate.videoInfo
                                    .getCurrentAdvInfo().VSC == null || mediaPlayerDelegate.videoInfo
                                    .getCurrentAdvInfo().VSC
                                    .equalsIgnoreCase(""))) {
                                DisposableStatsUtils
                                        .disposeVC(mediaPlayerDelegate.videoInfo);
                            }

                            return false;
                        }

                        @Override
                        public boolean onEndPlayAD(int index) {
                            Logger.d("PlayFlow", "onEndPlayAD");

                            if (mediaPlayerDelegate != null) {
                                mediaPlayerDelegate.isADShowing = false;
                            }
                            Track.onAdEnd();

                            if (mediaPlayerDelegate != null
                                    && mediaPlayerDelegate.videoInfo != null) {
                                AnalyticsWrapper.adPlayEnd(
                                        getApplicationContext(),
                                        mediaPlayerDelegate.videoInfo);
                            }
                            // 必须在removePlayedAdv之前调用
                            DisposableStatsUtils
                                    .disposeSUE(mediaPlayerDelegate.videoInfo);
                            // 当前广告成功播放完成后，从容器中移除
                            mediaPlayerDelegate.videoInfo.removePlayedAdv();
                            if (mediaPlayerDelegate.videoInfo.isCached()) {
                                ICacheInfo download = IMediaPlayerDelegate.mICacheInfo;
                                if (download != null) {
                                    if (download
                                            .isDownloadFinished(mediaPlayerDelegate.videoInfo
                                                    .getVid())) {
                                        VideoCacheInfo downloadInfo = download
                                                .getDownloadInfo(mediaPlayerDelegate.videoInfo
                                                        .getVid());
                                        if (YoukuBasePlayerActivity.isHighEnd) {
                                            mediaPlayerDelegate.videoInfo.cachePath = PlayerUtil
                                                    .getM3u8File(downloadInfo.savePath
                                                            + "youku.m3u8");
                                        }
                                    }
                                }
                            }
                            if (null != pluginManager) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        mPluginADPlay
                                                .closeInteractiveAdNotIcludeUI();
                                        pluginManager.onLoading();
                                    }
                                });
                            }
                            Logger.e(TAG, "onEndPlayAD");
                            return false;
                        }
                    });
            mediaPlayerDelegate.mediaPlayer
                    .setOnADCountListener(new OnADCountListener() {

                        @Override
                        public void onCountUpdate(final int count) {

                            position = mediaPlayerDelegate.getCurrentPosition();
                            final int currentPosition = mediaPlayerDelegate.getCurrentPosition() / 1000;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mPluginADPlay.notifyUpdate(count);
                                    mYoukuPlayerView.resizeMediaPlayer(false);

                                    DisposableStatsUtils.disposeSU(mediaPlayerDelegate.videoInfo, currentPosition);
                                }
                            });

                        }
                    });
            mediaPlayerDelegate.mediaPlayer
                    .setOnNetworkSpeedListener(new OnNetworkSpeedListener() {

                        @Override
                        public void onSpeedUpdate(final int count) {
                            if (null != pluginManager) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        pluginManager.onNetSpeedChange(count);
                                    }
                                });
                            }
                        }
                    });
        }
        mediaPlayerDelegate.mediaPlayer
                .setOnRealVideoStartListener(new OnRealVideoStartListener() {

                    @Override
                    public void onRealVideoStart() {
                        if (onPause)
                            return;
                        // 这个listener的理解是正片开始播放的时候调�?这个时候的
                        // mediaPlayerDelegate为空的概率比较大
                        Logger.d("PlayFlow", "正片开始播放，没有错误");
                        Track.isRealVideoStarted = true;
                        String vid = "";
                        if (mediaPlayerDelegate != null
                                && mediaPlayerDelegate.videoInfo != null)
                            vid = mediaPlayerDelegate.videoInfo.getVid();
                        Track.onRealVideoFirstLoadEnd(getApplicationContext(),
                                vid);
                        localStartSetDuration();
                        sentonVVBegin();
                        mYoukuPlayerView.setPlayerBlackGone();
                        if (mediaPlayerDelegate != null
                                && mediaPlayerDelegate.videoInfo != null) {
                            mediaPlayerDelegate.isADShowing = false;
                            Logger.e(TAG, "onRealVideoStart"
                                    + mediaPlayerDelegate.videoInfo.IsSendVV);
                        } else {
                            Logger.e(TAG,
                                    "onRealVideoStart mediaPlayerDelegate空指");
                        }
                        mediaPlayerDelegate.isLoading = false;
                        if (null != pluginManager) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    detectPlugin();
                                    pluginManager.onRealVideoStart();
                                    pluginManager.onLoaded();
                                }
                            });
                        }

                        if (mediaPlayerDelegate != null) {
                            if (mediaPlayerDelegate.videoInfo != null
                                    && mediaPlayerDelegate.videoInfo
                                    .getProgress() > 1000 && !mediaPlayerDelegate.videoInfo.isHLS) {
                                mediaPlayerDelegate
                                        .seekTo(mediaPlayerDelegate.videoInfo
                                                .getProgress());
                                Logger.e(
                                        "PlayFlow",
                                        "SEEK TO"
                                                + mediaPlayerDelegate.videoInfo
                                                .getProgress());
                            }
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (mInvestigate != null) {
                                    mInvestigate.show();
                                }
                            }
                        });
                    }
                });
        mediaPlayerDelegate.mediaPlayer
                .setOnLoadingStatusListener(new OnLoadingStatusListener() {

                    @Override
                    public void onStartLoading() {
                        Logger.e(TAG, "onStartLoading");
                        if (pluginManager == null || onPause)
                            return;
                        Track.onPlayLoadingStart(mediaPlayerDelegate.mediaPlayer
                                .getCurrentPosition());
                        if (mediaPlayerDelegate != null) {
                            mediaPlayerDelegate.isLoading = true;
                        }
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (pluginManager == null)
                                    return;
                                pluginManager.onLoading();
                                if (PlayerUtil.useUplayer() && !mediaPlayerDelegate.videoInfo.isUseCachePath())
                                    mediaPlayerDelegate.loadingPause();
                            }

                        });
                    }

                    @Override
                    public void onEndLoading() {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (pluginManager == null)
                                    return;
                                pluginManager.onLoaded();
                            }
                        });
                        Track.onPlayLoadingEnd();
                        if (null != mediaPlayerDelegate) {
                            mediaPlayerDelegate.isStartPlay = true;
                            mediaPlayerDelegate.isLoading = false;
                            if (null != mediaPlayerDelegate.videoInfo) {
                                id = mediaPlayerDelegate.videoInfo.getVid();
                                mediaPlayerDelegate.videoInfo.isFirstLoaded = true;
                            }
                            // 本地mp4不控制自动开�?
                            if (PlayerUtil.useUplayer() && !mediaPlayerDelegate.videoInfo.isUseCachePath())
                                mediaPlayerDelegate.start();
                        }

                        if (!firstLoaded
                                && !isFromLocal()
                                && !PreferenceUtil.getPreferenceBoolean(YoukuBasePlayerActivity.this, "video_lock",
                                false)) {
                            if (mediaPlayerDelegate != null
                                    && !mediaPlayerDelegate.isFullScreen
                                    && mediaPlayerDelegate.videoInfo != null
                                    && StaticsUtil.PLAY_TYPE_LOCAL
                                    .equals(mediaPlayerDelegate.videoInfo
                                            .getPlayType())
                                    || !PlayerUtil
                                    .isYoukuTablet(YoukuBasePlayerActivity.this)) {
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                            }
                            Logger.d("lelouch",
                                    "onLoaded setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);");
                            firstLoaded = true;
                        }

                    }
                });
        mediaPlayerDelegate.mediaPlayer
                .setOnPlayHeartListener(new OnPlayHeartListener() {

                    @Override
                    public void onPlayHeart() {
                        if (mediaPlayerDelegate != null
                                && mediaPlayerDelegate.videoInfo != null)
                            Track.trackPlayHeart(getApplicationContext(),
                                    mediaPlayerDelegate.videoInfo,
                                    mediaPlayerDelegate.isFullScreen);
                    }
                });
        mediaPlayerDelegate.mediaPlayer
                .setOnVideoIndexUpdateListener(new OnVideoIndexUpdateListener() {

                    @Override
                    public void onVideoIndexUpdate(int currentIndex, int ip) {
                        Logger.d("PlayFlow", "onVideoIndexUpdate:"
                                + currentIndex + "  " + ip);
                        if (mediaPlayerDelegate != null
                                && mediaPlayerDelegate.videoInfo != null)
                            Track.onVideoIndexUpdate(getApplicationContext(),
                                    currentIndex, ip,
                                    mediaPlayerDelegate.videoInfo
                                            .getCurrentQuality());
                    }
                });
        mediaPlayerDelegate.mediaPlayer
                .setOnHwDecodeErrorListener(new OnHwDecodeErrorListener() {

                    @Override
                    public void OnHwDecodeError() {
                        Logger.d("PlayFlow", "OnHwDecodeError");
//						DisposableHttpTask task = new DisposableHttpTask(
//								URLContainer.getHwErrorUrl());
//						task.setRequestMethod(DisposableHttpTask.METHOD_POST);
//						task.start();
                        MediaPlayerConfiguration.getInstance()
                                .setUseHardwareDecode(false);
                    }
                });
    }

    protected void sentonVVBegin() {
        if (null != mediaPlayerDelegate) {
            mediaPlayerDelegate.isStartPlay = true;
            if (null != mediaPlayerDelegate.videoInfo
                    && !mediaPlayerDelegate.videoInfo.isFirstLoaded) {
                id = mediaPlayerDelegate.videoInfo.getVid();
                mediaPlayerDelegate.videoInfo.isFirstLoaded = true;
                if (!mediaPlayerDelegate.videoInfo.IsSendVV) {
                    mediaPlayerDelegate.onVVBegin();
                }
            }
        }
    }

    protected void localStartSetDuration() {                        //-------------内部使用
        if (mediaPlayerDelegate == null
                || mediaPlayerDelegate.videoInfo == null)
            return;
        if (StaticsUtil.PLAY_TYPE_LOCAL.equals(mediaPlayerDelegate.videoInfo
                .getPlayType())) {

            mediaPlayerDelegate.videoInfo
                    .setDurationMills(mediaPlayerDelegate.mediaPlayer
                            .getDuration());
            Logger.e("PlayFow", "本地视频读取时间成功 :"
                    + mediaPlayerDelegate.mediaPlayer.getDuration());
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (pluginManager != null) {
                        pluginManager.onVideoInfoGetted();
                    }
                }
            });
        }
    }

    // 锁屏时系统状�?
    boolean isPause;

    private DeviceOrientationHelper orientationHelper;

    @Override
    protected void onPause() {
        Logger.d("NewDetailActivity#onPause()", "onpause");
        onPause = true;
        super.onPause();
//		if(!ApiManager.getInstance().getApiServiceState())return;
        if (pluginManager != null) {
            // 通知插件Activity进入onPause
            pluginManager.onPause();
        }
        if (!mYoukuPlayerView.firstOnloaded) {
            pauseBeforeLoaded = true;
        }
        if (null != handler)
            handler.removeCallbacksAndMessages(null);
        if (mediaPlayerDelegate != null && mediaPlayerDelegate.hasRight) {
            try {
                // 19575 解决优酷挂起后一直loading�?
//				if (Profile.PLANTFORM != Plantform.YOUKU)
//					pluginManager.onLoaded();
            } catch (Exception e) {
            }
            int nowPostition = mediaPlayerDelegate.getCurrentPosition();
            if (nowPostition > 0) {
                position = nowPostition;
            }
            mediaPlayerDelegate.isPause = true;
            mediaPlayerDelegate.release();
            mediaPlayerDelegate.isLoading = false;
            if (mediaPlayerDelegate.videoInfo != null)
                mediaPlayerDelegate.videoInfo.isFirstLoaded = false;
            mediaPlayerDelegate.onChangeOrient = true;
        }
        if (surfaceHolder != null && mediaPlayerDelegate.mediaPlayer != null
                && !mediaPlayerDelegate.hasRight) {
            surfaceHolder.removeCallback(mediaPlayerDelegate.mediaPlayer);
        }
        if (mYoukuPlayerView != null) {
            mYoukuPlayerView.setPlayerBlack();
        }
        dissmissPauseAD();
        Track.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
//		if(!ApiManager.getInstance().getApiServiceState())return;
        if (isImageADShowing && mImageAD != null) {
            if (mImageAD.getAdType() == AdVender.INMOBI) {
                mImageAD.destroyInmobiAd();
            }
            if (mImageAD.getAdType() == AdVender.ADSAGE) {
                mImageAD.setMobisageAdInvisible();
            }
        }
        dissmissPauseAD();
        if (!mediaPlayerDelegate.isAdvShowFinished()
                && mPluginADPlay.isInteractiveAdShow()) {
            mPluginADPlay.setInteractiveAdVisible(false);
        }
    }

    @Override
    protected void onResume() {
        Logger.d("PlayFlow", "YoukuBasePlayerActivity->onResume()");
        onPause = false;
        setTitle("");
        super.onResume();
//		if(!ApiManager.getInstance().getApiServiceState()){
//			showApiServiceNotAvailableDialog();
//			return;
//		}else{
        if (null != mYoukuPlayerView) {
            mYoukuPlayerView.onConfigrationChange();
        }
        // 无版�?
        if (mediaPlayerDelegate != null && !mediaPlayerDelegate.hasRight)
            return;

        if (null != handler) {
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    surfaceHolder = surfaceView.getHolder();
                    // 解决android4.4上有虚拟键的设备锁屏后播放没有调用surfaceChanged导致画面比例失常
                    if (UIUtils.hasKitKat()) {
                        surfaceView.requestLayout();
                    }
                    if (null != mediaPlayerDelegate && null != surfaceHolder) {
                        if (!mediaPlayerDelegate.isAdvShowFinished()) {
                            if (mediaPlayerDelegate.mAdType == AdType.AD_TYPE_VIDEO) {
                                updatePlugin(PLUGIN_SHOW_AD_PLAY);
                                if (null != mPluginADPlay) {
                                    mPluginADPlay.showPlayIcon();
                                }
                            } else if (mediaPlayerDelegate.mAdType == AdType.AD_TYPE_IMAGE
                                    && !isImageADShowing) {
                                updatePlugin(PLUGIN_SHOW_NOT_SET);
                            }

                        } else {
                            //解决前贴广告播放完，正片还没播放时挂起，回来不能播放问题
                            if (mPluginADPlay != null
                                    && mPluginADPlay.getVisibility() == View.VISIBLE
                                    && mPluginADPlay.isCountUpdateVisible()) {
                                mPluginADPlay.showPlayIcon();
                            }
                        }
                    }
                    if (mediaPlayerDelegate != null
                            && !mediaPlayerDelegate.isComplete
                            && mediaPlayerDelegate.mAdType == AdType.AD_TYPE_IMAGE
                            && mImageAD != null && mImageAD.isOnClick) {
                        mImageAD.isOnClick = false;
                        pluginManager.onLoading();
                        if (mediaPlayerDelegate != null) {
                            mediaPlayerDelegate.startPlayAfterImageAD();
                        }
                    }
                }
            }, 100);
        }

        callPluginBack();
        mWaitingLoginResult = false;

        mediaPlayerDelegate.dialogShowing = false;
        mediaPlayerDelegate.goFor3gSetting = false;
        if (mediaPlayerDelegate.isFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            changeConfiguration(new Configuration());
        }
        if (isImageADShowing && mImageAD != null
                && !mImageAD.isSaveOnOrientChange()) {
            if (!mImageAD.isDomobLandingPageOpened()) {
                mImageAD.dismissImageAD();
                pluginManager.onLoading();

                if (mediaPlayerDelegate != null) {
                    mediaPlayerDelegate.startPlayAfterImageAD();
                }
            }
        }

        if (isImageADShowing && mImageAD != null
                && mImageAD.isSaveOnOrientChange() && !mImageAD.isOnclick()) {
            pluginManager.addPlugin(mImageAD, player_holder);
            mImageAD.setVisible(true);
            mImageAD.setVisibility(View.VISIBLE);
            mImageAD.startTimer();
        }
//		}

    }

    private void callPluginBack() {                    //------------------->内部使用
        if (null != mPluginSmallScreenPlay)
            mPluginSmallScreenPlay.back();
        if (null != mFullScreenPauseAD) {
            mFullScreenPauseAD.back();
        }
        if (null != mImageAD) {
            mImageAD.back();
        }
        if (null != mPluginFullScreenPlay) {
            mPluginFullScreenPlay.back();
        }
        //全屏广告显示时和登录提示dialog显示时，不上报error
        if (pauseBeforeLoaded
                && !isImageADShowing
                && !(mAdDialogHint != null && mAdDialogHint.isShowing() && !mWaitingLoginResult)) {
            pluginManager.onError(MPPErrorCode.MEDIA_INFO_PREPARE_ERROR, 0);
        }
        // 位置要在back()之后
        pauseBeforeLoaded = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.d("PlayFlow", "YoukuBasePlayerActivity->onDestroy");
        if (mediaPlayerDelegate != null) {
            //mediaPlayerDelegate.onVVEnd();
            if (mImageAD != null) {
                mImageAD.release();
                mImageAD = null;
            }
            if (mInvestigate != null) {
                mInvestigate.release();
                mInvestigate = null;
            }

            mPaytipPlugin = null;
            mChangeQualityPlugin = null;
            Track.forceEnd(this, mediaPlayerDelegate.videoInfo, mediaPlayerDelegate.isFullScreen);
            Track.clear();
            try {
                if (surfaceHolder != null
                        && mediaPlayerDelegate.mediaPlayer != null)
                    surfaceHolder
                            .removeCallback(mediaPlayerDelegate.mediaPlayer);

                if (null != handler)
                    handler.removeCallbacksAndMessages(null);
            } catch (Exception e) {

            }
            if (orientationHelper != null) {
                orientationHelper.disableListener();
                orientationHelper.setCallback(null);
                orientationHelper = null;
            }
            if (mFullScreenPauseAD != null) {
                mFullScreenPauseAD.release();
                mFullScreenPauseAD = null;
            }
            mediaPlayerDelegate.mediaPlayer.setOnPreparedListener(null);
            mediaPlayerDelegate.mediaPlayer.clearListener();
            mediaPlayerDelegate.mediaPlayer = null;
//			RemoteInterface.clear();
            mPluginSmallScreenPlay = null;
            pluginManager = null;
            mPluginFullScreenPlay = null;
            mediaPlayerDelegate = null;
            surfaceView = null;
            surfaceHolder = null;
            mYoukuPlayerView = null;
            youkuContext = null;
            if (--mCreateTime <= 0)
                EGLUtil.setSurfaceHolder(null);
        }
    }

    public void playNoRightVideo(String mUri) {                                    //unused

        if (mUri == null || mUri.trim().equals("")) {
            Profile.from = Profile.PHONE;
            return;
        }
        if (mUri.startsWith("youku://")) {
            mUri = mUri.replaceFirst("youku://", "http://");
        } else {
            Profile.from = Profile.PHONE;
            return;
        }
        // 不能再初始化一遍，因为已经初始化过�?
        // initPlayAndSurface();
        if (mediaPlayerDelegate.videoInfo == null)
            mediaPlayerDelegate.videoInfo = new VideoUrlInfo();
        final int queryPosition = mUri.indexOf("?");
        if (queryPosition != -1) {
            String url = new String(URLUtil.decode(mUri.substring(0,
                    queryPosition).getBytes()));
            if (PlayerUtil.useUplayer()) {
                StringBuffer m3u8Url = new StringBuffer();
                m3u8Url.append("#PLSEXTM3U\n#EXT-X-TARGETDURATION:10000\n")
                        .append("#EXT-X-VERSION:2\n#EXT-X-DISCONTINUITY\n")
                        .append("#EXTINF:10000\n").append(url)
                        .append("\n#EXT-X-ENDLIST\n");
                mediaPlayerDelegate.videoInfo.setUrl(m3u8Url.toString());
            } else {
                mediaPlayerDelegate.videoInfo.setUrl(url);
            }
            String[] params = mUri.substring(queryPosition + 1).split("&");
            for (int i = 0; i < params.length; i++) {
                String[] param = params[i].split("=");
                if (param[0].trim().equals("vid")) {
                    mediaPlayerDelegate.videoInfo.setVid(param[1].trim());
                }
                if (param[0].trim().equals("title")) {
                    try {
                        mediaPlayerDelegate.videoInfo.setTitle(URLDecoder
                                .decode(param[1].trim(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            return;
        }
        onParseNoRightVideoSuccess();
    }

    public void onParseNoRightVideoSuccess() {                                //unused
        if (PlayerUtil.useUplayer()) {
            Profile.setVideoType_and_PlayerType(Profile.FORMAT_FLV_HD, this);
        } else {
            Profile.setVideoType_and_PlayerType(Profile.FORMAT_3GPHD, this);
        }
        Profile.from = Profile.PHONE_BROWSER;
        pluginManager.onVideoInfoGetted();
        pluginManager.onChangeVideo();
        goFullScreen();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d("PlayFlow", "onConfigurationChange:" + newConfig.orientation);
        if (Profile.PHONE_BROWSER == Profile.from
                || mediaPlayerDelegate.videoInfo != null
                && (StaticsUtil.PLAY_TYPE_LOCAL
                .equals(mediaPlayerDelegate.videoInfo.getPlayType()) || mediaPlayerDelegate.videoInfo.isHLS))
            return;
        if (mediaPlayerDelegate.isFullScreen
                && PlayerUtil.isYoukuTablet(YoukuBasePlayerActivity.this)) {
            return;
        }
        // 横屏有两种情�?�?
        if (isLand()) {
            fullWidth = getWindowManager().getDefaultDisplay().getWidth();
            Logger.d("lelouch", "isLand");
            Logger.d("PlayFlow", "isTablet:" + PlayerUtil.isYoukuTablet(this));
            // 不是平板去全�?
            if (!PlayerUtil.isYoukuTablet(this)) {
                onFullscreenListener();
                closeOptionsMenu();
                setPlayerFullScreen(false);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mYoukuPlayerView.onConfigrationChange();
                        if (mInvestigate != null) {
                            mInvestigate.updateLayout();
                        }
                    }
                });

                if (mediaPlayerDelegate.mAdType == AdType.AD_TYPE_IMAGE) {
                    updatePlugin(PLUGIN_SHOW_NOT_SET);

                    if (isImageADShowing && mImageAD != null
                            && !mImageAD.isOnclick()
                            && mImageAD.isSaveOnOrientChange()) {
                        pluginManager.addPlugin(mImageAD, player_holder);
                        mImageAD.setVisible(true);
                        mImageAD.setVisibility(View.VISIBLE);
                    } else if (mImageAD != null && mImageAD.isStartToShow()
                            && !mImageAD.isSaveOnOrientChange()) {
                        if (!mImageAD.isDomobLandingPageOpened()) {
                            mImageAD.dismissImageAD();
                            if (mediaPlayerDelegate != null) {
                                mediaPlayerDelegate.startPlayAfterImageAD();
                            }
                        }
                    }
                }
                if (null != handler) {
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mAdDialogHint != null
                                    && mAdDialogHint.isShowing()) {
                                mAdDialogHint.cancel();
                                mAdDialogHint = null;
                            }
                        }
                    }, 100);
                }
                return;
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                setPadHorizontalLayout();
                updatePlugin(!mediaPlayerDelegate.isAdvShowFinished() ? PLUGIN_SHOW_AD_PLAY
                        : PLUGIN_SHOW_NOT_SET);
                Logger.d("PlayFlow", "平板去横屏小播放");
                layoutHandler.removeCallbacksAndMessages(null);
            }

        } else {
            // if(Profile.PLANTFORM == Plantform.TUDOU && onPause) return;
            goSmall();
            if (UIUtils.hasKitKat()) {
                showSystemUI(mPluginADPlay);
            }
            orientationHelper.fromUser = false;
            updatePlugin(!mediaPlayerDelegate.isAdvShowFinished() ? PLUGIN_SHOW_AD_PLAY
                    : PLUGIN_SHOW_NOT_SET);
            //fitsSystemWindows为true时，转小屏，会保留横屏的padding，所以需重置
            player_holder.setPadding(0, 0, 0, 0);
            Logger.d("PlayFlow", "去竖屏小播放");
        }
        if (mInvestigate != null) {
            mInvestigate.updateLayout();
        }
        if (isImageADShowing && mImageAD != null && !mImageAD.isOnclick()
                && mImageAD.isSaveOnOrientChange()) {

            pluginManager.addPlugin(mImageAD, player_holder);
            mImageAD.setVisible(true);
            mImageAD.setVisibility(View.VISIBLE);
        } else if (mImageAD != null && mImageAD.isStartToShow()
                && !mImageAD.isSaveOnOrientChange() && !onPause) {
            if (!mImageAD.isDomobLandingPageOpened()) {
                mImageAD.dismissImageAD();
                if (mediaPlayerDelegate != null) {
                    mediaPlayerDelegate.startPlayAfterImageAD();
                }
            }
        }
        if (mAdDialogHint != null && mAdDialogHint.isShowing()) {
            mAdDialogHint.cancel();
            mAdDialogHint = null;
        }
    }

    public abstract void setPadHorizontalLayout();

    protected void changeConfiguration(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d("PlayFlow", "changeConfiguration");
        if (Profile.PHONE_BROWSER == Profile.from
                || mediaPlayerDelegate.videoInfo != null
                && (StaticsUtil.PLAY_TYPE_LOCAL
                .equals(mediaPlayerDelegate.videoInfo.getPlayType()) || mediaPlayerDelegate.videoInfo.isHLS))
            return;
        if (mediaPlayerDelegate.isFullScreen
                && PlayerUtil.isYoukuTablet(YoukuBasePlayerActivity.this)) {
            return;
        }
        // 横屏有两种情�?�?
        if (isLand()) {
            fullWidth = getWindowManager().getDefaultDisplay().getWidth();
            Logger.d("lelouch", "isLand");
            Logger.d("PlayFlow", "isTablet:" + PlayerUtil.isYoukuTablet(this));
            // 不是平板去全�?
            if (!PlayerUtil.isYoukuTablet(this)) {
                onFullscreenListener();
                closeOptionsMenu();
                setPlayerFullScreen(false);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mYoukuPlayerView.onConfigrationChange();
                    }
                });
                // detectPlugin();
                return;
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                setPadHorizontalLayout();
                updatePlugin(!mediaPlayerDelegate.isAdvShowFinished() ? PLUGIN_SHOW_AD_PLAY
                        : PLUGIN_SHOW_NOT_SET);
                Logger.d("PlayFlow", "平板去横屏小播放");
                layoutHandler.removeCallbacksAndMessages(null);
                if (isImageADShowing && mImageAD != null
                        && !mImageAD.isOnclick()
                        && mImageAD.isSaveOnOrientChange()) {
                    pluginManager.addPlugin(mImageAD, player_holder);
                    mImageAD.setVisible(true);
                    mImageAD.setVisibility(View.VISIBLE);
                }
            }

        } else {
            // if(Profile.PLANTFORM == Plantform.TUDOU && onPause) return;
            goSmall();
            updatePlugin(!mediaPlayerDelegate.isAdvShowFinished() ? PLUGIN_SHOW_AD_PLAY
                    : PLUGIN_SHOW_NOT_SET);
            Logger.d("PlayFlow", "去竖屏小播放");
        }

    }

    // private void setVerticalPlayerSmall(boolean setRequestOreitation) {
    // if(setRequestOreitation)
    // setScreenPortrait();

    private final int GET_LAND_PARAMS = 800;                            //------>内部使用
    private final int GET_PORT_PARAMS = 801;
    private Handler layoutHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 横屏
                case GET_LAND_PARAMS: {
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    mYoukuPlayerView.resizeMediaPlayer(false);
                    changeConfiguration(new Configuration());
                    if (null != mediaPlayerDelegate)
                        mediaPlayerDelegate.currentOriention = Orientation.LAND;
                    break;
                }
                // 竖屏
                case GET_PORT_PARAMS: {
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    if (PlayerUtil.isYoukuTablet(YoukuBasePlayerActivity.this)) {
                        orientationHelper.disableListener();
                    } else {
                        // Bug 7019 在onConfigurationChanged时videoinfo还没有替换成新的导致
                        if (mediaPlayerDelegate != null
                                && mediaPlayerDelegate.videoInfo != null
                                && StaticsUtil.PLAY_TYPE_LOCAL
                                .equals(mediaPlayerDelegate.videoInfo
                                        .getPlayType())) {
                            getWindow().setFlags(
                                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            mediaPlayerDelegate.currentOriention = Orientation.LAND;
                            return;
                        }
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    if (null != mediaPlayerDelegate)
                        mediaPlayerDelegate.currentOriention = Orientation.VERTICAL;
                    break;
                }
            }
            if (mediaPlayerDelegate.isFullScreen) {
                mYoukuPlayerView.setBackgroundResource(R.color.black);
            } else {
                mYoukuPlayerView.setBackgroundResource(R.color.white);
            }
        }
    };

    public void setPlayerFullScreen(final boolean lockSensor) {
        if (mediaPlayerDelegate == null)
            return;
        if (lockSensor) {
            if (UIUtils.hasGingerbread()
                    && !PreferenceUtil.getPreferenceBoolean(this,
                    "video_lock", false)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

        mediaPlayerDelegate.onChangeOrient = true;
        mediaPlayerDelegate.isFullScreen = true;
        updatePlugin(!mediaPlayerDelegate.isAdvShowFinished() ? PLUGIN_SHOW_AD_PLAY
                : PLUGIN_SHOW_NOT_SET);

        mYoukuPlayerView.setFullscreenBack();
        // 这里把他修改成为fillpearent
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        });
    }

    public void goFullScreen() {
        if (null != mediaPlayerDelegate)
            mediaPlayerDelegate.isFullScreen = true;
        onFullscreenListener();
        if (mInvestigate != null) {
            mInvestigate.updateLayout();
        }
        if (PlayerUtil.isYoukuTablet(this)) {
            setPlayerFullScreen(true);
        } else {
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (Profile.from != Profile.PHONE_BROWSER && !PlayerUtil.isYoukuTablet(this)
                    && mediaPlayerDelegate != null && (mediaPlayerDelegate.videoInfo == null || !StaticsUtil.PLAY_TYPE_LOCAL
                    .equals(mediaPlayerDelegate.videoInfo.getPlayType()))) {
                orientationHelper.enableListener();
                orientationHelper.isFromUser();
            }
            setPlayerFullScreen(true);
        }
    }

    private void setPlayerSmall(boolean setRequsetOreitation) {            //-------》内部使用
        mediaPlayerDelegate.isFullScreen = false;
//		if (PlayerUtil.isYoukuTablet(this)
//				&& isLand()) {
//			layoutHandler.sendEmptyMessageDelayed(GET_LAND_PARAMS, 0);
//		} else {
        Message message = layoutHandler.obtainMessage();
        message.what = GET_PORT_PARAMS;
        message.obj = setRequsetOreitation;

        layoutHandler.sendMessage(message);

        mYoukuPlayerView.setVerticalLayout();
//		}
    }

    public void goSmall() {
        onSmallscreenListener();
        setPluginHolderPaddingZero();
        if (isInteractiveAdShowing()) {
            mPluginADPlay.closeInteractiveAd();
        }
        mediaPlayerDelegate.isFullScreen = false;
        if (null != handler) {
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mInvestigate != null) {
                        mInvestigate.updateLayout();
                    }
                }
            }, 100);
        }
        if (PlayerUtil.isYoukuTablet(this)) {
            setPlayerSmall(true);
            if (mImageAD != null
                    && (mImageAD.isStartToShow() || isImageADShowing)
                    && !mImageAD.isSaveOnOrientChange() && !onPause) {
                mImageAD.dismissImageAD();
                if (mediaPlayerDelegate != null) {
                    mediaPlayerDelegate.startPlayAfterImageAD();
                }
            }
        } else {
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (mediaPlayerDelegate != null && !mediaPlayerDelegate.isComplete) {
                orientationHelper.enableListener();
                orientationHelper.isFromUser();
            }
            setPlayerSmall(true);
        }
    }

    public void playCompleteGoSmall() {
        onSmallscreenListener();
        // mPluginFullScreenPlay.showSystemUI();
        if (mediaPlayerDelegate == null)
            return;
        if (PlayerUtil.isYoukuTablet(this)) {
            setPlayerSmall(true);
        } else {
            // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            setPlayerSmall(true);
            Logger.d("test2", "goSmall isLand :" + isLand());
            orientationHelper.enableListener();
            orientationHelper.isFromUser();
            orientationHelper.isFromComplete();
        }
    }

    public void notifyUp() {
        pluginManager.setUp();
    }

    public void notifyDown() {
        pluginManager.setDown();
    }

    @Override
    public boolean onSearchRequested() {                                    //android系统调用
        return mediaPlayerDelegate.isFullScreen;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    // 取消长按menu键弹出键盘事�?
                    if (event.getRepeatCount() > 0) {
                        return true;
                    }
                    return mediaPlayerDelegate.isFullScreen;
                case KeyEvent.KEYCODE_BACK:
                    // 点击过快的取消操�?
                    if (event.getRepeatCount() > 0) {
                        return true;
                    }
                    if (!mediaPlayerDelegate.isDLNA) {
                        if (mediaPlayerDelegate.isFullScreen
                                && !isFromLocal()
                                && (mediaPlayerDelegate.videoInfo != null && !mediaPlayerDelegate.videoInfo.isHLS)) {
                            goSmall();
                            return true;
                        } else {
                            onkeyback();
                            return true;
                        }
                    } else {
                        return true;
                    }
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return volumeDown();
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return volumeUp();
                case KeyEvent.KEYCODE_SEARCH:
                    return mediaPlayerDelegate.isFullScreen;
                case 125:
                    /** 有些手机载入中弹出popupwindow */
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void onkeyback() {
        Logger.d("sgh", "onkeyback");
        try {
            if (!mediaPlayerDelegate.isStartPlay
                    && !mediaPlayerDelegate.isVVBegin998Send) {
                if (mediaPlayerDelegate.videoInfo == null
                        || TextUtils.isEmpty(mediaPlayerDelegate.videoInfo
                        .getVid())) {
                    Track.onError(
                            this,
                            id,
                            Device.guid,
                            StaticsUtil.PLAY_TYPE_NET,
                            PlayCode.USER_RETURN,
                            mediaPlayerDelegate.videoInfo == null ? Source.YOUKU
                                    : mediaPlayerDelegate.videoInfo.mSource,
                            Profile.videoQuality, 0,
                            mediaPlayerDelegate.isFullScreen);
                    mediaPlayerDelegate.isVVBegin998Send = true;
                } else if (!mediaPlayerDelegate.videoInfo.IsSendVV
                        && !mediaPlayerDelegate.videoInfo.isSendVVEnd) {
                    if (mediaPlayerDelegate.isADShowing) {
                        Track.onError(this, mediaPlayerDelegate.videoInfo
                                        .getVid(), Device.guid,
                                mediaPlayerDelegate.videoInfo.playType,
                                PlayCode.VIDEO_ADV_RETURN,
                                mediaPlayerDelegate.videoInfo.mSource,
                                mediaPlayerDelegate.videoInfo
                                        .getCurrentQuality(),
                                mediaPlayerDelegate.videoInfo.getProgress(),
                                mediaPlayerDelegate.isFullScreen);
                    } else {
                        Track.onError(
                                this,
                                mediaPlayerDelegate.videoInfo.getVid(),
                                Device.guid,
                                PlayerUtil
                                        .isBaiduQvodSource(mediaPlayerDelegate.videoInfo.mSource) ? StaticsUtil.PLAY_TYPE_NET
                                        : mediaPlayerDelegate.videoInfo.playType,
                                PlayCode.USER_LOADING_RETURN,
                                mediaPlayerDelegate.videoInfo.mSource,
                                mediaPlayerDelegate.videoInfo
                                        .getCurrentQuality(),
                                mediaPlayerDelegate.videoInfo.getProgress(),
                                mediaPlayerDelegate.isFullScreen);
                        if (!mediaPlayerDelegate.videoInfo.isAdvEmpty()) {
                            DisposableStatsUtils.disposeAdLoss(
                                    YoukuBasePlayerActivity.this,
                                    URLContainer.AD_LOSS_STEP3,
                                    SessionUnitil.playEvent_session,
                                    URLContainer.AD_LOSS_MF);
                        }
                    }
                }
            }
            mediaPlayerDelegate.isStartPlay = false;
            if (!mediaPlayerDelegate.isVVBegin998Send) {
                mediaPlayerDelegate.onVVEnd();
            } else {
                mediaPlayerDelegate.videoInfo.isSendVVEnd = true;
            }
        } catch (Exception e) {
        } finally {
//			setResult(RESULT_OK);
            Logger.d("sgh", "onkeyback finally finish");
            finish();
        }
    }

    /**
     * 是否静音
     */
    private boolean isMute = false;
    private AudioManager am;
    private int currentSound;

    private void initSound() {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentSound = am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private boolean volumeUp() {
        if (!isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, Math.min(
                    am.getStreamVolume(AudioManager.STREAM_MUSIC) + 1,
                    am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
        } else {
            if (currentSound >= 0) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, Math.min(
                        currentSound + 1,
                        am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
            }

        }
        pluginManager.onVolumnUp();
        return false;
    }

    private boolean volumeDown() {
        if (!isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC,
                    am.getStreamVolume(AudioManager.STREAM_MUSIC) - 1, 0);
        } else {
            if (currentSound >= 0) {
                int destSound = currentSound - 1;
                am.setStreamVolume(AudioManager.STREAM_MUSIC,
                        destSound >= 0 ? destSound : 0, 0);
            }
        }
        pluginManager.onVolumnDown();
        return false;
    }

    private boolean isFromLocal() {

        if (Profile.from == Profile.PHONE_BROWSER
                || (mediaPlayerDelegate.videoInfo != null && StaticsUtil.PLAY_TYPE_LOCAL
                .equals(mediaPlayerDelegate.videoInfo.getPlayType())))

            return true;
        return false;

    }

    private void onLoadingFailError() {
        if (null == mediaPlayerDelegate)
            return;
        try {
            mediaPlayerDelegate.onVVEnd();
            //mediaPlayerDelegate.onVVBegin996();
        } catch (Exception e) {

        }
    }

    @Override
    public void onBackPressed() {                                        //android系统调用
        Logger.d("sgh", "onBackPressed before super");
        super.onBackPressed();
        Logger.d("sgh", "onBackPressed");
        onkeyback();
    }

    public abstract void onInitializationSuccess(YoukuPlayer player);

    /**
     * 全屏的回�?当程序全屏的时候应该将其他的view都设置为gone
     */
    public abstract void onFullscreenListener();

    /**
     * 小屏幕的回调 当程序全屏的时候应该显示其他的view
     */
    public abstract void onSmallscreenListener();

    public void onCreateInitialize() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pluginManager = new PluginManager(this);
//		mediaPlayerDelegate = new IMediaPlayerDelegate(pluginManager, this);		//初始化MediaPlayerDelegate
        mediaPlayerDelegate.initial(pluginManager, this);

        initPlayerPart();
        if (!Util.hasInternet()
                && (mediaPlayerDelegate.videoInfo == null || !mediaPlayerDelegate.videoInfo
                .isCached())) {
            // Util.showTips("!Util.hasInternet(this)");
        } else {
            if ((mediaPlayerDelegate.videoInfo == null || !mediaPlayerDelegate.videoInfo
                    .isCached()) && Util.hasInternet() && !Util.isWifi()) {
                // 提示用户没有wifi
                // Util.showTips("mediaPlayerDelegate.videoInfo == null ");
            }
            // 读取是否自动播放
            getIntentData();
            autoPlay = PreferenceManager.getDefaultSharedPreferences(
                    YoukuBasePlayerActivity.this)
                    .getBoolean("ifautoplay", true);
            if (mediaPlayerDelegate.videoInfo == null) {
            } else {
                pluginManager.onVideoInfoGetted();
            }

        }

        initSound();
    }

    String currentVid;

    public void loadEndInfo(String vid) {
        currentVid = vid;
    }

    public void resizeMediaPlayer(boolean force) {
        mYoukuPlayerView.resizeMediaPlayer(true);
    }

    public void resizeMediaPlayer(int percent) {
        mYoukuPlayerView.resizeVideoView(percent, false);
    }

    @Override
    public void land2Port() {                                        //其它接口方法
        if (!PreferenceUtil.getPreferenceBoolean(this, "video_lock", false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        Logger.d("lelouch", "land2Port");
    }

    @Override
    public void port2Land() {//其它接口方法
        if (null != orientationHelper)
            orientationHelper.disableListener();
        if (PreferenceUtil.getPreferenceBoolean(this, "video_lock", false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            layoutHandler.removeCallbacksAndMessages(null);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    @Override
    public void reverseLand() {    //OrientationChangeCallback接口方法
        if (null != orientationHelper)
            orientationHelper.disableListener();
        if (!PreferenceUtil.getPreferenceBoolean(this, "video_lock", false)) {
            layoutHandler.removeCallbacksAndMessages(null);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
    }

    @Override
    public void reversePort() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onFullScreenPlayComplete() {

        if (null != orientationHelper)
            orientationHelper.disableListener();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void setOrientionDisable() {
        if (null != orientationHelper)
            orientationHelper.disableListener();
    }

    public void setOrientionEnable() {
        if (null != orientationHelper)
            orientationHelper.enableListener();
    }

    public void resizeVideoVertical() {

        // mYoukuPlayerView.resizeVideoVertical();
    }

    // 从youkuApplicaiton 挪到YoukuBaseActivity
    public static final String TAG_GLOBAL = "YoukuBaseActivity";


    //	public static boolean isHighEnd; // 是否高端机型
    public static String versionName;
    /**
     * 激活时�?
     */
    public static String ACTIVE_TIME = "";

    public static String NO_WLAN_DOWNLOAD_FLG = "nowlandownload";

    public static String NO_WLAN_UPLOAD_FLG = "nowlanupload";

    /**
     * 3.0新增Home页数�?
     */

    // Flags associated with the application.
    public static int flags = 7;

    public static boolean isAlertNetWork = false;


    public static final int TIMEOUT = 30000;


    @Override
    public void onLowMemory() {                    //android系统调用
        super.onLowMemory();
        Logger.d("----------LowMemory----------");
        System.gc();
    }


    /**
     * 建议格式 当视频格式无法播放时可以用第二中视频格式
     */
    public static String suggest_videotype;

    public static void setSuggestVideoType(String VideoType, Context context) {
        if (PlayerUtil.isNull(VideoType))
            return;

        if (VideoType.equals("5")) {
            Profile.playerType = Profile.PLAYER_OUR;
        } else {
            Profile.playerType = Profile.PLAYER_DEFAULT;
        }
        // if(F.isNeonSupported())
        // try {
        // if(F.getUPlayer(context)!=null)
        // Profile.playerType = Profile.PLAYER_OUR;
        // } catch (NameNotFoundException e) {
        // e.printStackTrace();
        // } catch (InstantiationException e) {
        // e.printStackTrace();
        // } catch (IllegalAccessException e) {
        // e.printStackTrace();
        // } catch (ClassNotFoundException e) {
        // e.printStackTrace();
        // }
        Logger.d("playertype:" + Profile.playerType);
        // 1- mp4（网站mp4�?
        // 2- 3gp
        // 3- flv
        // 4- 3gphd（手机mp4�?
        // 5- flvhd
        // 6- m3u8
        suggest_videotype = VideoType;

    }

    public static String getSuggestVideoType() {
        return suggest_videotype;
    }

    public static int getFormat(int videoType) {
        // if (F.isNull(videoType))
        // return "";
        if (videoType == 4) {
            PlayerUtil.out(1, "3gphd");
        } else if (videoType == 1) {
            PlayerUtil.out(1, "mp4");
        } else if (videoType == 2) {
            PlayerUtil.out(1, "3gp");
        } else if (videoType == 5) {
            PlayerUtil.out(1, "flv");
        } else if (videoType == 6) {
            PlayerUtil.out(1, "m3u8");
        }
        return videoType;
    }

    /**
     * added for Uplayer 2.4.1 获取url的接�?
     * http://test.api.3g.youku.com/layout/phone2_1
     * /play.text?point=1&id=XNDQyMDcxODQ4
     * &pid=a1c0f66d02e2a816&format=6,5,1,7&language
     * =guoyu&audiolang=1&guid=a83cf513b78750adcab88523857b652d
     * &ver=2.4.0.1&network=WIFI 当传入的是mp4，flv，hd2的时候需要返回所有的格式（超标高），用于获取所有的URL
     * m3u8的超标高接口数据有所不同，参见接�?
     *
     * @param videoType 当前需要播放的格式
     * @return 需要传给接口的format
     */
    public static String getFormatAll(String videoType) {
        if (PlayerUtil.isNull(videoType))
            return "";
        if (videoType.equals("4")) {
            PlayerUtil.out(1, "3gphd");
        } else if (videoType.equals("2")) {
            PlayerUtil.out(1, "3gp");
        } else if (videoType.equals("6")) {
            PlayerUtil.out(1, "m3u8");
        } else if (videoType.equals("5")) {
            PlayerUtil.out(1, "flv");
            return "1,5,7"; // 超标�?
        } else if (videoType.equals("7")) {
            PlayerUtil.out(1, "hd2");
            return "1,5,7"; // 超标�?
        } else if (videoType.equals("1")) {
            PlayerUtil.out(1, "mp4");
            return "1,5,7"; // 超标�?
        }
        return videoType;
    }

    public static boolean isnofreedata(String name) {
        if (name.startsWith("GT-I9228") || name.startsWith("Note")
                || name.startsWith("9220") || name.startsWith("I889")
                || name.startsWith("I717") || name.startsWith("I9228")) {
            Logger.e("Youku", "isnofreedata ERROR");
            return true;
        }
        return false;
    }

    /**
     * 播放广告
     *
     * @param isADshowing 是否正在播放广告
     */
    public void updatePlugin(final int pluginID) {
        Logger.e(TAG, "数组访问 updatePlugin");
        if (pluginManager == null) {
            return;
        }
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switch (pluginID) {
                    case PLUGIN_SHOW_AD_PLAY: {
                        pluginManager.addPlugin(mPluginADPlay, player_holder);
                        break;
                    }
                    case PLUGIN_SHOW_IMAGE_AD: {
                        pluginManager.addPlugin(mImageAD, player_holder);
                        break;
                    }
                    case PLUGIN_SHOW_INVESTIGATE: {
                        pluginManager.addInvestigatePlugin(mInvestigate,
                                player_holder);
                        break;
                    }
                    default:
                        detectPlugin();
                        break;
                }
            }

        });
    }

    protected void detectPlugin() {
        if (pluginManager == null) {
            return;
        }
        if (mediaPlayerDelegate != null && mediaPlayerDelegate.isFullScreen) {
            if (mPluginFullScreenPlay == null) {
                // 没有全屏用小屏幕插件
                pluginManager.addPlugin(mPluginSmallScreenPlay, player_holder);
                mPluginSmallScreenPlay.pluginEnable = true;
                return;
            }
            pluginManager.addPlugin(mPluginFullScreenPlay, player_holder);
            if (mediaPlayerDelegate.videoInfo == null) {
                return;
            }
        } else {
            pluginManager.addPlugin(mPluginSmallScreenPlay, player_holder);
            mPluginSmallScreenPlay.pluginEnable = true;
        }
    }

    boolean isPauseADShowing = false;
//	public boolean onPause;
//	public boolean isImageADShowing = false;

    /**
     * 在正片开始前暂停
     */
//	public boolean pauseBeforeLoaded = false;
    public void showPauseAD() {
        if (!MediaPlayerConfiguration.getInstance().showPauseAd())
            return;
        isPauseADShowing = true;
        if (mFullScreenPauseAD == null) {
            mFullScreenPauseAD = new PluginFullScreenPauseAD(this,
                    mediaPlayerDelegate);
        }
        mFullScreenPauseAD.setVisibility(View.VISIBLE);
        mFullScreenPauseAD.showPauseAD();
    }

    public void dissmissPauseAD() {
        if (mFullScreenPauseAD != null) {
            mFullScreenPauseAD.setVisible(false);
            isPauseADShowing = false;
        }
    }

    public void showImageAD(VideoAdvInfo videoAdvInfo) {
        if (isImageADShowing) {
            return;
        }
        //isImageADShowing = true;
        Logger.d("PlayFlow",
                "show Image AD");
        if (mImageAD == null) {
            mImageAD = new PluginImageAD(this,
                    mediaPlayerDelegate);
        }

        //mImageAD.setAdType(adType);
        mImageAD.showAD(videoAdvInfo);
    }

    public void dissmissImageAD() {
        if (mImageAD != null) {
            //mImageAD.setVisible(false);
            mImageAD.dismissImageAD();
        }
    }

    public void dismissInteractiveAD() {
        if (mPluginADPlay != null) {
            mPluginADPlay.closeInteractiveAd();
        }
    }

    public boolean isImageAdStartToShow() {
        if (mImageAD != null) {
            return mImageAD.isStartToShow();
        }
        return false;
    }

    public void notifyFav() {
        setFav();
        if (pluginManager != null)
            pluginManager.setFav();
    }

    private void setFav() {
        if (mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null) {
            mediaPlayerDelegate.videoInfo.isFaved = true;
        }
    }

    public void clearUpDownFav() {
        if (pluginManager != null)
            pluginManager.clearUpDownFav();
    }

    /**
     * 当视频发生变化的时候调�?留给底层复写
     */
    public void onVideoChange() {

    }

    public void setmPluginFullScreenPlay(PluginOverlay mPluginFullScreenPlay) {
        this.mPluginFullScreenPlay = mPluginFullScreenPlay;
    }

    public void setmPluginSmallScreenPlay(PluginOverlay mPluginSmallScreenPlay) {
        this.mPluginSmallScreenPlay = mPluginSmallScreenPlay;
    }

    public static VideoUrlInfo getRecordFromLocal(VideoUrlInfo mVideoUrlInfo) {                    //待定，在SDK内部使用
        if (mVideoUrlInfo.getVid() != null
                && IMediaPlayerDelegate.mIVideoHistoryInfo != null) {
            VideoHistoryInfo mVideoInfo = IMediaPlayerDelegate.mIVideoHistoryInfo
                    .getVideoHistoryInfo(mVideoUrlInfo.getVid());
            if (mVideoInfo != null) {
                int playHistory = mVideoInfo.playTime * 1000;
                if (playHistory > mVideoUrlInfo.getProgress())
                    mVideoUrlInfo.setProgress(playHistory);
            }
        }
        return mVideoUrlInfo;
    }


    /**
     * @param ispause 是否从挂起中恢复
     */
    public void startPlay() {
        if (mYoukuPlayerView != null) {
            mYoukuPlayerView.setDebugText("YoukuBasePlayerActivity startPlay ");
        }
        if (mediaPlayerDelegate == null)
            return;
        if (mediaPlayerDelegate.isPause && mediaPlayerDelegate.isAdvShowFinished()) {
            mediaPlayerDelegate.isPause = false;
        } else {
            mediaPlayerDelegate.start();
            mediaPlayerDelegate.seekToPausedADShowing(position);
        }
    }

    private boolean isLocalPlay() {
        return Profile.USE_SYSTEM_PLAYER
                || (mediaPlayerDelegate != null
                && mediaPlayerDelegate.videoInfo != null && StaticsUtil.PLAY_TYPE_LOCAL
                .equals(mediaPlayerDelegate.videoInfo.playType));
    }

    private boolean loadingADOverTime() {
        mediaPlayerDelegate.playVideoWhenADOverTime();
        updatePlugin(PLUGIN_SHOW_NOT_SET);
        if (pluginManager != null) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mPluginADPlay.closeInteractiveAd();
                    pluginManager.onVideoInfoGetting();
                    pluginManager.onVideoInfoGetted();
                    pluginManager.onLoading();
                }
            });
        }
        return true;
    }

    public void alertRetry() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // if (mPluginSmallScreenPlay != null)
                // mPluginSmallScreenPlay.showAlert();
            }
        });
    }


    private void getIntentData() {
        Intent intent = getIntent();
        if (null != intent && null != intent.getExtras()) {
            String tidString = intent.getExtras().getString("video_id");
            id = tidString;
        }
    }

    public void setDebugInfo(String string) {
        if (null != mYoukuPlayerView) {
            mYoukuPlayerView.setDebugText(string);
        }
    }

    public void interuptAD() {
        if (mediaPlayerDelegate != null) {
            mediaPlayerDelegate.isADShowing = false;
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    detectPlugin();
                }
            });
        }
    }

    public void setPlayerBlack() {
        if (mYoukuPlayerView != null) {
            mYoukuPlayerView.setPlayerBlack();
        }
    }

    public void playReleateNoRightVideo() {
        pluginManager.onPlayReleateNoRightVideo();
    }

    public int getVideoPosition() {
        return position;
    }

    public void showDialog() {
        DialogFragment newFragment = PasswordInputDialog
                .newInstance(R.string.player_error_dialog_password_required);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    class DialogPositiveClick implements OnPositiveClickListener {

        @Override
        public void onClick(String passWord) {
            doPositiveClick(passWord);
        }
    }

    private void doPositiveClick(String password) {
        MediaPlayerConfiguration.getInstance().mPlantformController
                .playVideoWithPassword(mediaPlayerDelegate, password);
    }

    private void doNegativeClick() {

    }

    public static class PasswordInputDialog extends DialogFragment {

        public static PasswordInputDialog newInstance(int title) {
            PasswordInputDialog frag = new PasswordInputDialog();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");
            LayoutInflater factory = LayoutInflater.from(getActivity());
            final View textEntryView = factory.inflate(
                    R.layout.yp_youku_dialog_password_interact, null);
            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setView(textEntryView)
                    .setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    EditText passwordEditText = (EditText) textEntryView
                                            .findViewById(R.id.password_edit);
                                    String password = passwordEditText
                                            .getText().toString();

                                    ((YoukuBasePlayerActivity) getActivity())
                                            .doPositiveClick(password);
                                }
                            })
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    ((YoukuBasePlayerActivity) getActivity())
                                            .doNegativeClick();
                                }
                            }).create();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (pluginManager != null)
            pluginManager.onStart();
    }


    public void onPayClick() {
        if (mediaPlayerDelegate != null
                && IMediaPlayerDelegate.mIPayCallBack != null
                && mediaPlayerDelegate.videoInfo != null) {
            mediaPlayerDelegate.release();
            mediaPlayerDelegate.onVVEnd();
            IMediaPlayerDelegate.mIPayCallBack.needPay(
                    mediaPlayerDelegate.videoInfo.getVid(),
                    mediaPlayerDelegate.videoInfo.mPayInfo);
        }
    }

    /**
     * 初始调查问卷状态，onRealVideoStart就会显示
     */
    public void startInvestigate(VideoAdvInfo videoAdvInfo) {
        if (mInvestigate != null) {
            mInvestigate.start(videoAdvInfo);
        }
    }

    /**
     * 隐藏调查问卷,清晰度切换和付费试看提示�?
     */
    public void hideTipsPlugin() {
        if (mInvestigate != null) {
            mInvestigate.hide();
        }
        if (mPaytipPlugin != null) {
            mPaytipPlugin.hide();
        }
        if (mChangeQualityPlugin != null) {
            mChangeQualityPlugin.hide();
        }
    }

    /**
     * 取消隐藏
     */
    public void unHideTipsPlugin() {
        if (mInvestigate != null) {
            mInvestigate.unHide();
        }
        if (mPaytipPlugin != null) {
            mPaytipPlugin.unHide();
        }
        if (mChangeQualityPlugin != null) {
            mChangeQualityPlugin.unHide();
        }
    }

    /**
     * 释放调查问卷，不再显�?
     */
    public void releaseInvestigate() {
        if (mInvestigate != null) {
            mInvestigate.release();
        }
    }

    public boolean canShowPluginChangeQuality() {
        return !mInvestigate.isShowing() && !mPaytipPlugin.isShowing();
    }

    public void hideSystemUI(PluginOverlay plugin) {
        if (mediaPlayerDelegate.isFullScreen) {
            plugin.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    public void showSystemUI(PluginOverlay plugin) {
        plugin.setSystemUiVisibility(0);
    }

    public void setPluginHolderPaddingZero() {
        if (player_holder != null) {
            player_holder.setPadding(0, 0, 0, 0);
        }
    }

    public void recreateSurfaceHolder() {
        surfaceView.recreateSurfaceHolder();
    }

    public boolean isInteractiveAdShowing() {
        if (mediaPlayerDelegate != null
                && !mediaPlayerDelegate.isAdvShowFinished()
                && mPluginADPlay != null
                && mPluginADPlay.isInteractiveAdShow()
                && !mPluginADPlay.isInteractiveAdHide()) {
            return true;
        }
        return false;
    }


}