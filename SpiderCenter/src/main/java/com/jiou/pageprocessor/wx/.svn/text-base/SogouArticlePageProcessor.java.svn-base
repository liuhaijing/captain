package com.jiou.pageprocessor.wx;

import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import com.alibaba.fastjson.JSONObject;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.SogouPipeline;

public class SogouArticlePageProcessor implements PageProcessor {

	public static final String coll_name = "sogou_articles";
	private static final String commentUrl = "http://mp.weixin.qq.com/mp/getcomment";
	public static final String uuid = "sogouArticleSpider";

	private Site site = Site.me().setRetryTimes(1).setSleepTime(100).setUserAgent(UserAgent.IE);;

	public void process(Page page) {
		int depth = page.getRequest().getDepth();
		Request req = page.getRequest();
		if (depth == 0) {// 解析内容
			page.setSkip(true);
			Document doc = Jsoup.parse(page.getRawText());
			String wxname = doc.select("#post-user").get(0).text().trim();
			String wxnum = doc.select("p.profile_meta:contains(微信号) > span.profile_meta_value").get(0).text().trim();
			String title = doc.select("#activity-name").get(0).text().trim();
			String content = doc.select("#js_content").get(0).text().trim();
			String commUrl = commentUrl + req.getUrl().substring(req.getUrl().indexOf("?"));
			Request request = new Request(commUrl);
			request.setDepth(1);
			request.setLast(true);
			request.putExtra("query", req.getExtra("query"));
			request.putExtra("pubtime", req.getExtra("pubtime"));
			request.putExtra("url", req.getUrl());
			request.putExtra("wxname", wxname);
			request.putExtra("wxnum", wxnum);
			request.putExtra("title", title);
			request.putExtra("content", content);
			page.addTargetRequest(request);
		} else {// 解析阅读数、点赞数
			int readnum = -1;
			int likenum = -1;
			try {
				JSONObject json = JSONObject.parseObject(page.getRawText());
				readnum = json.getIntValue(SogouPipeline.read_num);
				likenum = json.getIntValue(SogouPipeline.like_num);
			} catch (Exception ignore) {
				// logger.error("解析阅读点赞数错误:{}", commenthtml);
			}
			if (readnum != -1) {
				Object pub = req.getExtra("pubtime");
				if (pub != null && pub instanceof Long) {
					page.putField("pubtime", new Date((Long) pub));
				}
				page.putField("query", req.getExtra("query"));
				page.putField("url", req.getExtra("url"));
				page.putField("wxname", req.getExtra("wxname"));
				page.putField("wxnum", req.getExtra("wxnum"));
				page.putField("title", req.getExtra("title"));
				page.putField("content", req.getExtra("content"));
				page.putField(SogouPipeline.read_num, readnum);
				page.putField(SogouPipeline.like_num, likenum);
			} else {
				page.addTargetRequest(req);
			}
		}
	}

	public Site getSite() {
		return this.site;
	}

}
