package com.jiou.pipeline;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;
import orestes.bloomfilter.redis.helper.RedisPool;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.jiou.support.Consts;
import com.jiou.support.GZipCompress;
import com.jiou.support.Redis;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@SuppressWarnings("deprecation")
public class SogouPipeline implements Pipeline {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private static final String filterName = "sogou_bloom_filter";

	private BloomFilter<String> bloomFilter = new FilterBuilder(10000000, 0.001).name(filterName).redisBacked(true)
			.redisPool(new RedisPool(Redis.jedisPool)).buildBloomFilter();

	public static final String account = "1";
	public static final String artlist = "2";
	public static final String numlist = "3";

	public static final String mongo_coll_acnt = "wx_accounts";
	public static final String mongo_coll_html = "wx_art_htmls";
	public static final String mongo_coll_num = "wx_art_nums";
	public static final String mongo_coll_art = "wx_arts";
	public static final String mongo_coll_task = "wx_list";

	public static final String date = "date";// 日期
	public static final String user_id = "user_id";// 用户号    uid
	public static final String active_id = "active_id";// 活动号 act_id
	public static final String account_id = "account_id";//  aid
	public static final String wxnum = "wxnum";// 微信号
	public static final String wxname = "wxname";// 微信名
	public static final String url = "url";// URL
	public static final String portrait_url = "portrait_url";// 头像URL
	public static final String portrait = "portrait";// 头像
	public static final String qrcode = "qrcode";// 二维码
	public static final String brief = "brief";// 功能介绍
	public static final String auth = "auth";// 认证信息

	public static final String list_id = "list_id";// i_id
	public static final String uid = "uid";// 唯一标识
	public static final String title = "title";// 标题
	public static final String content = "content";// 正文
	public static final String pubtime = "pubtime";// 发布时间
	public static final String original = "original";// 是否原创
	public static final String idx = "idx";// 第几条
	public static final String read_num = "read_num";// 阅读数
	public static final String like_num = "like_num";// 点赞数
	public static final String html = "html";// html源码
	public static final String ismulti = "ismulti";// 是否单图文,0-否,1-是
	public static final String source_url = "source_url";// 源URL
	public static final String cover = "cover";// 图片URL
	public static final String data_source = "data_source";// 唯一标识

	private DBCollection acntColl;
	private DBCollection artColl;
	private DBCollection numColl;
	private DBCollection htmlColl;

	public SogouPipeline() {
		acntColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name).getCollection(mongo_coll_acnt);
		artColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name).getCollection(mongo_coll_art);
		numColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name).getCollection(mongo_coll_num);
		htmlColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name).getCollection(mongo_coll_html);
	}

//	@Override
	public void process(ResultItems resultItems, Task task) {
		if (resultItems == null || resultItems.getAll().isEmpty()) {
			return;
		}
		long start = System.currentTimeMillis();
		Map<String, Object> accountmap = resultItems.get(account);
		int count = persistAccount(accountmap);
		List<Map<String, Object>> arts = resultItems.get(artlist);
		if (arts != null) {
			count += persistArticle(arts);
		}
		List<Map<String, Object>> nums = resultItems.get(numlist);
		if (nums != null) {
			count += persistNums(nums);
		}
		long end = System.currentTimeMillis();
		logger.info("搜狗微信持久化耗时:{}ms,插入条数:{}", end - start, count);
	}

	private int persistNums(List<Map<String, Object>> nums) {
		int count = 0;
		for (Map<String, Object> map : nums) {
			try {
				DBObject update = new BasicDBObject();
				update.put(SogouPipeline.wxnum, map.get(SogouPipeline.wxnum));
				update.put(SogouPipeline.uid, map.get(SogouPipeline.uid));
				update.put(SogouPipeline.read_num, map.get(SogouPipeline.read_num));
				update.put(SogouPipeline.like_num, map.get(SogouPipeline.like_num));
				update.put(SogouPipeline.date, new Date());
				numColl.insert(update);
				count++;
			} catch (Exception ignore) {
			}
		}
		return count;
	}

	private int persistArticle(List<Map<String, Object>> arts) {
		Date date = new Date();
		int count = 0;
		for (Map<String, Object> map : arts) {
			String wxnum = (String) map.get(SogouPipeline.wxnum);
			String uid = (String) map.get(SogouPipeline.uid);
			if (StringUtils.isBlank(wxnum) || StringUtils.isBlank(uid)) {
				continue;
			}
			try {
				DBObject update = new BasicDBObject(SogouPipeline.wxnum, wxnum);
				update.put(SogouPipeline.uid, uid);
				update.put(SogouPipeline.html, GZipCompress.compressToByteArray((String) map.get(SogouPipeline.html)));
				update.put(SogouPipeline.date, date);
				htmlColl.insert(update);
				count++;
			} catch (Exception ignore) {
			}
			try {
				DBObject update = new BasicDBObject(SogouPipeline.wxnum, wxnum);
				update.put(SogouPipeline.uid, uid);
				update.put(SogouPipeline.title, map.get(SogouPipeline.title));
				update.put(SogouPipeline.content, map.get(SogouPipeline.content));
				update.put(SogouPipeline.pubtime, map.get(SogouPipeline.pubtime));
				update.put(SogouPipeline.original, map.get(SogouPipeline.original));
				update.put(SogouPipeline.ismulti, map.get(SogouPipeline.ismulti));
				update.put(SogouPipeline.idx, map.get(SogouPipeline.idx));
				update.put(SogouPipeline.source_url, map.get(SogouPipeline.source_url));
				update.put(SogouPipeline.cover, map.get(SogouPipeline.cover));
				update.put(SogouPipeline.url, map.get(SogouPipeline.url));
				update.put(SogouPipeline.date, date);
				artColl.insert(update);
				bloomFilter.add(uid);
				count++;
			} catch (Exception ignore) {
			}
		}
		return count;
	}

	private int persistAccount(Map<String, Object> map) {
		Date date = new Date();
		try {
			if (map == null) {
				return 0;
			}
			String wxnum = (String) map.get(SogouPipeline.wxnum);
			if (StringUtils.isBlank(wxnum)) {
				return 0;
			}
			DBObject query = new BasicDBObject(SogouPipeline.wxnum, wxnum);
			Iterator<DBObject> it = acntColl.find(query).limit(1).iterator();
			if (it.hasNext()) {
				DBObject update = it.next();
				if (map.get(SogouPipeline.wxname) != null) {
					update.put(SogouPipeline.wxname, map.get(SogouPipeline.wxname));
				}
				if (map.get(SogouPipeline.portrait_url) != null) {
					update.put(SogouPipeline.portrait_url, map.get(SogouPipeline.portrait_url));
				}
				if (map.get(SogouPipeline.portrait) != null) {
					update.put(SogouPipeline.portrait, map.get(SogouPipeline.portrait));
				}
				if (map.get(SogouPipeline.qrcode) != null) {
					update.put(SogouPipeline.qrcode, map.get(SogouPipeline.qrcode));
				}
				if (map.get(SogouPipeline.brief) != null) {
					update.put(SogouPipeline.brief, map.get(SogouPipeline.brief));
				}
				if (map.get(SogouPipeline.auth) != null) {
					update.put(SogouPipeline.auth, map.get(SogouPipeline.auth));
				}
				update.put(SogouPipeline.date, date);
				acntColl.update(query, update, true, false);
				return 1;
			} else {
				query.put(SogouPipeline.wxname, map.get(SogouPipeline.wxname));
				query.put(SogouPipeline.portrait_url, map.get(SogouPipeline.portrait_url));
				query.put(SogouPipeline.portrait, map.get(SogouPipeline.portrait));
				query.put(SogouPipeline.qrcode, map.get(SogouPipeline.qrcode));
				query.put(SogouPipeline.brief, map.get(SogouPipeline.brief));
				query.put(SogouPipeline.auth, map.get(SogouPipeline.auth));
				query.put(SogouPipeline.date, date);
				acntColl.insert(query);
				return 1;
			}
		} catch (Exception ignore) {
			return 0;
		}
	}

	public boolean existArt(String uid) {
		if (StringUtils.isBlank(uid)) {
			return true;
		}
		return artColl.count(new BasicDBObject(SogouPipeline.uid, uid)) > 0;
	}

	// public static void main(String[] args) {
	// boolean exist = new
	// SogouPipeline().existArt("6148e92e5c27a3f1952cf2abdf6cddd3");
	// System.out.println(exist);
	// }

}
