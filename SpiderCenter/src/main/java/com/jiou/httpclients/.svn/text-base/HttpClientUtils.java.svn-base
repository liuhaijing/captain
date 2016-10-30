package com.jiou.httpclients;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * @author zhe.li
 * @version 1.1.0
 */
public class HttpClientUtils {

	private static int keepAliveTimeInSeconds;

	private static boolean disableAutomaticRetries;
	private static boolean disableCookieManagement;

	private static final int minKeepAliveTimeInSeconds = 30;

	public static final String CHARSET_UTF8 = "UTF-8";
	public static final String CHARSET_GBK = "GBK";

	private static final String COOKIE = "Cookie";

	private static final PoolingHttpClientConnectionManager connectionManager;
	private static final IdleConnectionMonitor idleConnectionMonitor;

	private static final Map<String, CloseableHttpClient> httpClients = new WeakHashMap<String, CloseableHttpClient>();

	static {

		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance(SSLConnectionSocketFactory.SSL);
			sslContext.init(null, new TrustManager[] { new NullX509TrustManager() }, null);
		} catch (Exception e) {
			throw new RuntimeException("init ssl context error.");
		}
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslContext)).build();
		connectionManager = new PoolingHttpClientConnectionManager(registry);

		loadConfig(true);

		idleConnectionMonitor = new IdleConnectionMonitor(connectionManager, keepAliveTimeInSeconds);
		idleConnectionMonitor.start();
		new Task().start();
	}

	/**
	 * get a CloseableHttpClient by specific hostname from the cache,if not
	 * exists,create a new one.
	 * 
	 * @param host
	 * @return
	 */
	public static final CloseableHttpClient getHttpClient(String host) {
		if (host == null) {
			throw new IllegalArgumentException("host can not be null.");
		}

		CloseableHttpClient httpClient = httpClients.get(host);

		if (httpClient == null) {
			synchronized (HttpClientUtils.class) {
				httpClient = httpClients.get(host);
				if (httpClient == null) {
					httpClient = buildHttpClient();
					httpClients.put(host, httpClient);
				}
			}
		}

		return httpClient;
	}

	/**
	 * create a new CloseableHttpClient object.
	 */
	public static CloseableHttpClient buildHttpClient() {
		HttpClientBuilder builder = HttpClients.custom();
		builder.setConnectionManager(connectionManager);
		builder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy(5L));
		builder.setUserAgent(UserAgent.CHROME);
		if (disableAutomaticRetries) {
			builder.disableAutomaticRetries();
		}
		if (disableCookieManagement) {
			builder.disableCookieManagement();
		}
		builder.setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(60 * 1000).setSocketTimeout(60 * 1000)
				.build());

		List<Header> defaultHeaders = new ArrayList<Header>();
		defaultHeaders.add(new BasicHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
		defaultHeaders.add(new BasicHeader("Accept-Encoding", "gzip, deflate, sdch"));
		defaultHeaders.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8"));
		builder.setDefaultHeaders(defaultHeaders);

		return builder.build();
	}

	/**
	 * execute a http request by get method.
	 * 
	 * @param url
	 * @return com.ccx.httpclient.Pair<Integer, String>,the first is the http
	 *         status code,the second is the html source code.
	 * @throws Exception
	 */
	public static Pair<Integer, String> get(String url) throws Exception {

		if (url == null || "".equals(url)) {
			return null;
		}

		HttpGet request = new HttpGet(url);

		return getPair(request);
	}

	/**
	 * execute a http request by get method.
	 * 
	 * @param url
	 * @param additionalHeaders
	 * @return com.ccx.httpclient.Pair<Integer, String>,the first is the http
	 *         status code,the second is the html source code.
	 * @throws Exception
	 */
	public static Pair<Integer, String> get(String url, Map<String, String> additionalHeaders, RequestConfig config)
			throws Exception {

		if (url == null || "".equals(url)) {
			return null;
		}

		HttpGet request = new HttpGet(url);

		addHeaders(request, additionalHeaders);
		if (config != null) {
			request.setConfig(config);
		}

		return getPair(request);
	}

	/**
	 * execute a http request by post method.
	 * 
	 * @param url
	 * @param additionalHeaders
	 * @param config
	 * @return com.ccx.httpclient.Pair<Integer, String>,the first is the http
	 *         html source code.
	 * @throws Exception
	 */
	public static Pair<Integer, String> post(String url, Map<String, String> additionalHeaders, RequestConfig config)
			throws Exception {

		if (url == null || "".equals(url)) {
			return null;
		}

		HttpPost request = buildHttpPost(url);

		addHeaders(request, additionalHeaders);
		if (config != null) {
			request.setConfig(config);
		}

		return getPair(request);
	}

	/**
	 * execute a http request by post method.
	 * 
	 * @param url
	 * @return com.ccx.httpclient.Pair<Integer, String>,the first is the http
	 *         status code,the second is the html source code.
	 * @throws Exception
	 */
	public static Pair<Integer, String> post(String url) throws Exception {

		if (url == null || "".equals(url)) {
			return null;
		}

		HttpPost request = buildHttpPost(url);

		return getPair(request);

	}

	/**
	 * generate a http post request
	 * 
	 * @param url
	 *            the post url contains query parameters
	 * @return org.apache.http.client.methods.HttpPost
	 */
	public static HttpPost buildHttpPost(String url) {

		if (url == null || "".equals(url)) {
			return null;
		}

		String[] arr = url.split("\\?", 2);
		String posturl = arr[0];

		HttpPost request = new HttpPost(posturl);

		if (arr.length == 1) {
			return request;
		}

		String[] querys = arr[1].split("\\&");

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		for (String str : querys) {
			String[] query = str.split("=", 2);

			if (query.length == 1) {
				params.add(new BasicNameValuePair(query[0], ""));
			} else {
				params.add(new BasicNameValuePair(query[0], query[1]));
			}
		}

		try {
			request.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
			request.setEntity(new UrlEncodedFormEntity(params, CHARSET_UTF8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return request;
	}

	/**
	 * generate a http post request
	 * 
	 * @param posturl
	 *            the target url
	 * @param params
	 *            the post parameters
	 * @return org.apache.http.client.methods.HttpPost
	 */
	public static HttpPost buildHttpPost(String posturl, Map<String, String> params) {

		if (posturl == null || "".equals(posturl)) {
			return null;
		}

		HttpPost request = new HttpPost(posturl);

		if (params == null) {
			return request;
		}

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		try {
			request.addHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
			request.setEntity(new UrlEncodedFormEntity(parameters, CHARSET_UTF8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return request;
	}

	/**
	 * add headers to the specific http request.
	 * 
	 * @param request
	 * @param headers
	 */
	public static void addHeaders(HttpRequest request, Map<String, String> headers) {
		if (headers != null) {
			for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
				request.addHeader(headerEntry.getKey(), headerEntry.getValue());
			}
		}
	}

	/**
	 * extract html source code from the specific http response.
	 * 
	 * @param response
	 * @return
	 */
	public static String getHtml(CloseableHttpResponse response) {
		if (response == null) {
			return null;
		}

		HttpEntity entity = response.getEntity();

		if (entity == null) {
			return null;
		}

		try {
			byte[] bytes = EntityUtils.toByteArray(entity);
			String charset = CharsetUtil.analyze(bytes);
			return new String(bytes, charset);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getHtml(CloseableHttpResponse response, boolean close) {
		if (response == null) {
			return null;
		}

		HttpEntity entity = response.getEntity();

		if (entity == null) {
			return null;
		}

		try {
			byte[] bytes = EntityUtils.toByteArray(entity);
			String charset = CharsetUtil.analyze(bytes);
			return new String(bytes, charset);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (close) {
				close(response);
			}
		}
		return null;
	}

	public static byte[] getBytes(CloseableHttpResponse response) {
		if (response == null) {
			return null;
		}
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			return null;
		}
		try {
			byte[] bytes = EntityUtils.toByteArray(entity);
			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * send a get method http request
	 * 
	 * @param url
	 * @return the source html code
	 * @throws Exception
	 */
	public static String getHtml(String url) throws Exception {

		if (url == null || "".equals(url)) {
			return null;
		}

		HttpGet request = new HttpGet(url);

		return getHtml(execute(request));
	}

	/**
	 * send a post method http request
	 * 
	 * @param url
	 * @return the source html code
	 * @throws Exception
	 */
	public static String postHtml(String url) throws Exception {

		if (url == null || "".equals(url)) {
			return null;
		}

		HttpPost request = buildHttpPost(url);

		return getHtml(execute(request));
	}

	/**
	 * get http status code from the specific http response.
	 * 
	 * @param response
	 * @return
	 */
	public static int getStatuscode(CloseableHttpResponse response) {
		if (response == null) {
			return -1;
		}

		StatusLine statusLine = response.getStatusLine();
		if (statusLine == null) {
			return -1;
		}

		return statusLine.getStatusCode();
	}

	/**
	 * close the underlying resource.
	 * 
	 * @param closeable
	 */
	public static final void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * execute a http request.
	 * 
	 * @param request
	 * @return com.ccx.httpclient.Pair<Integer, String>,the first is the http
	 *         status code,the second is the html source code.
	 * @throws Exception
	 */
	public static Pair<Integer, String> getPair(HttpUriRequest request) throws Exception {

		CloseableHttpClient httpClient = getHttpClient(request.getURI().getHost());

		CloseableHttpResponse response = null;
		int status = -1;
		String html = null;

		try {
			response = httpClient.execute(request);
			status = getStatuscode(response);
			html = getHtml(response);
		} finally {
			close(response);
		}

		return new Pair<Integer, String>(status, html);
	}

	/**
	 * execute a http request.
	 * 
	 * @param request
	 * @return http response
	 * @throws Exception
	 */
	public static CloseableHttpResponse execute(HttpUriRequest request) throws Exception {

		CloseableHttpClient httpClient = getHttpClient(request.getURI().getHost());

		return httpClient.execute(request);
	}

	/**
	 * get the value of specific cookie name in the http response.
	 * 
	 * @param name
	 * @return cookie value or null.
	 */
	public static String getCookie(CloseableHttpResponse response, String name) {

		if (name == null || "".equals(name) || response == null) {
			return null;
		}

		HeaderIterator headerIt = response.headerIterator("Set-Cookie");

		if (headerIt == null) {
			return null;
		}

		while (headerIt.hasNext()) {
			Header header = headerIt.nextHeader();
			for (HeaderElement e : header.getElements()) {
				if (name.equalsIgnoreCase(e.getName())) {
					return e.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * get all the cookie value in the http response.
	 * 
	 * @param response
	 * @return cookie value or null.
	 */
	public static String getCookie(CloseableHttpResponse response) {
		if (response == null) {
			return null;
		}
		Header[] headers = response.getHeaders("Set-Cookie");

		if (headers == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < headers.length; x++) {
			String[] arr = headers[x].getValue().split(";");

			for (String s : arr) {
				if (s.contains("path=") || s.contains("Path=") || s.contains("expires=") || s.contains("HttpOnly")
						|| s.contains("Max-Age") || s.contains("domain")) {
					continue;
				}
				sb.append(s).append(";");
			}
		}

		String retValue = sb.toString();
		return retValue.endsWith(";") ? retValue.substring(0, retValue.length() - 1) : retValue;
	}

	/**
	 * get the header value in the http response.
	 * 
	 * @param response
	 * @param headerName
	 * @return
	 */
	public static String getHeaderValue(CloseableHttpResponse response, String headerName) {

		if (response == null) {
			return null;
		}

		HeaderIterator headerIt = response.headerIterator(headerName);

		if (headerIt == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		while (headerIt.hasNext()) {
			Header header = headerIt.nextHeader();
			sb.append(header.getValue()).append(";");
		}

		String header = sb.toString();
		header = header.endsWith(";") ? header.substring(0, header.length() - 1) : header;

		return header;
	}

	public static final void addUserAgent(HttpUriRequest request) {
		request.addHeader(HTTP.USER_AGENT, UserAgent.CHROME);
	}

	public static final byte[] loadBytes(String url, String cookie) throws Exception {
		HttpGet request = new HttpGet(url);
		if (cookie != null && !"".equals(cookie)) {
			request.addHeader(COOKIE, cookie);
		}
		CloseableHttpResponse resp = execute(request);
		byte[] bytes = EntityUtils.toByteArray(resp.getEntity());
		resp.close();
		return bytes;
	}

	public static final byte[] loadBytes(HttpUriRequest request) throws Exception {
		CloseableHttpResponse resp = execute(request);
		byte[] bytes = EntityUtils.toByteArray(resp.getEntity());
		resp.close();
		return bytes;
	}

	public static NameValuePair[] buildNVPArray(String url) {
		if (url == null || "".equals(url)) {
			return null;
		}
		String[] arr = url.split("\\?", 2);
		if (arr.length == 1) {
			return null;
		}
		String[] querys = arr[1].split("\\&");
		NameValuePair[] nvps = new NameValuePair[querys.length];
		for (int x = 0; x < querys.length; x++) {
			String[] query = querys[x].split("=", 2);
			if (query.length == 1) {
				nvps[x] = new BasicNameValuePair(query[0], "");
			} else {
				nvps[x] = new BasicNameValuePair(query[0], query[1]);
			}
		}
		return nvps;
	}

	public static List<NameValuePair> buildNVPList(String url) {
		if (url == null || "".equals(url)) {
			return null;
		}
		String[] arr = url.split("\\?", 2);
		if (arr.length == 1) {
			return null;
		}
		String[] querys = arr[1].split("\\&");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (String str : querys) {
			String[] query = str.split("=", 2);
			if (query.length == 1) {
				nvps.add(new BasicNameValuePair(query[0], ""));
			} else {
				nvps.add(new BasicNameValuePair(query[0], query[1]));
			}
		}
		return nvps;
	}

	public static PoolingHttpClientConnectionManager getConnectionmanager() {
		return connectionManager;
	}

	private static void loadConfig(boolean init) {
		InputStream in = null;
		try {
			in = HttpClientUtils.class.getClassLoader().getResourceAsStream("httpclients.properties");
			Properties props = new Properties();

			try {
				props.load(in);
			} catch (Exception ignore) {
			}

			int maxTotal = Integer.valueOf(props.getProperty("connection.max.total", "200"));
			int defaultMaxPerRoute = Integer.valueOf(props.getProperty("connection.default.perroute", "20"));
			keepAliveTimeInSeconds = Integer.valueOf(props.getProperty("connection.idle.time", "60"));
			keepAliveTimeInSeconds = keepAliveTimeInSeconds < minKeepAliveTimeInSeconds ? 60 : keepAliveTimeInSeconds;

			if (init) {
				disableAutomaticRetries = Boolean.valueOf(props.getProperty("disable.automatic.retries", "true"));
				disableCookieManagement = Boolean.valueOf(props.getProperty("disable.cookie.management", "true"));
			}

			// String maxPerRoutes =
			// props.getProperty("connection.max.httproute");

			synchronized (HttpClientUtils.class) {

				connectionManager.setMaxTotal(maxTotal);
				connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

				if (idleConnectionMonitor != null) {
					idleConnectionMonitor.setKeepAliveTimeInSeconds(keepAliveTimeInSeconds);
				}

				// if (/* init && */maxPerRoutes != null &&
				// !"".equals(maxPerRoutes)) {
				// String[] arr1 = maxPerRoutes.split(";");
				// for (String str : arr1) {
				// try {
				// String[] arr2 = str.split(":", 2);
				// connectionManager.setMaxPerRoute(new HttpRoute(new
				// HttpHost(arr2[0], 80)), Integer.valueOf(arr2[1]));
				// connectionManager.setMaxPerRoute(new HttpRoute(new
				// HttpHost(arr2[0], 433)), Integer.valueOf(arr2[1]));
				// } catch (Exception ignore) {
				// }
				// }
				// }

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(in);
		}
	}

	private static final class Task extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					TimeUnit.MINUTES.sleep(10);
					loadConfig(false);
				} catch (Exception ignore) {
				}
			}
		}

		@Override
		public synchronized void start() {
			this.setDaemon(true);
			super.start();
		}

	}

}
