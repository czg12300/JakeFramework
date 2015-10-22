
package cn.common.config;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2015/10/22 18:06
 */
public class BaseAppConFig {

    protected static boolean isDebug = false;

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setIsDebug(boolean isDebug) {
        BaseAppConFig.isDebug = isDebug;
    }
}
