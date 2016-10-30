package sogou;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.Jedis;

import com.jiou.cookiemanager.SogouCookieGenerator;
import com.jiou.support.Redis;

public class PushSogouCookie {
	public static void main(String[] args) throws Exception {
		Jedis jedis = Redis.jedisPool.getResource();
		SogouCookieGenerator sogouCookieGenerator = new SogouCookieGenerator();
		while (true) {
			String cookie = sogouCookieGenerator.generate();
			if (StringUtils.isNotBlank(cookie)) {
				System.out.println(Thread.currentThread().getName() + "正在执行。。。" + jedis.rpush("sogou_cookies", cookie));
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void run() {
		Jedis jedis = Redis.jedisPool.getResource();
		SogouCookieGenerator sogouCookieGenerator = new SogouCookieGenerator();
		while (true) {
			String cookie = sogouCookieGenerator.generate();
			if (StringUtils.isNotBlank(cookie)) {
				System.out.println(Thread.currentThread().getName() + "正在执行。。。" + jedis.rpush("sogou_cookies", cookie));
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
