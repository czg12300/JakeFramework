
package cn.common.ui.fragment;

import android.app.Activity;
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
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.common.eventbus.EventBus;
import cn.common.ui.activity.IUi;

public abstract class BaseFragment extends Fragment implements IUi {
    private static final int MSG_UI_INIT_DATA = 30000;

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
        }
    }

    private ArrayList<String> mActions;

    private BroadcastReceiver mReceiver;

    public LayoutInflater getLayoutInflater() {
        return getActivity().getLayoutInflater();
    }

    public View inflate(int layout, ViewGroup group) {
        return getLayoutInflater().inflate(layout, group);
    }

    public View inflate(int layout) {
        return getLayoutInflater().inflate(layout, null);
    }

    private FrameLayout mDecorView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
    public void onDetach() {
        super.onDetach();
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUiHandler = new UiHandler(this);

    }

    /**
     * 获取资源文件的颜色值
     *
     * @param id
     * @return
     */
    protected int getColor(int id) {
        return getResources().getColor(id);
    }

    /**
     * 获取资源文件的尺寸
     *
     * @param id
     * @return
     */
    protected float getDimension(int id) {
        return getResources().getDimension(id);
    }

    /**
     * 获取资源文件int数组
     *
     * @param id
     * @return
     */
    protected int[] getIntArray(int id) {
        return getResources().getIntArray(id);
    }

    /**
     * 获取资源文件string数组
     *
     * @param id
     * @return
     */
    protected String[] getStringArray(int id) {
        return getResources().getStringArray(id);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        sendEmptyUiMessage(MSG_UI_INIT_DATA);
        super.onActivityCreated(savedInstanceState);
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
        switch (msg.what) {
            case MSG_UI_INIT_DATA:
                initData();
                break;
        }
    }

    protected Bundle mSavedInstanceState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDecorView = new FrameLayout(getActivity());
        mSavedInstanceState = savedInstanceState;
        EventBus.getDefault().register(this.getClass().getClassLoader(), this.getClass().getName(), this);
        return mDecorView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initEvent();
    }

    protected View findViewById(int id) {
        return mDecorView.findViewById(id);
    }

    protected abstract void initView();

    protected void initEvent() {
    }

    protected void initData() {
    }

    public void setContentView(int layoutId) {
        getLayoutInflater().inflate(layoutId, mDecorView);
    }

    public void setContentView(View view) {
        mDecorView.addView(view, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

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

    public void sendBroadcast(String action) {
        getActivity().sendBroadcast(new Intent(action));
    }

    public void sendBroadcast(Intent it) {
        getActivity().sendBroadcast(it);
    }

    public boolean canShowDialog() {
        return getActivity() != null || !getActivity().isFinishing();
    }
}
