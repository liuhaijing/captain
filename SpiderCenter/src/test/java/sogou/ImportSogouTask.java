package sogou;

import java.io.File;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;

import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;
import com.mongodb.client.MongoCollection;

public class ImportSogouTask {
	public static void main(String[] args) throws Exception {

		Workbook workbook = new XSSFWorkbook(new File("d:/wx.xlsx"));
		Sheet sheet = workbook.getSheet("极盟微信资源库");
		MongoCollection<Document> coll = MongoPipeline.getMongoclient().getDatabase(Consts.mongo_database_name)
				.getCollection("wx_list");
		int max = sheet.getLastRowNum();
		for (int x = 1; x <= max; x++) {
			try {
				Row row = sheet.getRow(x);
				String wxname = row.getCell(0).getStringCellValue().trim();
				String wxnum = row.getCell(1).getStringCellValue().trim();
				Document doc = new Document();
				doc.put("wxnum", wxnum);
				doc.put("uid", 266);
				doc.put("wxname", wxname);
				doc.put("idx", 0);
				coll.insertOne(doc);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
