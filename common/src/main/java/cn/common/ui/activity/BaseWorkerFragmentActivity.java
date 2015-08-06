
package cn.common.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

public class BaseWorkerFragmentActivity extends BaseFragmentActivity {

    private HandlerThread mHandlerThread;

    private BackgroundHandler mBackgroundHandler;

    private static class BackgroundHandler extends Handler {

        private final WeakReference<BaseWorkerFragmentActivity> mActivityReference;

        BackgroundHandler(BaseWorkerFragmentActivity activity, Looper looper) {
            super(looper);
            mActivityReference = new WeakReference<BaseWorkerFragmentActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivityReference.get() != null) {
                mActivityReference.get().handleBackgroundMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandlerThread = new HandlerThread("activity worker:" + getClass().getSimpleName());
        mHandlerThread.start();
        mBackgroundHandler = new BackgroundHandler(this, mHandlerThread.getLooper());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null && mBackgroundHandler.getLooper() != null) {
            mBackgroundHandler.getLooper().quit();
        }
    }

    public void handleBackgroundMessage(Message msg) {
    }

    protected void sendBackgroundMessage(Message msg) {
        mBackgroundHandler.sendMessage(msg);
    }

    protected void sendBackgroundMessageDelayed(Message msg, long delay) {
        mBackgroundHandler.sendMessageDelayed(msg, delay);
    }

    protected void sendEmptyBackgroundMessage(int what) {
        mBackgroundHandler.sendEmptyMessage(what);
    }

    protected void sendEmptyBackgroundMessageDelayed(int what, long delay) {
        mBackgroundHandler.sendEmptyMessageDelayed(what, delay);
    }

    protected void removeBackgroundMessages(int what) {
        mBackgroundHandler.removeMessages(what);
    }

    protected Message obtainBackgroundMessage() {
        return mBackgroundHandler.obtainMessage();
    }
}
