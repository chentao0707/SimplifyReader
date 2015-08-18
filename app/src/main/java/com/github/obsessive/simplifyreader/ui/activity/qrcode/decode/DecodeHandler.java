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

package com.github.obsessive.simplifyreader.ui.activity.qrcode.decode;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.github.obsessive.library.utils.TLog;
import com.github.obsessive.simplifyreader.ui.activity.qrcode.CaptureActivity;
import com.github.obsessive.simplifyreader.ui.activity.qrcode.utils.Constants;
import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.ByteArrayOutputStream;

public final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final CaptureActivity activity;
    private boolean running = true;
    private DecodeUtils mDecodeUtils = null;
    private int mDecodeMode = DecodeUtils.DECODE_MODE_ZXING;

    DecodeHandler(CaptureActivity activity) {
        this.activity = activity;
        mDecodeUtils = new DecodeUtils(DecodeUtils.DECODE_DATA_MODE_ALL);
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case Constants.ID_DECODE:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case Constants.ID_QUIT:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        long start = System.currentTimeMillis();

        Camera.Size size = activity.getCameraManager().getPreviewSize();
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < size.height; y++) {
            for (int x = 0; x < size.width; x++)
                rotatedData[x * size.height + size.height - y - 1] = data[x + y * size.width];
        }

        int tmp = size.width;
        size.width = size.height;
        size.height = tmp;

        String resultStr = null;
        Rect cropRect = activity.getCropRect();
        if (null == cropRect) {
            activity.initCrop();
        }
        cropRect = activity.getCropRect();
        TLog.d(TAG, cropRect.toString());

        mDecodeUtils.setDataMode(activity.getDataMode());

        String zbarStr = mDecodeUtils.decodeWithZbar(rotatedData, size.width, size.height, cropRect);
        String zxingStr = mDecodeUtils.decodeWithZxing(rotatedData, size.width, size.height, cropRect);

        if (!TextUtils.isEmpty(zbarStr)) {
            mDecodeMode = DecodeUtils.DECODE_MODE_ZBAR;
            resultStr = zbarStr;
        } else if (!TextUtils.isEmpty(zxingStr)) {
            mDecodeMode = DecodeUtils.DECODE_MODE_ZXING;
            resultStr = zxingStr;
        }

        Handler handler = activity.getHandler();
        if (!TextUtils.isEmpty(resultStr)) {
            long end = System.currentTimeMillis();
            if (handler != null) {
                Message message = Message.obtain(handler, Constants.ID_DECODE_SUCCESS, resultStr);
                Bundle bundle = new Bundle();
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(rotatedData, size.width, size.height,
                        cropRect.left, cropRect.top,
                        cropRect.width(), cropRect.height(), false);

                bundle.putInt(DecodeThread.DECODE_MODE, mDecodeMode);
                bundle.putString(DecodeThread.DECODE_TIME, (end - start) + "ms");

                bundleThumbnail(source, bundle);
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler, Constants.ID_DECODE_FAILED);
                message.sendToTarget();
            }
        }
    }

    /**
     * create thumbnail
     *
     * @param source
     * @param bundle
     */

    private void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(DecodeThread.BARCODE_BITMAP, out.toByteArray());
    }
}
