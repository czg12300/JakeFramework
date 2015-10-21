package cn.common.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;

/**
 * 单例的httpclient
 *
 * @author jake
 */
public final class SingleHttpClient {
    private DefaultHttpClient mHttpClient;
    /**
     * http请求最大并发连接数
     */
    private static int MAX_CONNECTIONS = 10;
    /**
     * 超时时间，默认30秒
     */
    private static int SOCKET_TIMEOUT = 30 * 1000;

    /**
     * 默认重试次数
     */
    private static final int DEFAULT_MAX_RETRIES = 5;

    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    private static final String ENCODING_GZIP = "gzip";
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 10 * 1024 * 1024;

    private SingleHttpClient() {
        initHttpClient();
    }

    private static SingleHttpClient mInstance;

    public static synchronized SingleHttpClient getInstance() {
        if (mInstance == null) {
            mInstance = new SingleHttpClient();
        }
        return mInstance;
    }

    public synchronized DefaultHttpClient getHttpClient() {
        return mHttpClient;
    }

    /**
     * 初始化httpclient
     */
    private void initHttpClient() {
        mHttpClient = getDefaultHttpClient();
        // mHttpClient = new DefaultHttpClient();
        // 添加拦截器 在Http消息发出前，对HttpRequest request做些处理。比如加头等
        mHttpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) {
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
                request.addHeader("Accept", "application/json");
            }
        });
        mHttpClient.addResponseInterceptor(new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse response, HttpContext context) {
                final HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return;
                }
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            break;
                        }
                    }
                }
            }
        });
        // 设置连接重试
        mHttpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_MAX_RETRIES));
    }

    /**
     * 生成一个httpclient
     */
    private DefaultHttpClient getDefaultHttpClient() {
        final HttpParams httpParams = new BasicHttpParams();
        // 设置在请求中携带由服务器返回的Cookie
        httpParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        // timeout: get connections from connection pool
        ConnManagerParams.setTimeout(httpParams, SOCKET_TIMEOUT);
        // timeout: connect to the server
        HttpConnectionParams.setConnectionTimeout(httpParams, SOCKET_TIMEOUT);
        // timeout: transfer data from server
        HttpConnectionParams.setSoTimeout(httpParams, SOCKET_TIMEOUT);

        // set max connections per host
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(MAX_CONNECTIONS));
        // set max total connections
        ConnManagerParams.setMaxTotalConnections(httpParams, MAX_CONNECTIONS);

        // use expect-continue handshake
        HttpProtocolParams.setUseExpectContinue(httpParams, true);
        // disable stale check
        HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);

//		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

        HttpClientParams.setRedirecting(httpParams, false);
        // set user agent
        String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
        HttpProtocolParams.setUserAgent(httpParams, userAgent);
        // disable Nagle algorithm
        HttpConnectionParams.setTcpNoDelay(httpParams, true);

        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);
        // scheme: http and https
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager manager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
        return new DefaultHttpClient(manager, httpParams);
    }

    /**
     * GZIP 解压
     */
    private static class InflatingEntity extends HttpEntityWrapper {

        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

    /**
     * httpclient连接重试
     */
    private static class RetryHandler implements HttpRequestRetryHandler {

        private static final int RETRY_SLEEP_TIME_MILLIS = 1500;
        private static HashSet<Class<?>> exceptionWhitelist = new HashSet<Class<?>>();
        private static HashSet<Class<?>> exceptionBlacklist = new HashSet<Class<?>>();

        static {
            // Retry if the server dropped connection on us
            exceptionWhitelist.add(NoHttpResponseException.class);
            // retry-this, since it may happens as part of a Wi-Fi to 3G
            // failover
            exceptionWhitelist.add(UnknownHostException.class);
            // retry-this, since it may happens as part of a Wi-Fi to 3G
            // failover
            exceptionWhitelist.add(SocketException.class);

            // never retry timeouts
            exceptionBlacklist.add(InterruptedIOException.class);
            // never retry SSL handshake failures
            exceptionBlacklist.add(SSLHandshakeException.class);
        }

        private final int maxRetries;

        public RetryHandler(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            boolean retry = true;

            Boolean b = (Boolean) context.getAttribute(ExecutionContext.HTTP_REQ_SENT);
            boolean sent = (b != null && b.booleanValue());

            if (executionCount > maxRetries) {
                // Do not retry if over max retry count
                retry = false;
            } else if (exceptionBlacklist.contains(exception.getClass())) {
                // immediately cancel retry if the error is blacklisted
                retry = false;
            } else if (exceptionWhitelist.contains(exception.getClass())) {
                // immediately retry if error is whitelisted
                retry = true;
            } else if (!sent) {
                // for most other errors, retry only if request hasn't been
                // fully
                // sent yet
                retry = true;
            }

            if (retry) {
                // resend all idempotent requests
                HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
                String requestType = currentReq.getMethod();
                retry = !requestType.equals("POST");
            }

            if (retry) {
                SystemClock.sleep(RETRY_SLEEP_TIME_MILLIS);
            } else {
                exception.printStackTrace();
            }

            return retry;
        }
    }

}
