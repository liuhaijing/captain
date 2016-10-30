package com.jiou.pipeline;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.jiou.support.Consts;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@SuppressWarnings("deprecation")
public class MeiPaiPipeline implements Pipeline {

	public static final String UPDATE_TIME = "update_time";

	public static final String USER_LIST_KEY = "user_list";
	public static final String VIDEO_LIST_KEY = "video_list";

	public static final String COLLECTION_USER = "meipai_user";
	public static final String COLLECTION_VIDEO = "meipai_video";

	public static final String ID_KEY = "id";

	private DBCollection userDBColl;
	private DBCollection videoDBColl;

	public MeiPaiPipeline() {
		this.userDBColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
				.getCollection(COLLECTION_USER);
		this.videoDBColl = MongoPipeline.getMongoclient().getDB(Consts.mongo_database_name)
				.getCollection(COLLECTION_VIDEO);
	}

	@SuppressWarnings("unchecked")
//	@Override
	public void process(ResultItems resultItems, Task task) {
		if (resultItems == null || resultItems.getAll().isEmpty()) {
			return;
		}
		Object userObj = resultItems.get(USER_LIST_KEY);
		if (userObj != null && userObj instanceof List) {
			List<Map<String, Object>> users = (List<Map<String, Object>>) userObj;
			for (Map<String, Object> map : users) {
				update(map, USER_LIST_KEY);
			}
		}
		Object videoObj = resultItems.get(VIDEO_LIST_KEY);
		if (videoObj != null && videoObj instanceof List) {
			List<Map<String, Object>> videos = (List<Map<String, Object>>) videoObj;
			for (Map<String, Object> map : videos) {
				update(map, VIDEO_LIST_KEY);
			}
		}
	}

	private void update(Map<String, Object> map, String key) {
		BasicDBObject document = new BasicDBObject();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof java.math.BigDecimal) {
				DecimalFormat df = new DecimalFormat();
				Number n = null;
				try {
					n = df.parse(value.toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				value = n;
			}
			document.append(entry.getKey(), value);
		}
		document.append(UPDATE_TIME, new Date());
		if (USER_LIST_KEY == key) {
			this.userDBColl.update(new BasicDBObject(ID_KEY, map.get(ID_KEY)), document, true, false);
		} else if (VIDEO_LIST_KEY == key) {
			this.videoDBColl.update(new BasicDBObject(ID_KEY, map.get(ID_KEY)), document, true, false);
		}
	}

}
