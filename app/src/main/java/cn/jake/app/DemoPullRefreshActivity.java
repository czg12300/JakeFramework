package cn.jake.app;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.common.ui.adapter.BaseListAdapter;
import cn.common.ui.widgt.pull.PullDragHelper;
import cn.common.ui.widgt.pull.PullEnableListView;
import cn.common.ui.widgt.pull.PullListener;
import cn.common.ui.widgt.pull.PullToRefreshLayout;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2015/10/23 16:40
 */
public class DemoPullRefreshActivity extends CommonTitleActivity {

  private PullToRefreshLayout mPullToRefreshLayout;

  private MyAdapter mMyAdapter;

  @Override
  protected void initView() {
    setTitle("下拉刷新, 上拉加载");
    mPullToRefreshLayout = new PullToRefreshLayout(this);
    ListView listView = new PullEnableListView(this);
    mPullToRefreshLayout.setContentView(listView);
    mMyAdapter = new MyAdapter(this, getData());
    setContentView(mPullToRefreshLayout);
    listView.setAdapter(mMyAdapter);
  }

  @Override
  protected void initEvent() {
    super.initEvent();
    mPullToRefreshLayout.setPullListener(new PullListener() {
      @Override
      public void onLoadMore(PullDragHelper pullDragHelper) {
        mMyAdapter.addAll(getData());
        mMyAdapter.notifyDataSetChanged();
        pullDragHelper.finishTask(true);
      }

      @Override
      public void onRefresh(PullDragHelper pullDragHelper) {
        mMyAdapter.setData(getData());
        mMyAdapter.notifyDataSetChanged();
        pullDragHelper.finishTask(true);
      }
    });
  }

  private List<String> getData() {
    List<String> list = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      list.add("测试数据");
    }
    return list;
  }

  private class MyAdapter extends BaseListAdapter<String> {
    public MyAdapter(Context context, List<String> list) {
      super(context, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView tv;
      if (convertView == null) {
        tv = new TextView(getContext());
        tv.setPadding(40, 40, 0, 40);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        tv.setTextColor(Color.parseColor("#363636"));
        convertView = tv;
      } else {
        tv = (TextView) convertView;
      }
      tv.setText(mDataList.get(position));
      return convertView;
    }
  }
}
