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

package com.youku.service.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.youku.service.acc.AcceleraterServiceManager;

public class DownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Logger.d("Accelerater_", "DownloadReceiver : from = " + intent.getStringExtra("from"));
		
		String from = intent.getStringExtra("from");
		if (from != null && from.equals(AcceleraterServiceManager.FROM_ACC)) {
			String restrictBy = intent.getStringExtra(AcceleraterServiceManager.RESTRICTBY);
			if (!TextUtils.isEmpty(restrictBy)) {
				Logger.d("Accelerater_", "DownloadReceiver : restrictBy = " + restrictBy);
//				IStaticsManager.p2pFail(restrictBy);
			}
			
			String succstartp2p = intent.getStringExtra(AcceleraterServiceManager.SUCCSTARTP2P);
			if (!TextUtils.isEmpty(succstartp2p)) {
				Logger.d("Accelerater_", "DownloadReceiver : succstartp2p = " + succstartp2p);
//				IStaticsManager.p2pSuccess(succstartp2p);
			}
			
			return;
		}
		
		String vid = intent.getStringExtra("vid");
		int state = intent.getIntExtra("state", 0);
		int source = intent.getIntExtra("source", 0);
//		IStaticsManager.p2pCacheVideoState(vid, state, source);
	}

}
