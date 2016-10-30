package com.jiou.pageprocessor.wx;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ibm.icu.text.SimpleDateFormat;
import com.jiou.httpclients.UserAgent;
import com.jiou.pipeline.SogouPipeline;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public class SogouQueryArticlePageProcessor implements PageProcessor {

	public static final String filterName = "sogou_Query_Article_filter";

	public static final String format = //
	"http://weixin.sogou.com/weixin?query=%s&_sug_type_=&_sug_=y&type=2&page=%d&ie=utf8";
	
	public static final String format2 = //
	"http://mp.weixin.qq.com";
	
	protected static final String commentUrl = //
	"http://mp.weixin.qq.com/mp/getcomment";
	
	protected static final String charset = "UTF-8";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(30000).setUserAgent(UserAgent.IE);
	
	public void process(Page page) {
		int depth = page.getRequest().getDepth();
		if (depth == 0) {
			page.setSkip(true);
			parseWxnum(page);
		}  else if (depth == 1) {
			parseWxlist(page);
		}else if (depth == 2) {
			parseWxRead(page);
		}   
	}

	
	/**
	 * 解析得到相应的微信公众号的url、wxnum、wxname
	 * @param page
	 */
	private void parseWxnum(Page page)  {
		 
			String wx_num = (String) page.getRequest().getExtra(SogouPipeline.wxnum);
			String wx_name = (String) page.getRequest().getExtra(SogouPipeline.wxname);
			
			Document doc = Jsoup.parse(page.getRawText());
			
			Elements items = doc.select("div.results.mt7 > div.wx-rb.bg-blue.wx-rb_v1._item[id]");
			for (Element e : items) {
				
				String page_wxnum = e.select("div.txt-box > h4 > span > label").text().trim();
				if (!wx_num.trim().equals(page_wxnum.trim())) {
					continue;
				}
				String page_wxname= e.select("div.txt-box > h3").text().trim();
				if (page_wxname!=null&&!"".equals(page_wxname)) {
					wx_name=page_wxname;
				}
				String url = e.attr("href").trim();
				
				Request request = new Request(url);
	 
				request.putExtra(SogouPipeline.wxnum, wx_num);
				request.putExtra(SogouPipeline.wxname, wx_name);
				request.setDepth(1);
				page.addTargetRequest(request);
				 break;
			
			}
			
	}
	
	
	/**
	 * 进入对应的公众号主页爬取对应公众号下三天内发布的文章的wxnum、wxname、title、pubtime、和点赞数阅读数请求的url
	 * @param page
	 */
	protected void parseWxlist(Page page) {
		
			 
			String wx_num = (String) page.getRequest().getExtra(SogouPipeline.wxnum);
			String wx_name = (String) page.getRequest().getExtra(SogouPipeline.wxname);
			
			Document doc = Jsoup.parse(page.getRawText());
			
			String html = doc.toString();
			
			
				Matcher m = Pattern.compile("var msgList = '\\{.+\\}").matcher(html);
				if (m.find()) {
					String s = m.group().replace("var msgList = '", "").trim().replace("&quot;", "\"")
							.replace("&amp;", "&");
					JSONArray items = JSONObject.parseObject(s).getJSONArray("list");
					
					for (int x = 0; x < items.size(); x++) {
							
						JSONObject json = items.getJSONObject(x);
						Date pubtime = null;
						try {
						  pubtime = new Date(json.getJSONObject("comm_msg_info").getLongValue("datetime") * 1000);
						
						int day = daysBetween(pubtime,new Date());
						if (day>3) {
							return;
						}
						String urlEnd =	 json.getJSONObject("app_msg_ext_info").getString("content_url").trim()
												.replace("\\", "").replace("&amp;", "&") ;
						String url = commentUrl+urlEnd.substring(urlEnd.indexOf("?"));
										System.out.println(url);
						String title = json.getJSONObject("app_msg_ext_info").getString("title").trim();
						String Contenturl = format2+urlEnd;
										Request request = new Request(url);
										request.putExtra(SogouPipeline.wxnum, wx_num);
										request.putExtra(SogouPipeline.wxname, wx_name);
										request.putExtra(SogouPipeline.title, title);
										request.putExtra(SogouPipeline.pubtime, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pubtime));
										request.putExtra(SogouPipeline.url, Contenturl);
										request.setDepth(2);
										request.setLast(true);
										page.addTargetRequest(request);	
						} catch (Exception e) {
							logger.error("解析文章列表错误 app_msg_ext_info ", e);
						}
										
						JSONArray  subitem = json.getJSONObject("app_msg_ext_info").getJSONArray("multi_app_msg_item_list");
						if (subitem != null) {
							for (int y = 0; y < subitem.size(); y++) {
								try {
									
									JSONObject subjson = subitem.getJSONObject(y);
									String subitem_urlEnd = subjson.getString("content_url").trim()
											.replace("\\", "").replace("&amp;", "&") ;
									String subitem_url = commentUrl+subitem_urlEnd.substring(subitem_urlEnd.indexOf("?"));
									System.out.println(subitem_url);
									String subitem_title = subjson.getString("title").trim();
									String subitem_Contenturl = format2+subitem_urlEnd;
									
									Request subitem_request = new Request(subitem_url);
									subitem_request.putExtra(SogouPipeline.wxnum, wx_num);
									subitem_request.putExtra(SogouPipeline.wxname, wx_name);
									subitem_request.putExtra(SogouPipeline.title, subitem_title);
									subitem_request.putExtra(SogouPipeline.pubtime, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pubtime));
									subitem_request.putExtra(SogouPipeline.url, subitem_Contenturl);
									subitem_request.setDepth(2);
									subitem_request.setLast(true);
									page.addTargetRequest(subitem_request);	
								 
								} catch (Exception e) {
									logger.error("解析文章列表错误  multi_app_msg_item_list ", e);
								}
							}
						}
										
					}
				} else {
					logger.debug("====>解析文章列表为空");
					logger.debug(html);
				}
	}
	
	
	/**
	 * 获取对应文章标题的点赞数和阅读数
	 * @param page
	 */
	protected void parseWxRead(Page page) {
		int readnum = -1;
		int likenum = -1;
		
		String wx_num = (String) page.getRequest().getExtra(SogouPipeline.wxnum);
		String wx_name = (String) page.getRequest().getExtra(SogouPipeline.wxname);
		String title = (String) page.getRequest().getExtra(SogouPipeline.title);
		String pubtime = (String) page.getRequest().getExtra(SogouPipeline.pubtime);
		String url = (String) page.getRequest().getExtra(SogouPipeline.url);
		
		try {
			JSONObject json = JSONObject.parseObject(page.getRawText());
			readnum = json.getIntValue(SogouPipeline.read_num);
			likenum = json.getIntValue(SogouPipeline.like_num);
		} catch (Exception ignore) {
			
		}
		if (readnum != -1) {
//			page.putField(SogouPipeline.uid, genUid(wx_num, wx_name, title, pubtime));
			page.putField(SogouPipeline.wxnum, wx_num);
			page.putField(SogouPipeline.wxname, wx_name);
			page.putField(SogouPipeline.title, title);
			page.putField(SogouPipeline.pubtime, pubtime);
			page.putField(SogouPipeline.read_num, readnum);
			page.putField(SogouPipeline.like_num, likenum);
			page.putField(SogouPipeline.url, url);
		}
	}

	
	

//	protected String genUid(String wxnum , String wxname, String title, String pubtime) {
//		return DigestUtils.md5Hex(wxnum + wxname + title + pubtime);
//	}

	public Site getSite() {
		return site;
	}

	
    public static int daysBetween(Date smdate,Date bdate) { 
    	try {
	        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
				smdate=sdf.parse(sdf.format(smdate));
	        bdate=sdf.parse(sdf.format(bdate));  
	        Calendar cal = Calendar.getInstance();    
	        cal.setTime(smdate);    
	        long time1 = cal.getTimeInMillis();                 
	        cal.setTime(bdate);  
	        long time2 = cal.getTimeInMillis();         
	        long between_days=(time2-time1)/(1000*3600*24);  
	            
	         return Integer.parseInt(String.valueOf(between_days));       
	         
    	} catch (ParseException e) {
    		e.printStackTrace();
    		return -1;
    	}finally{
    	}
    	
    }    

}
