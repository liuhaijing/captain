package com.jiou.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import com.jiou.cookiemanager.CookieManager;
import com.jiou.cookiemanager.SogouCookieManager;
import com.jiou.downloader.SogouDownloader;
import com.jiou.pageprocessor.wx.SogouPageProcessor;
import com.jiou.pipeline.MongoPipeline;
import com.jiou.pipeline.SogouPipeline;
import com.jiou.support.Redis;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

@Component("sogouFetchSerice")
@Scope("singleton")
public class SogouFetchSerice {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String urlformat = //
	"http://weixin.sogou.com/weixin?type=1&query=%s&ie=utf8&_sug_=n&_sug_type_=";

	private SogouPipeline sogouPipeline = new SogouPipeline();
	private CookieManager cookieManager = new SogouCookieManager();
	// private CookieManager cookieManager = new SogouCookieManager(15);
	
	private Spider spider;

	public SogouFetchSerice() {
		SogouPageProcessor sogouPageProcessor = new SogouPageProcessor();
		sogouPageProcessor.setCookieManager(cookieManager);
		SogouDownloader sogouDownloader = new SogouDownloader();
		spider = Spider.create(sogouPageProcessor).addPipeline(sogouPipeline).setUUID("SogouSpider")
				.setExitWhenComplete(false).setDownloader(sogouDownloader)
				.setScheduler(new RedisScheduler(Redis.jedisPool)).thread(10);
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			throw new RuntimeException(e);
		}
//		chijy 909
		spider.start(); 
	}

	public synchronized void addTask() {
		// List<String> list = new ArrayList<String>();
		// list.add("nvwang388");
		// list.add("wxzbtxx");
		List<String> list = initOATask();
		int count = 0;
		for (String wxnum : list) {
			Request request = new Request(String.format(urlformat, wxnum));
			request.putExtra(SogouPipeline.wxnum, wxnum);
			spider.addRequest(request);
			count++;
			if (count % 1000 == 0) {
				logger.info("已加载任务条数:{}", count);
			}
		}
		logger.info("共加载任务条数:{}", count);
	}

	@SuppressWarnings("deprecation")
	protected List<String> initOATask() {
		int no = 0;
		List<String> list = new ArrayList<String>();
		try {
			MongoClient mongoclient = MongoPipeline.getMongoclient();
			DBCollection dBCollection = mongoclient.getDB("spider").getCollection(SogouPipeline.mongo_coll_task);
			DBObject query = new BasicDBObject();
			// query.put("idx", new BasicDBObject("$gte", 0));
			// query.put("uid", new BasicDBObject("$ne", 266));
			Iterator<DBObject> it = dBCollection.find(query).iterator();
			while (it.hasNext()) {
				DBObject doc = it.next();
				String wxnum = (String) doc.get("wxnum");
				if (StringUtils.isBlank(wxnum)) {
					continue;
				}
				list.add(wxnum);
			}
		} catch (Exception e) {
			logger.error("初始化任务失败", e);
		} finally {
		}
		// logger.info("加载微信号数目:{}", list.size());
		return list;
	}

	public static void main(String[] args) {
		SogouFetchSerice service = new SogouFetchSerice();
		service.addTask();
		// service.startOA();
	}
}
