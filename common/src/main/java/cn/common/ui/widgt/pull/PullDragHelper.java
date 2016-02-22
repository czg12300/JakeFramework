
package cn.common.ui.widgt.pull;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

public class PullDragHelper implements Handler.Callback {

    class MyTimer {
        private Handler handler;

        private Timer timer;

        private MyTask mTask;

        public MyTimer(Handler handler) {
            this.handler = handler;
            timer = new Timer();
        }

        public void schedule(long delay) {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
            mTask = new MyTask(handler);
            timer.schedule(mTask, delay, 5);
        }

        public void cancel() {
            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }
        }

        class MyTask extends TimerTask {
            private Handler handler;

            public MyTask(Handler handler) {
                this.handler = handler;
            }

            @Override
            public void run() {
                handler.obtainMessage().sendToTarget();
            }

        }
    }

    // 回滚速度
    public float MOVE_SPEED = 8;

    private int state = PullDragListener.INIT;

    private PullEnable mPullEnable;

    private float downY, lastY;

    // 过滤多点触碰
    private int mEvents;

    private float pullDownY = 0;

    private float pullUpY = 0;

    private boolean isMultiTouch = false;

    private boolean canPullDown = true;

    private boolean canPullUp = true;

    private float radio = 2;

    private boolean isTouch = false;

    // 释放刷新的距离
    private float refreshDist = 200;

    // 释放加载的距离
    private float loadMoreDist = 200;

    private MyTimer timer;

    private PullDragListener pullDragListener;

    private PullListener pullListener;

    private long resultShowTime = 0;

    private Handler updateHandler = new Handler(this);

    public PullDragHelper(PullEnable pullEnable) {
        mPullEnable = pullEnable;
        timer = new MyTimer(updateHandler);
    }

    private void changeState(int to, boolean loadSuccess) {
        state = to;
        if (pullDragListener != null) {
            pullDragListener.changeState(state, loadSuccess);
        }
    }

    private void changeState(int to) {
        changeState(to, false);
    }

    public void onTouch(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                lastY = downY;
                mEvents = 0;
                releasePull();
                if (pullDragListener != null) {
                    refreshDist = pullDragListener.getRefreshViewHeight();
                    loadMoreDist = pullDragListener.getLoadMoreViewHeight();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                if (isMultiTouch) {
                    mEvents = -1;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float height = 0;
                if (pullDragListener != null) {
                    height = pullDragListener.getViewHeight();
                }
                if (mEvents == 0) {
                    if (mPullEnable.canPullDown() && canPullDown
                            && state != pullDragListener.LOADING) {
                        // 可以下拉，正在加载时不能下拉
                        // 对实际滑动距离做缩小，造成用力拉的感觉
                        pullDownY = pullDownY + (ev.getY() - lastY) / radio;
                        if (pullDownY < 0) {
                            pullDownY = 0;
                            canPullDown = false;
                            canPullUp = true;
                        }
                        if (pullDownY > height)
                            pullDownY = height;
                        if (state == PullDragListener.REFRESHING) {
                            // 正在刷新的时候触摸移动
                            isTouch = true;
                        }
                    } else if (mPullEnable.canPullUp() && canPullUp
                            && state != PullDragListener.REFRESHING) {
                        // 可以上拉，正在刷新时不能上拉
                        pullUpY = pullUpY + (ev.getY() - lastY) / radio;
                        if (pullUpY > 0) {
                            pullUpY = 0;
                            canPullDown = true;
                            canPullUp = false;
                        }
                        if (pullUpY < -height)
                            pullUpY = -height;
                        if (state == PullDragListener.LOADING) {
                            // 正在加载的时候触摸移动
                            isTouch = true;
                        }
                    } else {
                        releasePull();
                    }
                } else {
                    mEvents = 0;
                }
                lastY = ev.getY();
                // 根据下拉距离改变比例
                radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / height
                        * (pullDownY + Math.abs(pullUpY))));
                pullDragListener.changeView(pullDownY, pullUpY);
                if (pullDownY < refreshDist && state == PullDragListener.RELEASE_TO_REFRESH) {
                    // 如果下拉距离没达到刷新的距离且当前状态是释放刷新，改变状态为下拉刷新
                    changeState(PullDragListener.INIT);
                }
                if (pullDownY > refreshDist && state == PullDragListener.INIT) {
                    // 如果下拉距离达到刷新的距离且当前状态是初始状态刷新，改变状态为释放刷新
                    changeState(PullDragListener.RELEASE_TO_REFRESH, false);
                }
                // 下面是判断上拉加载的，同上，注意pullUpY是负值
                if (-pullUpY < loadMoreDist && state == PullDragListener.RELEASE_TO_LOAD) {
                    changeState(PullDragListener.INIT);
                }
                if (-pullUpY > loadMoreDist && state == PullDragListener.INIT) {
                    changeState(PullDragListener.RELEASE_TO_LOAD);
                }
                // 因为刷新和加载操作不能同时进行，所以pullDownY和pullUpY不会同时不为0，因此这里用(pullDownY +
                // Math.abs(pullUpY))就可以不对当前状态作区分了
                if ((pullDownY + Math.abs(pullUpY)) > 8) {
                    // 防止下拉过程中误触发长按事件和点击事件
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (pullDownY > refreshDist || -pullUpY > loadMoreDist)
                    // 正在刷新时往下拉（正在加载时往上拉），释放后下拉头（上拉头）不隐藏
                    isTouch = false;
                if (state == PullDragListener.RELEASE_TO_REFRESH) {
                    changeState(PullDragListener.REFRESHING);
                    // 刷新操作
                    if (pullListener != null) {
                        pullListener.onRefresh(this);
                    }
                } else if (state == PullDragListener.RELEASE_TO_LOAD) {
                    changeState(PullDragListener.LOADING);
                    // 加载操作
                    if (pullListener != null) {
                        pullListener.onLoadMore(this);
                    }
                }
                hide(0);
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        // 回弹速度随下拉距离moveDeltaY增大而增大
        float height = 0;
        if (pullDragListener != null) {
            height = pullDragListener.getViewHeight();
        }
        MOVE_SPEED = (float) (8 + 5 * Math.tan(Math.PI / 2 / height
                * (pullDownY + Math.abs(pullUpY))));
        if (!isTouch) {
            // 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."
            if (state == PullDragListener.REFRESHING && pullDownY <= refreshDist) {
                pullDownY = refreshDist;
                timer.cancel();
            } else if (state == PullDragListener.LOADING && -pullUpY <= loadMoreDist) {
                pullUpY = -loadMoreDist;
                timer.cancel();
            }

        }
        if (pullDownY > 0)
            pullDownY -= MOVE_SPEED;
        else if (pullUpY < 0)
            pullUpY += MOVE_SPEED;
        if (pullDownY < 0) {
            // 已完成回弹
            pullDownY = 0;
            // 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
            if (state != PullDragListener.REFRESHING && state != PullDragListener.LOADING)
                changeState(PullDragListener.INIT);
            timer.cancel();
        }
        if (pullUpY > 0) {
            // 已完成回弹
            pullUpY = 0;
            // 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
            if (state != PullDragListener.REFRESHING && state != PullDragListener.LOADING)
                changeState(PullDragListener.INIT);
            timer.cancel();
        }
        // 刷新布局,会自动调用onLayout
        pullDragListener.changeView(pullDownY, pullUpY);
        return true;
    }

    private void hide(long delay) {
        if (timer != null) {
            timer.schedule(delay);
        }
    }

    private void releasePull() {
        canPullDown = true;
        canPullUp = true;
    }

    public boolean isMultiTouch() {
        return isMultiTouch;
    }

    public void setIsMultiTouch(boolean isMultiTouch) {
        this.isMultiTouch = isMultiTouch;
    }

    public PullDragListener getPullDragListener() {
        return pullDragListener;
    }

    public void setPullDragListener(PullDragListener pullDragListener) {
        this.pullDragListener = pullDragListener;
    }

    public PullListener getPullListener() {
        return pullListener;
    }

    public void setPullListener(PullListener pullListener) {
        this.pullListener = pullListener;
    }

    /**
     * 完成刷新或者加载
     */
    public void finishTask(boolean loadSuccess) {
        if (state == PullDragListener.LOADING) {
            changeState(PullDragListener.FINISH_LOAD, loadSuccess);
        } else if (state == PullDragListener.REFRESHING) {
            changeState(PullDragListener.FINISH_REFRESH, loadSuccess);
        }
        hide(resultShowTime);
    }

    public void setResultShowTime(long resultShowTime) {
        this.resultShowTime = resultShowTime;
    }
}
