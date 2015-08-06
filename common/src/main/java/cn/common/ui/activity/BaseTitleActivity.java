
package cn.common.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import cn.common.ui.widgt.ChangeThemeUtils;

public abstract class BaseTitleActivity extends BaseSwipeBackFragmentActivity {
    private FrameLayout mFlTitle;

    private FrameLayout mFlContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        mFlTitle = new FrameLayout(this);
        mFlContent = new FrameLayout(this);
        mFlTitle.setBackgroundColor(Color.BLACK);
        layout.addView(mFlTitle, new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        layout.addView(mFlContent, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        super.setContentView(layout);
        ChangeThemeUtils.adjustStatusBar(mFlTitle, this);
        initView();
    }

    protected abstract void initView();

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

    public void setTitleLayout(View view) {
        mFlTitle.setPadding(0, 0, 0, 0);
        mFlTitle.addView(view, new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        ChangeThemeUtils.adjustStatusBar(view, this);
    }

}
