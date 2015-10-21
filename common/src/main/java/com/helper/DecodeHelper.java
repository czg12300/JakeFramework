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

package com.helper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public class DecodeHelper implements Handler.Callback {

    public static final String BARCODE_BITMAP = "barcode_bitmap";

    public static final String BARCODE_RESULT_TEXT = "barcode_result_text";

    public static final String BARCODE_SCALED_FACTOR = "barcode_scaled_factor";

    public static final int BARCODE_MODE = 0X100;

    public static final int QRCODE_MODE = 0X200;

    public static final int ALL_MODE = 0X300;

    public static final int MSG_DECODE = 0;

    public static final int MSG_QUIT = 1;

    private Handler mHandler;

    private CaptureHelper mCaptureHelper;

    private HandlerThread mHandlerThread;

    private MultiFormatReader mMultiFormatReader;

    public DecodeHelper(CaptureHelper helper, ResultPointCallback callback) {
        this(helper, callback, ALL_MODE);
    }

    public DecodeHelper(CaptureHelper helper, ResultPointCallback callback, int decodeMode) {
        mCaptureHelper = helper;
        mMultiFormatReader = new MultiFormatReader();
        mHandlerThread = new HandlerThread("decode worker:" + DecodeHelper.class.getSimpleName());
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
        mMultiFormatReader.setHints(getHints(callback, decodeMode));
    }


    private Map<DecodeHintType, Object> getHints(ResultPointCallback resultPointCallback,
                                                 int decodeMode) {
        Map<DecodeHintType, Object> hints = new EnumMap<DecodeHintType, Object>(
                DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = new ArrayList<BarcodeFormat>();
        decodeFormats.addAll(EnumSet.of(BarcodeFormat.AZTEC));
        decodeFormats.addAll(EnumSet.of(BarcodeFormat.PDF_417));
        switch (decodeMode) {
            case BARCODE_MODE:
                decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
                break;

            case QRCODE_MODE:
                decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
                break;

            case ALL_MODE:
                decodeFormats.addAll(DecodeFormatManager.getBarCodeFormats());
                decodeFormats.addAll(DecodeFormatManager.getQrCodeFormats());
                break;

            default:
                break;
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
        return hints;
    }

    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_DECODE:
                decode((byte[]) msg.obj, msg.arg1, msg.arg2);
                break;
            case MSG_QUIT:
                mHandlerThread.quit();
                break;
        }
        return true;
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it
     * took. For efficiency, reuse the same reader objects from one decode to
     * the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        Size size = mCaptureHelper.getCameraManager().getPreviewSize();

        // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < size.height; y++) {
            for (int x = 0; x < size.width; x++)
                rotatedData[x * size.height + size.height - y - 1] = data[x + y * size.width];
        }

        // 宽高也要调整
        int tmp = size.width;
        size.width = size.height;
        size.height = tmp;

        Result rawResult = null;
        PlanarYUVLuminanceSource source = buildLuminanceSource(rotatedData, size.width,
                size.height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = mMultiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                mMultiFormatReader.reset();
            }
        }

        Handler helperHander = mCaptureHelper.getHandler();
        if (rawResult != null) {
            if (helperHander != null) {
                Message message = Message.obtain(helperHander, CaptureHelper.MSG_DECODE_SUCCEEDED,
                        rawResult);
                Bundle bundle = new Bundle();
                bundleThumbnail(source, bundle);
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (helperHander != null) {
                Message message = Message.obtain(helperHander, CaptureHelper.MSG_DECODE_FAILED);
                message.sendToTarget();
            }
        }

    }

    private static void bundleThumbnail(PlanarYUVLuminanceSource source, Bundle bundle) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height,
                Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        bundle.putByteArray(BARCODE_BITMAP, out.toByteArray());
        bundle.putFloat(BARCODE_SCALED_FACTOR, (float) width / source.getWidth());
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on
     * the format of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = mCaptureHelper.getScanFrameRect();
        if (rect == null) {
            return null;
        }
        if (rect.width() > width) {
            rect.set(rect.left, rect.top, rect.left + width, rect.bottom);
        }
        if (rect.height() > height) {
            rect.set(rect.left, rect.top, rect.right, rect.top + height);
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(),
                rect.height(), false);
    }

    static class DecodeFormatManager {

        // 1D解码
        private static final Set<BarcodeFormat> PRODUCT_FORMATS;

        private static final Set<BarcodeFormat> INDUSTRIAL_FORMATS;

        private static final Set<BarcodeFormat> ONE_D_FORMATS;

        // 二维码解码
        private static final Set<BarcodeFormat> QR_CODE_FORMATS;

        static {
            PRODUCT_FORMATS = EnumSet.of(BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
                    BarcodeFormat.EAN_13, BarcodeFormat.EAN_8, BarcodeFormat.RSS_14,
                    BarcodeFormat.RSS_EXPANDED);
            INDUSTRIAL_FORMATS = EnumSet.of(BarcodeFormat.CODE_39, BarcodeFormat.CODE_93,
                    BarcodeFormat.CODE_128, BarcodeFormat.ITF, BarcodeFormat.CODABAR);
            ONE_D_FORMATS = EnumSet.copyOf(PRODUCT_FORMATS);
            ONE_D_FORMATS.addAll(INDUSTRIAL_FORMATS);

            QR_CODE_FORMATS = EnumSet.of(BarcodeFormat.QR_CODE);
        }

        public static Collection<BarcodeFormat> getQrCodeFormats() {
            return QR_CODE_FORMATS;
        }

        public static Collection<BarcodeFormat> getBarCodeFormats() {
            return ONE_D_FORMATS;
        }
    }
}
