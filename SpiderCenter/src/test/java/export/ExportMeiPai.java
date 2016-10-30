package export;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.bson.Document;

import com.jiou.pipeline.MongoPipeline;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class ExportMeiPai {
	public static void main(String[] args) throws Exception {
		MongoDatabase md = MongoPipeline.getMongoclient().getDatabase("spider");
		MongoCollection<Document> mvideoColl = md.getCollection("meipai_video");
		String mapFunction = "function(){emit(this.uid,{'comment_num':this.comment_num,'like_num':this.like_num})}";
		String reduceFunction = "function(key, values){var comment_num=0;var like_num=0;"//
				+ "values.forEach(function(val){comment_num+=val.comment_num;like_num+=val.like_num});return {'comment_num':comment_num,'like_num':like_num}}";
		MongoCursor<Document> mc = mvideoColl.mapReduce(mapFunction, reduceFunction).iterator();
		Map<Integer, Map<String, Double>> map = new HashMap<Integer, Map<String, Double>>();
		while (mc.hasNext()) {
			Document doc = mc.next();
			Object obj = doc.get("_id");
			if (obj == null) {
				continue;
			}
			int id = ((Number) obj).intValue();
			Document value = (Document) doc.get("value");
			double comment_num = getDouble(value.get("comment_num"));
			double like_num = getDouble(value.get("like_num"));
			System.out.println(id + "::" + comment_num + "::" + like_num);
			Map<String, Double> map1 = new HashMap<String, Double>();
			map1.put("comment_num", comment_num);
			map1.put("like_num", like_num);
			map.put(id, map1);
		}
		Workbook workbook = new SXSSFWorkbook();
		Sheet sheet = workbook.createSheet("网红");
		Sheet sheet2 = workbook.createSheet("视频");
		exportUser(sheet, md, map);
		exportVideo(sheet2, md);
		OutputStream os = new FileOutputStream("d:/美拍数据.xlsx");
		workbook.write(os);
	}

	private static void exportUser(Sheet sheet, MongoDatabase md, Map<Integer, Map<String, Double>> map) {
		MongoCollection<Document> muserColl = md.getCollection("meipai_user_copy");
		MongoCursor<Document> mc = muserColl.find().iterator();
		String[] headers = { "序号", "昵称", "ID", "粉丝数", "平台", "认证类型", "视频数", "平均评论数", "历史被赞数", "认证信息", "分类" };
		Row header = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			header.createCell(i).setCellValue(headers[i]);
		}
		int x = 1;
		while (mc.hasNext()) {
			Document doc = mc.next();
			int id = doc.getInteger("id", 0);
			if (id == 0) {
				continue;
			}
			String username = doc.getString("screen_name");
			// String userUrl = doc.getString("url");
			// String desc = null;
			// try {
			// desc =
			// Jsoup.connect(userUrl).get().select("#rightUser > p.user-descript").get(0).text().trim();
			// } catch (Exception ignore) {
			// continue;
			// }
			// String desc = doc.getString("desc");
			int videos_count = doc.getInteger("videos_count", 0);
			int followers_count = doc.getInteger("followers_count", 0);
			Row row = sheet.createRow(x);
			row.createCell(0).setCellValue(x);
			row.createCell(1).setCellValue(username);
			row.createCell(2).setCellValue(id);
			row.createCell(3).setCellValue(followers_count);
			row.createCell(4).setCellValue("美拍");
			row.createCell(5).setCellValue(doc.getBoolean("verified", false) ? "是" : "否");
			row.createCell(6).setCellValue(videos_count);
			Map<String, Double> map2 = map.get(id);
			if (map2 == null) {
				System.out.println("uid==" + id + "统计为空");
				continue;
			}
			Number comment_total = map2.get("comment_num");
			Number liked_total = map2.get("like_num");
			if (comment_total != null) {
				row.createCell(7).setCellValue(scale(comment_total.doubleValue() / videos_count, 2));
			} else {
				row.createCell(7).setCellValue(0);
			}
			if (liked_total != null) {
				row.createCell(8).setCellValue(liked_total.doubleValue());
			} else {
				row.createCell(8).setCellValue(0);
			}
			String verified_reason = getVerifiedReason(doc);
			if (StringUtils.isNotBlank(verified_reason)) {
				row.createCell(9).setCellValue(verified_reason);
				row.createCell(10).setCellValue(verified_reason);
			}
			x++;
		}
	}

	private static void exportVideo(Sheet sheet, MongoDatabase md) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		MongoCollection<Document> mvideoColl = md.getCollection("meipai_video");
		String[] headers = { "视频简介", "链接", "用户ID", "数据采集时间" };
		Row header = sheet.createRow(0);
		for (int i = 0; i < headers.length; i++) {
			header.createCell(i).setCellValue(headers[i]);
		}
		MongoCursor<Document> mc = mvideoColl.find().iterator();
		int x = 1;
		while (mc.hasNext()) {
			Document doc = mc.next();
			Object obj = doc.get("uid");
			if (obj == null) {
				continue;
			}
			int uid = ((Number) obj).intValue();
			String title = doc.getString("title");
			String url = doc.getString("url");
			Date date = doc.getDate("update_time");
			Row row = sheet.createRow(x);
			row.createCell(0).setCellValue(title);
			row.createCell(1).setCellValue(url);
			row.createCell(2).setCellValue(uid);
			row.createCell(3).setCellValue(df.format(date));
			x++;
		}
	}

	private static double getDouble(Object obj) {
		double d = 0d;
		if (obj == null) {
			return d;
		}
		if (obj instanceof Number) {
			d = ((Number) obj).doubleValue();
		}
		return d;
	}

	private static double scale(double d, int scale) {
		BigDecimal b = new BigDecimal(d);
		return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	private static String getVerifiedReason(Document doc) {
		try {
			String s = ((Document) ((Document) doc.get("external_platforms")).get("weibo"))
					.getString("verified_reason");
			return s;
		} catch (Exception ignore) {
		}
		return null;
	}

}
