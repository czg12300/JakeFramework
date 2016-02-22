
package cn.common.utils;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import cn.common.ui.activity.BaseApplication;

/**
 * 描述：用于显示toast
 *
 * @author Created by jakechen on 2015/8/11.
 */
public class BaseToastUtil {
    protected static Toast mToast;
    protected static String mMsg;

    public static void show(int stringId) {
        show(BaseApplication.getInstance().getString(stringId));
    }

    public static void show(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (!TextUtils.equals(msg, mMsg)) {
            mToast = Toast.makeText(BaseApplication.getInstance().getContext(), msg, Toast.LENGTH_SHORT);
            mMsg = msg;
        }
        mToast.show();
    }
}
