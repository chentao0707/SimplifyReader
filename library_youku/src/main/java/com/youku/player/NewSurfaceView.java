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

package com.youku.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

public class NewSurfaceView extends SurfaceView{

	private int mForceHeight = 0;
	private int mForceWidth = 0;
	
	public NewSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NewSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NewSurfaceView(Context context) {
		super(context);
	}

	public void setDimensions(int w, int h) {
		this.mForceHeight = h;
		this.mForceWidth = w;
		invalidate();
	}
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		if(mForceHeight == 0)
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		else
			setMeasuredDimension(mForceWidth, mForceHeight);
	}
	
	public void recreateSurfaceHolder() {
		setVisibility(View.INVISIBLE);
		setVisibility(View.VISIBLE);
	}
}
