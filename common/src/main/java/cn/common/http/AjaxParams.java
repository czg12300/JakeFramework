package cn.common.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 * <p>
 * 使用方法:
 * <p>
 *
 * <pre>
 * AjaxParams params = new AjaxParams();
 * params.put("username", "michael");
 * params.put("password", "123456");
 * params.put("email", "test@tsz.net");
 * params.put("profile_picture", new File("/mnt/sdcard/pic.jpg")); // 上传文件
 * params.put("profile_picture2", inputStream); // 上传数据流
 * params.put("profile_picture3", new ByteArrayInputStream(bytes)); // 提交字节流
 *  *
 * </pre>
 */

public class AjaxParams {
	private static String ENCODING = "UTF-8";

	protected ConcurrentHashMap<String, String> urlParams;
	protected ConcurrentHashMap<String, FileWrapper> fileParams;

	public AjaxParams() {
		init();
	}

	public AjaxParams(Map<String, String> source) {
		init();
		for (Map.Entry<String, String> entry : source.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	public AjaxParams(String key, String value) {
		init();
		put(key, value);
	}

	public AjaxParams(Object... keysAndValues) {
		init();
		int len = keysAndValues.length;
		if (len % 2 != 0) {
			throw new IllegalArgumentException("Supplied arguments must be even");
		}
		for (int i = 0; i < len; i += 2) {
			String key = String.valueOf(keysAndValues[i]);
			String val = String.valueOf(keysAndValues[i + 1]);
			put(key, val);
		}
	}

	public void put(String key, String value) {
		if (key != null && value != null) {
			urlParams.put(key, value);
		}
	}

	public void put(String key, File file) throws FileNotFoundException {
		put(key, new FileInputStream(file), file.getName());
	}

	public void put(String key, InputStream stream) {
		put(key, stream, null);
	}

	public void put(String key, InputStream stream, String fileName) {
		put(key, stream, fileName, null);
	}

	/**
	 * 添加 inputStream 到请求中.
	 *
	 * @param key
	 *            表单名.
	 * @param stream
	 *            输入流.
	 * @param fileName
	 *            文件名称.
	 * @param contentType
	 *            文件类型, eg. application/json
	 */
	public void put(String key, InputStream stream, String fileName, String contentType) {
		if (key != null && stream != null) {
			fileParams.put(key, new FileWrapper(stream, fileName, contentType));
		}
	}

	/**
	 * 移除参数
	 *
	 * @param key
	 */
	public void remove(String key) {
		urlParams.remove(key);
		fileParams.remove(key);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
			if (result.length() > 0) {
				result.append("&");
			}

			result.append(entry.getKey());
			result.append("=");
			result.append(entry.getValue());
		}

		for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
			if (result.length() > 0) {
				result.append("&");
			}

			result.append(entry.getKey());
			result.append("=");
			result.append("FILE");
		}

		return result.toString();
	}

	/**
	 * Returns an HttpEntity containing all request parameters
	 */
	public HttpEntity getEntity() {
		HttpEntity entity = null;
		if (!fileParams.isEmpty()) {
			MultipartEntity multipartEntity = new MultipartEntity();

			// Add string params
			for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
				multipartEntity.addPart(entry.getKey(), entry.getValue());
			}

			// Add File params
			int currentIndex = 0;

			int lastIndex = fileParams.entrySet().size() - 1;
			for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams.entrySet()) {
				FileWrapper file = entry.getValue();
				if (file.inputStream != null) {

					boolean isLast = currentIndex == lastIndex;
					if (file.contentType != null) {
						multipartEntity.addPart(entry.getKey(), file.getFileName(), file.inputStream, file.contentType, isLast);
					} else {
						multipartEntity.addPart(entry.getKey(), file.getFileName(), file.inputStream, isLast);
					}
				}
				currentIndex++;
			}

			entity = multipartEntity;
		} else {
			try {
				entity = new UrlEncodedFormEntity(getParamsList(), ENCODING);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return entity;
	}

	private void init() {
		urlParams = new ConcurrentHashMap<String, String>();
		fileParams = new ConcurrentHashMap<String, FileWrapper>();
	}

	protected List<BasicNameValuePair> getParamsList() {
		List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();
		for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
			lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return lparams;
	}

	public String getParamString() {
		StringBuilder result = new StringBuilder();
		for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
			if (result.length() > 0) {
				result.append("&");
			}

			try {
				result.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), ENCODING));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	/**
	 * 上传文件结构对象
	 *
	 * @author chen
	 *
	 */
	private static class FileWrapper {

		public String fileName;
		public String contentType;
		public InputStream inputStream;

		public FileWrapper(InputStream inputStream, String fileName, String contentType) {
			this.inputStream = inputStream;
			this.fileName = fileName;
			this.contentType = contentType;
		}

		public String getFileName() {
			if (fileName != null) {
				return fileName;
			} else {
				return "nofilename";
			}
		}
	}

}