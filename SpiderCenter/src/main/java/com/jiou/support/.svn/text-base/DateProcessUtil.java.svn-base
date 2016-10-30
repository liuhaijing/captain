package com.jiou.support;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * @author zhe.li
 */
public final class DateProcessUtil {

	/**
	 * The default date pattern
	 */
	public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

	private static Map<Object, Object> datePath = new LinkedHashMap<Object, Object>();
	static {
		datePath.put("(\\d{4})[-](\\d{1,2})[-](\\d{1,2})[\\s]+(\\d{2})[:](\\d{2})[:](\\d{2})", "yyyy-MM-dd HH:mm:ss");
		datePath.put("(\\d{4})[-](\\d{1,2})[-](\\d{1,2})[\\s]+(\\d{2})[:](\\d{2})", "yyyy-MM-dd HH:mm");
		datePath.put("(\\d{4})[-](\\d{1,2})[-](\\d{1,2})", "yyyy-MM-dd");
		datePath.put("(\\d{4})[/](\\d{1,2})[/](\\d{1,2})[\\s]+(\\d{2})[:](\\d{2})[:](\\d{2})", "yyyy/MM/dd HH:mm:ss");
		datePath.put("(\\d{4})[/](\\d{1,2})[/](\\d{1,2})[\\s]+(\\d{2})[:](\\d{2})", "yyyy/MM/dd HH:mm");
		datePath.put("(\\d{4})[/](\\d{1,2})[/](\\d{1,2})", "yyyy/MM/dd");
		datePath.put("(\\d{4})[年](\\d{1,2})[月](\\d{1,2})[日][\\s]+(\\d{2})[:](\\d{2})[:](\\d{2})",
				"yyyy年MM月dd日 HH:mm:ss");
		datePath.put("(\\d{4})[年](\\d{1,2})[月](\\d{1,2})[日][\\s]+(\\d{2})[:](\\d{2})", "yyyy年MM月dd日 HH:mm");
		datePath.put("(\\d{4})[年](\\d{1,2})[月](\\d{1,2})[日]", "yyyy年MM月dd日");
		datePath.put("(\\d{4})[年](\\d{1,2})[月]", "yyyy年MM月");
		datePath.put("(\\d{2})[-](\\d{1,2})[-](\\d{1,2})[\\s]+(\\d{2})[:](\\d{2})[:](\\d{2})", "yy-MM-dd HH:mm:ss");
		datePath.put("(\\d{2})[-](\\d{1,2})[-](\\d{1,2})[\\s]+(\\d{2})[:](\\d{2})", "yy-MM-dd HH:mm");
		datePath.put("(\\d{2})[-](\\d{1,2})[-](\\d{1,2})", "yy-MM-dd");
		datePath.put("(\\d{4})[.](\\d{1,2})[.](\\d{1,2})[\\s]+(\\d{2})[:](\\d{2})[:](\\d{2})", "yyyy.MM.dd HH:mm:ss");
		datePath.put("(\\d{4})[.](\\d{1,2})[.](\\d{1,2})[\\s]+(\\d{2})[:](\\d{2})", "yyyy.MM.dd HH:mm");
		datePath.put("(\\d{4})[.](\\d{1,2})[.](\\d{1,2})", "yyyy.MM.dd");
		datePath.put("(\\d{1,2})/(\\d{1,2})/(\\d{4})", "MM/dd/yyyy");
	}

	/**
	 * Convert date to string
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String dateToStr(Date date, String pattern) {
		try {
			return DateFormatUtils.format(date, pattern);
		} catch (Exception e) {
			return DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
		}
	}

	/**
	 * Extract a date object from specific string
	 * 
	 * @param items
	 * @param tp
	 * @return
	 * @throws ParseException
	 */
	public static Date process(String dateStr) {
		for (Iterator<Object> keys = datePath.keySet().iterator(); keys.hasNext();) {
			Object key = keys.next();
			Object value = datePath.get(key);
			Pattern p = Pattern.compile(key.toString());
			Matcher m = p.matcher(dateStr);
			if (m.find()) {
				try {
					return DateUtils.parseDate(m.group(), value.toString());
				} catch (ParseException e) {
					return null;
				}
			}
		}
		return null;
	}

	// public static void main(String[] args) {
	// String s = "87dsagdsalk2014.02.26 23:23:24asdfkjasl;kdfj";
	// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// System.out.println(df.format(DateProcessUtil.process(s)));
	// }
}
