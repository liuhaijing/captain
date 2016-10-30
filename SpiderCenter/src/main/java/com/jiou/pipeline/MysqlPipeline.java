package com.jiou.pipeline;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * @author zhe.li
 */
public class MysqlPipeline implements Pipeline {

	private static final String insertFormat = "insert into %s(%s) values(%s)";
	private static final String countFormat = "select count(*) from %s where %s";

	private Connection conn;
	private String tableName;

	public MysqlPipeline(String jdbcUrl, String user, String password,
			String tableName) {
		try {
			conn = DriverManager.getConnection(jdbcUrl, user, password);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		if (tableName == null) {
			throw new RuntimeException("表名不能为空");
		}
		this.tableName = tableName;
	}

	@SuppressWarnings("unchecked")
//	@Override
	public void process(ResultItems resultItems, Task task) {
		Map<String, Object> map = resultItems.getAll();

		if (map.keySet().size() == 0) {
			return;
		}

		Object obj = map.get("list");
		if (obj != null) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
			for (Map<String, Object> map2 : list) {
				insert(map2);
			}
		} else {
			insert(map);
		}
	}

	public void insert(Map<String, Object> map) {

		if (map == null || map.keySet().size() == 0) {
			return;
		}

		StringBuilder fieldBuilder = new StringBuilder();
		StringBuilder questBuilder = new StringBuilder();
		for (String key : map.keySet()) {
			fieldBuilder.append(key).append(",");
			questBuilder.append("?").append(",");
		}
		String paramStr = fieldBuilder.toString().substring(0,
				fieldBuilder.toString().length() - 1);
		String questStr = questBuilder.toString().substring(0,
				questBuilder.toString().length() - 1);

		String sql = String.format(insertFormat, tableName, paramStr, questStr);
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			int x = 0;
			for (String key : map.keySet()) {
				x++;
				ps.setObject(x, map.get(key));
			}
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeStatement(ps);
		}

	}

	public boolean exist(Map<String, Object> params) {

		Set<String> keySet = params.keySet();
		if (keySet.size() == 0) {
			return false;
		}

		StringBuilder sb = new StringBuilder();
		// for (String key : params.keySet()) {
		// sb.append(params.get(key)).append("*");
		// }
		// if (BloomFilterCache.me().contains(sb)) {
		// return true;
		// }
		// sb.delete(0, sb.length());

		int paramIndex = 0;
		for (String key : keySet) {
			paramIndex++;
			if (paramIndex == 1) {
				sb.append(key).append("=?");
			} else {
				sb.append(" and ").append(key).append("=? ");
			}
		}

		String sql = String.format(countFormat, tableName, sb.toString());
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			int x = 0;
			for (String key : keySet) {
				x++;
				ps.setObject(x, params.get(key));
			}
			ResultSet rs = ps.executeQuery();
			rs.next();
			return rs.getInt(1) > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeStatement(ps);
		}

		return false;
	}

	private void closeStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
