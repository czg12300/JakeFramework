
package cn.common.ui.widgt.pull;

/**
 * 描述:上下拉监听
 *
 * @author jakechen
 * @since 2015/9/16 18:05
 */
public interface PullListener {
    void onLoadMore(PullDragHelper pullDragHelper);

    void onRefresh(PullDragHelper pullDragHelper);
}
