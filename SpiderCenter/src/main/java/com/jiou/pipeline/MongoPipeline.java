package com.jiou.pipeline;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.OS;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@SuppressWarnings({ "unused", "unchecked" })
public class MongoPipeline implements Pipeline {

	private static final Logger logger = LoggerFactory.getLogger(MongoPipeline.class);

	private static final MongoClient mongoClient;

	public static final String insert_time = "insert_time";
	public static final String update_time = "update_time";
	public static final String key = "list";

	static {
		if (OS.isFamilyUnix()) {
			List<ServerAddress> seeds = new ArrayList<ServerAddress>();
			seeds.add(new ServerAddress("10.24.163.145", 27017));
			seeds.add(new ServerAddress("10.25.156.39", 27017));
			seeds.add(new ServerAddress("10.45.40.90", 27017));
			mongoClient = new MongoClient(seeds);
//			mongoClient = new MongoClient(new ServerAddress("60.205.59.157", 27017));
		} else {			
			mongoClient = new MongoClient(new ServerAddress("60.205.59.157", 27017));			
		}
	}
	
	protected String databaseName;
	protected String collectionName;
	protected MongoDatabase mongoDatabase;
	protected MongoCollection<Document> collection;

	public MongoPipeline(String databaseName, String collectionName) {
		super();
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.mongoDatabase = mongoClient.getDatabase(databaseName);
		this.collection = this.mongoDatabase.getCollection(collectionName);
	}

//	@Override
	public void process(ResultItems resultItems, Task task) {
		if (resultItems == null || resultItems.getAll().isEmpty()) {
			return;
		}
		insert(resultItems.getAll());
	}

	public void insert(Map<String, Object> values) {
		if (values == null || values.keySet().size() == 0) {
			return;
		}
		Document document = new Document();
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			if (key.equals(entry.getKey()) && entry.getValue() instanceof List) {
				List<Map<String, Object>> list = (List<Map<String, Object>>) entry.getValue();
				for (Map<String, Object> map : list) {
					insertOne(map);
				}
				return;
			} else {
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
		}
		this.collection.insertOne(document.append(insert_time, new Date()));
	}

	public void insertOne(Map<String, Object> values) {
		if (values == null || values.keySet().size() == 0) {
			return;
		}
		Document document = new Document();
		for (Map.Entry<String, Object> entry : values.entrySet()) {
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
		this.collection.insertOne(document.append(insert_time, new Date()));
	}

	public boolean exist(Map<String, Object> params) {
		if (params == null || params.keySet().size() == 0) {
			return true;
		}

		Document document = new Document();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			document.append(entry.getKey(), entry.getValue());
		}

		return this.collection.count(document) > 0;
	}

	public MongoCollection<Document> getMongoCollection() {
		return collection;
	}

	public static MongoClient getMongoclient() {
		return mongoClient;
	}

}
