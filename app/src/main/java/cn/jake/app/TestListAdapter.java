package cn.jake.app;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.common.ui.adapter.BaseListAdapter;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2016/2/23 17:50
 */
public class TestListAdapter extends BaseListAdapter<String>{
    public TestListAdapter(Context context) {
      super(context,  getData());
    }
  public static List<String> getData() {
    List<String> list = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      list.add("测试数据"+new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(System.currentTimeMillis())));
    }
    return list;
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
