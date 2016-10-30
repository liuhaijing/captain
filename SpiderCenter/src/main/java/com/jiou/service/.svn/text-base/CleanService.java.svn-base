package com.jiou.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;
import com.jiou.support.JDBCUtils;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public abstract class CleanService {
	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected String url = //
	"jdbc:mysql://182.92.193.117/jm?characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull";
	protected String user = "jm";
	protected String password = "jm@wxh#123";

	protected Connection getConn() {
		return JDBCUtils.getConnection(url, user, password);
	}

	// 获取粉丝数量
	protected int getFunsCount(Object obj) {
		if (obj == null) {
			return 0;
		}
		if (obj instanceof String) {
			String s = (String) obj;
			if (s.contains("万")) {
				Double d = Double.valueOf(s.replace("万", "").trim().replace(",", "")) * 10000;
				return d.intValue();
			} else {
				return Integer.valueOf(s.trim().replace(",", ""));
			}
		} else if (obj instanceof Integer) {
			return Integer.parseInt(String.valueOf( obj ));
		} else {
			return 0;
		}
	}

	protected Integer getUid(String wxno, Connection conn) {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("select uid from tp30_accounts where wxnum=?");
			ps.setString(1, wxno);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			JDBCUtils.closeStatement(ps);
		}
	}

	protected int getWXDegree(String s) {
		if ("低".equals(s)) {
			return 1;
		} else if ("中".equals(s)) {
			return 2;
		} else if ("高".equals(s)) {
			return 3;
		} else {
			return 0;
		}
	}

	protected Double extractDouble(Object obj) {
		if (obj == null) {
			return null;
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
		return null;
	}

	// 获取分类字段
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		MongoClient mongoClient = MongoPipeline.getMongoclient();
		DBCollection dbCollection = mongoClient.getDB(Consts.mongo_database_name).getCollection(
				WeiBoYiService.mongo_collection_name_pai);
		@SuppressWarnings("unchecked")
		List<String> list = dbCollection.distinct("domain");
		for (String s : list) {
			System.out.println(s);
		}
	}

}
