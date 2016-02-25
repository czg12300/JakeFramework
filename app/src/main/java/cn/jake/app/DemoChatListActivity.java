package cn.jake.app;

import cn.common.ui.widgt.pull.PullDragHelper;
import cn.common.ui.widgt.pull.PullEnableListView;
import cn.common.ui.widgt.pull.PullListener;
import cn.common.ui.widgt.pull.PullToRefreshLayout;
import cn.common.utils.CommonUtil;

/**
 * 描述:聊天列表
 *
 * @author jakechen
 * @since 2016/2/19 10:41
 */
public class DemoChatListActivity extends CommonTitleActivity {
  TestListAdapter mTestListAdapter;
  private PullEnableListView listView;
  private PullToRefreshLayout layout;

  @Override
  protected void initView() {
    setTitle("聊天列表");
    setContentView(R.layout.activity_chat);
    layout = (PullToRefreshLayout) findViewById(R.id.pull);
    listView = new PullEnableListView(this);
    layout.setContentView(listView);
//    ListView chatListView= (ListView) findViewById(R.id.lv_chat)
    layout.setPullListener(new PullListener() {
      @Override
      public void onLoadMore(PullDragHelper pullDragHelper) {
        CommonUtil.showSoftInput(DemoChatListActivity.this);
        pullDragHelper.finishTask(false);
      }

      @Override
      public void onRefresh(PullDragHelper pullDragHelper) {
        mTestListAdapter.addAll(0, TestListAdapter.getData());
        mTestListAdapter.notifyDataSetChanged();
        listView.setSelectionFromTop(11, layout.getRefreshHeight());
        pullDragHelper.finishTask(false);
      }
    });
    mTestListAdapter = new TestListAdapter(this);
    listView.setAdapter(new TestListAdapter(this));
  }
}
