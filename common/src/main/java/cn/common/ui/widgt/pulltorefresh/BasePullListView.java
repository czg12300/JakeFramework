
package cn.common.ui.widgt.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import cn.common.ui.widgt.pull.PullToRefreshLayout;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2015/9/17 17:17
 */
public abstract class BasePullListView extends PullToRefreshLayout {
    public MyListView mListView;

    private boolean canPullUp = true;

    private boolean canPullDown = true;

    public boolean isCanPullUp() {
        return canPullUp;
    }

    public void setCanPullUp(boolean canPullUp) {
        this.canPullUp = canPullUp;
    }

    public boolean isCanPullDown() {
        return canPullDown;
    }

    public void setCanPullDown(boolean canPullDown) {
        this.canPullDown = canPullDown;
    }

    public BasePullListView(Context context) {
        super(context);
    }

    public BasePullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getContentView() {
        mListView = new MyListView(getContext());
        return mListView;
    }

    public MyListView getListView() {
        return mListView;
    }

    public class MyListView extends ListView implements PullEnable {

        public MyListView(Context context) {
            super(context);
        }

        @Override
        public boolean canPullUp() {
            return getLastVisiblePosition() == getCount() - 1 && canPullUp;
        }

        @Override
        public boolean canPullDown() {
            return getFirstVisiblePosition() == 0 && canPullDown;
        }
    }

}
