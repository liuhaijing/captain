package com.jiou.pageprocessor.wx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;
import orestes.bloomfilter.redis.helper.RedisPool;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.exec.OS;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.UrlUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.net.HttpHeaders;
import com.jiou.cookiemanager.CookieManager;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.SogouPipeline;
import com.jiou.support.Mayi;
import com.jiou.support.Redis;

public class SogouPageProcessor implements PageProcessor {

	private static final String contentUrl = "http://mp.weixin.qq.com/";
	private static final String commentUrl = "http://mp.weixin.qq.com/mp/getcomment";
	private static final String suffix = "#wechat_redirect";
	private static final String filterName = "sogou_bloom_filter";

	private Logger logger;
	private Site site;
	private CookieManager cookieManager;
	private CloseableHttpClient client;
	private BloomFilter<String> bloomFilter;

	public SogouPageProcessor() {
		this.logger = LoggerFactory.getLogger(getClass());
		this.site = Site.me().setRetryTimes(1).setSleepTime(1000).setUserAgent(UserAgent.CHROME);
		this.bloomFilter = new FilterBuilder(10000000, 0.001).name(filterName).redisBacked(true)
				.redisPool(new RedisPool(Redis.jedisPool)).buildBloomFilter();
		Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(reg);
		connectionManager.setDefaultMaxPerRoute(20);
		this.client = HttpClientBuilder
				.create()
				.disableCookieManagement()
				.setConnectionManager(connectionManager)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(site.getRetryTimes(), true))
				.setDefaultRequestConfig(
						RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build()).build();
	}

	public void process(Page page) {
		String wxnum = (String) page.getRequest().getExtra(SogouPipeline.wxnum);
		String refer = page.getRequest().getUrl();
		if (StringUtils.isBlank(wxnum)) {
			logger.warn("微信号不能为空!");
			return;
		}
		try {
			Map<String, Object> accountMap = parseAccount(page, wxnum);
			if (accountMap == null) {
				logger.warn("解析微信号:{}搜索页面错误", wxnum);
				return;
			} else {
				page.putField(SogouPipeline.account, accountMap);
			}
			String listurl = (String) accountMap.get(SogouPipeline.url);
			String listHtml = null;
			int exeCount = 0;
			boolean success = false;
			while (!success && exeCount <= site.getRetryTimes()) {
				exeCount++;
				listHtml = getHtml(listurl, false);
				if (!StringUtils.isBlank(listHtml)) {
					success = true;
				}
			}
			if (StringUtils.isBlank(listHtml)) {
				logger.error("下载列表页错误:wxnum={},url={}", wxnum, listurl);
				return;
			}
			
			/* chijy update 829 
			List<Map<String, Object>> arts = parseArticleList(listHtml, wxnum, refer);
			if (arts == null || arts.size() == 0) {
				logger.error("解析微信文章列表页为空:wxnum={}", wxnum);
				return;
			}
			List<Map<String, Object>> finalarts = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> finalnums = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> artmap : arts) {
				try {
					String queryurl = (String) artmap.get(SogouPipeline.url);
					if (StringUtils.isBlank(queryurl)) {
						continue;
					}
					String uid = (String) artmap.get(SogouPipeline.uid);
					// 抓取文章详情
					if (!this.bloomFilter.contains(uid)) {
						// if (!sogouPipeline.existArt(uid)) {
						String url = UrlUtils.canonicalizeUrl(queryurl, contentUrl);
						exeCount = 0;
						success = false;
						String dethtml = null;
						while (!success && exeCount <= site.getRetryTimes()) {
							exeCount++;
							dethtml = getHtml(url, true);
							if (!StringUtils.isBlank(dethtml)) {
								success = true;
							}
						}
						if (StringUtils.isBlank(dethtml) || dethtml.contains("系统出错")) {
							logger.error("下载文章详情错误:{}-->{}", wxnum, url);
						} else {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put(SogouPipeline.wxnum, wxnum);
							map.putAll(artmap);
							map.put(SogouPipeline.html, dethtml);
							Document snapshot = Jsoup.parse(dethtml);
							map.put(SogouPipeline.content, snapshot.select("#js_content").get(0).text().trim());
							map.put(SogouPipeline.original, getOri(snapshot.select("#copyright_logo")));
							map.put(SogouPipeline.url, url);
							finalarts.add(map);
						}
					}
					// 抓取文章阅读数和点赞数
					String numurl = commentUrl + queryurl.substring(queryurl.indexOf("?"));
					String commenthtml = null;
					exeCount = 0;
					success = false;
					while (!success && exeCount <= site.getRetryTimes()) {
						exeCount++;
						commenthtml = getHtml(numurl, false);
						if (!StringUtils.isBlank(commenthtml)) {
							success = true;
						}
					}
					if (StringUtils.isBlank(commenthtml)) {
						logger.error("下载文章阅读点赞数错误错误:{}-->{}", wxnum, numurl);
					} else {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(SogouPipeline.wxnum, wxnum);
						map.put(SogouPipeline.uid, uid);
						int readnum = -1;
						int likenum = -1;
						try {
							JSONObject json = JSONObject.parseObject(commenthtml);
							readnum = json.getIntValue(SogouPipeline.read_num);
							likenum = json.getIntValue(SogouPipeline.like_num);
						} catch (Exception ignore) {
							// logger.error("解析阅读点赞数错误:{}", commenthtml);
						}
						if (readnum != -1) {
							map.put(SogouPipeline.read_num, readnum);
							map.put(SogouPipeline.like_num, likenum);
							finalnums.add(map);
						}
					}
				} catch (Exception ex) {
					logger.error("处理文章列表错误", ex);
				}
			}
			page.putField(SogouPipeline.artlist, finalarts);
			page.putField(SogouPipeline.numlist, finalnums);
		*/	
		} catch (Throwable ex) {
			logger.error("搜狗微信错误", ex);
		}
	}

	private List<Map<String, Object>> parseArticleList(String html, String wxnum, String refer) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			Matcher m = Pattern.compile("var msgList = '\\{.+\\}").matcher(html);
			if (m.find()) {
				String s = m.group().replace("var msgList = '", "").trim().replace("&quot;", "\"")
						.replace("&amp;", "&");
				JSONArray items = JSONObject.parseObject(s).getJSONArray("list");
				for (int x = 0; x < items.size(); x++) {
					int idx = 0;
					JSONObject json = items.getJSONObject(x);
					Date pubtime = new Date(json.getJSONObject("comm_msg_info").getLongValue("datetime") * 1000);
					int ismulti = json.getJSONObject("app_msg_ext_info").getIntValue("is_multi");
					try {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(SogouPipeline.idx, ++idx);
						map.put(SogouPipeline.pubtime, pubtime);
						String title = json.getJSONObject("app_msg_ext_info").getString("title").trim();
						map.put(SogouPipeline.title, title);
						map.put(SogouPipeline.uid, genUid(wxnum, title, pubtime));
						map.put(SogouPipeline.ismulti, ismulti);
						try {
							map.put(SogouPipeline.cover, json.getJSONObject("app_msg_ext_info").getString("cover")
									.trim().replace("\\", "").replace("&amp;", "&"));
						} catch (Exception ignore) {
						}
						try {
							map.put(SogouPipeline.source_url,
									json.getJSONObject("app_msg_ext_info").getString("source_url").trim()
											.replace("\\", "").replace("&amp;", "&"));
						} catch (Exception ignore) {
						}
						String url = json.getJSONObject("app_msg_ext_info").getString("content_url").trim()
								.replace("\\/", "").replace("&amp;", "&");
						if (StringUtils.isNotBlank(url) && !url.endsWith(suffix)) {
							map.put(SogouPipeline.url, url);
							list.add(map);
						}
					} catch (Exception ignore) {
					}
					JSONArray subitem = null;
					try {
						subitem = json.getJSONObject("app_msg_ext_info").getJSONArray("multi_app_msg_item_list");
					} catch (Exception ignore) {
					}
					if (subitem != null) {
						for (int y = 0; y < subitem.size(); y++) {
							try {
								Map<String, Object> map = new HashMap<String, Object>();
								map.put(SogouPipeline.idx, ++idx);
								map.put(SogouPipeline.pubtime, pubtime);
								JSONObject subjson = subitem.getJSONObject(y);
								String title = subjson.getString("title").trim();
								map.put(SogouPipeline.title, title);
								map.put(SogouPipeline.uid, genUid(wxnum, title, pubtime));
								map.put(SogouPipeline.ismulti, ismulti);
								try {
									map.put(SogouPipeline.cover, subjson.getString("cover").trim().replace("\\", "")
											.replace("&amp;", "&"));
								} catch (Exception ignore) {
								}
								try {
									map.put(SogouPipeline.source_url,
											subjson.getString("source_url").trim().replace("\\", "")
													.replace("&amp;", "&"));
								} catch (Exception ignore) {
								}
								String url = subjson.getString("content_url").trim().trim().replace("\\/", "")
										.replace("&amp;", "&");
								if (StringUtils.isNotBlank(url) && !url.endsWith(suffix)) {
									map.put(SogouPipeline.url, url);
									list.add(map);
								}
							} catch (Exception ignore) {
							}
						}
					}
				}
			} else {
				logger.debug("====>解析文章列表为空");
				logger.debug(html);
			}
		} catch (Exception ex) {
			logger.error("解析文章列表错误", ex);
			// logger.error(html);
		}
		return list;
	}

	private Map<String, Object> parseAccount(Page page, String wxnum) {
		try {
			String html = page.getRawText();
			if (html.contains("相关的官方认证订阅号")) {
				logger.warn("微信号:{}不存在!", wxnum);
				return null;
			}
			Document doc = Jsoup.parse(html);
			Element e = doc.select("#main > div.weixin-public > div > div.results.mt7 > div._item").get(0);
			String url = UrlUtils.canonicalizeUrl(e.attr("href"), page.getRequest().getUrl());// 公众号URL
			if (StringUtils.isBlank(url)) {
				return null;
			}
			String wxname = e.select("div.txt-box > h3").get(0).text().trim();// 微信名称
			String portrait_url = e.select("div.img-box > img").get(0).attr("src");// 头像URL
			byte[] portrait = null;// 头像
			try {
				portrait = loadBytes(portrait_url);
			} catch (Exception ignore) {
			}
			String qrcode_url = e.select("div.pos-ico > div.pos-box > img").get(0).attr("src");// 二维码URL
			byte[] qrcode = null;// 二维码
			try {
				qrcode = loadBytes(qrcode_url);
			} catch (Exception ignore) {
			}
			String brief = null;// 简介
			try {
				brief = e.select("span.sp-tit:contains(功能介绍：) ~ span").get(0).text().trim();
			} catch (Exception ignore) {
			}
			String auth = null;// 认证
			try {
				auth = e.select("span.sp-tit:contains(认证：) ~ span").get(0).text().trim();
			} catch (Exception ignore) {
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(SogouPipeline.wxnum, wxnum);
			map.put(SogouPipeline.wxname, wxname);
			map.put(SogouPipeline.url, url);
			map.put(SogouPipeline.portrait_url, portrait_url);
			map.put(SogouPipeline.portrait, portrait);
			map.put(SogouPipeline.qrcode, qrcode);
			map.put(SogouPipeline.brief, brief);
			map.put(SogouPipeline.auth, auth);
			return map;
		} catch (Throwable e) {
			logger.error("解析微信搜索页面错误", e);
			// logger.error(page.getRawText());
		}
		return null;
	}

	public Site getSite() {
		return site;
	}

	private String genUid(String wxnum, String title, Date pubtime) {
		return DigestUtils.md5Hex(wxnum + title + pubtime);
	}

	private static final String data_src = "data-src";
	private static final String src = "src";

	protected String getHtml(String url, boolean snapshot) {
		String cookie = null;
		String html = null;
		boolean forbid = false;
		try {
			TimeUnit.MILLISECONDS.sleep(200);
			cookie = this.cookieManager.get();
			HttpUriRequest request = RequestBuilder.get(url).addHeader(HttpHeaders.USER_AGENT, UserAgent.CHROME)
					.addHeader(HttpHeaders.COOKIE, cookie).build();
//			String authHeader = Mayi.buildAuthHeader();
//			HttpUriRequest request = RequestBuilder
//					.get(url)
//					.addHeader(HttpHeaders.USER_AGENT, UserAgent.CHROME)
//					.addHeader(HttpHeaders.COOKIE, cookie)
//					.addHeader(Mayi.auth, authHeader)
//					.setConfig(
//							RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(30000)
//									.setProxy(new HttpHost(Mayi.host, Mayi.port)).build()).build();
			CloseableHttpResponse resp = this.client.execute(request);
			html = HttpClientUtils.getHtml(resp, true);
			forbid = forbid(html);
		} catch (Exception e) {
			logger.error("下载网页错误url={}", url);
			// logger.error("下载网页错误", e);
		} finally {
			if (forbid) {
				this.cookieManager.put(cookie, false);
			} else {
				this.cookieManager.put(cookie, true);
			}
		}
		if (forbid) {
			return null;
		} else {
			if (snapshot) {
				html = UrlUtils.fixAllRelativeSrcs(html.replace(data_src, src), url);
				if (OS.isFamilyWindows()) {
					try {
						FileUtils.writeStringToFile(new File("d:/sogou.html"), html);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				logger.info("下载:{}", url);
			}
			return html;
		}
	}

	protected byte[] loadBytes(String url) {
		CloseableHttpResponse resp = null;
		String cookie = null;
		try {
			TimeUnit.MILLISECONDS.sleep(50);
			cookie = this.cookieManager.get();
			HttpGet request = new HttpGet(url);
			request.addHeader(HttpHeaders.USER_AGENT, UserAgent.CHROME);
			if (StringUtils.isNotBlank(cookie)) {
				request.addHeader(HttpHeaders.COOKIE, cookie);
			}
			resp = client.execute(request);
			logger.info("下载图片url:{}", url);
			return EntityUtils.toByteArray(resp.getEntity());
		} catch (Exception e) {
			logger.error("下载图片错误url={}", url);
			// logger.error("下载图片错误", e);
		} finally {
			if (cookie != null) {
				this.cookieManager.put(cookie, true);
			}
			HttpClientUtils.close(resp);
		}
		return null;
	}

	private boolean getOri(Elements items) {
		if (items != null && items.size() > 0 && items.get(0).text().contains("原创")) {
			return true;
		}
		return false;
	}

	private boolean forbid(String html) {// 被禁
		if (StringUtils.isNotBlank(html) && html.contains("访问过于频繁")) {
			logger.info("被封了......");
			return true;
		}
		return false;
	}

	public void setCookieManager(CookieManager cookieManager) {
		this.cookieManager = cookieManager;
	}

}
