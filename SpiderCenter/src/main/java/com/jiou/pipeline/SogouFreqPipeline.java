package com.jiou.pipeline;

import java.util.Date;

import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;
import orestes.bloomfilter.redis.helper.RedisPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.jiou.pageprocessor.wx.SogouFreqPageProcessor;
import com.jiou.support.Consts;
import com.jiou.support.Redis;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@SuppressWarnings("deprecation")
public class SogouFreqPipeline implements Pipeline {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected static final String insert_time = "insert_time";

	protected static final String sogou_freq_article = "sogou_freq_article";
	protected static final String sogou_freq_nums = "sogou_freq_nums";
	protected static final String query_data_source = "data_source";

	protected BloomFilter<String> bloomFilter = new FilterBuilder(5000000, 0.0001)
			.name(SogouFreqPageProcessor.filterName)//
			.redisBacked(true).redisPool(new RedisPool(Redis.jedisPool)).buildBloomFilter();

	protected DBCollection artColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
			.getCollection(sogou_freq_article);
	protected DBCollection numColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
			.getCollection(sogou_freq_nums);

	public void process(ResultItems resultItems, Task task) {
		if (resultItems == null || resultItems.getAll() == null) {
			return;
		}
		String title = resultItems.get(SogouPipeline.title);
		if (title != null) {
			saveArticle(resultItems);
		}
		Integer readnum = resultItems.get(SogouPipeline.read_num);
		if (readnum != null && readnum != -1) {
			saveNums(resultItems);
		}
	}

	protected void saveArticle(ResultItems resultItems) {
		try {
			DBObject doc = new BasicDBObject();
			String uid = resultItems.get(SogouPipeline.uid);
			doc.put(SogouPipeline.uid, uid);
			doc.put(SogouPipeline.list_id, resultItems.get(SogouPipeline.list_id));
			doc.put(SogouPipeline.title, resultItems.get(SogouPipeline.title));
			doc.put(SogouPipeline.user_id, resultItems.get(SogouPipeline.user_id));
			doc.put(SogouPipeline.active_id, resultItems.get(SogouPipeline.active_id));
			doc.put(SogouPipeline.account_id, resultItems.get(SogouPipeline.account_id));
			doc.put(SogouPipeline.wxnum, resultItems.get(SogouPipeline.wxnum));
			doc.put(SogouPipeline.wxname, resultItems.get(SogouPipeline.wxname));
			doc.put(SogouPipeline.url, resultItems.get(SogouPipeline.url));
			doc.put(SogouPipeline.content, resultItems.get(SogouPipeline.content));
			doc.put(SogouPipeline.pubtime, new Date(Long.parseLong(String.valueOf( 
					resultItems.get(SogouPipeline.pubtime)))) );
			doc.put(insert_time, new Date());
			doc.put(SogouPipeline.data_source, resultItems.get(SogouPipeline.data_source));
			artColl.insert(doc);
			bloomFilter.add(uid);
		} catch (Exception ex) {
			logger.error("插入文章内容错误", ex);
		}
	}

	protected void saveNums(ResultItems resultItems) {
		try {
			DBObject doc = new BasicDBObject();
			doc.put(SogouPipeline.uid, resultItems.get(SogouPipeline.uid));
			doc.put(SogouPipeline.read_num, resultItems.get(SogouPipeline.read_num));
			doc.put(SogouPipeline.like_num, resultItems.get(SogouPipeline.like_num));
			doc.put(insert_time, new Date());
			numColl.insert(doc);
		} catch (Exception ignore) {
		}
	}
}
