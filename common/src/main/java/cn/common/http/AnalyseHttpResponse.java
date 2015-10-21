package cn.common.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;

import cn.common.http.exception.HttpExcHandler;
import cn.common.http.exception.HttpException;


public class AnalyseHttpResponse {

    private String charset;

    private int statusCode;

    private HttpEntity entity;

    private String body;

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public HttpEntity getEntity() {
        return entity;
    }

    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    public String getBody() throws HttpException {
        if (null == body && entity != null) {
            try {
                body = deleteDOM(EntityUtils.toString(new BufferedHttpEntity(entity), charset));
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                throw HttpExcHandler.hasIOException();
            }
        }
        return body;
    }

    public byte[] getBytes() throws IOException {
        byte[] responseBody = null;
        if (entity != null) {
            responseBody = EntityUtils.toByteArray(new BufferedHttpEntity(entity));
        }
        return responseBody;
    }

    /**
     * 删除UTF-8的DOM头文件
     *
     * @param string
     * @return
     */
    public static String deleteDOM(String string) {
        if (string != null && string.startsWith("\ufeff")) {
            return string.substring(1);
        } else {
            return string;
        }
    }

}
