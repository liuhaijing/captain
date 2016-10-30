package com.jiou.pageprocessor.sina;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

public abstract class AbstractSinaPageProcessor implements PageProcessor {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected Site site = Site.me().setRetryTimes(2).setSleepTime(50).setTimeOut(60000);

	public Site getSite() {
		return site;
	}

	protected String extractUid(String s) {
		if (StringUtils.isBlank(s)) {
			return null;
		}
		Matcher m = Pattern.compile("[/|id=](\\d+)").matcher(s);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	protected String parseUName(String html) {
		if (StringUtils.isBlank(html)) {
			return null;
		}
		Matcher m = Pattern.compile("<h1\\s+class=\\\\\"username\\\\\">(.+)<\\\\/h1>").matcher(html);
		if (m.find()) {
			String s = m.group(1).trim();
			return s;
		}
		return null;
	}

	/**
	 * 解析性别,1-男,2-女,3-未知
	 */
	protected int parseGender(String html) {
		if (StringUtils.isBlank(html)) {
			return 3;
		}
		Matcher m = Pattern.compile("W_icon\\s+icon_pf_female|W_icon\\s+icon_pf_male").matcher(html);
		if (m.find()) {
			String s = m.group();
			if (s.contains("female")) {
				return 2;
			} else if (s.contains("male")) {
				return 1;
			}
		}
		return 3;
	}

	protected String parseBrief(String html) {
		if (StringUtils.isBlank(html)) {
			return null;
		}
		Matcher m = Pattern.compile("<div\\s+class=\\\\\"pf_intro\\\\\"\\s+title=[^>]+>([^<]+)<\\\\/div>")
				.matcher(html);
		if (m.find()) {
			String s = m.group(1).trim().replaceAll("\\\\[rnt]", "");
			return s;
		}
		return null;
	}

	protected String parseAddress(String html) {// TODO 暂时解析不出来
		if (StringUtils.isBlank(html)) {
			return null;
		}
		try {
			Matcher m = Pattern
					.compile(
							"<li class=\\\\\"item S_line2 clearfix\\\\\">.+<em class=\\\\\"W_ficon ficon_cd_place S_ficon\\\\\">\\d+<\\\\/em>.+<\\\\/li>")
					.matcher(html);
			if (m.find()) {
				String s = m.group().trim().replaceAll("\\\\[rnt]", "");
				s = Jsoup.parse(s).select("em.W_ficon.ficon_cd_place.S_ficon").get(0).parent().nextElementSibling()
						.text();
				return s;
			}
		} catch (Exception ex) {
			logger.error("解析地址错误", ex);
		}
		return null;
	}

	protected String parseLabel(String html) {
		if (StringUtils.isBlank(html)) {
			return null;
		}
		try {
			Matcher m = Pattern.compile(
					"<a[^h]+href=\\\\\"http:\\\\/\\\\/s\\.weibo\\.com\\\\/user\\\\/&tag=[^>]+>([^<]+)<\\\\/a>")
					.matcher(html);
			StringBuilder sb = new StringBuilder();
			while (m.find()) {
				String s = m.group(1).trim().replaceAll("\\\\[rnt]", "");
				sb.append(s).append(" ");
			}
			String s = sb.toString().trim();
			return StringUtils.isBlank(s) ? null : s;
		} catch (Exception ex) {
			logger.error("解析标签错误", ex);
		}
		return null;
	}

	protected Boolean parseVeri(String html) {// 解析是否加V
		if (StringUtils.isBlank(html)) {
			return null;
		}
		if (html.contains("icon_bed W_fl")) {
			return true;
		} else {
			return false;
		}
	}

}
