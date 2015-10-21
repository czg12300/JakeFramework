package cn.common.http;
/**
 * http返回码
 *
 * @author libin
 *
 */
public interface HttpReturnCode {

	public static final int STATUS_OK = 200;
	public static final int STATUS_NO_NETWORK = 999;
	public static final int RESPONSE_IO_ERROR = 1000;
	public static final int RESPONSE_FORMAT_ERROR = 1001;
	public static final int HTTP_RESPONSE_ERROR_CODE = 1015;
	public static final int HTTP_RESPONSE_TIMEOUT_CODE = 1016;
	public static final int HTTP_NO_HOST_NAME = 1017;
	public static final int OTHER_ERROR = 1018;
}