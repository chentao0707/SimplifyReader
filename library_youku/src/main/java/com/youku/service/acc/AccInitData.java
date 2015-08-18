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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.baseproject.utils.Logger;

public class AccInitData {
	
	private static final String TAG = "Accelerater_InitData";
	
	public static final String PREFS_NAME = "p2p_init";
	
	public static final int PREFS_MODE = 4;
	
	public static final String USER_ID = "user_id";
	
	public static final String P2P_SWITCH = "p2p_switch";
	
	public static final String MEMORY_RESTRICT = "memory_restrict";
	
	public static final String CPU_RESTRICT = "cpu_restrict";
	
	public static final String SDCARD_RESTRICT = "sdcard_restrict";
	
	public static final String ANDROID_VERSION_RESTRICT = "android_version_restrict";
	
	public static String sUserID = null;
	
	public static int sP2pSwitch = 1;
	
	public static int sMemoryRestrict = 47; //默认47M
	
	public static int sCpuRestrict = 800; //默认800M
	
	public static Boolean sSdcardRestrict = true;
	
	public static int sAndroidVerRestrict = 9; //默认android 2.3
	
	private static boolean sIsCpuinfoReaded = false;
	
	// 表明是否支持uplayer
	private static boolean sIsUplayerSupported = false;
	
	public static boolean sHasNeon;
	
	public static boolean sIsArmv7a;
	
	public static int sCpuFreq = 0;
	
	public static Boolean sInited = false;
	
	public static void printAll(Context context) {
		Logger.d(TAG, "=====p2p initData======");
		Logger.d(TAG, "sP2pSwitch = " + getP2pSwitch(context)
				+ "\nsMemoryRestrict = " + getMemoryRestrict(context) + "M"
				+ "\nsCpuRestrict = " + getCpuRestrict(context) + "M"
				+ "\nsSdcardRestrict = " + getSdcardRestrict(context)
				+ "\nsAndroidVerRestrict = " + getAndroidVersionRestrict(context));
	}
	
	public static boolean isUplayerSupported(int cpuRestrict) {

		/*if(sIsCpuinfoReaded)
			return sIsUplayerSupported;
		sIsCpuinfoReaded = true;*/

		final String ARMV7A = "armeabi-v7a";

		String strLine;
		sHasNeon = false;
		if(android.os.Build.CPU_ABI.toLowerCase().equals("x86")){
			sIsUplayerSupported = true;
			return sIsUplayerSupported;
		}
		sIsArmv7a = android.os.Build.CPU_ABI.toLowerCase()
				.equals(ARMV7A);
		int sdkVersion = 0;

		try {
			sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			sdkVersion = 0;
		}

		if (sdkVersion < 8) {
			Logger.e(TAG,
					"Android version is less than 2.2, not supported by Uplayer!!");
			return false;
		}

		String cpuInfo = "";
		try {
			BufferedReader cpuinfoReader = new BufferedReader(new FileReader(
					"/proc/cpuinfo"));
			while ((strLine = cpuinfoReader.readLine()) != null) {
				cpuInfo = cpuInfo + strLine + "\n";
				strLine = strLine.toUpperCase();

				if (strLine.startsWith("FEATURES")) {
					int idx = strLine.indexOf(':');
					if (idx != -1) {
						strLine = strLine.substring(idx + 1);
						sHasNeon = (strLine.indexOf("NEON") != -1);
					}
				}
			}

			cpuinfoReader.close();
			cpuinfoReader = null;

			cpuinfoReader = new BufferedReader(new FileReader(
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"));
			strLine = cpuinfoReader.readLine();
			if (strLine != null) {
				strLine = strLine.trim();
				sCpuFreq = Integer.parseInt(strLine);
				cpuInfo = cpuInfo + "cpu0 max frequency: " + strLine;
			}

			cpuinfoReader.close();

		} catch (IOException e) {
		}

		//Logger.e(TAG, cpuInfo);

		sCpuFreq += 999;
		sCpuFreq /= 1000;
		sIsUplayerSupported = sCpuFreq >= cpuRestrict && sIsArmv7a && sHasNeon;
		return sIsUplayerSupported;
	
	}
	
	public static void setP2pSwitch(Context context, int p2pSwitch) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, PREFS_MODE).edit();
		editor.putInt(P2P_SWITCH, p2pSwitch);
		editor.commit();
	}
	
	public static void setUserID(Context context, String userID) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, PREFS_MODE).edit();
		editor.putString(USER_ID, userID);
		editor.commit();
	}
	
	public static void setMemoryRestrict(Context context, int space) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, PREFS_MODE).edit();
		editor.putInt(MEMORY_RESTRICT, space);
		editor.commit();
	}
	
	public static void setCpuRestrict(Context context, int cpu) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, PREFS_MODE).edit();
		editor.putInt(CPU_RESTRICT, cpu);
		editor.commit();
	}
	
	public static void setAndroidVersionRestrict(Context context, int version) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, PREFS_MODE).edit();
		editor.putInt(ANDROID_VERSION_RESTRICT, version);
		editor.commit();
	}
	
	public static void setSdcardRestrict(Context context, Boolean sdcard) {
		Editor editor = context.getSharedPreferences(PREFS_NAME, PREFS_MODE).edit();
		editor.putBoolean(SDCARD_RESTRICT, sdcard);
		editor.commit();
	}
	
	/**
	 * @brief 获取P2P开关
	 * 
	 * @param context
	 * @return 默认关闭
	 */
	public static int getP2pSwitch(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, PREFS_MODE);
		return savedata.getInt(P2P_SWITCH, 1);
	}
	
	/**
	 * @brief 获取userID
	 * 
	 * @param context
	 * @return 默认返回值null
	 */
	public static String getUserID(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, PREFS_MODE);
		return savedata.getString(USER_ID, null);
	}
	
	/**
	 * @brief 获取可用内存限制
	 * 
	 * @param context
	 * @return 默认返回值47M
	 */
	public static int getMemoryRestrict(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, PREFS_MODE);
		return savedata.getInt(MEMORY_RESTRICT, 47);
	}
	
	/**
	 * @brief 获取cpu主频限制
	 * 
	 * @param context
	 * @return 默认返回值800M， 单位M
	 */
	public static int getCpuRestrict(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, PREFS_MODE);
		return savedata.getInt(CPU_RESTRICT, 800);
	}
	
	/**
	 * @brief 获取android版本号限制
	 * 
	 * @param context
	 * @return 默认返回值9,即android 2.3
	 */
	public static int getAndroidVersionRestrict(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, PREFS_MODE);
		return savedata.getInt(ANDROID_VERSION_RESTRICT, 9);
	}
	
	/**
	 * @brief 获取SD卡限制
	 * 
	 * @param context
	 * @return true表示需要sd卡，false表示不需要sd卡，默认返回值为true
	 */
	public static Boolean getSdcardRestrict(Context context) {
		SharedPreferences savedata = context.getSharedPreferences(PREFS_NAME, PREFS_MODE);
		return savedata.getBoolean(SDCARD_RESTRICT, true);
	}

}
