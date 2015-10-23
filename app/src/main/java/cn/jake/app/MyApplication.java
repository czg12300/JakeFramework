
package cn.jake.app;

import cn.common.ui.activity.BaseApplication;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2015/10/23 17:12
 */
public class MyApplication extends BaseApplication {
    @Override
    protected void onConfig() {
    }

    @Override
    protected void onRelease() {
    }

    @Override
    protected BaseApplication getChildInstance() {
        return this;
    }
}
