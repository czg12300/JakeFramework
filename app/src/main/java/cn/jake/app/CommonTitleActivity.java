
package cn.jake.app;

import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.common.ui.activity.BaseTitleActivity;
import cn.common.utils.CommonUtil;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2015/10/23 16:40
 */
public abstract class CommonTitleActivity extends BaseTitleActivity {
    protected ImageView mIvBack;

    protected TextView mTvTitle;

    protected View mVTitle;

    @Override
    protected View getTitleLayoutView() {
        mVTitle = getLayoutInflater().inflate(R.layout.title_common_back, null);
        mIvBack = (ImageView) mVTitle.findViewById(R.id.iv_back);
        mTvTitle = (TextView) mVTitle.findViewById(R.id.tv_title);
        mIvBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isFinishing()) {
                    finish();
                }
                onBack();
            }
        });
        setBackgroundColor(Color.parseColor("#f9f9f9"));
        return mVTitle;
    }

    /**
     * 设置点击隐藏软键盘
     */
    public void setHideInputView(int id) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.hideSoftInput(CommonTitleActivity.this);
            }
        });
    }

    @Override
    protected void setTitle(String title) {
        mTvTitle.setText(title);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onBack();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isFinishing()) {
                finish();
            }
            onBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /**
     * 返回按键或退出按钮的回调接口
     */
    protected void onBack() {

    }
}
