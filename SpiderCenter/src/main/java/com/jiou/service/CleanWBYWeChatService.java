package com.jiou.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collections;
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

/**
 * 微播易微信公众号数据清洗
 * 
 * @author zhe.li
 */
@Component("cleanWBYWeChatService")
@Scope("singleton")
public class CleanWBYWeChatService extends CleanService {

	/**
	 * 微信公众号清洗
	 */
	public void cleanWX() {
		long startTime = System.currentTimeMillis();
		Map<String, Integer> wxCatMap = loadWXCatMap();
		Connection conn = null;
		try {
			conn = getConn();
			MongoClient mongoClient = MongoPipeline.getMongoclient();
			MongoCollection<Document> collection = mongoClient.getDatabase(Consts.mongo_database_name).getCollection(
					WeiBoYiService.mongo_collection_name_wx);
			Date start = DateUtils.truncate(new Date(), Calendar.DATE);
			Date end = DateUtils.addDays(start, 1);
			FindIterable<Document> fi = collection.find(new BasicDBObject().append("insert_time", new BasicDBObject(
					QueryOperators.GTE, start).append(QueryOperators.LT, end)));
			MongoCursor<Document> mc = fi.iterator();
			String wxno = null;
			while (mc.hasNext()) {
				try {
					Document doc = mc.next();
					wxno = doc.getString("wxno");
					if (StringUtils.isBlank(wxno)) {
						continue;
					}
					int catid = getWXCatId(doc.getString("domain"), wxCatMap);
					Integer uid = getUid(wxno, conn);
					if (uid == null) {// 表中不存在该微信公众号
						insertWX(doc, catid, conn);
					} else if (uid == 266) {// 可以更新全部字段
						updateWXAll(doc, catid, conn);
					} else {// 只能更新部分字段
						updateWXPart(doc, catid, conn);
					}
				} catch (Exception ex) {
					logger.error("清洗{}错误", wxno);
					logger.error("清洗错误", ex);
				}
			}
		} finally {
			JDBCUtils.closeConnection(conn);
			logger.info("微博易清洗微信公众号耗时:{}ms", System.currentTimeMillis() - startTime);
		}
	}

	private static final String insertWXSql = //
	"insert into tp30_accounts(wxname,wxnum,is_ren,is_original,jump_url,"//
			+ "friends_describe,c_notice,area,roles,fans_num,"//
			+ "y_price,y_one_price,y_two_price,y_three_price,r_price,"//
			+ "r_one_price,r_two_price,r_three_price,read_num,w_upate,"//
			+ "w_degree,exponent,uid,acc_type,one_read_num,two_read_num,"//
			+ "three_read_num,four_read_num) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	// 插入微博易微信公众号
	private void insertWX(Document doc, int catid, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(insertWXSql);
			ps.setString(1, doc.getString("wxname"));
			ps.setString(2, doc.getString("wxno"));
			ps.setInt(3, getWXAuth(doc.getInteger("is_auth", 0)));
			ps.setInt(4, getWXOri(doc.getInteger("can_ori", 0)));
			ps.setString(5, doc.getString("home_url"));
			ps.setString(6, doc.getString("desc"));
			ps.setString(7, doc.getString("coop_noti"));
			ps.setInt(8, catid);
			ps.setInt(9, doc.getInteger("role", 0));
			ps.setInt(10, getFunsCount(doc.get("funsno")));
			ps.setDouble(11, doc.getInteger("single_hard", 0));
			ps.setDouble(12, doc.getInteger("first_hard", 0));
			ps.setDouble(13, doc.getInteger("second_hard", 0));
			ps.setDouble(14, doc.getInteger("third_hard", 0));
			ps.setDouble(15, doc.getInteger("single_soft", 0));
			ps.setDouble(16, doc.getInteger("first_soft", 0));
			ps.setDouble(17, doc.getInteger("second_soft", 0));
			ps.setDouble(18, doc.getInteger("third_soft", 0));
			ps.setInt(
					19,
					doc.getInteger("sin_read_num", 0) + doc.getInteger("fst_read_num", 0)
							+ doc.getInteger("sec_read_num", 0) + doc.getInteger("thd_read_num", 0));
			ps.setInt(20, doc.getInteger("week_update", 0));
			ps.setInt(21, getWXDegree(doc.getString("coop_degree")));
			ps.setString(22, doc.getString("snbt"));
			ps.setInt(23, 266);
			ps.setInt(24, 2);
			ps.setInt(25, doc.getInteger("sin_read_num", 0));
			ps.setInt(26, doc.getInteger("fst_read_num", 0));
			ps.setInt(27, doc.getInteger("sec_read_num", 0));
			ps.setInt(28, doc.getInteger("thd_read_num", 0));
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeStatement(ps);
		}
	}

	private static final String updateWXAllSql = //
	"update tp30_accounts set wxname=?,wxnum=?,is_ren=?,is_original=?,"
			+ "jump_url=?,friends_describe=?,c_notice=?,"
			+ "area=?,roles=?,fans_num=?,"//
			+ "y_price=?,y_one_price=?,y_two_price=?,y_three_price=?,"
			+ "r_price=?,r_one_price=?,r_two_price=?,r_three_price=?," //
			+ "read_num=?,w_upate=?,w_degree=?,exponent=?,"//
			+ "one_read_num=?,two_read_num=?,three_read_num=?,four_read_num=?"//
			+ " where wxnum=?";

	// 更新微博易微信公众号全部
	private void updateWXAll(Document doc, int catid, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(updateWXAllSql);
			ps.setString(1, doc.getString("wxname"));
			ps.setString(2, doc.getString("wxno"));
			ps.setInt(3, getWXAuth(doc.getInteger("is_auth", 0)));
			ps.setInt(4, getWXOri(doc.getInteger("can_ori", 0)));
			ps.setString(5, doc.getString("home_url"));
			ps.setString(6, doc.getString("desc"));
			ps.setString(7, doc.getString("coop_noti"));
			ps.setInt(8, catid);
			ps.setInt(9, doc.getInteger("role", 0));
			ps.setInt(10, getFunsCount(doc.get("funsno")));
			ps.setDouble(11, doc.getInteger("single_hard", 0));
			ps.setDouble(12, doc.getInteger("first_hard", 0));
			ps.setDouble(13, doc.getInteger("second_hard", 0));
			ps.setDouble(14, doc.getInteger("third_hard", 0));
			ps.setDouble(15, doc.getInteger("single_soft", 0));
			ps.setDouble(16, doc.getInteger("first_soft", 0));
			ps.setDouble(17, doc.getInteger("second_soft", 0));
			ps.setDouble(18, doc.getInteger("third_soft", 0));
			ps.setInt(
					19,
					doc.getInteger("sin_read_num", 0) + doc.getInteger("fst_read_num", 0)
							+ doc.getInteger("sec_read_num", 0) + doc.getInteger("thd_read_num", 0));
			ps.setInt(20, doc.getInteger("week_update", 0));
			ps.setInt(21, getWXDegree(doc.getString("coop_degree")));
			ps.setString(22, doc.getString("snbt"));
			ps.setInt(23, doc.getInteger("sin_read_num", 0));
			ps.setInt(24, doc.getInteger("fst_read_num", 0));
			ps.setInt(25, doc.getInteger("sec_read_num", 0));
			ps.setInt(26, doc.getInteger("thd_read_num", 0));
			ps.setString(27, doc.getString("wxno"));
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeStatement(ps);
		}
	}

	private static final String updateWXPartSql = //
	"update tp30_accounts set "//
			+ "is_original=?,c_notice=?,roles=?,w_upate=?,"//
			+ "w_degree=?,exponent=?,jump_url=? "//
			+ "where wxnum=?";

	// 更新微博易微信公众号部分
	private void updateWXPart(Document doc, int catid, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(updateWXPartSql);
			ps.setInt(1, getWXOri(doc.getInteger("can_ori", 0)));
			ps.setString(2, doc.getString("coop_noti"));
			ps.setInt(3, doc.getInteger("role", 0));
			ps.setInt(4, doc.getInteger("week_update", 0));
			ps.setInt(5, getWXDegree(doc.getString("coop_degree")));
			ps.setString(6, doc.getString("snbt"));
			ps.setString(7, doc.getString("home_url"));
			ps.setString(8, doc.getString("wxno"));
			ps.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeStatement(ps);
		}
	}

	private Map<String, Integer> loadWXCatMap() {
		Map<String, Integer> wxCatMap = new HashMap<String, Integer>();
		Connection conn = null;
		try {
			conn = getConn();
			ResultSet rs = conn.createStatement().executeQuery("select name,id from tp30_area where c_type=0");
			while (rs.next()) {
				String key = rs.getString(1);
				Integer value = rs.getInt(2);
				wxCatMap.put(key, value);
			}
			Integer wxMax = Collections.max(wxCatMap.values()) + 1;
			wxCatMap.put("_MAX_", wxMax);
			wxCatMap.put("影视", 27);
			wxCatMap.put("文艺", 33);
			wxCatMap.put("综合媒体", 1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.closeConnection(conn);
		}
		return wxCatMap;
	}

	// 获取微信分类id
	private int getWXCatId(String s, Map<String, Integer> wxCatMap) {
		if (StringUtils.isBlank(s)) {
			return wxCatMap.get("_MAX_");
		}
		for (Map.Entry<String, Integer> entry : wxCatMap.entrySet()) {
			if (s.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return wxCatMap.get("_MAX_");
	}

	private int getWXAuth(int is_auth) {
		switch (is_auth) {
		case 1:
			return 2;
		default:
			return 1;
		}
	}

	private int getWXOri(int is_ori) {
		switch (is_ori) {
		case 1:
			return 2;
		default:
			return 1;
		}
	}

	public static void main(String[] args) {
		CleanWBYWeChatService cleanWBYDataService = new CleanWBYWeChatService();
		cleanWBYDataService.cleanWX();
	}

}
