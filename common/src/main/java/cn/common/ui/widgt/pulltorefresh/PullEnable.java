
package cn.common.ui.widgt.pulltorefresh;

/**
 * can pull or not
 *
 * @author jakechen
 * @since 2015/9/17 10:33
 */
public interface PullEnable {
    boolean canPullUp();

    boolean canPullDown();
}
