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

package com.github.obsessive.simplifyreader.player;

public class MusicPlayState {

	public static final int MPS_LIST_EMPTY = -1; // 播放列表为空
	
	public static final int MPS_LIST_FULL = 0; // 播放列表有数据

	public static final int MPS_PREPARED = 1; // 准备就绪

	public static final int MPS_PLAYING = 2; // 播放中

	public static final int MPS_PAUSE = 3; // 暂停
	
	public static final int MPS_STOP = 4; // 停止

	public static final String ACTION_MUSIC_PLAY = "ACTION_MUSIC_PLAY"; // 开始播放
	public static final String ACTION_MUSIC_REPLAY = "ACTION_MUSIC_REPLAY"; // 重新播放
	public static final String ACTION_MUSIC_PAUSE = "ACTION_MUSIC_PAUSE"; // 暂停播放
	public static final String ACTION_MUSIC_STOP = "ACTION_MUSIC_STOP"; // 停止播放
	public static final String ACTION_MUSIC_NEXT = "ACTION_MUSIC_NEXT"; // 播放下一曲
	public static final String ACTION_MUSIC_PREV = "ACTION_MUSIC_PREV"; // 播放上一曲
	public static final String ACTION_SEEK_TO = "ACTION_SEEK_TO"; // 调节进度
	public static final String ACTION_EXIT = "ACTION_EXIT"; // 调节进度

}