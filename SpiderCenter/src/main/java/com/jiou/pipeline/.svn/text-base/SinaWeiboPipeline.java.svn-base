package com.jiou.pipeline;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.jiou.domain.SinaWeibo;
import com.jiou.support.Consts;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@SuppressWarnings({ "deprecation", "unused" })
public class SinaWeiboPipeline implements Pipeline {

	public static final String insert_time = "insert_time";
	public static final String update_time = "update_time";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private DBCollection userColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
			.getCollection(SinaWeibo.weibo_user);
	private DBCollection artColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
			.getCollection(SinaWeibo.weibo_art);

	public void process(ResultItems resultItems, Task task) {
		if (resultItems == null || resultItems.getAll().isEmpty()) {
			return;
		}
		try {
			String uid = resultItems.get(SinaWeibo.uid);
			String uname = resultItems.get(SinaWeibo.uname);
			DBObject query = new BasicDBObject(SinaWeibo.uid, uid);
			DBObject update = new BasicDBObject();
			update.put(SinaWeibo.url, resultItems.get(SinaWeibo.url));
			update.put(SinaWeibo.uid, uid);
			update.put(SinaWeibo.group, resultItems.get(SinaWeibo.group));
			update.put(SinaWeibo.cat, resultItems.get(SinaWeibo.cat));
			update.put(SinaWeibo.uname, uname);
			update.put(SinaWeibo.gender, resultItems.get(SinaWeibo.gender));
			update.put(SinaWeibo.brief, resultItems.get(SinaWeibo.brief));
			update.put(SinaWeibo.address, resultItems.get(SinaWeibo.address));
			update.put(SinaWeibo.label, resultItems.get(SinaWeibo.label));
			update.put(SinaWeibo.isveri, resultItems.get(SinaWeibo.isveri));
			update.put(SinaWeibo.isvip, resultItems.get(SinaWeibo.isvip));
			update.put(SinaWeibo.concern, resultItems.get(SinaWeibo.concern));
			update.put(SinaWeibo.fans, resultItems.get(SinaWeibo.fans));
			update.put(SinaWeibo.blognums, resultItems.get(SinaWeibo.blognums));
			update.put(SinaWeibo.level, resultItems.get(SinaWeibo.level));
			update.put(update_time, new Date());
			userColl.update(query, update, true, false);
			logger.info("save博主:{},uid:{}", uname, uid);
		} catch (Exception ex) {
			logger.error("保存博主错误", ex);
		}
	}

}
