
package cn.common.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 字符编码，简单对系统api进行封装，解决把空格转为+号，导致某些接口处理异常的问题
 * 
 * @author jake
 */
public class UrlEncodeUtil {

    public static String encode(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return URLEncoder.encode(str).replace("+", "%20");
    }

    public static String encode(String str, String enc) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            return URLEncoder.encode(str, enc).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return encode(str);
        }

    }
}
