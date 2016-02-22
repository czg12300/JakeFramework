package cn.common.ui.widgt;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 描述：已经解决了冲突的viewpager
 * 作者：jake on 2016/1/3 13:43
 */
public class NoConflictViewPager extends ViewPager {
    private float xDistance, yDistance, xLast, yLast;

    public NoConflictViewPager(Context context) {
        this(context, null);
    }

    public NoConflictViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

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
                if (xDistance > yDistance) {
                    return true;
                }
        }
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

}