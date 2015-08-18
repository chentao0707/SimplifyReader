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

package com.youku.service.acc;

import java.io.File;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;

public class AcceleraterService extends Service {

	public static final String ACTION_START_SERVER = "com.youku.acc.ACTION_START_SERVER";
	public static final String ACTION_STOP_SERVER = "com.youku.acc.ACTION_STOP_SERVER";
	
	public static final String ACTION_START_SUCCESS = "com.youku.acc.ACTION_START_SUCCESS";
	public static final String ACTION_START_FAILURE = "com.youku.acc.ACTION_START_FAILURE";

	private static final String TAG = "Accelerater_Service";
	
	AcceleraterServiceManager mAccServiceManager;
	
	/** 初始化注册网络广播的锁，防止错误的网络变化 */
	private boolean mInitLock = false;
	
	private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (mInitLock) {
				mInitLock = false;
				return;
			}
			
			if (Util.hasInternet()) {
				if (Util.isWifi()) {// wifi可用加速器
					Logger.d(TAG, "network --------> wifi");
					mAccServiceManager.resumeAcc();
				} else {// 2g/3g不可用加速器
					Logger.d(TAG, "network --------> 2/3g");
					mAccServiceManager.pauseAcc();
				}
			} else {
				Logger.d(TAG, "network --------> no network");
				mAccServiceManager.pauseAcc();
			}
		}
		
	};
	
	private IAcceleraterService.Stub mBinder = new IAcceleraterService.Stub() {
		
		@Override
		public void stop() throws RemoteException {
			mAccServiceManager.stopAcc();
		}
		
		@Override
		public void start() throws RemoteException {
			int flag = mAccServiceManager.startAcc(AcceleraterService.this);
			if (flag == 0) {
				Intent intent = new Intent(ACTION_START_SUCCESS);
				AcceleraterService.this.sendBroadcast(intent);
			} else {
				Intent intent = new Intent(ACTION_START_FAILURE);
				AcceleraterService.this.sendBroadcast(intent);
			}
		}
		
		@Override
		public int resume() throws RemoteException {
			return mAccServiceManager.resumeAcc();
		}
		
		@Override
		public int pause() throws RemoteException {
			return mAccServiceManager.pauseAcc();
		}
		
		@Override
		public int isAvailable() throws RemoteException {
			return mAccServiceManager.isAccAvailable();
		}
		
		@Override
		public int getHttpProxyPort() throws RemoteException {
			return mAccServiceManager.getAccHttpProxyPort();
		}
		
		@Override
		public boolean isACCEnable() throws RemoteException {
			return AcceleraterServiceManager.isACCEnable();
		}

		@Override
		public String getAccPort() throws RemoteException {
			return AcceleraterServiceManager.ACC_PORT;
		}

		@Override
		public String getVersionName() throws RemoteException {
			return AcceleraterServiceManager.getAccVersionName();
		}

		@Override
		public int getVersionCode() throws RemoteException {
			return AcceleraterServiceManager.getAccVersionCode();
		}

		@Override
		public int getCurrentStatus() throws RemoteException {
			return mAccServiceManager.getCurrentStatus();
		}

	};

	@Override
	public void onCreate() {
		Logger.d(TAG, "onCreate()");
		
		mAccServiceManager = AcceleraterServiceManager.getInstance();
		mInitLock = true;
		IntentFilter i = new IntentFilter();
		i.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mNetworkReceiver, i);
		
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.d(TAG, "onStartCommand() intent = " + intent);
		AccInitData.printAll(this);
		
		if (intent == null) {
			if (Util.hasInternet() && !Util.isWifi()) {
				mAccServiceManager.pauseAcc();
				return START_NOT_STICKY;
			}
			
			if (!AcceleraterServiceManager.isACCEnable()) {
				Logger.d(TAG, "ACC启动失败/手机不满足ACC运行条件");
				if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.STARTED) {
					mAccServiceManager.pauseAcc();
				}
			}else {
				Logger.d(TAG, "status = " + mAccServiceManager.getCurrentStatus());
				
				
				if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.INIT) {
					mAccServiceManager.startAcc(this);
				} else if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.PAUSED) {
					mAccServiceManager.resumeAcc();
				} 			
			}
			return START_NOT_STICKY;
		}
		
		
		String action = intent.getAction();
		if (action == null) {
			return START_NOT_STICKY;
		}
		
		if (action.equals(ACTION_START_SERVER)) {// 启动ACC
			Intent i = new Intent("android.intent.action.DOWNLOAD_TRACKER");
			i.putExtra("from", AcceleraterServiceManager.FROM_ACC);
			
			if (Util.hasInternet() && !Util.isWifi()) {
				mAccServiceManager.pauseAcc();
				i.putExtra(AcceleraterServiceManager.RESTRICTBY, "9-网络环境不满足要求");
				sendBroadcast(i);
				Logger.d(TAG, "统计失败原因");
				return START_STICKY;
			}
			
//			String cachePath =AcceleraterServiceManager.getDefauleSDCardPath();
//			if (cachePath != null
//					&& TextUtils.getTrimmedLength(cachePath) > 0) {
//				File f = new File(cachePath + "/youku/youkudisk/");
//				if (!f.exists()) {
//					if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.STARTED) {
//						mAccServiceManager.pauseAcc();
//					}
//					i.putExtra(AcceleraterServiceManager.RESTRICTBY, "10-无youkudisk文件夹");
//					sendBroadcast(i);
//					Logger.d(TAG, "统计失败原因");
//					return START_STICKY;
//				} 
//			} else {
//				if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.STARTED) {
//					mAccServiceManager.pauseAcc();
//				}
//				i.putExtra(AcceleraterServiceManager.RESTRICTBY, "7-获取缓存路径失败:" + cachePath);
//				sendBroadcast(i);
//				Logger.d(TAG, "统计失败原因");
//				return START_STICKY;
//			}
			
			if (!AcceleraterServiceManager.isACCEnable()) {
				Logger.d(TAG, "ACC启动失败/手机不满足ACC运行条件");
				if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.STARTED) {
					mAccServiceManager.pauseAcc();
				}
				i.putExtra(AcceleraterServiceManager.RESTRICTBY, AcceleraterServiceManager.sFailReason);
				sendBroadcast(i);
				Logger.d(TAG, "统计失败原因");
			} else {
				Logger.d(TAG, "status = " + mAccServiceManager.getCurrentStatus());
				if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.INIT) {
					mAccServiceManager.startAcc(this);
					return START_STICKY;
				} 
				
				if (checkCacheDir(i) == -1) {
					return START_STICKY;
				}
				
				if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.PAUSED) {
					if (-1 == mAccServiceManager.resumeAcc()) {
						i.putExtra(AcceleraterServiceManager.RESTRICTBY, "8-resumeAcc失败");
						sendBroadcast(i);
						Logger.d(TAG, "统计失败原因");
					}
				}
			}
		} else if (action.equals(ACTION_STOP_SERVER)) {
			mAccServiceManager.pauseAcc();
		}
		
		return START_STICKY;
	}
	
	private int checkCacheDir(Intent intent) {
		String cachePath =AcceleraterServiceManager.getDefauleSDCardPath();
		if (cachePath != null
				&& TextUtils.getTrimmedLength(cachePath) > 0) {
			File f = new File(cachePath + "/youku/youkudisk/");
			if (!f.exists()) {
				if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.STARTED) {
					mAccServiceManager.pauseAcc();
				}
				intent.putExtra(AcceleraterServiceManager.RESTRICTBY, "10-无youkudisk文件夹");
				sendBroadcast(intent);
				Logger.d(TAG, "统计失败原因");
				return -1;
			} 
		} else {
			if (mAccServiceManager.getCurrentStatus() == AcceleraterStatus.STARTED) {
				mAccServiceManager.pauseAcc();
			}
			intent.putExtra(AcceleraterServiceManager.RESTRICTBY, "7-获取缓存路径失败:" + cachePath);
			sendBroadcast(intent);
			Logger.d(TAG, "统计失败原因");
			return -1;
		}
		
		return 0;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Logger.d(TAG, "AcceleraterService onBind()");
		return mBinder;
	}

	@Override
	public void onDestroy() {
		Logger.d(TAG, "AcceleraterService onDestroy()");
		unregisterReceiver(mNetworkReceiver);
		super.onDestroy();
	}
	
}
