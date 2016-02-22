
package cn.common.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import cn.common.ui.widgt.ChangeThemeUtils;

public abstract class BaseTitleActivity extends BaseSwipeBackFragmentActivity {
    private FrameLayout mFlTitle;

    private static final int MSG_UI_INIT_DATA = 10000;

    private FrameLayout mFlContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        mFlTitle = new FrameLayout(this);
        mFlContent = new FrameLayout(this);
        mFlContent.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mFlTitle.setVisibility(View.GONE);
        layout.addView(mFlTitle, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        layout.addView(mFlContent, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        super.setContentView(layout);
        setTitleLayout(getTitleLayoutView());
        initView();
        initEvent();
        sendEmptyUiMessage(MSG_UI_INIT_DATA);
    }

    protected void setBackgroundColor(int color) {
        mFlContent.setBackgroundColor(color);
    }

    protected abstract View getTitleLayoutView();

    /**
     * 初始化view
     */
    protected abstract void initView();

    /**
     * 初始化事件
     */
    protected void initEvent() {
    }

    @Override
    public void handleUiMessage(Message msg) {
        switch (msg.what) {
            case MSG_UI_INIT_DATA:
                initData();
                break;
        }
    }

    /**
     * 初始化数据
     */
    protected void initData() {
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(getLayoutInflater().inflate(layoutResID, null));
    }

    @Override
    public void setContentView(View view) {
        setContentView(view, null);
    }

    @Override
    public void setContentView(View view, android.view.ViewGroup.LayoutParams lp) {
        mFlContent.addView(view, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    public void setTitleLayout(int resId) {
        setTitleLayout(getLayoutInflater().inflate(resId, null));
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            setTitle(title.toString());
        }
    }

    @Override
    public void setTitle(int titleId) {
        String title = getString(titleId);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

    }

    protected void setTitle(String title) {
    }

    protected boolean hasTitle() {
        return true;
    }

    private void setTitleLayout(View view) {
        if (hasTitle()) {
            mFlTitle.setVisibility(View.VISIBLE);
            mFlTitle.setPadding(0, 0, 0, 0);
            mFlTitle.addView(view, new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        }
    }

}
