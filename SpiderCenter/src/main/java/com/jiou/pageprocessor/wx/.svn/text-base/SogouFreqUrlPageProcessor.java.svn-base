package com.jiou.pageprocessor.wx;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;
import orestes.bloomfilter.redis.helper.RedisPool;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.SogouPipeline;
import com.jiou.service.SogouFreqUrlService;
import com.jiou.support.Redis;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class SogouFreqUrlPageProcessor implements PageProcessor {

	public static final String filterName = "sogou_freq_url_filter";
	
	protected static final String query_list_id = "list_id";
	
	protected static final String query_user_id = "user_id";
	
	protected static final String query_active_id = "active_id";
	
	protected static final String query_account_id = "account_id";

	protected static final String query_key = "query";
	
	protected static final String query_wxname = "wxname";

	protected static final String query_data_source = "data_source";
	
	public static final String format = //
	"http://weixin.sogou.com/weixin?query=%s&_sug_type_=&_sug_=y&type=2&page=%d&ie=utf8";
	
	protected static final String commentUrl = "http://mp.weixin.qq.com/mp/getcomment";
	
	protected static final String charset = "UTF-8";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected BloomFilter<String> bloomFilter = new FilterBuilder(5000000, 0.0001).name(filterName)//
			.redisBacked(true).redisPool(new RedisPool(Redis.jedisPool)).buildBloomFilter();

	protected Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(30000).setUserAgent(UserAgent.IE);
	
	public void process(Page page) {
		int depth = page.getRequest().getDepth();
		if (depth == 0) {
			page.setSkip(true);
			parseTitle(page);
		} else if (depth == 1) {
			parseList(page);
		} else if (depth == 2) {
			parseContent(page);
		} else if (depth == 3) {
			parseNum(page);
		}
	}

	private void parseTitle(Page page) {
		String  list_id = (String) page.getRequest().getExtra(query_list_id);
		String  user_id = (String) page.getRequest().getExtra(query_user_id);
		String  active_id = (String) page.getRequest().getExtra(query_active_id);
		String  account_id = (String) page.getRequest().getExtra(query_account_id);
		String wx_name = (String) page.getRequest().getExtra(query_wxname);
		String data_source = (String) page.getRequest().getExtra(query_data_source);
		String utfquery = null;
		
		Document doc = Jsoup.parse(page.getRawText());
		String title = "";
		try {
			title = doc.select("#activity-name").get(0).text().trim().replaceAll(" ", " ");
		} catch (Exception e) {
			logger.error("url参数错误","e");
			return;
		}
		
		
		if (StringUtils.isBlank(title)) {
			logger.error("title can not be blank.");
		}
		
		try {
			utfquery = URLEncoder.encode((title+" "+wx_name).replaceAll("[\\pP\\p{Punct}]", " "), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		for (int x = 1; x <= 5; x++) {
//			将参数拼接在url上
			String url = String.format(format, utfquery, x)+SogouFreqUrlService.getUid();
			Request req = new Request(url);
			req.putExtra(query_list_id, list_id);
			req.putExtra(query_user_id, user_id);
			req.putExtra(query_active_id, active_id);
			req.putExtra(query_account_id, account_id);
			req.putExtra(query_key, title);
			req.putExtra(query_wxname, wx_name);
			req.putExtra(query_data_source, data_source);
			req.setDepth(1);
			page.addTargetRequest(req);
		}
			
		
	}

	protected void parseNum(Page page) {
		int readnum = -1;
		int likenum = -1;
		try {
			JSONObject json = JSONObject.parseObject(page.getRawText());
			readnum = json.getIntValue(SogouPipeline.read_num);
			likenum = json.getIntValue(SogouPipeline.like_num);
		} catch (Exception ignore) {
		}
		if (readnum != -1) {
			page.putField(SogouPipeline.uid, page.getRequest().getExtra(SogouPipeline.uid));
			page.putField(SogouPipeline.read_num, readnum);
			page.putField(SogouPipeline.like_num, likenum);
		}
	}

	protected void parseContent(Page page) {
		try {
			Request req = page.getRequest();
			Document doc = Jsoup.parse(page.getRawText());
			String content = doc.select("#page-content").get(0).text().trim();
			String wxnum = doc.select("p.profile_meta:contains(微信号) > span.profile_meta_value").get(0).text().trim();
			
			page.putField(SogouPipeline.list_id, req.getExtra(SogouPipeline.list_id));
			page.putField(SogouPipeline.title, req.getExtra(SogouPipeline.title));
			page.putField(SogouPipeline.user_id, req.getExtra(SogouPipeline.user_id));
			page.putField(SogouPipeline.active_id, req.getExtra(SogouPipeline.active_id));
			page.putField(SogouPipeline.account_id, req.getExtra(SogouPipeline.account_id));
			page.putField(SogouPipeline.wxname, req.getExtra(SogouPipeline.wxname));
			page.putField(SogouPipeline.pubtime, req.getExtra(SogouPipeline.pubtime));
			page.putField(SogouPipeline.uid, req.getExtra(SogouPipeline.uid));
			page.putField(SogouPipeline.wxnum, wxnum);
			page.putField(SogouPipeline.content, content);
			page.putField(SogouPipeline.url, req.getUrl());
			page.putField(query_data_source, req.getExtra(query_data_source));
		} catch (Exception ex) {
			logger.error("解析内容错误", ex);
			logger.error(page.getRawText());
		}
	}

	protected void parseList(Page page) {
		try {
			String  list_id = (String) page.getRequest().getExtra(query_list_id);
			String  user_id = (String) page.getRequest().getExtra(query_user_id);
			String  act_id = (String) page.getRequest().getExtra(query_active_id);
			String  account_id = (String) page.getRequest().getExtra(query_account_id);
			String query = (String) page.getRequest().getExtra(query_key);
			String wx_name = (String) page.getRequest().getExtra(query_wxname);
			String data_source = (String) page.getRequest().getExtra(query_data_source);
			if (StringUtils.isBlank(user_id)) {
				logger.error("url  user_id can not be blank.");
			}
			if (StringUtils.isBlank(act_id)) {
				logger.error("url  act_id can not be blank.");
			}
			if (StringUtils.isBlank(account_id)) {
				logger.error("url  account_id can not be blank.");
			}
			if (StringUtils.isBlank(query)) {
				logger.error("url  keywords can not be blank.");
			}
			if (StringUtils.isBlank(wx_name)) {
				logger.error("url  wx_name can not be blank.");
			}
			Document doc = Jsoup.parse(page.getRawText());
			Elements items = doc.select("div.results > div.wx-rb.wx-rb3[id]");
			for (Element e : items) {
				String title = e.select("div.txt-box > h4 > a[id]").text().trim();
				if (!(query.replaceAll("[\\pP\\p{Punct}]", "")).equals(title.replaceAll("[\\pP\\p{Punct}]", ""))) {
					continue;
				}
				String wxname = e.select("a.wx-name").get(0).attr("title");
				if(!(wx_name.replaceAll("[\\pP\\p{Punct}]", "")).equals(wxname.replaceAll("[\\pP\\p{Punct}]", ""))){
					continue;
				}
				
				long pubtime = Long.parseLong((e.select("div.s-p[t]").get(0).attr("t"))) * 1000;
				String uid = genUid(list_id, data_source);
				String url = e.select("div.txt-box > h4 > a[id]").get(0).attr("href").trim()+SogouFreqUrlService.getUid();
				if (!this.bloomFilter.contains(uid)) {// 未抓过内容
					Request request = new Request(url);
					request.putExtra(SogouPipeline.list_id, list_id);
					request.putExtra(SogouPipeline.user_id, user_id);
					request.putExtra(SogouPipeline.active_id, act_id);
					request.putExtra(SogouPipeline.account_id, account_id);
					request.putExtra(SogouPipeline.title, title);
					request.putExtra(SogouPipeline.wxname, wxname);
					request.putExtra(SogouPipeline.pubtime, pubtime);
					request.putExtra(SogouPipeline.uid, uid);
					request.putExtra(query_data_source, data_source);
					request.setDepth(2);
					page.addTargetRequest(request);
				}
				// 抓取阅读数、点赞数
				String numurl = commentUrl + url.substring(url.indexOf("?"))+SogouFreqUrlService.getUid();
				Request request = new Request(numurl);
				request.setDepth(3);
				request.setLast(true);
				request.putExtra(SogouPipeline.uid, uid);
				page.addTargetRequest(request);
			}
		} catch (Exception ex) {
			logger.error("解析列表错误", ex);
			logger.error(page.getRawText());
		}
	}

	protected String genUid(String list_id,String data_source) {
		return DigestUtils.md5Hex(list_id +data_source);
	}

	public Site getSite() {
		return site;
	}


}
