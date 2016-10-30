package com.jiou.downloader;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.UrlUtils;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.jiou.cookiemanager.CookieManager;
import com.jiou.cookiemanager.SogouCookieManager;

public class SogouDownloader extends HttpClientDownloader {
	protected static CookieManager cookieManager = new SogouCookieManager();

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private static final String data_src = "data-src";
	private static final String src = "src";

	public SogouDownloader() {
		super();
	}

	@Override
	public Page download(Request request, Task task) {
		String cookieStr = cookieManager.get();
		Page page = null;
		try {
			page = httpDownload(request, task, cookieStr, page);
			logger.info("downloading page {}", request.getUrl());
		} catch (Exception ex) {
			logger.warn("download page " + request.getUrl() + " error", ex);
			onError(request);
			return null;
		} finally {
			if (page != null) {
				if (checkLimit(page.getRawText())) {
					// cookieManager.put(cookieStr, false);
					// this.spider.addRequest(request);
					return null;
				} else {
					cookieManager.put(cookieStr, true);
				}
			} else {
				cookieManager.put(cookieStr, true);
			}
		}
		return page;
	}

	private Page httpDownload(Request request, Task task, String cookieStr, Page page) throws Exception {
		Site site = null;
		if (task != null) {
			site = task.getSite();
		}
		Set<Integer> acceptStatCode;
		String charset = null;
		Map<String, String> headers = null;
		if (site != null) {
			acceptStatCode = site.getAcceptStatCode();
			charset = site.getCharset();
			headers = site.getHeaders();
		} else {
			acceptStatCode = Sets.newHashSet(200);
		}
		CloseableHttpResponse httpResponse = null;
		int statusCode = 0;
		try {
			HttpUriRequest httpUriRequest = createHttpUriRequest(request, site, headers);
			httpUriRequest.addHeader(HttpHeaders.COOKIE, cookieStr);
			httpResponse = getHttpClient(site).execute(httpUriRequest);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			request.putExtra(Request.STATUS_CODE, statusCode);
			if (statusAccept(acceptStatCode, statusCode)) {
				page = handleResponse(request, charset, httpResponse, task);
				page.getRequest().setCookie(cookieStr);
				onSuccess(request);
				return page;
			} else {
				logger.warn("code error " + statusCode + "\t" + request.getUrl());
				return null;
			}
		} finally {
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

	protected HttpUriRequest createHttpUriRequest(Request request, Site site, Map<String, String> headers) {
		RequestBuilder requestBuilder = selectRequestMethod(request).setUri(request.getUrl());
		if (headers != null) {
			for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
				requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
			}
		}
		// 允许循环重定向
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
				.setConnectionRequestTimeout(site.getTimeOut()).setSocketTimeout(site.getTimeOut())
				.setConnectTimeout(site.getTimeOut()).setCookieSpec(CookieSpecs.DEFAULT)
				.setCircularRedirectsAllowed(true);
		if (site.getHttpProxyPool().isEnable()) {
			HttpHost host = site.getHttpProxyFromPool();
			requestConfigBuilder.setProxy(host);
			request.putExtra(Request.PROXY, host);
		}
		requestBuilder.setConfig(requestConfigBuilder.build());
		HttpUriRequest httpUriRequest = requestBuilder.build();
		return httpUriRequest;
	}

	protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task)
			throws IOException {
		String content = getContent(charset, httpResponse);
		content = UrlUtils.fixAllRelativeSrcs(content.replace(data_src, src), request.getUrl());
		Page page = new Page();
		page.setRawText(content);
		page.setUrl(new PlainText(request.getUrl()));
		page.setRequest(request);
		page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
		return page;
	}

	// public void setCookieManager(CookieManager cookieManager) {
	// this.cookieManager = cookieManager;
	// }

	// public void setSpider(Spider spider) {
	// this.spider = spider;
	// }

	private boolean checkLimit(String html) {
		if (StringUtils.isNotBlank(html) && html.contains("访问过于频繁")) {
			logger.info("被封了......");
			return true;
		}
		return false;
	}
}
