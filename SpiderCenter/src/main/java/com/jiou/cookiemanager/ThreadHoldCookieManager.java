package com.jiou.cookiemanager;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程间不共享池中的同一个cookie.
 * 
 * @author zhe.li
 */
@ThreadSafe
public abstract class ThreadHoldCookieManager implements CookieManager {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected BlockingQueue<String> cookies;

	// cookie池最大数
	protected int cookieMaxNum;

	// 当前可用的cookie(包括被取走的cookie)
	protected int cookieNum;

	protected ThreadHoldCookieManager(int cookieMaxNum) {
		if (cookieMaxNum < 1) {
			throw new IllegalArgumentException("cookie池最大数不能小于1");
		}
		this.cookieMaxNum = cookieMaxNum;
		this.cookies = new ArrayBlockingQueue<String>(cookieMaxNum);
	}

//	@Override
	public String get(Object... args) {
		if (cookieNum < cookieMaxNum) {
			synchronized (this) {
				if (cookieNum < cookieMaxNum) {
					String cookie = generateCookie(args);
					if (StringUtils.isBlank(cookie)) {
						return get(args);
					} else {
						cookieNum++;
						return cookie;
					}
				} else {
					return get(args);
				}
			}
		} else {
			try {
				return cookies.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

//	@Override
	public boolean put(String cookie, boolean available, Object... args) {
		if (available && StringUtils.isNotBlank(cookie)) {// 该cookie可用
			try {
				cookies.put(cookie);
			} catch (InterruptedException e) {
				synchronized (this) {
					cookieNum--;
				}
				e.printStackTrace();
				return false;
			}
			return true;
		} else {// 该cookie不可用
			synchronized (this) {
				cookieNum--;
			}
			return false;
		}
	}

	/**
	 * 需子类实现，生成cookie池中的cookie
	 * 
	 * @param args
	 *            可选参数
	 * @return
	 */
	protected abstract String generateCookie(Object... args);

	public int getCookieMaxNum() {
		return cookieMaxNum;
	}

	public int getCookieNum() {
		return cookieNum;
	}

	public int getPoolNum() {
		return cookies.size();
	}

	public void reload() {
		synchronized (this) {
			this.cookieNum = 0;
			this.cookies.clear();
		}
	}

}
