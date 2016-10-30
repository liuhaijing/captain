package com.jiou.pageprocessor;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;

/**
 * 新榜http://newrank.cn/public/info/list.html?period=day&type=data
 * 
 * @author zhe.li
 */
@Component("newRankPageProcessor")
public class NewRankPageProcessor implements PageProcessor {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private Site site = Site.me().setRetryTimes(1).setTimeOut(60000).setSleepTime(3000).setUserAgent(UserAgent.CHROME)
			.setCharset(charset);

	private static final String charset = "UTF-8";
	private static final Map<String, String> rankCat = new LinkedHashMap<String, String>();
	private static final String mongo_coll_name = "newrank";
	private static final String group = "group";
	private static final String cat = "cat";
	private static final String rankdate = "rankdate";
	private static final String ranktype = "ranktype";

	static {
		rankCat.put("时事", "资讯");
		rankCat.put("民生", "资讯");
		rankCat.put("财富", "资讯");
		rankCat.put("科技", "资讯");
		rankCat.put("创业", "资讯");
		rankCat.put("汽车", "资讯");
		rankCat.put("楼市", "资讯");
		rankCat.put("职场", "资讯");
		rankCat.put("教育", "资讯");
		rankCat.put("学术", "资讯");
		rankCat.put("政务", "资讯");
		rankCat.put("企业", "资讯");
		rankCat.put("文化", "生活");
		rankCat.put("百科", "生活");
		rankCat.put("健康", "生活");
		rankCat.put("时尚", "生活");
		rankCat.put("美食", "生活");
		rankCat.put("乐活", "生活");
		rankCat.put("旅行", "生活");
		rankCat.put("幽默", "生活");
		rankCat.put("情感", "生活");
		rankCat.put("体娱", "生活");
		rankCat.put("美体", "生活");
		rankCat.put("文摘", "生活");
	}

	public void process(Page page) {
		String html = page.getRawText();
		if (StringUtils.isBlank(html)) {
			logger.info("json为空,url:{}", page.getRequest().getUrl());
			return;
		}
		String firstcat = (String) page.getRequest().getExtra(group);
		String secondcat = (String) page.getRequest().getExtra(cat);
		String date = (String) page.getRequest().getExtra(rankdate);
		String rtype = (String) page.getRequest().getExtra(ranktype);
		if (StringUtils.isBlank(firstcat) || StringUtils.isBlank(secondcat) || StringUtils.isBlank(date)
				|| StringUtils.isBlank(rtype)) {
			logger.error("分类、排名日期、排名类型必须设置");
			return;
		}
		try {
			JSONObject json = JSONObject.parseObject(html);
			JSONArray items = json.getJSONArray("value");
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (int x = 0; x < items.size(); x++) {
				try {
					Map<String, Object> map = new LinkedHashMap<String, Object>();
					map.putAll(items.getJSONObject(x));
					map.put(group, firstcat);
					map.put(cat, secondcat);
					map.put(rankdate, date);
					map.put(ranktype, rtype);
					list.add(map);
				} catch (Exception ignore) {
				}
			}
			page.putField(MongoPipeline.key, list);
		} catch (Exception e) {
			logger.error("解析错误", page.getRequest().getUrl());
			logger.error("解析错误", e);
			logger.error("解析错误", html);
		}
	}

	public Site getSite() {
		return site;
	}

	private static final String urlFormat = //
	"http://newrank.cn/xdnphb/list/day/rank?end=%s&rank_name=%s&rank_name_group=%s&start=%s&nonce=%s&xyz=%s";
	private static final String paramFormat = //
	"/xdnphb/list/day/rank?AppKey=joker&end=%s&rank_name=%s&rank_name_group=%s&start=%s&nonce=%s";

	public void start() {
		Spider spider = Spider.create(new NewRankPageProcessor())
				.addPipeline(new MongoPipeline(Consts.mongo_database_name, mongo_coll_name)).thread(1);
		try {
			String date = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -2));
			for (Map.Entry<String, String> entry : rankCat.entrySet()) {
				String rank_name = entry.getKey();
				String rank_name_group = entry.getValue();
				String nonce = getNonce();
				String a = String.format(paramFormat, date, rank_name, rank_name_group, date, nonce);
				String xyz = DigestUtils.md5Hex(a);
				String url = String.format(urlFormat, date, URLEncoder.encode(rank_name, charset),
						URLEncoder.encode(rank_name_group, charset), date, nonce, xyz);
				Request request = new Request(url);
				request.putExtra(group, rank_name_group);
				request.putExtra(cat, rank_name);
				request.putExtra(rankdate, date);
				request.putExtra(ranktype, "day");
				spider.addRequest(request);
			}
			SpiderMonitor.instance().register(spider);
		} catch (Exception e) {
			e.printStackTrace();
		}
		spider.start();
	}

	public static String getNonce() {
		String[] a = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
		StringBuilder c = new StringBuilder();
		for (int d = 0; 9 > d; d++) {
			int e = new Double(Math.floor(16 * Math.random())).intValue();
			c.append(a[e]);
		}
		return c.toString();
	}

	public static void main(String[] args) {
		new NewRankPageProcessor().start();
	}
}
