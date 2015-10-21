
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

/**
 * has under line tab's view pager
 *
 * @author jake
 */
public class IndicatorViewPager extends LinearLayout implements OnPageChangeListener {
    private IndicatorView mIndicator;

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
        mIndicator = new IndicatorView(getContext());
        mViewPager = new ViewPagerCompat(getContext());
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setId(123456);
        setTabHeight((int) dip(45));
        addView(mIndicator);
        addView(mViewPager, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mIndicator.onScrolled((mViewPager.getWidth() + mViewPager.getPageMargin()) * position
                + positionOffsetPixels);
        if (mIsSwitchAnimation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            switchAnmation(position, positionOffset);
        }
    }

    /**
     * swtich annmation
     *
     * @param position
     * @param positionOffset
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void switchAnmation(int position, float positionOffset) {
        if (position < mViewPager.getAdapter().getCount() - 1) {
            mViewPager.getChildAt(position + 1).setAlpha(positionOffset);
        }
        mViewPager.getChildAt(position).setAlpha(1 - positionOffset);

    }

    @Override
    public void onPageSelected(int position) {
        mIndicator.onSwitched(position);
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

    private float dip(int dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                getContext().getResources().getDisplayMetrics());
    }

    public void setTabHeight(float tabHeight) {
        mIndicator.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) tabHeight));
    }

    public void setTabBackgroundColor(int color) {
        mIndicator.setBackgroundColor(color);
    }

    public void setTabTextSize(float textSize) {
        mIndicator.setTextSize(textSize);
    }

    public void setTabTextColor(int color) {
        mIndicator.setTextColor(color);
    }

    public void setTabChangeColor(boolean isChange) {
        mIndicator.setChangeTabColor(isChange);
    }

    public void setTabSelectColor(int color) {
        mIndicator.setTabSelectColor(color);
    }

    public void setTabLineColor(int color) {
        mIndicator.setUnderLineColor(color);
    }

    public void setTabLineHeight(float px) {
        mIndicator.setUnderLineHeight(px);
    }

    public void setTabLineNormalHeight(float px) {
        mIndicator.setUnderLineNormalHeight(px);
    }

    public void setAverage(boolean isAvaerage) {
        mIndicator.setAverage(isAvaerage);
    }

    public void setIndicator(IIndicator indicator) {
        if (indicator == null) {
            throw new NullPointerException("indicator is null");
        }
        if (indicator.getLabelList() == null) {
            throw new NullPointerException("indicator's labelList  is null");

        }
        if (indicator.getAdapter() == null) {
            throw new NullPointerException("indicator's pageAdapter  is null");

        }
        if (indicator.getLabelList().size() != indicator.getAdapter().getCount()) {
            throw new IllegalArgumentException(
                    "indicator's labelList size is not equal pageAdapter size");

        }

        mViewPager.setAdapter(indicator.getAdapter());
        mIndicator.init(0, indicator.getLabelList(), mViewPager);

    }

    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    /**
     * 设置换成页面
     *
     * @param page
     */
    public void setOffscreenPageLimit(int page) {
        mViewPager.setOffscreenPageLimit(page);
    }

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
