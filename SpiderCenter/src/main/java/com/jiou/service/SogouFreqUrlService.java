package com.jiou.service;

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

import com.jiou.downloader.SogouUrlDownloader;
import com.jiou.pageprocessor.wx.SogouFreqUrlPageProcessor;
import com.jiou.pipeline.SogouFreqUrlPipeline;
import com.jiou.support.MySqlUtils;
import com.jiou.support.Redis;

@Component("sogouFreqUrlService")
@Scope("singleton")
public class SogouFreqUrlService {



	protected static final String query_list_id ="list_id";
	protected static final String query_user_id = "user_id";
	protected static final String query_active_id = "active_id";
	protected static final String query_account_id = "account_id";
	protected static final String query_key = "query";
	protected static final String query_wxname = "wxname";
	protected static final String charset = "UTF-8";
	protected static final String query_data_source = "data_source";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Spider spider;

	public SogouFreqUrlService() {
		initSpider();
	}

	public void start() {
		List<Map> list = initTask();
		logger.info("加载url任务条数:{}", list.size());
		for (Map map : list) {
			String list_id = (String) map.get("list_id");
			String user_id = (String) map.get("user_id");
			String active_id = (String) map.get("active_id");
			String account_id = (String) map.get("account_id");
			String query = (String) map.get("query");
			String wxname = (String) map.get("wxname");
			String data_source = (String) map.get(query_data_source);
			
			
			String url = query+getUid();
				Request req = new Request(url);
				req.putExtra(query_list_id, list_id);
				req.putExtra(query_user_id, user_id);
				req.putExtra(query_active_id, active_id);
				req.putExtra(query_account_id, account_id);
				req.putExtra(query_key, query);
				req.putExtra(query_wxname, wxname);
				req.putExtra(query_data_source, data_source);
				this.spider.addRequest(req);
				req.putExtra(query_data_source, data_source);
//			}
		}
		initSpider();
	}
	
		
	

	// 每隔20分钟执行一次
//	protected List<Map> initTask() {
//		List<Map> list = new ArrayList<Map>();
//				Map<String,String>  map = new HashMap<String, String>();
// 
//				map.put(query_user_id, "1");
//				map.put(query_active_id, "822");
//				map.put(query_account_id, "0");
//				map.put(query_key, "http://mp.weixin.qq.com/s?_ &mid=2654739119&idx=1&sn=9a8ecf2cbd7c66e18b382220332c788c&chksm=bd01e1888a76689ecaab469dd0a6e5221b1a6e800f4dc6517897e1c7ecac1f92b208782fcebb&scene=0#wechat_redirect");
//				map.put(query_wxname, "");
//					list.add(map);
//		return list;
//	}
	

	// 每隔20分钟执行一次
	protected List<Map> initTask() {
		List<Map> list = new ArrayList<Map>();
		PreparedStatement ps = null;
		try {
			ps = MySqlUtils.getConn(".online").prepareStatement(""
					+ "SELECT j1.i_id , j1.uid ,j1.act_id ,j1.aid , j1.copy_link ,j1.acc_name  FROM jm_user_intention_list j1 ,jm_activity j2   "
					+ "WHERE j1.uid = j2.uid  and j1.act_id=j2.act_id  AND j2.monitor_type=1  "
					+ "AND  NOW()  BETWEEN j1.start_time AND  j1.end_time  "
					+ "AND j1.acc_num is not null  and j1.status = 1 ");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String,String>  map = new HashMap<String, String>();
				String list_id = rs.getString("i_id");
				String user_id = rs.getString("uid");
				String active_id =rs.getString("act_id");
				String account_id =rs.getString("aid");
				String query = rs.getString("copy_link");
				String wxname =rs.getString("acc_name");
				if(query!=null&&wxname!=null){
					map.put(query_list_id, list_id);
					map.put(query_user_id, user_id);
					map.put(query_active_id, active_id);
					map.put(query_account_id, account_id);
					map.put(query_key, query.replace("&amp;", "&"));
					map.put(query_wxname, wxname);
					map.put(query_data_source, "0");
					list.add(map);
				}
			}
			

			ps = MySqlUtils.getConn(".test").prepareStatement(""
					+ "SELECT j1.i_id , j1.uid ,j1.act_id ,j1.aid , j1.copy_link ,j1.acc_name  FROM jm_user_intention_list j1 ,jm_activity j2   "
					+ "WHERE j1.uid = j2.uid  and j1.act_id=j2.act_id  AND j2.monitor_type=1  "
					+ "AND  NOW()  BETWEEN j1.start_time AND  j1.end_time  "
					+ "AND j1.acc_num is not null  and j1.status = 1 ");
			ResultSet rstest = ps.executeQuery();
			while (rstest.next()) {
				Map<String,String>  map = new HashMap<String, String>();
				String list_id = rstest.getString("i_id");
				String user_id = rstest.getString("uid");
				String active_id =rstest.getString("act_id");
				String account_id =rstest.getString("aid");
				String query = rstest.getString("copy_link");
				String wxname =rstest.getString("acc_name");
				if(query!=null&&wxname!=null){
					map.put(query_list_id, list_id);
					map.put(query_user_id, user_id);
					map.put(query_active_id, active_id);
					map.put(query_account_id, account_id);
					map.put(query_key, query.replace("&amp;", "&"));
					map.put(query_wxname, wxname);
					map.put(query_data_source, "1");
					list.add(map);
				}
			}
		
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return list;
	}

	protected void initSpider() {
		if (this.spider == null) {
			synchronized (this) {
				if (this.spider == null) {
					this.spider = Spider.create(new SogouFreqUrlPageProcessor())//
							.addPipeline(new SogouFreqUrlPipeline())//
							.setScheduler(new RedisScheduler(Redis.jedisPool))//
							.setDownloader(new SogouUrlDownloader())//
							.setUUID("sogouFreqUrlSpider")//
							.thread(10)//
							.setExitWhenComplete(true);
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
	
	public static String getUid() {
		  int num = (int)(Math.random()*100);
		  StringBuffer uuid = new StringBuffer();
		  for (int i = 0; i < num; i++) {
			  uuid.append("&");
		}
		  return uuid.toString();
	}
	
	
	public static void main(String[] args) throws Exception {
		SogouFreqUrlService service = new SogouFreqUrlService();
			service.start();
	}

}
