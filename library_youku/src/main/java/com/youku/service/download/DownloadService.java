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

import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.baseproject.utils.Logger;

/**
 * DownloadService.下载服务
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2012-11-5 下午1:16:02
 */
public class DownloadService extends Service {

	private static final String TAG = "Download_Service";

	public static final String ACTION_CREATE = "create";
	public static final String ACTION_STAER_NEWTASK = "startNewTask";

	public static final String KEY_TASKID = "taskId";

	private DownloadServiceManager manager;

	private static long time = 0l;

	private IDownloadService.Stub mBinder = new IDownloadService.Stub() {
		@Override
		public void createDownload(String videoId, String videoName)
				throws RemoteException {
			manager.createDownload(videoId, videoName);
		}

		@Override
		public void createDownloads(String[] videoIds, String[] videoNames)
				throws RemoteException {
			manager.createDownloads(videoIds, videoNames);
		}

		@Override
		public void down(String taskId) throws RemoteException {
			manager.startDownload(taskId);
		}

		@Override
		public void pause(String taskId) throws RemoteException {
			manager.pauseDownload(taskId);
		}

		@Override
		public boolean delete(String taskId) throws RemoteException {
			return manager.deleteDownloading(taskId);
		}

		@Override
		public boolean deleteAll() throws RemoteException {
			return manager.deleteAllDownloading();
		}

		@Override
		public Map<String, DownloadInfo> getDownloadingData()
				throws RemoteException {
			return manager.getDownloadingData();
		}

		@Override
		public void registerCallback(ICallback cb) throws RemoteException {
			manager.registerCallback(cb);
		}

		@Override
		public void unregister() throws RemoteException {
			manager.unregister();
		}

		@Override
		public void refresh() throws RemoteException {
			manager.refresh();
		}

		@Override
		public void startNewTask() throws RemoteException {
			manager.startNewTask();
		}

		@Override
		public void stopAllTask() throws RemoteException {
			manager.stopAllTask();
		}

		@Override
		public String getCurrentDownloadSDCardPath() throws RemoteException {
			return manager.getCurrentDownloadSDCardPath();
		}

		@Override
		public void setCurrentDownloadSDCardPath(String path)
				throws RemoteException {
			manager.setCurrentDownloadSDCardPath(path);
		}

		@Override
		public boolean canUse3GDownload() throws RemoteException {
			return manager.canUse3GDownload();
		}

		@Override
		public void setCanUse3GDownload(boolean flag) throws RemoteException {
			manager.setCanUse3GDownload(flag);
		}

		@Override
		public boolean canUseAcc() throws RemoteException {
			return manager.canUseAcc();
		}

		@Override
		public void setP2p_switch(int value) throws RemoteException {
			manager.setP2p_switch(value);
		}

		@Override
		public String getAccPort() throws RemoteException {
			return manager.getAccPort();
		}

		@Override
		public int getDownloadFormat() throws RemoteException {
			return manager.getDownloadFormat();
		}

		@Override
		public void setDownloadFormat(int format) throws RemoteException {
			manager.setDownloadFormat(format);
		}

		@Override
		public int getDownloadLanguage() throws RemoteException {
			return manager.getDownloadLanguage();
		}

		@Override
		public void setDownloadLanguage(int language) throws RemoteException {
			manager.setDownloadLanguage(language);
		}

		@Override
		public void setTimeStamp(long time) throws RemoteException {
			manager.setTimeStamp(time);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		Logger.d(TAG, "onBind()");
		return mBinder;
	}

	@Override
	public void onCreate() {
		Logger.d(TAG, "onCreate()");
		manager = DownloadServiceManager.getInstance();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return START_NOT_STICKY;
		}
		Logger.d(TAG, "onStartCommand():" + flags + "/" + startId + "/"
				+ intent.getAction());
		if (ACTION_CREATE.equals(intent.getAction())) {
			long now = System.currentTimeMillis();
			if ((now - time) > 1000l) {
				time = now;
				String videoId = intent.getStringExtra("videoId");
				String videoName = intent.getStringExtra("videoName");
				manager.createDownload(videoId, videoName);
			}
			return START_NOT_STICKY;
		} else if (ACTION_STAER_NEWTASK.equals(intent.getAction())) {
			manager.startNewTask();
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		Logger.d(TAG, "onDestroy()");
		manager.destroy();
		super.onDestroy();
	}
}
