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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.baseproject.utils.Logger;
import com.youku.player.YoukuPlayerApplication;
import com.youku.player.YoukuPlayerBaseApplication;
import com.youku.player.util.PlayerUtil;
import com.youku.service.acc.AccInitData;
import com.youku.service.acc.AcceleraterManager;

/**
 * DownloadManager.缓存管理
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2012-11-5 下午1:16:02
 */
public class DownloadManager extends BaseDownload {

	private static final String TAG = "Download_Manager";

	private AcceleraterManager acceleraterManager;
	
	private static DownloadManager instance;

	private OnChangeListener listener;

	// 服务接口
	private IDownloadService downloadService;

	/** 已缓存完成的对象列表 */
	private static HashMap<String, DownloadInfo> downloadedData;

	private ICallback mCallback = new ICallback.Stub() {

		@Override
		public void onChanged(DownloadInfo info) throws RemoteException {
			if (listener != null)
				listener.onChanged(info);
		}

		@Override
		public void onFinish(DownloadInfo info) throws RemoteException {
			if (downloadedData != null)
				downloadedData.put(info.videoid,
						getDownloadInfoBySavePath(info.savePath));
			if (listener != null)
				listener.onFinish();
		}

		@Override
		public void refresh() throws RemoteException {
			downloadedData = getNewDownloadedData();
		}

	};

	// 服务绑定器
	private ServiceConnection sConnect = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Logger.d(TAG, "onServiceDisconnected() called");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// 绑定到服务
			Logger.d(TAG, "onServiceConnected() called");
			downloadService = IDownloadService.Stub.asInterface(service);
			try {
				downloadService.registerCallback(mCallback);
			} catch (RemoteException e) {
				Logger.e(TAG, e);
			}
		}
	};

	public synchronized static DownloadManager getInstance() {
		if (instance == null) {
			Logger.d(TAG, "getInstance()");
			instance = new DownloadManager(YoukuPlayerApplication.context);
		}
		return instance;
	}

	private DownloadManager(Context context) {
		acceleraterManager = AcceleraterManager.getInstance(context);
//		setDownloadPath();
		this.context = context;
		bindService(context);
	}

	private void bindService(Context context) {
		Intent intent = new Intent(context, DownloadService.class);
		context.bindService(intent, sConnect, Context.BIND_AUTO_CREATE);
	}

	public void unregister() {
		try {
			downloadService.unregister();
		} catch (RemoteException e) {
			Logger.e(TAG, e);
		}catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	public void setOnChangeListener(OnChangeListener listener) {
		this.listener = listener;
	}

	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, DownloadInfo> getDownloadingData() {
		if (downloadService != null) {
			try {
				return (HashMap<String, DownloadInfo>) downloadService
						.getDownloadingData();
			} catch (RemoteException e) {
				Logger.e(TAG, e);
			}
		}

		HashMap<String, DownloadInfo> downloadingData = new HashMap<String, DownloadInfo>();
		if (sdCard_list == null
				&& (sdCard_list = SDCardManager.getExternalStorageDirectory()) == null) {
			return downloadingData;
		}
		for (int j = 0; j < sdCard_list.size(); j++) {
			File dir = new File(sdCard_list.get(j).path + YoukuPlayerApplication.getDownloadPath());
			if (!dir.exists())
				continue;
			String[] dirs = dir.list();
			for (int i = dirs.length - 1; i >= 0; i--) {
				String vid = dirs[i];
				DownloadInfo info = getDownloadInfoBySavePath(sdCard_list
						.get(j).path + YoukuPlayerApplication.getDownloadPath() + vid + "/");
				if (info != null
						&& info.getState() != DownloadInfo.STATE_FINISH
						&& info.getState() != DownloadInfo.STATE_CANCEL) {
					// info.downloadListener = new DownloadListenerImpl(context,
					// info);
					downloadingData.put(info.taskId, info);
				}
			}
		}
		return downloadingData;
	}

	/**
	 * 已经完成的视频缓存列表
	 * 
	 * @return Map<String, DownloadInfo>
	 */
	public HashMap<String, DownloadInfo> getDownloadedData() {
//		if (downloadedData == null) {
			downloadedData = getNewDownloadedData();
//		}
		return downloadedData;
	}

	private HashMap<String, DownloadInfo> getNewDownloadedData() {
		downloadedData = new HashMap<String, DownloadInfo>();
		if ((sdCard_list = SDCardManager.getExternalStorageDirectory()) == null) {
			return downloadedData;
		}
		for (int j = 0; j < sdCard_list.size(); j++) {
			File dir = new File(sdCard_list.get(j).path + YoukuPlayerApplication.getDownloadPath());
			if (!dir.exists())
				continue;
			String[] dirs = dir.list();
			for (int i = dirs.length - 1; i >= 0; i--) {
				String vid = dirs[i];
				final DownloadInfo d = getDownloadInfoBySavePath(sdCard_list
						.get(j).path + YoukuPlayerApplication.getDownloadPath() + vid + "/");
				if (d != null && d.getState() == DownloadInfo.STATE_FINISH) {
					downloadedData.put(d.videoid, d);
					if (d.segCount != d.segsSeconds.length) {
						new Thread() {
							public void run() {
								try {
									DownloadUtils.getDownloadData(d);
									downloadedData.put(d.videoid, d);
									DownloadUtils.makeDownloadInfoFile(d);
									DownloadUtils.makeM3U8File(d);
								} catch (Exception e) {
								}
							};
						}.start();
					}
				}
			}
		}
		return downloadedData;
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<DownloadInfo> getDownloadedList() {
		ArrayList<DownloadInfo> list = new ArrayList<DownloadInfo>();
		Iterator iter = getDownloadedData().entrySet().iterator(); // 获得map的Iterator
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			list.add((DownloadInfo) entry.getValue());
		}
		return list;
	}

	/**
	 * TODO 根据showid和集数号查本地视频，为null则不存在
	 */
	@SuppressWarnings("rawtypes")
	public DownloadInfo getDownloadInfo(String showId, int show_videoseq) {
		if (showId == null)
			return null;
		Iterator iter = getDownloadedData().entrySet().iterator(); // 获得map的Iterator
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			if (showId.equals(info.showid)
					&& info.show_videoseq == show_videoseq
					&& info.getState() != DownloadInfo.STATE_CANCEL) {
				return info;
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<DownloadInfo> getDownloadInfoListById(
			String videoIdOrShowId) {
		if (videoIdOrShowId == null) {
			return null;
		}
		ArrayList<DownloadInfo> list = null;
		if (getDownloadedData().containsKey(videoIdOrShowId)) {
			if (list == null)
				list = new ArrayList<DownloadInfo>();
			list.add(getDownloadedData().get(videoIdOrShowId));
		} else {
			Iterator iter = getDownloadedData().entrySet().iterator(); // 获得map的Iterator
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				DownloadInfo info = (DownloadInfo) entry.getValue();
				if (videoIdOrShowId.equals(info.showid)) {
					if (list == null)
						list = new ArrayList<DownloadInfo>();
					list.add(info);
				}
			}
			if (list != null && list.size() > 1) {
				DownloadInfo.compareBySeq = true;// 按剧集排序
				Collections.sort(list);// 排序
			}
		}
		return list;
	}

	/**
	 * TODO Comment：获得本地下一集视频的相关信息
	 * 
	 * @param vid
	 * @return DownloadInfo下一个视频信息,若无下一集则返回null
	 */
	@SuppressWarnings("rawtypes")
	public DownloadInfo getNextDownloadInfo(String videoId) {
		DownloadInfo thisinfo = getDownloadInfo(videoId);
		ArrayList<DownloadInfo> temp = new ArrayList<DownloadInfo>();
		Iterator iter = getDownloadedData().entrySet().iterator(); // 获得map的Iterator

		if (thisinfo == null)
			return null;
		boolean isthis = false;
		if (thisinfo.isSeries()) {// 是剧集
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				DownloadInfo info = (DownloadInfo) entry.getValue();
				if (info.showid.equals(thisinfo.showid)) {
					temp.add(info);
				}
			}
			DownloadInfo.compareBySeq = true;// 按剧集排序
			Collections.sort(temp);// 排序
			for (DownloadInfo info : temp) {
				if (isthis) {
					return info;
				} else if (info.videoid.equals(videoId)) {
					isthis = true;
				}
			}
		} else {// 非剧集
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				DownloadInfo info = (DownloadInfo) entry.getValue();
				temp.add(info);
			}
			DownloadInfo.compareBySeq = false;// 按时间排序
			Collections.sort(temp);// 排序
			for (DownloadInfo info : temp) {
				if (isthis && !info.isSeries()) {
					return info;
				} else if (thisinfo.videoid.equals(info.videoid)) {
					isthis = true;
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据showId获得已缓存的视频数
	 * @param showId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public int getHowManyDownloadsByShowId(String showId) {
		if (showId == null || showId.length() == 0)
			return 0;
		int count = 0;
		Iterator iter = getDownloadedData().entrySet().iterator(); // 获得map的Iterator
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			if (showId.equals(info.showid)) {
				count++;
			}
		}
		return count;
	}

	private OnCreateDownloadListener lis = null;

	/**
	 * TODO 创建下载任务
	 */
	public void createDownload(String videoId, String videoName,
			OnCreateDownloadListener listener) {
		setOnCreateDownloadListener(listener);
		try {
			downloadService.createDownload(videoId, videoName);
		} catch (RemoteException e) {
			Logger.e(TAG, e);
		}
	}

	/**
	 * TODO 创建下载任务(批量)
	 */
	public void createDownloads(String[] videoIds, String[] videoNames,
			OnCreateDownloadListener listener) {
		setOnCreateDownloadListener(listener);
		try {
			downloadService.createDownloads(videoIds, videoNames);
		} catch (RemoteException e) {
			Logger.e(TAG, e);
		}
	}

	private void setOnCreateDownloadListener(OnCreateDownloadListener listener) {
		lis = listener;
		if (listener == null) {
			return;
		}
		OnCreateDownloadReceiver on = new OnCreateDownloadReceiver() {
			@Override
			public void onOneReady() {
				if (lis != null)
					lis.onOneReady();
			}

			@Override
			public void onOneFailed() {
				if (lis != null)
					lis.onOneFailed();
			}

			@Override
			public void onfinish(boolean isNeedRefresh) {
				if (lis != null)
					lis.onfinish(isNeedRefresh);
				lis = null;
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CREATE_DOWNLOAD_ONE_READY);
		filter.addAction(ACTION_CREATE_DOWNLOAD_ONE_FAILED);
		filter.addAction(ACTION_CREATE_DOWNLOAD_ALL_READY);
		YoukuPlayerApplication.context.registerReceiver(on, filter);
	}

	public abstract class OnCreateDownloadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (IDownload.ACTION_CREATE_DOWNLOAD_ONE_READY.equals(action)) {
				Logger.d("Download_OnCreateDownloadListener", "onOneReady()");
				onOneReady();
			} else if (IDownload.ACTION_CREATE_DOWNLOAD_ALL_READY
					.equals(action)) {
				boolean value = intent.getBooleanExtra(
						IDownload.KEY_CREATE_DOWNLOAD_IS_NEED_REFRESH, true);
				Logger.d("Download_OnCreateDownloadListener", "onAllReady():"
						+ value);
				onfinish(value);
			} else if (IDownload.ACTION_CREATE_DOWNLOAD_ONE_FAILED
					.equals(action)) {
				Logger.d("Download_OnCreateDownloadListener", "onOneFailed()");
				onOneFailed();
			}
		}

		/** 当每一个下载已准备的时候 */
		public abstract void onOneReady();

		/** 当每一个下载失败 */
		public abstract void onOneFailed();

		/**
		 * 当全部下载已准备的时候
		 * 
		 * @param isNeedRefresh
		 *            是否需要刷新数据
		 */
		public abstract void onfinish(boolean isNeedRefresh);

	}

	@Override
	public void startDownload(String taskId) {
		try {
			downloadService.down(taskId);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	@Override
	public void pauseDownload(String taskId) {
		try {
			downloadService.pause(taskId);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	@Override
	public void refresh() {
		try {
			downloadService.refresh();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	@Override
	public void startNewTask() {
		Intent i = new Intent(context, DownloadService.class);
		i.setAction(DownloadService.ACTION_STAER_NEWTASK);
		context.startService(i);
	}

	@Override
	public void stopAllTask() {
		try {
			downloadService.stopAllTask();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	@Override
	public boolean deleteDownloading(String taskId) {
		Logger.d(TAG, "deleteDownloading() :" + taskId);
		try {
			downloadService.delete(taskId);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		return false;
	}

	@Override
	public boolean deleteAllDownloading() {
		Logger.d(TAG, "deleteAllDownloading()");
		try {
			return downloadService.deleteAll();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		return false;
	}

	public boolean deleteDownloaded(final DownloadInfo info) {
		Logger.d(TAG, "deleteDownloaded() :" + info.title);
		// info.setState(DownloadInfo.STATE_CANCEL);
		downloadedData.remove(info.videoid);
		if (YoukuPlayerApplication.getPreference(KEY_LAST_NOTIFY_TASKID).equals(info.taskId)) {
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
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

	public boolean deleteDownloadeds(final ArrayList<DownloadInfo> infos) {
		Logger.d(TAG, "deleteDownloadeds() : ArrayList");
		if (infos == null || infos.size() == 0)
			return true;
		String nId = YoukuPlayerApplication.getPreference(KEY_LAST_NOTIFY_TASKID);
		for (DownloadInfo info : infos) {
			downloadedData.remove(info.videoid);
			if (nId.equals(info.taskId)) {
				NotificationManager nm = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancel(NOTIFY_ID);
				YoukuPlayerApplication.savePreference(KEY_LAST_NOTIFY_TASKID, "");
			}
		}
		new Thread() {
			public void run() {
				for (DownloadInfo info : infos) {
					PlayerUtil.deleteFile(new File(info.savePath));
				}
			};
		}.start();
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean deleteAllDownloaded() {
		Logger.d(TAG, "deleteAllDownloaded()");
		if (getDownloadedData().size() == 0)
			return true;
		final HashMap<String, DownloadInfo> clone = (HashMap<String, DownloadInfo>) getDownloadedData()
				.clone();
		String nId = YoukuPlayerApplication.getPreference(KEY_LAST_NOTIFY_TASKID);
		Iterator iter = getDownloadedData().entrySet().iterator(); // 获得map的Iterator
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
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
		getDownloadedData().clear();
		return true;
	}

	@Override
	public final String getCurrentDownloadSDCardPath() {
		try {
			return downloadService.getCurrentDownloadSDCardPath();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		return YoukuPlayerApplication.getPreference("download_file_path",
				SDCardManager.getDefauleSDCardPath());
	}

	@Override
	public void setCurrentDownloadSDCardPath(String path) {
		try {
			downloadService.setCurrentDownloadSDCardPath(path);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}

	}

	@Override
	public boolean canUse3GDownload() {
		try {
			return downloadService.canUse3GDownload();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		return YoukuPlayerApplication.getPreferenceBoolean("allowCache3G", false);
	}

	@Override
	public void setCanUse3GDownload(boolean flag) {
		try {
			downloadService.setCanUse3GDownload(flag);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	@Override
	public boolean canUseAcc() {
		try {
			return downloadService.canUseAcc();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		return false;
	}

	@Override
	public void setP2p_switch(int value) {
		YoukuPlayerApplication.savePreference("p2p_switch", value);
		try {
			downloadService.setP2p_switch(value);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
//		if (value == 1) {
//			Logger.d("Download_ACCFlow", "获取P2P开关结果：打开");
//			if (canUseAcc()) {
//				acceleraterManager.startService();
//				IStaticsManager.p2pStart();
//			} else {
//				Logger.d("Download_ACCFlow", "ACC启动失败/不满足ACC运行条件");
//				acceleraterManager.stopService();
//				IStaticsManager.p2pFail("ACC启动失败/不满足ACC运行条件");
//			}
//		} else if (value == 0) {
//			Logger.d("Download_ACCFlow", "获取P2P开关结果：关闭");
//			acceleraterManager.stopService();
//			IStaticsManager.p2pFail("获取P2P开关结果：关闭");
//		} else if (value == -1) {
//			Logger.d("Download_ACCFlow", "获取P2P开关结果：获取失败，默认关闭");
//			acceleraterManager.stopService();
//			IStaticsManager.p2pFail("获取P2P开关结果：获取失败，默认关闭");
//		}
		AccInitData.setP2pSwitch(YoukuPlayerApplication.context, value);
		acceleraterManager.startService();
//		IStaticsManager.p2pStart();
	}

	@Override
	public String getAccPort() {
		try {
			return downloadService.getAccPort();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		return "";
	}

	@Override
	public int getDownloadFormat() {
		try {
			return downloadService.getDownloadFormat();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		return DownloadUtils.getDownloadFormat();
	}

	@Override
	public void setDownloadFormat(int format) {
		DownloadUtils.setDownloadFormat(format);
		try {
			downloadService.setDownloadFormat(format);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	@Override
	public int getDownloadLanguage() {
		try {
			return downloadService.getDownloadLanguage();
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		return DownloadUtils.getDownloadLanguage();
	}

	@Override
	public void setDownloadLanguage(int language) {
		DownloadUtils.setDownloadLanguage(language);
		try {
			downloadService.setDownloadLanguage(language);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	public void setTimeStamp(long time) {
		try {
			downloadService.setTimeStamp(time);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}
//
//	@Override
//	public void setDownloadPath() {
//		// TODO Auto-generated method stub
//		if(!TextUtils.isEmpty(YoukuPlayerBaseApplication.downloadRootFolderName)){
////			FILE_PATH = YoukuPlayerBaseApplication.downloadRootFolderName + FILE_PATH;
//		}
//	}

}
