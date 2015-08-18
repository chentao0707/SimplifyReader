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

package com.youku.player;

import android.app.Activity;
import android.os.Handler;

import com.decapi.DecAPI;
import com.youku.player.ui.R;
import com.youku.service.download.DownloadManager;
import com.youku.ui.activity.CacheActivity;

public abstract class YoukuPlayerBaseApplication extends YoukuPlayerApplication {
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		DownloadManager.getInstance();		

		DecAPI.init(context,R.raw.aes);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				DownloadManager.getInstance().startNewTask();
			}
		}, 1000);
	}


	@Override
	public int getNotifyLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.notify;
	}
	
	public static void exit(){
		YoukuPlayerApplication.exit();
		DownloadManager.getInstance().unregister();
	}


	@Override
	public Class<? extends Activity> getCachedActivityClass() {
		// TODO Auto-generated method stub
		return CacheActivity.class;
	}


	@Override
	public Class<? extends Activity> getCachingActivityClass() {
		// TODO Auto-generated method stub
		return CacheActivity.class;
	}

}
