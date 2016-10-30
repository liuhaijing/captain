package com.jiou.pageprocessor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import com.jiou.cookiemanager.CookieManager;
import com.jiou.cookiemanager.WeiBoYiCookieManager;
import com.jiou.httpclients.UserAgent;
import com.jiou.support.SpringContextUtils;

/**
 * 微播易美拍、秒拍爬虫
 * 
 * @author zhe.li
 */
public class WeiBoYiPaiPageProcessor implements PageProcessor {

	public static final String format1 = // 秒拍
	"http://chuanbo.weiboyi.com/hworder/miaopai/filterlist/source/all?web_csrf_token=570b0bf5cdd60&price_keys%%5Breservation%%5D=1&start=%d&limit=20";
	public static final String format2 = // 美拍
	"http://chuanbo.weiboyi.com/hworder/meipai/filterlist/source/all?web_csrf_token=570b374bc3f20&price_keys%%5Breservation%%5D=1&start=%d&limit=20";

	private static final String index_key = "{index}";
	private static final String page_key = "start=0";
	private static final String miaopai_key = "miaopai";
	private static final String meipai_key = "meipai";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final int source_miaopai = 1;// 秒拍
	private static final int source_meipai = 2;// 美拍

	private static final Map<String, String> parseMap = new LinkedHashMap<String, String>();
	static {
		parseMap.put("face_url", "$.data.rows[{index}].cells.face_url");// 头像
		parseMap.put("name", "$.data.rows[{index}].cells.weibo_name");// 名称
		parseMap.put("home_url", "$.data.rows[{index}].cells.url");// 主页外链
		parseMap.put("profession", "$.data.rows[{index}].cells.professions");// 职业
		parseMap.put("domain", "$.data.rows[{index}].cells.domains");// 领域分类
		parseMap.put("area", "$.data.rows[{index}].cells.area_name");// 区域
		parseMap.put("gender", "$.data.rows[{index}].cells.gender");// 性别,1-男,2-女,3-其他
		parseMap.put("desc", "$.data.rows[{index}].cells.pack_info.brief_introduction");// 账号描述
		parseMap.put("coop_notice", "$.data.rows[{index}].cells.pack_info.reservation_notice");// 合作须知
		parseMap.put("can_ori", "$.data.rows[{index}].cells.can_origin");// 是否可原创
		parseMap.put("funsno", "$.data.rows[{index}].cells.followers_count");// 粉丝数
		parseMap.put("external_refer_price_min",
				"$.data.rows[{index}].cells.external_reference_price.reference_price_range_default.min");// 外部参考报价MIN
		parseMap.put("external_refer_price_max",
				"$.data.rows[{index}].cells.external_reference_price.reference_price_range_default.max");// 外部参考报价MAX
		parseMap.put("gross_deal_price", "$.data.rows[{index}].cells.gross_deal_price.reference_price_default");// 总成交价
		// parseMap.put("net_deal_price",
		// "$.data.rows[{index}].cells.net_deal_price.tweet_price");// 净交易价格
		parseMap.put("views", "$.data.rows[{index}].cells.average_play_num");// 平均播放数
		parseMap.put("reviews", "$.data.rows[{index}].cells.average_posts_num");// 平均评论数
		parseMap.put("approvals", "$.data.rows[{index}].cells.average_like_num");// 平均点赞数
		parseMap.put("coop_degree", "$.data.rows[{index}].cells.cooperation_index");// 配合度
	}

	private Site site = Site.me().setRetryTimes(1).setTimeOut(30000).setUserAgent(UserAgent.CHROME);

	private CookieManager cookieManager = SpringContextUtils.getBean(WeiBoYiCookieManager.class);

	public void process(Page page) {
		String html = page.getJson().get();
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
			page.addTargetRequest(page.getRequest());
			logger.error("解析json错误", ignore);
			logger.error(html);
			return;
		}

		// 首页,拼页码
		Integer start = (Integer) page.getRequest().getExtra("start");
		if (start == null || start == 0) {
			int total = jsonObject.getJSONObject("data").getIntValue("total");
			logger.info("===>解析出总数为：{}", total);
			start = 20;
			String seedUrl = page.getRequest().getUrl();
			while (start < total) {
				String url = seedUrl.replace(page_key, "start=".concat(start.toString()));
				// String url = String.format(format1, start);
				Request request = new Request(url);
				request.putExtra("start", start);
				request.setCookie(cookieManager.get());
				// request.setCookie(weiBoYiService.getCookieManager().get());
				page.addTargetRequest(request);
				start += 20;
			}
		}

		// 解析数据
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		int source = getSource(page.getRequest().getUrl());
		for (Integer x = 0; x < jsonObject.getJSONObject("data").getJSONArray("rows").size(); x++) {
			try {
				Map<String, Object> map = new LinkedHashMap<String, Object>();
				for (Map.Entry<String, String> entry : parseMap.entrySet()) {
					Object obj = null;
					try {
						obj = JsonPath.read(html, entry.getValue().replace(index_key, x.toString()));
					} catch (Exception ignore) {
					}
					map.put(entry.getKey(), obj);
				}
				// 设置来源是美拍or秒拍
				map.put("source", source);
				list.add(map);
			} catch (Exception ex) {
				logger.error("解析数据错误", ex);
			}
		}
		page.getResultItems().put("list", list);

	}

	private int getSource(String url) {
		if (url.contains(miaopai_key)) {
			return source_miaopai;
		} else if (url.contains(meipai_key)) {
			return source_meipai;
		}
		return 0;
	}

	public Site getSite() {
		return site;
	}

}
