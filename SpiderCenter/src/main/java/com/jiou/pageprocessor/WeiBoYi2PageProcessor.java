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
 * 微播易微信公众号
 * 
 * @author zhe.li
 */
public class WeiBoYi2PageProcessor implements PageProcessor {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	// private WeiBoYiService weiBoYiService =
	// SpringContextUtils.getBean(WeiBoYiService.class);

	private static final String index_key = "{index}";

	// public static final String format = //
	// "http://chuanbo.weiboyi.com/hworder/weixin/filterlist/source/all?web_csrf_token=57061a28ed81a&price_keys%%5Btop%%5D=1&start=%d&limit=20&category_filter=%d";
	public static final String format = //
	"http://chuanbo.weiboyi.com/hworder/weixin/filterlist/source/all?web_csrf_token=57061a28ed81a&price_keys%%5Btop%%5D=1&start=%d&limit=20";

	private CookieManager cookieManager = SpringContextUtils.getBean(WeiBoYiCookieManager.class);

//	private Site site = Site.me().setRetryTimes(1).setTimeOut(30000).setUserAgent(UserAgent.CHROME).setSleepTime(3000);
	private Site site = Site.me().setRetryTimes(1).setTimeOut(30000).setUserAgent(UserAgent.CHROME).setSleepTime(1500);
	
	private static final Map<String, String> wxMap = new LinkedHashMap<String, String>();
	static {
		wxMap.put("wxname", "$.data.rows[{index}].cells.weibo_name");// 微信名称
		wxMap.put("wxno", "$.data.rows[{index}].cells.weibo_id");// 微信号
		wxMap.put("home_url", "$.data.rows[{index}].cells.url");// 主页外链
		wxMap.put("face_url", "$.data.rows[{index}].cells.face_url");// 头像
		wxMap.put("funsno", "$.data.rows[{index}].cells.followers_count");// 粉丝数
		wxMap.put("gender", "$.data.rows[{index}].cells.gender");// 性別
		wxMap.put("is_auth", "$.data.rows[{index}].cells.is_vip");// 是否微信认证-1是,2否
		wxMap.put("can_ori", "$.data.rows[{index}].cells.can_origin");// 是否可原创-1是,2否
		
		wxMap.put("desc", "$.data.rows[{index}].cells.pack_info.brief_introduction");// 账号描述
		wxMap.put("coop_noti", "$.data.rows[{index}].cells.pack_info.reservation_notice");// 合作须知（预约须知）
		wxMap.put("domain", "$.data.rows[{index}].cells.domains");// 领域分类
		wxMap.put("role", "$.data.rows[{index}].cells.pack_type");// 用户角色（名人/媒体）1-名人,2-媒体
		wxMap.put("week_update", "$.data.rows[{index}].cells.graphic_send_num");// 周更新
		wxMap.put("coop_degree", "$.data.rows[{index}].cells.cooperation_index");// 配合度
		wxMap.put("snbt", "$.data.rows[{index}].cells.snbt_exponent");// SNBT指数
		
//		wxMap.put("single_min", "$.data.rows[{index}].cells.external_reference_price.reference_price_range_single.min");// 单图文最低报价
//		wxMap.put("single_max", "$.data.rows[{index}].cells.external_reference_price.reference_price_range_single.max");// 单图文最高报价
//		wxMap.put("fisrt_min", "$.data.rows[{index}].cells.external_reference_price.reference_price_range_multi_top.min");// 多图文第一条最低报价
//		wxMap.put("first_max", "$.data.rows[{index}].cells.external_reference_price.reference_price_range_multi_top.max");// 多图文第一条最高报价
//		wxMap.put("second_min", "$.data.rows[{index}].cells.external_reference_price.reference_price_range_multi_second.min");// 多图文第二条最低报价
//		wxMap.put("second_max", "$.data.rows[{index}].cells.external_reference_price.reference_price_range_multi_second.max");// 多图文第二条最高报价
//		wxMap.put("third_min", "$.data.rows[{index}].cells.external_reference_price.reference_price_range_multi_other.min");// 多图文第3-n条最低报价
//		wxMap.put("third_max", "$.data.rows[{index}].cells.external_reference_price.reference_price_range_multi_other.max");// 多图文第3-n条最高报价

		wxMap.put("single_hard", "$.data.rows[{index}].cells.external_reference_price.single.quote");// 单图文硬广报价
		wxMap.put("first_hard",  "$.data.rows[{index}].cells.external_reference_price.multi_top.quote");// 多图文第一条硬广报价
		wxMap.put("second_hard", "$.data.rows[{index}].cells.external_reference_price.multi_second.quote");// 多图文第二条硬广报价
		wxMap.put("third_hard",  "$.data.rows[{index}].cells.external_reference_price.multi_other.quote");// 多图文第3-n条硬广报价
		
		wxMap.put("single_hard_1", "$.data.rows[{index}].cells.gross_deal_price.single_graphic_price");// 单图文硬广报价
		wxMap.put("first_hard_1",  "$.data.rows[{index}].cells.gross_deal_price.multi_graphic_top_price");// 多图文第一条硬广报价
		wxMap.put("second_hard_1", "$.data.rows[{index}].cells.gross_deal_price.multi_graphic_second_price");// 多图文第二条硬广报价
		wxMap.put("third_hard_1",  "$.data.rows[{index}].cells.gross_deal_price.multi_graphic_other_price");// 多图文第3-n条硬广报价
		
//		wxMap.put("single_soft", "$.data.rows[{index}].cells.external_reference_price.soft_single_graphic_price");// 单图文软广报价
//		wxMap.put("first_soft",  "$.data.rows[{index}].cells.external_reference_price.soft_multi_graphic_top_price");// 多图文第一条软广报价
//		wxMap.put("second_soft", "$.data.rows[{index}].cells.external_reference_price.soft_multi_graphic_second_price");// 多图文第二条软广报价
//		wxMap.put("third_soft",  "$.data.rows[{index}].cells.external_reference_price.soft_multi_graphic_other_price");// 多图文第3-n条软广报价
		
		wxMap.put("sin_read_num", "$.data.rows[{index}].cells.single_graphic_read_num");// 单图文阅读量
		wxMap.put("fst_read_num", "$.data.rows[{index}].cells.multi_graphic_top_read_num");// 多图文第一条阅读量
		wxMap.put("sec_read_num", "$.data.rows[{index}].cells.multi_graphic_second_read_num");// 多图文第二条阅读量
		wxMap.put("thd_read_num", "$.data.rows[{index}].cells.multi_graphic_other_read_num");// 多图文第3-n条阅读量		
	}

	public Site getSite() {
		return site;
	}

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
			while (start < total) {
				String url = String.format(format, start);
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
		for (Integer x = 0; x < jsonObject.getJSONObject("data").getJSONArray("rows").size(); x++) {
			try {
				Map<String, Object> map = new LinkedHashMap<String, Object>();
				for (Map.Entry<String, String> entry : wxMap.entrySet()) {
					Object obj = null;
					try {
						obj = JsonPath.read(html, entry.getValue().replace(index_key, x.toString()));
					} catch (Exception ignore) {
					}
					map.put(entry.getKey(), obj);
				}
				if (map.get("wxno") == null) {
					continue;
				}
				list.add(map);
			} catch (Exception ex) {
				logger.error("解析数据错误", ex);
			}
		}
		page.getResultItems().put("list", list);
	}

}
