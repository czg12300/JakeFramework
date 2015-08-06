
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
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * has under line's tab
 * 
 * @author jake
 */
public class IndicatorView extends LinearLayout
        implements View.OnClickListener, OnFocusChangeListener {

    private int mSelectTabColor;

    /**
     * position under line srcoll location
     */
    private int mCurrentScroll = 0;

    /**
     * tab list
     */
    private List<String> mTabs;

    /**
     * depend on viewpager
     */
    private ViewPager mViewPager;

    /**
     * tab text color
     */
    private int mTextColor;

    private float mTextSize;

    private int mSelectedTab = 0;

    private final int BSSEEID = 0xffff0;;

    private int mCurrID = 0;

    private int mPerItemWidth = 0;

    private boolean isChangeTabColor = false;

    private List<TextView> tvTitles;

    /**
     * under line paint
     */
    private Paint mPaintUnderLineSelected;

    private Paint mPaintUnderLineNormal;

    /***
     * under lint path
     */
    private Path mPathNormal;

    private Path mPathSelected;

    /**
     * is under lint average with tab
     */
    private boolean mIsAverage = true;

    public IndicatorView(Context context) {
        this(context, null);

    }

    public IndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setOnFocusChangeListener(this);
        mSelectTabColor = Color.parseColor("#cccccc");
        mTextColor = Color.parseColor("#363636");
        initDraw(mSelectTabColor);
        tvTitles = new ArrayList<TextView>();
    }

    /**
     * Initialize draw objects
     */
    private void initDraw(int underLineColor) {
        mPaintUnderLineSelected = new Paint();
        mPaintUnderLineSelected.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintUnderLineSelected.setStrokeWidth(dip(1.5f));
        mPaintUnderLineSelected.setColor(underLineColor);
        mPaintUnderLineNormal = new Paint();
        mPaintUnderLineNormal.setColor(underLineColor);
        mPaintUnderLineNormal.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintUnderLineNormal.setStrokeWidth(dip(1.5f) / 2);
        mPathNormal = new Path();
        mPathSelected = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float xScroll = 0;
        if (mTabs.size() != 0) {
            mPerItemWidth = getWidth() / mTabs.size();
            int tabID = mSelectedTab;
            xScroll = (mCurrentScroll - ((tabID) * (getWidth() + mViewPager.getPageMargin())))
                    / mTabs.size();
        } else {
            mPerItemWidth = getWidth();
            xScroll = mCurrentScroll;
        }
        if (tvTitles.size() > 1) {
            float xLeft = 0;
            float xRight = 0;
            xLeft = mSelectedTab * mPerItemWidth + xScroll;
            xRight = (mSelectedTab + 1) * mPerItemWidth + xScroll;
            if (!mIsAverage) {
                TextView tvTitle = tvTitles.get(mSelectedTab);
                float tabTextWidth = tvTitle.getPaint().measureText(tvTitle.getText().toString());
                float tabWidth = xRight - xLeft;
                // math text to view padding
                float scanx = (tabWidth - tabTextWidth) / 2;
                xLeft += scanx;
                xRight -= scanx;
            }
            mPathNormal.reset();
            mPathSelected.reset();
            mPathNormal.moveTo(0, getHeight() - mPaintUnderLineNormal.getStrokeWidth());
            mPathNormal.lineTo(getWidth(), getHeight() - mPaintUnderLineNormal.getStrokeWidth());
            mPathSelected.moveTo(xLeft, getHeight() - mPaintUnderLineSelected.getStrokeWidth());
            mPathSelected.lineTo(xRight, getHeight() - mPaintUnderLineSelected.getStrokeWidth());
            canvas.drawPath(mPathNormal, mPaintUnderLineNormal);
            canvas.drawPath(mPathSelected, mPaintUnderLineSelected);
        }
    }

    public void onScrolled(int scroll) {
        mCurrentScroll = scroll;
        invalidate();
    }

    public synchronized void onSwitched(int position) {
        if (mSelectedTab == position) {
            return;
        }
        setCurrentTab(position);
        invalidate();
    }

    public void init(int startPos, List<String> tabs, ViewPager viewPager) {
        mViewPager = viewPager;
        mTabs = tabs;
        for (int i = 0; i < tabs.size(); i++) {
            add(tabs.get(i));
        }
        setCurrentTab(startPos);
        invalidate();
    }

    public void setTabs(List<String> tabs) {
        removeAllViews();
        tvTitles.clear();
        mCurrID = 0;
        mTabs = tabs;
        for (int i = 0; i < tabs.size(); i++) {
            add(tabs.get(i));
        }
        if (mSelectedTab >= tabs.size()) {
            mSelectedTab = 0;
        }
        invalidate();
    }

    protected void add(String label) {
        TextView tvTitle = new TextView(getContext());
        tvTitle.setTextColor(mTextColor);
        tvTitle.setGravity(Gravity.CENTER);
        if (mTextSize > 0) {
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }
        tvTitle.setText(label);
        tvTitle.setId(BSSEEID + (mCurrID++));
        tvTitle.setOnClickListener(this);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        lp.gravity = Gravity.CENTER_VERTICAL;
        addView(tvTitle, lp);
        tvTitles.add(tvTitle);
    }

    @Override
    public void onClick(View v) {
        int position = v.getId() - BSSEEID;
        setCurrentTab(position);
    }

    public int getTabCount() {
        int children = getChildCount();
        return children;
    }

    public synchronized void setCurrentTab(int index) {
        if (index < 0 || index >= getTabCount()) {
            return;
        }
        View oldTab = getChildAt(mSelectedTab);
        oldTab.setSelected(false);
        setTabTextColor(oldTab, false);
        mSelectedTab = index;
        View newTab = getChildAt(mSelectedTab);
        newTab.setSelected(true);
        setTabTextColor(newTab, true);
        mViewPager.setCurrentItem(mSelectedTab, false);
        invalidate();
    }

    private void setTabTextColor(View tab, boolean selected) {
        TextView tv = (TextView) tab;
        tv.setTextColor(selected ? mSelectTabColor : mTextColor);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == this && hasFocus && getTabCount() > 0) {
            getChildAt(mSelectedTab).requestFocus();
            return;
        }
        if (hasFocus) {
            int i = 0;
            int numTabs = getTabCount();
            while (i < numTabs) {
                if (getChildAt(i) == v) {
                    setCurrentTab(i);
                    break;
                }
                i++;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mCurrentScroll == 0 && mSelectedTab != 0) {
            mCurrentScroll = (getWidth() + mViewPager.getPageMargin()) * mSelectedTab;
        }
    }

    private float dip(float dip) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                getResources().getDisplayMetrics());
    }

    public boolean isAverage() {
        return mIsAverage;
    }

    public void setAverage(boolean isAverage) {
        mIsAverage = isAverage;
    }

    public void setTabSelectColor(int color) {
        mPaintUnderLineSelected.setColor(color);
        mSelectTabColor = color;
        invalidate();
    }

    public void setUnderLineColor(int color) {
        mPaintUnderLineNormal.setColor(color);
        invalidate();
    }

    public boolean isChangeTabColor() {
        return isChangeTabColor;
    }

    public void setChangeTabColor(boolean isChangeTabColor) {
        this.isChangeTabColor = isChangeTabColor;
        if (isChangeTabColor) {
            setCurrentTab(mSelectedTab);

        }
    }

    /**
     * set tab text color
     */
    public void setTextColor(int textColor) {
        mTextColor = textColor;
        invalidate();
    }

    /**
     * set tab text size
     * 
     * @param textSize
     */
    public void setTextSize(float textSize) {
        mTextSize = textSize;
    }

    /**
     * set under line height
     * 
     * @param footerLineHeight
     */
    public void setUndlerLineHeight(float footerLineHeight) {
        mPaintUnderLineSelected.setStrokeWidth(footerLineHeight);
        mPaintUnderLineNormal.setStrokeWidth(footerLineHeight / 2);
        invalidate();
    }
}
