package us.codecraft.webmagic.scheduler.component;

import com.google.common.collect.Sets;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author code4crafer@gmail.com
 */
public class HashSetDuplicateRemover implements DuplicateRemover {

	private Set<String> urls = Sets.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

//	@Override
	public boolean isDuplicate(Request request, Task task) {
		// request设置为要去重才去判断
		if (request.isFilter()) {
			return !urls.add(getUrl(request));
		} else {
			return false;
		}
	}

	protected String getUrl(Request request) {
		return request.getUrl();
	}

//	@Override
	public void resetDuplicateCheck(Task task) {
		urls.clear();
	}

//	@Override
	public int getTotalRequestsCount(Task task) {
		return urls.size();
	}
}
