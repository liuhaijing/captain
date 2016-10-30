package com.jiou.downloader;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.HttpClientDownloader;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.jiou.domain.SinaWeibo;
import com.jiou.httpclients.DefaultConnectionKeepAliveStrategy;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.httpclients.UserAgent;
import com.jiou.support.Mayi;
import com.jiou.support.Redis;

public class SinaSpreadDownloader extends HttpClientDownloader {

	private static final String seedFormat = "http://s.weibo.com/weibo/%s&scope=ori&suball=1&Refer=g&page=%d";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private static final String cookieList = "sina_cookies";

	protected CloseableHttpClient client = buildHttpClient();

	@Override
	public Page download(Request request, Task task) {
		Site site = null;
		if (task != null) {
			site = task.getSite();
		}
		Set<Integer> acceptStatCode;
		String charset = null;
		if (site != null) {
			acceptStatCode = site.getAcceptStatCode();
			charset = site.getCharset();
		} else {
			acceptStatCode = Sets.newHashSet(200);
		}
		CloseableHttpResponse httpResponse = null;
		int statusCode = 0;
		String cookie = get();
		try {
			if (request.getDepth() == 0 && !request.getUrl().startsWith("http")) {
				String query = request.getUrl();
				request.putExtra(SinaWeibo.query, query);
				request.putExtra(SinaWeibo.page, 1);
				request.setUrl(String.format(seedFormat, URLEncoder.encode(query, SinaWeibo.charset), 1));
			}
			HttpUriRequest httpUriRequest = createHttpUriRequest(request.getUrl(), false);
			httpUriRequest.addHeader(HttpHeaders.COOKIE, cookie);
			httpResponse = this.client.execute(httpUriRequest);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			request.putExtra(Request.STATUS_CODE, statusCode);
			if (statusAccept(acceptStatCode, statusCode)) {
				Page page = handleResponse(request, charset, httpResponse, task);
				onSuccess(request);
				logger.info("downloading page {}", request.getUrl());
				return page;
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.warn("download page " + request.getUrl() + " error", e);
			if (site.getCycleRetryTimes() > 0) {
				return addToCycleRetry(request, site);
			}
			onError(request);
			return null;
		} finally {
			put(cookie);
			request.putExtra(Request.STATUS_CODE, statusCode);
			try {
				if (httpResponse != null) {
					EntityUtils.consume(httpResponse.getEntity());
				}
			} catch (IOException e) {
				logger.warn("close response fail", e);
			}
		}
	}

	private HttpHost proxy = new HttpHost(Mayi.host, Mayi.port);

	public HttpUriRequest createHttpUriRequest(String url, boolean useproxy) {
		String authHeader = Mayi.buildAuthHeader();
		RequestConfig.Builder requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(30000);
		RequestBuilder requestBuilder = RequestBuilder.get().setUri(url)
				.addHeader(HttpHeaders.USER_AGENT, UserAgent.IE);
		HttpUriRequest request = null;
		if (useproxy) {
			request = requestBuilder.setConfig(requestConfig.setProxy(proxy).build()).addHeader(Mayi.auth, authHeader)
					.build();
		} else {
			request = requestBuilder.setConfig(requestConfig.build()).build();
		}
		return request;
	}

	protected CloseableHttpClient buildHttpClient() {
		HttpClientBuilder builder = HttpClients.custom();
		builder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy(5L));
		builder.setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build());
		List<Header> defaultHeaders = new ArrayList<Header>();
		defaultHeaders.add(new BasicHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
		defaultHeaders.add(new BasicHeader("Accept-Encoding", "gzip, deflate, sdch"));
		defaultHeaders.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8"));
		builder.setDefaultHeaders(defaultHeaders);
		builder.setConnectionManager(HttpClientUtils.getConnectionmanager());
		builder.setRetryHandler(DefaultHttpRequestRetryHandler.INSTANCE);
		return builder.build();
	}

	@Override
	public void setThread(int thread) {
		// do nothing
	}

	protected String get() {
		Jedis jedis = Redis.jedisPool.getResource();
		try {
			String cookie = jedis.lpop(cookieList);
			while (cookie == null) {
				waitNewCookie();
				cookie = jedis.lpop(cookieList);
			}
			return cookie;
		} catch (Exception ex) {
			logger.error("放回cookie错误");
		} finally {
			jedis.close();
		}
		return null;
	}

	protected void put(String cookie) {
		if (StringUtils.isNotBlank(cookie)) {
			Jedis jedis = Redis.jedisPool.getResource();
			try {
				jedis.rpush(cookieList, cookie);
				signalNewCookie();
			} catch (Exception ex) {
				logger.error("放回cookie错误");
			} finally {
				jedis.close();
			}
		}
	}

	protected ReentrantLock lock = new ReentrantLock();
	protected Condition condition = lock.newCondition();
	protected int emptySleepTime = 100;

	protected void waitNewCookie() {
		lock.lock();
		try {
			condition.await(emptySleepTime, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.warn("sina waitNewCookie - interrupted, error {}", e);
		} finally {
			lock.unlock();
		}
	}

	protected void signalNewCookie() {
		try {
			lock.lock();
			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}

	public static void main(String[] args) throws Exception {
		String url = "http://weibo.com/u/2063148581?refer_flag=1087030701_2975_7002_0";
		SinaSpreadDownloader downloader = new SinaSpreadDownloader();
		// HttpUriRequest request = downloader.createHttpUriRequest(url, true);
		// CloseableHttpResponse resp = HttpClientUtils.execute(request);
		// System.out.println(HttpClientUtils.getHeaderValue(resp,
		// "X-User-Agent"));
		// String html = HttpClientUtils.getHtml(resp);
		// System.out.println(html);
		Page page = downloader.download(new Request(url), null);
		System.out.println(page.getRawText());
	}

}
