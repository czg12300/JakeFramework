package com.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.helper.camera.CameraManager;

import java.io.IOException;

public class CaptureHelper implements SurfaceHolder.Callback, Handler.Callback {


  public static interface CaptureListener {
    void handleCodeResult(String code, Bitmap bitmap);

    void foundPossibleResultPoint(ResultPoint point);
  }

  private enum State {
    PREVIEW, SUCCESS, DONE
  }

  public static final int MSG_RESTART_PREVIEW = 0;

  public static final int MSG_DECODE_SUCCEEDED = 1;

  public static final int MSG_DECODE_FAILED = 2;

  public static final int MSG_RETURN_SCAN_RESULT = 3;

  public static final int MSG_LAUNCH_PRODUCT_QUERY = 4;

  private SurfaceView mSvCamera;

  private Activity mActivity;

  private InactivityTimer mInactivityTimer;

  private BeepManager mBeepManager;

  private CameraManager mCameraManager;

  private State mState;

  private Handler mHandler = new Handler(this);

  private boolean isHasSurface = false;

  private CaptureListener mListener;

  private Rect mScanFrameRect;

  private DecodeHelper mDecodeHelper;

  public CaptureHelper(SurfaceView sv, Activity activity) {
    mSvCamera = sv;
    mActivity = activity;
    mInactivityTimer = new InactivityTimer(activity);
    mBeepManager = new BeepManager(activity);
    mCameraManager = new CameraManager(mActivity);
    mCameraManager.setPreviewCallback(new PreviewCallback() {
      @Override
      public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCameraManager == null || mDecodeHelper == null) {
          return;
        }
        Point cameraResolution = mCameraManager.getConfigManager().getCameraResolution();
        if (cameraResolution != null) {
          Message message = mDecodeHelper.getHandler().obtainMessage(DecodeHelper.MSG_DECODE, cameraResolution.x, cameraResolution.y, data);
          message.sendToTarget();
        }

      }
    });
  }

  private void displayFrameworkBugMessageAndExit() {
    // camera error
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    builder.setTitle("提示");
    builder.setMessage("相机打开出错，请稍后重试");
    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        mActivity.finish();
      }

    });
    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

      @Override
      public void onCancel(DialogInterface dialog) {
        mActivity.finish();
      }
    });
    builder.show();
  }

  public CameraManager getCameraManager() {
    return mCameraManager;
  }

  public CaptureListener getCaptureListener() {
    return mListener;
  }

  public Handler getHandler() {
    return mHandler;
  }

  /**
   * A valid barcode has been found, so give an indication of success and show
   * the results.
   *
   * @param msg The extras
   */
  private void handleDecode(Message msg) {
    if (msg == null) {
      return;
    }
    mInactivityTimer.onActivity();
    mBeepManager.playBeepSoundAndVibrate();
    String code = null;
    Bitmap barcode = null;
    Bundle bundle = msg.getData();
    float scaleFactor = 1.0f;
    if (bundle != null) {
      byte[] compressedBitmap = bundle.getByteArray(DecodeHelper.BARCODE_BITMAP);
      if (compressedBitmap != null) {
        barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
        // Mutable copy:
        barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
      }
      scaleFactor = bundle.getFloat(DecodeHelper.BARCODE_SCALED_FACTOR);
    }
    if (msg.obj instanceof Result) {
      Result rawResult = (Result) msg.obj;
      if (rawResult != null) {
        code = rawResult.getText();
        drawResultPoints(barcode, scaleFactor, rawResult);
      }
    }
    if (mListener != null) {
      mListener.handleCodeResult(code, barcode);
    }
  }

  /**
   * Superimpose a line for 1D or dots for 2D to highlight the key features of
   * the barcode.
   *
   * @param barcode     A bitmap of the captured image.
   * @param scaleFactor amount by which thumbnail was scaled
   * @param rawResult   The decoded results which contains the points to draw.
   */
  private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
    ResultPoint[] points = rawResult.getResultPoints();
    if (points != null && points.length > 0) {
      Canvas canvas = new Canvas(barcode);
      Paint paint = new Paint();
      paint.setColor(Color.parseColor("#b0000000"));
      if (points.length == 2) {
        paint.setStrokeWidth(4.0f);
        drawLine(canvas, paint, points[0], points[1], scaleFactor);
      } else if (points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
        // Hacky special case -- draw two lines, for the barcode and
        // metadata
        drawLine(canvas, paint, points[0], points[1], scaleFactor);
        drawLine(canvas, paint, points[2], points[3], scaleFactor);
      } else {
        paint.setStrokeWidth(10.0f);
        for (ResultPoint point : points) {
          if (point != null) {
            canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
          }
        }
      }
    }
  }

  private void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
    if (a != null && b != null) {
      canvas.drawLine(scaleFactor * a.getX(), scaleFactor * a.getY(), scaleFactor * b.getX(), scaleFactor * b.getY(), paint);
    }
  }

  @Override
  public boolean handleMessage(Message msg) {
    switch (msg.what) {
      case MSG_RESTART_PREVIEW:
        restartPreviewAndDecode();
        break;
      case MSG_DECODE_SUCCEEDED:
        mState = State.SUCCESS;
        handleDecode(msg);
        break;
      case MSG_DECODE_FAILED:
        mState = State.PREVIEW;
        mCameraManager.requestPreviewFrame();
        break;
      case MSG_RETURN_SCAN_RESULT:
        mActivity.setResult(Activity.RESULT_OK, (Intent) msg.obj);
        mActivity.finish();
        break;
    }
    return true;
  }

  public void onPause() {
    mState = State.DONE;
    mCameraManager.stopPreview();
    Message quit = Message.obtain(mDecodeHelper.getHandler(), DecodeHelper.MSG_QUIT);
    quit.sendToTarget();
    mDecodeHelper = null;
    mHandler.removeMessages(MSG_DECODE_SUCCEEDED);
    mHandler.removeMessages(MSG_DECODE_FAILED);
    mInactivityTimer.onPause();
    mBeepManager.close();
    mCameraManager.closeDriver();
    if (!isHasSurface) {
      mSvCamera.getHolder().removeCallback(this);
    }
  }

  public void onResume() {
    if (isHasSurface) {
      if (!mCameraManager.isOpened()) {
        try {
          mCameraManager.openDriver(mSvCamera.getHolder());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } else {
      mSvCamera.getHolder().addCallback(this);
    }
    mDecodeHelper = new DecodeHelper(this, new ResultPointCallback() {

      @Override
      public void foundPossibleResultPoint(ResultPoint point) {
        if (mListener != null) {
          mListener.foundPossibleResultPoint(point);
        }
      }
    });
    mInactivityTimer.onResume();
  }

  public void onDestroy() {
    mInactivityTimer.shutdown();
  }

  /**
   * 设置打开闪光灯
   *
   * @param isOn
   */
  public void setTorch(boolean isOn) {
    mCameraManager.setTorch(isOn);
  }

  public void restartPreviewAfterDelay(long delayMS) {
    if (mHandler != null) {
      mHandler.sendEmptyMessageDelayed(MSG_RESTART_PREVIEW, delayMS);
    }
  }

  public void restartPreview() {
    restartPreviewAfterDelay(0);
  }

  private void restartPreviewAndDecode() {
    if (mState == State.SUCCESS) {
      mState = State.PREVIEW;
      mCameraManager.requestPreviewFrame();
    }
  }


  public Rect getScanFrameRect() {
    if (mScanFrameRect == null) {
      mScanFrameRect = new Rect(0, 0, mSvCamera.getWidth(), mSvCamera.getHeight());
    }
    return mScanFrameRect;
  }

  public SurfaceView getPreviewView() {
    return mSvCamera;
  }

  public void setBeepRawId(int rawId) {
    mBeepManager.setIsPlayBeep(true);
    mBeepManager.setBeepRawId(rawId);
  }

  public void setCaptureListener(CaptureListener listener) {
    mListener = listener;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (!isHasSurface) {
      isHasSurface = true;
      try {
        mCameraManager.openDriver(holder);
        mState = State.SUCCESS;
        mCameraManager.startPreview();
        restartPreviewAndDecode();
      } catch (IOException ioe) {
        displayFrameworkBugMessageAndExit();
      } catch (RuntimeException e) {
        displayFrameworkBugMessageAndExit();
      }
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    isHasSurface = false;
  }

}
