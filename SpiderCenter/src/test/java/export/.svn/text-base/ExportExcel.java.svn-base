package export;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.bson.Document;

import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

public class ExportExcel {

	public static void main(String[] args) throws Exception {
		Workbook workbook = new SXSSFWorkbook();
		OutputStream os = new FileOutputStream("d:/微播易城外圈数据" + new SimpleDateFormat("yyyy-MM-dd").format(new Date())
				+ ".xlsx");
		Date start = DateUtils.truncate(new Date(), Calendar.DATE);
		Date end = DateUtils.addDays(start, 1);
		exportWBY(workbook, os, start, end);
		exportCWQ(workbook, os, start, end);
		workbook.write(os);
	}

	public static void exportCWQ(Workbook workbook, OutputStream os, Date start, Date end) throws Exception {

		Sheet sheet = workbook.createSheet("城外圈");

		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue("微信昵称");
		header.createCell(1).setCellValue("微信号");
		header.createCell(2).setCellValue("粉丝数");
		header.createCell(3).setCellValue("多图头条报价");
		header.createCell(4).setCellValue("多图次条报价");
		header.createCell(5).setCellValue("多图3-n条报价");
		header.createCell(6).setCellValue("阅读量");
		header.createCell(7).setCellValue("日期");

		CellStyle cellStyle = workbook.createCellStyle();
		DataFormat format = workbook.createDataFormat();
		cellStyle.setDataFormat(format.getFormat("yyyy年MM月dd日"));

		MongoCollection<Document> collection = new MongoPipeline(Consts.mongo_database_name, "chengwaiquan")
				.getMongoCollection();
		FindIterable<Document> fit = collection.find(new BasicDBObject().append("insert_time", new BasicDBObject(
				QueryOperators.GTE, start).append(QueryOperators.LT, end)));
		MongoCursor<Document> mc = fit.iterator();
		int x = 0;
		while (mc.hasNext()) {
			Document doc = mc.next();
			String weichatno = doc.getString("weichatno");
			weichatno = weichatno == null ? null : weichatno;
			if (StringUtils.isBlank(weichatno)) {
				continue;
			}
			x++;
			String nickname = doc.getString("nickname");
			nickname = nickname == null ? "" : nickname;
			String funsnum = doc.getString("funsnum");
			funsnum = funsnum == null ? "" : funsnum;
			int first_price = doc.getInteger("first_price", 0);
			int second_price = doc.getInteger("second_price", 0);
			int third_price = doc.getInteger("third_price", 0);
			int read_num = doc.getInteger("read_num", 0);
			Date insert_time = doc.getDate("insert_time");

			Row row = sheet.createRow(x);
			row.createCell(0).setCellValue(nickname);
			row.createCell(1).setCellValue(weichatno);
			row.createCell(2).setCellValue(funsnum);
			row.createCell(3).setCellValue(first_price);
			row.createCell(4).setCellValue(second_price);
			row.createCell(5).setCellValue(third_price);
			row.createCell(6).setCellValue(read_num);
			Cell cell = row.createCell(7);
			cell.setCellValue(insert_time);
			cell.setCellStyle(cellStyle);
		}

		// for (int i = 0; i <= 7; i++) {
		sheet.autoSizeColumn(7);
		sheet.autoSizeColumn(7, true);
		// }

	}

	public static void exportWBY(Workbook workbook, OutputStream os, Date start, Date end) throws Exception {

		Sheet sheet = workbook.createSheet("微播易");

		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue("微信昵称");
		header.createCell(1).setCellValue("微信号");
		header.createCell(2).setCellValue("粉丝数");
		header.createCell(3).setCellValue("单图文最低报价");
		header.createCell(4).setCellValue("单图文最高报价");
		header.createCell(5).setCellValue("多图文第一条最低报价");
		header.createCell(6).setCellValue("多图文第一条最高报价");
		header.createCell(7).setCellValue("多图文第二条最低报价");
		header.createCell(8).setCellValue("多图文第二条最高报价");
		header.createCell(9).setCellValue("多图文第3-n条最低报价");
		header.createCell(10).setCellValue("多图文第3-n条最高报价");
		header.createCell(11).setCellValue("单图文硬广报价");
		header.createCell(12).setCellValue("单图文软广报价");
		header.createCell(13).setCellValue("多图文第一条硬广报价");
		header.createCell(14).setCellValue("多图文第一条软广报价");
		header.createCell(15).setCellValue("多图文第二条硬广报价");
		header.createCell(16).setCellValue("多图文第二条软广报价");
		header.createCell(17).setCellValue("多图文第3-n条硬广报价");
		header.createCell(18).setCellValue("多图文第3-n条软广报价");
		header.createCell(19).setCellValue("单图文阅读量");
		header.createCell(20).setCellValue("多图文第一条阅读量");
		header.createCell(21).setCellValue("多图文第二条阅读量");
		header.createCell(22).setCellValue("多图文第3-n条阅读量");
		header.createCell(23).setCellValue("日期");

		CellStyle cellStyle = workbook.createCellStyle();
		DataFormat format = workbook.createDataFormat();
		cellStyle.setDataFormat(format.getFormat("yyyy年MM月dd日"));

		MongoCollection<Document> collection = new MongoPipeline(Consts.mongo_database_name, "weiboyi")
				.getMongoCollection();
		FindIterable<Document> fit = collection.find(new BasicDBObject().append("insert_time", new BasicDBObject(
				QueryOperators.GTE, start).append(QueryOperators.LT, end)));
		MongoCursor<Document> mc = fit.iterator();
		int x = 0;
		while (mc.hasNext()) {
			Document doc = mc.next();
			String weichatno = doc.getString("weichatno");
			weichatno = weichatno == null ? null : weichatno;
			if (StringUtils.isBlank(weichatno)) {
				continue;
			}
			x++;
			String nickname = doc.getString("nickname");
			nickname = nickname == null ? "" : nickname;
			String funsnum = doc.getString("funsnum");
			funsnum = funsnum == null ? "" : funsnum;
			String single_price_min = doc.getString("single_price_min");
			single_price_min = single_price_min == null ? "" : single_price_min;
			String single_price_max = doc.getString("single_price_max");
			single_price_max = single_price_max == null ? "" : single_price_max;
			String first_price_min = doc.getString("first_price_min");
			first_price_min = first_price_min == null ? "" : first_price_min;
			String first_price_max = doc.getString("first_price_max");
			first_price_max = first_price_max == null ? "" : first_price_max;
			String second_price_min = doc.getString("second_price_min");
			second_price_min = second_price_min == null ? "" : second_price_min;
			String second_price_max = doc.getString("second_price_max");
			second_price_max = second_price_max == null ? "" : second_price_max;
			String third_price_min = doc.getString("third_price_min");
			third_price_min = third_price_min == null ? "" : third_price_min;
			String third_price_max = doc.getString("third_price_max");
			third_price_max = third_price_max == null ? "" : third_price_max;
			int single_price_hard = doc.getInteger("single_price_hard", 0);
			int single_price_soft = doc.getInteger("single_price_soft", 0);
			int first_price_hard = doc.getInteger("first_price_hard", 0);
			int first_price_soft = doc.getInteger("first_price_soft", 0);
			int second_price_hard = doc.getInteger("second_price_hard", 0);
			int second_price_soft = doc.getInteger("second_price_soft", 0);
			int third_price_hard = doc.getInteger("third_price_hard", 0);
			int third_price_soft = doc.getInteger("third_price_soft", 0);
			int single_read_num = doc.getInteger("single_read_num", 0);
			int first_read_num = doc.getInteger("first_read_num", 0);
			int second_read_num = doc.getInteger("second_read_num", 0);
			int third_read_num = doc.getInteger("third_read_num", 0);
			Date insert_time = doc.getDate("insert_time");

			Row row = sheet.createRow(x);
			row.createCell(0).setCellValue(nickname);
			row.createCell(1).setCellValue(weichatno);
			row.createCell(2).setCellValue(funsnum);
			row.createCell(3).setCellValue(single_price_min);
			row.createCell(4).setCellValue(single_price_max);
			row.createCell(5).setCellValue(first_price_min);
			row.createCell(6).setCellValue(first_price_max);
			row.createCell(7).setCellValue(second_price_min);
			row.createCell(8).setCellValue(second_price_max);
			row.createCell(9).setCellValue(third_price_min);
			row.createCell(10).setCellValue(third_price_max);
			row.createCell(11).setCellValue(single_price_hard);
			row.createCell(12).setCellValue(single_price_soft);
			row.createCell(13).setCellValue(first_price_hard);
			row.createCell(14).setCellValue(first_price_soft);
			row.createCell(15).setCellValue(second_price_hard);
			row.createCell(16).setCellValue(second_price_soft);
			row.createCell(17).setCellValue(third_price_hard);
			row.createCell(18).setCellValue(third_price_soft);
			row.createCell(19).setCellValue(single_read_num);
			row.createCell(20).setCellValue(first_read_num);
			row.createCell(21).setCellValue(second_read_num);
			row.createCell(22).setCellValue(third_read_num);
			Cell cell = row.createCell(23);
			cell.setCellValue(insert_time);
			cell.setCellStyle(cellStyle);
		}

		// for (int i = 0; i < 24; i++) {
		sheet.autoSizeColumn(23);
		sheet.autoSizeColumn(23, true);
		// }

	}
}
