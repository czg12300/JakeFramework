package cn.common.http.exception;

public class HttpException extends Exception {

	private static final long serialVersionUID = 3551261921033531632L;

	private int errorCode;
	private String message;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String errorMsg) {
		this.message = errorMsg;
	}

	public HttpException(int code, String msg) {
		super(msg);
		this.errorCode = code;
		this.message = msg;
	}

}
