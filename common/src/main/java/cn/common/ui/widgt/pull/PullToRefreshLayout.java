package cn.common.ui.widgt.pull;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.common.R;
import cn.common.utils.ViewUtil;

public class PullToRefreshLayout extends RelativeLayout implements PullDragListener {
  private View vRefresh;

  private View vLoadMore;
  // private ImageView ivRefresh;
  //
  // private ImageView ivLoadMore;

  private ProgressBar pbRefresh;

  private ProgressBar pbLoadMore;

  private TextView tvRefresh;

  private TextView tvLoadMore;

  private FrameLayout flContent;

  private PullDragHelper pullDragHelper;

  // 用于改变下拉是的布局
  private int pullDistance = 0;
  // private RotateAnimation rotateAnimation;

  // private RotateAnimation refreshingAnimation;
  public PullToRefreshLayout(Context context) {
    this(context, null);
  }

  public PullToRefreshLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    inflate(context, R.layout.view_pull_layout, this);
    vRefresh = findViewById(R.id.rl_refresh);
    vLoadMore = findViewById(R.id.rl_load_more);
    // ivRefresh = (ImageView) findViewById(R.id.iv_refresh_arrow);
    // ivLoadMore = (ImageView) findViewById(R.id.iv_load_arrow);
    pbRefresh = (ProgressBar) findViewById(R.id.pb_refresh_loading);
    pbLoadMore = (ProgressBar) findViewById(R.id.pb_load_loading);
    tvRefresh = (TextView) findViewById(R.id.tv_refresh_tip);
    tvLoadMore = (TextView) findViewById(R.id.tv_load_tip);
    flContent = (FrameLayout) findViewById(R.id.fl_content);
    // rotateAnimation = new RotateAnimation(0, 180, 0.5f, 0.5f);
    // rotateAnimation.setDuration(1500);
    // rotateAnimation.setRepeatCount(0);
  }

  public void setContentView(View view) {
    if (view instanceof PullEnable) {
      flContent.addView(view, new FrameLayout.LayoutParams(-1, -1));
      pullDragHelper = new PullDragHelper((PullEnable) view);
      pullDragHelper.setIsMultiTouch(true);
      pullDragHelper.setPullDragListener(this);
    }
  }

  public void setPullListener(PullListener pullListener) {
    if (pullListener != null && pullDragHelper != null) {
      pullDragHelper.setPullListener(pullListener);
    }
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (pullDragHelper != null) {
      pullDragHelper.onTouch(ev);
    }
    // 事件分发交给父类
    super.dispatchTouchEvent(ev);
    return true;
  }

  @Override
  public void changeView(float pullDownY, float pullUpY) {
    pullDistance = (int) (pullDownY + pullUpY);
    requestLayout();
  }

  public int getRefreshHeight() {
    return vRefresh.getHeight();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    vRefresh.layout(0, pullDistance - vRefresh.getMeasuredHeight(), vRefresh.getMeasuredWidth(), pullDistance);
    flContent.layout(0, pullDistance, flContent.getMeasuredWidth(), pullDistance + flContent.getMeasuredHeight());
    vLoadMore.layout(0, pullDistance + flContent.getMeasuredHeight(), vLoadMore.getMeasuredWidth(), pullDistance + flContent.getMeasuredHeight() + vLoadMore.getMeasuredHeight());
  }


  @Override
  public void changeState(int state, boolean loadSuccess) {
    switch (state) {
      case INIT:
        // 下拉布局初始状态
        // 上拉布局初始状态
        tvRefresh.setText("下拉刷新");
        tvLoadMore.setText("上拉加载更多");
        break;
      case RELEASE_TO_REFRESH:
        // 释放刷新状态
        tvRefresh.setText("释放刷新");
        // ivRefresh.startAnimation(rotateAnimation);
        break;
      case REFRESHING:
        // 正在刷新状态
        tvRefresh.setText("正在刷新");
        ViewUtil.setViewVisibility(pbRefresh, VISIBLE);
        break;
      case FINISH_REFRESH:
        ViewUtil.setViewVisibility(pbRefresh, GONE);
        // 加载完毕
        if (loadSuccess) {
          tvRefresh.setText("刷新成功");
        } else {
          tvRefresh.setText("刷新失败");
        }
        break;
      case RELEASE_TO_LOAD:
        // 释放加载状态
        tvLoadMore.setText("释放加载");
        break;
      case LOADING:
        // 正在加载状态
        tvLoadMore.setText("正在加载");
        ViewUtil.setViewVisibility(pbLoadMore, VISIBLE);
        break;
      case FINISH_LOAD:
        // 加载完毕
        ViewUtil.setViewVisibility(pbLoadMore, GONE);
        if (loadSuccess) {
          tvLoadMore.setText("加载成功");
        } else {
          tvLoadMore.setText("加载失败");
        }
        break;
    }
  }

  @Override
  public float getViewHeight() {
    return getMeasuredHeight();
  }

  @Override
  public float getRefreshViewHeight() {
    return vRefresh.getHeight();
  }

  @Override
  public float getLoadMoreViewHeight() {
    return vLoadMore.getHeight();
  }

  public void finishTask(boolean showLoadSuccess) {
    if (pullDragHelper != null) {
      pullDragHelper.finishTask(showLoadSuccess);
    }
  }

  public void setResultShowTime(long time) {
    if (pullDragHelper != null) {
      pullDragHelper.setResultShowTime(time);
    }
  }

}
