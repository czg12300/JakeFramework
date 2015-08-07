
package cn.common.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.common.ui.activity.IUi;

public abstract class BaseFragment extends Fragment implements IUi {
    protected static final int REQUEST_CODE = 0x125f;

    protected static final int RESULT_CODE = 0x126f;

    public static final int MATCH_PARENT = -1;

    public static final int WRAP_CONTENT = -2;

    private Handler mUiHandler;

    private static class UiHandler extends Handler {
        private final WeakReference<BaseFragment> mActivityReference;

        public UiHandler(BaseFragment activity) {
            mActivityReference = new WeakReference<BaseFragment>(activity);
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

    public LayoutInflater getLayoutInflater() {
        return getActivity().getLayoutInflater();
    }

    protected View convertView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            getActivity().registerReceiver(mReceiver, filter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        convertView = initView();
        return convertView;
    }

    protected View findViewById(int id) {
        return convertView.findViewById(id);
    }

    public abstract View initView();

    @Override
    public void goActivity(Class<?> clazz) {
        goActivity(clazz, null);
    }

    @Override
    public void goActivity(Class<?> clazz, Bundle bundle) {
        Intent it = new Intent();
        it.setClass(getActivity(), clazz);
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
        it.setClass(getActivity(), clazz);
        if (bundle != null) {
            it.putExtras(bundle);
        }
        startActivityForResult(it, requestCode);
    }
}
