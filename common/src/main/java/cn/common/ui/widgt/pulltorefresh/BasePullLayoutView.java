
package cn.common.ui.widgt.pulltorefresh;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * description：base of pull layout view
 *
 * @author jakechen
 * @since 2015/9/16 18:11
 */
public abstract class BasePullLayoutView extends RelativeLayout implements Handler.Callback {
    private final static float OFFSET_RADIO = 1.8f;

    private static final int MSG_HEADER = 0;

    private static final int MSG_HEADER_REFRESH = 1;

    private static final int MSG_FOOTER = 2;

    private static final int MSG_FOOTER_LOAD = 3;

    private FrameLayout mFlHeader;

    private FrameLayout mFlFooter;
    private View mVContent;

    private PullEnable mPullEnable;

    private float mLastY;

    private PullListener mPullListener;

    private Timer mTimer;

    private Handler mHandler = new Handler(this);

    private int spitH;

    private int spitF;

    public BasePullLayoutView(Context context) {
        this(context, null);
    }

    public BasePullLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTimer = new Timer();
        mFlHeader = new FrameLayout(context);
        mFlHeader.setId(1234);
        mFlFooter = new FrameLayout(context);
        mFlFooter.setId(1235);
        LayoutParams rl = new LayoutParams(-1, -2);
        rl.addRule(ALIGN_PARENT_TOP);
        addView(mFlHeader, rl);
        mVContent = getContentView();
        if (mVContent instanceof PullEnable) {
            mPullEnable = (PullEnable) mVContent;
        } else {
            throw new IllegalArgumentException("content view is not instance of PullEnable");
        }
        rl = new LayoutParams(-1, -2);
        rl.addRule(ALIGN_PARENT_BOTTOM);
        addView(mFlFooter, rl);
        rl = new LayoutParams(-1, -1);
        rl.addRule(BELOW, mFlHeader.getId());
        rl.addRule(ABOVE, mFlFooter.getId());
        addView(mVContent, rl);
        getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        setPadding(getPaddingLeft(), -mFlHeader.getHeight(), getPaddingRight(),
                                0);
                        LayoutParams lp = (LayoutParams) mFlFooter.getLayoutParams();
                        lp.bottomMargin = -mFlFooter.getHeight();
                        mFlFooter.setLayoutParams(lp);
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

    private int pullDownY;

    private int pullUpY;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                if (deltaY > 0 && mPullEnable.canPullDown()) {
                    pullDownY = (int) (deltaY / OFFSET_RADIO) - mFlHeader.getHeight();
                    setPadding(getPaddingLeft(), pullDownY, getPaddingRight(), getPaddingBottom());
                } else if (deltaY < 0 && mPullEnable.canPullUp()) {
                    int offset = -(int) (deltaY / OFFSET_RADIO);
                    pullUpY = offset - mFlFooter.getHeight();
                    LayoutParams lp = (LayoutParams) mFlFooter.getLayoutParams();
                    lp.bottomMargin = pullUpY;
                    mFlFooter.setLayoutParams(lp);
//                    setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), pullUpY);
//                    LayoutParams lp = new LayoutParams(-1, -1);
//                    lp.bottomMargin = offset;
//                    mVContent.setLayoutParams(lp);
                }
                break;
            case MotionEvent.ACTION_UP:
                reset();
                break;
        }
        return true;
    }

    private void reset() {
        if (getPaddingTop() > 0) {
            hideHeader(true);
            handleRefreshing(mFlHeader);
        } else if (getPaddingTop() < 0 && getPaddingTop() > -mFlHeader.getHeight()) {
            hideHeader(false);
        }
        if (getPaddingBottom() > 0) {
            hideFooter(true);
            handleLoading(mFlFooter);
        } else if (getPaddingBottom() < 0 && getPaddingBottom() > -mFlFooter.getHeight()) {
            hideFooter(false);
        }

    }

    private void hideHeader(final boolean isToRefresh) {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isToRefresh) {
                    if (getPaddingTop() > 0) {
                        mHandler.sendEmptyMessage(MSG_HEADER_REFRESH);
                    } else {
                        cancel();
                    }
                } else {
                    if (getPaddingTop() > -mFlHeader.getHeight()) {
                        mHandler.sendEmptyMessage(MSG_HEADER);
                    } else {
                        cancel();
                    }
                }
            }
        }, 0, 5);
    }

    private void hideFooter(final boolean isToLoad) {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isToLoad) {
                    if (getPaddingBottom() > 0) {
                        mHandler.sendEmptyMessage(MSG_FOOTER_LOAD);
                    } else {
                        cancel();
                    }
                } else {
                    if (getPaddingBottom() > -mFlFooter.getHeight()) {
                        mHandler.sendEmptyMessage(MSG_FOOTER);
                    } else {
                        cancel();
                    }
                }
            }
        }, 0, 5);
    }

    protected abstract void handleRefreshing(View header);

    protected abstract void handleLoading(View footer);

    protected void handleRefreshAnimation(View header) {
    }

    protected void handleLoadAnimation(View footer) {
    }

    public void setHeaderView(View header) {
        mFlHeader.addView(header);
    }

    public void setFooterView(View footer) {
        mFlFooter.addView(footer);
    }

    protected abstract View getContentView();

    public PullListener getPullListener() {
        return mPullListener;
    }

    public void setPullListener(PullListener pullListener) {
        this.mPullListener = pullListener;
    }

    @Override
    public boolean handleMessage(Message msg) {
        spitH = (int) (8 + 5
                * Math.tan(Math.PI / 2 / getMeasuredHeight() * (pullDownY + Math.abs(pullDownY))));
        spitF = (int) (8
                + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (pullUpY + Math.abs(pullUpY))));
        int offsetH = getPaddingTop() - spitH;
        int offsetF = getPaddingBottom() - spitF;
        switch (msg.what) {
            case MSG_HEADER_REFRESH:
                if (offsetH > 0) {
                    setPadding(getPaddingLeft(), offsetH, getPaddingRight(), getPaddingBottom());
                } else {
                    setPadding(getPaddingLeft(), 0, getPaddingRight(), getPaddingBottom());
                }
                break;
            case MSG_HEADER:
                setPadding(getPaddingLeft(), offsetH, getPaddingRight(), getPaddingBottom());
                break;
            case MSG_FOOTER_LOAD:
                if (offsetF > 0) {
                    setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), offsetF);
                } else {
                    setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), offsetF);
                }
                break;
            case MSG_FOOTER:
                setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), offsetF);
                break;
        }
        return true;
    }
}
