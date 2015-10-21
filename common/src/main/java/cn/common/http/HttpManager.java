
package cn.common.http;

import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.SocketException;

import cn.common.http.exception.HttpExcHandler;
import cn.common.http.exception.HttpException;

/**
 * http接口请求中心
 *
 * @author jake
 */
public class HttpManager<T> {
    private boolean isCancelRequest;

    public static enum Method {
        GET, POST, DELETE
    }

    private Class<?> mClazz;

    public HttpManager(Class<?> clazz) {
        mClazz = clazz;
        isCancelRequest = false;
    }

    /**
     * 取消请求
     */
    public synchronized void cancelRequest() {
        isCancelRequest = true;
    }

    /**
     * post数据请求方式
     */
    public T post(String url, AjaxParams params) {
        return getDataFromWeb(url, params, Method.POST);
    }

    /**
     * get数据请求方式
     */
    public void get(String url, AjaxParams params) {
        getDataFromWeb(url, params, Method.GET);
    }

    /**
     * 处理请求
     */
    private T getDataFromWeb(String url, AjaxParams params, Method method) {
        try {
            String result = getData(getRequest(url, params, method));
            if (!isCancelRequest) {
                return parseResult(result);
            }
        } catch (HttpException e) {
            e.printStackTrace();
            final String errorMsg = e.getMessage();
            final int errorCode = e.getErrorCode();
            if (errorCode == HttpReturnCode.HTTP_RESPONSE_ERROR_CODE
                    || errorCode == HttpReturnCode.HTTP_NO_HOST_NAME) {
                // URLs.nextServer();
            }
        }
        return null;
    }

    /**
     * 解析json
     *
     * @throws HttpException
     */
    private T parseResult(String result) throws HttpException {
        try {
            JsonParse parse = (JsonParse) mClazz.newInstance();
            return (T) parse.parse(result);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取请求方式
     */
    private HttpUriRequest getRequest(String url, AjaxParams params, Method method) {
        HttpUriRequest request = null;
        switch (method) {
            case GET:
                url = getUrlWithQueryString(url, params);
                request = new HttpGet(url);
                break;
            case POST:
                request = new HttpPost(url);
                ((HttpPost) request).setEntity(params.getEntity());
                break;
            case DELETE:
                url = getUrlWithQueryString(url, params);
                request = new HttpDelete(url);
                // L.i("DELETE:" + url);
                break;
            default:
                break;
        }
        return request;
    }

    /**
     * 拼接成http请求
     */
    public String getUrlWithQueryString(String url, AjaxParams params) {
        if (null != params) {
            String paramString = params.getParamString();
            if (url.endsWith("&")) {
                url += paramString;
            } else {
                url += "?" + paramString;
            }
        }
        return url;
    }

    /**
     * 获取网络数据
     */
    private String getData(final HttpUriRequest request) throws HttpException {
        org.apache.http.HttpResponse httpResponse = getHttpResponse(request);
        int status = httpResponse.getStatusLine().getStatusCode();
        String result = null;
        try {
            if (status == HttpStatus.SC_OK) {
                result = EntityUtils.toString(new BufferedHttpEntity(httpResponse.getEntity()),
                        HTTP.UTF_8);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw HttpExcHandler.hasIOException();
        } catch (IOException e) {
            e.printStackTrace();
            // e.printStackTrace();
            throw HttpExcHandler.hasIOException();
        }
        if (result != null && result.startsWith("\ufeff")) {
            return result.substring(1);
        }
        if (status != HttpStatus.SC_OK) {
            throw HttpExcHandler.getCustomException(status, result);
        } else {
        }
        return result;
    }

    /**
     * 获取http的回复
     */
    private org.apache.http.HttpResponse getHttpResponse(HttpUriRequest request)
            throws HttpException {
        org.apache.http.HttpResponse httpResponse = null;
        try {
            httpResponse = createHttpClient().execute(request);
        } catch (ConnectTimeoutException e) {
            // e.printStackTrace();
            throw HttpExcHandler.responseTimeOut();
        } catch (SocketException e) {
            // e.printStackTrace();
            throw HttpExcHandler.responseTimeOut();
        } catch (NoHttpResponseException e) {
            // e.printStackTrace();
            throw HttpExcHandler.noHttpResponse();
        } catch (IOException e) {
            // e.printStackTrace();
            throw HttpExcHandler.hasIOException();
        } catch (IllegalArgumentException e) {
            // e.printStackTrace();
            throw HttpExcHandler.IllegalArgumentException();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpResponse;
    }

    // 创建一个HttpClient对象
    private static HttpClient createHttpClient() {
        HttpClient client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();
        httpParams.setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        // 设置 连接超时时间
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        // 设置 读数据超时时间
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
        return client;
    }

}
