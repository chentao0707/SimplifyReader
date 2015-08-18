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

import java.util.HashMap;

/**
 * IDownload.视频缓存接口
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2012-10-15 下午4:03:24
 */
public interface IDownload {

	/** 配置文件路径 /youku/offlinedata/ */
//	public static String FILE_PATH = "/videocache/offlinedata/";
//	public static final String FILE_PATH = "/videocache/offlinedata/";

	/** 缩略图名字 */
	public static final String THUMBNAIL_NAME = "1.png";

	/** 配置文件名info */
	public static final String FILE_NAME = "info";

	/** SD卡发生插拔操作的广播动作 */
	public static final String ACTION_SDCARD_CHANGED = "com.youku.service.download.ACTION_SDCARD_CHANGED";

	/** SD卡路径切换后的广播动作 */
	public static final String ACTION_SDCARD_PATH_CHANGED = "com.youku.service.download.ACTION_SDCARD_PATH_CHANGED";

	/** 需要刷新页面的广播动作 */
	public static final String ACTION_THUMBNAIL_COMPLETE = "com.youku.service.download.ACTION_THUMBNAIL_COMPLETE";

	/** 创建下载文件：每当一个创建完毕的广播动作 */
	public static final String ACTION_CREATE_DOWNLOAD_ONE_READY = "com.youku.service.download.ACTION_CREATE_DOWNLOAD_ONE_READY";

	/** 创建下载文件：全部创建完毕的广播动作 */
	public static final String ACTION_CREATE_DOWNLOAD_ALL_READY = "com.youku.service.download.ACTION_CREATE_DOWNLOAD_ALL_READY";

	/** 创建下载文件：每当一个创建失败的广播动作 */
	public static final String ACTION_CREATE_DOWNLOAD_ONE_FAILED = "com.youku.service.download.ACTION_CREATE_DOWNLOAD_ONE_FAILED";

	/** 下载完成的广播动作 */
	public static final String ACTION_DOWNLOAD_FINISH = "com.youku.service.download.ACTION_DOWNLOAD_FINISH";

	/** 下载公用的notify_id */
	public static final int NOTIFY_ID = 2046;

	/** 键-最后的消息taskid */
	public static final String KEY_LAST_NOTIFY_TASKID = "download_last_notify_taskid";

	/** 是否需要奥刷新 */
	public static final String KEY_CREATE_DOWNLOAD_IS_NEED_REFRESH = "isNeedRefresh";

	/**
	 * 是否存在该缓存
	 */
	public boolean existsDownloadInfo(String videoId);

	/**
	 * 是否已下载完成
	 */
	public boolean isDownloadFinished(String videoId);

	/**
	 * 获得本地下载的视频的相关信息
	 */
	public DownloadInfo getDownloadInfo(String videoId);

	/**
	 * Returns 正在缓存的视频缓存列表
	 */
	public HashMap<String, DownloadInfo> getDownloadingData();

	/**
	 * 开始下载任务
	 */
	public void startDownload(String taskId);

	/**
	 * 暂停下载任务
	 */
	public void pauseDownload(String taskId);

	/**
	 * 单个删除视频缓存
	 */
	public boolean deleteDownloading(String taskId);

	/**
	 * 删除全部正在缓存的视频
	 */
	public boolean deleteAllDownloading();

	/**
	 * 重新获取数据
	 */
	public void refresh();

	/***
	 * 开始一个新的下载任务
	 */
	public void startNewTask();

	public void stopAllTask();

	/**
	 * 获得当前下载SD卡路径/mnt/sdcard
	 */
	public String getCurrentDownloadSDCardPath();

	public void setCurrentDownloadSDCardPath(String path);

	/**
	 * 能否在3G环境下下载
	 */
	public boolean canUse3GDownload();

	public void setCanUse3GDownload(boolean flag);

	public boolean canUseAcc();

	/** P2P 开关，-1获取失败，0关闭，1开启 */
	public void setP2p_switch(int value);

	public String getAccPort();

	public int getDownloadFormat();

	public void setDownloadFormat(int format);

	public int getDownloadLanguage();

	public void setDownloadLanguage(int language);

}
