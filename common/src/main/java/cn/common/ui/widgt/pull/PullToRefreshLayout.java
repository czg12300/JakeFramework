package cn.common.ui.widgt.pull;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import cn.common.ui.widgt.pulltorefresh.PullEnable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 自定义的布局，用来管理三个子控件，其中一个是下拉头，一个是包含内容的pullableView（可以是实现Pullable接口的的任何View），
 * 还有一个上拉头
 *
 * @author jake
 */
public abstract class PullToRefreshLayout extends RelativeLayout implements Handler.Callback {

  // 初始状态
  public static final int INIT = 0;

  // 释放刷新
  public static final int RELEASE_TO_REFRESH = 1;

  // 正在刷新
  public static final int REFRESHING = 2;

  // 释放加载
  public static final int RELEASE_TO_LOAD = 3;

  // 正在加载
  public static final int LOADING = 4;

  // 操作完毕
  public static final int DONE = 5;

  // 当前状态
  private int state = INIT;

  // 刷新回调接口
  private OnRefreshListener mListener;

  // 按下Y坐标，上一个事件点Y坐标
  private float mLastX, mLastY, mScrollY;
  /**
   * 方向-水平
   */
  private static final int ORIENTATION_HORIZONTAL = 1;

  /**
   * 方向-垂直
   */
  private static final int ORIENTATION_VERTICAL = 2;

  // 下拉的距离。注意：pullDownY和pullUpY不可能同时不为0
  public float pullDownY = 0;

  // 上拉的距离
  private float pullUpY = 0;

  // 释放刷新的距离
  private float refreshDist = 200;

  // 释放加载的距离
  private float loadmoreDist = 200;

  private Timer timer;

  // 回滚速度
  public float MOVE_SPEED = 8;

  // 在刷新过程中滑动操作
  private boolean isTouch = false;

  // 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化
  private float radio = 2;
  // 实现了Pullable接口的View

  // 过滤多点触碰
  private int mEvents;

  // 这两个变量用来控制pull的方向，如果不加控制，当情况满足可上拉又可下拉时没法下拉
  private boolean canPullDown = true;

  private boolean canPullUp = true;

  private FrameLayout mHeader;

  private FrameLayout mFooter;

  /**
   * 执行自动回滚的handler
   */
  private Handler updateHandler = new Handler(this);

  private int orientation;

  private View mVContent;

  private PullEnable mPullEnable;
  private TimerTask task;

  public void setOnRefreshListener(OnRefreshListener listener) {
    mListener = listener;
  }

  public PullToRefreshLayout(Context context) {
    this(context, null);
  }

  public PullToRefreshLayout(Context context, AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initView(context);
  }

  private void initView(Context context) {
    timer = new Timer();
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
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

      @Override
      public void onGlobalLayout() {
        refreshDist = mHeader.getHeight();
        loadmoreDist = mFooter.getHeight();
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

  private void hide() {
    if (task == null) {
      task = new TimerTask() {
        @Override
        public void run() {
          updateHandler.sendEmptyMessage(0);
        }
      };
    }
    timer.schedule(task, 0, 5);
  }

  /**
   * 不限制上拉或下拉
   */
  private void releasePull() {
    canPullDown = true;
    canPullUp = true;
  }

  private void cancelTask() {
    if (task != null) {
      task.cancel();
      task = new TimerTask() {
        @Override
        public void run() {
          updateHandler.sendEmptyMessage(0);
        }
      };
    }
  }

  /*
   * （非 Javadoc）由父控件决定是否分发事件，防止事件冲突
   * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
   */
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    switch (ev.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        mLastX = ev.getX();
        mLastY = ev.getY();
        mScrollY = ev.getY();
        cancelTask();
        mEvents = 0;
        releasePull();
        break;
      case MotionEvent.ACTION_POINTER_DOWN:
      case MotionEvent.ACTION_POINTER_UP:
        // 过滤多点触碰
        mEvents = -1;
        break;
      case MotionEvent.ACTION_MOVE:
        orientation = getMoveOrientation(ev);
        if (orientation == ORIENTATION_VERTICAL && mPullEnable.canPullDown() && canPullDown && state != LOADING && mEvents == 0) {
          // 可以下拉，正在加载时不能下拉
          // 对实际滑动距离做缩小，造成用力拉的感觉
          pullDownY = pullDownY + (ev.getY() - mScrollY) / radio;
          if (pullDownY < 0) {
            pullDownY = 0;
            canPullDown = false;
            canPullUp = true;
          }
          if (pullDownY > getMeasuredHeight()) pullDownY = getMeasuredHeight();
          if (state == REFRESHING) {
            // 正在刷新的时候触摸移动
            isTouch = true;
          }
          if (pullDownY <= refreshDist && (state == RELEASE_TO_REFRESH || state == DONE)) {
            // 如果下拉距离没达到刷新的距离且当前状态是释放刷新，改变状态为下拉刷新
          }
          if (pullDownY >= refreshDist && (state == INIT || state == DONE)) {
            // 如果下拉距离达到刷新的距离且当前状态是初始状态刷新，改变状态为释放刷新
          }
          if (pullDownY > 10) {
            // 防止下拉过程中误触发长按事件和点击事件
            ev.setAction(MotionEvent.ACTION_CANCEL);
          }
          mScrollY = ev.getY();
          // 根据下拉距离改变比例
          radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (pullDownY + Math.abs(pullUpY))));
          requestLayout();

        } else if (orientation == ORIENTATION_VERTICAL && mPullEnable.canPullUp() && canPullUp && state != REFRESHING && mEvents == 0) {
          // 可以上拉，正在刷新时不能上拉
          pullUpY = pullUpY + (ev.getY() - mScrollY) / radio;
          if (pullUpY > 0) {
            pullUpY = 0;
            canPullDown = true;
            canPullUp = false;
          }
          if (pullUpY < -getMeasuredHeight()) pullUpY = -getMeasuredHeight();
          if (state == LOADING) {
            // 正在加载的时候触摸移动
            isTouch = true;
          }
          // 下面是判断上拉加载的，同上，注意pullUpY是负值
          if (-pullUpY <= loadmoreDist && (state == RELEASE_TO_LOAD || state == DONE)) {
          }
          if (-pullUpY >= loadmoreDist && (state == INIT || state == DONE)) {
          }
          mScrollY = ev.getY();
          // 根据下拉距离改变比例
          radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (pullDownY + Math.abs(pullUpY))));
          // 因为刷新和加载操作不能同时进行，所以pullDownY和pullUpY不会同时不为0，因此这里用(pullDownY
          // +
          // Math.abs(pullUpY))就可以不对当前状态作区分了
          if (pullUpY < -10) {
            // 防止下拉过程中误触发长按事件和点击事件
            ev.setAction(MotionEvent.ACTION_CANCEL);
          }
          requestLayout();
        } else {
          mEvents++;
        }
        break;
      case MotionEvent.ACTION_UP:
        if (pullDownY > refreshDist || -pullUpY > loadmoreDist)
          // 正在刷新时往下拉（正在加载时往上拉），释放后下拉头（上拉头）不隐藏
          isTouch = false;
        if (state == RELEASE_TO_REFRESH) {
          // 刷新操作
          if (mListener != null) mListener.onRefresh();
        } else if (state == RELEASE_TO_LOAD) {
          // 加载操作
          if (mListener != null) mListener.onLoadMore();
        }
        hide();
      default:
        break;
    }
    // 事件分发交给父类
    super.dispatchTouchEvent(ev);
    return true;
  }

  private boolean isLayout = false;

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    // 改变子控件的布局，这里直接用(pullDownY + pullUpY)作为偏移量，这样就可以不对当前状态作区分
    mHeader.layout(0, (int) (pullDownY + pullUpY) - mHeader.getMeasuredHeight(), mHeader.getMeasuredWidth(), (int) (pullDownY + pullUpY));
    mVContent.layout(0, (int) (pullDownY + pullUpY), mVContent.getMeasuredWidth(), (int) (pullDownY + pullUpY) + mVContent.getMeasuredHeight());
    mFooter.layout(0, (int) (pullDownY + pullUpY) + mVContent.getMeasuredHeight(), mFooter.getMeasuredWidth(), (int) (pullDownY + pullUpY) + mVContent.getMeasuredHeight() + mFooter.getMeasuredHeight());
  }

  /**
   * 获取移动方向
   *
   * @param ev
   * @return
   */
  private int getMoveOrientation(MotionEvent ev) {
    int orientation;
    float distanceX = Math.abs(ev.getX() - mLastX);
    float distanceY = Math.abs(ev.getY() - mLastY);
    if (distanceX > distanceY) {
      // X轴
      orientation = ORIENTATION_HORIZONTAL;
    } else {
      // Y轴
      orientation = ORIENTATION_VERTICAL;
    }
    return orientation;
  }

  @Override
  public boolean handleMessage(Message msg) {
    // 回弹速度随下拉距离moveDeltaY增大而增大
    MOVE_SPEED = (float) (8 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (pullDownY + Math.abs(pullUpY))));
    if (!isTouch) {
      // 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."
      if (state == REFRESHING && pullDownY <= refreshDist) {
        pullDownY = refreshDist;
        cancelTask();
      } else if (state == LOADING && -pullUpY <= loadmoreDist) {
        pullUpY = -loadmoreDist;
        cancelTask();
      }

    }
    if (pullDownY > 0) pullDownY -= MOVE_SPEED;
    else if (pullUpY < 0) pullUpY += MOVE_SPEED;
    if (pullDownY < 0) {
      // 已完成回弹
      pullDownY = 0;
      // 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
      if (state != REFRESHING && state != LOADING) cancelTask();
    }
    if (pullUpY > 0) {
      // 已完成回弹
      pullUpY = 0;
      // 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
      if (state != REFRESHING && state != LOADING) cancelTask();
    }
    // 刷新布局,会自动调用onLayout
    requestLayout();
    return true;
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


  /**
   * 刷新加载回调接口
   *
   * @author chenjing
   */
  public interface OnRefreshListener {
    /**
     * 刷新操作
     */
    void onRefresh();

    /**
     * 加载操作
     */
    void onLoadMore();
  }

}
