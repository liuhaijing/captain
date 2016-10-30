package com.jiou.cookiemanager;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import redis.clients.jedis.JedisPool;

import com.alibaba.fastjson.JSONObject;
import com.google.common.net.HttpHeaders;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.httpclients.UserAgent;
import com.jiou.support.Redis;

public class SogouLoginCookieManager extends RedisCookieManager {
	public static final String cookieListName = "sogou_login_cookies";

	protected static final String indexurl = //
	"https://account.sogou.com/web/webLogin";
	protected static final String checkNeedCaptchaUrl = //
	"https://account.sogou.com/web/login/checkNeedCaptcha?username=%s&client_id=1120&t=%d";
	protected static final String loginurl = //
	"https://account.sogou.com/web/login";

	protected static final String autoLoginValue = "1";
	protected static final String clientIdValue = "1120";
	protected static final String xdValue = "https://account.sogou.com/static/api/jump.htm";

	protected static final String username = "username";
	protected static final String password = "password";
	protected static final String captcha = "captcha";
	protected static final String autoLogin = "autoLogin";
	protected static final String client_id = "client_id";
	protected static final String xd = "xd";
	protected static final String token = "token";

	protected SogouCookieGenerator sogouCookieGenerator = new SogouCookieGenerator();

	public SogouLoginCookieManager(JedisPool jedisPool) {
		super(cookieListName, jedisPool);
	}

	public String login(String user, String pwd) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
		CloseableHttpResponse resp = client.execute(RequestBuilder
				.get(String.format(checkNeedCaptchaUrl, user, new Date().getTime())).setConfig(requestConfig).build());
		String jsonstr = HttpClientUtils.getHtml(resp, true);
		logger.info(jsonstr);
		HttpUriRequest request = null;
		RequestBuilder requestBuilder = RequestBuilder.post(loginurl)//
				.setConfig(requestConfig)//
				.addHeader(HttpHeaders.USER_AGENT, UserAgent.IE)//
				.addParameter(username, user)//
				.addParameter(password, pwd)//
				.addParameter(autoLogin, autoLoginValue)//
				.addParameter(client_id, clientIdValue)//
				.addParameter(xd, xdValue)//
				.addParameter(token, StringUtils.EMPTY);
		if (JSONObject.parseObject(jsonstr).getJSONObject("data").getBooleanValue("needCaptcha")) {// 需要验证码
			request = requestBuilder.addParameter(captcha, StringUtils.EMPTY).build();
		} else { // 不需要验证码
			request = requestBuilder.build();
		}
		resp = client.execute(request);
		// String cookie = HttpClientUtils.getCookie(resp);
		// logger.info("User: {}获取的Cookie是: {}", user, cookie);
		String ppinf = HttpClientUtils.getCookie(resp, "ppinf");
		String pprdig = HttpClientUtils.getCookie(resp, "pprdig");
		logger.info("===>:ppinf={}", ppinf);
		logger.info("===>:pprdig={}", pprdig);
		if (StringUtils.isBlank(ppinf) || StringUtils.isBlank(pprdig)) {
			return null;
		}
		StringBuilder cookie = new StringBuilder();
		cookie.append("ppinf=").append(ppinf).append(";pprdig=").append(pprdig);
		String cookiestr = sogouCookieGenerator.generate();
		if (StringUtils.isNotBlank(cookiestr)) {
			cookie.append(";").append(cookiestr);
		}
		return cookie.toString();
	}

	protected static final String[] users = //
	{ "2413474772@qq.com", "2799194663@qq.com", "1744841056@qq.com", "cute_boy@sina.cn", "3446374781@qq.com" };
	protected static final String[] pwds = //
	{ "458856795", "458856795", "458856795", "458856795", "458856795" };

	@Override
	public void reload() {
		try {
			lock.lock();
			clear();
			int count = 0;
			for (int x = 0; x < users.length; x++) {
				String cookie = this.login(users[x], pwds[x]);
				logger.info("generate cookie:{}", cookie);
				if (cookie != null) {
					put(cookie, true);
					count++;
				}
			}
			logger.info("sogou generate cookie size:{}", count);
		} catch (Exception e) {
			logger.error("reload cookie error", e);
		} finally {
			lock.unlock();
		}
	}

	public static void main(String[] args) throws Exception {
		SogouLoginCookieManager cookieManager = new SogouLoginCookieManager(Redis.jedisPool);
		cookieManager.reload();
	}
}
