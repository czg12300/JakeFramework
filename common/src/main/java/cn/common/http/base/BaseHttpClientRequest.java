
package cn.common.http.base;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import cn.common.config.BaseAppConFig;
import cn.common.exception.AppException;
import cn.common.utils.LogUtil;
import cn.common.utils.UrlEncodeUtil;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2015/10/22 18:33
 */
public abstract class BaseHttpClientRequest<T> {
    public static final int CONNECT_TIME_OUT = 6 * 1000;

    public static final int READ_TIME_OUT = 6 * 1000;

    private final static String TAG = "HttpClient";

    // 最大重试次数
    private final static int MAX_RETRY_NUM = 2;

    private String mSvc;

    protected Hashtable<String, String> mParams;

    private Class<?> mClazz;

    private boolean isGet = false;

    public boolean isGet() {
        return isGet;
    }

    public void setIsGet(boolean isGet) {
        this.isGet = isGet;
    }

    public BaseHttpClientRequest(String svc, Class<?> clazz) {
        mSvc = svc;
        mClazz = clazz;
        mParams = new Hashtable<String, String>();
        addCommonParam();
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

    public void setParams(Hashtable<String, String> params) {
        if (params != null && params.size() > 0) {
            mParams = params;
        }
    }

    private String getUrl() {
        if (!TextUtils.isEmpty(mSvc) && mSvc.startsWith("http")) {
            return mSvc;
        }
        return getServerUrl() + mSvc;
    }

    protected abstract String getServerUrl();

    /**
     * 发起网络请求，并读取服务器返回的数据
     *
     * @throws AppException
     */
    public T request() throws AppException {
        return request(true);
    }

    /**
     * 发起网络请求，并读取服务器返回的数据
     *
     * @param isRetry 是否重试
     * @throws AppException
     */
    public T request(boolean isRetry) throws AppException {
        return requestWithoutCheck(isRetry);
    }

    public T requestWithoutCheck(boolean isRetry) throws AppException {
        HttpClient httpClient = null;
        HttpUriRequest uriReq = null;
        int tryNum = 0;
        do {
            try {
                httpClient = createHttpClient();
                uriReq = createHttpUriRequest();
                HttpResponse httpResponse = httpClient.execute(uriReq);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED
                        && statusCode != HttpStatus.SC_NO_CONTENT
                        && statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
                    throw AppException.http(statusCode);
                }
                String result = EntityUtils.toString(
                        new BufferedHttpEntity(httpResponse.getEntity()), HTTP.UTF_8);
                LogUtil.d("response", result);
                if (mClazz != null) {
                    BaseResponse response = (BaseResponse) mClazz.newInstance();
                    if (response != null) {
                        return (T) response.parse(result);
                    }
                }
                break;
            } catch (Exception e) {
                tryNum++;
                if (tryNum < MAX_RETRY_NUM) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                    continue;
                }
                // 发生网络异常
                e.printStackTrace();
                throw AppException.network(e);
            } finally {
                // 释放连接
                if (httpClient != null) {
                    // 释放连接
                    httpClient.getConnectionManager().shutdown();
                }
                httpClient = null;
            }
        } while (isRetry && tryNum < MAX_RETRY_NUM);
        return null;
    }

    // 创建一个HttpClient对象
    private HttpClient createHttpClient() {
        HttpClient client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();
        httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        // 设置 连接超时时间
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECT_TIME_OUT);
        // 设置 读数据超时时间
        HttpConnectionParams.setSoTimeout(httpParams, READ_TIME_OUT);
        return client;
    }

    // 创建一个HttpUriRequest对象
    private HttpUriRequest createHttpUriRequest() throws AppException {
        String prevUrl = getUrl();
        String params = getGetRequestParams();
        if (BaseAppConFig.isDebug()) {
            Log.d(TAG, prevUrl + params);
        }
        HttpUriRequest uriReq = null;
        if (isGet) {
            uriReq = new HttpGet(prevUrl + params);
        } else {
            uriReq = new HttpPost(prevUrl);
            ((HttpPost) uriReq).setEntity(getPostRequestEntity());
        }
        // 设置头
        Hashtable<String, String> headers = getRequestHeaders();
        if (headers == null) {
            headers = new Hashtable<String, String>();
        }
        if (!headers.containsKey("Accept-Language")) {
            headers.put("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        }
        if (!headers.containsKey("Content-Type")) {
            headers.put("Content-Type", "application/x-www-form-urlencoded");
        }
        for (String key : headers.keySet()) {
            if (!"Connection".equalsIgnoreCase(key) && !"User-Agent".equalsIgnoreCase(key)) {
                // 屏弊自定义Connection和User-Agent
                uriReq.setHeader(key, headers.get(key));
            }
        }
        uriReq.setHeader("Connection", "Keep-Alive");
        uriReq.setHeader("User-Agent", getUserAgent());
        LogUtil.d("request", uriReq.getURI().toString());
        return uriReq;
    }

    protected abstract Hashtable<String, String> getRequestHeaders();

    protected HttpEntity getPostRequestEntity() {
        List<BasicNameValuePair> fParams = new ArrayList<BasicNameValuePair>();
        final Set<String> keys = mParams.keySet();
        for (String key : keys) {
            fParams.add(new BasicNameValuePair(key, mParams.get(key)));
        }
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(fParams, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return entity;
    }

    protected String getGetRequestParams() {
        if (mParams != null && mParams.size() >= 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("?");
            final Set<String> keys = mParams.keySet();
            for (String key : keys) {
                builder.append(key).append("=").append(mParams.get(key)).append("&");
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        }
        return null;
    }

    private String sUserAgent;

    public String getUserAgent() {
        if (TextUtils.isEmpty(sUserAgent)) {
            StringBuilder ua = new StringBuilder();
            // 平台
            ua.append("Android");
            // 系统版本
            ua.append("/" + android.os.Build.VERSION.RELEASE);
            // 手机型号
            ua.append("/" + android.os.Build.MODEL);
            sUserAgent = UrlEncodeUtil.encode(ua.toString());
        }
        return sUserAgent;
    }

}
