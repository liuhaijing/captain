package com.jiou.service;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import com.google.common.net.HttpHeaders;
import com.jiou.cookiemanager.CookieManager;
import com.jiou.cookiemanager.SogouLoginCookieManager;
import com.jiou.downloader.SogouDownloader;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.httpclients.UserAgent;
import com.jiou.pageprocessor.wx.SogouArticlePageProcessor;
import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;
import com.jiou.support.Mayi;
import com.jiou.support.Redis;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@SuppressWarnings("deprecation")
@Component("sogouQueryService")
public class SogouQueryService {
	public static final String task_list = "sogou_task_list";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public static final String format = //
	"http://weixin.sogou.com/weixin?query=%s&_sug_type_=&_sug_=y&type=2&page=%d&ie=utf8";

	protected DBCollection keysColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
			.getCollection("sogou_keys");

	private CookieManager cookieManager = new SogouLoginCookieManager(Redis.jedisPool);

	private Spider spider;

	public synchronized void start() {
		initSpider();
		cookieManager.reload();
		try {
			DBCollection coll = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
					.getCollection(task_list);
			Iterator<DBObject> it = coll.find().iterator();
			while (it.hasNext()) {
				String query = (String) it.next().get("query");
				try {
					if (StringUtils.isBlank(query)) {
						continue;
					}
					// System.out.println("---" + query + "---");
					logger.info("start fetch:{}", query);
					fetch(query.trim());
				} catch (Exception ex) {
					logger.error("fetch error:{}", query);
					logger.error("fetch error", ex);
				}
			}
		} catch (Exception e) {
			logger.error("fetch sogou article list error", e);
		}
	}

	public synchronized void interval() {
		initSpider();

	}

	protected void fetch(String query) throws Exception {
		String utfquery = URLEncoder.encode(query, "UTF-8");
		String url = String.format(format, utfquery, 1);
		logger.info("Download:{}", url);
		Document doc = get(url);
		int searchCount = Integer.parseInt(doc.select("#scd_num").get(0).text().trim().replace(",", ""));
		int maxpage = searchCount % 10 == 0 ? searchCount / 10 : searchCount / 10 + 1;
		logger.info("====>searchCount:{},maxpage:{}", searchCount, maxpage);
		maxpage = maxpage > 100 ? 100 : maxpage;
		parseData(doc, query);
		for (int x = 2; x <= maxpage; x++) {
			try {
				TimeUnit.SECONDS.sleep(5);
				url = String.format(format, utfquery, x);
				logger.info("Download:{}", url);
				doc = get(url);
				parseData(doc, query);
			} catch (Exception ex) {
				logger.error("错误:", ex);
			}
		}
	}

	protected void parseData(Document doc, String query) {
		try {
			Elements items = doc.select("div.results > div.wx-rb.wx-rb3[id]");
			logger.info("解析出列表条数:{}", items.size());
			for (Element e : items) {
				String url = e.select("div.txt-box > h4 > a[id]").get(0).attr("href").trim();
				long pubtime = Long.parseLong((e.select("div.s-p[t]").get(0).attr("t"))) * 1000;
				Request request = new Request(url);
				request.putExtra("query", query);
				request.putExtra("pubtime", pubtime);
				spider.addRequest(request);
			}
		} catch (Exception ex) {
			logger.error("解析错误", ex);
			logger.error(doc.html());
		}
	}

	protected long parsePubtime(String text) {
		if (StringUtils.isBlank(text)) {
			return 0;
		}
		Matcher m = Pattern.compile("'(\\d+)'").matcher(text);
		if (m.find()) {
			return Long.parseLong(m.group(1)) * 1000;
		}
		return 0;
	}

	protected Document get(String url) throws Exception {
		String cookie = cookieManager.get();
		try {
			HttpUriRequest request = RequestBuilder
					.get()
					.setUri(url)
					.addHeader(HttpHeaders.COOKIE, cookie)
					.addHeader(HttpHeaders.USER_AGENT, UserAgent.IE)
					.addHeader(Mayi.auth, Mayi.buildAuthHeader())
					.setConfig(
							RequestConfig.custom().setProxy(new HttpHost(Mayi.host, Mayi.port)).setSocketTimeout(60000)
									.setConnectionRequestTimeout(60000).build()).build();
			CloseableHttpResponse resp = HttpClientUtils.execute(request);
			String html = HttpClientUtils.getHtml(resp, true);
			// logger.info(html);
			return Jsoup.parse(html);
		} finally {
			cookieManager.put(cookie, true);
		}
	}

	protected void initSpider() {
		if (spider == null) {
			SogouDownloader downloader = new SogouDownloader();
			spider = Spider.create(new SogouArticlePageProcessor())//
					.addPipeline(new MongoPipeline(Consts.mongo_database_name, SogouArticlePageProcessor.coll_name))//
					.setScheduler(new RedisScheduler(Redis.jedisPool))//
					.setDownloader(downloader)//
					.setUUID(SogouArticlePageProcessor.uuid)//
					.setExitWhenComplete(false)//
					.thread(5);
			try {
				SpiderMonitor.instance().register(spider);
			} catch (JMException e) {
				e.printStackTrace();
			}
			spider.start();
		}
	}

	class FetchTask implements Runnable {
		private String query;

		public FetchTask(String query) {
			this.query = query;
		}

		public void run() {
			try {
				fetch(this.query);
			} catch (Exception e) {
				logger.error("抓取错误", e);
			}
		}
	}

	public static void main(String[] args) {
		SogouQueryService service = new SogouQueryService();
		service.start();
	}

}
