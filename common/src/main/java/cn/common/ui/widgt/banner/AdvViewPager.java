package cn.common.ui.widgt.banner;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;

public class AdvViewPager extends ViewPager {
	private float xDistance, yDistance, xLast, yLast;

	public AdvViewPager(Context context) {
		super(context);
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