/*
 * Copyright © 2012-2013 LiuZhongnan. All rights reserved.
 * 
 * Email:qq81595157@126.com
 * 
 * PROPRIETARY/CONFIDENTIAL.
 */

package com.youku.service.download;
import com.youku.service.download.ICallback;
import com.youku.service.download.DownloadInfo;
import java.util.Map;

/**
 * IDownloadService.下载服务接口
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2013-10-5 下午1:16:02
 */
interface IDownloadService{
	void registerCallback(ICallback callback);
	void unregister();
	void createDownload(in String videoId, in String videoName);
	void createDownloads(in String[] videoIds, in String[] videoNames);
	void down(in String taskId);	 //添加一个队列到下载服务中
	void pause(in String taskId);
	boolean delete(in String taskId);
	boolean deleteAll();
	void refresh();
	Map getDownloadingData();
	void startNewTask();
	void stopAllTask();
	String getCurrentDownloadSDCardPath();
	void setCurrentDownloadSDCardPath(in String path);
	int getDownloadFormat();
	void setDownloadFormat(in int format);
	int getDownloadLanguage();
	void setDownloadLanguage(in int language);
	boolean canUse3GDownload();
	void setCanUse3GDownload(in boolean flag);
	boolean canUseAcc();
	void setP2p_switch(in int value);
	String getAccPort();
	void setTimeStamp(in long path);
}