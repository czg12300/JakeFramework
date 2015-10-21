package cn.common.http.exception;


import cn.common.http.HttpReturnCode;

public class HttpExcHandler {
    public static final String $服务器无响应$ = "服务器无响应";
    public static final String $网络错误$ = "错误代码{0}，异常信息{1}";
    public static final String $网络数据异常$ = "网络数据异常，请重试";
    public static final String $域名不存在$ = "域名不存在,请检查访问域名";
    public static final String $网络连接超时$ = "网络连接超时，请稍后再试";
    public static final String $数据获取失败$ = "数据获取失败，请检查网络设置";
    public static final String $无法连接到网络请检查网络配置$ = "无法连接到网络请检查网络配置";
    public static final String $json数据解析异常$ = "json数据解析异常";

    public static String Lang(String lang, String... args) {
        if (null != lang) {
            for (int i = 0; i < args.length; i++) {
                if (null != args[i]) {
                    lang = lang.replace("{" + i + "}", args[i]);
                }
            }
        } else {
            lang = "";
        }
        return lang;

    }

    public static HttpException getCustomException(String msg) {
        return new HttpException(HttpReturnCode.OTHER_ERROR, msg);
    }

    public static HttpException getCustomException(int statusCode, String msg) {
        return new HttpException(statusCode, Lang($网络错误$, statusCode + "", ""));
    }

    public static HttpException wrongJsonFormat(String rawString) {
        return new HttpException(HttpReturnCode.RESPONSE_FORMAT_ERROR, rawString);
    }

    public static HttpException responseTimeOut() {
        return new HttpException(HttpReturnCode.HTTP_RESPONSE_TIMEOUT_CODE, $网络连接超时$);
    }

    public static HttpException noHttpResponse() {
        return new HttpException(HttpReturnCode.HTTP_RESPONSE_ERROR_CODE, $服务器无响应$);
    }

    public static HttpException hasIOException() {
        return new HttpException(HttpReturnCode.RESPONSE_IO_ERROR, $网络数据异常$);
    }

    public static HttpException jsonParseException() {
        return new HttpException(-1, $json数据解析异常$);
    }

    public static HttpException IllegalArgumentException() {
        return new HttpException(HttpReturnCode.HTTP_NO_HOST_NAME, $域名不存在$);
    }

    public static HttpException handleHttpResponseError(int statusCode, String msg) {
        return new HttpException(HttpReturnCode.HTTP_RESPONSE_ERROR_CODE, Lang($网络错误$, statusCode + "", msg));
    }

}
