package com.jiou.pageprocessor;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

import com.jiou.cookiemanager.CWQCookieManager;
import com.jiou.cookiemanager.CookieManager;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.CWQPipeline;

/**
 * @author zhe.li
 */
@Component("CWQByIdPageProcessor")
@Scope("singleton")
public class CWQByIdPageProcessor implements PageProcessor {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String mongo_collection_name = "chengwaiquan_add";
	
	private CookieManager cookieManager = new CWQCookieManager(5);
	
	private Site site = Site.me().setRetryTimes(2).setTimeOut(3000)
			.setUserAgent(UserAgent.CHROME).setSleepTime(1000).setCharset("UTF-8");

	private static final String pageFormat = "http://www.cwq.com/weixin/%d.html";
	
	private static final int accountNum = 100000;
//	private static final int accountNum = 3;
	
	private Spider spider;
	
	public void init() {
		spider = Spider.create(this)
//				.addPipeline(new MongoPipeline(Consts.mongo_database_name, mongo_collection_name))
				.addPipeline(new CWQPipeline())
				.setScheduler(new QueueScheduler()).thread(3).setUUID("CWQSpiderById");
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		this.init();
		this.addTask();
		spider.start();
	}
	
	public Site getSite() {
		return this.site;
	}
	
	public synchronized void addTask() 
	{
		int count = 0;
		for(int i=1; i<=accountNum; i++)
		{
			Request request = new Request(String.format(pageFormat, i));
			request.setCookie(cookieManager.get());
			spider.addRequest(request);
			count++;
			if (count % 1000 == 0) {
				logger.info("已加载任务条数:{}", count);
			}
		}
		logger.info("共加载任务条数:{}", count);	
	}
	
	public String rmSymbol(String str) {
		if( str==null )
			return null;
		
		str = str.replaceAll("￥", "");
		str = str.replaceAll("标签：", "");
		str = str.replaceAll("简介：", "");
		return str;
	}
	
	
	public void process(Page page) 
	{
		Document doc = Jsoup.parse(page.getRawText());
		String wxnum =null;
		String wxname = null;
		String auth = null;
		String brief =null;
		String classes = null;
		String tag = null;
		String fans_num = null;
		String yg_first_price = null;
		String yg_second_price = null;
		String rg_first_price = null;
		String rg_second_price = null;
		
		// 微信号.
		try {
			wxnum = doc.select("body > div.w1200.detailed > "
					+ "div:nth-child(4) > div.wx_cot > div.wx_cleft > div.mt40 > span.chaoshou").text();
		} catch (Exception ignore) {
		}
		if (StringUtils.isBlank(wxnum)) {
			return;
		}
		// 微信名称.
		try {
			wxname = doc.select("body > div.w1200.detailed > "
					+ "div:nth-child(4) > div.wx_cot > div.wx_cleft > div.mt40 > span.version").get(0).text().trim();
		} catch (Exception ignore) {
		}
		// 认证主体.
		try {
			auth = doc.select("body > div.w1200.detailed > "
					+ "div:nth-child(4) > div.wx_cot > div.wx_cleft > div.foodt").text();
		} catch (Exception ignore) {
		}
		// 简介.
		try {
			brief = doc.select("body > div.w1200.detailed > "
					+ "div:nth-child(4) > div.wx_cot > div.wx_cleft > div.duction").text();
			brief = rmSymbol(brief);
		} catch (Exception ignore) {
		}
		// 分类.
		try {
			classes = doc.select("body > div.w1200.detailed > "
					+ "div:nth-child(10) > div.fans_left > div.num_tex.mt80 > span.original.fans_back").text();
		} catch (Exception ignore) {
		}
		// 标签.
		try {
			tag = doc.select("body > div.w1200.detailed > "
					+ "div:nth-child(4) > div.wx_cot > div.wx_cright > div.label").text();
			tag = rmSymbol(tag);
		} catch (Exception ignore) {
		}
		// 粉丝量.
		try {
			fans_num = doc.select("body > div.w1200.detailed > "
					+ "div:nth-child(6) > div.fans_left > div.num_tex.mt80 > span.number_y").text();
		} catch (Exception ignore) {
		}
		// (硬广)多图-头条报价.
		try {
			yg_first_price = doc.select("#_buy > dl > dt:nth-child(1) > div.head_line > span.head_dq.hccor").text();
			yg_first_price = rmSymbol(yg_first_price);
		} catch (Exception ignore) {
		}
		// (硬广)多图-次条报价.
		try {
			yg_second_price = doc.select("#_buy > dl > dt:nth-child(2) > div.head_line > span.head_dq.hccor1").text();
			yg_second_price = rmSymbol(yg_second_price);
		} catch (Exception ignore) {
		}
		// (软广)多图-头条报价.
		try {
			rg_first_price = doc.select("#_buy > dl > dt:nth-child(3) > div.head_line > span.head_dq.hccor2").text();
			rg_first_price = rmSymbol(rg_first_price);
		} catch (Exception ignore) {
		}
		// (软广)多图-次条报价.
		try {
			rg_second_price = doc.select("#_buy > dl > dt:nth-child(4) > div.head_line > span.head_dq.hccor3").text();
			rg_second_price = rmSymbol(rg_second_price);
		} catch (Exception ignore) {
		}
		
		// 打印.
		System.out.printf("%s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s | %s \n", 
				page.getUrl(), wxnum, wxname, auth, brief, classes, tag, fans_num, 
				yg_first_price, yg_second_price, rg_first_price, rg_second_price);
		
//		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//		{
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("url", page.getUrl().toString());
//			map.put("wxnum", wxnum);
//			map.put("wxname", wxname);
//			map.put("auth", auth);
//			map.put("brief", brief);
//			map.put("classes", classes);
//			map.put("tag", tag);
//			map.put("fans_num", fans_num);
//			map.put("yg_first_price", yg_first_price);
//			map.put("yg_second_price", yg_second_price);
//			map.put("rg_first_price", rg_first_price);
//			map.put("rg_second_price", rg_second_price);
//			
//			list.add(map);
//		}
//		page.getResultItems().put("list", list);
			
		
		try {
			Map<String, Object> cwqMap = new HashMap<String, Object>();
			cwqMap.put(CWQPipeline.url, page.getUrl().toString());
			cwqMap.put(CWQPipeline.wxnum, wxnum);
			cwqMap.put(CWQPipeline.wxname, wxname);
			cwqMap.put(CWQPipeline.auth, auth);
			cwqMap.put(CWQPipeline.brief, brief);
			cwqMap.put(CWQPipeline.classes, classes);
			cwqMap.put(CWQPipeline.tag, tag);
			cwqMap.put(CWQPipeline.fans_num, fans_num);
			cwqMap.put(CWQPipeline.yg_first_price, yg_first_price);
			cwqMap.put(CWQPipeline.yg_second_price, yg_second_price);
			cwqMap.put(CWQPipeline.rg_first_price, rg_first_price);
			cwqMap.put(CWQPipeline.rg_second_price, rg_second_price);
			
			page.putField(CWQPipeline.ACCOUNT_KEY, cwqMap);
		} catch (Exception ignore) {
			logger.warn("解析城外圈页面:{}{}{}错误", page.getUrl(), wxnum, wxname);
		}
	}
	
	
	public static void main(String[] args) {
		CWQByIdPageProcessor proc = new CWQByIdPageProcessor();
		proc.start();
	}
}
