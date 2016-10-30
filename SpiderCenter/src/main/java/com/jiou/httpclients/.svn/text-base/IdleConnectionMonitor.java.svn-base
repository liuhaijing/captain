package com.jiou.httpclients;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * use it to close idle http connection in the specific http connection pool.
 * 
 * @author zhe.li
 */
public class IdleConnectionMonitor extends Thread implements Closeable {

	private static final String TOTAL_STAT_PREFIX = "HttpPoolState:";
	@SuppressWarnings("unused")
	private static final String ROUTE_STAT_PREFIX = "HttpRouteState:";

	private PoolingHttpClientConnectionManager connectionManager;
	private int keepAliveTimeInSeconds;
	private boolean running;

	public IdleConnectionMonitor(PoolingHttpClientConnectionManager connectionManager, int keepAliveTimeInSeconds) {
		if (connectionManager == null || keepAliveTimeInSeconds <= 0) {
			throw new IllegalArgumentException("args error.");
		}
		this.connectionManager = connectionManager;
		this.keepAliveTimeInSeconds = keepAliveTimeInSeconds;
	}

	@Override
	public void run() {
		while (this.running) {
			synchronized (this) {

				try {
					TimeUnit.SECONDS.sleep(keepAliveTimeInSeconds);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				connectionManager.closeExpiredConnections();
				connectionManager.closeIdleConnections(keepAliveTimeInSeconds, TimeUnit.SECONDS);

				System.out.println(TOTAL_STAT_PREFIX + connectionManager.getTotalStats().toString());
				Set<HttpRoute> routes = connectionManager.getRoutes();
				if (routes != null) {
					for (HttpRoute route : routes) {
						System.out.println(route.getTargetHost().toHostString() + connectionManager.getStats(route));
					}
				}

			}
		}
	}

	@Override
	public synchronized void start() {
		this.setDaemon(true);
		this.running = true;
		super.start();
	}

	public void setKeepAliveTimeInSeconds(int keepAliveTimeInSeconds) {
		this.keepAliveTimeInSeconds = keepAliveTimeInSeconds;
	}

	/**
	 * stop the minitor task.
	 */
	@Deprecated
//	@Override
	public void close() {
		this.running = false;
	}

}
