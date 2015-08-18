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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.youku.player.ui.R;
import com.youku.player.ui.interf.IMediaPlayerDelegate;
import com.youku.player.util.AnalyticsWrapper;

public class TudouEncryptDialog extends Dialog {

	private EditText mPassWord;
	private TextView mConfirmDialog;
	private TextView mCancleDialog;
	private OnPositiveClickListener mPositiveClickListener;

	public TudouEncryptDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public TudouEncryptDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.yp_tudou_encrypt_dialog);
		initViews();
		setListeners();
	}

	private void initViews() {
		mPassWord = (EditText) findViewById(R.id.password_edit);
		mConfirmDialog = (TextView) findViewById(R.id.tudou_dialog_confirm);
		mCancleDialog = (TextView) findViewById(R.id.tudou_dialog_cancel);
	}

	private void setListeners() {
		mConfirmDialog.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPositiveClickListener == null) {
					TudouEncryptDialog.this.dismiss();
					return;
				}
				AnalyticsWrapper.trackCustomEvent(getContext(),
						"加密视频密码输入框确认按钮点击", "视频播放页", null,
						IMediaPlayerDelegate.getUserID());
				mPositiveClickListener.onClick(mPassWord.getText().toString());
				TudouEncryptDialog.this.dismiss();
				mPassWord.setText("");
			}
		});

		mCancleDialog.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TudouEncryptDialog.this.dismiss();
				mPassWord.setText("");
			}
		});
	}

	public interface OnPositiveClickListener {

		public void onClick(String passWord);

	}

	public void setPositiveClickListener(OnPositiveClickListener listener) {

		this.mPositiveClickListener = listener;
	}
}
