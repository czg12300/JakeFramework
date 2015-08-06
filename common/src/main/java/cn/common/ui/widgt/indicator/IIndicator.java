
package cn.common.ui.widgt.indicator;

import android.support.v4.view.PagerAdapter;

import java.util.List;

public interface IIndicator {
    List<String> getLabelList();

    PagerAdapter getAdapter();
}
