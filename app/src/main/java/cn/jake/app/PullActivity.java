
package cn.jake.app;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.common.ui.adapter.BaseListAdapter;
import cn.common.ui.widgt.pulltorefresh.BasePullListView;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2015/10/23 16:40
 */
public class PullActivity extends CommonTitleActivity {

    private PullListView mPullListView;

    private MyAdapter mMyAdapter;

    @Override
    protected void initView() {
        setTitle("下拉刷新");
        mPullListView = new PullListView(this);
        mMyAdapter = new MyAdapter(this, getData());
        setContentView(mPullListView);
        mPullListView.getListView().setAdapter(mMyAdapter);
    }

    private List<String> getData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("加快该撒娇干撒");
        }
        return list;
    }

    private class PullListView extends BasePullListView {
        public PullListView(Context context) {
            super(context);
        }

        @Override
        protected void handleRefreshing(View header) {
        }

        @Override
        protected void handleLoading(View footer) {
        }
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
