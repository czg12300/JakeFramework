
package cn.common.ui.widgt.pulltorefresh;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * description：base of pull layout view
 *
 * @author jakechen
 * @since 2015/9/16 18:11
 */
public abstract class BasePullLayoutView extends LinearLayout {
    private final static float OFFSET_RADIO = 1.8f;

    private static final int MSG_HEADER = 0;

    private static final int MSG_HEADER_REFRESH = 1;

    private static final int MSG_FOOTER = 2;

    private static final int MSG_FOOTER_LOAD = 3;

    private static final int SCROLL_BACK_HEADER = 0;

    private static final int SCROLL_BACK_FOOTER = 1;

    private FrameLayout mHeader;

    private FrameLayout mFooter;

    private View mVContent;

    private PullEnable mPullEnable;

    private float mLastY;

    private PullListener mPullListener;

    private Scroller mScroller;

    private int mScrollBack;

    private int mHeaderHeight;

    private int mFooterHeight;

    private int pullDownY;

    private int pullUpY;

    public BasePullLayoutView(Context context) {
        this(context, null);
    }

    public BasePullLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        mScroller = new Scroller(context, new DecelerateInterpolator());
        mHeader = new FrameLayout(context);
        mFooter = new FrameLayout(context);
        addView(mHeader, new LayoutParams(-1, -2));
        mVContent = getContentView();
        if (mVContent instanceof PullEnable) {
            mPullEnable = (PullEnable) mVContent;
        } else {
            throw new IllegalArgumentException("content view is not instance of PullEnable");
        }
        LayoutParams lp = new LayoutParams(-1, 0);
        lp.weight = 1;
        addView(mVContent, lp);
        addView(mFooter, new LayoutParams(-1, -2));
        getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        mHeaderHeight = mHeader.getHeight();
                        mFooterHeight = mFooter.getHeight();
                        setHeaderTopMargin(-mHeaderHeight);
                        setFooterBottomMargin(-mFooterHeight);
                        ViewTreeObserver observer = getViewTreeObserver();
                        if (null != observer) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                observer.removeGlobalOnLayoutListener(this);
                            } else {
                                observer.removeOnGlobalLayoutListener(this);
                            }
                        }
                    }
                });
    }

    protected void onPullDown(View header, float radio, boolean isSliding) {
    }

    protected void onPullUp(View header, float radio, boolean isSliding) {
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceY = ev.getRawY() - mLastY;
                if (distanceY > 0 && mPullEnable.canPullDown()
                        || distanceY < 0 && mPullEnable.canPullUp()) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                if (deltaY > 0 && mPullEnable.canPullDown()) {
                    pullDownY = (int) (deltaY / OFFSET_RADIO) - mHeader.getHeight();
                    handlePullDownAnimation(true);
                    setHeaderTopMargin(pullDownY);
                    setFooterBottomMargin(-mFooterHeight);
                    pullUpY = 0;
                } else if (deltaY < 0 && mPullEnable.canPullUp()) {
                    int offset = -(int) (deltaY / OFFSET_RADIO);
                    pullUpY = offset - mFooter.getHeight();
                    handlePullUpAnimation(true);
                    setFooterBottomMargin(pullUpY);
                    setHeaderTopMargin(-mHeaderHeight);
                    pullDownY = 0;
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d("tag", "pullUpY =" + pullUpY + "  mFooterHeight=" + mFooterHeight
                        + "   pullDownY=" + pullDownY + " mHeaderHeight=" + mHeaderHeight);
                if (pullUpY == 0 && mPullEnable.canPullDown()) {
                    mScrollBack = SCROLL_BACK_HEADER;
                    if (pullDownY >= mHeaderHeight) {
                        mScroller.startScroll(0, pullDownY, 0, 0 - pullDownY);
                    } else {
                        if (pullDownY > 0) {
                            mScroller.startScroll(0, pullDownY, pullDownY + mHeaderHeight, 0);
                        } else {
                            mScroller.startScroll(0, -pullDownY, mHeaderHeight + pullDownY, 0);
                        }
                    }
                }
                if (pullDownY == 0 && mPullEnable.canPullUp()) {
                    mScrollBack = SCROLL_BACK_FOOTER;
                    if (pullUpY >= mFooterHeight) {
                        mScroller.startScroll(0, pullUpY, 0, 0 - pullUpY);
                    } else {
                        if (pullUpY > 0) {
                            mScroller.startScroll(0, pullUpY, pullUpY + mFooterHeight, 0);
                        } else {
                            mScroller.startScroll(0, -pullUpY, mFooterHeight + pullUpY, 0);
                        }
                    }
                }
                handlePullDownAnimation(false);
                handlePullUpAnimation(false);
                invalidate();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void handlePullUpAnimation(boolean isSliding) {
        if (pullUpY < 0) {
            onPullUp(mFooter, Math.abs((float) pullUpY / (float) mFooterHeight), isSliding);
        } else {
            onPullUp(mFooter, 1, isSliding);
        }
    }

    private void handlePullDownAnimation(boolean isSliding) {
        if (pullDownY < 0) {
            onPullDown(mHeader, Math.abs((float) pullDownY / (float) mHeaderHeight), isSliding);
        } else {
            onPullDown(mHeader, 1, isSliding);
        }
    }

    /**
     * 设置头部的滑动
     *
     * @param marginTop
     */
    private void setHeaderTopMargin(int marginTop) {
        LayoutParams lp = (LayoutParams) mHeader.getLayoutParams();
        lp.topMargin = marginTop;
        mHeader.setLayoutParams(lp);
        requestLayout();
    }

    /**
     * 设置底部的滑动
     *
     * @param marginBottom
     */
    private void setFooterBottomMargin(int marginBottom) {
        LayoutParams lp = (LayoutParams) mFooter.getLayoutParams();
        lp.bottomMargin = marginBottom;
        mFooter.setLayoutParams(lp);
        requestLayout();
    }

    private int getHeaderTopMargin() {
        return ((LayoutParams) mHeader.getLayoutParams()).topMargin;
    }

    private int getFooterBottomMargin() {
        return ((LayoutParams) mFooter.getLayoutParams()).bottomMargin;
    }

    public void setHeaderView(View header) {
        mHeader.addView(header);
    }

    public void setHeaderView(int layoutId) {
        mHeader.addView(inflate(getContext(), layoutId, null));
    }

    public void setFooterView(int layoutId) {
        mFooter.addView(inflate(getContext(), layoutId, null));
    }

    public void setFooterView(View footer) {
        mFooter.addView(footer);
    }

    protected abstract View getContentView();

    public PullListener getPullListener() {
        return mPullListener;
    }

    public void setPullListener(PullListener pullListener) {
        this.mPullListener = pullListener;
    }

    @Override
    public void computeScroll() {
        Log.d("tag", "mScroller.getCurrY()=" + mScroller.getCurrY());
        if (mScroller.computeScrollOffset()) {
            if (mScrollBack == SCROLL_BACK_HEADER) {
                if (pullDownY < mHeaderHeight) {
                    int total = Math.abs(mHeaderHeight - pullDownY);
                    setHeaderTopMargin(-(total - mScroller.getCurrY()));
                } else {
                    setHeaderTopMargin(mScroller.getCurrY());
                }
            } else {
                if (pullUpY < mFooterHeight) {
                    int total = Math.abs(mFooterHeight - pullUpY);
                    setFooterBottomMargin(-(total - mScroller.getCurrY()));
                } else {
                    setFooterBottomMargin(mScroller.getCurrY());
                }
            }
            postInvalidate();
        }
        super.computeScroll();
    }
}
