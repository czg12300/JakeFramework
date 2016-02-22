package cn.common.ui.widgt.banner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import cn.common.utils.DisplayUtil;

public class DotView extends View {

    /**
     * 当前选择下标
     */
    private int mIndex;
    /**
     * 总页数
     */
    private int mPageCount;
    /**
     * 默认画笔
     */
    private Paint normalPaint;
    /**
     * 选中画笔
     */
    private Paint selectPaint;

    /**
     * 左边距
     */
    private int margin = 10;

    private int mRadius;

    public int getRadius() {
        return mRadius;
    }

    public int getMargin() {
        return margin;
    }

    public void setRadius(int mRadius) {
        this.mRadius = mRadius;
    }

    public DotView(Context context) {
        super(context);
        mIndex = 0;
        mRadius = DisplayUtil.dip(5);
        selectPaint = new Paint();
        selectPaint.setAntiAlias(true);
        selectPaint.setColor(Color.WHITE);
        normalPaint = new Paint();
        normalPaint.setAntiAlias(true);
        normalPaint.setColor(Color.GRAY);

    }

    public void setNormalColor(int color) {
        normalPaint.setColor(color);
    }

    public void setSelectColor(int color) {
        selectPaint.setColor(color);
    }

    /**
     * 设置当前选择下标
     *
     * @param index
     */
    public void selectPosition(int index) {
        this.mIndex = index;
        invalidate();
    }

    /**
     * 设置总页数
     *
     * @param count
     */
    public void setTotalCount(int count) {
        this.mPageCount = count;
    }

    /**
     * 获取总页数
     *
     * @return
     */
    public int getTotalCount() {
        return mPageCount;
    }

    /**
     * 设置左间隔
     *
     * @param margin
     */
    public void setMargin(int margin) {
        this.margin = margin;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int tempX = margin + mRadius * 2;

        if (mPageCount <= 1) {
            return;
        }

        int len = mPageCount;
        for (int i = 0; i < len; i++) {

            canvas.drawCircle(i * tempX + mRadius, mRadius, mRadius, normalPaint);
        }
        canvas.drawCircle(mIndex * tempX + mRadius, mRadius, mRadius, selectPaint);
    }
}
