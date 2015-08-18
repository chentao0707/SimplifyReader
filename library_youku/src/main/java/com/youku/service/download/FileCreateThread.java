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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;

import com.baseproject.utils.Logger;
import com.youku.player.YoukuPlayerApplication;
import com.youku.player.ui.R;
import com.youku.player.util.PlayerUtil;
import com.youku.service.acc.AcceleraterServiceManager;
import com.youku.service.download.SDCardManager.SDCardInfo;

/**
 * FileCreateThread.创建下载视频文件
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2012-10-15 下午4:03:24
 */
public class FileCreateThread extends Thread {

	private static final String TAG = "Download_CreateRunnable";
	private static final String pageName = "缓存模块";
	private DownloadServiceManager download;
	// private Activity activity;
	/** 添加等待队列，防止重复添加 */
	private ArrayList<DownloadInfo> download_temp_infos;
	public static Map<String, String> tempCreateData;
	private int successCount = 0;
	private int failCount = 0;
	private boolean hasMaryPaths = false;
	private static boolean isRunning = false;

	public static boolean isRunning() {
		return isRunning;
	}

	public FileCreateThread(String videoId, String videoName) {
		download = DownloadServiceManager.getInstance();
		isRunning = true;
		if (download_temp_infos == null)
			download_temp_infos = new ArrayList<DownloadInfo>();
		if (tempCreateData == null)
			tempCreateData = new HashMap<String, String>();
		if (tempCreateData.containsKey(videoId)) {
			return;
		}
		DownloadInfo info;
		Logger.d(TAG, "title:" + videoName + "/vid:" + videoId);
		info = new DownloadInfo();
		info.videoid = videoId;
		info.title = videoName;
		info.format = DownloadUtils.getDownloadFormat();
		info.language = DownloadUtils.getDownloadLanguageName();
		info.savePath = download.getCurrentDownloadSDCardPath()
				+ YoukuPlayerApplication.getDownloadPath() + info.videoid + "/";
		download_temp_infos.add(info);
		tempCreateData.put(info.videoid, "");
	}

	public FileCreateThread(String[] vids, String[] titles) {
		download = DownloadServiceManager.getInstance();
		isRunning = true;
		if (download_temp_infos == null)
			download_temp_infos = new ArrayList<DownloadInfo>();
		if (tempCreateData == null)
			tempCreateData = new HashMap<String, String>();
		int format = DownloadUtils.getDownloadFormat();
		String language = DownloadUtils.getDownloadLanguageName();
		String path = download.getCurrentDownloadSDCardPath();
		DownloadInfo info;
		for (int i = 0, n = vids.length; i < n; i++) {
			Logger.d(TAG, "titles:" + titles[i] + "/vids:" + vids[i]);
			if (tempCreateData.containsKey(vids[i])) {
				continue;
			}
			info = new DownloadInfo();
			info.title = titles[i];
			info.videoid = vids[i];
			info.format = format;
			info.language = language;
			info.savePath = path + YoukuPlayerApplication.getDownloadPath() + info.videoid + "/";
			download_temp_infos.add(info);
			tempCreateData.put(info.videoid, "");
		}
	}

	private void over() {
		YoukuPlayerApplication.context.sendBroadcast(new Intent(
				IDownload.ACTION_CREATE_DOWNLOAD_ALL_READY));
		download_temp_infos.clear();
		isRunning = false;
	}

	@Override
	public void run() {
		for (DownloadInfo info : download_temp_infos) {
			if (download.existsDownloadInfo(info.videoid)) {
				if (download.isDownloadFinished(info.videoid)) {// 已下载完成
					PlayerUtil.showTips(R.string.download_exist_finished);
				} else {
					PlayerUtil.showTips(R.string.download_exist_not_finished);
				}
				continue;
			}
			long time = System.currentTimeMillis();
			info.createTime = time;
			info.taskId = String.valueOf(time).substring(5);// 用时间戳做ID
			if (init(info)) {
				Logger.d("DownloadFlow","init() success");
				download.addDownloadingInfo(info);
				switch (info.format) {
				case DownloadInfo.FORMAT_HD2:
					break;
				case DownloadInfo.FORMAT_MP4:
					break;
				default:
					break;
				}
				successCount++;
				YoukuPlayerApplication.context.sendBroadcast(new Intent(
						IDownload.ACTION_CREATE_DOWNLOAD_ONE_READY));
			} else {
				Logger.d("DownloadFlow","init() fail");
				failCount++;
				YoukuPlayerApplication.context.sendBroadcast(new Intent(
						IDownload.ACTION_CREATE_DOWNLOAD_ONE_FAILED));
				if (info.getExceptionId() == DownloadInfo.EXCEPTION_NO_SPACE) {// 没有空间，提示切换空间
					failCount = download_temp_infos.size() - successCount;
					ArrayList<SDCardInfo> card = SDCardManager
							.getExternalStorageDirectory();
					if (card != null && card.size() > 1) {
						hasMaryPaths = true;
						HandlerThread ht = new HandlerThread("handler_thread1");
						ht.start();
						new Handler(ht.getLooper()) {
							public void handleMessage(Message msg) {
/*								YoukuPlayerApplication.context
										.startActivity(new Intent(
												YoukuPlayerApplication.context,
												EmptyActivity.class)
												.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));*/
								Toast.makeText(YoukuPlayerApplication.context, "存储空间不足", Toast.LENGTH_SHORT).show();
							};

						}.sendEmptyMessageDelayed(0, 500L);

					}
					break;
				}
				continue;
			}
		}
		for (DownloadInfo info : download_temp_infos) {
			tempCreateData.remove(info.videoid);
		}
		showTips();
		over();
		HandlerThread ht = new HandlerThread("handler_thread2");
		ht.start();
		new Handler(ht.getLooper()) {
			public void handleMessage(Message msg) {
				Logger.d("DownloadFlow","FileCreateThread: create task to download");
				download.startNewTask();
			};

		}.sendEmptyMessageDelayed(0, 1000L);
		super.run();
	}

	private void showTips() {
		if (download_temp_infos.size() == successCount) {// 全部成功
			PlayerUtil.showTips(R.string.download_add_success);
			return;
		} else if (download_temp_infos.size() == 1) {// 单个添加失败
			if (hasMaryPaths == false) {
				String e = download_temp_infos.get(0).getExceptionInfo();
				if (e != null && e.length() != 0){
					PlayerUtil.showTips(e);
					Logger.e("DownloadFlow",e);
				}

					
			}
			return;
		}
		// 多个添加失败
		int differenceFailCount = 0;// 不同错误的数量
		int exceptionId = -1;
		for (DownloadInfo info : download_temp_infos) {
			if (info.getExceptionId() != 0) {
				if (info.getExceptionId() != exceptionId) {
					exceptionId = info.getExceptionId();
					differenceFailCount++;
				}
			}
		}

		if (differenceFailCount == 1) {// 只有一种错误原因的
			switch (exceptionId) {
			case DownloadInfo.EXCEPTION_NO_SDCARD:
			case DownloadInfo.EXCEPTION_NO_NETWORK:
				break;
			case DownloadInfo.EXCEPTION_NO_SPACE:
				if (hasMaryPaths == false) {
					String e = YoukuPlayerApplication.context.getResources().getString(
							R.string.download_many_fail_no_space);
					e = e.replace("S", String.valueOf(successCount));
					e = e.replace("F", String.valueOf(failCount));
					PlayerUtil.showTips(e);
				}
				break;
			case DownloadInfo.EXCEPTION_NO_COPYRIGHT:
			case DownloadInfo.EXCEPTION_NO_RESOURCES:
			case DownloadInfo.EXCEPTION_HTTP_NOT_FOUND:
				String e = YoukuPlayerApplication.context.getResources().getString(
						R.string.download_many_fail);
				e = e.replace("S", String.valueOf(successCount));
				e = e.replace("F", String.valueOf(failCount));
				PlayerUtil.showTips(e);
				break;
			case DownloadInfo.EXCEPTION_TIMEOUT:
				e = YoukuPlayerApplication.context.getResources().getString(
						R.string.download_many_fail_timeout);
				e = e.replace("S", String.valueOf(successCount));
				e = e.replace("F", String.valueOf(failCount));
				PlayerUtil.showTips(e);
				break;
			case DownloadInfo.EXCEPTION_WRITE_ERROR:
			case DownloadInfo.EXCEPTION_UNKNOWN_ERROR:
				e = YoukuPlayerApplication.context.getResources().getString(
						R.string.download_many_fail_unknown_error);
				e = e.replace("S", String.valueOf(successCount));
				e = e.replace("F", String.valueOf(failCount));
				PlayerUtil.showTips(e);
				break;
			default:
				break;
			}
		} else if (differenceFailCount > 1) {// 有两种不同错误以上
			String s = YoukuPlayerApplication.context.getResources().getString(
					R.string.download_many_fail_unknown_error);
			s = s.replace("S", String.valueOf(successCount));
			s = s.replace("F", String.valueOf(failCount));
			PlayerUtil.showTips(s);
		}
	}

	private boolean init(final DownloadInfo info) {
		info.setState(DownloadInfo.STATE_INIT);
		if (!DownloadUtils.getDownloadData(info)) {
			return false;
		}
		if (!DownloadUtils.getVideoInfo(info)) {
			return false;
		}
		if (hasEnoughSpace(info.size)) {
			new Thread() {
				public void run() {
					DownloadUtils.createVideoThumbnail(info.imgUrl,
							info.savePath);
				};
			}.start();
			DownloadUtils.makeM3U8File(info);
		} else {
			info.setExceptionId(DownloadInfo.EXCEPTION_NO_SPACE);
			return false;
		}
		if (!DownloadUtils.makeDownloadInfoFile(info)) {
			return false;
		}
		Logger.d("DownloadFlow","FileCreateThread: init() end: return true");
		info.downloadListener = new DownloadListenerImpl(YoukuPlayerApplication.context, info);
		return true;
	}

	/***
	 * 是否有足够的空间
	 * 
	 * @param needSpace
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean hasEnoughSpace(long needSpace) {
		String savePath = download.getCurrentDownloadSDCardPath();
		SDCardManager m = new SDCardManager(savePath);
		if (!m.exist())
			return false;
		// 下载中的视频还需要的空间
		long needsize = 0L;
		Map<String, DownloadInfo> data = download.getDownloadingData();
		Iterator<?> iter = data.entrySet().iterator(); // 获得map的Iterator
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadInfo info = (DownloadInfo) entry.getValue();
			if (info.savePath.contains(savePath)) {
				needsize += (info.size - info.downloadedSize);
			}
		}
		// 未使用的剩余空间-未完成的视频所需空间-新视频所需空间
		if (savePath.equals(SDCardManager.getDefauleSDCardPath())) {
			// 固定加入p2p所占空间大小100M
			if (AcceleraterServiceManager.isACCEnable()) {
				if ((m.getFreeSize() - needsize - needSpace) < 200 * 1024 * 1024) {
					return false;// SD卡空间不足
				}
			} else {
				if ((m.getFreeSize() - needsize - needSpace) < 100 * 1024 * 1024) {
					return false;// SD卡空间不足
				}
			}
		} else {
			if ((m.getFreeSize() - needsize - needSpace) < 100 * 1024 * 1024) {
				return false;// SD卡空间不足
			}
		}
		return true;
	}

}
