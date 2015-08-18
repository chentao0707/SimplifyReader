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

package com.youku.player.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class YoukuImageView extends ImageView {

	public YoukuImageView(Context context) {
		super(context);
	}

	public YoukuImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// setMeasuredDimension(measuredWidth, measuredHeight)
		int w = MeasureSpec.getSize(widthMeasureSpec);
		int h = (int) (w * 9 / 16);
		// h = MeasureSpec.makeMeasureSpec((int) size, MeasureSpec.EXACTLY);
		// h = MeasureSpec.getSize(h);
		setMeasuredDimension(w, h);
		// Logger.d("Youku",
		// "widthMeasureSpec = "+MeasureSpec.getSize(widthMeasureSpec) +
		// " heightMeasureSpec = "+h);
		// super.onMeasure(widthMeasureSpec, (int) (widthMeasureSpec*size));
	}
}
