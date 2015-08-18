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

package com.youku.player.util;


import android.app.Activity;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;
/**
 * 播放器横竖屏翻转 传感器 代替系统sensor
 * @author hail_lelouch
 *
 */
public class DeviceOrientationHelper extends OrientationEventListener{
	
	private OrientationChangeCallback callback;
	private DeviceOrientation localOrientation = DeviceOrientation.UNKONWN;
	
	//判断是否为用户点击放大缩小按钮   竖屏点击放大时判断
	public boolean fromUser = false;
	
	private boolean fromComplete = false;
	
	//设备方向
	public static enum DeviceOrientation{
		
		                 
		
		
		UNKONWN,         
		
		
		
		              					    //		   _______
											//        | _____ |
											//        ||     ||
		UPRIGHT,							//        ||_____||
											//        |       |
											//        |       |
											//        |_______|    
		
											
		
		                                    //	     __________________
											//		｜             __  |
		LEFTONTOP, 						    //		｜            |  | |
											//		｜            |__| |
											//		｜_________________|
		
		
		
											//		   _______
											//        |       |
											//        |       |
		BOTTOMUP,  							//        | _____ |
											//        ||     ||
											//        ||_____||
											//        |_______| 
       
		              
		
											//		 __________________
											//		｜ __              |
		RIGHTONTOP							//		｜|  |             |
											//		｜|__|             |
											//		｜_________________|
		
		
		
	}
	
	public DeviceOrientationHelper(Activity ctxt, OrientationChangeCallback callback){
		
		super(ctxt, SensorManager.SENSOR_DELAY_NORMAL);
		this.callback = callback;
		
	}

	private void initLocalOrientation(int orientation) {

		if ((orientation >= 0 && orientation <= 30) || (orientation >= 330 && orientation <= 360)) {

			localOrientation = DeviceOrientation.UPRIGHT;

		} else if ((orientation >= 60) && orientation <= 120) {

			localOrientation = DeviceOrientation.LEFTONTOP;

		} else if (orientation >= 150 && orientation <= 210) {

			localOrientation = DeviceOrientation.BOTTOMUP;

		} else if (orientation >= 240 && orientation <= 300) {

			localOrientation = DeviceOrientation.RIGHTONTOP;

		}
	}

	@Override
	public void onOrientationChanged(int orientation) {
		
		if (localOrientation == DeviceOrientation.UNKONWN) {
			initLocalOrientation(orientation);
		}

		if((orientation >= 0 && orientation <= 30) || (orientation >= 330 && orientation <= 360)){
			
			if(!fromUser && localOrientation != DeviceOrientation.UPRIGHT && (localOrientation == DeviceOrientation.LEFTONTOP || localOrientation == DeviceOrientation.RIGHTONTOP) && !fromComplete){
				if(callback != null) {
					callback.land2Port();
				}
			}else if(fromUser){
				fromUser = false;
			}else if(fromComplete){
				if(callback != null) {
					callback.onFullScreenPlayComplete();
					fromComplete = false;
				}
			}
			localOrientation = DeviceOrientation.UPRIGHT;
			
		}else if((orientation >= 60) && orientation <= 120){
			
			if(localOrientation != DeviceOrientation.LEFTONTOP){
				if(callback != null) {
					callback.reverseLand();
				}
			}
			
			localOrientation = DeviceOrientation.LEFTONTOP;
			
		}else if(orientation >= 150 && orientation <= 210){
			if(localOrientation != DeviceOrientation.BOTTOMUP) {
				if(callback != null) {
					callback.reversePort();
				}
			}
			
			localOrientation = DeviceOrientation.BOTTOMUP;
			
		}else if(orientation >= 240 && orientation <= 300){
			
			if(!fromUser && localOrientation != DeviceOrientation.RIGHTONTOP && (localOrientation == DeviceOrientation.UPRIGHT || localOrientation == DeviceOrientation.BOTTOMUP)){
				if(callback != null) {
					callback.port2Land();
				}
			} else if(fromUser) {
				fromUser = false;
			}
			
			localOrientation = DeviceOrientation.RIGHTONTOP;
			
		}
		
	}
	
	public interface OrientationChangeCallback{
		 
		public void land2Port();
		
		public void port2Land();
		
		//点击放大按钮后 旋转180%
		public void  reverseLand();
		
		public void reversePort();
		
		public void onFullScreenPlayComplete();
		
	 }
	
	public void enableListener(){
		
		if(this.canDetectOrientation())
			this.enable();
		
	}
	
	public void disableListener(){
		
		this.disable();
		localOrientation = DeviceOrientation.UNKONWN;
		
	}
	
	public void isFromUser(){
		fromUser = true;
	}
	
	public void isFromComplete(){
		fromComplete = true;
	}
	
	public DeviceOrientation getLocalOrientation(){
		
		return localOrientation;
		
	}

	public void setCallback(OrientationChangeCallback callback) {
		this.callback = callback;
	}
	
}
