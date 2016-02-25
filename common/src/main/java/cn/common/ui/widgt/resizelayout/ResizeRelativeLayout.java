
package cn.common.ui.widgt.resizelayout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;

/**
 * 描述:自动适配软键盘弹出的layout
 *
 * @author jakechen
 * @since 2016/2/22 10:01
 */
public class ResizeRelativeLayout extends RelativeLayout implements
        ViewTreeObserver.OnGlobalLayoutListener {
    private Activity activity;

    private View mActivityContentView;

    private int usableHeightPrevious;

    private int height = 0;

    public ResizeRelativeLayout(Context context) {
        this(context, null);
    }

    public ResizeRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ResizeRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        activity = ((Activity) context);
        mActivityContentView = activity.findViewById(android.R.id.content);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mActivityContentView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mActivityContentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (height == 0) {
            height = getMeasuredHeight();
        }
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mActivityContentView.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = getSoftShowHeight(height, heightDifference);
                requestLayout();
                if (onResizeListener != null) {
                    onResizeListener.onKeyboardShow(heightDifference);
                }
            } else {
                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = height;
                requestLayout();
                if (onResizeListener != null) {
                    onResizeListener.onKeyboardHide();
                }
            }
            usableHeightPrevious = usableHeightNow;
        }
    }

    /**
     * 当软键盘显示时计算界面高度
     * 
     * @param usableHeightSansKeyboard
     * @return
     */
    private int getSoftShowHeight(int usableHeightSansKeyboard, int heightDifference) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return usableHeightSansKeyboard - heightDifference + getStatusBarHeight();
        }
        return usableHeightSansKeyboard - heightDifference;
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public int getStatusBarHeight() {
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object obj = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            int id = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources()
                .getDisplayMetrics());
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mActivityContentView.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

    @Override
    public void onGlobalLayout() {
        possiblyResizeChildOfContent();
    }

    private OnResizeListener onResizeListener;

    public void setOnResizeListener(OnResizeListener onResizeListener) {
        this.onResizeListener = onResizeListener;
    }

    public static interface OnResizeListener {

        /**
         * 软键盘关闭
         */
        void onKeyboardHide();

        /**
         * 软键盘高度改变
         */
        void onKeyboardShow(int height);
    }
}
