
package cn.common.ui.widgt.pull;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import cn.common.ui.widgt.pulltorefresh.PullEnable;
import cn.common.ui.widgt.pulltorefresh.PullListener;

/**
 * 描述:上下拉控件
 *
 * @author jakechen
 * @since 2015/10/26 15:05
 */
public abstract class PullLayoutView extends BasePullLayoutView {
    private Scroller mScroller;

    private FrameLayout mHeader;

    private FrameLayout mFooter;

    private View mVContent;

    private PullEnable mPullEnable;

    private float mLastY;

    private static final int SCROLL_BACK_HEADER = 0;

    private static final int SCROLL_BACK_FOOTER = 1;

    private PullListener mPullListener;

    private int mScrollBack;

    private int mHeaderHeight;

    private int mFooterHeight;

    private int pullDownY;

    private int pullUpY;

    // private boolean isRefreshing = false;
    //
    // private boolean isLoading = false;

    public PullLayoutView(Context context) {
        this(context, null);
    }

    public PullLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context, new DecelerateInterpolator());
        mHeader = new FrameLayout(context);
        mFooter = new FrameLayout(context);
        mHeader.setId(1234);
        mFooter.setId(1235);
        LayoutParams rl = new LayoutParams(-1, -2);
        rl.addRule(ALIGN_PARENT_TOP);
        addView(mHeader, rl);
        mVContent = getContentView();
        if (mVContent instanceof PullEnable) {
            mPullEnable = (PullEnable) mVContent;
        } else {
            throw new IllegalArgumentException("content view is not instance of PullEnable");
        }
        rl = new LayoutParams(-1, -2);
        rl.addRule(ALIGN_PARENT_BOTTOM);
        addView(mFooter, rl);
        rl = new LayoutParams(-1, -1);
        rl.addRule(BELOW, mHeader.getId());
        rl.addRule(ABOVE, mFooter.getId());
        addView(mVContent, rl);
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

    protected abstract View getContentView();

    @Override
    protected void handleReset() {
        Log.d("tag", "pullUpY =" + pullUpY + "  mFooterHeight=" + mFooterHeight + "   pullDownY="
                + pullDownY + " mHeaderHeight=" + mHeaderHeight);
        if (pullUpY == 0 && mPullEnable.canPullDown()) {
            mScrollBack = SCROLL_BACK_HEADER;
            if (pullDownY >= mHeaderHeight) {
                mScroller.startScroll(0, pullDownY, 0, 0 - pullDownY);
                if (mPullListener != null) {
                    mPullListener.onRefresh();
                }
            } else {
                if (pullDownY > 0) {
                    mScroller.startScroll(0, pullDownY, pullDownY + mHeaderHeight, 0);
                } else {
                    mScroller.startScroll(0, -pullDownY, mHeaderHeight + pullDownY, 0);
                }
            }
            handlePullDownAnimation(false);
        }
        if (pullDownY == 0 && mPullEnable.canPullUp()) {
            mScrollBack = SCROLL_BACK_FOOTER;
            if (pullUpY >= mFooterHeight) {
                mScroller.startScroll(0, pullUpY, 0, 0 - pullUpY);
                if (mPullListener != null) {
                    mPullListener.onLoad();
                }
            } else {
                if (pullUpY > 0) {
                    mScroller.startScroll(0, pullUpY, pullUpY + mFooterHeight, 0);
                } else {
                    mScroller.startScroll(0, -pullUpY, mFooterHeight + pullUpY, 0);
                }
            }
            handlePullUpAnimation(false);
        }
        invalidate();
    }

    @Override
    protected void handlePullUp(int deltaY) {
        if (mPullEnable.canPullUp()) {
            pullUpY = -deltaY - mFooterHeight;
            handlePullUpAnimation(true);
            setFooterBottomMargin(pullUpY);
            pullDownY = 0;
        }
    }

    @Override
    protected void handlePullDown(int deltaY) {
        if (mPullEnable.canPullDown()) {
            pullDownY = deltaY - mHeaderHeight;
            handlePullDownAnimation(true);
            setHeaderTopMargin(pullDownY);
            setFooterBottomMargin(-mFooterHeight);
            pullUpY = 0;
        }
    }

    @Override
    public void computeScroll() {
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceY = ev.getRawY() - mLastY;
                if (distanceY > 0 && mPullEnable.canPullDown()
                        || distanceY > 0 && getFooterBottomMargin() > -mFooterHeight
                        || distanceY < 0 && getHeaderTopMargin() > -mHeaderHeight
                        || distanceY < 0 && mPullEnable.canPullUp()) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
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
        invalidate();
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
        invalidate();
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

    public PullListener getPullListener() {
        return mPullListener;
    }

    public void setPullListener(PullListener pullListener) {
        this.mPullListener = pullListener;
    }

    protected void onPullDown(View header, float radio, boolean isSliding) {
    }

    protected void onPullUp(View header, float radio, boolean isSliding) {
    }
}
