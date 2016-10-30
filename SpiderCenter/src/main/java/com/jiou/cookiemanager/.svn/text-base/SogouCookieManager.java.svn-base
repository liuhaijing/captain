package com.jiou.cookiemanager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

import com.jiou.support.Redis;

@SuppressWarnings("deprecation")
public class SogouCookieManager implements CookieManager {

	public static final String queue_name = "sogou_cookies";

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected Queue<String> cookies = new ConcurrentLinkedQueue<String>();

//	@Override
	public String get(Object... args) {
		String cookie = cookies.poll();
		if (StringUtils.isNotBlank(cookie)) {
			return cookie;
		}
		Jedis jedis = Redis.jedisPool.getResource();
		try {
			cookie = jedis.lpop(queue_name);
			while (StringUtils.isBlank(cookie)) {
				TimeUnit.SECONDS.sleep(10);
				cookie = jedis.lpop(queue_name);
			}
			return cookie;
		} catch (Exception ex) {
			logger.error("获取cookie错误", ex);
		} finally {
			Redis.jedisPool.returnResource(jedis);
		}
		return null;
	}

//	@Override
	public boolean put(String cookie, boolean available, Object... args) {
		if (available && StringUtils.isNotBlank(cookie)) {// 该cookie可用
			cookies.offer(cookie);
			return true;
		}
		return false;
	}

//	@Override
	public int getCookieMaxNum() {
		return getCookieNum() + cookies.size();
	}

//	@Override
	public int getCookieNum() {
		Jedis jedis = Redis.jedisPool.getResource();
		try {
			return jedis.llen(queue_name).intValue();
		} catch (Exception ignore) {
		} finally {
			Redis.jedisPool.returnResource(jedis);
		}
		return 0;
	}

//	@Override
	public int getPoolNum() {
		return 0;
	}

//	@Override
	public void reload() {
	}

}
