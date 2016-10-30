package com.jiou.pageprocessor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import com.google.common.net.HttpHeaders;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;

/**
 * 清博指数http://www.gsdata.cn/Rank/detail?gid=0
 * 
 * @author zhe.li
 */
@Component("gsDataPageProcessor")
public class GSDataPageProcessor implements PageProcessor {

	private static final String seed = "http://www.gsdata.cn/Rank/detail";
	private static final String urlFormat = //
	"http://www.gsdata.cn/newRank/getwxranks?gid=%s&date=%s&page=%d&type=day&cp=all&t=%.16f&action=";

	private static final String mongo_coll_name = "gsdata";
	// private static final String charset = "UTF-8";

	private static final String group = "group";
	private static final String cat = "cat";
	private static final String rankdate = "rankdate";
	private static final String ranktype = "ranktype";

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected Site site = Site.me().addHeader(HttpHeaders.USER_AGENT, UserAgent.CHROME).setSleepTime(2000)
			.setTimeOut(60000).addHeader(HttpHeaders.X_REQUESTED_WITH, "XMLHttpRequest");

	public void process(Page page) {
		String html = page.getRawText();
		if (StringUtils.isBlank(html)) {
			logger.error("抓取页面为空,url:{}", page.getRequest().getUrl());
			return;
		}
		String groupstr = (String) page.getRequest().getExtra(group);
		String catstr = (String) page.getRequest().getExtra(cat);
		String date = (String) page.getRequest().getExtra(rankdate);
		String rtype = (String) page.getRequest().getExtra(ranktype);
		if (StringUtils.isBlank(groupstr) || StringUtils.isBlank(catstr) || StringUtils.isBlank(date)
				|| StringUtils.isBlank(rtype)) {
			logger.error("分类、排名日期、排名类型必须设置");
			return;
		}
		try {
			JSONObject json = JSONObject.parseObject(html);
			JSONArray items = json.getJSONObject("data").getJSONArray("rows");
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (int x = 0; x < items.size(); x++) {
				try {
					Map<String, Object> map = new LinkedHashMap<String, Object>();
					map.putAll(items.getJSONObject(x));
					map.put(group, groupstr);
					map.put(cat, catstr);
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

	public void start() {
		String type = "day";
		String date = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -2));
		Spider spider = Spider.create(new GSDataPageProcessor())
				.addPipeline(new MongoPipeline(Consts.mongo_database_name, mongo_coll_name)).thread(1);
		try {
			Document doc = Jsoup.connect(seed).timeout(30000).get();
			Elements items = doc.select("#wxGroup > li");
			int count = 0;
			for (Element li : items) {
				try {
					String groupstr = li.select("a").get(0).text().trim();
					for (Element subli : li.select("ul > li[id]")) {
						String catstr = subli.text().trim();
						String catid = subli.attr("data-gid");
						for (int page = 1; page <= 5; page++) {
							String url = String.format(urlFormat, catid, date, page, Math.random());
							Request request = new Request(url);
							request.putExtra(group, groupstr);
							request.putExtra(cat, catstr);
							request.putExtra(rankdate, date);
							request.putExtra(ranktype, type);
							spider.addRequest(request);
							count++;
						}
					}
				} catch (Exception ignore) {
					ignore.printStackTrace();
				}
			}
			logger.info("清博加载URL数目:{}", count);
			SpiderMonitor.instance().register(spider);
		} catch (Exception ex) {
			logger.error("初始化清博指数爬虫失败", ex);
		}
		spider.start();
	}

	public Site getSite() {
		return site;
	}

	// public static void main(String[] args) {
	// new GSDataPageProcessor().start();
	// }

}
