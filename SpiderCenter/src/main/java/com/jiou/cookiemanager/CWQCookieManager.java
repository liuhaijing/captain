package com.jiou.cookiemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;

import com.alibaba.fastjson.JSONObject;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.httpclients.UserAgent;
import com.jiou.support.UnicodeUtils;

/**
 * 城外圈cookie manager
 * 
 * @author zhe.li
 */
public class CWQCookieManager extends ThreadShareCookieManager {

	private static final String[] users = //
	{ "18001159112", "bjlixia0520", "wangshuang2016", "mengrufanchen", "17004958410" };
	private static final String[] pwds = //
	{ "8875longge", "hai123456", "19882016A", "meng123456", "123456789a" };

	// private static final String imgurl =
	// "http://www.cwq.com/Owner/Account/verify/";
	private static final String posturl = "http://www.cwq.com/Owner/Account/check_login/";

	private boolean login;
	protected List<String> cookies = new ArrayList<String>();

	public CWQCookieManager(int cookieMaxNum) {
		super(cookieMaxNum);
		if (cookieMaxNum > users.length) {
			throw new IllegalArgumentException("cookie池数量不能超过" + users.length);
		}
	}

	@Override
	public String get(Object... args) {
		if (!login) {
			synchronized (this) {
				if (!login) {
					for (int x = 0; x < cookieMaxNum; x++) {
						String cookie = generateCookie(users[x], pwds[x]);
						if (cookie != null) {
							cookies.add(cookie);
							cookieNum++;
						} else {
							logger.error("登陆失败,用户名:{}", users[x]);
						}
					}
					logger.info("可使用的cookie数为:{}", cookieNum);
					if (cookieNum > 0) {
						login = true;
					} else {
						throw new RuntimeException("登陆失败,生成cookie数目为0");
					}
				}
			}
		}
		return cookies.get(random.nextInt(cookies.size()));
	}

	@Override
	protected String generateCookie(Object... args) {
		CloseableHttpClient client = HttpClients.createDefault();

		// HttpGet get = new HttpGet(imgurl);
		// get.addHeader(HTTP.USER_AGENT, UserAgent.CHROME);
		CloseableHttpResponse response = null;
		// try {
		// response = client.execute(get);
		// } catch (Exception e) {
		// e.printStackTrace();
		// return null;
		// }
		// String cookie = HttpClientUtils.getCookie(response);
		// byte[] bytes = null;
		// try {
		// bytes = EntityUtils.toByteArray(response.getEntity());
		// } catch (Exception e) {
		// e.printStackTrace();
		// return null;
		// }
		// String verify = Coding.coding(bytes);
		String verify = "";

		Map<String, String> params = new HashMap<String, String>();
		params.put("account", args[0].toString());
		params.put("password", args[1].toString());
		params.put("verify", verify);
		params.put("autologin", "1");
		params.put("inajax", "1");
		HttpPost request = HttpClientUtils.buildHttpPost(posturl, params);
		// request.addHeader("Cookie", cookie);
		request.addHeader(HTTP.USER_AGENT, UserAgent.CHROME);
		try {
			response = client.execute(request);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		String cookie = HttpClientUtils.getCookie(response);
		String html = UnicodeUtils.decode(HttpClientUtils.getHtml(response));
		logger.info("城外圈登陆反馈:{}", html);
		try {
			if (!"1".equals(JSONObject.parseObject(html).getString("status"))) {
				return null;
			}
		} catch (Exception ignore) {
			return null;
		}
		return cookie;
	}

	public void reload() {
		synchronized (this) {
			this.login = false;
			this.cookieNum = 0;
			this.cookies.clear();
		}
	}

}
