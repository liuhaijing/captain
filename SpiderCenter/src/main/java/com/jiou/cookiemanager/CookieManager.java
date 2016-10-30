package com.jiou.cookiemanager;

/**
 * @author zhe.li
 */
public interface CookieManager {

	/**
	 * 获取一个cookie
	 * 
	 * @param args
	 *            可选参数
	 * @return
	 */
	public String get(Object... args);

	/**
	 * 放入一个cookie
	 * 
	 * @param cookie
	 *            放入的cookie
	 * @param available
	 *            放入的cookie是否可用
	 * @param args
	 *            可选参数
	 * @return true,放回成功;false,放回失败
	 */
	public boolean put(String cookie, boolean available, Object... args);

	/**
	 * 获取cookie池最大数
	 * 
	 * @return
	 */
	public int getCookieMaxNum();

	/**
	 * 获取可用cookie数目
	 * 
	 * @return
	 */
	public int getCookieNum();

	/**
	 * 获取cookie池中的cookie数目
	 * 
	 * @return
	 */
	public int getPoolNum();

	/**
	 * 重新加载cookie
	 */
	public void reload();

}
