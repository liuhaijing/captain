package sogou;

import org.apache.commons.lang3.time.DateUtils;

import com.jiou.pipeline.MongoPipeline;
import com.jiou.support.Consts;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@SuppressWarnings("deprecation")
public class SogouFreqTest {
	public static void main(String[] args) throws Exception {
		DBCollection coll = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
				.getCollection("sogou_freq_task");
		DBObject doc = new BasicDBObject();
		doc.put("query", "生命中最暖心暖肺的事,幸福一直在我们身边!");
		doc.put("start_time", DateUtils.parseDate("2016-06-01 16:30:00", "yyyy-MM-dd HH:mm:ss"));
		doc.put("end_time", DateUtils.parseDate("2016-06-01 18:00:00", "yyyy-MM-dd HH:mm:ss"));
		doc.put("last_exec_time", DateUtils.parseDate("2016-06-01 11:30:00", "yyyy-MM-dd HH:mm:ss"));
		doc.put("interval", 300);// in seconds
		doc.put("enable", true);// in seconds
		coll.insert(doc);
	}
}
