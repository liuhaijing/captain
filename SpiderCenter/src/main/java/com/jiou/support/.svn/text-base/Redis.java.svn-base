package com.jiou.support;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.JedisPool;

public class Redis {
	public static final String host = "101.201.75.190";
	public static final int port = 6379;
	public static final JedisPool jedisPool;
	static {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxTotal(100);
		poolConfig.setMaxIdle(20);
		poolConfig.setTestOnBorrow(true); 
		poolConfig.setTestOnReturn(true); 
		jedisPool = new JedisPool(poolConfig, host, port, 10000);
	}
}
