
package cn.common.ui.widgt.indicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.common.utils.DisplayUtil;

/**
 * has under line's tab
 *
 * @author jake
 */
public class IndicatorView extends FrameLayout implements View.OnClickListener {
    class LineView extends View {
        private Paint selectPaint = new Paint();

        private Paint normalPaint = new Paint();
        private float xSelectLeft;
        private float xSelectRight;
        private int normalColor;
        private int selectColor;
        private boolean showNormal;
        private int normalHeight;

        public LineView(Context context) {
            super(context);
            selectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            normalPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }

        public void setNormalColor(int color) {
            normalColor = color;
        }

        public void setSelectColor(int color) {
            selectColor = color;
        }

        public void update(float xLeft, float xRight) {
            xSelectLeft = xLeft;
            xSelectRight = xRight;
            invalidate();
        }

        public void setShowNormal(boolean showNormal) {
            this.showNormal = showNormal;
        }

        public void setNormalHeight(int height) {
            normalHeight = height;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (normalHeight == 0) {
                normalHeight = 1;
            }
            selectPaint.setStrokeWidth(getHeight());
            selectPaint.setColor(selectColor);
            normalPaint.setColor(normalColor);
            selectPaint.setStrokeWidth(normalHeight);
            if (showNormal) {
                if (normalHeight > getHeight()) {
                    normalHeight = getHeight();
                }
                Path path = new Path();
                path.moveTo(0, getHeight() - normalHeight);
                path.lineTo(getWidth(), getHeight() - normalHeight);
                path.lineTo(getWidth(), getHeight());
                path.lineTo(0, getHeight());
                path.lineTo(0, getHeight() - normalHeight);
                canvas.drawPath(path, normalPaint);
            }
            Path path = new Path();
            path.moveTo(xSelectLeft, 0);
            path.lineTo(xSelectRight, 0);
            path.lineTo(xSelectRight, getHeight());
            path.lineTo(xSelectLeft, getHeight());
            path.lineTo(xSelectLeft, 0);
            canvas.drawPath(path, selectPaint);
        }
    }

    private int tabSelectColor;

    private int mCurrentScroll = 0;
    private List<String> mTabs;
    private ViewPager viewPager;
    private int textColor;

    private float textSize;

    private int mSelectedTab = 0;

    private final int BSSEEID = 0xffff0;

    private int mCurrID = 0;


    private boolean isChangeTabColor = false;

    private List<TextView> tvTitles;
    private boolean isAverage = true;
    //是否显示正常的线
    private LinearLayout tabLayout;
    private LineView lineView;
    private boolean isUpdataLine = true;

    public IndicatorView(Context context) {
        this(context, null);

    }

    public IndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        tabSelectColor = Color.parseColor("#cccccc");
        textColor = Color.parseColor("#363636");
        tvTitles = new ArrayList<>();
        tabLayout = new LinearLayout(context);
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);
        lineView = new LineView(context);
        addView(tabLayout, new LayoutParams(-1, -1));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -2);
        params.gravity = Gravity.BOTTOM;
        params.height = 2;
        addView(lineView, params);
    }

    private void updateLine() {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        float xScroll = 0;
        int selectLineWidth = width;
        if (mTabs != null && mTabs.size() != 0) {
            selectLineWidth = width / mTabs.size();
            int tabID = mSelectedTab;
            xScroll = (mCurrentScroll - ((tabID) * (getWidth() + viewPager.getPageMargin())))
                    / mTabs.size();
        }
        if (tvTitles.size() > 1) {
            float xLeft = mSelectedTab * selectLineWidth + xScroll;
            float xRight = (mSelectedTab + 1) * selectLineWidth + xScroll;
            if (!isAverage) {
                TextView tvTitle = tvTitles.get(mSelectedTab);
                float tabTextWidth = tvTitle.getPaint().measureText(tvTitle.getText().toString());
                float tabWidth = xRight - xLeft;
                float scanx = (tabWidth - tabTextWidth) / 2;
                xLeft += scanx;
                xRight -= scanx;
            }

            if (lineView != null) {
                lineView.update(xLeft- DisplayUtil.dip(5), xRight+DisplayUtil.dip(5));
            }
        }
    }


    private TextView createTab(String label) {
        TextView tvTitle = new TextView(getContext());
        tvTitle.setTextColor(textColor);
        tvTitle.setGravity(Gravity.CENTER);
        if (textSize > 0) {
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
        tvTitle.setText(label);
        tvTitle.setId(BSSEEID + (mCurrID++));
        tvTitle.setOnClickListener(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        lp.weight = 1;
        lp.gravity = Gravity.CENTER_VERTICAL;
        tvTitle.setLayoutParams(lp);
        tvTitles.add(tvTitle);
        return tvTitle;
    }

    @Override
    public void onClick(View v) {
        int position = v.getId() - BSSEEID;
        onPageSelected(position);
    }


    public void setAverage(boolean isAverage) {
        this.isAverage = isAverage;
    }

    public void setTabSelectColor(int color) {
        tabSelectColor = color;
        lineView.setSelectColor(color);
    }

    public void setLineColor(int color) {
        lineView.setNormalColor(color);
    }

    public boolean isChangeTabColor() {
        return isChangeTabColor;
    }

    public void setChangeTabColor(boolean isChangeTabColor) {
        this.isChangeTabColor = isChangeTabColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public void setSelectLineHeight(int selectLineHeight) {
        FrameLayout.LayoutParams params = (LayoutParams) lineView.getLayoutParams();
        params.height = selectLineHeight;
        lineView.setLayoutParams(params);
    }

    public void setNormalLineHeight(int normalLineHeight) {
        lineView.setNormalHeight(normalLineHeight);
    }


    public void setShowNormalLine(boolean show) {
        lineView.setShowNormal(show);
    }

    public void setLineMarginBottom(float lineMarginBottom) {
        FrameLayout.LayoutParams params = (LayoutParams) lineView.getLayoutParams();
        params.bottomMargin = (int) lineMarginBottom;
        lineView.setLayoutParams(params);
    }

    public void onPageScrolled(int scroll) {
        mCurrentScroll = scroll;
        updateLine();
    }

    public synchronized void onPageSelected(int position) {
        if (position < 0 || position >= getTabCount()) {
            return;
        }
        View oldTab = tabLayout.getChildAt(mSelectedTab);
        oldTab.setSelected(false);
        ((TextView) oldTab).setTextColor(textColor);
        mSelectedTab = position;
        View newTab = tabLayout.getChildAt(mSelectedTab);
        newTab.setSelected(true);
        ((TextView) newTab).setTextColor(tabSelectColor);
        viewPager.setCurrentItem(mSelectedTab, false);
        updateLine();
    }


    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    public void setTabList(List<String> tabs) {
        tvTitles.clear();
        mCurrID = 0;
        mTabs = tabs;
        for (int i = 0; i < tabs.size(); i++) {
            tabLayout.addView(createTab(tabs.get(i)));
        }
        tabLayout.requestLayout();
        requestLayout();
        if (mSelectedTab >= tabs.size()) {
            mSelectedTab = 0;
        }
        onPageSelected(0);
    }

    private int getTabCount() {
        int children = getChildCount();
        return children;
    }
}
