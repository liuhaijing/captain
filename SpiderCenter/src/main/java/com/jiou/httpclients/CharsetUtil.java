package com.jiou.httpclients;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * @author zhe.li
 */
public class CharsetUtil {

	public static final Pattern metaPattern = Pattern.compile("<meta\\s+([^>]*http-equiv=\"?content-type\"?[^>]*)>", Pattern.CASE_INSENSITIVE);

	public static final Pattern charsetPattern = Pattern.compile("charset=\\s*([a-z][_\\-0-9a-z]*)", Pattern.CASE_INSENSITIVE);

	private static ThreadLocal<CharsetDetector> detectors = new ThreadLocal<CharsetDetector>();

	private static Map<String, String> ALIAS = new HashMap<String, String>();

	static {
		ALIAS.put("gb2312", "gb18030");
		ALIAS.put("gbk", "gb18030");
	}

	static CharsetDetector getDetector() {
		CharsetDetector d = detectors.get();

		if (d == null) {
			d = new CharsetDetector();

			detectors.set(d);
		}

		return d;
	}

	public static String analyze(byte[] bytes) {
		String charset = null;
		CharsetDetector detector = getDetector();
		detector.setText(bytes);
		CharsetMatch match = detector.detect();
		int confidence = match.getConfidence();

		if (confidence >= 60) {
			charset = match.getName();
		}

		if (charset == null) {
			charset = parseHtmlCharset(bytes);
		}

		if (charset == null) {
			charset = "UTF-8";
		}

		if (charset.contains("JP")) {
			charset = "GB18030";
		} else if (charset.contains("EUC-KR")) {
			charset = "GB18030";
		}

		if (ALIAS.containsKey(charset)) {
			charset = ALIAS.get(charset);
		}

		return charset;
	}

	private static String parseHtmlCharset(byte[] content) {

		String s = new String(content);
		Matcher meta = metaPattern.matcher(s);

		while (meta.find()) {
			String match = meta.group();
			Matcher cm = charsetPattern.matcher(match);

			if (cm.find()) {
				return cm.group(1);
			}
		}

		return null;
	}

}
