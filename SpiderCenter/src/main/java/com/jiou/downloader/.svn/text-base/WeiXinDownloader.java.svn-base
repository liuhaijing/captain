package com.jiou.downloader;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.HttpClientDownloader;

import com.google.common.collect.Sets;

public class WeiXinDownloader extends HttpClientDownloader {

	public static String uin = "MTcyMjE2ODkzMQ==";
//	public static String uin = "MzE2NjE0NTkzNA%253D%253D";
	public static String key = //
	"b28b03434249256bc84540f5c12dc8aa8bf73abe3a76d5944c859ea8642e7b68d321938ec774b73b3561516ae5e7e105";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public WeiXinDownloader() {
		super();
	}

	@Override
	public Page download(Request request, Task task) {
		String realUrl = request.getUrl().replace("{uin}", uin).replace("{key}", key);
		request.setUrl(realUrl);
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
		logger.info("downloading page {}", request.getUrl());
		CloseableHttpResponse httpResponse = null;
		int statusCode = 0;
		try {
			HttpUriRequest httpUriRequest = getHttpUriRequest(request, site, headers);
			httpResponse = getHttpClient(site).execute(httpUriRequest);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			request.putExtra(Request.STATUS_CODE, statusCode);
			if (statusAccept(acceptStatCode, statusCode)) {
				Page page = handleResponse(request, charset, httpResponse, task);
				onSuccess(request);
				return page;
			} else {
				logger.warn("code error " + statusCode + "\t" + request.getUrl());
				return null;
			}
		} catch (IOException e) {
			logger.warn("download page " + request.getUrl() + " error", e);
			if (site.getCycleRetryTimes() > 0) {
				return addToCycleRetry(request, site);
			}
			onError(request);
			return null;
		} finally {
			request.putExtra(Request.STATUS_CODE, statusCode);
			try {
				if (httpResponse != null) {
					// ensure the connection is released back to pool
					EntityUtils.consume(httpResponse.getEntity());
				}
			} catch (IOException e) {
				logger.warn("close response fail", e);
			}
		}
	}

}
