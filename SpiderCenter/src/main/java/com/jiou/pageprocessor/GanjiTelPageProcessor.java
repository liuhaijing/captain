package com.jiou.pageprocessor;

import javax.management.JMException;

import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;
import orestes.bloomfilter.redis.helper.RedisPool;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.RedisScheduler;
import us.codecraft.webmagic.utils.UrlUtils;

import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;
import com.jiou.support.Redis;

@Component("GanjiTelSpider")
@Scope("singleton")
public class GanjiTelPageProcessor implements PageProcessor {

	protected static final String seed = "http://www.ganji.com/index.htm";
	protected static final String mongo_coll = "ganji_tel";

	protected static final String CITY = "city";
	protected static final String CAT = "cat";
	protected static final String CONTACTS = "contacts";
	protected static final String TEL = "tel";
	protected static final String QQ = "qq";

	protected BloomFilter<String> bloomFilter = new FilterBuilder(1000000, 0.001).name("ganji_tel_filter")
			.redisBacked(true).redisPool(new RedisPool(Redis.jedisPool)).buildBloomFilter();

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Site site = //
	Site.me().setRetryTimes(1).setSleepTime(1000).setTimeOut(60000).setUserAgent(UserAgent.IE);

	public GanjiTelPageProcessor() {
		this.start();
	}

	public void process(Page page) {
		int depth = page.getRequest().getDepth();
		if (depth == 0) {// 解析城市列表
			parseCityList(page, depth);
		} else if (depth == 1) {// 解析分类列表
			parseCatList(page, depth);
		} else if (depth == 2) {// 解析货物列表
			parseProList(page, depth);
		} else {// 解析详情
			parseDetail(page);
		}
	}

	private void parseDetail(Page page) {
		page.putField(CITY, page.getRequest().getExtra(CITY));
		page.putField(CAT, page.getRequest().getExtra(CAT));
		Document doc = Jsoup.parse(page.getRawText());
		try {
			String contacts = doc.select("label:contains(联系人)").get(0).nextSibling().toString().trim()
					.replace("&nbsp;", "");
			page.putField(CONTACTS, contacts);
		} catch (Exception ignore) {
		}
		try {
			String tel = doc.select("label:contains(话) ~ span.phoneNum-style").get(0).text().trim();
			tel = StringUtils.deleteWhitespace(tel);
			if (StringUtils.isBlank(tel) || this.bloomFilter.contains(tel) || !tel.matches("\\d+")) {
				page.setSkip(true);
				return;
			} else {
				this.bloomFilter.add(tel);
			}
			page.putField(TEL, tel);
		} catch (Exception ignore) {
			page.setSkip(true);
			return;
		}
		try {
			String qq = doc.select("label:contains(QQ) ~ span.phoneNum-style").get(0).text().trim();
			page.putField(QQ, qq);
		} catch (Exception ignore) {
		}
	}

	private void parseProList(Page page, int depth) {
		page.setSkip(true);
		try {
			int nextdepth = depth + 1;
			String city = (String) page.getRequest().getExtra(CITY);
			String cat = (String) page.getRequest().getExtra(CAT);
			String refer = page.getRequest().getUrl();
			Elements items = Jsoup.parse(page.getRawText()).select(
					"div.layoutlist > dl.list-bigpic.clearfix > dd.feature > div.ft-db > ul > li.js-item > a");
			for (Element e : items) {
				String url = UrlUtils.canonicalizeUrl(e.attr("href").trim(), refer);
				Request req = new Request(url);
				req.putExtra(CITY, city);
				req.putExtra(CAT, cat);
				req.setLast(true);
				req.setDepth(nextdepth);
				page.addTargetRequest(req);
			}
		} catch (Exception ex) {
			logger.error("解析商品列表错误", ex);
		}
	}

	protected static final String prefix1 = "/";
	protected static final String prefix2 = "o";

	private void parseCatList(Page page, int depth) {
		page.setSkip(true);
		int nextdepth = depth + 1;
		try {
			String city = (String) page.getRequest().getExtra(CITY);
			String refer = page.getRequest().getUrl();
			// Elements items =
			// Jsoup.parse(page.getRawText()).select("div.col-two.ershou-eara a[gjalog]");
			Elements items = Jsoup.parse(page.getRawText()).select("div.col-two.ershou-eara span.f12 > a[gjalog]");
			for (Element e : items) {
				String cat = e.text().trim();
				String url = UrlUtils.canonicalizeUrl(e.attr("href").trim(), refer);
				if (StringUtils.isBlank(url)) {
					continue;
				}
				boolean flag = url.endsWith(prefix1);
				for (Integer x = 1; x <= 50; x++) {
					Request req = null;
					if (flag) {
						req = new Request(url.concat(prefix2).concat(x.toString()));
					} else {
						req = new Request(url.concat(prefix1).concat(prefix2).concat(x.toString()));
					}
					req.putExtra(CITY, city);
					req.putExtra(CAT, cat);
					req.setDepth(nextdepth);
					req.setLast(true);
					page.addTargetRequest(req);
				}
			}
		} catch (Exception ex) {
			logger.error("解析分类列表错误", ex);
		}
	}

	private void parseCityList(Page page, int depth) {
		page.setSkip(true);
		int nextdepth = depth + 1;
		try {
			Elements items = Jsoup.parse(page.getRawText()).select("div.all-city > dl > dd > a");
			for (Element e : items) {
				String city = e.text().trim();
				String url = e.attr("href").trim();
				Request req = new Request(url);
				req.setDepth(1);
				req.putExtra(CITY, city);
				req.setDepth(nextdepth);
				page.addTargetRequest(req);
			}
		} catch (Exception ex) {
			logger.error("解析城市列表错误", ex);
		}
	}

	public Site getSite() {
		return site;
	}

	public void start() {
		Spider spider = Spider.create(this)//
				.setUUID("GanjiTelSpider")//
				.setExitWhenComplete(false)//
				.thread(3)//
				.scheduler(new RedisScheduler(Redis.jedisPool))//
				// .addUrl(seed)//
				.addPipeline(new MongoPipeline(Consts.mongo_database_name, mongo_coll));//
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
		spider.start();
	}

	public static void main(String[] args) {
		new GanjiTelPageProcessor();
	}

}
