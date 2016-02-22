
package cn.common.ui.widgt.indicator;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import cn.common.utils.DisplayUtil;

/**
 * has under line tab's view pager
 *
 * @author jake
 */
public class IndicatorViewPager extends LinearLayout implements OnPageChangeListener {

    private View vDivider;

    public class ViewPagerCompat extends ViewPager {

        public ViewPagerCompat(Context context) {
            super(context);
        }

        public ViewPagerCompat(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
            if (!canScroll) {
                return true;
            }
            return super.canScroll(v, checkV, dx, x, y);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (!canScroll) {
                return false;
            }
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!canScroll) {
                return false;
            }
            return super.onTouchEvent(ev);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (!isLeftMost() && !isRightMost()) {
                requestDisallowInterceptTouchEvent(false);
            }
            return super.dispatchTouchEvent(ev);
        }

    }

    private IndicatorView indicator;

    private ViewPagerCompat mViewPager;

    private boolean mIsSwitchAnimation;

    private boolean canScroll = true;

    public boolean canScroll() {
        return canScroll;
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    public IndicatorViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        initView();
    }

    public IndicatorViewPager(Context context) {
        this(context, null);
    }

    private void initView() {
        indicator = new IndicatorView(getContext());
        mViewPager = new ViewPagerCompat(getContext());
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setId(123456);
        setTabHeight((int) dip(45));
        addView(indicator);
        vDivider = new View(getContext());
        vDivider.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0));
        addView(vDivider);
        addView(mViewPager, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        indicator.setViewPager(mViewPager);
    }

    public View getDivider() {
        return vDivider;
    }

    public void setDividerHeight(int px) {
        LinearLayout.LayoutParams params = (LayoutParams) vDivider.getLayoutParams();
        params.height = px;
        vDivider.setLayoutParams(params);
        requestLayout();
    }

    public void setDividerColor(int color) {
        vDivider.setBackgroundColor(color);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        indicator.onPageScrolled((mViewPager.getWidth() + mViewPager.getPageMargin()) * position
                + positionOffsetPixels);
        if (mIsSwitchAnimation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            switchAnimation(position, positionOffset);
        }
    }

    /**
     * swtich annmation
     *
     * @param position
     * @param positionOffset
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void switchAnimation(int position, float positionOffset) {
        if (position < mViewPager.getAdapter().getCount() - 1) {
            mViewPager.getChildAt(position + 1).setAlpha(positionOffset);
        }
        mViewPager.getChildAt(position).setAlpha(1 - positionOffset);

    }

    private float dip(int dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                getContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onPageSelected(int position) {
        indicator.onPageSelected(position);
        if (indicatorListener != null) {
            indicatorListener.onTabPageSelected(position);
        }
    }

    public void isSwitchAnnmation(boolean b) {
        mIsSwitchAnimation = b;
    }

    /**
     * is most left
     */
    public boolean isLeftMost() {
        if (mViewPager != null) {
            if (mViewPager.getCurrentItem() == 0 && mViewPager.getAdapter().getCount() > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * is most right
     *
     * @return
     */
    public boolean isRightMost() {
        if (mViewPager != null) {
            if (mViewPager.getCurrentItem() == mViewPager.getAdapter().getCount() - 1) {
                return true;
            }
        }
        return false;
    }


    public void setTabHeight(float tabHeight) {
        indicator.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) tabHeight));
    }


    public ViewPager getViewPager() {
        return mViewPager;
    }

    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }


    public IndicatorView getIndicator() {
        return indicator;
    }

    private IIndicatorListener indicatorListener;

    public IIndicatorListener getIndicatorListener() {
        return indicatorListener;
    }

    public void setIndicatorListener(IIndicatorListener indicatorListener) {
        this.indicatorListener = indicatorListener;
    }

    public static interface IIndicatorListener {
        void onTabPageSelected(int position);
    }
}
