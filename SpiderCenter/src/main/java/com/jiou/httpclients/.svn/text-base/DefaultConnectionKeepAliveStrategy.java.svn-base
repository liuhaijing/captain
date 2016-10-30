package com.jiou.httpclients;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/**
 * @author zhe.li
 */
public final class DefaultConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {

	private long keepAliveInSeconds;

	public DefaultConnectionKeepAliveStrategy(long keepAliveInSeconds) {
		this.keepAliveInSeconds = keepAliveInSeconds;
	}

//	@Override
	public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
		final HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
		while (it.hasNext()) {
			final HeaderElement he = it.nextElement();
			final String param = he.getName();
			final String value = he.getValue();
			if (value != null && param.equalsIgnoreCase("timeout")) {
				try {
					return Long.parseLong(value) * 1000;
				} catch (final NumberFormatException ignore) {
				}
			}
		}

		if (keepAliveInSeconds <= 0) {
			return -1;
		}

		return keepAliveInSeconds * 1000;
	}

}
