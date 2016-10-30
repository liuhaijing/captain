package com.jiou.cookiemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程间可以共享池中的同一个cookie.
 * 
 * @author zhe.li
 */
@ThreadSafe
public abstract class ThreadShareCookieManager implements CookieManager {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected List<String> cookies = new ArrayList<String>();

	protected Random random = new Random();

	// cookie池最大数
	protected int cookieMaxNum;

	// 当前可用的cookie(包括被取走的cookie)
	protected int cookieNum;

	protected ThreadShareCookieManager(int cookieMaxNum) {
		if (cookieMaxNum < 1) {
			throw new IllegalArgumentException("cookie池最大数不能小于1");
		}
		this.cookieMaxNum = cookieMaxNum;
	}

//	@Override
	public String get(Object... args) {
		String cookie = null;
		if (cookieNum < cookieMaxNum) {
			synchronized (this) {
				if (cookieNum < cookieMaxNum) {
					cookie = generateCookie(args);
					if (cookie != null && cookies.add(cookie)) {
						cookieNum++;
					}
				} else {
					cookie = cookies.get(random.nextInt(cookies.size()));
				}
			}
		} else {
			cookie = cookies.get(random.nextInt(cookies.size()));
		}
		return cookie;
	}

	// @Override
	// public String get(Object... args) {
	// String cookie = null;
	// if (cookies.size() > 0) {
	// cookie = cookies.get(random.nextInt(cookies.size()));
	// } else {
	// if (cookieNum < cookieMaxNum) {
	// synchronized (this) {
	// if (cookieNum < cookieMaxNum) {
	// cookie = generateCookie(args);
	// if (cookie != null && cookies.add(cookie)) {
	// cookieNum++;
	// }
	// } else {
	// cookie = cookies.get(random.nextInt(cookies.size()));
	// }
	// }
	// } else {
	// cookie = cookies.get(random.nextInt(cookies.size()));
	// }
	// }
	// return cookie;
	// }

//	@Override
	public boolean put(String cookie, boolean available, Object... args) {
		if (!available) {// cookie已失效,移除
			synchronized (this) {
				int index = cookies.indexOf(cookie);
				if (index >= 0) {
					cookies.remove(index);
					cookieNum--;
				}
			}
			return false;
		}
		return true;
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
			this.cookies.clear();
			this.cookieNum = 0;
		}
	}

}
