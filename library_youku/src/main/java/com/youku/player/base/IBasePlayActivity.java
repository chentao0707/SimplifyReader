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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Profile;
import com.youku.player.YoukuPlayerApplication;
import com.youku.player.ad.AdVender;
import com.youku.player.goplay.VideoAdvInfo;
import com.youku.player.module.VideoHistoryInfo;
import com.youku.player.module.VideoUrlInfo;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.util.PlayerUiUtile;
import com.youku.player.util.RemoteInterface;
import com.youku.player.util.URLContainer;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    15/7/13
 * Description:
 */
public abstract class IBasePlayActivity extends AppCompatActivity {

    private static SharedPreferences s;
    private static SharedPreferences.Editor e;
    public IMediaPlayerDelegate mediaPlayerDelegate;
    public boolean isImageADShowing = false;
    public boolean onPause;
    public boolean pauseBeforeLoaded = false;
    public String id;
    public static Handler handler = new Handler() {
    };
    public static boolean isHighEnd;

    public IBasePlayActivity() {
    }

    @SuppressLint({"CommitPrefEdits"})
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        s = PreferenceManager.getDefaultSharedPreferences(this);
        e = s.edit();
        if (Profile.DEBUG) {
            Logger.d("IBaseActivity", "youku player debug");
            URLContainer.YOUKU_WIRELESS_DOMAIN = "http://test1.api.3g.youku.com/openapi-wireless/";
            URLContainer.YOUKU_WIRELESS_LAYOUT_DOMAIN = "http://test.api.3g.youku.com/layout/";
            URLContainer.YOUKU_DOMAIN = "http://test1.api.3g.youku.com";
            URLContainer.YOUKU_AD_DOMAIN = "http://test1.api.3g.youku.com";
            AdVender.MobiSage_ID = "4c83b6591cac465d86d93bbe419797ab";
            URLContainer.STATIC_DOMAIN = "http://test1.api.3g.youku.com";
        } else {
            Logger.d("IBaseActivity", "youku player official");
            URLContainer.YOUKU_DOMAIN = "http://a.play.api.3g.youku.com";
            URLContainer.YOUKU_AD_DOMAIN = "http://ad.api.3g.youku.com";
            URLContainer.STATIC_DOMAIN = "http://statis.api.3g.youku.com";
        }

        Logger.d("sgh", "initial mediaPlayerDelegate in IBaseActivity");
        PlayerUiUtile.refreshMediaplayerDelegate(YoukuPlayerApplication.context);
        this.mediaPlayerDelegate = RemoteInterface.mediaPlayerDelegate;
    }

    public abstract IMediaPlayerDelegate getMediaPlayerDelegate();

    public abstract void showDialog();

    public abstract void showPauseAD();

    public abstract void onPayClick();

    public abstract void recreateSurfaceHolder();

    public abstract void dissmissImageAD();

    public abstract void dissmissPauseAD();

    public abstract void releaseInvestigate();

    public abstract void dismissInteractiveAD();

    public abstract void goFullScreen();

    public abstract void showImageAD(VideoAdvInfo var1);

    public abstract void startInvestigate(VideoAdvInfo var1);

    public abstract void goSmall();

    public abstract void playCompleteGoSmall();

    public abstract void setOrientionDisable();

    public abstract void setOrientionEnable();

    public abstract void resizeVideoVertical();

    public abstract int getVideoPosition();

    public abstract boolean isImageAdStartToShow();

    public abstract void initPlayerPart();

    public abstract SurfaceHolder getSurfaceHolder();

    public abstract void resizeMediaPlayer(boolean var1);

    public static int getCurrentFormat() {
        return isHighEnd ? 5 : 4;
    }

    public static void addToPlayHistory(VideoUrlInfo videoInfo) {
        if (IMediaPlayerDelegate.mIVideoHistoryInfo != null) {
            Logger.d("HistoryFlow", "IBaseActivity: addToPlayHistory()");
            IMediaPlayerDelegate.mIVideoHistoryInfo.addToPlayHistory(videoInfo);
        }
    }

    public static VideoUrlInfo getRecordFromLocal(VideoUrlInfo mVideoUrlInfo) {
        if (mVideoUrlInfo.getVid() != null && IMediaPlayerDelegate.mIVideoHistoryInfo != null) {
            VideoHistoryInfo mVideoInfo = IMediaPlayerDelegate.mIVideoHistoryInfo.getVideoHistoryInfo(mVideoUrlInfo.getVid());
            if (mVideoInfo != null) {
                int playHistory = mVideoInfo.playTime * 1000;
                if (playHistory > mVideoUrlInfo.getProgress()) {
                    mVideoUrlInfo.setProgress(playHistory);
                }
            }
        }

        return mVideoUrlInfo;
    }

    public static void savePreference(String key, String value) {
        e.putString(key, value).commit();
    }

    public static void savePreference(String key, int value) {
        e.putInt(key, value).commit();
    }

    public static void savePreference(String key, Boolean value) {
        e.putBoolean(key, value.booleanValue()).commit();
    }

    public static boolean getPreferenceBoolean(String key) {
        return s.getBoolean(key, false);
    }

    public static String getPreference(String key) {
        return s.getString(key, "");
    }

    public static int getPreferenceInt(String key) {
        return s.getInt(key, 0);
    }

    public static boolean getPreferenceBoolean(String key, boolean def) {
        return s.getBoolean(key, def);
    }

    public static String getPreference(String key, String def) {
        return s.getString(key, def);
    }

    public static int getPreferenceInt(String key, int def) {
        return s.getInt(key, def);
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        Logger.d("IBaseActivity", "onDestroy");
    }

    protected void onResume() {
        super.onResume();
    }

    public void showApiServiceNotAvailableDialog() {
        Toast.makeText(this, "优酷播放器API不可用，请安装播放器apk！", 2000).show();
    }
}
