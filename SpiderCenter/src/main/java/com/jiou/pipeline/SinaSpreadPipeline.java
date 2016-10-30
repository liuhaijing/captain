package com.jiou.pipeline;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.jiou.domain.SinaSpread;
import com.jiou.domain.SinaWeibo;
import com.jiou.support.Consts;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@SuppressWarnings("deprecation")
public class SinaSpreadPipeline implements Pipeline {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected DBCollection bloggerColl;
	protected DBCollection blogColl;

	public SinaSpreadPipeline() {
		this.bloggerColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
				.getCollection(SinaWeibo.weibo_user);
		this.blogColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
				.getCollection(SinaWeibo.microblog);
	}

	public void process(ResultItems resultItems, Task task) {
		// do nothing
	}

	public void save(SinaSpread data) {
		if (data == null) {
			return;
		}
		try {
			DBObject update = new BasicDBObject();
			update.put(SinaWeibo.uid, data.getUid());
			update.put(SinaWeibo.query, data.getQuery());
			update.put(SinaWeibo.puid, data.getPuid());

			update.put(SinaWeibo.uname, data.getUname());
			update.put(SinaWeibo.gender, data.getGender());
			update.put(SinaWeibo.brief, data.getBrief());
			update.put(SinaWeibo.address, data.getAddress());
			update.put(SinaWeibo.label, data.getLabel());
			update.put(SinaWeibo.isveri, data.getIsveri());
			update.put(SinaWeibo.isvip, data.getIsvip());
			update.put(SinaWeibo.concern, data.getConcern());
			update.put(SinaWeibo.fans, data.getFans());
			update.put(SinaWeibo.blognums, data.getBlognums());
			update.put(SinaWeibo.level, data.getLevel());

			update.put(SinaWeibo.pubtime, data.getPubtime());
			update.put(SinaWeibo.reviews, data.getReviews());
			update.put(SinaWeibo.forwards, data.getForwards());
			update.put(SinaWeibo.likenum, data.getLikenum());
			update.put(SinaWeibo.content, data.getContent());

			update.put(SinaWeibo.insert_time, new Date());
			bloggerColl.insert(update);
			logger.info("新浪传播链保存:{}", data.toString());
		} catch (Exception ex) {
			logger.error("保存博主错误", ex);
		}
	}

}
