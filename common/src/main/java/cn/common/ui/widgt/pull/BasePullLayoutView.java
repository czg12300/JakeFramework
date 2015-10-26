
package cn.common.ui.widgt.pull;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * 描述:上下拉的基类
 *
 * @author jakechen
 * @since 2015/10/26 14:58
 */
public abstract class BasePullLayoutView extends RelativeLayout {
    private final static float OFFSET_RADIO = 1.8f;

    private float mLastY;

    public BasePullLayoutView(Context context) {
        this(context, null);
    }

    public BasePullLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                if (deltaY > 0) {
                    handlePullDown((int) (deltaY / OFFSET_RADIO));
                } else if (deltaY < 0) {
                    handlePullUp((int) (deltaY / OFFSET_RADIO));
                }
                break;
            case MotionEvent.ACTION_UP:
                handleReset();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    protected abstract void handleReset();

    protected abstract void handlePullUp(int deltaY);

    protected abstract void handlePullDown(int deltaY);
}
