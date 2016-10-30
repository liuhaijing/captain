package com.jiou.httpclients;

/**
 * @author zhe.li
 */
public interface UserAgent {
	/**
	 * ie用户代理
	 */
	public static final String IE = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0)";

	/**
	 * 谷歌浏览器用户代理
	 */
	public static final String CHROME = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36";

	/**
	 * 火狐用户代理
	 */
	public static final String FIREFOX = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0";

	/**
	 * 安卓
	 */
	public static final String ANDROID = "Dalvik/1.6.0 (Linux; U; Android 4.4.4; A31 Build/KTU84P)";
}
