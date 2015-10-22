
package cn.common.http.urlconnect;

import cn.common.http.base.BaseRequest;
import cn.common.http.base.BaseResponse;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * 描述:
 *
 * @author jakechen
 * @since 2015/10/22 16:04
 */
public class BaseUrlConnect {
    /**
     * 连接超时间隔
     */
    public static final int CONNECT_TIME_OUT = 6 * 1000;

    /**
     * 读取超时间隔
     */
    public static final int READ_TIME_OUT = 6 * 1000;

    public void post(BaseRequest request, BaseResponse response) {
        HttpURLConnection conn = null;
        URL url = null;
        try {
            url = new URL("");
            conn = (HttpURLConnection) url.openConnection();
            setPostConnectParam(conn);
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                setRequestParam(conn.getOutputStream());
                String result = getResponse(conn.getInputStream());
            } else {
                conn.connect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    private String getResponse(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        StringBuffer buffer = new StringBuffer();
        while ((line = bufferedReader.readLine()) != null) {
            buffer.append(line);
        }
        bufferedReader.close();
        return buffer.toString();
    }

    private void setRequestParam(OutputStream outputStream) throws IOException {
        DataOutputStream os = new DataOutputStream(outputStream);
        os.writeBytes("");
        os.flush();
        os.close();
    }

    private void setPostConnectParam(HttpURLConnection conn) throws ProtocolException {
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setConnectTimeout(CONNECT_TIME_OUT);
        conn.setReadTimeout(READ_TIME_OUT);
        conn.setRequestMethod("POST");
        conn.setChunkedStreamingMode(0);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
    }
}
