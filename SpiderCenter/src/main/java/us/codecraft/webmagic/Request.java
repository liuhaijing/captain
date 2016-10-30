package us.codecraft.webmagic;

import us.codecraft.webmagic.utils.Experimental;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Object contains url to crawl.<br>
 * It contains some additional information.<br>
 * 
 * 增加last和filter两个字段,用于@see
 * {@link us.codecraft.webmagic.scheduler.RedisScheduler}</br>
 * 
 * @author code4crafter@gmail.com <br>
 * @since 0.1.0
 */
public class Request implements Serializable, Cloneable {

	private static final long serialVersionUID = 2062192774891352043L;

	public static final String CYCLE_TRIED_TIMES = "_cycle_tried_times";
	public static final String STATUS_CODE = "statusCode";
	public static final String PROXY = "proxy";

	private String url;

	private String method;

	/**
	 * Store additional information in extras.
	 */
	private Map<String, Object> extras;

	/**
	 * Priority of the request.<br>
	 * The bigger will be processed earlier. <br>
	 * 
	 * @see us.codecraft.webmagic.scheduler.PriorityScheduler
	 */
	private long priority;

	private boolean last;

	private boolean filter;

	private String cookie;

	/**
	 * 深度,默认0
	 */
	private int depth;

	private int executeCount;

	public Request() {
	}

	public Request(String url) {
		this.url = url;
	}

	public long getPriority() {
		return priority;
	}

	/**
	 * Set the priority of request for sorting.<br>
	 * Need a scheduler supporting priority.<br>
	 * 
	 * @see us.codecraft.webmagic.scheduler.PriorityScheduler
	 * 
	 * @param priority
	 * @return this
	 */
	@Experimental
	public Request setPriority(long priority) {
		this.priority = priority;
		return this;
	}

	public Object getExtra(String key) {
		if (extras == null) {
			return null;
		}
		return extras.get(key);
	}

	public Request putExtra(String key, Object value) {
		if (extras == null) {
			extras = new HashMap<String, Object>();
		}
		extras.put(key, value);
		return this;
	}

	public String getUrl() {
		return url;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Request request = (Request) o;

		if (!url.equals(request.url))
			return false;

		return true;
	}

	public Map<String, Object> getExtras() {
		return extras;
	}

	@Override
	public int hashCode() {
		return url.hashCode();
	}

	public void setExtras(Map<String, Object> extras) {
		this.extras = extras;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * The http method of the request. Get for default.
	 * 
	 * @return httpMethod
	 * @see us.codecraft.webmagic.utils.HttpConstant.Method
	 * @since 0.5.0
	 */
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * 是否最后一层
	 * 
	 * @return
	 */
	public boolean isLast() {
		return last;
	}

	/**
	 * 设置该url是否最后一层,默认false
	 * 
	 * @param last
	 */
	public void setLast(boolean last) {
		this.last = last;
	}

	/**
	 * 是否去重
	 * 
	 * @return
	 */
	public boolean isFilter() {
		return filter;
	}

	/**
	 * 设置该url是否去重，默认false
	 * 
	 * @param filter
	 */
	public void setFilter(boolean filter) {
		this.filter = filter;
	}

	/**
	 * 获取cookie
	 * 
	 * @return
	 */
	public String getCookie() {
		return cookie;
	}

	/**
	 * 设置cookie
	 * 
	 * @param cookie
	 */
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getExecuteCount() {
		return executeCount;
	}

	public void setExecuteCount(int executeCount) {
		this.executeCount = executeCount;
	}

	@Override
	public String toString() {
		return "Request{" + "url='" + url + '\'' + ", method='" + method + '\'' + ", extras=" + extras + ", priority="
				+ priority + '}';
	}

	@Override
	public Request clone() {
		try {
			return (Request) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
