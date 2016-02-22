
package cn.common.ui.widgt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ListAdapter;

import cn.common.R;

/**
 * 横向滚动的gridview
 */
public class HorizontalScrollGridView extends HorizontalScrollView {

    private GridView gridView;

    /**
     * 设置显示多少行
     */
    private int numLines = 1;

    /**
     * 设置每个列的宽度，ps：必须设置，否则无法正常显示
     */
    private int columnWidth;

    /**
     * 设置横向的itme之间的分割距离
     */
    private int horizontalSpacing;

    /**
     * 设置纵向的itme之间的分割距离
     */
    private int verticalSpacing;

    public void setNumLines(int numLines) {
        this.numLines = numLines;
    }

    public int getNumLines() {
        return numLines;
    }

    public int getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
        gridView.setColumnWidth(columnWidth);
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
        gridView.setHorizontalSpacing(horizontalSpacing);
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        gridView.setVerticalSpacing(verticalSpacing);
        this.verticalSpacing = verticalSpacing;

    }

    public HorizontalScrollGridView(Context context) {
        this(context, null);
    }

    public HorizontalScrollGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_horzontal_scroll_grid_view, this);
        gridView = (GridView) findViewById(R.id.gv_content);
    }

    public void setAdapter(ListAdapter adapter) {
        if (adapter == null) {
            return;
        }
        gridView.setAdapter(adapter);
        if (adapter.getCount() > 0) {
            notifyDataSetChanged();
        }
    }

    /**
     * 更新adapter数据的时候需要调用这个方法
     */
    public void notifyDataSetChanged() {
        if (gridView == null && gridView.getAdapter() == null) {
            return;
        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        if (columnWidth == 0) {
            params.width = getResources().getDisplayMetrics().widthPixels;
        } else {
            params.width = gridView.getAdapter().getCount() / numLines
                    * (columnWidth + horizontalSpacing);
        }
        gridView.setLayoutParams(params);
        gridView.setNumColumns(gridView.getAdapter().getCount() / numLines);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        gridView.setOnItemClickListener(listener);
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        gridView.setOnItemLongClickListener(listener);
    }

    /***
     * 解决与viewpager的冲突,当拦截触摸事件到达此位置的时候，返回true
     */
    private float xDistance, yDistance, xLast, yLast;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                if (gridView.getWidth() > xLast) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;
                if (xDistance > yDistance && canScroll() ) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            default:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (canScroll()) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }
    }

    private boolean canScroll() {
        return gridView.getWidth() > getWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heighSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heighSpec);
    }

}
