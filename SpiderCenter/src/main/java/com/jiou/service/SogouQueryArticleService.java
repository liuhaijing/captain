package com.jiou.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import com.jiou.downloader.SogouQueryArticleDownloader;
import com.jiou.pageprocessor.wx.SogouQueryArticlePageProcessor;
import com.jiou.pipeline.MongoPipeline;
import com.jiou.pipeline.SogouPipeline;
import com.jiou.pipeline.SogouQueryArticlePipeline;
import com.jiou.support.Consts;
import com.jiou.support.Redis;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@Component("sogouQueryArticleService")
@Scope("singleton")
public class SogouQueryArticleService {



	public static final String task_list = "wx_list";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public static final String format = //
	"http://weixin.sogou.com/weixin?query=%s&_sug_type_=&_sug_=y&type=1&page=%d&ie=utf8";

	protected static final String charset = "UTF-8";
	
	protected Spider spider;

	public SogouQueryArticleService() {
		initSpider();
	}

	public void start() {
		List<Map<String, String>> list = initTask();
		logger.info("加载article任务条数:{}", list.size());
		String utfquery = null;
		for (Map<String, String> map : list) {
			
			try {
				String wx_num = map.get(SogouPipeline.wxnum);
				String wx_name = map.get(SogouPipeline.wxname);
				utfquery = URLEncoder.encode((wx_num), charset);
				String url = String.format(format, utfquery, 1);
				
				Request req = new Request(url);
				req.putExtra(SogouPipeline.wxnum, wx_num);
				req.putExtra(SogouPipeline.wxname, wx_name);
				this.spider.addRequest(req);
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	// 每隔20分钟执行一次
	protected List<Map<String, String>> initTask() {
		
		 List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			DBCollection coll = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
					.getCollection(task_list);
			Iterator<DBObject> it = coll.find().iterator();
			while (it.hasNext()) {
				 Map<String, String>  map = new HashMap<String, String>();
		        DBObject doc = it.next();
		         String  wx_num = (String) doc.get(SogouPipeline.wxnum );
				String wx_name = (String) doc.get(SogouPipeline.wxname);
				try {
					if (StringUtils.isBlank(wx_num)) {
						continue;
					}
					map.put(SogouPipeline.wxnum , wx_num);
					map.put(SogouPipeline.wxname, wx_name);
					list.add(map);
				} catch (Exception ex) {
					logger.error("initTask error", ex);
				}
			}
		} catch (Exception e) {
			logger.error("fetch sogou article list error", e);
		}
		
		return list;
		
	}

	protected void initSpider() {
		if (this.spider == null) {
			synchronized (this) {
				if (this.spider == null) {
					this.spider = Spider.create(new SogouQueryArticlePageProcessor())//
							.addPipeline(new SogouQueryArticlePipeline())//
							.setScheduler(new RedisScheduler(Redis.jedisPool))//
							.setDownloader(new SogouQueryArticleDownloader())//
							.setUUID("sogouQueryArticleSpider")//
							.thread(40)
							.setExitWhenComplete(false);
						
					try {
						SpiderMonitor.instance().register(spider);
					} catch (JMException e) {
						e.printStackTrace();
					}
				}
			}
		}
		this.spider.start();
	}
	
	
	public static void main(String[] args) throws Exception {
		SogouQueryArticleService service = new SogouQueryArticleService();
			service.start();
	}

}
