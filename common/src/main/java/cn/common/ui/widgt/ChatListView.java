package cn.common.ui.widgt;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;

/**
 * 描述:聊天列表
 *
 * @author jakechen
 * @since 2016/2/23 16:07
 */
public class ChatListView extends ListView {
  private final static float OFFSET_RADIO = 1.8f;

  private final static int SCROLL_DURATION = 200;
  private LinearLayout lHeader;
  private LinearLayout lFooter;
  private View header;
  private View footer;
  private Scroller mScroller;
  private boolean isScrollHeader = false;
  private boolean canPullDown = false;
  private boolean canPullUp = false;
  private float mLastY;
  private int headerHeight;
  private int footerHeight;
  private boolean mPullLoading = false;

  private boolean mPullRefreshing = false;

  public ChatListView(Context context) {
    this(context, null);
  }

  public ChatListView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ChatListView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    lHeader = createLinearLayout(context);
    lFooter = createLinearLayout(context);
    header = new View(context);
    footer = new View(context);
    lHeader.addView(header);
    lFooter.addView(footer);
    super.addHeaderView(lHeader);
    super.addFooterView(lFooter);
    mScroller = new Scroller(context, new DecelerateInterpolator());
    // init header height
    ViewTreeObserver observer = lHeader.getViewTreeObserver();
    if (null != observer) {
      observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @SuppressWarnings("deprecation")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onGlobalLayout() {
          headerHeight = lHeader.getHeight();
          footerHeight = lFooter.getHeight();
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
  }

  private LinearLayout createLinearLayout(Context context) {
    LinearLayout lHeader = new LinearLayout(context);
    lHeader.setOrientation(LinearLayout.VERTICAL);
    return lHeader;
  }


  @Override
  public void addHeaderView(View v) {
    lHeader.addView(v);
  }

  @Deprecated
  public void addHeaderView(View v, Object data, boolean isSelectable) {
  }

  @Override
  public void addFooterView(View v) {
    lFooter.addView(v);
  }

  @Deprecated
  public void addFooterView(View v, Object data, boolean isSelectable) {
    super.addFooterView(v, data, isSelectable);
  }


  @Override
  public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      if (isScrollHeader) {
        changeHeader(mScroller.getCurrY());
      } else {
        changeFooter(mScroller.getCurrY());
      }
      postInvalidate();
    }
    super.computeScroll();

  }

  private void changeHeader(int marginTop) {
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) header.getLayoutParams();
    params.topMargin = marginTop - headerHeight;
    lHeader.setLayoutParams(params);
  }

  private void changeFooter(int marginBottom) {
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) footer.getLayoutParams();
    params.bottomMargin = marginBottom - footerHeight;
    lFooter.setLayoutParams(params);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (mLastY == -1) {
      mLastY = ev.getRawY();
    }
    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mLastY = ev.getRawY();
        break;
      case MotionEvent.ACTION_MOVE:
        final float deltaY = ev.getRawY() - mLastY;
        mLastY = ev.getRawY();
        if (canPullDown && getFirstVisiblePosition() == 0 && (lHeader.getHeight() > 0 || deltaY > 0)) {
          // the first item is showing, header has shown or pull down.
          changeHeader((int) (deltaY / OFFSET_RADIO));

        } else if (canPullUp && getLastVisiblePosition() == getCount() - 1 && (lFooter.getHeight() > 0 || deltaY < 0)) {
          // last item, already pulled up or want to pull up.
          changeFooter((int) (-deltaY / OFFSET_RADIO));
        }
        break;
      default:
        // reset
        mLastY = -1;
        if (canPullDown && getFirstVisiblePosition() == 0) {
          // invoke refresh
          if (lHeader.getHeight() > headerHeight) {
            mPullRefreshing = true;
            if (listener != null) {
              listener.pullTop(this);
            }
          } else {
            resetHeaderHeight();
          }

        } else if (canPullUp && getLastVisiblePosition() == getCount() - 1) {
          // invoke load more.
          if (lFooter.getHeight() > footerHeight) {
            mPullLoading = true;
            if (listener != null) {
              listener.pullBottom();
            }
          } else {
            resetFooterHeight();
          }
        }
        break;
    }
    return super.onTouchEvent(ev);
  }

  public void resetFooterHeight() {
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) footer.getLayoutParams();
    int bottomMargin = params.bottomMargin;
    if (bottomMargin > 0) {
      isScrollHeader = false;
      mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
      invalidate();
    }
  }

  public void resetHeaderHeight() {
    int height = lHeader.getHeight();
    if (height == 0) return;
    // default: scroll back to dismiss header.
    int finalHeight = 0;
    // is refreshing, just scroll back to show all the header.
    if (mPullRefreshing && height > headerHeight) {
      finalHeight = headerHeight;
    }
    isScrollHeader = true;
    mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
    // trigger computeScroll
    invalidate();
  }

  private IListener listener;

  public static interface IListener {
    void pullBottom();

    void pullTop(ChatListView listView);

  }

  void setPullListener(IListener listener) {
    this.listener = listener;
  }


}
