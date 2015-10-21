package cn.common.http.base;

import android.text.TextUtils;

import java.util.HashMap;

import cn.common.http.AjaxParams;
import cn.common.http.HttpManager;

/**
 * base of http request
 *
 * @author jakechen
 * @since 2015/9/21 16:33
 */
public abstract class BaseRequest<T> {
  private String mSvc;

  private HashMap<String, String> mParams;

  private Class<?> mClazz;

  private HttpManager<T> mHttpManager;

  public BaseRequest(String svc, Class<?> clazz) {
    mSvc = svc;
    mClazz = clazz;
    mParams = new HashMap<String, String>();
    addCommonParam();
    mHttpManager = new HttpManager<T>(mClazz);
  }

  protected abstract void addCommonParam();

  public void addParam(String key, String value) {
    mParams.put(key, value);
  }

  public void addParams(HashMap<String, String> params) {
    if (params != null && params.size() > 0) {
      mParams.putAll(params);
    }
  }

  public void setParams(HashMap<String, String> params) {
    if (params != null && params.size() > 0) {
      mParams = params;
    }
  }

  /**
   * 开始请求
   */
  public T sendRequest() {
    return mHttpManager.post(getUrl(), getParams());
  }

  private String getUrl() {
    if (!TextUtils.isEmpty(mSvc) && mSvc.startsWith("http")) {
      return mSvc;
    }
    return getServerUrl() + mSvc;
  }

  protected abstract String getServerUrl();

  private AjaxParams getParams() {
    return new AjaxParams(mParams);
  }

  /**
   * 取消请求
   */
  public synchronized void cancelRequest() {
    mHttpManager.cancelRequest();
  }
}