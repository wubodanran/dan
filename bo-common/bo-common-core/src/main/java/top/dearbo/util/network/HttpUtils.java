package top.dearbo.util.network;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.dearbo.util.exception.AppException;
import top.dearbo.util.file.StreamUtil;
import top.dearbo.util.network.common.HttpGlobalConfig;
import top.dearbo.util.network.common.HttpStatusCode;
import top.dearbo.util.network.exception.HttpException;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @fileName: HttpClientUtil
 * @author: Dan
 * @createDate: 2019-01-24 8:47.
 * @description: https、http(get,post)
 */
public class HttpUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

	/**
	 * 默认编码
	 */
	private static final String DEFAULT_ENCODING = "UTF-8";
	/**
	 * Https请求
	 */
	private static final String HTTPS = "https";

	/**
	 * http
	 */
	private static final String HTTP = "http";
	private static final String METHOD_GET = "GET";
	private static final String METHOD_POST = "POST";
	private static final int DEFAULT_SUCCESS_CODE = 200;
	private static final String ACCEPT = "Accept";
	private static final String USER_AGENT = "User-Agent";
	private static final String ACCEPT_CONTENT_TYPE = "application/json, text/plain, */*";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_1 = "content-type";
	private static final String CONTENT_TYPE_JSON = "application/json";
	//private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final int DEFAULT_CONNECT_TIMEOUT = 1000 * 10;
	private static final int DEFAULT_READ_TIMEOUT = 1000 * 10;
	/**
	 * 结果转String类型
	 */
	private boolean resultToStringFlag = true;
	/**
	 * 是否自动关闭
	 */
	private boolean disconnectFlag = true;
	/**
	 * 表示成功的编码
	 */
	private int successCode = DEFAULT_SUCCESS_CODE;
	/**
	 * 是否对请求参数key进行编码(默认需要)
	 */
	private boolean encodeParamKeyFlag = true;
	/**
	 * 是否对请求参数value进行编码(默认需要)
	 */
	private boolean encodeParamValueFlag = true;
	/**
	 * 参数编码
	 */
	private String paramCharset = DEFAULT_ENCODING;
	/**
	 * 返回结果编码
	 */
	private String resultCharset;
	/**
	 * 默认cookie字段
	 */
	private String defaultHeaderCookieField = "Set-Cookie";
	private Integer connectTimeout;
	private Integer readTimeout;

	/**
	 * 当前代理类
	 */
	private Proxy proxy;
	/**
	 * 是否使用全局代理
	 */
	private boolean globalProxyFlag;

	/**
	 * 是否设置通用属性
	 */
	private boolean usePropertyFlag = true;

	private HttpUtils() {

	}

	public static HttpUtils createRequest() {
		return new HttpUtils();
	}

	public ResultResponse doPost(String requestUrl) {
		return doPost(requestUrl, null, null);
	}

	public ResultResponse doGet(String requestUrl) {
		return doGet(requestUrl, null, null);
	}

	public ResultResponse doPost(String requestUrl, Map<String, Object> paramMap) {
		return doPost(requestUrl, paramMap, null);
	}

	public ResultResponse doGet(String requestUrl, Map<String, Object> paramMap) {
		return doGet(requestUrl, paramMap, null);
	}

	public ResultResponse doPost(String requestUrl, Map<String, Object> paramMap, Map<String, String> headerMap) {
		return wrapRequest(requestUrl, paramMap, headerMap, METHOD_POST, null);
	}

	public ResultResponse doGet(String requestUrl, Map<String, Object> paramMap, Map<String, String> headerMap) {
		return wrapRequest(requestUrl, paramMap, headerMap, METHOD_GET, null);
	}

	public ResultResponse doPost(String requestUrl, Map<String, Object> paramMap, Map<String, String> headerMap, String resultEncoding) {
		setResultCharset(resultEncoding);
		return wrapRequest(requestUrl, paramMap, headerMap, METHOD_POST, null);
	}

	public ResultResponse doGet(String requestUrl, Map<String, Object> paramMap, Map<String, String> headerMap, String resultEncoding) {
		setResultCharset(resultEncoding);
		return wrapRequest(requestUrl, paramMap, headerMap, METHOD_GET, null);
	}

	public ResultResponse doPostJson(String requestUrl, String paramJson) {

		return doPostJson(requestUrl, paramJson, null);
	}

	public ResultResponse doPostJson(String requestUrl, String paramJson, Map<String, String> headerMap) {

		return doPostJson(requestUrl, paramJson, headerMap, null);
	}

	public ResultResponse doPostJson(String requestUrl, String paramJson, Map<String, String> headerMap, String resultEncoding) {
		if (headerMap == null) {
			headerMap = new HashMap<>(16);
		}
		String contentType = StringUtils.isBlank(headerMap.get(CONTENT_TYPE)) ? headerMap.get(CONTENT_TYPE_1) : headerMap.get(CONTENT_TYPE);
		if (StringUtils.isBlank(contentType)) {
			headerMap.put(CONTENT_TYPE, CONTENT_TYPE_JSON);
		}
		if (StringUtils.isBlank(headerMap.get(ACCEPT))) {
			headerMap.put(ACCEPT, ACCEPT_CONTENT_TYPE);
		}
		setResultCharset(resultEncoding);
		return wrapRequest(requestUrl, null, headerMap, METHOD_POST, paramJson);
	}

	/**
	 * 设置http/https,post/get
	 */
	private ResultResponse wrapRequest(String requestUrl, Map<String, Object> paramMap, Map<String, String> headerMap, String requestMethod, String paramJson) {
		if (StringUtils.isBlank(requestUrl)) {
			logger.info("请求地址不能为空!requestUrl:【{}】", requestUrl);
			AppException.throwEx("请求地址不能为空!requestUrl:" + requestUrl);
		}
		requestMethod = StringUtils.isBlank(requestMethod) ? METHOD_POST : requestMethod;
		boolean methodPostFlag = true;
		if (requestMethod.toUpperCase().equals(METHOD_GET)) {
			methodPostFlag = false;
		}
		boolean httpsFlag = false;
		if (requestUrl.startsWith(HTTPS)) {
			httpsFlag = true;
		}
		//当前是否有开启全局代理
		if (proxy == null && globalProxyFlag) {
			proxy = HttpGlobalConfig.getInstance().getProxy();
		}
		return request(requestUrl, methodPostFlag, paramMap, headerMap, httpsFlag, paramJson);
	}

	private ResultResponse request(String requestUrl, boolean methodPostFlag, Map<String, Object> paramMap, Map<String, String> headerMap, boolean httpsFlag, String paramJson) {
		InputStream inputStream = null;
		PrintWriter out = null;
		HttpURLConnection connection = null;
		String paramCharset = getParamCharset();
		String resultEncoding = getResultCharset();
		try {
			if (!methodPostFlag && paramMap != null) {
				char spliceChar = '?';
				if (requestUrl.indexOf(spliceChar) > -1) {
					spliceChar = '&';
				}
				requestUrl = requestUrl + spliceChar + genUrlParam(paramMap, paramCharset, isEncodeParamKeyFlag(), isEncodeParamValueFlag());
			}
			URL url = new URL(requestUrl);
			// 打开和URL之间的连接
			if (httpsFlag) {
				if (proxy != null) {
					connection = (HttpsURLConnection) url.openConnection(proxy);
				} else {
					connection = (HttpsURLConnection) url.openConnection();
				}
				setHttps((HttpsURLConnection) connection);
			} else {
				if (proxy != null) {
					connection = (HttpURLConnection) url.openConnection(proxy);
				} else {
					connection = (HttpURLConnection) url.openConnection();
				}
			}
			connection.setConnectTimeout(getConnectTimeout());
			connection.setReadTimeout(getReadTimeout());
			// 设置请求方式（GET/POST）
			if (methodPostFlag) {
				// 发送POST请求必须设置如下两行
				//是否输入参数
				connection.setDoOutput(true);
				//是否读取参数
				connection.setDoInput(true);
				connection.setRequestMethod(METHOD_POST);
			} else {
				connection.setRequestMethod(METHOD_GET);
			}
			connection.setUseCaches(false);
			//设置头部
			if (headerMap != null && headerMap.size() > 0) {
				for (Map.Entry<String, String> headerItem : headerMap.entrySet()) {
					connection.setRequestProperty(headerItem.getKey(), headerItem.getValue());
				}
			}
			// 设置通用的请求属性
			if (usePropertyFlag) {
				if (headerMap == null || headerMap.get(ACCEPT) == null) {
					connection.addRequestProperty(ACCEPT, "*/*");
				}
				if (headerMap == null || headerMap.get(USER_AGENT) == null) {
					//  Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)
					connection.addRequestProperty(USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64)");
				}
				connection.addRequestProperty("Connection", "keep-alive");
			}

			// 当paramMap不为null时向输出流写数据
			if (methodPostFlag) {
				boolean paramFlag = null != paramMap && paramMap.size() > 0 || StringUtils.isNotBlank(paramJson);
				if (paramFlag) {
					// 获取URLConnection对象对应的输出流
					//new OutputStreamWriter(connection.getOutputStream(), encoding):解决中文乱码问题
					out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), paramCharset));
					// 发送请求参数
					if (StringUtils.isNotBlank(paramJson)) {
						out.print(paramJson);
					}
					if (paramMap != null && paramMap.size() > 0) {
						out.print(genUrlParam(paramMap, paramCharset, isEncodeParamKeyFlag(), isEncodeParamValueFlag()));
					}
					// flush输出流的缓冲
					out.flush();
				}
			}
			int responseCode = connection.getResponseCode();
			ResultResponse resultResponse = new ResultResponse(responseCode, connection.getResponseMessage());
			resultResponse.setHeaderCookieField(defaultHeaderCookieField);
			resultResponse.setResultResponse(connection);
			if (responseCode == getSuccessCode()) {
				if (resultToStringFlag) {
					// 从输入流读取返回内容
					inputStream = connection.getInputStream();
					resultResponse.setData(toInputStreamConvertString(inputStream, resultEncoding));
				}
			} else if (connection.getErrorStream() != null) {
				inputStream = connection.getErrorStream();
				resultResponse.setErrorData(toInputStreamConvertString(inputStream, resultEncoding));
			}
			return resultResponse;
		} catch (IOException ce) {
			//ce.printStackTrace();
			logger.error("url:【{}】,msg:【{}】", requestUrl, ce.getMessage(), ce);
			//AppException.throwEx(ce);
			return HttpException.handleException(ce);
		} finally {
			if (out != null) {
				out.close();
			}
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (disconnectFlag && connection != null) {
				connection.disconnect();
			}
		}
	}

	private String toInputStreamConvertString(InputStream inputStream, String encoding) throws IOException {
		if (StringUtils.isNotBlank(encoding)) {
			return StreamUtil.inputStreamToReaderString(inputStream, encoding);
		}
		return StreamUtil.inputStreamToArrayString(inputStream, encoding);
	}

	private void setHttps(HttpsURLConnection connection) {
		// 创建SSLContext对象，并使用我们指定的信任管理器初始化
		TrustManager[] tm = {new MyX509TrustManager()};
		try {
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new SecureRandom());
			// 从上述SSLContext对象中得到SSLSocketFactory对象
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			connection.setSSLSocketFactory(ssf);
		} catch (NoSuchAlgorithmException | KeyManagementException | NoSuchProviderException e) {
			e.printStackTrace();
			logger.error("https设置异常!msg:【{}】", e.getMessage(), e);
			AppException.throwEx(e);
		}

	}

	private String genUrlParam(Map<String, Object> paramMap) {
		return genUrlParam(paramMap, null, false, false);
	}

	/**
	 * 拼接Url
	 *
	 * @param paramMap       参数
	 * @param encoderCharset 编码字符
	 * @param keyEncoder     是否对key编码
	 * @param valueEncoder   是否对value编码
	 * @return String
	 */
	private String genUrlParam(Map<String, Object> paramMap, String encoderCharset, boolean keyEncoder, boolean valueEncoder) {
		if (paramMap == null || paramMap.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Object> item : paramMap.entrySet()) {
			if (keyEncoder && StringUtils.isNotBlank(encoderCharset)) {
				sb.append(urlEncoder(item.getKey(), encoderCharset));
			} else {
				sb.append(item.getKey());
			}
			sb.append("=");
			if (valueEncoder && StringUtils.isNotBlank(encoderCharset)) {
				sb.append(urlEncoder(item.getValue().toString(), encoderCharset));
			} else {
				sb.append(item.getValue());
			}
			sb.append("&");
		}
		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * url编码
	 *
	 * @param str     字符串
	 * @param charset 编码
	 */
	public String urlEncoder(String str, String charset) {
		if (StringUtils.isBlank(str)) {
			return "";
		}
		try {
			return URLEncoder.encode(str, charset);
		} catch (UnsupportedEncodingException e) {
			logger.error("value:【{}】 to charset:【{}】 error:【{}】", str, charset, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public boolean isUsePropertyFlag() {
		return usePropertyFlag;
	}

	public HttpUtils setUsePropertyFlag(boolean usePropertyFlag) {
		this.usePropertyFlag = usePropertyFlag;
		return this;
	}

	public int getConnectTimeout() {
		return connectTimeout == null ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout == null ? DEFAULT_READ_TIMEOUT : readTimeout;
	}

	public String getParamCharset() {
		return StringUtils.isNotBlank(paramCharset) ? paramCharset : DEFAULT_ENCODING;
	}

	public HttpUtils setParamCharset(String paramCharset) {
		if (StringUtils.isNotBlank(paramCharset)) {
			this.paramCharset = paramCharset;
		}
		return this;
	}

	public String getResultCharset() {
		return resultCharset;
	}

	public HttpUtils setResultCharset(String resultCharset) {
		if (resultCharset != null) {
			this.resultCharset = resultCharset;
		}
		return this;
	}

	public HttpUtils setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout < 1 ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
		return this;
	}

	public HttpUtils setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout < 0 ? DEFAULT_READ_TIMEOUT : readTimeout;
		return this;
	}

	public HttpUtils setResultToString(boolean flag) {
		this.resultToStringFlag = flag;
		return this;
	}

	public HttpUtils setDisconnectFlag(boolean disconnectFlag) {
		this.disconnectFlag = disconnectFlag;
		return this;
	}

	public HttpUtils setHeaderCookieField(String headerCookieField) {
		if (headerCookieField != null && headerCookieField.length() > 0) {
			this.defaultHeaderCookieField = headerCookieField;
		}
		return this;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public HttpUtils setProxy(Proxy proxy) {
		this.proxy = proxy;
		return this;
	}

	public boolean isGlobalProxyFlag() {
		return globalProxyFlag;
	}

	public HttpUtils setGlobalProxyFlag(boolean globalProxyFlag) {
		this.globalProxyFlag = globalProxyFlag;
		return this;
	}

	public int getSuccessCode() {
		return successCode;
	}

	public HttpUtils setSuccessCode(int successCode) {
		this.successCode = successCode;
		return this;
	}

	public boolean isEncodeParamKeyFlag() {
		return encodeParamKeyFlag;
	}

	public HttpUtils setEncodeParamKeyFlag(boolean encodeParamKeyFlag) {
		this.encodeParamKeyFlag = encodeParamKeyFlag;
		return this;
	}

	public boolean isEncodeParamValueFlag() {
		return encodeParamValueFlag;
	}

	public HttpUtils setEncodeParamValueFlag(boolean encodeParamValueFlag) {
		this.encodeParamValueFlag = encodeParamValueFlag;
		return this;
	}

	public static class ResultResponse implements Serializable {
		private static final long serialVersionUID = -884562198636894001L;
		private int status;
		private String msg;
		private String message;
		private String data;
		private transient String errorData;
		private transient String headerCookieField;
		private transient HttpURLConnection resultResponse;

		private ResultResponse() {

		}

		public ResultResponse(int status, String message) {
			this.status = status;
			this.msg = HttpStatusCode.getHttpStatusMsg(status);
			this.message = message;
		}

		public ResultResponse(int status, String message, String data) {
			this.status = status;
			this.msg = HttpStatusCode.getHttpStatusMsg(status);
			this.message = message;
			this.data = data;
		}

		public ResultResponse(int status, String message, String msg, String data) {
			this.status = status;
			this.msg = msg;
			this.message = message;
			this.data = data;
		}

		@Override
		public String toString() {
			return String.format("{\"status\":%s,\"message\":'%s',\"msg\":'%s',\"data\":'%s',\"errorData\":'%s'}", status, message, msg, data, errorData);
		}

		public boolean isSuccess() {
			return status == DEFAULT_SUCCESS_CODE;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.msg = HttpStatusCode.getHttpStatusMsg(status);
			this.status = status;
		}

		public String getMsg() {
			return msg;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		public String getErrorData() {
			return errorData;
		}

		public void setErrorData(String errorData) {
			this.errorData = errorData;
		}

		public HttpURLConnection getResultResponse() {
			return resultResponse;
		}

		public void setResultResponse(HttpURLConnection resultResponse) {
			this.resultResponse = resultResponse;
		}

		public Map<String, List<String>> getHeaders() {
			if (resultResponse == null) {
				return Collections.emptyMap();
			}
			Map<String, List<String>> headerFields = resultResponse.getHeaderFields();
			return headerFields != null ? headerFields : Collections.emptyMap();
		}

		/**
		 * 在Header里获取所有cookie
		 * 可能会包含:[Domain,Expires,Path,HttpOnly]等信息
		 * 处理后的:cookies()
		 * headerCookieField:默认Set-Cookie
		 *
		 * @return List
		 */
		public List<String> getHeaderCookies() {
			return getHeaderCookies(getHeaderCookieField());
		}

		public List<String> getHeaderCookies(String headerCookieField) {
			if (headerCookieField == null || headerCookieField.isEmpty() || resultResponse == null) {
				return Collections.emptyList();
			}
			List<String> cookieList = getHeaders().get(headerCookieField);
			return cookieList != null ? cookieList : Collections.emptyList();
		}

		/**
		 * 获取cookie,在原cookie基础上,截取第一个 ; 前
		 *
		 * @return map(key, value)
		 */
		public Map<String, String> getCookies() {
			List<String> cookies = getHeaderCookies();
			if (cookies.size() < 1) {
				return Collections.emptyMap();
			}
			Map<String, String> cookieMap = new LinkedHashMap<>(cookies.size());
			for (String cookie : cookies) {
				String subCookie = StringUtils.substring(cookie, 0, cookie.indexOf(";"));
				int indexOf = subCookie.indexOf('=');
				if (indexOf > -1) {
					String key = StringUtils.substring(subCookie, 0, indexOf);
					String value = StringUtils.substring(subCookie, indexOf + 1);
					cookieMap.put(key, value);
				}
			}
			return cookieMap;
		}

		/**
		 * cookie转成String
		 *
		 * @return String
		 */
		public String getCookiesToString() {
			List<String> cookies = getHeaderCookies();
			if (cookies.size() < 1) {
				return null;
			}
			StringBuilder stringBuilder = new StringBuilder();
			for (String cookie : cookies) {
				String subCookie = StringUtils.substring(cookie, 0, cookie.indexOf(";"));
				int indexOf = subCookie.indexOf('=');
				if (indexOf > -1) {
					stringBuilder.append(subCookie).append(';');
				}
			}
			return stringBuilder.toString();
		}

		public void close() {
			if (resultResponse != null) {
				resultResponse.disconnect();
			}
		}

		public String getHeaderCookieField() {
			return headerCookieField;
		}

		public void setHeaderCookieField(String headerCookieField) {
			if (StringUtils.isNotBlank(headerCookieField)) {
				this.headerCookieField = headerCookieField;
			}
		}
	}

	/**
	 * 信任管理器
	 */
	class MyX509TrustManager implements X509TrustManager {


		/**
		 * 检查客户端证书
		 */
		@Override
		public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

		}

		/**
		 * 检查服务器端证书
		 */
		@Override
		public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

		}

		/**
		 * 返回受信任的X509证书数组
		 */
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			//return new X509Certificate[0];
			return null;
		}
	}

}
