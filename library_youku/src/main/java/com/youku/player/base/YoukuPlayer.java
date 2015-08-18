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

package com.youku.player.base;

import android.util.Log;

import com.youku.player.YoukuPlayerBaseApplication;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.util.PlayerUtil;
import com.youku.service.download.DownloadInfo;
import com.youku.service.download.DownloadManager;

public class YoukuPlayer {

	public IMediaPlayerDelegate mMediaPlayerDelegate;
	YoukuBasePlayerActivity activity;

	public YoukuPlayer(YoukuBasePlayerActivity mYoukuBaseActivity) {
		super();
		activity = mYoukuBaseActivity;
		mMediaPlayerDelegate = mYoukuBaseActivity.getMediaPlayerDelegate();

	}

	public IMediaPlayerDelegate getmMediaPlayerDelegate() {
		return this.mMediaPlayerDelegate;
	}

	/**
	 * 通过vid播放视频
	 * 
	 * @param vid
	 */
	public void playVideo(final String vid) {
		Log.d("sgh","[YoukuPlayer] playVideo(final String vid)");
		mMediaPlayerDelegate.playVideo(vid);
	}

	/**
	 * 通过vid和playlist_id播放视频
	 * 
	 * @param vid
	 * @param playlistId
	 */
	public void playVideo(final String vid, final String playlistId) {
		Log.d("sgh","[YoukuPlayer] playVideo(final String vid, final String playlistId)");
		mMediaPlayerDelegate.playVideo(vid, playlistId);
	}

	/**
	 * 通过vid和视频的password播放加密视频
	 * @param vid
	 * @param password
	 */
	public void playVideoWithPassword(final String vid, final String password) {
		Log.d("sgh","[YoukuPlayer] playVideoWithPassword");
		mMediaPlayerDelegate.playVideoWithPassword(vid, password);
	}
	
	public void playLocalVideo(final String vid, String url, String videoTitle) {
		Log.d("sgh","[YoukuPlayer] playLocalVideo #1");		
		mMediaPlayerDelegate.playLocalVideo(vid,!PlayerUtil.useUplayer() ? url : PlayerUtil.getM3u8File(url),videoTitle);
	}
	public void playLocalVideo(String vid, String url, String title, int progress){
		Log.d("sgh","[YoukuPlayer] playLocalVideo #2");
		mMediaPlayerDelegate.playLocalVideo(vid, title, progress);
	}
	
	
	public void playLocalVideo(String local_vid){
		DownloadInfo downloadInfo = DownloadManager.getInstance().getDownloadInfo(local_vid);
		String savePath = downloadInfo.savePath;		
		playLocalVideo(local_vid, savePath + (YoukuPlayerBaseApplication.isHighEnd ? "/youku.m3u8" : "/1.3gp"), downloadInfo.title);
	}


}
