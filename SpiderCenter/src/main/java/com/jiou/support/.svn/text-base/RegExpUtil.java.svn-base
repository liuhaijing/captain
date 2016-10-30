package com.jiou.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhe.li
 */
public class RegExpUtil {

	public static Long extractLong(String regex, String url) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(url);
		if (m.find()) {
			return extractNumber(m.group());
		}
		return null;
	}

	private static Long extractNumber(String str) {
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return Long.valueOf(m.group());
		}
		return null;
	}

}
