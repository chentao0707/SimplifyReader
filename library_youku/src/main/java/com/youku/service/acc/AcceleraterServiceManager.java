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

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.baseproject.image.Utils;
import com.baseproject.utils.Logger;
import com.comscore.utils.FileUtils;


public class AcceleraterServiceManager {

	private static final String TAG = "Accelerater_Service_Manager";
	
	public static String ACC_PORT = "";
	
	public static final String RESTRICTBY = "restrictby"; 
	
	public static final String SUCCSTARTP2P = "succStartP2p";
	
	private int mCurrentStatus = AcceleraterStatus.INIT;
	
	public static final String FROM_ACC = "from_acc";
	
	public static final String ACTION_START_SUCCESS = "com.youku.acc.ACTION_START_SUCCESS";
	
	public static final String ACTION_START_FAILURE = "com.youku.acc.ACTION_START_FAILURE";
	
	private static AcceleraterServiceManager mInstance = new AcceleraterServiceManager();

	private AcceleraterServiceManager() {
	}

	public static AcceleraterServiceManager getInstance() {
		return mInstance;
	}
	
	/**
	 * 启动acc
	 * 
	 * @param context
	 * @return 成功返回0， 失败返回-1或错误码
	 */
	
	public int startAcc(Context context) {
		Logger.d(TAG, "startAcc()");
		int flag = 0;
		int port;
		
		if (mCurrentStatus != AcceleraterStatus.INIT) {
			Logger.e(TAG, "startAcc() error : mCurrentStatus = " + mCurrentStatus);
			return -1;
		}
		
		String cachePath = getDefauleSDCardPath();
		if (cachePath != null
				&& TextUtils.getTrimmedLength(cachePath) > 0) {
			File f = new File(cachePath + "/youku/");
			boolean success = true;
			if (!f.exists()) {
				success = f.mkdirs();
			}
			
			if (success) {
				int i = start("--mobile-data-path=" + cachePath
						+ " --mobile-meta-path=" + cachePath + "/youku"
						+ " --android-version=android_"
						+ android.os.Build.VERSION.RELEASE);
				if (i == 0) {
					port = getHttpProxyPort();
					if (port != -1) {
						ACC_PORT = "&myp=" + port;
						mCurrentStatus = AcceleraterStatus.STARTED;
						Logger.d(TAG, "ACC启动成功/PORT地址：" + ACC_PORT);
					} else {
						Logger.d(TAG,
								"ACC启动失败/Accstub.getHttpProxyPort()==-1");
						ACC_PORT = "";
						sFailReason = "6-获取端口号失败";
						flag = -1;
					}
				} else {
					Logger.d(TAG, "ACC启动失败/Accstub.start()==" + i);
					ACC_PORT = "";
					flag = i;
					sFailReason = "11-其他因素失败:" + i;
				}
			} else {
				flag = -1;
				sFailReason = "10-无youkudisk文件夹";
			}
			
		} else {
			Logger.d(TAG, "ACC启动失败 /cachePath:" + cachePath);
			sFailReason = "7-获取缓存路径失败:" + cachePath;
			flag = -1;
		}
		
		Intent intent = new Intent("android.intent.action.DOWNLOAD_TRACKER");
		intent.putExtra("from", FROM_ACC);
		if (flag != 0) {
			intent.putExtra(RESTRICTBY, sFailReason);
			context.sendBroadcast(intent);
			Logger.d(TAG, "统计失败原因");
		} else {
			intent.putExtra(SUCCSTARTP2P, "0-加速器启动成功");
			context.sendBroadcast(intent);
			Logger.d(TAG, "统计启动成功");
		}
		
		return flag;
	}
	/*public int startAcc(Context context) {
		Logger.d(TAG, "startAcc()");
		
		int flag = 0;
		
		switch (isAvailable()) {
		case 1:
			int port = getHttpProxyPort();
			if (port != -1) {
				Logger.d(TAG, "ACC已启动");
				ACC_PORT = "&myp=" + port;
			} else {
				Logger.d(TAG, "ACC启动失败/进入重新启动");
				ACC_PORT = "";
				startAcc(context);
			}
			break;
		case 0:
			flag = resume();
			break;
		case -1:
			if (!isACCEnable()) {
				Logger.d(TAG, "ACC启动失败/手机不满足ACC运行条件");
				flag = -1;
			} else {
				String cachePath = getDefauleSDCardPath();

				if (cachePath != null
						&& TextUtils.getTrimmedLength(cachePath) > 0) {
					File f = new File(cachePath + "/youku/");
					if (!f.exists())
						f.mkdirs();
					int i = start("--mobile-data-path=" + cachePath
							+ " --mobile-meta-path=" + cachePath + "/youku"
							+ " --android-version=android_"
							+ android.os.Build.VERSION.RELEASE);
					if (i == 0) {// 需要延迟1秒后获取端口
						port = getHttpProxyPort();
						if (port != -1) {
							ACC_PORT = "&myp=" + port;
							Logger.d(TAG, "ACC启动成功/PORT地址：" + ACC_PORT);
						} else {
							Logger.d(TAG,
									"ACC启动失败/Accstub.getHttpProxyPort()==-1");
							ACC_PORT = "";
						}
					} else {
						Logger.d(TAG, "ACC启动失败/Accstub.start()==" + i);
						ACC_PORT = "";
						flag = i;
					}
				} else {
					Logger.d(TAG, "ACC启动失败 /cachePath:" + cachePath);
					flag = -1;
				}
			}
			break;
		default:
			break;
		}
		
		return flag;
	}*/
	
	public int pauseAcc() {
		Logger.d(TAG, "pauseAcc()");
		if (mCurrentStatus != AcceleraterStatus.STARTED) {
			Logger.e(TAG, "pauseAcc() error : mCurrentStatus = " + mCurrentStatus);
			return -1;
		}
		
		if (pause() == -1) {
			Logger.e(TAG, "pause() == -1");
			return -1;
		}
		
		mCurrentStatus = AcceleraterStatus.PAUSED;
		Logger.d(TAG, "pauseAcc() success!");
		
		return 0;
	}
	
	public int resumeAcc() {
		Logger.d(TAG, "resumeAcc()");
		
		if (mCurrentStatus != AcceleraterStatus.PAUSED) {
			Logger.e(TAG, "resumeAcc() error : mCurrentStatus = " + mCurrentStatus);
			return -1;
		}
		
		if (resume() == -1) {
			Logger.e(TAG, "resume() == -1");
			return -1;
		}
		
		mCurrentStatus = AcceleraterStatus.STARTED;
		Logger.d(TAG, "resumeAcc() success!");
		
		return 0;
	}
	
	public void stopAcc() {
		Logger.d(TAG, "stopAcc()");
		
		ACC_PORT = "";
		stop();
		
		mCurrentStatus = AcceleraterStatus.STOPED;
	}
	
	public int getCurrentStatus() {
		return mCurrentStatus;
	}
	
	public int isAccAvailable() {
		Logger.d(TAG, "isAccAvailable()");
		
		if (!isACCEnable()) {
			return -1;
		}
		
		return isAvailable();
	}
	
	public static String getAccVersionName() {
		Logger.d(TAG, "getAccVersionName()");
		return getVersionName();
	}
	
	public static int getAccVersionCode() {
		Logger.d(TAG, "getAccVersionCode()");
		return getVersionCode();
	}
	
	public int getAccHttpProxyPort() {
		Logger.d(TAG, "getAccHttpProxyPort()");
		return getHttpProxyPort();
	}
	
	public static String sFailReason;
	
	public static boolean isACCEnable() {
		Logger.d(TAG, "isACCEnable()");
		Context context = com.baseproject.utils.Profile.mContext;
		Logger.e(TAG, "p2pSwitch = " + AccInitData.getP2pSwitch(context));
		switch (AccInitData.getP2pSwitch(context)) {
		case 0:
			Logger.e(TAG, "2-P2P开关获取成功，但开关状态为关闭");
			sFailReason = "2-P2P开关获取成功，但开关状态为关闭";
			return false;
			
		case -1:
			Logger.e(TAG, "5-P2P开关获取失败");
			sFailReason = "5-P2P开关获取失败";
			return false;

		default:
			break;
		}
		
		if (Build.VERSION.SDK_INT < AccInitData.getAndroidVersionRestrict(context)) {
			Logger.e(TAG, "4-Andriod版本低于");
			sFailReason = "4-Andriod版本低于" + AccInitData.getAndroidVersionRestrict(context);
			return false;
		}
		
		if (Utils.getMemoryClass(context) < AccInitData.getMemoryRestrict(context)) {
			Logger.e(TAG, "0-内存<" + AccInitData.getMemoryRestrict(context) + "M");
			sFailReason = "0-内存<" + AccInitData.getMemoryRestrict(context) + "M";
			return false;
		}
		
		if (!hasSDCard()) {
			Logger.e(TAG, "3-没sd卡");
			sFailReason = "3-没sd卡";
			return false;
		}
		
		if (!AccInitData.isUplayerSupported(AccInitData.getCpuRestrict(context))) {
			Logger.e(TAG, "1-CPU未满足软解要求" + ", abi = " + android.os.Build.CPU_ABI
					+ ", hasNeon = " + AccInitData.sHasNeon
					+ ", freq = " + AccInitData.getCpuRestrict(context));
			sFailReason = "1-CPU未满足软解要求" 
							+ ", abi = " + android.os.Build.CPU_ABI
							+ ", hasNeon = " + AccInitData.sHasNeon
							+ ", freq = " + AccInitData.sCpuFreq;
			return false;
		}
		
		
		/*if (Build.VERSION.SDK_INT >= 9 && MediaPlayerProxy.isUplayerSupported()
				&& Utils.getMemoryClass(com.baseproject.utils.Profile.mContext) >= 47
				&& hasSDCard()) {
			//AnalyticsWrapper.trackExtendCustomEvent(context, name, page, target, userID, extend);
			return true;
		}*/
		Logger.e(TAG, "p2p启动的条件全部满足");
		return true;
	}
	
	/** Returns 是否有SD卡 */
	public static boolean hasSDCard() {
		Logger.d(TAG, "hasSDCard()");
		return Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	public static String getDefauleSDCardPath() {
		Logger.d(TAG, "getDefauleSDCardPath()");
		return hasSDCard() ? Environment.getExternalStorageDirectory()
				.getAbsolutePath() : "";
	}
	
	/**
	 * @brief 启动加速器
	 * @return 成功启动返回0，否则返回相应的错误号
	 */
	private native int start(final String command);

	/**
	 * @brief 停止Iku加速器
	 */
	private native void stop();

	/**
	 * @return -1代表未成功获取
	 */
	private native int getHttpProxyPort();

	/**
	 * 暂停加速（要求在调用start成功或resume（isAvailablie返回1）成功之后调用该接口函数）
	 * 
	 * @return 0成功；-1失败
	 */
	private native int pause();

	/**
	 * 恢复运行加速器（要求在调用pause成功（isAvailablie返回0）之后调用该接口函数）
	 * 
	 * @return 0成功；-1失败
	 */
	private native int resume();

	/**
	 * 判断加速器是否可用，当前运行状态
	 * 
	 * @return -1不可用；0处于暂停；1可用
	 */
	private native int isAvailable();

	/**
	 * 获取加速去版本号
	 */
	private static native String getVersionName();
	
	private static native int getVersionCode();
	
	public static void postEventFromNative(int what, int arg1, int arg2, Object obj) {
		Logger.e(TAG, "postEventFromNative : what = " + what);
	}
	
	static {
//		String accPath = SoUpgradeStatics.getAccSo(com.baseproject.utils.Profile.mContext);
//		
//		if (FileUtils.isFileExist(accPath)) {
//			Logger.d(SoUpgradeService.TAG, "System.load(" + accPath + ")");
//			System.load(accPath);
//		} else {
//			Logger.d(SoUpgradeService.TAG, "System.loadLibrary(accstub)");
			System.loadLibrary("accstub");
//		}
		
	}

}
