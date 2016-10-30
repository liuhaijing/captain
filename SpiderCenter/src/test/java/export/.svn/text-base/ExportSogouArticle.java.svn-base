package export;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@SuppressWarnings("deprecation")
public class ExportSogouArticle {
	public static void main(String[] args) throws Exception {
		Workbook workbook = new SXSSFWorkbook();
		OutputStream os = new FileOutputStream("d:/搜狗微信数据" + new SimpleDateFormat("yyyy-MM-dd").format(new Date())
				+ ".xlsx");
		Sheet sheet = workbook.createSheet("搜狗微信");
		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue("关键词");
		header.createCell(1).setCellValue("标题");
		header.createCell(2).setCellValue("URL");
		header.createCell(3).setCellValue("发布时间");
		header.createCell(4).setCellValue("作者");
		header.createCell(5).setCellValue("微信号");
		header.createCell(6).setCellValue("内容");
		header.createCell(7).setCellValue("阅读数");
		header.createCell(8).setCellValue("点赞数");
		header.createCell(9).setCellValue("抓取时间");

		CellStyle cellStyle = workbook.createCellStyle();
		DataFormat format = workbook.createDataFormat();
		cellStyle.setDataFormat(format.getFormat("yyyy-MM-dd HH:mm:ss"));

		DBCollection coll = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
				.getCollection("sogou_articles");
		Iterator<DBObject> it = coll.find().iterator();
		int x = 0;
		while (it.hasNext()) {
			x++;
			DBObject doc = it.next();
			Row row = sheet.createRow(x);
			row.createCell(0).setCellValue((String) doc.get("query"));
			row.createCell(1).setCellValue((String) doc.get("title"));
			row.createCell(2).setCellValue((String) doc.get("url"));
			Cell pubtimeCell = row.createCell(3);
			pubtimeCell.setCellStyle(cellStyle);
			pubtimeCell.setCellValue((Date) doc.get("pubtime"));
			row.createCell(4).setCellValue((String) doc.get("wxname"));
			row.createCell(5).setCellValue((String) doc.get("wxnum"));
			row.createCell(6).setCellValue((String) doc.get("content"));
			row.createCell(7).setCellValue((Integer) doc.get("read_num"));
			row.createCell(8).setCellValue((Integer) doc.get("like_num"));
			Cell insertTimeCell = row.createCell(9);
			insertTimeCell.setCellStyle(cellStyle);
			insertTimeCell.setCellValue((Date) doc.get("insert_time"));
		}

		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(9);

		workbook.write(os);
	}
}
