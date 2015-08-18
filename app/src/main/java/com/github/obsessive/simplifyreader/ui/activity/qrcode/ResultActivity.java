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

package com.github.obsessive.simplifyreader.ui.activity.qrcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.obsessive.library.eventbus.EventCenter;
import com.github.obsessive.library.netstatus.NetUtils;
import com.github.obsessive.library.utils.CommonUtils;
import com.github.obsessive.simplifyreader.R;
import com.github.obsessive.simplifyreader.ui.activity.base.BaseSwipeBackActivity;
import com.github.obsessive.simplifyreader.ui.activity.qrcode.decode.DecodeThread;
import com.github.obsessive.simplifyreader.ui.activity.qrcode.decode.DecodeUtils;

import butterknife.InjectView;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    15/7/24
 * Description:
 */
public class ResultActivity extends BaseSwipeBackActivity {

    public static final String BUNDLE_KEY_SCAN_RESULT = "BUNDLE_KEY_SCAN_RESULT";

    @InjectView(R.id.result_image)
    ImageView resultImage;
    @InjectView(R.id.result_type)
    TextView resultType;
    @InjectView(R.id.result_content)
    TextView resultContent;

    private Bitmap mBitmap;
    private int mDecodeMode;
    private String mResultStr;
    private String mDecodeTime;

    @Override
    protected void getBundleExtras(Bundle extras) {
        if (extras != null) {
            byte[] compressedBitmap = extras.getByteArray(DecodeThread.BARCODE_BITMAP);
            if (compressedBitmap != null) {
                mBitmap = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                mBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
            }

            mResultStr = extras.getString(BUNDLE_KEY_SCAN_RESULT);
            mDecodeMode = extras.getInt(DecodeThread.DECODE_MODE);
            mDecodeTime = extras.getString(DecodeThread.DECODE_TIME);
        }
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.activity_result;
    }

    @Override
    protected void onEventComming(EventCenter eventCenter) {

    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected void initViewsAndEvents() {
        setTitle("扫描结果");

        StringBuilder sb = new StringBuilder();
        sb.append("扫描方式:\t\t");
        if (mDecodeMode == DecodeUtils.DECODE_MODE_ZBAR) {
            sb.append("ZBar扫描");
        } else if (mDecodeMode == DecodeUtils.DECODE_MODE_ZXING) {
            sb.append("ZXing扫描");
        }

        if (!CommonUtils.isEmpty(mDecodeTime)) {
            sb.append("\n\n扫描时间:\t\t");
            sb.append(mDecodeTime);
        }
        sb.append("\n\n扫描结果:");

        resultType.setText(sb.toString());
        resultContent.setText(mResultStr);

        if (null != mBitmap) {
            resultImage.setImageBitmap(mBitmap);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    @Override
    protected void onNetworkConnected(NetUtils.NetType type) {

    }

    @Override
    protected void onNetworkDisConnected() {

    }

    @Override
    protected boolean isApplyStatusBarTranslucency() {
        return true;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    protected boolean toggleOverridePendingTransition() {
        return true;
    }

    @Override
    protected TransitionMode getOverridePendingTransitionMode() {
        return TransitionMode.RIGHT;
    }

    @Override
    protected boolean isApplyKitKatTranslucency() {
        return true;
    }
}
