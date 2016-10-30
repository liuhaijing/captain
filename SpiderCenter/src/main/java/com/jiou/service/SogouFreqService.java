package com.jiou.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import com.jiou.downloader.SogouDownloader;
import com.jiou.pageprocessor.wx.SogouFreqPageProcessor;
import com.jiou.pipeline.SogouFreqPipeline;
import com.jiou.support.MySqlUtils;
import com.jiou.support.Redis;

@Component("sogouFreqService")
@Scope("singleton")
public class SogouFreqService {

	public static final String format = //
	"http://weixin.sogou.com/weixin?query=%s&_sug_type_=&_sug_=y&type=2&page=%d&ie=utf8";
	
	protected static final String query_list_id = "list_id";
	protected static final String query_user_id = "user_id";
	protected static final String query_active_id = "active_id";
	protected static final String query_account_id = "account_id";
	protected static final String query_key = "query";
	protected static final String query_wxname = "wxname";
	protected static final String charset = "UTF-8";
	protected static final String query_data_source = "data_source";
	

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Spider spider;

	public SogouFreqService() {
		initSpider();
	}

	public void start() {
		List<Map> list = initTask();
		logger.info("加载任务条数:{}", list.size());
		for (Map map : list) {
			String list_id = (String) map.get("list_id");
			String user_id = (String) map.get("user_id");
			String active_id = (String) map.get("active_id");
			String account_id = (String) map.get("account_id");
			String query = (String) map.get("query");
			String wxname = (String) map.get("wxname");
			String data_source = (String) map.get(query_data_source);
			String utfquery = null;
			try {
				utfquery = URLEncoder.encode((query+" "+wxname).replaceAll("[\\pP\\p{Punct}]", " "), charset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				continue;
			}
			for (int x = 1; x <= 5; x++) {
//				将参数拼接在url上
				String url = String.format(format, utfquery, x)+getUid();
				Request req = new Request(url);
				req.putExtra(query_list_id, list_id);
				req.putExtra(query_user_id, user_id);
				req.putExtra(query_active_id, active_id);
				req.putExtra(query_account_id, account_id);
				req.putExtra(query_key, query);
				req.putExtra(query_wxname, wxname);
				req.putExtra(query_data_source, data_source);
				this.spider.addRequest(req);
			}
		}
		initSpider();
	}
	
		

	// 每隔20分钟执行一次
	protected List<Map> initTask() {
		List<Map> list = new ArrayList<Map>();
		PreparedStatement ps = null;
		try {
			ps = MySqlUtils.getConn(".online").prepareStatement(""
					+ "SELECT j1.i_id , j1.uid ,j1.act_id ,j1.aid , j1.copy_title ,j1.acc_name  FROM jm_user_intention_list j1 ,jm_activity j2   "
					+ "WHERE j1.uid = j2.uid  and j1.act_id=j2.act_id  AND j2.monitor_type=2  "
					+ "AND  NOW()  BETWEEN j1.start_time AND  j1.end_time  "
					+ "AND j1.acc_num is not null  and j1.status = 1 ");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String,String>  map = new HashMap<String, String>();
				String list_id = rs.getString("i_id");
				String user_id = rs.getString("uid");
				String active_id =rs.getString("act_id");
				String account_id =rs.getString("aid");
				String query = rs.getString("copy_title");
				String wxname =rs.getString("acc_name");
				map.put(query_list_id, list_id);
				map.put(query_user_id, user_id);
				map.put(query_active_id, active_id);
				map.put(query_account_id, account_id);
				map.put(query_key, query);
				map.put(query_wxname, wxname);
				map.put(query_data_source, "0");
				if(query!=null&&wxname!=null){
					list.add(map);
				}
			}
			

			ps = MySqlUtils.getConn(".test").prepareStatement(""
					+ "SELECT j1.i_id , j1.uid ,j1.act_id ,j1.aid , j1.copy_title ,j1.acc_name  FROM jm_user_intention_list j1 ,jm_activity j2   "
					+ "WHERE j1.uid = j2.uid  and j1.act_id=j2.act_id  AND j2.monitor_type=2  "
					+ "AND  NOW()  BETWEEN j1.start_time AND  j1.end_time  "
					+ "AND j1.acc_num is not null  and j1.status = 1 ");
			ResultSet rstest = ps.executeQuery();
			while (rstest.next()) {
				Map<String,String>  map = new HashMap<String, String>();
				String list_id = rstest.getString("i_id");
				String user_id = rstest.getString("uid");
				String active_id =rstest.getString("act_id");
				String account_id =rstest.getString("aid");
				String query = rstest.getString("copy_title");
				String wxname =rstest.getString("acc_name");
				map.put(query_list_id, list_id);
				map.put(query_user_id, user_id);
				map.put(query_active_id, active_id);
				map.put(query_account_id, account_id);
				map.put(query_key, query);
				map.put(query_wxname, wxname);
				map.put(query_data_source, "1");
				if(query!=null&&wxname!=null){
					list.add(map);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return list;
	}

	protected void initSpider() {
 
					this.spider = Spider.create(new SogouFreqPageProcessor())//
							.addPipeline(new SogouFreqPipeline())//
							.setScheduler(new RedisScheduler(Redis.jedisPool))//
							.setDownloader(new SogouDownloader())//
							.setUUID("sogouFreqSpider")//
							.thread(10)//
							.setExitWhenComplete(true);
					try {
						SpiderMonitor.instance().register(spider);
					} catch (JMException e) {
						e.printStackTrace();
					}
					this.spider.start();
	}

	public static void main(String[] args) throws Exception {
		SogouFreqService service = new SogouFreqService();
		while (true) {
			service.start();
			Thread.sleep(600000);
		}
	}

	public static String getUid() {
		  int num = (int)(Math.random()*100);
		  StringBuffer uuid = new StringBuffer();
		  for (int i = 0; i < num; i++) {
			  uuid.append("&");
		}
		  return uuid.toString();
	}
}
