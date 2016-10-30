package com.jiou.support;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import com.google.common.base.Joiner;
import com.google.common.net.HttpHeaders;
import com.jiou.httpclients.HttpClientUtils;

public class Mayi {
	private static final String appkey = "36302991";
	private static final String secret = "fec83ed4c11d4b1b47724d3f7982e52f";
	public static final String host = "123.56.238.200";
	public static final int port = 8123;
	public static final String auth = "Proxy-Authorization";

	public static void main(String[] args) throws Exception {
		// String url =
		// "http://weibo.com/u/2063148581?refer_flag=1087030701_2975_7002_0";
		String url = "http://1212.ip138.com/ic.asp";
		while (true) {
			String authHeader = buildAuthHeader();
			RequestConfig.Builder requestBuilder = RequestConfig.custom().setSocketTimeout(30000)
					.setConnectTimeout(30000);
			HttpUriRequest request = RequestBuilder.get().setUri(url)
					.setConfig(requestBuilder.setProxy(new HttpHost(host, port)).build()).addHeader(auth, authHeader)
					.addHeader(HttpHeaders.USER_AGENT, "360Spider").build();
			CloseableHttpResponse resp = HttpClientUtils.execute(request);
			System.out.println(HttpClientUtils.getHeaderValue(resp, "X-User-Agent"));
			String html = HttpClientUtils.getHtml(resp, true);
			System.out.println(html);
			TimeUnit.SECONDS.sleep(1l);
		}
	}

	public static String buildAuthHeader() {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("app_key", appkey);
		paramMap.put("random-useragent", "disabled");
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("GMT+8"));// 使用中国时间，以免时区不同导致认证错误
		paramMap.put("timestamp", format.format(new Date()));
		String[] keyArray = paramMap.keySet().toArray(new String[0]);
		Arrays.sort(keyArray);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(secret);
		for (String key : keyArray) {
			stringBuilder.append(key).append(paramMap.get(key));
		}
		stringBuilder.append(secret);
		String codes = stringBuilder.toString();
		String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(codes).toUpperCase();
		paramMap.put("sign", sign);
		String authHeader = "MYH-AUTH-MD5 " + Joiner.on('&').withKeyValueSeparator("=").join(paramMap);
		return authHeader;
	}
}
