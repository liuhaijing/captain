package com.jiou.cookiemanager;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public abstract class RedisCookieManager implements CookieManager {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Lock lock = new ReentrantLock();
	protected Condition condition = lock.newCondition();

	protected String cookieListName;
	protected JedisPool jedisPool;

	protected RedisCookieManager(String cookieListName, JedisPool jedisPool) {
		super();
		this.cookieListName = cookieListName;
		this.jedisPool = jedisPool;
	}

	public String get(Object... args) {
		Jedis jedis = this.jedisPool.getResource();
		try {
			String cookie = jedis.lpop(cookieListName);
			while (cookie == null) {
				waitNewCookie();
				cookie = jedis.lpop(cookieListName);
			}
			return cookie;
		} catch (Exception ex) {
			logger.error("get cookie error", ex);
		} finally {
			if (jedis != null)
				jedis.close();
		}
		return null;
	}

	public boolean put(String cookie, boolean available, Object... args) {
		if (available && StringUtils.isNotBlank(cookie)) {
			Jedis jedis = this.jedisPool.getResource();
			try {
				jedis.rpush(cookieListName, cookie);
				signalNewCookie();
				return true;
			} catch (Exception ex) {
				logger.error("put back cookie error", ex);
			} finally {
				if (jedis != null)
					jedis.close();
			}
		}
		return false;
	}

	@Deprecated
	public int getCookieMaxNum() {
		return getPoolNum();
	}

	@Deprecated
	public int getCookieNum() {
		return getPoolNum();
	}

	public int getPoolNum() {
		Jedis jedis = this.jedisPool.getResource();
		try {
			jedis.llen(this.cookieListName);
		} catch (Exception ex) {
			logger.error("reload cookie error", ex);
		} finally {
			if (jedis != null)
				jedis.close();
		}
		return 0;
	}

	protected void clear() {
		Jedis jedis = this.jedisPool.getResource();
		try {
			jedis.del(this.cookieListName);
		} catch (Exception ex) {
			logger.error("clear cookie error", ex);
		} finally {
			if (jedis != null)
				jedis.close();
		}
	}

	public abstract void reload();

	private void waitNewCookie() {
		try {
			lock.lock();
			condition.await(50, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	private void signalNewCookie() {
		try {
			lock.lock();
			condition.signalAll();
		} finally {
			lock.unlock();
		}
	}

}
