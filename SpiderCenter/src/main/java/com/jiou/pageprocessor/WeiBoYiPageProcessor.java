package com.jiou.pageprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.scheduler.QueueScheduler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jiou.cookiemanager.CookieManager;
import com.jiou.cookiemanager.WeiBoYiCookieManager;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;

/**
 * 微播易
 * 
 * @author zhe.li
 */
@Deprecated
public class WeiBoYiPageProcessor implements PageProcessor {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public static final String mongo_collection_name = "weiboyi";
	public static final String mongo_collection_name_friends = "weiboyi_friends";
	public static final String mongo_collection_name_pai = "weiboyi_mmpai";

	public static final int media_weichat = 1;// 微信公众号
	// private static final int media_friends = 2;
	// private static final int media_weibo_sina = 3;
	// private static final int media_meipai = 4;
	// private static final int media_miaopai = 5;
	// private static final int media_qq = 5;
	// private static final int media_weishi = 6;
	// private static final int media_weibo_qq = 7;

	private static final String format = //
	"http://chuanbo.weiboyi.com/hworder/weixin/filterlist/source/all?web_csrf_token=57061a28ed81a&price_keys%%5Btop%%5D=1&start=%d&limit=20&category_filter=%d";
	// private static final String format =//
	// "http://chuanbo.weiboyi.com/hworder/weixin/filterlist/source/all?web_csrf_token=57061a28ed81a&price_keys%%5Btop%%5D=1&start=%d&limit=20";

	private CookieManager cookieManager = new WeiBoYiCookieManager(4);

	private Site site = Site.me().setRetryTimes(1).setTimeOut(30000).setUserAgent(UserAgent.CHROME).setSleepTime(5000);

	public Site getSite() {
		String cookies = cookieManager.get();
		if (StringUtils.isNotBlank(cookies)) {
			for (String s : cookies.split(";")) {
				String[] arr = s.split("\\=", 2);
				site.addCookie(arr[0], arr[1]);
			}
		}
		return site;
	}

	public void process(Page page) {
		String html = page.getJson().get();
		// html = UnicodeUtils.decode(html);
		if (html.contains("广告主登录")) {
			cookieManager.reload();
			// weiBoYiService.getCookieManager().reload();
			Request request = page.getRequest();
			request.setCookie(cookieManager.get());
			page.addTargetRequest(request);
			return;
		}

		JSONObject jsonObject = null;
		try {
			jsonObject = JSONObject.parseObject(html);
		} catch (Exception ignore) {
			logger.error("解析json错误", ignore);
			logger.error(html);
			return;
		}

		Integer start = (Integer) page.getRequest().getExtra("start");
		if (start == null || start == 0) {
			int total = jsonObject.getJSONObject("data").getIntValue("total");
			logger.info("===>解析出总数为：{}", total);
			start = 20;
			while (start < total) {
				String url = String.format(format, start);
				Request request = new Request(url);
				request.putExtra("start", start);
				page.addTargetRequest(request);
				start += 20;
			}
		}

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JSONArray items = jsonObject.getJSONObject("data").getJSONArray("rows");
		for (int x = 0; x < items.size(); x++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("media", media_weichat);

			JSONObject json = items.getJSONObject(x).getJSONObject("cells");
			try {
				String weichatno = json.getString("weibo_id");
				if (StringUtils.isBlank(weichatno)) {
					continue;
				}
				map.put("weichatno", weichatno);// 微信号
			} catch (Exception ignore) {
			}
			try {
				map.put("nickname", json.getString("weibo_name"));// 微信昵称
			} catch (Exception ignore) {
			}
			try {
				map.put("funsnum", json.getString("followers_count"));// 粉丝数
			} catch (Exception ignore) {
			}
			JSONObject priceJson = json.getJSONObject("external_reference_price");
			if (priceJson != null) {
				try {// 单图文最低报价
					map.put("single_price_min", priceJson.getJSONObject("reference_price_range_single")
							.getString("min"));
				} catch (Exception ignore) {
				}
				try {// 单图文最高报价
					map.put("single_price_max", priceJson.getJSONObject("reference_price_range_single")
							.getString("max"));
				} catch (Exception ignore) {
				}
				try {// 多图文第一条最低报价
					map.put("first_price_min",
							priceJson.getJSONObject("reference_price_range_multi_top").getString("min"));
				} catch (Exception ignore) {
				}
				try { // 多图文第一条最高报价
					map.put("first_price_max",
							priceJson.getJSONObject("reference_price_range_multi_top").getString("max"));
				} catch (Exception ignore) {
				}
				try { // 多图文第二条最低报价
					map.put("second_price_min", priceJson.getJSONObject("reference_price_range_multi_second")
							.getString("min"));
				} catch (Exception ignore) {
				}
				try {// 多图文第二条最高报价
					map.put("second_price_max", priceJson.getJSONObject("reference_price_range_multi_second")
							.getString("max"));
				} catch (Exception ignore) {
				}
				try { // 多图文第3-n条最低报价
					map.put("third_price_min",
							priceJson.getJSONObject("reference_price_range_multi_other").getString("min"));
				} catch (Exception ignore) {
				}
				try { // 多图文第3-n条最高报价
					map.put("third_price_max",
							priceJson.getJSONObject("reference_price_range_multi_other").getString("max"));
				} catch (Exception ignore) {
				}
			}
			JSONObject grossJson = json.getJSONObject("gross_deal_price");
			if (grossJson != null) {
				try {// 单图文硬广报价
					map.put("single_price_hard", grossJson.getIntValue("single_graphic_price"));
				} catch (Exception ignore) {
				}
				try {// 单图文软广报价
					map.put("single_price_soft", grossJson.getIntValue("soft_single_graphic_price"));
				} catch (Exception ignore) {
				}
				try {// 多图文第一条硬广报价
					map.put("first_price_hard", grossJson.getIntValue("multi_graphic_top_price"));
				} catch (Exception ignore) {
				}
				try { // 多图文第一条软广报价
					map.put("first_price_soft", grossJson.getIntValue("soft_multi_graphic_top_price"));
				} catch (Exception ignore) {
				}
				try { // 多图文第二条硬广报价
					map.put("second_price_hard", grossJson.getIntValue("multi_graphic_second_price"));
				} catch (Exception ignore) {
				}
				try {// 多图文第二条软广报价
					map.put("second_price_soft", grossJson.getIntValue("soft_multi_graphic_second_price"));
				} catch (Exception ignore) {
				}
				try { // 多图文第3-n条硬广报价
					map.put("third_price_hard", grossJson.getIntValue("multi_graphic_other_price"));
				} catch (Exception ignore) {
				}
				try { // 多图文第3-n条软广报价
					map.put("third_price_soft", grossJson.getIntValue("soft_multi_graphic_other_price"));
				} catch (Exception ignore) {
				}
			}

			try {// 单图文阅读量
				map.put("single_read_num", json.getIntValue("single_graphic_read_num"));
			} catch (Exception ignore) {
			}
			try {// 多图文第一条阅读量
				map.put("first_read_num", json.getIntValue("multi_graphic_top_read_num"));
			} catch (Exception ignore) {
			}
			try {// 多图文第二条阅读量
				map.put("second_read_num", json.getIntValue("multi_graphic_second_read_num"));
			} catch (Exception ignore) {
			}
			try {// 多图文第3-n条阅读量
				map.put("third_read_num", json.getIntValue("multi_graphic_other_read_num"));
			} catch (Exception ignore) {
			}

			list.add(map);
		}
		page.getResultItems().put("list", list);
	}

	public void start() {
		Spider spider = Spider.create(new WeiBoYiPageProcessor())
				.addPipeline(new MongoPipeline(Consts.mongo_database_name, mongo_collection_name))
				.addUrl(String.format(format, 0)).setScheduler(new QueueScheduler()).thread(1).setUUID("weiBoYiSpider");
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
		spider.start();
	}

	public static void main(String[] args) {
		new WeiBoYiPageProcessor().start();
	}

}
