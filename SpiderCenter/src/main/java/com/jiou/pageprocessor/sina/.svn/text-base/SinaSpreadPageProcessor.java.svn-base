package com.jiou.pageprocessor.sina;

import java.net.URLEncoder;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import com.alibaba.fastjson.JSONObject;
import com.jiou.domain.SinaWeibo;
import com.jiou.downloader.SinaSpreadDownloader;
import com.jiou.pipeline.SinaSpreadPipeline;
import com.jiou.support.Redis;
import com.jiou.support.UnicodeUtils;

public class SinaSpreadPageProcessor extends AbstractSinaPageProcessor {

	private static final String seedFormat = "http://s.weibo.com/weibo/%s&scope=ori&suball=1&Refer=g&page=%d";

	private SinaSpreadDownloader downloader = new SinaSpreadDownloader();

	public void process(Page page) {
		page.setSkip(true);
		try {
			int depth = page.getRequest().getDepth();
			String query = (String) page.getRequest().getExtra(SinaWeibo.query);
			String html = UnicodeUtils.decode(page.getRawText());
			if (depth == 0) {
				Integer p = (Integer) page.getRequest().getExtra(SinaWeibo.page);
				if (p != null && p == 1) {
					int maxpage = parseMaxPage(html);
					if (maxpage > 1) {
						String url = String.format(seedFormat, URLEncoder.encode(query, SinaWeibo.charset), maxpage);
						Request request = new Request(url);
						page.addTargetRequest(request);
					}
				} else {// 解析数据
					parseSeed(page, html);
				}
			} else if (depth == 1) {
				
			}
		} catch (Exception ex) {
			logger.error("新浪微博传播度处理错误", ex);
			logger.error(page.toString());
		}
	}

	private void parseSeed(Page page, String html) {
		try {
			Matcher m = Pattern.compile("\\{\"pid\":\"pl_weibo_direct\".+\\}").matcher(html);
			String str = null;
			if (m.find()) {
				String s = m.group();
				str = JSONObject.parseObject(s).getString("html");
			}
			if (StringUtils.isBlank(str)) {
				logger.error("解析种子数据错误");
				return;
			}
			Document doc = Jsoup.parse(str);
			Element e = doc.select("#pl_weibo_direct > div.search_feed > div > div.feed_lists.W_texta > div.clearfix")
					.last();
			String uid = extractUid(e.select("div.feed_content wbcon > a.W_texta.W_fb[usercard]").get(0)
					.attr("usercard"));
			if (StringUtils.isBlank(uid)) {
				logger.error("解析uid错误");
				return;
			}
			Date pubtime = new Date(Long.parseLong(e.select("div.feed_from W_textb > a[date]").attr("date").trim()));
			int reviews = Integer.parseInt(e.select("span.line S_line1:contains(评论) > em").get(0).text().trim());
			int forwards = Integer.parseInt(e.select("span.line S_line1:contains(转发) > em").get(0).text().trim());
			int likenum = Integer.parseInt(e.select("span.line S_line1:has(i.W_ico12.icon_praised_b) > em").get(0)
					.text().trim());
			String content = e.select("p.comment_txt").get(0).text().trim();
			String url = e.select("div.feed_content wbcon > a.W_texta.W_fb[usercard]").get(0).attr("href").trim();
			String snapshot = this.downloader.download(url).get();

		} catch (Exception ex) {
			logger.error("解析种子数据错误", ex);
		}
	}

	protected int parseMaxPage(String html) {
		int maxpage = -1;
		if (StringUtils.isBlank(html)) {
			return maxpage;
		}
		Matcher m = Pattern.compile("第(\\d+)页").matcher(html);
		while (m.find()) {
			int p = Integer.parseInt(m.group(1));
			maxpage = p > maxpage ? p : maxpage;
		}
		return maxpage;
	}

	public void start() {
		Spider spider = Spider.create(this)//
				.setUUID("SinaSpreadSpider")//
				.scheduler(new RedisScheduler(Redis.jedisPool))//
				.setDownloader(downloader)//
				.addPipeline(new SinaSpreadPipeline())//
				.setExitWhenComplete(false)//
				.thread(1);
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
		spider.addUrl("中国移动宣布6月30日下线短信转飞信业务");
		spider.start();
	}

	public static void main(String[] args) {
		SinaSpreadPageProcessor pageProcessor = new SinaSpreadPageProcessor();
		pageProcessor.start();
	}

}
