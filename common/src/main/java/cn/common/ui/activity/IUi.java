
package cn.common.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import java.util.List;

public interface IUi {
    void setupBroadcastActions(List<String> actions);

    void handleBroadcast(Context context, Intent intent);

    void handleUiMessage(Message msg);

    void goActivity(Class<?> clazz);

    void goActivity(Class<?> clazz, Bundle bundle);

    void goActivityForResult(Class<?> clazz);

    void goActivityForResult(Class<?> clazz, Bundle bundle);

    void goActivityForResult(Class<?> clazz, Bundle bundle, int requestCode);
}
