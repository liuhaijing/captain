package com.jiou.service;

import javax.management.JMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.scheduler.QueueScheduler;

import com.jiou.cookiemanager.CookieManager;
import com.jiou.cookiemanager.WeiBoYiCookieManager;
import com.jiou.pageprocessor.WeiBoYi2PageProcessor;
import com.jiou.pageprocessor.WeiBoYiFriendPageProcessor;
import com.jiou.pageprocessor.WeiBoYiPaiPageProcessor;
import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;
import com.jiou.support.SpringContextUtils;

@Component("weiBoYiService")
@Scope("prototype")
public class WeiBoYiService {

	public static final String mongo_collection_name_wx = "weiboyi_wx";// 微信公众号
	public static final String mongo_collection_name_friends = "weiboyi_friends";// 朋友圈
	public static final String mongo_collection_name_pai = "weiboyi_mmpai";// 美拍、秒拍

	private Logger logger = LoggerFactory.getLogger(getClass());

	private CookieManager cookieManager = SpringContextUtils.getBean(WeiBoYiCookieManager.class);

	public void startWX() {
		logger.info("微播易微信公众号爬虫启动...");
		Spider spider = Spider.create(new WeiBoYi2PageProcessor())
				.addPipeline(new MongoPipeline(Consts.mongo_database_name, mongo_collection_name_wx))
				.setScheduler(new QueueScheduler())
				.thread(1)
				.setUUID("WeiBoYiWXSpider");
		Request request = new Request(String.format(WeiBoYi2PageProcessor.format, 0));
		request.setCookie(cookieManager.get());
		spider.addRequest(request);
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
		spider.start();
	}
	
	public void startFriends() {
		logger.info("微播易朋友圈爬虫启动...");
		Spider spider = Spider.create(new WeiBoYiFriendPageProcessor())
				.addPipeline(new MongoPipeline(Consts.mongo_database_name, mongo_collection_name_friends))
				.setScheduler(new QueueScheduler()).thread(1).setUUID("WeiBoYiFriendsSpider");
		Request request = new Request(String.format(WeiBoYiFriendPageProcessor.format, 0));
		request.setCookie(cookieManager.get());
		spider.addRequest(request);
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
		spider.start();
	}

	public void startPai() {
		logger.info("微播易美妙拍爬虫启动...");
		Spider spider = Spider.create(new WeiBoYiPaiPageProcessor())
				.addPipeline(new MongoPipeline(Consts.mongo_database_name, mongo_collection_name_pai))
				.setScheduler(new QueueScheduler()).thread(1).setUUID("WeiBoYiMPaiSpider");
		Request request1 = new Request(String.format(WeiBoYiPaiPageProcessor.format1, 0));
		request1.setCookie(cookieManager.get());
		spider.addRequest(request1);
		Request request2 = new Request(String.format(WeiBoYiPaiPageProcessor.format2, 0));
		request2.setCookie(cookieManager.get());
		spider.addRequest(request2);
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
		spider.start();
	}

	// public CookieManager getCookieManager() {
	// return cookieManager;
	// }

	public static void main(String[] args) {
		SpringContextUtils.load("classpath*:spring/context.xml");
		WeiBoYiService weiBoYiService = SpringContextUtils.getBean(WeiBoYiService.class);
//		weiBoYiService.startPai();
//		weiBoYiService.startFriends();
		weiBoYiService.startWX();
	}

}
