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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Author:  Tau.Chen
 * Email:   1076559197@qq.com | tauchen1990@gmail.com
 * Date:    15/7/23
 * Description:
 */
public class DecodeUtils {

    public static final int DECODE_MODE_ZBAR = 10001;
    public static final int DECODE_MODE_ZXING = 10002;

    public static final int DECODE_DATA_MODE_ALL = 10003;
    public static final int DECODE_DATA_MODE_QRCODE = 10004;
    public static final int DECODE_DATA_MODE_BARCODE = 10005;

    private int mDataMode;
    private ImageScanner mImageScanner;

    static {
        System.loadLibrary("iconv");
    }

    public DecodeUtils(int dataMode) {
        mImageScanner = new ImageScanner();
        mImageScanner.setConfig(0, Config.X_DENSITY, 3);
        mImageScanner.setConfig(0, Config.Y_DENSITY, 3);
        mDataMode = (dataMode != 0) ? dataMode : DECODE_DATA_MODE_ALL;
    }

    public String decodeWithZbar(byte[] data, int width, int height, Rect crop) {
        changeZBarDecodeDataMode();

        Image barcode = new Image(width, height, "Y800");
        barcode.setData(data);
        if (null != crop) {
            barcode.setCrop(crop.left, crop.top, crop.width(), crop.height());
        }

        int result = mImageScanner.scanImage(barcode);
        String resultStr = null;

        if (result != 0) {
            SymbolSet syms = mImageScanner.getResults();
            for (Symbol sym : syms) {
                resultStr = sym.getData();
            }
        }

        return resultStr;
    }

    public String decodeWithZxing(byte[] data, int width, int height, Rect crop) {
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(changeZXingDecodeDataMode());

        Result rawResult = null;
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height,
                crop.left, crop.top, crop.width(), crop.height(), false);

        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        return rawResult != null ? rawResult.getText() : null;
    }

    public String decodeWithZbar(Bitmap bitmap) {
        changeZBarDecodeDataMode();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Image barcode = new Image(width, height, "Y800");

        int size = width * height;
        int[] pixels = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        byte[] pixelsData = new byte[size];
        for (int i = 0; i < size; i++) {
            pixelsData[i] = (byte) pixels[i];
        }

        barcode.setData(pixelsData);

        int result = mImageScanner.scanImage(barcode);
        String resultStr = null;

        if (result != 0) {
            SymbolSet syms = mImageScanner.getResults();
            for (Symbol sym : syms) {
                resultStr = sym.getData();
            }
        }

        return resultStr;
    }

    public String decodeWithZxing(Bitmap bitmap) {
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(changeZXingDecodeDataMode());

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        Result rawResult = null;
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

        if (source != null) {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(binaryBitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        return rawResult != null ? rawResult.getText() : null;
    }

    private Map<DecodeHintType, Object> changeZXingDecodeDataMode() {
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = new ArrayList<BarcodeFormat>();

        switch (mDataMode) {
            case DECODE_DATA_MODE_ALL:
                decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
                decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
                break;

            case DECODE_DATA_MODE_QRCODE:
                decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
                break;

            case DECODE_DATA_MODE_BARCODE:
                decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
                break;
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        return hints;
    }

    private void changeZBarDecodeDataMode() {
        switch (mDataMode) {
            case DECODE_DATA_MODE_ALL:
                mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 1);
                break;

            case DECODE_DATA_MODE_QRCODE:
                mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
                // bar code
                mImageScanner.setConfig(Symbol.I25, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.CODABAR, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.CODE128, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.CODE39, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.CODE93, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.DATABAR, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.DATABAR_EXP, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.EAN13, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.EAN8, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.ISBN10, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.ISBN13, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.UPCA, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.UPCE, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.PARTIAL, Config.ENABLE, 0);
                // qr code
                mImageScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.PDF417, Config.ENABLE, 1);

                break;

            case DECODE_DATA_MODE_BARCODE:
                mImageScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
                // bar code
                mImageScanner.setConfig(Symbol.I25, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.CODABAR, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.CODE128, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.CODE39, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.CODE93, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.DATABAR, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.DATABAR_EXP, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.EAN13, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.EAN8, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.ISBN10, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.ISBN13, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.UPCA, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.UPCE, Config.ENABLE, 1);
                mImageScanner.setConfig(Symbol.PARTIAL, Config.ENABLE, 1);
                // qr code
                mImageScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 0);
                mImageScanner.setConfig(Symbol.PDF417, Config.ENABLE, 0);

                break;
        }
    }

    public int getDataMode() {
        return mDataMode;
    }

    public void setDataMode(int dataMode) {
        this.mDataMode = dataMode;
    }
}
