package com.jiou.pipeline;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.jiou.support.Consts;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;


@SuppressWarnings("deprecation")
public class CWQPipeline implements Pipeline 
{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String ACCOUNT_KEY = "cwq_ids";
	public static final String ID_KEY = "id";
	
	public static final String CWQ_COLLECTION = "chengwaiquan_add";
	private DBCollection cwqDBColl;
	
	public static final String url = "url";
	public static final String wxnum = "wxnum";
	public static final String wxname = "wxname";
	public static final String auth = "auth";
	public static final String brief = "brief";
	public static final String classes = "classes";
	public static final String tag = "tag";
	public static final String fans_num = "fans_num";
	public static final String yg_first_price = "yg_first_price";
	public static final String yg_second_price = "yg_second_price";
	public static final String rg_first_price = "rg_first_price";
	public static final String rg_second_price = "rg_second_price";
	public static final String update_time = "update_time";
	

	public CWQPipeline() {
		this.cwqDBColl = MongoPipeline.getMongoclient()
				.getDB(Consts.mongo_database_name)
				.getCollection(CWQ_COLLECTION);
	}

	@SuppressWarnings("unchecked")
	public void process(ResultItems resultItems, Task task) 
	{
		long start = System.currentTimeMillis();
		
		Map<String, Object> accMap = resultItems.get(ACCOUNT_KEY);
		int count = persistAccount(accMap);
		
		long end = System.currentTimeMillis();
		logger.info("城外圈持久化耗时:{}ms, 更新条数:{}", end - start, count);
	}

	private int persistAccount(Map<String, Object> map) 
	{
		try {
			if (map == null) {
				return 0;
			}
			String wxnum = (String) map.get(CWQPipeline.wxnum);
			if (StringUtils.isBlank(wxnum)) {
				return 0;
			}
			
			DBObject query = new BasicDBObject(CWQPipeline.wxnum, wxnum);
			Iterator<DBObject> it = cwqDBColl.find(query).limit(1).iterator();
			
			if (it.hasNext()) {
				DBObject update = it.next();
				if (map.get(CWQPipeline.url) != null) {
					update.put(CWQPipeline.url, map.get(CWQPipeline.url));
				}
				if (map.get(CWQPipeline.wxnum) != null) {
					update.put(CWQPipeline.wxnum, map.get(CWQPipeline.wxnum));
				}
				if (map.get(CWQPipeline.wxname) != null) {
					update.put(CWQPipeline.wxname, map.get(CWQPipeline.wxname));
				}
				if (map.get(CWQPipeline.auth) != null) {
					update.put(CWQPipeline.auth, map.get(CWQPipeline.auth));
				}
				if (map.get(CWQPipeline.brief) != null) {
					update.put(CWQPipeline.brief, map.get(CWQPipeline.brief));
				}
				if (map.get(CWQPipeline.classes) != null) {
					update.put(CWQPipeline.classes, map.get(CWQPipeline.classes));
				}
				if (map.get(CWQPipeline.tag) != null) {
					update.put(CWQPipeline.tag, map.get(CWQPipeline.tag));
				}
				if (map.get(CWQPipeline.fans_num) != null) {
					update.put(CWQPipeline.fans_num, map.get(CWQPipeline.fans_num));
				}
				if (map.get(CWQPipeline.yg_first_price) != null) {
					update.put(CWQPipeline.yg_first_price, map.get(CWQPipeline.yg_first_price));
				}
				if (map.get(CWQPipeline.yg_second_price) != null) {
					update.put(CWQPipeline.yg_second_price, map.get(CWQPipeline.yg_second_price));
				}
				if (map.get(CWQPipeline.rg_first_price) != null) {
					update.put(CWQPipeline.rg_first_price, map.get(CWQPipeline.rg_first_price));
				}
				if (map.get(CWQPipeline.rg_second_price) != null) {
					update.put(CWQPipeline.rg_second_price, map.get(CWQPipeline.rg_second_price));
				}
				update.put(CWQPipeline.update_time, new Date());
				cwqDBColl.update(query, update, true, false);
				return 1;
			} 
			else {
				query.put(CWQPipeline.url, map.get(CWQPipeline.url));
				query.put(CWQPipeline.wxnum, map.get(CWQPipeline.wxnum));
				query.put(CWQPipeline.wxname, map.get(CWQPipeline.wxname));
				query.put(CWQPipeline.auth, map.get(CWQPipeline.auth));
				query.put(CWQPipeline.brief, map.get(CWQPipeline.brief));
				query.put(CWQPipeline.classes, map.get(CWQPipeline.classes));
				query.put(CWQPipeline.tag, map.get(CWQPipeline.tag));
				query.put(CWQPipeline.fans_num, map.get(CWQPipeline.fans_num));
				query.put(CWQPipeline.yg_first_price, map.get(CWQPipeline.yg_first_price));
				query.put(CWQPipeline.yg_second_price, map.get(CWQPipeline.yg_second_price));
				query.put(CWQPipeline.rg_first_price, map.get(CWQPipeline.rg_first_price));
				query.put(CWQPipeline.rg_second_price, map.get(CWQPipeline.rg_second_price));
				query.put(CWQPipeline.update_time, new Date());
				cwqDBColl.insert(query);
				return 1;
			}
		} catch (Exception ignore) {
			return 0;
		}
	}

}
