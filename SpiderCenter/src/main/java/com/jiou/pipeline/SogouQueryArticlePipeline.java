package com.jiou.pipeline;

import java.text.SimpleDateFormat;
import java.util.Date;

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
public class SogouQueryArticlePipeline implements Pipeline {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected static final String insert_time = "insert_time";

	protected static final String sogou_article = "sogou_query_article";

	protected DBCollection artColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
			.getCollection(sogou_article);

	public void process(ResultItems resultItems, Task task) {
		if (resultItems == null || resultItems.getAll() == null) {
			return;
		}
		
		Integer readnum = resultItems.get(SogouPipeline.read_num);
		if (readnum != null && readnum != -1) {
			saveArticle(resultItems);
		}
	}

	protected void saveArticle(ResultItems resultItems) {
		try {
			DBObject doc = new BasicDBObject();
			
//			doc.put(SogouPipeline.uid, resultItems.get(SogouPipeline.uid));
			doc.put(SogouPipeline.wxnum, resultItems.get(SogouPipeline.wxnum));
			doc.put(SogouPipeline.wxname, resultItems.get(SogouPipeline.wxname));
			doc.put(SogouPipeline.title, resultItems.get(SogouPipeline.title));
			doc.put(SogouPipeline.read_num, resultItems.get(SogouPipeline.read_num));
			doc.put(SogouPipeline.like_num, resultItems.get(SogouPipeline.like_num));
			doc.put(SogouPipeline.url, resultItems.get(SogouPipeline.url));
			doc.put(SogouPipeline.pubtime, 	new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) resultItems.get(SogouPipeline.pubtime)));
			doc.put(insert_time, new Date());
			artColl.insert(doc);
		} catch (Exception ex) {
			logger.error("article 插入文章内容错误", ex);
		}
	}

}
