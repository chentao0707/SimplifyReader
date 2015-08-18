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

package com.github.obsessive.simplifyreader;

import com.github.obsessive.library.base.BaseAppManager;
import com.github.obsessive.simplifyreader.api.ApiConstants;
import com.github.obsessive.simplifyreader.utils.ImageLoaderHelper;
import com.github.obsessive.simplifyreader.utils.VolleyHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.youku.player.YoukuPlayerBaseApplication;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    2015/3/9.
 * Description: App Application Context
 */
public class SimplifyReaderApplication extends YoukuPlayerBaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        MobclickAgent.setDebugMode(true);
        MobclickAgent.updateOnlineConfig(this);
        MobclickAgent.openActivityDurationTrack(false);
        UmengUpdateAgent.update(this);

        VolleyHelper.getInstance().init(this);
        ImageLoader.getInstance().init(ImageLoaderHelper.getInstance(this).getImageLoaderConfiguration(ApiConstants.Paths.IMAGE_LOADER_CACHE_PATH));
    }

    @Override
    public String configDownloadPath() {
        return null;
    }

    @Override
    public void onLowMemory() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onLowMemory();
    }

    public void exitApp() {
        BaseAppManager.getInstance().clear();
        System.gc();
        MobclickAgent.onKillProcess(this);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
