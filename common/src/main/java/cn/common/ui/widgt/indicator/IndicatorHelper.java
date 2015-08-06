
package cn.common.ui.widgt.indicator;

/**
 * Indicator的持有类
 * 
 * @author libin
 */
public class IndicatorHelper {
    // public class IndicatorHelper implements OnPageChangeListener {
    //
    // public static final String EXTRA_TAB = "tab";
    //
    // private Activity context;
    //
    // // private IndicatorView titleIndicator;
    // private ViewPagerCompat viewPagerCompat;
    //
    // // 存放选项卡信息的列表
    // protected ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    //
    // private ViewPagerAdapter myAdapter;
    //
    // private onViewPageChangeListener viewPageChangeListener;
    //
    // /**
    // * 当前选中tab
    // */
    // private int mCurrentTab;
    //
    // public int getmCurrentTab() {
    // return mCurrentTab;
    // }
    //
    // public IndicatorHelper(Activity context) {
    // this.context = context;
    // initObj();
    // }
    //
    // /**
    // * 初始化控�?
    // *
    // * @param pagerIndicatorId
    // * @param viewPageId
    // */
    // public void setContrlView(View view, int pagerIndicatorId, int
    // viewPageId) {
    // titleIndicator = (IndicatorView) view.findViewById(pagerIndicatorId);
    // viewPagerCompat = (ViewPagerCompat) view.findViewById(viewPageId);
    // if (isInitialized()) {
    // myAdapter = new ViewPagerAdapter();
    // viewPagerCompat.setAdapter(myAdapter);
    // viewPagerCompat.setOnPageChangeListener(this);
    // titleIndicator.init(mCurrentTab, mTabs, viewPagerCompat);
    // }
    // }
    //
    // private void initObj() {
    // Intent intent = context.getIntent();
    // if (intent != null) {
    // mCurrentTab = intent.getIntExtra(EXTRA_TAB, mCurrentTab);
    // }
    // }
    //
    // /**
    // * 跳转到任意�?�项�?
    // *
    // * @param tabId
    // * 选项卡下�?
    // */
    // public void navigate(int tabId) {
    // for (int index = 0, count = mTabs.size(); index < count; index++) {
    // if (mTabs.get(index).getId() == tabId) {
    // viewPagerCompat.setCurrentItem(index, false);
    // }
    // }
    // }
    //
    // /**
    // * 添加�?个�?�项�?
    // *
    // * @param tab
    // */
    // public void addTabInfo(TabInfo tab) {
    // if (isInitialized()) {
    // mTabs.add(tab);
    // myAdapter.addView(tab.view);
    // titleIndicator.setTabs(mTabs);
    // myAdapter.notifyDataSetChanged();
    // } else {
    // throw new NullPointerException(IndicatorHelper.class.getName() +
    // "：addTabInfo Error,对象没有初始�?");
    // }
    // }
    //
    // /**
    // * 判断控件是否初始�?
    // */
    // private boolean isInitialized() {
    // if (titleIndicator != null && viewPagerCompat != null) {
    // return true;
    // }
    // return false;
    // }
    //
    // /**
    // * 从列表添加�?�项�?
    // *
    // * @param tabs
    // */
    // public void addTabInfos(ArrayList<TabInfo> tabs) {
    // mTabs.addAll(tabs);
    // myAdapter.notifyDataSetChanged();
    // }
    //
    // /*
    // * 设置tab的颜色一起是否改�?
    // */
    // public void setChangeTabColor(boolean isChangeTabColor) {
    // titleIndicator.setChangeTabColor(isChangeTabColor);
    //
    // }
    //
    // /*
    // * 设置下划线的高度
    // */
    // public void setFooterLineHeight(int heightOfDip) {
    // titleIndicator.setFooterLineHeight(heightOfDip);
    // }
    //
    // /*
    // * 设置下划线的颜色
    // */
    // public void setFootLineColor(int color) {
    // titleIndicator.setFootLineColor(color);
    // }
    //
    // /*
    // * 设置tab的字体颜�?
    // */
    // public void setTabTextColor(int color) {
    // titleIndicator.setTextColor(color);
    // }
    //
    //
    // @Override
    // public void onPageScrolled(int position, float positionOffset, int
    // positionOffsetPixels) {
    // titleIndicator.onScrolled((viewPagerCompat.getWidth() +
    // viewPagerCompat.getPageMargin()) * position + positionOffsetPixels);
    // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    // switchAnmation(position, positionOffset);
    // }
    // }
    //
    // /**
    // * 左右滑动渐变动画
    // *
    // * @param position
    // * @param positionOffset
    // */
    // @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    // private void switchAnmation(int position, float positionOffset) {
    // if (position < myAdapter.mListViews.size() - 1) {
    // myAdapter.mListViews.get(position + 1).setAlpha(positionOffset);
    // }
    // myAdapter.mListViews.get(position).setAlpha(1 - positionOffset);
    //
    // }
    //
    // @Override
    // public void onPageSelected(int position) {
    // if (viewPageChangeListener != null) {
    // viewPageChangeListener.onPageSelected(position);
    // }
    // titleIndicator.onSwitched(position);
    // mCurrentTab = position;
    // }
    //
    // @Override
    // public void onPageScrollStateChanged(int state) {
    // if (state == ViewPager.SCROLL_STATE_IDLE) {
    // }
    // }
    //
    // /**
    // * 是否在最左边
    // */
    // public boolean isLeftMost() {
    // if (myAdapter != null && viewPagerCompat != null) {
    // if (viewPagerCompat.getCurrentItem() == 0 && myAdapter.getCount() > 1) {
    // return true;
    // }
    // }
    // return false;
    // }
    //
    // /**
    // * 是否在最右边
    // *
    // * @return
    // */
    // public boolean isRightMost() {
    // if (myAdapter != null && viewPagerCompat != null) {
    // if (viewPagerCompat.getCurrentItem() == myAdapter.getCount() - 1) {
    // return true;
    // }
    // }
    // return false;
    // }
    //
    // public interface onViewPageChangeListener {
    // public void onPageSelected(int position);
    // }
    //
    // public void setViewPageChangeListener(onViewPageChangeListener
    // viewPageChangeListener) {
    // this.viewPageChangeListener = viewPageChangeListener;
    // }
    //
    // /**
    // * 设置指示背景
    // *
    // * @param resId
    // */
    // public void setTitleIndicatorBg(int resId) {
    // titleIndicator.setBackgroundResource(resId);
    // }
    //
    // public void setTabTextSize(float textSize) {
    // titleIndicator.setTextSize(textSize);
    // }
    //
    // public void setFootLineNormalColor(int color) {
    // titleIndicator.setFootLineNormalColor(color);
    // }
    //
}
