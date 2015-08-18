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

package com.youku.player.plugin;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.youku.player.base.GoplayException;
import com.youku.player.ui.interf.IMediaPlayerDelegate;

/**
 * Class PluginOverlay
 * 目前使用了framelayout解决了控制层和播放器级别的问题
 * 其实也可以考虑使用windowsmanager创建浮动窗口
 * 利用反射 @com.android.internal.policy.impl.Policy 解决窗口问题
 * 
 */
public abstract class PluginOverlay extends FrameLayout implements
		LoginObserver, MediaPlayerObserver {

	public IMediaPlayerDelegate mMediaPlayerDelegate;
	public boolean  pluginEnable = false;
	public PluginOverlay(Context context, IMediaPlayerDelegate mediaPlayerDelegate) {
		super(context);
		mMediaPlayerDelegate = mediaPlayerDelegate;
	}

	public PluginOverlay(Context context) {
	    super(context);
    }
    public PluginOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PluginOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	/**
   */
	public abstract void onUp();

	/**
   */
	public abstract void onDown();
	
	/**
	 * 清空顶踩信息
	 */
	public abstract void onClearUpDownFav();

	/**
   */
	public abstract void onFavor();

	/**
   */
	public abstract void onUnFavor();
	
	/**
	 * 播放新视频，需要重置一些layout和参数
	 */
	public abstract void newVideo();
	
	//声音调大
	public abstract void onVolumnUp();
	
	public abstract void onVolumnDown(); 
	
	/**
	 * 是否静音
	 * @param mute
	 */
	public abstract void onMute(boolean mute);
	
	/**
	 * Activity onStart触发
	 */
	public abstract void onStart();
	
	/**
	 * Activity onPause触发
	 */
	public abstract void onPause();
	
	/**
	 * 切换新视频
	 */
	public abstract void onVideoChange();
	
	/**
	 * 开始获取播放地址
	 */
	public abstract void onVideoInfoGetting();
	
	/**
	 * 获取播放地址成功
	 */
	public abstract void onVideoInfoGetted();
	/**
	 * 正片开始播放
	 */
	public abstract void onRealVideoStart();
	
	/**
	 * 播放无版权视频
	 * @param e
	 */
	public abstract void onPlayNoRightVideo(GoplayException e);
	
	/**
	 * 无版权视频时候播放相关视频
	 * @param e
	 */
	public abstract void onPlayReleateNoRightVideo();
	
	/**
	 * 正片开始播放
	 */
	public abstract void onADplaying();
	
	/**
	 * 获取播放地址失败
	 * @param needRetry 需要重试
	 */
	public abstract void onVideoInfoGetFail(boolean needRetry);
	
	/**
   */
	public void download() {
	}

	/**
   */
	public void loginSucc() {
	}

	/**
   */
	public void loginFail() {
	}

	/**
	 * 添加下载失败
	 */
	public void onDownloadSucc() {
		
	}
	
	/**
	 * 添加下载失败
	 * @param msg
	 */
	public void onDownloadFail(String msg) {
		
	}
	
	public void onPluginAdded() {
		
	}
	
	public abstract void setVisible(boolean visible);

	/**
	 * 从3g返回后设置
	 * 在onresume的时候会调用插件的back
	 * 例如：挂起后启动
	 */
	public abstract  void back();
	
	/**
	 * 增加网速通知回调
	 * @param speed
	 */
	public void onNetSpeedChange(int speed){
		
	}
	
	/**
	 * 订阅
	 */
	public void onSubscribe() {

	}

	/**
	 * 取消订阅
	 */
	public void onUnSubscribe() {

	}
	
	public void onRelease() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isShowing() {
		return false;
	}
	
	/**
	 * 重播回调
	 */
	public void onReplay() {

	}

}
