
package cn.common.ui.widgt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2016/1/22 15:03
 */
public class NoConflictScrollView extends ScrollView {
    public NoConflictScrollView(Context context) {
        super(context);
    }

    public NoConflictScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoConflictScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float xDistance, yDistance, xLast, yLast;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;
                // 解决与viewpager冲突的问题
                if (xDistance >yDistance) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
