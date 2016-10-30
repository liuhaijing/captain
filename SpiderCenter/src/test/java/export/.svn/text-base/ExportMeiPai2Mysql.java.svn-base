package export;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;

import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.JDBCUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class ExportMeiPai2Mysql {

	protected static String url = //
	"jdbc:mysql://182.92.193.117/jm?characterEncoding=UTF8&zeroDateTimeBehavior=convertToNull";
	protected static String user = "jm";
	protected static String password = "jm@wxh#123";

	protected static Connection getConn() {
		return JDBCUtils.getConnection(url, user, password);
	}

	public static void main(String[] args) throws Exception {
		// importFromExcel();
		updateFromMongo();
	}

	public static void updateFromMongo() throws Exception {
		MongoDatabase md = MongoPipeline.getMongoclient().getDatabase("spider");
		MongoCollection<Document> muserColl = md.getCollection("meipai_user_copy");
		Connection conn = getConn();
		PreparedStatement ps = conn.prepareStatement("select user_id from tp30_red_network");
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			try {
				int uid = rs.getInt(1);
				MongoCursor<Document> mc = muserColl.find(new BasicDBObject("id", uid)).iterator();
				if (mc.hasNext()) {
					Document doc = mc.next();
					String desc = doc.getString("description");
					String img = doc.getString("avatar");
					String jump_url = doc.getString("url");
					int gender = getgender(doc.getString("gender"));
					boolean is_ren = doc.getBoolean("verified", false);
					PreparedStatement ps2 = conn
							.prepareStatement("update tp30_red_network set img=?,jump_url=?,sex=?,is_ren=?,description=? where user_id=?");
					// PreparedStatement ps2 = conn
					// .prepareStatement("update tp30_red_network set img=?,jump_url=?,sex=?,is_ren=? where user_id=?");
					ps2.setString(1, img);
					ps2.setString(2, jump_url);
					ps2.setInt(3, gender);
					ps2.setBoolean(4, is_ren);
					ps2.setInt(5, uid);
					ps2.setString(6, desc);
					ps2.execute();
					JDBCUtils.closeStatement(ps2);
				}
			} catch (Exception t) {
				t.printStackTrace();
			}
		}
	}

	public static void importFromExcel() throws Exception {
		Workbook workbook = new XSSFWorkbook(new File("D:/美拍数据2.xlsx"));
		Sheet sheet = workbook.getSheetAt(0);
		int max = sheet.getLastRowNum();
		Connection conn = getConn();
		for (int x = 1; x <= max; x++) {
			PreparedStatement ps = conn
					.prepareStatement("insert into tp30_red_network(name,user_id,fans_num,platform_type,comment_num,like_num) values(?,?,?,?,?,?)");
			try {
				Row row = sheet.getRow(x);
				String nickname = row.getCell(1).getStringCellValue();
				int id = ((Double) row.getCell(2).getNumericCellValue()).intValue();
				int funsno = ((Double) row.getCell(3).getNumericCellValue()).intValue();
				int platform = 1;
				double commont_num = row.getCell(9).getNumericCellValue();
				int like_num = ((Double) row.getCell(10).getNumericCellValue()).intValue();
				ps.setString(1, nickname);
				ps.setInt(2, id);
				ps.setInt(3, funsno);
				ps.setInt(4, platform);
				ps.setDouble(5, commont_num);
				ps.setInt(6, like_num);
				ps.execute();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				JDBCUtils.closeStatement(ps);
			}
		}
	}

	private static int getgender(String s) {
		if ("m".equalsIgnoreCase(s)) {
			return 1;// 男
		} else if ("f".equalsIgnoreCase(s)) {
			return 2;// 女
		}
		return 3;
	}

}
