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

package com.youku.player.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.baseproject.image.ImageResizer;
import com.baseproject.image.ImageWorker;

public class YoukuFragment extends Fragment {
	
	private ImageResizer mImageWorker;

	public YoukuFragment() {
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		BaseActivity base = (BaseActivity) getActivity();
		mImageWorker = new ImageResizer(getActivity());//(ImageResizer) ImageWorker.getInstance(context);
	}

	public ImageResizer getImageWorker() {
		return mImageWorker;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
}
