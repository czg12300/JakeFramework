
package cn.common.ui.widgt.pull;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class PullEnableListView extends ListView implements PullEnable {
    private boolean canScrollUp = true;

    private boolean canScrollDown = true;

    public PullEnableListView(Context context) {
        super(context);
    }

    public PullEnableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullEnableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean canPullDown() {
        if (getCount() == 0 && canScrollDown) {
            // 没有item的时候也可以下拉刷新
            return true;
        } else if (getFirstVisiblePosition() == 0 && getChildAt(0).getTop() >= 0 && canScrollDown) {
            // 滑到ListView的顶部了
            return true;
        } else
            return false;
    }

    @Override
    public boolean canPullUp() {
        if (getCount() == 0) {
            // 没有item的时候也可以上拉加载
            return true;
        } else if (getLastVisiblePosition() == (getCount() - 1) && canScrollUp) {
            // 滑到底部了
            if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
                    && getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()).getBottom() <= getMeasuredHeight())
                return true;
        }
        return false;
    }

    public boolean isCanScrollUp() {
        return canScrollUp;
    }

    public void setCanScrollUp(boolean canScrollUp) {
        this.canScrollUp = canScrollUp;
    }

    public boolean isCanScrollDown() {
        return canScrollDown;
    }

    public void setCanScrollDown(boolean canScrollDown) {
        this.canScrollDown = canScrollDown;
    }
}
