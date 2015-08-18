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

/**
 * OnCreateDownloadListener.创建下载文件情况监听
 * 
 * @author 刘仲男 qq81595157@126.com
 * @version v3.5
 * @created time 2013-10-17 下午1:16:02
 */
public abstract class OnCreateDownloadListener {

	/** 当每一个下载已准备的时候 */
	public void onOneReady() {
	}

	/** 当每一个下载失败 */
	public void onOneFailed() {
	}

	/**
	 * 当全部下载已准备的时候
	 * 
	 * @param isNeedRefresh
	 *            是否需要刷新数据
	 */
	public abstract void onfinish(boolean isNeedRefresh);

}
