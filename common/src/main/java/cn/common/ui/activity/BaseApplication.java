
package cn.common.ui.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Process;

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by jakechen on 2015/8/5.
 */
public abstract class BaseApplication extends Application {
    public static BaseApplication mInstance;

    private HashMap<String, WeakReference<Activity>> mActivityMap;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        onRelease();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mActivityMap = new HashMap<String, WeakReference<Activity>>();
        onConfig();
    }

    protected abstract void onConfig();

    protected abstract void onRelease();

    public void addActivity(Activity activity) {
        if (activity != null) {
            mActivityMap.put(activity.getClass().getSimpleName(),
                    new WeakReference<Activity>(activity));
        }
    }

    public void removeActivity(String activityName) {
        if (mActivityMap != null && mActivityMap.containsKey(activityName)) {
            mActivityMap.remove(activityName);
        }
    }

    public void exitApp() {
        if (mActivityMap != null && mActivityMap.size() > 0) {
            for (String key : mActivityMap.keySet()) {
                if (mActivityMap.get(key) != null) {
                    mActivityMap.get(key).get().finish();
                }
            }
        }
        mActivityMap.clear();
        android.os.Process.killProcess(Process.myPid());
        System.exit(0);
    }

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }
}
