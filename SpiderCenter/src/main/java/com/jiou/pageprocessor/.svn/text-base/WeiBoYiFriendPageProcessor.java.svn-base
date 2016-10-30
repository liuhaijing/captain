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

public class WeiBoYiFriendPageProcessor implements PageProcessor {

	private static final String index_key = "{index}";

	public static final String format = //
	"http://chuanbo.weiboyi.com/hworder/moments/filterlist/source/all?web_csrf_token=570749091894e&price_keys%%5Btweet%%5D=1&start=%d&limit=20";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private static final Map<String, String> fmap = new LinkedHashMap<String, String>();
	static {
		fmap.put("face_url", "$.data.rows[{index}].cells.face_url");// 头像
		fmap.put("wxname", "$.data.rows[{index}].cells.weibo_name");// 微信名称
		fmap.put("wxno", "$.data.rows[{index}].cells.weibo_id");// 微信号
		fmap.put("home_url", "$.data.rows[{index}].cells.url");// URL
		fmap.put("domain", "$.data.rows[{index}].cells.industry_name");// 行业分类
		fmap.put("area", "$.data.rows[{index}].cells.area_name");// 区域
		fmap.put("gender", "$.data.rows[{index}].cells.gender");// 性别,1-男,2-女,3-其他
		fmap.put("age", "$.data.rows[{index}].cells.age");// 年龄
		fmap.put("impression", "$.data.rows[{index}].cells.friend_desc");// 好友描述
		fmap.put("friendsno", "$.data.rows[{index}].cells.followers_count");// 好友数
		fmap.put("tfcase", "$.data.rows[{index}].cells.release_history");// 投放案例
		fmap.put("mon_orders", "$.data.rows[{index}].cells.orders_monthly");// 月订单数
		fmap.put("week_orders", "$.data.rows[{index}].cells.orders_weekly");// 周订单数
		// map.put("total_orders", "$.data.rows[{index}].cells.weibo_name");//
		// TODO 被约次数（订单数累计）
		fmap.put("external_refer_price_min",
				"$.data.rows[{index}].cells.external_reference_price.reference_price_range_default.min");// 外部参考报价MIN
		fmap.put("external_refer_price_max",
				"$.data.rows[{index}].cells.external_reference_price.reference_price_range_default.max");// 外部参考报价MAX
		fmap.put("gross_deal_price", "$.data.rows[{index}].cells.gross_deal_price.tweet_price");// 总成交价
		fmap.put("net_deal_price", "$.data.rows[{index}].cells.net_deal_price.tweet_price");// 净交易价格
		// map.put("is_veri", "$.data.rows[{index}].cells.weibo_name");//
		// 身份信息是否核实,1-是,2-否
		fmap.put("can_ori", "$.data.rows[{index}].cells.can_origin");// 是否可原创,1-是,2-否,3-未知
	}

	private static final String weibo_type_path = "$.data.rows[{index}].cells.weibo_type";
	private static final String weibo_nature_path = "$.data.rows[{index}].cells.work_nature_be_identified";
	private static final String weibo_area_path = "$.data.rows[{index}].cells.area_be_identified";
	private static final String weibo_follower_path = "$.data.rows[{index}].cells.follower_be_identified";

	private Site site = Site.me().setRetryTimes(1).setTimeOut(30000).setUserAgent(UserAgent.CHROME).setSleepTime(4000);

	// private WeiBoYiService weiBoYiService =
	// SpringContextUtils.getBean(WeiBoYiService.class);

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
				for (Map.Entry<String, String> entry : fmap.entrySet()) {
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
				try {// 身份信息是否核实:1-是,2-否
					map.put("is_veri", 2);
					int weibo_type = JsonPath.read(html, weibo_type_path.replace(index_key, x.toString()));
					int nature = JsonPath.read(html, weibo_nature_path.replace(index_key, x.toString()));
					int area = JsonPath.read(html, weibo_area_path.replace(index_key, x.toString()));
					int follower = JsonPath.read(html, weibo_follower_path.replace(index_key, x.toString()));
					if (weibo_type == 23 && nature == 1 && area == 1 && follower == 1) {
						map.put("is_veri", 1);
					}
				} catch (Exception ignore) {
				}
				list.add(map);
			} catch (Exception ex) {
				logger.error("解析数据错误", ex);
			}
		}
		page.getResultItems().put("list", list);
	}

	public Site getSite() {
		return site;
	}

}
