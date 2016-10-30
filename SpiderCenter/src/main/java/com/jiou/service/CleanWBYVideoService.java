package com.jiou.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
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

/**
 * 微播易视频数据清洗
 * 
 * @author zhe.li
 */
@Component("cleanWBYVideoService")
@Scope("singleton")
public class CleanWBYVideoService extends CleanService {

	public void clean() {
		long startTime = System.currentTimeMillis();
		Map<String, Integer> aCatMap = loadAreaCatMap();
		Map<String, Integer> pCatMap = loadProCatMap();
		Connection conn = null;
		try {
			conn = getConn();
			MongoClient mongoClient = MongoPipeline.getMongoclient();
			MongoCollection<Document> collection = mongoClient.getDatabase(Consts.mongo_database_name).getCollection(
					WeiBoYiService.mongo_collection_name_pai);
			Date start = DateUtils.truncate(new Date(), Calendar.DATE);
			Date end = DateUtils.addDays(start, 1);
			FindIterable<Document> fi = collection.find(new BasicDBObject().append("insert_time", new BasicDBObject(
					QueryOperators.GTE, start).append(QueryOperators.LT, end)));
			MongoCursor<Document> mc = fi.iterator();
			String wxno = null;
			while (mc.hasNext()) {
				try {
					Document doc = mc.next();
					String home_url = doc.getString("home_url");
					wxno = getWXNO(home_url);
					if (StringUtils.isBlank(wxno)) {
						continue;
					}
					int platform = getPlatForm(doc.getInteger("source"));
					int acatid = getCatId(doc.getString("domain"), aCatMap);
					int pcatid = getCatId(doc.getString("profession"), pCatMap);
					int role = getRole(doc);
					Integer uid = getUid(wxno, conn);
					if (uid == null) {// 表中不存在该微信公众号
						insert(wxno, doc, acatid, pcatid, platform, role, conn);
					} else if (uid == 266) {// 可以更新全部字段
						updateAll(wxno, doc, acatid, pcatid, platform, role, conn);
					} else {// 只能更新部分字段
						updatePart(wxno, doc, acatid, pcatid, platform, role, conn);
					}
				} catch (Exception ex) {
					logger.error("清洗{}错误", wxno);
					logger.error("清洗错误", ex);
				}
			}
		} catch (Exception e) {
			logger.error("微博易视频清洗错误", e);
		} finally {
			JDBCUtils.closeConnection(conn);
			logger.info("微博易视频清洗耗时:{}ms", System.currentTimeMillis() - startTime);
		}
	}

	private static final String insertSql = "insert into tp30_accounts"//
			+ "(uid,acc_type,platform_type,wxname,wxnum,jump_url,area,"//
			+ "vocation,sex,city_name,"//
			+ "friends_describe,c_notice,fans_num," //
			+ "is_original,w_degree,"//
			+ "start_price,end_price," //
			+ "avg_num_views,avg_num_reviews,avg_like_num," //
			+ "roles" //
			+ ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	private void insert(String wxno, Document doc, int acatid, int pcatid, int platform, int role, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(insertSql);
			ps.setInt(1, 266);
			ps.setInt(2, 2);
			ps.setInt(3, platform);
			ps.setString(4, doc.getString("name"));
			ps.setString(5, wxno);
			ps.setString(6, doc.getString("home_url"));
			if (role == 1) {// 名人
				ps.setInt(7, 0);
				ps.setInt(8, pcatid);
			} else {
				ps.setInt(7, acatid);
				ps.setInt(8, 0);
			}
			ps.setInt(9, doc.getInteger("gender", 3));
			ps.setString(10, doc.getString("area"));
			ps.setString(11, doc.getString("desc"));
			ps.setString(12, doc.getString("coop_notice"));
			ps.setInt(13, getFunsCount(doc.get("funsno")));
			ps.setInt(14, doc.getInteger("can_ori", 1));
			ps.setInt(15, getWXDegree(doc.getString("coop_degree")));
			ps.setDouble(16, extractDouble(doc.get("external_refer_price_min")));
			ps.setDouble(17, extractDouble(doc.get("external_refer_price_max")));
			Double views = extractDouble(doc.get("views"));
			if (views == null) {
				ps.setDouble(18, 0d);
			} else {
				ps.setDouble(18, views);
			}
			Double reviews = extractDouble(doc.get("reviews"));
			if (reviews == null) {
				ps.setDouble(19, 0d);
			} else {
				ps.setDouble(19, reviews);
			}
			Double approvals = extractDouble(doc.get("approvals"));
			if (approvals == null) {
				ps.setDouble(20, 0d);
			} else {
				ps.setDouble(20, approvals);
			}
			ps.setInt(21, role);
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeStatement(ps);
		}
	}

	private static final String updateAllSql = "update tp30_accounts set "//
			+ "wxname=?,jump_url=?,area=?,"//
			+ "vocation=?,sex=?,city_name=?,"//
			+ "friends_describe=?,c_notice=?,fans_num=?," //
			+ "is_original=?,w_degree=?,"//
			+ "start_price=?,end_price=?," //
			+ "avg_num_views=?,avg_num_reviews=?,avg_like_num=?,roles=?" //
			+ " where wxnum=?";

	private void updateAll(String wxno, Document doc, int acatid, int pcatid, int platform, int role, Connection conn) {

		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(updateAllSql);
			ps.setString(1, doc.getString("name"));
			ps.setString(2, doc.getString("home_url"));
			if (role == 1) {// 名人
				ps.setInt(3, 0);
				ps.setInt(4, pcatid);
			} else {
				ps.setInt(3, acatid);
				ps.setInt(4, 0);
			}
			ps.setInt(5, doc.getInteger("gender", 3));
			ps.setString(6, doc.getString("area"));
			ps.setString(7, doc.getString("desc"));
			ps.setString(8, doc.getString("coop_notice"));
			ps.setInt(9, getFunsCount(doc.get("funsno")));
			ps.setInt(10, doc.getInteger("can_ori", 1));
			ps.setInt(11, getWXDegree(doc.getString("coop_degree")));
			ps.setDouble(12, extractDouble(doc.get("external_refer_price_min")));
			ps.setDouble(13, extractDouble(doc.get("external_refer_price_max")));
			ps.setDouble(14, extractDouble(doc.get("views")));
			ps.setDouble(15, extractDouble(doc.get("reviews")));
			ps.setDouble(16, extractDouble(doc.get("approvals")));
			ps.setInt(17, role);
			ps.setString(18, wxno);
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeStatement(ps);
		}

	}

	private static final String updatePartSql = "update tp30_accounts set "//
			+ "start_price=?,end_price=?," //
			+ "avg_num_views=?,avg_num_reviews=?,avg_like_num=?,roles=?" //
			+ " where wxnum=?";

	private void updatePart(String wxno, Document doc, int acatid, int pcatid, int platform, int role, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(updatePartSql);
			ps.setDouble(1, extractDouble(doc.get("external_refer_price_min")));
			ps.setDouble(2, extractDouble(doc.get("external_refer_price_max")));
			ps.setDouble(3, extractDouble(doc.get("views")));
			ps.setDouble(4, extractDouble(doc.get("reviews")));
			ps.setDouble(5, extractDouble(doc.get("approvals")));
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeStatement(ps);
		}
	}

	private int getCatId(String s, Map<String, Integer> catMap) {
		for (Map.Entry<String, Integer> entry : catMap.entrySet()) {
			if (s.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return catMap.get("其他") == null ? 0 : catMap.get("其他");
	}

	private Map<String, Integer> loadAreaCatMap() {
		Map<String, Integer> catMap = new LinkedHashMap<String, Integer>();
		Connection conn = null;
		try {
			conn = getConn();
			ResultSet rs = conn.createStatement().executeQuery("select name,id from tp30_area where c_type=2");
			while (rs.next()) {
				String key = rs.getString(1);
				Integer value = rs.getInt(2);
				if (key.contains("/")) {
					String[] arr = key.split("/");
					for (String s : arr) {
						catMap.put(s, value);
					}
				} else {
					catMap.put(key, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeConnection(conn);
		}
		return catMap;
	}

	private Map<String, Integer> loadProCatMap() {
		Map<String, Integer> catMap = new LinkedHashMap<String, Integer>();
		Connection conn = null;
		try {
			conn = getConn();
			ResultSet rs = conn.createStatement().executeQuery("select name,id from tp30_profession where p_type=1");
			while (rs.next()) {
				String key = rs.getString(1);
				Integer value = rs.getInt(2);
				if (key.contains("/")) {
					String[] arr = key.split("/");
					for (String s : arr) {
						catMap.put(s, value);
					}
				} else {
					catMap.put(key, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeConnection(conn);
		}
		return catMap;
	}

	private int getRole(Document doc) {
		if (StringUtils.isBlank(doc.getString("profession"))) {
			return 2;// 媒体
		} else if (StringUtils.isBlank(doc.getString("domain"))) {
			return 1;// 名人
		} else {
			return 0;
		}
	}

	private String getWXNO(String s) {
		try {
			String str = s.trim().split("\\?")[0];
			return str.substring(str.lastIndexOf("/") + 1);
		} catch (Throwable ignore) {
		}
		return null;
	}

	private Integer getPlatForm(int source) {
		switch (source) {
		case 2:
			return 2;// 美拍
		default:
			return 3;
		}
	}

	public Double extractDouble(Object obj) {
		if (obj == null) {
			return 0d;
		}
		if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		}
		if (obj instanceof String) {
			String s = (String) obj;
			if (s.contains("万")) {
				return Double.valueOf(s.replace("万", "")) * 10000;
			} else {
				return Double.valueOf(s);
			}
		}
		return 0d;
	}

	public static void main(String[] args) {
		new CleanWBYVideoService().clean();
	}

}
