package cn.common.http;

public interface HttpRequstListener<T> {

	/**
	 * 开始
	 */
	public void onStart();

	/**
	 * 成功
	 */
	public void onSuccess(T result);

	/**
	 * 失败
	 */
	public void onFailure(int statusCode, String content);

	/**
	 * 超时
	 */
	public void onTimeout();

	/**
	 * 没有网络
	 */
	public void onNoNetwork();

	/**
	 * 取消
	 */
	public void onCancel();

}
