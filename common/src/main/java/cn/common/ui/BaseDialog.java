
package cn.common.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

/**
 * 描述：dialog的父类 Created by jakechen on 2015/8/28.
 */
public class BaseDialog extends Dialog {

    public static final int ANIMATION_ALPHA_IN_OUT = 0;

    public static final int ANIMATION_UP_TO_DOWN = 1;

    public static final int ANIMATION_LEFT_TO_RIGHT = 2;

    public static final int ANIMATION_DOWN_TO_UP = 3;

    public static final int ANIMATION_RIGHT_TO_LEFT = 4;

    private static final int DEFAULT_INT = -1;

    private Window window = null;

    public BaseDialog(Context context) {
        super(context);
        window = getWindow(); // 得到对话框
    }

    // 设置窗口显示
    public void setWindow(int animStyle, float showDimAmount) {
        setWindow(DEFAULT_INT, DEFAULT_INT, animStyle, showDimAmount);
    }

    public void setWindow(int x, int y) {
        setWindow(x, y, DEFAULT_INT);
    }

    public void setWindow(int x, int y, int animStyle) {
        setWindow(x, y, animStyle, 1.0f);
    }

    public void setWindow(int x, int y, float showDimAmount) {
        setWindow(x, y, DEFAULT_INT, showDimAmount);
    }

    // 设置窗口显示
    public void setWindow(int x, int y, int animStyle, float showDimAmount) {
        if (animStyle != DEFAULT_INT) {
            window.setWindowAnimations(animStyle); // 设置窗口弹出动画
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        // //设置对话框背景为透明
        WindowManager.LayoutParams lp = window.getAttributes();
        // 根据x，y坐标设置窗口需要显示的位置
        if (x != DEFAULT_INT) {
            lp.x = x;
        }
        if (y != DEFAULT_INT) {
            lp.y = y;
        }
        lp.dimAmount = showDimAmount;
        // wl.alpha = 0.6f; //设置透明度
        // wl.gravity = Gravity.BOTTOM; //设置重力
        // window.setAttributes(lp);
        // hasSetWindow = true;
    }

    Handler handler = new Handler();

    public void dismissDelayed(long delayMillis) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, delayMillis);
    }

}
