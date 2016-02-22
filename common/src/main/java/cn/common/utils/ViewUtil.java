
package cn.common.utils;

import android.view.View;
import android.widget.TextView;

/**
 * 描述：view 的工具类 作者：jake on 2016/1/17 11:05
 */
public final class ViewUtil {
    private ViewUtil() {
    }

    ;

    public static void setText2TextView(TextView tv, CharSequence sequence) {
        if (tv != null && sequence != null) {
            tv.setText(sequence);
        }
    }

    public static void setViewVisibility(View view, int visible) {
        if (view != null) {
            if (view.getVisibility() != visible) {
                view.setVisibility(visible);
            }
        }
    }
}
