package com.jiou.cookiemanager;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jiou.coding.Coding;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.support.UnicodeUtils;

/**
 * 微播易cookie manager
 * 
 * @author zhe.li
 */
@Component
@Scope("singleton")
public class WeiBoYiCookieManager extends ThreadShareCookieManager {

	private static final String[] users = //
	{ "wanmeilongge", "bjlixia0520", "shibeibijia", "begin123456" };
	private static final String[] pwds = //
	{ "8875longge", "hai123456", "19882016A", "123456789a" };

	private int codeNum;
	private boolean login;
	protected List<String> cookies = new ArrayList<String>();

	public WeiBoYiCookieManager() {
		super(4);
		if (cookieMaxNum > users.length) {
			throw new IllegalArgumentException("cookie池数量不能超过" + users.length);
		}
	}

	public WeiBoYiCookieManager(int cookieMaxNum) {
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
						}
					}
					if (cookieNum > 0) {
						login = true;
					}
					logger.info("可使用的cookie数为:{}", cookieNum);
				}
			}
		}
		return cookies.get(random.nextInt(cookies.size()));
	}

	@Override
	protected String generateCookie(Object... args) {
		CloseableHttpClient client = HttpClients.createDefault();

		String url = String.format(
				"http://chuanbo.weiboyi.com/hwauth/index/captchaajax?callback=jQuery18205593431168235838_%d&_=%d",
				new Date().getTime(), new Date().getTime());
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response;
		try {
			response = client.execute(get);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		String html = HttpClientUtils.getHtml(response);
		String cookie = HttpClientUtils.getCookie(response);

		String imgurl = getImgUrl(html);
		if (imgurl == null) {
			return null;
		}
		get.setURI(URI.create(imgurl));
		get.addHeader("Cookie", cookie);
		byte[] bytes;
		try {
			bytes = HttpClientUtils.loadBytes(get);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// FileUtils.writeByteArrayToFile(new File("D:/1.jpg"), bytes);

		synchronized (WeiBoYiCookieManager.class) {
			codeNum++;
			if (codeNum > 20) {
				codeNum = 0;
				logger.error("====>打码次数太多,睡5分钟");
				try {
					TimeUnit.MINUTES.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		String captcha = Coding.coding(bytes);

		String loginUrl = String
				.format("http://chuanbo.weiboyi.com/hwauth/index/domainlogin?callback=jQuery18205593431168235838_%d&username=%s&password=%s&captcha=%s&_=%d",
						new Date().getTime(), args[0].toString(), args[1].toString(), captcha, new Date().getTime());
		HttpGet loginGet = new HttpGet(loginUrl);
		loginGet.addHeader("Cookie", cookie);
		try {
			response = client.execute(loginGet);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		html = HttpClientUtils.getHtml(response);
		html = UnicodeUtils.decode(html);
		logger.info("user:{}登陆反馈:{}", args[0].toString(), html);
		if (!html.contains("登录成功")) {
			return null;
		}
		String cookie2 = HttpClientUtils.getCookie(response);

		String cookie3 = cookie + ";" + cookie2;
		logger.info("=====>获取的cookie为:{}", cookie3);

		HttpClientUtils.close(client);

		return cookie3;
	}

	private static String getImgUrl(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		Matcher m = Pattern.compile("\\w+\\.png").matcher(str);
		if (m.find()) {
			return "http://chuanbo.weiboyi.com/images/captcha/".concat(m.group());
		}
		return null;
	}

	public void reload() {
		synchronized (this) {
			this.login = false;
			this.cookieNum = 0;
			this.cookies.clear();
		}
	}

}
