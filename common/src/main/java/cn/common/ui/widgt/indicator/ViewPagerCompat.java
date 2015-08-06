/*
 * @author http://blog.csdn.net/singwhatiwanna
 */

package cn.common.ui.widgt.indicator;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerCompat extends ViewPager {

    public ViewPagerCompat(Context context) {
        super(context);
    }

    public ViewPagerCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewPager mParentViewPager;// 此处我直接在调用的时候静态赋值过来 的

    private int abc = 1;

    private float mLastMotionX;

    public void setParentViewPager(ViewPager parent) {
        mParentViewPager = parent;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final float x = ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mParentViewPager.requestDisallowInterceptTouchEvent(true);
                abc = 1;
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                if (abc == 1) {
                    if (x - mLastMotionX > 5 && getCurrentItem() == 0) {
                        abc = 0;
                        mParentViewPager.requestDisallowInterceptTouchEvent(false);
                    }

                    if (x - mLastMotionX < -5 && getCurrentItem() == getAdapter().getCount() - 1) {
                        abc = 0;
                        mParentViewPager.requestDisallowInterceptTouchEvent(false);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mParentViewPager.requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

}
