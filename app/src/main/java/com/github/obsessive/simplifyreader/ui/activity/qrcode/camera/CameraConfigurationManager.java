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

package com.github.obsessive.simplifyreader.ui.activity.qrcode.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 */
public final class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";

    private final Context context;
    private Point screenResolution;
    private Point cameraResolution;

    CameraConfigurationManager(Context context) {
        this.context = context;
    }

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point theScreenResolution = new Point();
        theScreenResolution = getDisplaySize(display);

        screenResolution = theScreenResolution;
        Log.i(TAG, "Screen resolution: " + screenResolution);

        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = screenResolution.x;
        screenResolutionForCamera.y = screenResolution.y;

        if (screenResolution.x < screenResolution.y) {
            screenResolutionForCamera.x = screenResolution.y;
            screenResolutionForCamera.y = screenResolution.x;
        }

        cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolution);
        Log.i(TAG, "Camera resolution x: " + cameraResolution.x);
        Log.i(TAG, "Camera resolution y: " + cameraResolution.y);
    }


    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private Point getDisplaySize(final Display display) {
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }

    void setDesiredCameraParameters(Camera camera, boolean safeMode) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters == null) {
            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        initializeTorch(parameters, safeMode);

        CameraConfigurationUtils.setFocus(
                parameters,
                true,
                true,
                safeMode);

        if (!safeMode) {
            CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            CameraConfigurationUtils.setVideoStabilization(parameters);
            CameraConfigurationUtils.setFocusArea(parameters);
            CameraConfigurationUtils.setMetering(parameters);
        }

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        Log.i(TAG, "Final camera parameters: " + parameters.flatten());

        camera.setParameters(parameters);
        Camera.Parameters afterParameters = camera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize != null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
            Log.w(TAG, "Camera said it supported preview size " + cameraResolution.x + 'x' + cameraResolution.y +
                    ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
            cameraResolution.x = afterSize.width;
            cameraResolution.y = afterSize.height;
        }

        camera.setDisplayOrientation(90);
    }

    Point getCameraResolution() {
        return cameraResolution;
    }

    boolean getTorchState(Camera camera) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters != null) {
                String flashMode = parameters.getFlashMode();
                return flashMode != null &&
                        (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) ||
                                Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
            }
        }
        return false;
    }

    void setTorch(Camera camera, boolean newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        doSetTorch(parameters, newSetting, false);
        camera.setParameters(parameters);
    }

    private void initializeTorch(Camera.Parameters parameters, boolean safeMode) {
        doSetTorch(parameters, false, safeMode);
    }

    private void doSetTorch(Camera.Parameters parameters, boolean newSetting, boolean safeMode) {
        CameraConfigurationUtils.setTorch(parameters, newSetting);
        if (!safeMode) {
            CameraConfigurationUtils.setBestExposure(parameters, newSetting);
        }
    }

}
