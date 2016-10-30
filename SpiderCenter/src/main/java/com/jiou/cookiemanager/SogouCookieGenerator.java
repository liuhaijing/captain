package com.jiou.cookiemanager;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.alibaba.fastjson.JSON;
import com.google.common.net.HttpHeaders;
import com.jiou.coding.Coding;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.httpclients.UserAgent;
import com.jiou.support.Consts;
import com.jiou.support.Mayi;

@SuppressWarnings("unused")
public class SogouCookieGenerator {

	protected static Logger logger = LoggerFactory.getLogger(SogouCookieGenerator.class);

	private static final String SUID_URL = "http://weixin.sogou.com";
	private static final String SNUID_URL = "http://weixin.sogou.com/weixin?query=%s";
	private static final String SUV_URL = "http://pb.sogou.com/pb.js";

	public static final String SUID = "SUID";
	public static final String SUV = "SUV";
	public static final String SNUID = "SNUID";

	// private static final String antiurl =
	// "http://weixin.sogou.com/antispider";
	private static final String imgurlfor = "http://weixin.sogou.com/antispider/util/seccode.php?tc=%s";
	private static final String referurl = "http://weixin.sogou.com/antispider/?from=";
	private static final String coposturl = "http://weixin.sogou.com/antispider/thank.php";

	public String generate() {

		StringBuilder cookie = new StringBuilder();

		RequestConfig.Builder requestBuilder = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000);
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestBuilder.build()).build();

		HttpGet request = new HttpGet();
		request.addHeader(HttpHeaders.USER_AGENT, UserAgent.CHROME);

		CloseableHttpResponse resp = null;
		request.setURI(URI.create(SUID_URL));
		String suid = null;
		try {
			resp = client.execute(request);
			suid = HttpClientUtils.getCookie(resp, SUID);
			if (StringUtils.isBlank(suid)) {
				logger.warn("获取cookie错误:{}", SUID);
				return null;
			}
			cookie.append(SUID).append("=").append(suid).append(";");
		} catch (Exception e) {
			logger.error("搜狗微信生成cookie错误", e.getMessage());
			return null;
		} finally {
			HttpClientUtils.close(resp);
		}

		String suv = null;
		try {
			request.setURI(URI.create(SUV_URL));
			request.setConfig(requestBuilder.build());
			resp = client.execute(request);
			suv = HttpClientUtils.getCookie(resp, SUV);
			if (StringUtils.isBlank(suv)) {
				logger.warn("获取cookie错误:{}", SUV);
				return null;
			}
			cookie.append(SUV).append("=").append(suv).append(";");
		} catch (Exception e) {
			logger.error("搜狗微信生成cookie错误", e.getMessage());
			return null;
		} finally {
			HttpClientUtils.close(resp);
		}

		try {
			String url = String.format(SNUID_URL, UUID.randomUUID());
			request.setURI(URI.create(url));
			request.setConfig(requestBuilder.setProxy(new HttpHost(Mayi.host, Mayi.port)).build());
			String authHeader = Mayi.buildAuthHeader();
			request.addHeader(Mayi.auth, authHeader);
			resp = client.execute(request);
			String html = HttpClientUtils.getHtml(resp);
			String snuid = null;
			if (html != null && html.contains("访问过于频繁")) {
				logger.warn("被封了,打码ing...");
				// Document doc = Jsoup.parse(html);
				// String refer = referurl + doc.select("#from").get(0).val();
				// String imgurl = String.format(imgurlfor, new
				// Date().getTime());
				// snuid = this.code(client, cookie.toString(), suid, suv,
				// refer, imgurl);
			} else {
				snuid = HttpClientUtils.getCookie(resp, SNUID);
			}
			if (StringUtils.isBlank(snuid)) {
				logger.warn("获取cookie错误:{}", SNUID);
				return null;
			} else {
				cookie.append(SNUID).append("=").append(snuid);
				// if (OS.isFamilyWindows()) {// windows上检测下成功否
				// request.addHeader(HttpHeaders.COOKIE, cookie.toString());
				// resp = client.execute(request);
				// logger.info(HttpClientUtils.getHtml(resp));
				// }
			}
		} catch (Exception e) {
			logger.error("搜狗微信生成cookie错误", e);
			return null;
		} finally {
			HttpClientUtils.close(resp);
		}

		String cookiestr = cookie.toString();
		logger.info("generate cookie:{}", cookiestr);
		return cookiestr;
	}

	// http://pb.sogou.com/pv.gif?uigs_productid=webapp&type=antispider&subtype=imgCost&domain=weixin&suv=%s&snuid=%s&t=%d&cost=13403931
	// http://pb.sogou.com/pv.gif?uigs_productid=webapp&type=antispider&subtype=seccodeBlur&domain=weixin&suv=%s&snuid=%s&t=%d
	// http://pb.sogou.com/pv.gif?uigs_productid=webapp&type=antispider&subtype=seccodeFocus&domain=weixin&suv=%s&snuid=%s&t=%d

	private String code(CloseableHttpClient client, String cookie, String suid, String suv, String refer, String imgurl) {
		HttpUriRequest request = RequestBuilder.get()//
				// .addHeader(HttpHeaders.COOKIE, cookie)//
				.addHeader(HttpHeaders.USER_AGENT, UserAgent.CHROME)//
				.addHeader(HttpHeaders.REFERER, refer)//
				.setUri(imgurl).build();
		CloseableHttpResponse resp = null;
		try {
			resp = client.execute(request);
			// logger.info("搜狗cookie为:{}", HttpClientUtils.getCookie(resp));
			byte[] bytes = HttpClientUtils.getBytes(resp);
			if (OS.isFamilyWindows()) {
				FileUtils.writeByteArrayToFile(new File("D:/sougo.jpg"), bytes);
			}
			HttpClientUtils.close(resp);
			String checkcode = null;
			checkcode = Coding.coding(bytes);
			request = RequestBuilder.post()//
					// .addHeader(HttpHeaders.COOKIE, cookie)//
					.addHeader(HttpHeaders.USER_AGENT, UserAgent.CHROME)//
					.addHeader(HttpHeaders.REFERER, refer)//
					.addParameter("c", checkcode)//
					.addParameter("r", refer)//
					.addParameter("v", "5")//
					.setUri(coposturl).build();
			resp = client.execute(request);
			String html = HttpClientUtils.getHtml(resp);
			logger.info(html);
			int code = JSON.parseObject(html).getIntValue("code");
			if (code == 0) {
				return JSON.parseObject(html).getString("id");
			}
		} catch (Exception e) {
			logger.error("搜狗打码错误", e);
		}
		return null;
	}

	public static void main(String[] args) {
		String cookie = new SogouCookieGenerator().generate();
		System.out.println(cookie);
	}

}
