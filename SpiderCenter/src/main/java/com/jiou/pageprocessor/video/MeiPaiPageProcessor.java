package com.jiou.pageprocessor.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.MeiPaiPipeline;

@Component("meiPaiPageProcessor")
public class MeiPaiPageProcessor implements PageProcessor {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private static final int MAXPAGE = 80;

	private static final String cat_key = "cat";
	private static final String funs_min_key = "minfunsno";
	private static final String uid_key = "uid";
	// private static final String page_key = "page";
	// private static final String videos_key = "videos";

	// private static final String seed = "http://www.meipai.com/";
	// private static final String userListFormat = //
	// "http://www.meipai.com/squares/new_timeline?page=%d&count=24&tid=%d";
	private static final String videoListFormat = //
	"http://www.meipai.com/users/user_timeline?page=%d&count=12&single_column=1&uid=%d";

	private Site site = Site.me().setTimeOut(30000).setSleepTime(1000).setUserAgent(UserAgent.CHROME);

	private static final Map<String, String> catSeedMap = new LinkedHashMap<String, String>();
	private static final Map<Integer, String> idCatdMap = new LinkedHashMap<Integer, String>();
	static {
		catSeedMap.put("热门", "http://www.meipai.com/home/hot_timeline?page=%d&count=12&maxid=503927040");
		catSeedMap.put("搞笑", "http://www.meipai.com/squares/new_timeline?page=%d&count=24&tid=13");
		catSeedMap.put("明星名人", "http://www.meipai.com/squares/new_timeline?page=%d&count=24&tid=16");
		catSeedMap.put("女神", "http://www.meipai.com/squares/new_timeline?page=%d&count=24&tid=19");
		catSeedMap.put("舞蹈",
				"http://www.meipai.com/topics/hot_timeline?page=%d&count=24&tid=5872239354896137479&maxid=503908353");
		catSeedMap.put("音乐",
				"http://www.meipai.com/topics/hot_timeline?page=%d&count=24&tid=5871155236525660080&maxid=503865226");
		catSeedMap.put("二次元", "http://www.meipai.com/squares/new_timeline?page=%d&count=24&tid=193");
		catSeedMap.put("美食",
				"http://www.meipai.com/topics/hot_timeline?page=%d&count=24&tid=5870490265939297486&maxid=503979290");
		catSeedMap.put("美装时尚", "http://www.meipai.com/squares/new_timeline?page=%d&count=24&tid=27");
		catSeedMap.put("旅行",
				"http://www.meipai.com/topics/hot_timeline?page=%d&count=24&tid=5866185182888386743&maxid=503930574");
		catSeedMap.put("男神", "http://www.meipai.com/squares/new_timeline?page=%d&count=24&tid=31");

		idCatdMap.put(13, "搞笑");
		idCatdMap.put(16, "明星名人");
		idCatdMap.put(19, "女神");
		idCatdMap.put(63, "舞蹈");
		idCatdMap.put(62, "音乐");
		idCatdMap.put(193, "二次元");
		idCatdMap.put(59, "美食");
		idCatdMap.put(27, "美妆时尚");
		idCatdMap.put(3, "旅行");
		idCatdMap.put(31, "男神");
		idCatdMap.put(5, "涨姿势");
		idCatdMap.put(18, "宝宝");
		idCatdMap.put(6, "宠物");
		idCatdMap.put(22, "赞不绝口");
		// idCatdMap.put(, "value");
	}

	public void process(Page page) {
		int depth = page.getRequest().getDepth();
		if (depth == 0) {// 处理分类用户列表
			JSONObject jsonObject = null;
			try {
				jsonObject = JSONObject.parseObject(page.getRawText());
			} catch (Exception ex) {
				logger.error(page.getRawText());
			}
			JSONArray items = jsonObject.getJSONArray("medias");
			List<Map<String, Object>> userList = new ArrayList<Map<String, Object>>();
			for (int x = 0; x < items.size(); x++) {
				JSONObject json = null;
				try {
					json = items.getJSONObject(x).getJSONObject("user");

					// 过滤id为空,无视频,粉丝少的用户
					Long uid = json.getLong("id");
					int videos = json.getIntValue("videos_count");
					Long funsno = json.getLong("followers_count");
					String userUrl = json.getString("url");
					if (uid == null || videos == 0 || funsno == null
							|| funsno < (Long) page.getRequest().getExtra(funs_min_key) || StringUtils.isBlank(userUrl)) {
						continue;
					}
					String desc = null;
					try {
						desc = Jsoup.connect(userUrl).get().select("#rightUser > p.user-descript").get(0).text().trim();
					} catch (Exception ignore) {
						continue;
					}
					// 抓取用户视频信息
					int pages = videos % 12 == 0 ? videos / 12 : videos / 12 + 1;
					for (int i = 1; i <= pages; i++) {
						String url = String.format(videoListFormat, i, uid);
						Request request = new Request(url);
						request.setDepth(1);
						// request.putExtra(cat_key,
						// page.getRequest().getExtra(cat_key));
						request.putExtra(uid_key, uid);
						page.addTargetRequest(request);
					}
					json.put("desc", desc);// 网红简介
					userList.add(json);
				} catch (Exception ignore) {
					logger.error("解析错误", ignore);
				}
			}
			page.getResultItems().put(MeiPaiPipeline.USER_LIST_KEY, userList);
		} else if (depth == 1) {// 处理视频列表
			JSONObject jsonObject = null;
			try {
				jsonObject = JSONObject.parseObject(page.getRawText());
			} catch (Exception ex) {
				logger.error(page.getRawText());
			}
			JSONArray items = jsonObject.getJSONArray("medias");
			List<Map<String, Object>> videoList = new ArrayList<Map<String, Object>>();
			for (int x = 0; x < items.size(); x++) {// title/描述/日期/视频url/收藏数/评论数
				JSONObject json = null;
				try {
					json = items.getJSONObject(x);
					long id = json.getLongValue("id");
					if (id == 0) {
						continue;
					}
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", id);
					map.put("category", json.get("category"));
					map.put(uid_key, page.getRequest().getExtra(uid_key));
					map.put("title", json.get("caption_origin"));
					map.put("desc", json.get("caption_complete"));
					map.put("created_at", json.get("created_at"));
					map.put("url", json.get("url"));
					map.put("like_num", extractIntValue(json.get("likes_count")));
					map.put("comment_num", extractIntValue(json.get("comments_count")));
					videoList.add(map);
				} catch (Exception ignore) {
					logger.error("解析错误", ignore);
					logger.error(json.toJSONString());
				}
			}
			page.getResultItems().put(MeiPaiPipeline.VIDEO_LIST_KEY, videoList);
		}
	}

	private int extractIntValue(Object obj) {
		try {
			if (obj == null) {
				return 0;
			}
			if (obj instanceof Number) {
				return ((Number) obj).intValue();
			}
			if (obj instanceof String) {
				String s = ((String) obj).replaceAll("<[^<|^>]+>", "").trim();
				if (s.contains("万")) {
					return ((Double) (Double.parseDouble(s.replace("万", "")) * 10000)).intValue();
				} else {
					return Integer.parseInt(s);
				}
			}
		} catch (Exception ignore) {
		}
		return 0;
	}

	public Site getSite() {
		return site;
	}

	public void start() {
		logger.info("美拍爬虫启动...");
		Spider spider = Spider.create(new MeiPaiPageProcessor())//
				.addPipeline(new MeiPaiPipeline())//
				.thread(25)//
				.setUUID("MeiPaiSpider");
		try {
			SpiderMonitor.instance().register(spider);
		} catch (JMException e) {
			e.printStackTrace();
		}
		for (Map.Entry<String, String> entry : catSeedMap.entrySet()) {
			for (int x = 1; x <= MAXPAGE; x++) {
				Request request = new Request(String.format(entry.getValue(), x));
				request.putExtra(cat_key, entry.getKey());
				if ("旅行".equals(entry.getKey())) {
					request.putExtra(funs_min_key, 10000L);
				} else {
					request.putExtra(funs_min_key, 100000L);
				}
				spider.addRequest(request);
			}
		}
		spider.start();
	}

	public static void main(String[] args) {
		new MeiPaiPageProcessor().start();
	}
}
