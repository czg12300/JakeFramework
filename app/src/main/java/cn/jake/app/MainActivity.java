
package cn.jake.app;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.common.ui.adapter.BaseListAdapter;

public class MainActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setListAdapter(new MyAdapter(this, getActivityList()));
//        onListItemClick(getListView(), null, 0, 0);
    }

    private List<Info> getActivityList() {
        List<Info> list = new ArrayList<Info>();
        list.add(new Info("下拉刷新", DemoPullRefreshActivity.class));
        list.add(new Info("聊天列表", DemoChatListActivity.class));
        return list;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Info info = (Info) l.getAdapter().getItem(position);
        startActivity(new Intent(this, info.clazz));
    }

    class Info {
        String label;

        Class<?> clazz;

        public Info(String label, Class<?> clazz) {
            this.label = label;
            this.clazz = clazz;
        }
    }

    class MyAdapter extends BaseListAdapter<Info> {

        public MyAdapter(Context context, List<Info> list) {
            super(context, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = new TextView(getContext());
                tv.setPadding(0, 40, 0, 40);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                tv.setTextColor(Color.parseColor("#363636"));
                convertView = tv;
            } else {
                tv = (TextView) convertView;
            }
            tv.setText(mDataList.get(position).label);
            return convertView;
        }
    }
}
