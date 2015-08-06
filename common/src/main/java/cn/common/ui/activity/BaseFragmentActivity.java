
package cn.common.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseFragmentActivity extends FragmentActivity implements IUi {
    protected static final int REQUEST_CODE = 0x123f;

    protected static final int RESULT_CODE = 0x124f;

    /**
     * layout params
     */
    public static final int MATCH_PARENT = -1;

    /**
     * layout params
     */
    public static final int WRAP_CONTENT = -2;

    private Handler mUiHandler;

    private static class UiHandler extends Handler {
        private final WeakReference<BaseFragmentActivity> mActivityReference;

        public UiHandler(BaseFragmentActivity activity) {
            mActivityReference = new WeakReference<BaseFragmentActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivityReference.get() != null) {
                mActivityReference.get().handleUiMessage(msg);
            }
        };
    }

    private ArrayList<String> mActions;

    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getInstance().addActivity(this);
        mUiHandler = new UiHandler(this);
        mActions = new ArrayList<String>();
        setupBroadcastActions(mActions);
        if (mActions != null && mActions.size() > 0) {
            IntentFilter filter = new IntentFilter();
            for (String action : mActions) {
                filter.addAction(action);
            }
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    handleBroadcast(context, intent);
                }
            };
            registerReceiver(mReceiver, filter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        BaseApplication.getInstance().removeActivity(this.getClass().getSimpleName());
    }

    protected void sendUiMessage(Message msg) {
        mUiHandler.sendMessage(msg);
    }

    protected void sendUiMessageDelayed(Message msg, long delayMillis) {
        mUiHandler.sendMessageDelayed(msg, delayMillis);
    }

    protected void sendEmptyUiMessage(int what) {
        mUiHandler.sendEmptyMessage(what);
    }

    protected void sendEmptyUiMessageDelayed(int what, long delayMillis) {
        mUiHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected void removeUiMessages(int what) {
        mUiHandler.removeMessages(what);
    }

    protected Message obtainUiMessage() {
        return mUiHandler.obtainMessage();
    }

    @Override
    public void setupBroadcastActions(List<String> actions) {

    }

    @Override
    public void handleBroadcast(Context context, Intent intent) {

    }

    @Override
    public void handleUiMessage(Message msg) {

    }

    @Override
    public void goActivity(Class<?> clazz) {
        goActivity(clazz, null);
    }

    @Override
    public void goActivity(Class<?> clazz, Bundle bundle) {
        Intent it = new Intent();
        it.setClass(this, clazz);
        if (bundle != null) {
            it.putExtras(bundle);
        }
        startActivity(it);
    }

    @Override
    public void goActivityForResult(Class<?> clazz) {
        goActivityForResult(clazz, null);
    }

    @Override
    public void goActivityForResult(Class<?> clazz, Bundle bundle) {
        goActivityForResult(clazz, bundle, REQUEST_CODE);
    }

    @Override
    public void goActivityForResult(Class<?> clazz, Bundle bundle, int requestCode) {
        Intent it = new Intent();
        it.setClass(this, clazz);
        if (bundle != null) {
            it.putExtras(bundle);
        }
        startActivityForResult(it, requestCode);
    }
}
