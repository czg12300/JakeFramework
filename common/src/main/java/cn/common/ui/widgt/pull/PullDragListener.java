package cn.common.ui.widgt.pull;

public interface PullDragListener {
    int INIT = 0;
    int RELEASE_TO_REFRESH = 1;
    int REFRESHING = 2;
    int RELEASE_TO_LOAD = 3;
    int LOADING = 4;
    int FINISH_REFRESH = 5;
    int FINISH_LOAD = 6;

    void changeView(float pullDownY, float pullUpY);

    void changeState(int state,boolean loadSuccess);

    float getViewHeight();

    float getRefreshViewHeight();

    float getLoadMoreViewHeight();
}
