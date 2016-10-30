package com.jiou.pageprocessor.sina;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import com.alibaba.fastjson.JSONObject;
import com.jiou.domain.SinaWeibo;
import com.jiou.downloader.SinaWeiboDownloader;
import com.jiou.pipeline.SinaWeiboPipeline;
import com.jiou.support.Redis;

@Component("weiboUserPageProcessor")
public class WeiboUserPageProcessor extends AbstractSinaPageProcessor {

	protected static final String seed = "http://weibo.com/login.php";

	private Spider spider;

	public WeiboUserPageProcessor() {
		super();
		this.spider = Spider.create(this).addPipeline(new SinaWeiboPipeline()).setDownloader(new SinaWeiboDownloader())
				.setScheduler(new RedisScheduler(Redis.jedisPool)).setUUID("WeiboUserSpider").thread(1)
				.setExitWhenComplete(false);
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
		spider.start();
	}

	public void process(Page page) {
		int depth = page.getRequest().getDepth();
		if (depth == 0) {
			parseSeed(page);
		} else if (depth == 1) {
			parseUserList(page);
		} else if (depth == 2) {
			parseUserDetail(page);
		}
	}

	private static final String male = "male";
	private static final String female = "female";

	private void parseUserDetail(Page page) {
		try {
			Request request = page.getRequest();
			page.putField(SinaWeibo.url, request.getUrl());
			page.putField(SinaWeibo.uid, request.getExtra(SinaWeibo.uid));
			page.putField(SinaWeibo.group, request.getExtra(SinaWeibo.group));
			page.putField(SinaWeibo.cat, request.getExtra(SinaWeibo.cat));
			page.putField(SinaWeibo.uname, request.getExtra(SinaWeibo.uname));
			page.putField(SinaWeibo.concern, request.getExtra(SinaWeibo.concern));
			page.putField(SinaWeibo.blognums, request.getExtra(SinaWeibo.blognums));
			page.putField(SinaWeibo.isveri, request.getExtra(SinaWeibo.isveri));
			page.putField(SinaWeibo.isvip, request.getExtra(SinaWeibo.isvip));
			page.putField(SinaWeibo.brief, request.getExtra(SinaWeibo.brief));
			page.putField(SinaWeibo.address, request.getExtra(SinaWeibo.address));
			page.putField(SinaWeibo.label, request.getExtra(SinaWeibo.label));
			try {// 粉丝数
				Matcher m = Pattern.compile("粉丝\\((\\d+)\\)").matcher(page.getRawText());
				if (m.find()) {
					int fans = Integer.parseInt(m.group(1));
					page.putField(SinaWeibo.fans, fans);
				} else {
					page.putField(SinaWeibo.fans, request.getExtra(SinaWeibo.fans));
				}
			} catch (Exception ignore) {
				page.putField(SinaWeibo.fans, request.getExtra(SinaWeibo.fans));
			}
			try {// 性别,1-男,2-女,3-未知
				Matcher m = Pattern.compile("icon_pf_(male|female)").matcher(page.getRawText());
				if (m.find()) {
					String s = m.group(1);
					if (male.equalsIgnoreCase(s)) {
						page.putField(SinaWeibo.gender, 1);// 男
					} else if (female.equalsIgnoreCase(s)) {
						page.putField(SinaWeibo.gender, 2);// 女
					} else {
						page.putField(SinaWeibo.gender, 3);// 未知
					}
				} else {
					page.putField(SinaWeibo.gender, 3);
				}
			} catch (Exception ignore) {
				page.putField(SinaWeibo.gender, 3);
			}
			try {// 微博等级
				Matcher m = Pattern.compile("微博等级(\\d+)").matcher(page.getRawText());
				if (m.find()) {
					int level = Integer.parseInt(m.group(1));
					page.putField(SinaWeibo.level, level);
				}
			} catch (Exception ignore) {
			}
		} catch (Exception ex) {
			logger.error("解析用户详情页错误", ex);
		}
	}

	private void parseUserList(Page page) {
		page.setSkip(true);
		try {
			Request request = page.getRequest();
			String html = page.getRawText();
			if (StringUtils.isBlank(html)) {
				logger.error("博主列表下载为空:{}", request.getUrl());
				return;
			}
			int p = Integer.parseInt(String.valueOf(request.getExtra(SinaWeibo.page)));
			Document doc = Jsoup.parse(html);
			if (p == 1) {// 解析页码
				Elements pageitems = doc.select("div.W_pages > a.page");
				int maxpage = 0;
				for (Element e : pageitems) {
					String s = e.text();
					if (StringUtils.isNotBlank(s) && s.trim().matches("\\d+")) {
						int pa = Integer.parseInt(s.trim());
						maxpage = maxpage > pa ? maxpage : pa;
					}
				}
				for (Integer x = 2; x <= maxpage; x++) {
					Request req = new Request();
					req.setUrl(request.getUrl().concat("?page=").concat(x.toString()));
					req.setDepth(1);
					req.putExtra(SinaWeibo.page, x);
					req.putExtra(SinaWeibo.group, request.getExtra(SinaWeibo.group));
					req.putExtra(SinaWeibo.cat, request.getExtra(SinaWeibo.cat));
					page.addTargetRequest(req);
				}
			}
			// 解析列表页
			Elements items = doc.select("ul.follow_list > li.follow_item.S_line2");
			for (Element e : items) {
				try {
					String url = e.select("a.S_txt1[usercard][title]").get(0).attr("href");
					String uid = extractUid(e.select("a > strong[usercard]").get(0).attr("usercard"));
					if (StringUtils.isBlank(url) || StringUtils.isBlank(uid)) {
						logger.error("解析出url或uid为空", e.toString());
						continue;
					}
					Request req = new Request();
					req.setUrl(url);
					req.setDepth(2);
					req.setLast(true);
					// req.setPriority(Long.MAX_VALUE);
					req.putExtra(SinaWeibo.group, request.getExtra(SinaWeibo.group));
					req.putExtra(SinaWeibo.cat, request.getExtra(SinaWeibo.cat));
					req.putExtra(SinaWeibo.uid, uid);
					req.putExtra(SinaWeibo.uname, e.select("a > strong[usercard]").get(0).text().trim());
					req.putExtra(SinaWeibo.concern,
							Integer.parseInt(e.select("span:contains(关注) > em.count").get(0).text().trim()));// 关注数
					req.putExtra(SinaWeibo.blognums,
							Integer.parseInt(e.select("span:contains(微博) > em.count").get(0).text().trim()));// 微博数
					try {// 是否加V
						boolean isveri = e.select("a > i.W_icon.icon_approve,a > i.W_icon.icon_approve_co").size() > 0;
						req.putExtra(SinaWeibo.isveri, isveri);
					} catch (Exception ignore) {
					}
					try {// 是否微博会员
						boolean isvip = e.select("a[title=微博会员] > i.W_icon.icon_member").size() > 0;
						req.putExtra(SinaWeibo.isvip, isvip);
					} catch (Exception ignore) {
					}
					try {// 简介
						String brief = e.select("div.info_intro:contains(简介) > span").get(0).text().trim();
						req.putExtra(SinaWeibo.brief, brief);
					} catch (Exception ignore) {
					}
					try {// 地址
						String address = e.select("div.info_add:contains(地址) > span").get(0).text().trim();
						req.putExtra(SinaWeibo.address, address);
					} catch (Exception ignore) {
					}
					try {// 标签
						String label = e.select("div.info_relation:contains(标签)").get(0).text().replace("标签：", "")
								.trim();
						req.putExtra(SinaWeibo.label, label);
					} catch (Exception ignore) {
					}
					try {// 粉丝数
						String fans = e.select("span:contains(粉丝) > em.count").get(0).text().trim();
						int fansnum = fans.contains("万") ? Integer.parseInt(fans.replace("万", "")) * 10000 : Integer
								.parseInt(fans);
						req.putExtra(SinaWeibo.fans, fansnum);
					} catch (Exception ignore) {
					}
					page.addTargetRequest(req);
				} catch (Exception ignore) {
					logger.error("解析用户错误", ignore);
					logger.error(e.toString());
				}
			}
		} catch (Exception ex) {
			logger.error("解析用户列表页错误", ex);
		}
	}

	private void parseSeed(Page page) {
		page.setSkip(true);
		try {
			String html = page.getRawText();
			String str = null;
			Matcher m = Pattern.compile("FM.view\\((\\{\"pid\":\"pl_unlogin_home_hotpersoncategory.+)\\)")
					.matcher(html);
			if (m.find()) {
				str = m.group(1);
				// logger.info(str);
			}
			if (StringUtils.isBlank(str)) {
				logger.error("解析种子页面为空:{}", page.getRequest().getUrl());
				return;
			}
			Elements items = Jsoup.parse(JSONObject.parseObject(str).getString("html")).select(
					"div.WB_innerwrap > div.m_wrap.clearfix > div.list_wrap");
			int count = 0;
			for (Element e : items) {
				String group = e.select("h3").get(0).text().trim();
				Elements subitems = e.select("ul > li > a");
				for (Element sube : subitems) {
					String cat = sube.select("span").get(0).text().trim();
					String url = sube.attr("href");
					Request request = new Request(url);
					request.putExtra(SinaWeibo.group, group);
					request.putExtra(SinaWeibo.cat, cat);
					request.putExtra(SinaWeibo.page, 1);
					request.setDepth(1);
					page.addTargetRequest(request);
					count++;
				}
			}
			logger.info("种子页解析出任务数:{}", count);
		} catch (Exception ex) {
			logger.error("解析种子页错误", ex);
		}
	}

	public void start() {
		spider.addUrl(seed);
	}

	public static void main(String[] args) {
		new WeiboUserPageProcessor().start();
	}
}
