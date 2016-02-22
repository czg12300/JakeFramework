
package cn.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.common.ui.activity.BaseApplication;

/**
 * Created by jakechen on 2015/8/11.
 */
public class CommonUtil {
    public static void hideSoftInput(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
        if (isOpen) {
            View view = activity.getWindow().peekDecorView();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showSoftInput(final Activity activity) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                InputMethodManager m = (InputMethodManager) activity
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                boolean isOpen = m.isActive();// isOpen若返回true，则表示输入法打开
                if (isOpen) {
                    m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }

        }, 500);
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public static int getAppVersion() {
        try {
            return BaseApplication.getInstance().getPackageManager().getPackageInfo(BaseApplication.getInstance().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static boolean isListAvailable(List<?> list) {
        return list != null && list.size() > 0;
    }

    public static boolean isSameObject(Object obj1, Object obj2) {
        return obj1 == obj2;
    }
}
