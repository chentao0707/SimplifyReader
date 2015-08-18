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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.baseproject.utils.Util;
import com.youku.player.YoukuPlayerApplication;
import com.youku.player.ui.R;
import com.youku.player.util.PlayerUtil;
import com.youku.player.util.URLContainer;
import com.youku.service.acc.AcceleraterManager;
import com.youku.service.acc.AcceleraterServiceManager;

/**
 * DownloadServiceManager.缓存管理
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2012-11-5 下午1:16:02
 */
@SuppressWarnings("rawtypes")
public class DownloadServiceManager extends BaseDownload {

	private static final String TAG = "Download_ServiceManager";

	private static DownloadServiceManager instance;

	private HashMap<String, DownloadInfo> downloadingData;

	private FileDownloadThread thread;

	private ICallback callback;

	/** 初始化注册网络广播的锁，防止错误的网络变化 */
	private boolean initlock = false;

	private boolean first_tips = true;

	private AcceleraterManager acceleraterManager;

	/** 网络改变事件接收器 */
	private BroadcastReceiver networkReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context c, Intent intent) {
			if (initlock) {
				initlock = false;
				return;
			}
			boolean hasNetwork = Util.hasInternet();
			Logger.d(TAG, "network changed : " + hasNetwork);
			if (!hasNetwork) {// 无网络
				if (hasLivingTask()) {
					PlayerUtil.showTips(R.string.download_no_network);
				}
				stopAllTask();
			} else {// 有网络
				cleanRetry();
				/*
				 * if (getP2p_switch() == 1 &&
				 * IAcceleraterService.isACCEnable(YoukuPlayerApplication.context)) { if
				 * (YoukuUtil.isWifi()) {// wifi可用加速器 switch
				 * (Accstub.isAvailable()) { case -1:
				 * IAcceleraterService.startAcc(YoukuPlayerApplication.context); break; case 0:
				 * Accstub.resume(); break; } } else {// 2g/3g不可用加速器 if
				 * (Accstub.isAvailable() == 1) { Accstub.pause(); } } } else {
				 * IAcceleraterService.closeAcc(YoukuPlayerApplication.context); }
				 */

				if (hasDownloadingTask()) {// 有网络，且有下载中视频
					// if (!isAccAvailable() && canUseAcc()) {
					// new Handler() {
					// }.postDelayed(new Runnable() {
					//
					// @Override
					// public void run() {
					// IAcceleraterService.startAcc();// ACC加速的Service
					// }
					// }, 3000);
					// } else {
					// if (BuildConfig.DEBUG)
					// Logger.d("Download_ACCFlow", "有下载中视频,acc状态保持不变");
					// }
				} else {// 有网络，且无下载中视频
					new Handler() {
					}.postDelayed(new Runnable() {

						@Override
						public void run() {
							startNewTask();
						}
					}, 1500);

				}
				if (Util.isWifi()) {// wifi
					first_tips = true;
				} else {// 3G
					friendlyTips();
				}
			}
		}
	};

	/** SD卡事件接收器 */
	private BroadcastReceiver sdcardReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {// 装载的;删除SD上的文件时也会执行此方法
				if (sdCard_list == null) {
					Logger.d(TAG, "装载的");
					sdCard_list = SDCardManager.getExternalStorageDirectory();
					refresh();
				} else {
					String path = intent.getData().getPath();
					boolean hasPath = false;
					for (int i = 0; i < sdCard_list.size(); i++) {
						if (sdCard_list.get(i).path.equals(path)) {
							hasPath = true;
							break;
						}
					}
					if (!hasPath) {
						Logger.d(TAG, "装载的");
						sdCard_list = SDCardManager
								.getExternalStorageDirectory();
						refresh();
					} else {
						Logger.d(TAG, "有文件被删除");
					}
				}
				context.sendBroadcast(new Intent(
						IDownload.ACTION_SDCARD_CHANGED));
				startNewTask();
			} else if (Intent.ACTION_MEDIA_EJECT.equals(action)/* 弹出的 */) {
				Logger.d(TAG, "弹出的");
				if (sdCard_list == null)
					sdCard_list = SDCardManager.getExternalStorageDirectory();
				String path = intent.getData().getPath();
				// 因为卸载SD卡后mount命令刷新是不是很及时，所以做以下处理
				if (sdCard_list != null) {
					for (int i = 0; i < sdCard_list.size(); i++) {
						if (sdCard_list.get(i).path.equals(path)) {
							sdCard_list.remove(i);
							break;
						}
					}
					if (sdCard_list.size() != 0)
						setCurrentDownloadSDCardPath(sdCard_list.get(0).path);
				}
				removeByPath(path);
				context.sendBroadcast(new Intent(
						IDownload.ACTION_SDCARD_CHANGED));
				NotificationManager nm = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancel(NOTIFY_ID);
				YoukuPlayerApplication.savePreference(KEY_LAST_NOTIFY_TASKID, "");
				startNewTask();
			}
		}

	};

	public synchronized static DownloadServiceManager getInstance() {
		if (instance == null) {
			Logger.d(TAG, "getInstance()");
			instance = new DownloadServiceManager(YoukuPlayerApplication.context);
		}
		return instance;
	}

	private DownloadServiceManager(Context context) {
		acceleraterManager = AcceleraterManager.getInstance(context);
		acceleraterManager.bindService();
		this.context = context;
		initlock = true;
		try {
			String path = getCurrentDownloadSDCardPath();
			Logger.d(TAG, "getDownloadFilePath():" + path);
			registerReceiver();
			File f = new File(path + "/youku/offlinedata/");
			if (!f.exists())
				f.mkdirs();
			if (f.exists())
				new File(path + "/youku/offlinedata/", ".nomedia")
						.createNewFile();
		} catch (IOException e) {
			Logger.e(TAG, e);
		}
	}

	/** 网络改变及SD卡事件注册 */
	private void registerReceiver() {
		IntentFilter i = new IntentFilter();
		i.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(networkReceiver, i);
		i = new IntentFilter();
		i.addAction(Intent.ACTION_MEDIA_MOUNTED);
		i.addAction(Intent.ACTION_MEDIA_EJECT);
		i.addDataScheme("file");
		context.registerReceiver(sdcardReceiver, i);
	}

	public void registerCallback(ICallback cb) {
		callback = cb;
	}

	public void unregister() {
		callback = null;
	}

	public ICallback getCallback() {
		return callback;
	}

	/** 是否有正在下载中的任务 */
	public boolean hasDownloadingTask() {
		boolean state = false;
		if (thread != null && thread.isStop() == false) {
			state = true;
		}
		Logger.d(TAG, "hasDownloadingTask():" + state);
		return state;
	}

	@Override
	public HashMap<String, DownloadInfo> getDownloadingData() {
		if (downloadingData != null) {
			return downloadingData;
		}
		downloadingData = getNewDownloadingData();
		return downloadingData;
	}

	public void addDownloadingInfo(DownloadInfo info) {
		Logger.d("DownloadFlow","DownloadServiceManager: addDownloadingInfo()");
		if (getDownloadingData() != null) {
			info.downloadListener = new DownloadListenerImpl(context, info);
			downloadingData.put(info.taskId, info);
		}
	}

	/**
	 * 创建下载任务
	 */
	public void createDownload(String videoId, String videoName) {
		// 先判断是否存在
		if (FileCreateThread.tempCreateData != null
				&& FileCreateThread.tempCreateData.containsKey(videoId)) {
			PlayerUtil.showTips(R.string.download_exist_not_finished);
		} else if (existsDownloadInfo(videoId)) {
			if (isDownloadFinished(videoId)) {// 已下载完成
				PlayerUtil.showTips(R.string.download_exist_finished);
			} else {
				PlayerUtil.showTips(R.string.download_exist_not_finished);
			}
		} else if (!Util.hasSDCard()) {// 无sd卡
			PlayerUtil.showTips(R.string.download_no_sdcard);
		} else if (Util.hasInternet()) {
			if (Util.isWifi()) {
				new FileCreateThread(videoId, videoName).start();
				return;
			} else {
				friendlyTips();
				if (canUse3GDownload()) {
					new FileCreateThread(videoId, videoName).start();
					return;
				} else {// 不可用3G/2G下载
					PlayerUtil.showTips(R.string.download_cannot_ues_3g);
				}
			}
		} else {// 无网络
			PlayerUtil.showTips(R.string.download_no_network);
		}

		if (FileCreateThread.tempCreateData != null
				&& FileCreateThread.tempCreateData.containsKey(videoId)) {
			PlayerUtil.showTips(R.string.download_exist_not_finished);
		}

		context.sendBroadcast(new Intent(
				IDownload.ACTION_CREATE_DOWNLOAD_ALL_READY).putExtra(
				IDownload.KEY_CREATE_DOWNLOAD_IS_NEED_REFRESH, false));
	}

	/**
	 * 创建下载任务(批量)
	 */
	public void createDownloads(String[] videoIds, String[] videoNames) {
		if (!Util.hasSDCard()) {// 无sd卡
			PlayerUtil.showTips(R.string.download_no_sdcard);
		} else if (Util.hasInternet()) {
			if (Util.isWifi()) {
				new FileCreateThread(videoIds, videoNames).start();
				return;
			} else {
				friendlyTips();
				if (canUse3GDownload()) {
					new FileCreateThread(videoIds, videoNames).start();
					return;
				} else {// 不可用3G/2G下载
					PlayerUtil.showTips(R.string.download_cannot_ues_3g);
				}
			}
		} else {// 无网络
			PlayerUtil.showTips(R.string.download_no_network);
		}
		context.sendBroadcast(new Intent(
				IDownload.ACTION_CREATE_DOWNLOAD_ALL_READY).putExtra(
				IDownload.KEY_CREATE_DOWNLOAD_IS_NEED_REFRESH, false));
	}

	@Override
	public void startDownload(String taskId) {
		
		if (hasDownloadingTask()) {
			getDownloadingData().get(taskId).setState(
					DownloadInfo.STATE_WAITING);
		} else {
			startThread(taskId);
		}
	}

	private void startThread(String taskId) {
		Logger.d("DownloadFlow","DownloadServiceManager: startThread()");
		DownloadInfo info = getDownloadingData().get(taskId);
		thread = new FileDownloadThread(info);
		info.thread = thread;
		thread.start();
	}

	@Override
	public void pauseDownload(String taskId) {
		getDownloadingData().get(taskId).setState(DownloadInfo.STATE_PAUSE);
	}

	@Override
	public void startNewTask() {
		Logger.d("DownloadFlow","DownloadServiceManager: startNewTask()");
		Logger.d(TAG, "startNewTask()");
		if (!Util.hasInternet()) {
			stopAllTaskNoTips();
			return;
		} else if (!Util.isWifi()) {// 如果不是wifi
			if (canUse3GDownload() == false) {// 不可用3G/2G下载
				return;
			}
		}
		if (hasDownloadingTask())// 如果有下载中视频，直接返回
			return;
		long startTime = 0l;
		String taskId = null;
		Iterator iter = getDownloadingData().entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();			
			int state = info.getState();
			if (state == DownloadInfo.STATE_DOWNLOADING) {// 发现有正在下载中状态的，则优先下载

				startThread(info.taskId);
				return;
			} else 
				if ((state == DownloadInfo.STATE_WAITING
					|| state == DownloadInfo.STATE_EXCEPTION || state == DownloadInfo.STATE_INIT)
					&& info.startTime > startTime) {
				startTime = info.startTime;
				taskId = info.taskId;
			}
		}
		// 开始最后下载操作的视频
		DownloadInfo lastInfo = getDownloadingData().get(taskId);
		Logger.d("DownloadFlow","DownloadUtil: download_info: " + lastInfo);
		if (lastInfo == null) {
		} else if (lastInfo.getState() == DownloadInfo.STATE_WAITING
				|| lastInfo.getState() == DownloadInfo.STATE_INIT) {
			startThread(taskId);
			return;
		} else if (lastInfo.getState() == DownloadInfo.STATE_EXCEPTION
				&& lastInfo.retry <= 0) {// 可控制重试次数
			lastInfo.retry++;
			startThread(taskId);
			return;
		}
		// 没有最后可操作的视频，则按创建时间顺序开始下载
		long firstStartTime = 999999999999999999L;
		iter = getDownloadingData().entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			int state = info.getState();
			if (state == DownloadInfo.STATE_DOWNLOADING) {// 发现有正在下载中状态的，则优先下载
				startThread(info.taskId);
				return;
			}else 
				if ((state == DownloadInfo.STATE_WAITING
					|| state == DownloadInfo.STATE_INIT || state == DownloadInfo.STATE_EXCEPTION)
					&& info.retry < 1 && info.createTime < firstStartTime) {// 可控制重试次数
				firstStartTime = info.createTime;
				taskId = info.taskId;
			}
		}
		DownloadInfo firstInfo = getDownloadingData().get(taskId);
		if (firstInfo == null) {
			return;
		} else if (firstInfo.getState() == DownloadInfo.STATE_WAITING
				|| firstInfo.getState() == DownloadInfo.STATE_INIT) {
			startThread(taskId);
			return;
		} else if (firstInfo.getState() == DownloadInfo.STATE_EXCEPTION
				&& firstInfo.retry <= 0) {// 可控制重试次数
			firstInfo.retry++;
			startThread(taskId);
			return;
		}

	}

	@Override
	public void refresh() {
		Logger.d("DownloadFlow","refresh()");
		if (thread != null)
			thread.cancel();
		downloadingData = getNewDownloadingData();
		if (callback != null) {
			Logger.d("DownloadFlow","refresh(), callback != null");
			try {
				callback.refresh();
			} catch (RemoteException e) {
				Logger.e(TAG, e);
			}
		}
	}

	public void removeByPath(String path) {
		Iterator iter = getDownloadingData().entrySet().iterator();
		ArrayList<String> list = new ArrayList<String>();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			if (info.savePath.contains(path)) {
				if (info.thread != null) {
					thread.cancel();
				}
				list.add(info.taskId);
			}
		}
		for (String taskId : list) {
			downloadingData.remove(taskId);
		}
		if (callback != null) {
			try {
				callback.refresh();
			} catch (RemoteException e) {
				Logger.e(TAG, e);
			}
		}
	}

	@Override
	public void stopAllTask() {
		if (thread != null)
			thread.cancel();
		Iterator iter = getDownloadingData().entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			if (info.getState() == DownloadInfo.STATE_DOWNLOADING) {
				info.setState(DownloadInfo.STATE_WAITING);
			}
		}
	}

	public void stopAllTaskNoTips() {
		if (thread != null)
			thread.cancel();
		Iterator iter = getDownloadingData().entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			if (info.getState() == DownloadInfo.STATE_DOWNLOADING) {
				info.state = DownloadInfo.STATE_WAITING;
				DownloadUtils.makeDownloadInfoFile(info);
				try {
					if (getCallback() != null)
						getCallback().onChanged(info);
				} catch (Exception e) {
					Logger.e(TAG, e);
				}
			}
		}
	}

	@Override
	public boolean deleteDownloading(String taskId) {
		final DownloadInfo info = getDownloadingData().get(taskId);
		// 如果是下载中的视频，改变状态自己就会删除
		info.setState(DownloadInfo.STATE_CANCEL);
		downloadingData.remove(taskId);
		if (thread != null && thread.isStop() == false
				&& taskId.equals(thread.getTaskId())) {
			thread.cancel();
		}
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		if (YoukuPlayerApplication.getPreference(KEY_LAST_NOTIFY_TASKID).equals(info.taskId)) {
			nm.cancel(NOTIFY_ID);
			YoukuPlayerApplication.savePreference(KEY_LAST_NOTIFY_TASKID, "");
		}
		new Thread() {
			public void run() {
				PlayerUtil.deleteFile(new File(info.savePath));
			};
		}.start();
		startNewTask();
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean deleteAllDownloading() {
		String nId = YoukuPlayerApplication.getPreference(KEY_LAST_NOTIFY_TASKID);
		final HashMap<String, DownloadInfo> clone = (HashMap<String, DownloadInfo>) getDownloadingData()
				.clone();
		Iterator iter = clone.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			info.setState(DownloadInfo.STATE_CANCEL);
			if (nId.equals(info.taskId)) {
				NotificationManager nm = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancel(NOTIFY_ID);
				YoukuPlayerApplication.savePreference(KEY_LAST_NOTIFY_TASKID, "");
			}
		}
		new Thread() {
			public void run() {
				Iterator iter = clone.entrySet().iterator();
				while (iter.hasNext()) {
					Entry entry = (Entry) iter.next();
					DownloadInfo info = (DownloadInfo) entry.getValue();
					PlayerUtil.deleteFile(new File(info.savePath));
				}
			};
		}.start();
		getDownloadingData().clear();
		return true;
	}

	/** 是否有正在下载和等待下载的任务 */
	private boolean hasLivingTask() {
		Iterator iter = getDownloadingData().entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			int state = info.getState();
			if (state == DownloadInfo.STATE_INIT
					|| state == DownloadInfo.STATE_DOWNLOADING
					|| state == DownloadInfo.STATE_WAITING
					|| state == DownloadInfo.STATE_EXCEPTION) {
				Logger.d(TAG, "hasLivingTask():true");
				return true;
			}
		}
		Logger.d(TAG, "hasLivingTask():false");
		return false;
	}

	/**
	 * 清除重试次数
	 */
	public void cleanRetry() {
		Iterator iter = getDownloadingData().entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			info.retry = 0;
		}
	}

	/**
	 * 友情提示：您将使用2G或3G网络缓存视频
	 */
	private void friendlyTips() {
		if (first_tips) {
			// 同一网络环境下，只提示一次友情提示
			PlayerUtil.showTips(R.string.player_tips_use_3g);
			first_tips = false;
		}
	}

	public void destroy() {
		stopAllTaskNoTips();
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFY_ID);
	}

	@Override
	public final String getCurrentDownloadSDCardPath() {

		String defauleSDCardPath = SDCardManager.getDefauleSDCardPath();
		if (YoukuPlayerApplication.getPreferenceBoolean("first_install_for_download_path", true)) {
			YoukuPlayerApplication.savePreference("first_install_for_download_path", false);
			if (sdCard_list == null || sdCard_list.size() == 0) {
				sdCard_list = SDCardManager.getExternalStorageDirectory();
			}
			// 如果有两个存储空间
			if (sdCard_list != null && sdCard_list.size() > 1) {
				File dir = new File(defauleSDCardPath + YoukuPlayerApplication.getDownloadPath());
				int count = 0;
				if (dir.exists()) {// 如果内置的存储区可能存在缓存
					String[] dirs = dir.list();
					for (int i = dirs.length - 1; i >= 0; i--) {
						String vid = dirs[i];
						DownloadInfo d = getDownloadInfo(vid);
						if (d != null
								&& d.getState() != DownloadInfo.STATE_CANCEL) {
							count++;
						}
					}

				}
				// 如果内置卡里没有缓存的视频，则设外置为默认
				if (count == 0) {
					for (int i = 0; i < sdCard_list.size(); i++) {
						if (sdCard_list.get(i).isExternal) {
							YoukuPlayerApplication.savePreference("download_file_path",
									sdCard_list.get(i).path);
							break;
						}
					}
				}
			}
		} else if (YoukuPlayerApplication.getPreferenceBoolean(
				"first_install_for_download_path_33", true)) {// 兼容一个S4手机上的错误,在v3.3上修复
			YoukuPlayerApplication.savePreference("first_install_for_download_path_33", false);
			String path = YoukuPlayerApplication.getPreference("download_file_path",
					defauleSDCardPath);
			if (sdCard_list != null) {
				boolean xiangtong = false;
				for (int i = 0; i < sdCard_list.size(); i++) {
					if (sdCard_list.get(i).path.equals(path)) {
						xiangtong = true;
					}
				}
				if (xiangtong == false) {
					YoukuPlayerApplication.savePreference("first_install_for_download_path",
							true);
					return getCurrentDownloadSDCardPath();
				}
			}
		} else if (YoukuPlayerApplication.getPreferenceBoolean(
				"first_install_for_download_path_40", true)) {
			// 4.0 版本 OS 4.4 以上，只能选择主外置 SD 卡的默认位置
			YoukuPlayerApplication.savePreference("first_install_for_download_path_40", false);
			String path = YoukuPlayerApplication.getPreference("download_file_path", "");
			if (!TextUtils.isEmpty(path)
					&& !SDCardManager.getDefauleSDCardPath().equals(path)) {
				YoukuPlayerApplication.savePreference("first_install_for_download_path", true);
				return getCurrentDownloadSDCardPath();
			}

		}
		String path = YoukuPlayerApplication.getPreference("download_file_path",
				defauleSDCardPath);
		SDCardManager m = new SDCardManager(path);
		if (!m.exist()) {
			if (!defauleSDCardPath.equals(path)) {
				path = defauleSDCardPath;
				YoukuPlayerApplication.savePreference("download_file_path", path);
			}
		}
		return path;

	}

	@Override
	public void setCurrentDownloadSDCardPath(String path) {
		YoukuPlayerApplication.savePreference("download_file_path", path);
		context.sendBroadcast(new Intent(IDownload.ACTION_SDCARD_PATH_CHANGED));
	}

	@Override
	public boolean canUse3GDownload() {
		return YoukuPlayerApplication.getPreferenceBoolean("allowCache3G", false);
	}

	@Override
	public void setCanUse3GDownload(boolean flag) {
		YoukuPlayerApplication.savePreference("allowCache3G", flag);
	}

	@Override
	public boolean canUseAcc() {
		if (getP2p_switch() == 1 && AcceleraterServiceManager.isACCEnable()
				&& Util.hasInternet() && Util.isWifi()) {
			return true;
		}
		return false;
	}

	/**
	 * P2P 开关，-1获取失败，0关闭，1开启
	 * 
	 * @return
	 */
	public int getP2p_switch() {
		return YoukuPlayerApplication.getPreferenceInt("p2p_switch", -1);
	}

	@Override
	public void setP2p_switch(int value) {
		YoukuPlayerApplication.savePreference("p2p_switch", value);
	}

	@Override
	public String getAccPort() {
		// return YoukuPlayerApplication.getPreference("acc_port", "");
		return acceleraterManager.getAccPort();
	}

	public boolean isAccAvailable() {
		return acceleraterManager.isAvailable() == 1
				&& getAccPort().length() != 0;
	}

	public int getAccState() {
		return acceleraterManager.isAvailable();
	}

	@Override
	public int getDownloadFormat() {
		return DownloadUtils.getDownloadFormat();
	}

	@Override
	public void setDownloadFormat(int format) {
		DownloadUtils.setDownloadFormat(format);
	}

	@Override
	public int getDownloadLanguage() {
		return DownloadUtils.getDownloadLanguage();
	}

	@Override
	public void setDownloadLanguage(int language) {
		DownloadUtils.setDownloadLanguage(language);
	}

	public void setTimeStamp(long time) {
		URLContainer.TIMESTAMP = time;
	}
}
