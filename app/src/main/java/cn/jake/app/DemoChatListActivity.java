package cn.jake.app;

import cn.common.ui.widgt.ChatListView;

/**
 * 描述:聊天列表
 *
 * @author jakechen
 * @since 2016/2/19 10:41
 */
public class DemoChatListActivity extends CommonTitleActivity{
  @Override
  protected void initView() {
    setTitle("聊天列表");
    setContentView(R.layout.activity_chat);
    ChatListView chatListView= (ChatListView) findViewById(R.id.lv_chat);
    chatListView.setAdapter(new TestListAdapter(this));
  }
}
