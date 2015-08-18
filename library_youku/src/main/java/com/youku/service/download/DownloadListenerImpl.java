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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.RemoteViews;

import com.baseproject.utils.Logger;
import com.youku.player.YoukuPlayerApplication;
import com.youku.player.ui.R;

/**
 * DownloadListenerImpl.下载状态改变的监听实现
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2012-11-5 下午1:16:02
 */
@SuppressLint("InlinedApi")
public class DownloadListenerImpl implements DownloadListener {
	private static final String TAG = "Download_ListenerImpl";
	// private static final long UPDATE_RATE = 3000;// 设置进度、通知、数据库更新时间间隔
	private static final String pageName = "缓存模块";
	public static Context context;
	public DownloadServiceManager download;

	public static NotificationManager nm;
	public static WifiLock wifiLock;
	public static WakeLock wakeLock;
	private DownloadInfo info;

	@SuppressWarnings("static-access")
	public DownloadListenerImpl(Context context, DownloadInfo info) {
		this.context = context;
		this.info = info;
		download = DownloadServiceManager.getInstance();
		if (nm == null)
			nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		if (wifiLock == null)
			wifiLock = ((WifiManager) context
					.getSystemService(Context.WIFI_SERVICE)).createWifiLock(
					WifiManager.WIFI_MODE_FULL_HIGH_PERF,
					context.getPackageName());
		if (wakeLock == null) {
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"mywakelock");
		}

	}

	@Override
	public void onStart() {
			Logger.d(TAG, "onstart() ：" + info.title);
		notify(info, "开始缓存" + info.title,
				"缓存中... - " + DownloadUtils.getProgress(info) + "%", false,
				true);
		info.startTime = System.currentTimeMillis();
		DownloadUtils.makeDownloadInfoFile(info);
		if (wakeLock != null)
			wakeLock.acquire();
		if (wifiLock != null)
			wifiLock.acquire();
		try {
			if (download.getCallback() != null)
				download.getCallback().onChanged(info);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	@Override
	public void onPause() {
			Logger.d(TAG, "onPause() ：" + info.title);
		if (info.thread != null)
			info.thread.cancel();
		DownloadUtils.makeDownloadInfoFile(info);
		try {
			if (download.getCallback() != null)
				download.getCallback().onChanged(info);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		if (!download.hasDownloadingTask()) {
			notify(info, info.title + "已暂停", "暂停中", true, false);
			download.startNewTask();
		}

		release();
	}

	@Override
	public void onCancel() {
			Logger.d(TAG, "onCancel() ：" + info.title);
		DownloadUtils.makeDownloadInfoFile(info);
		// try {
		// if (download.getCallback() != null)
		// download.getCallback().onChanged(info);
		// } catch (Exception e) {
		// Logger.e(TAG, e);
		// }
	}

	@Override
	public void onException() {
			Logger.d(TAG, "onException() ：" + info.title);
		if (info.thread != null) {
			info.thread.cancel();
			info.thread = null;
		}
		if (!download.hasDownloadingTask())
			notify(info, "等待缓存" + info.title, "等待中...", true, false);
		if (info.getExceptionId() == DownloadInfo.EXCEPTION_NO_SDCARD) {
			nm.cancel(IDownload.NOTIFY_ID);
		} else {
			DownloadUtils.makeDownloadInfoFile(info);
		}
		try {
			if (download.getCallback() != null)
				download.getCallback().onChanged(info);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		release();
		if (info.getExceptionId() != DownloadInfo.EXCEPTION_NO_SPACE
				&& info.getExceptionId() != DownloadInfo.EXCEPTION_NO_SDCARD)
			download.startNewTask();
	}

	@Override
	public void onFinish() {
			Logger.d(TAG, "onFinish() ：" + info.title);
		notify(info, info.title + "缓存完成", "缓存完成", true, false);
		info.finishTime = System.currentTimeMillis();
		DownloadUtils.makeDownloadInfoFile(info);
		context.sendBroadcast(new Intent(BaseDownload.ACTION_DOWNLOAD_FINISH));
		try {
			if (download.getCallback() != null)
				download.getCallback().onFinish(info);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
		download.cleanRetry();
		release();
		download.startNewTask();
	}

	@Override
	public void onProgressChange(double progress) {
		long time = System.currentTimeMillis();
			Logger.d(TAG, "onProgressChange() ：" + progress + "%");
		if (info.getState() == DownloadInfo.STATE_DOWNLOADING)
			notify(info, "开始缓存" + info.title,
					"缓存中... - " + DownloadUtils.getProgress(info) + "%", false,
					true);
		info.lastUpdateTime = time;
		DownloadUtils.makeDownloadInfoFile(info);
		try {
			if (download.getCallback() != null)
				download.getCallback().onChanged(info);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	@Override
	public void onWaiting() {
			Logger.d(TAG, "onwainting() ：" + info.title);
		if (info.thread != null) {
			info.thread.cancel();
			info.thread = null;
		}
		if (!download.hasDownloadingTask())
			notify(info, "等待缓存" + info.title, "等待中...", true, false);
		DownloadUtils.makeDownloadInfoFile(info);
		try {
			if (download.getCallback() != null)
				download.getCallback().onChanged(info);
		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	/**
	 * 更新通知
	 * 
	 * @param info
	 * @param ticker
	 * @param notify_state
	 * @param playSound
	 * @param iconRunning
	 */
	private static void notify(DownloadInfo info, String ticker,
			String contentText, boolean autoCancel, boolean iconRunning) {
		if (info.notification == null) {
			info.notification = createNotification(context);
		}
		Logger.d("DownloadFlow","createNotification");
		Notification n = info.notification;
		n.icon = iconRunning ? android.R.drawable.stat_sys_download
				: android.R.drawable.stat_sys_download_done;
		n.flags = autoCancel ? Notification.FLAG_AUTO_CANCEL
				: Notification.FLAG_NO_CLEAR;
//		n.flags |= Notification.FLAG_ONGOING_EVENT;
		n.tickerText = ticker;
		String pkg = YoukuPlayerApplication.context.getPackageName();
		int icon_id = YoukuPlayerApplication.context.getResources().getIdentifier("noitfy_icon", "id", pkg);
		int text_id = YoukuPlayerApplication.context.getResources().getIdentifier("notify_text", "id", pkg);
		int state_id = YoukuPlayerApplication.context.getResources().getIdentifier("notify_state", "id", pkg);
		int notify_processbar_id = YoukuPlayerApplication.context.getResources().getIdentifier("notify_processbar", "id", pkg);
		
		n.contentView.setImageViewResource(icon_id, n.icon);
		n.contentView.setTextViewText(text_id, info.title);
		n.contentView.setTextViewText(state_id, contentText);
		n.contentView.setProgressBar(
				notify_processbar_id,
				100,
				(int) info.getProgress(),
				info.getState() == DownloadInfo.STATE_WAITING
						|| info.getState() == DownloadInfo.STATE_PAUSE
						|| info.getState() == DownloadInfo.STATE_EXCEPTION);
		if (info.getState() == DownloadInfo.STATE_FINISH) {
			n.defaults = Notification.DEFAULT_SOUND;
			n.contentView.setProgressBar(notify_processbar_id, 100, 100,
					false);
			Intent i = new Intent(context, YoukuPlayerApplication.instance.getCachedActivityClass());
			i.putExtra("go", "downloaded");
			n.contentIntent = PendingIntent.getActivity(context, 4, i,
					PendingIntent.FLAG_UPDATE_CURRENT);
			needChange = true;
		} else {
			n.defaults = 0;
			if (needChange) {
				Intent i = new Intent(context, YoukuPlayerApplication.instance.getCachingActivityClass());
				i.putExtra("go", "downloading");
				n.contentIntent = PendingIntent.getActivity(context, 4, i,
						PendingIntent.FLAG_UPDATE_CURRENT);
				needChange = false;
			}
		}
		YoukuPlayerApplication.savePreference(IDownload.KEY_LAST_NOTIFY_TASKID, info.taskId);
		nm.notify(IDownload.NOTIFY_ID, n);
	}

	private static boolean needChange = true;

	/**
	 * TODO 创建通知对象
	 */
	private static Notification createNotification(Context c) {
		Notification n = new Notification();
		Class<? extends Activity> cacheActivity = YoukuPlayerApplication.instance.getCachingActivityClass();
		Intent i = new Intent(c, cacheActivity);
		i.putExtra("go", "downloading");
		n.contentIntent = PendingIntent.getActivity(c, 4, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		n.contentView = new RemoteViews(c.getPackageName(), R.layout.notify);
		return n;
	}

	private void release() {
		if (wifiLock != null && wifiLock.isHeld())
			wifiLock.release();
		if (wakeLock != null && wakeLock.isHeld())
			wakeLock.release();
	}
}
