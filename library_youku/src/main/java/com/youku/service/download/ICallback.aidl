/*
 * Copyright © 2012-2013 LiuZhongnan. All rights reserved.
 * 
 * Email:qq81595157@126.com
 * 
 * PROPRIETARY/CONFIDENTIAL.
 */

package com.youku.service.download;
import com.youku.service.download.DownloadInfo;

/**
 * ICallback.下载状态改变的回掉
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2013-10-5 下午1:16:02
 */
interface ICallback{
	void onChanged(in DownloadInfo info);
	void onFinish(in DownloadInfo info);
	void refresh();
}