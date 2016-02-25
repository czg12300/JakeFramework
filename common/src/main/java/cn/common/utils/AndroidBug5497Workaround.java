package cn.common.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import cn.common.ui.widgt.ChangeThemeUtils;

public class AndroidBug5497Workaround {

    // For more information, see https://code.google.com/p/android/issues/detail?id=5497  
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.  

    public static void assistActivity(Activity activity, boolean isAdjustStatusBar) {
        new AndroidBug5497Workaround(activity, isAdjustStatusBar);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;
    private Activity mActivity;
    private boolean isAdjustStatusBar = false;

    private AndroidBug5497Workaround(final Activity activity, boolean isAdjustStatusBar) {
        mActivity = activity;
        this.isAdjustStatusBar = isAdjustStatusBar;
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible  
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            } else {
                // keyboard probably just became hidden  
                frameLayoutParams.height = usableHeightSansKeyboard;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        if (isAdjustStatusBar && Build.VERSION.SDK_INT >= 19) {
            return (r.bottom - r.top + ChangeThemeUtils.getStatusBarHeight(mActivity));
        }
        return (r.bottom - r.top);
    }

}  