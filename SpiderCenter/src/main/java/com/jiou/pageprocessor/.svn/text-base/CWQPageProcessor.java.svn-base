package com.jiou.pageprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
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
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.utils.HttpConstant;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jiou.cookiemanager.CWQCookieManager;
import com.jiou.cookiemanager.CookieManager;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;

/**
 * @author zhe.li
 */
@Component("CWQPageProcessor")
@Scope("singleton")
public class CWQPageProcessor implements PageProcessor {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public static final String mongo_collection_name = "chengwaiquan";

	private static final String key_depth = "depth";
	private static final String key_page = "p";
	private static final String key_region_id = "regid";
	private static final String key_type = "type";// 1-硬广,2-软广

	private static final int media_weichat = 1;// 微信公众号
	// private static final int media_friends = 2;
	// private static final int media_weibo_sina = 3;
	// private static final int media_meipai = 4;
	// private static final int media_miaopai = 5;
	// private static final int media_qq = 5;
	// private static final int media_weishi = 6;
	// private static final int media_weibo_qq = 7;

	private static final String postDataFormat = //
	"http://www.cwq.com/Owner/Weixin/get_weixin_list?is_celebrity=0&ids=&order_by=&all=&flex=%d&zfjg_type=2&dfmr_mt=%s&p=%d";
	private static final String postDataUrl = "http://www.cwq.com/Owner/Weixin/get_weixin_list/";

	private static final String postRegionUrl = "http://www.cwq.com/Owner/Tool/get_Region_Data/";
	private static final String postRegionUrlFormat = "http://www.cwq.com/Owner/Tool/get_Region_Data?parent_id=%s";

	private Site site = Site.me()
			.setRetryTimes(1)
			.setTimeOut(30000)
			.setUserAgent(UserAgent.CHROME)
			.setSleepTime(5000)
			.setCharset("UTF-8");

	private CookieManager cookieManager = new CWQCookieManager(5);

	private AtomicInteger errorCount = new AtomicInteger();

	public void process(Page page) {
		int depth = Integer.parseInt(String.valueOf(page.getRequest().getExtra(key_depth)));
		
		// 第1层,处理种子url,得到各省份下的市的region_id
		if (depth == 1) {
			page.getResultItems().setSkip(true);
			try {
				String html = page.getJson().get();
				JSONArray items = JSONObject.parseObject(html).getJSONArray("data");
				for (int x = 1; x < items.size(); x++) {
					try {
						JSONObject json = items.getJSONObject(x);
						String url = String.format(postRegionUrlFormat, json.getString("region_id"));
						Request request = new Request(postRegionUrl);
						request.setMethod(HttpConstant.Method.POST);
						putNvps(request, url);
						request.putExtra(key_depth, 2);
						page.addTargetRequest(request);
					} catch (Exception ignore) {
					}
				}
			} catch (Exception e) {
				logger.error("处理种子URL错误", e);
			}
		} 
		// 第2层,处理各省份下各市,得到各市的软硬广列表
		else if (depth == 2) {
			page.getResultItems().setSkip(true);
			try {
				String html = page.getJson().get();
				JSONArray items = JSONObject.parseObject(html).getJSONArray("data");
				for (int x = 0; x < items.size(); x++) {
					try {
						JSONObject json = items.getJSONObject(x);
						String region_id = json.getString("region_id");
						// is_celebrity=0&ids=&order_by=&all=&flex=%d&zfjg_type=2&dfmr_mt=%s&p=%d
						String url = String.format(postDataFormat, 1, region_id, 1);// 硬广
						Request request = new Request(postDataUrl);
						request.setMethod(HttpConstant.Method.POST);
						putNvps(request, url);
						request.putExtra(key_depth, 3);
						request.putExtra(key_page, 1);
						request.putExtra(key_region_id, region_id);
						request.putExtra(key_type, 1);
						page.addTargetRequest(request);

						String url2 = String.format(postDataFormat, 2, region_id, 1);// 软广
						request.putExtra(key_type, 2);
						request.setUrl(url2);
						page.addTargetRequest(request);
					} catch (Exception ignore) {
					}
				}
			} catch (Exception e) {
				logger.error("处理省份URL错误", e);
			}
		} 
		// 第3层,处理各市的数据
		else if (depth == 3) {
			int p = Integer.parseInt(String.valueOf(page.getRequest().getExtra(key_page)));
			JSONObject data = null;
			try {
				data = JSONObject.parseObject(page.getJson().get()).getJSONObject("data");
			} catch (Exception ex) {
				page.addTargetRequest(page.getRequest());
				logger.error("解析数据错误", ex);
				if (errorCount.incrementAndGet() == 3) {
					cookieManager.reload();
					errorCount.set(0);
				}
				return;
			}
			// 第1页,解析总页码
			if (p == 1) {
				int count = data.getIntValue("count");
				int totalPage = count % 20 == 0 ? count / 20 : count / 20 + 1;
				String region_id = (String) page.getRequest().getExtra(key_region_id);
				for (int x = 2; x <= totalPage; x++) {
					// String url = String.format(postDataFormat, region_id, x);
					String url = page.getRequest().getUrl().replace("p=1", "p=" + x);
					Request request = new Request(postDataUrl);
					request.setMethod(HttpConstant.Method.POST);
					putNvps(request, url);
					request.putExtra(key_depth, 3);
					request.putExtra(key_page, x);
					request.putExtra(key_region_id, region_id);
					request.putExtra(key_type, page.getRequest().getExtra(key_type));
					page.addTargetRequest(request);
				}
			}

			// 解析数据
			parseData(data, page);
		}
	}

	private void parseData(JSONObject data, Page page) {
		JSONArray items = data.getJSONArray("list");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Object type = page.getRequest().getExtra(key_type);
		for (int x = 0; x < items.size(); x++) {
			JSONObject json = items.getJSONObject(x);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("media", media_weichat);
			map.put(key_type, type);
			
			// 微信号
			try {
				String weichatno = json.getString("bs_weixinhao");
				if (StringUtils.isBlank(weichatno)) {
					continue;
				}
				map.put("wxnum", weichatno);
			} catch (Exception ignore) {
			}
			// 微信名称
			try {
				map.put("wxname", json.getString("bs_account_name"));
			} catch (Exception ignore) {
			}
			// 粉丝数
			try {
//				map.put("funsnum", json.getString("pg_fans_num_explain"));
				map.put("fans_num", json.getString("bs_fans_num"));
			} catch (Exception ignore) {
			}
			// 分类
			try {
				map.put("tag", json.getString("bs_fan_tag"));
			} catch (Exception ignore) {
			}
			// 周更新频率
			try {
				map.put("update_freq", json.getString("bs_update_frequency"));
			} catch (Exception ignore) {
			}
			// 接单类型
			try {
				map.put("order_note", json.getString("bs_order_note"));
			} catch (Exception ignore) {
			}
			// 是否验证
			try {
				map.put("bs_verified", json.getString("bs_verified"));
			} catch (Exception ignore) {
			}
			// 是否授权
			try {
				map.put("bs_authenticate", json.getString("bs_authenticate"));
			} catch (Exception ignore) {
			}			
			// (硬广)多图头条报价	
			try {
				map.put("yg_first_price", json.getIntValue("dtwdyt"));
			} catch (Exception ignore) {
			}
			// (硬广)多图次条报价	
			try {
				map.put("yg_second_price", json.getIntValue("dtwdet"));
			} catch (Exception ignore) {
			}
			// (软广)多图头条报价	
			try {
				map.put("rg_first_price", json.getIntValue("soft_price2"));
			} catch (Exception ignore) {
			}
			// (软广)多图次条报价	
			try {
				map.put("rg_second_price", json.getIntValue("soft_price3"));
			} catch (Exception ignore) {
			}
			
			// 多图3-n条报价
//			try {
//				map.put("third_price", json.getIntValue("dtwqtwz"));
//			} catch (Exception ignore) {
//			}
			// 阅读量
//			try {
//				map.put("read_num", json.getIntValue("sy_read_number"));
//			} catch (Exception ignore) {
//			}
			list.add(map);
		}
		page.getResultItems().put("list", list);
	}

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

	public void start() {
		Spider spider = Spider.create(this)
				.addPipeline(new MongoPipeline(Consts.mongo_database_name, mongo_collection_name))
				.setScheduler(new QueueScheduler())
				.thread(1)
				.setUUID("CWQSpider");
		Request request = new Request(postRegionUrl);
		request.setMethod(HttpConstant.Method.POST);
		putNvps(request, String.format(postRegionUrlFormat, "1"));
		request.putExtra(key_depth, 1);
		spider.addRequest(request);
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
		spider.start();
	}

	private void putNvps(Request request, String url) {
		NameValuePair[] nvps = HttpClientUtils.buildNVPArray(url);
		request.putExtra("nameValuePair", nvps);
	}

	public static void main(String[] args) {
		new CWQPageProcessor().start();
	}

}
