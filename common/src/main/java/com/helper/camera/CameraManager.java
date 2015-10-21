/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helper.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager implements Camera.AutoFocusCallback {

    public static final int NO_REQUESTED_CAMERA = -1;

    public static final long AUTO_FOCUS_INTERVAL_MS = 2000L;

    private CameraConfigurationManager configManager;

    public CameraConfigurationManager getConfigManager() {
        return configManager;
    }

    private Camera mCamera;

    private boolean isInitialized;

    private boolean isPreviewing;

    private int requestedCameraId = NO_REQUESTED_CAMERA;

    private Camera.PreviewCallback mPreviewCallback;

    /**
     * Preview frames are delivered here, which we pass on to the registered
     * handler. Make sure to clear the handler so it will only receive one
     * message.
     */

    public CameraManager(Context context) {
        this.configManager = new CameraConfigurationManager(context);
    }

    /**
     * 打开摄像头
     * 
     * @param cameraId
     * @return
     */
    @SuppressLint("NewApi")
    private Camera open(int cameraId) {
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            return null;
        }
        boolean explicitRequest = cameraId >= 0;
        if (!explicitRequest) {
            int index = 0;
            while (index < numCameras) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(index, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    break;
                }
                index++;
            }

            cameraId = index;
        }
        Camera camera;
        if (cameraId < numCameras) {
            camera = Camera.open(cameraId);
        } else {
            if (explicitRequest) {
                camera = null;
            } else {
                camera = Camera.open(0);
            }
        }
        return camera;
    }

    public synchronized boolean isOpened() {
        return mCamera != null;
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview
     *            frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    public synchronized void openDriver(SurfaceHolder holder) throws IOException {
        if (isOpened()) {
            return;
        } else {
            mCamera = open(requestedCameraId);
        }
        if (mCamera == null) {
            // if open camera fail ,return
            return;
        }
        mCamera.setPreviewDisplay(holder);
        if (!isInitialized) {
            isInitialized = true;
            configManager.initFromCameraParameters(mCamera);
        }
        Camera.Parameters parameters = mCamera.getParameters();
        String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save
        try {
            configManager.setDesiredCameraParameters(mCamera);
        } catch (RuntimeException re) {
            if (parametersFlattened != null) {
                parameters = mCamera.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    mCamera.setParameters(parameters);
                    configManager.setDesiredCameraParameters(mCamera);
                } catch (RuntimeException re2) {
                }
            }
        }
        if (mPreviewCallback != null) {
            mCamera.setPreviewCallback(mPreviewCallback);
        }
    }

    public void setPreviewCallback(Camera.PreviewCallback callback) {
        mPreviewCallback = callback;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            // Make sure to clear these each time we close the camera, so that
            // any scanning rect
            // requested by intent is forgotten.
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
        Camera theCamera = mCamera;
        if (theCamera != null && !isPreviewing) {
            theCamera.startPreview();
            isPreviewing = true;
            startFocus();
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        stopFocus();
        if (mCamera != null && isPreviewing) {
            mCamera.stopPreview();
            isPreviewing = false;
        }
    }

    /**
     * Convenience method for
     * {@link com.google.zxing.client.android.CaptureActivity}
     *
     * @param newSetting if {@code true}, light should be turned on if currently
     *            off. And vice versa.
     */
    public synchronized void setTorch(boolean newSetting) {
        if (newSetting != configManager.getTorchState(mCamera)) {
            if (mCamera != null) {
                stopFocus();
                configManager.setTorch(mCamera, newSetting);
                startFocus();
            }
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data
     * will arrive as byte[] in the message.obj field, with width and height
     * encoded as message.arg1 and message.arg2, respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public synchronized void requestPreviewFrame() {
        Camera theCamera = mCamera;
        if (theCamera != null && isPreviewing) {
            if (mPreviewCallback != null) {
                theCamera.setOneShotPreviewCallback(mPreviewCallback);
            }
        }
    }

    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 5 * resolution / 8; // Target 5/8 of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview
     * frame, not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview
     *         size
     */
    // public synchronized Rect getFramingRectInPreview() {
    // if (mFramingRectInPreview == null) {
    // Rect framingRect = getFramingRect();
    // if (framingRect == null) {
    // return null;
    // }
    // Rect rect = new Rect(framingRect);
    // Point cameraResolution = configManager.getCameraResolution();
    // Point screenResolution = configManager.getScreenResolution();
    // if (cameraResolution == null || screenResolution == null) {
    // // Called early, before init even finished
    // return null;
    // }
    // rect.left = rect.left * cameraResolution.x / screenResolution.x;
    // rect.right = rect.right * cameraResolution.x / screenResolution.x;
    // rect.top = rect.top * cameraResolution.y / screenResolution.y;
    // rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
    // mFramingRectInPreview = rect;
    // }
    // return mFramingRectInPreview;
    // }

    /**
     * Allows third party apps to specify the camera ID, rather than determine
     * it automatically based on available cameras and their orientation.
     *
     * @param cameraId camera ID of the camera to use. A negative value means
     *            "no preference".
     */
    public synchronized void setManualCameraId(int cameraId) {
        requestedCameraId = cameraId;
    }

    /**
     * 获取相机分辨率
     * 
     * @return
     */
    public Point getCameraResolution() {
        return configManager.getCameraResolution();
    }

    public Size getPreviewSize() {
        if (null != mCamera) {
            return mCamera.getParameters().getPreviewSize();
        }
        return null;
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera theCamera) {
        if (!success) {
            mCamera.autoFocus(CameraManager.this);
        }
    }

    private Timer mAutoFocusTimer;

    private synchronized void startFocus() {
        if (mAutoFocusTimer != null) {
            mAutoFocusTimer.cancel();
            mAutoFocusTimer = null;
        }
        mAutoFocusTimer = new Timer();
        mAutoFocusTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                mCamera.autoFocus(CameraManager.this);
            }
        }, 0, AUTO_FOCUS_INTERVAL_MS);
    }

    private synchronized void stopFocus() {
        try {
            mCamera.cancelAutoFocus();
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
        if (mAutoFocusTimer != null) {
            mAutoFocusTimer.cancel();
            mAutoFocusTimer = null;
        }
    }

}
