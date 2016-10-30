package com.jiou.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;
import com.jiou.support.JDBCUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryOperators;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@Component("cleanWBYFriendService")
@Scope("singleton")
public class CleanWBYFriendService extends CleanService {

	public void cleanFriend() {
		long startTime = System.currentTimeMillis();
		Map<String, Integer> catMap = loadCatMap();
		Connection conn = null;
		try {
			conn = getConn();
			MongoClient mongoClient = MongoPipeline.getMongoclient();
			MongoCollection<Document> collection = mongoClient.getDatabase(Consts.mongo_database_name).getCollection(
					WeiBoYiService.mongo_collection_name_friends);
			Date start = DateUtils.truncate(new Date(), Calendar.DATE);
			Date end = DateUtils.addDays(start, 1);
			FindIterable<Document> fi = collection.find(new BasicDBObject().append("insert_time", new BasicDBObject(
					QueryOperators.GTE, start).append(QueryOperators.LT, end)));
			MongoCursor<Document> mc = fi.iterator();
			String wxno = null;
			while (mc.hasNext()) {
				try {
					Document doc = mc.next();
					wxno = getWXNo(doc);
					if (StringUtils.isBlank(wxno)) {
						continue;
					}
					Integer uid = getUid(wxno, conn);
					int catid = getCatId(doc.getString("domain"), catMap);
					if (uid == null) {// 表中不存在该微信公众号
						insert(wxno, doc, catid, conn);
					} else if (uid == 266) {// 可以更新全部字段
						updateAll(wxno, doc, catid, conn);
					} else {// 只能更新部分字段
						updatePart(wxno, doc, catid, conn);
					}
				} catch (Exception ex) {
					logger.error("清洗{}错误", wxno);
					logger.error("清洗错误", ex);
				}
			}
		} catch (Exception e) {
			logger.error("微博易朋友圈清洗错误", e);
		} finally {
			JDBCUtils.closeConnection(conn);
			logger.info("微博易朋友圈清洗耗时:{}ms", System.currentTimeMillis() - startTime);
		}
	}

	private static final String insertSql = "insert into tp30_accounts"//
			+ "(platform_type,wxname,wxnum,jump_url,area,"//
			+ "province_name,city_name,sex,"//
			+ "age,friends_describe,fans_num," //
			+ "put_cases,month_order_num,"//
			+ "week_order_num,toatl_order_num," //
			+ "share_price,price,is_verified,is_original," //
			+ "uid,acc_type" //
			+ ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

	private void insert(String wxno, Document doc, int catid, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(insertSql);
			ps.setInt(1, 1);
			ps.setString(2, doc.getString("wxname"));
			ps.setString(3, wxno);
			ps.setString(4, doc.getString("home_url"));
			ps.setInt(5, catid);
			// Pair<String, String> pair =
			// getProvinceAndArea(doc.getString("area"));
			ps.setString(6, null);
			ps.setString(7, doc.getString("area"));
			// ps.setString(6, pair.getFirst());
			// ps.setString(7, pair.getSecond());
			ps.setInt(8, doc.getInteger("gender", 3));
			ps.setInt(9, doc.getInteger("age", 0));
			ps.setString(10, doc.getString("impression"));
			ps.setInt(11, getFunsCount(doc.get("friendsno")));
			ps.setString(12, doc.getString("tfcase"));
			ps.setInt(13, doc.getInteger("mon_orders", 0));
			ps.setInt(14, doc.getInteger("week_orders", 0));
			ps.setInt(15, doc.getInteger("total_orders", 0));
			ps.setDouble(16, getMinPrice(doc));
			ps.setDouble(17, getMaxPrice(doc));
			ps.setInt(18, getIsVeri(doc.getInteger("is_veri", 2)));
			ps.setInt(19, getIsOri(doc.getInteger("can_ori", 2)));
			ps.setInt(20, 266);
			ps.setInt(21, 2);
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeStatement(ps);
		}
	}

	private static final String updateAllSql = "update tp30_accounts set "//
			+ "wxname=?,wxnum=?,jump_url=?,area=?,"//
			+ "city_name=?,sex=?,"//
			+ "age=?,friends_describe=?,fans_num=?," //
			+ "put_cases=?,month_order_num=?,"//
			+ "week_order_num=?,toatl_order_num=?," //
			+ "share_price=?,price=?,is_verified=?,is_original=?" //
			+ " where wxnum=?";

	private void updateAll(String wxno, Document doc, int catid, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(updateAllSql);
			ps.setString(1, doc.getString("wxname"));
			ps.setString(2, wxno);
			ps.setString(3, doc.getString("home_url"));
			ps.setInt(4, catid);
			ps.setString(5, doc.getString("area"));
			ps.setInt(6, doc.getInteger("gender", 3));
			ps.setInt(7, doc.getInteger("age", 0));
			ps.setString(8, doc.getString("impression"));
			ps.setInt(9, getFunsCount(doc.get("friendsno")));
			ps.setString(10, doc.getString("tfcase"));
			ps.setInt(11, doc.getInteger("mon_orders", 0));
			ps.setInt(12, doc.getInteger("week_orders", 0));
			ps.setInt(13, doc.getInteger("total_orders", 0));
			ps.setDouble(14, getMinPrice(doc));
			ps.setDouble(15, getMaxPrice(doc));
			ps.setInt(16, getIsVeri(doc.getInteger("is_veri", 2)));
			ps.setInt(17, getIsOri(doc.getInteger("can_ori", 2)));
			ps.setString(18, wxno);
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeStatement(ps);
		}
	}

	private static final String updatePartSql = "update tp30_accounts set "//
			+ "is_original=?,jump_url=?,"//
			+ "month_order_num=?,week_order_num=?,toatl_order_num=?,"//
			+ "is_verified=?"//
			+ " where wxnum=?";

	private void updatePart(String wxno, Document doc, int catid, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(updatePartSql);
			ps.setInt(1, getIsOri(doc.getInteger("can_ori", 2)));
			ps.setString(2, doc.getString("home_url"));
			ps.setInt(3, doc.getInteger("mon_orders", 0));
			ps.setInt(4, doc.getInteger("week_orders", 0));
			ps.setInt(5, doc.getInteger("total_orders", 0));
			ps.setInt(6, getIsVeri(doc.getInteger("is_veri", 2)));
			ps.setString(7, wxno);
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeStatement(ps);
		}
	}

	private String getWXNo(Document doc) {
		if (doc == null) {
			return null;
		}
		String url = doc.getString("home_url");
		if (StringUtils.isNotBlank(url) && url.startsWith("weixin://")) {
			String s = url.substring(url.lastIndexOf("/") + 1);
			return s;
		}
		return doc.getString("wxno");
	}

	private Map<String, Integer> loadCatMap() {
		Map<String, Integer> catMap = new HashMap<String, Integer>();
		Connection conn = null;
		try {
			conn = getConn();
			ResultSet rs = conn.createStatement().executeQuery("select name,id from tp30_area where c_type=1");
			while (rs.next()) {
				String key = rs.getString(1);
				Integer value = rs.getInt(2);
				catMap.put(key, value);
			}
			catMap.put("未知", catMap.get("其他"));
			catMap.put("游戏动漫", catMap.get("游戏/动漫"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeConnection(conn);
		}
		return catMap;
	}

	// 获取分类id
	private int getCatId(String s, Map<String, Integer> catMap) {
		if (StringUtils.isBlank(s)) {
			return catMap.get("其他");
		}
		Integer i = catMap.get(s);
		return i == null ? catMap.get("其他") : i;
	}

	private double getMinPrice(Document doc) {
		if (doc != null) {
			Double d = extractDouble(doc.get("external_refer_price_min"));
			if (d != null) {
				return d;
			}
			d = extractDouble(doc.get("net_deal_price"));
			if (d != null) {
				return d;
			}
		}
		return 0d;
	}

	private double getMaxPrice(Document doc) {
		if (doc != null) {
			Double d = extractDouble(doc.get("external_refer_price_max"));
			if (d != null) {
				return d;
			}
			d = extractDouble(doc.get("gross_deal_price"));
			if (d != null) {
				return d;
			}
		}
		return 0d;
	}

	private int getIsVeri(int is_veri) {
		return is_veri == 1 ? 2 : 1;
	}

	private int getIsOri(int isOri) {
		return isOri == 1 ? 2 : 1;
	}

	public static void main(String[] args) {
		// NlpAnalysis.parse("你好");
		CleanWBYFriendService cleanWBYDataService = new CleanWBYFriendService();
		cleanWBYDataService.cleanFriend();
	}

}
