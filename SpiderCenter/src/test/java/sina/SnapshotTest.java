package sina;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import com.google.common.net.HttpHeaders;
import com.jiou.httpclients.DefaultConnectionKeepAliveStrategy;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.httpclients.UserAgent;
import com.jiou.support.Mayi;
import com.jiou.support.UnicodeUtils;

public class SnapshotTest {

	// http://weibo.com/u/5934584135/home

	private static final String seedFormat = "http://s.weibo.com/weibo/%s&scope=ori&suball=1&Refer=g&page=%d";

	public static void main(String[] args) throws Exception {
		// String url =
		// "http://s.weibo.com/weibo/%25E4%25B8%25AD%25E5%259B%25BD%25E7%25A7%25BB%25E5%258A%25A8%25E5%25AE%25A3%25E5%25B8%25836%25E6%259C%258830%25E6%2597%25A5%25E4%25B8%258B%25E7%25BA%25BF%25E7%259F%25AD%25E4%25BF%25A1%25E8%25BD%25AC%25E9%25A3%259E%25E4%25BF%25A1%25E4%25B8%259A%25E5%258A%25A1?topnav=1&wvr=6&b=1";
		String url2 = String.format(seedFormat, URLEncoder.encode("中国移动宣布6月30日下线短信转飞信业务", "UTF-8"), 4);
		System.out.println(url2);
		CloseableHttpClient client = buildHttpClient();
		HttpUriRequest request = createHttpUriRequest(url2, false);
		CloseableHttpResponse resp = client.execute(request);
		String cookie = HttpClientUtils.getCookie(resp);
		String html = HttpClientUtils.getHtml(resp, true);
		html = UnicodeUtils.decode(html);
		System.out.println(cookie);
		System.out.println(html);
		FileUtils.writeStringToFile(new File("d:/sina.html"), html);
		int maxPage = parseMaxPage(html);
		System.out.println(maxPage);
	}

	public static int parseMaxPage(String html) {
		int maxpage = -1;
		if (StringUtils.isBlank(html)) {
			return maxpage;
		}
		Matcher m = Pattern.compile("第(\\d+)页").matcher(html);
		while (m.find()) {
			int p = Integer.parseInt(m.group(1));
			maxpage = p > maxpage ? p : maxpage;
		}
		return maxpage;
	}

	// 3450426725@qq.com
	// _2A256OUZ0DeTxGeNH6FYU-CrNyDmIHXVZTzC8rDV_PUNbuNAPLULQkW9LHeuP_rCRzdJEBYkJ4Ahou3wEPEEGRA..
	private static final String cookie = //
	"SUS=SID-5934175638-1463630142-XD-p8o2l-7f8e5115666f79b80d0082df55c6af78; path=/; domain=.weibo.com;SUS=SID-5934175638-1463630142-XD-p8o2l-7f8e5115666f79b80d0082df55c6af78; path=/; domain=.weibo.com; httponly;SUE=es%3D43a31d9b7fd9f0c568ee7603b818efa9%26ev%3Dv1%26es2%3D12d63e8261fb32eac1221c42a6649e3c%26rs0%3D58Kt5RjyQE8Ftf1tKnXzVo%252BaMJsn4NzYcvCpsmB4zzj3EjQvVJiq%252F0FjaR6FOH0czeWIRdx2AS3EXI%252BiBNG8UAPvI0oxcA5X9Ljha27K1Ppi4tflSvYlEoJ%252FEGhioCdfKAOO62ubo2qnShmfD8XoxcpmA36%252B0wc0MyFve92wHdM%253D%26rv%3D0;path=/;domain=.weibo.com;Httponly;SUP=cv%3D1%26bt%3D1463630142%26et%3D1463716542%26d%3Dc909%26i%3Daf78%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D2%26uid%3D5934175638%26name%3D1744841056%2540qq.com%26nick%3D%25E6%2589%2580%25E4%25BB%25A5%25E5%2591%25A22016%26fmp%3D%26lcp%3D;path=/;domain=.weibo.com;SUB=_2A256OUluDeTRGeNH6FYQ9yvKyDSIHXVZTz2mrDV8PUNbuNAPLWb_kW9LHet4Rli_YV4WJ9ERX6cHYypoNPz43g..; path=/; domain=.weibo.com; httponly;SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhFd-QRMof6YKNcqO4xS0yv5JpX5K2hUgL.Fo-4e0BpS0-ce0nt; expires=Friday, 19-May-17 03:55:42 GMT; path=/; domain=.weibo.com;SUHB=0QSpsK6yeiya0F; expires=Friday, 19-May-17 03:55:42 GMT; path=/; domain=.weibo.com;SRT=E.vAfsKOJrJOVsiZJtv!ShKvmBvXvCvXMQLO!_vnmBBvzvvv4mamEtqXVmPqw16XfCvCAivOmKvAmLvAmMvXvCFvmvFXMQLO!_*B.vAflW-P9Rc0lR-ykPDvnJqiQVbiRVPBtS!r3JZPQVqbgVdWiMZ4siOzu4DbmKPWfMcAk4PHiU-R-d4kKR3ELiGYOJGb-i49ndDPIJcYPSrnlMc0kiF4nAeBOJCsZSOBlWv77; expires=Sunday, 17-May-26 03:55:42 GMT; path=/; domain=.passport.weibo.com; httponly;SRF=1463630142; expires=Sunday, 17-May-26 03:55:42 GMT; path=/; domain=.passport.weibo.com;ALF=1495166142; expires=Fri, 19-May-2017 03:55:42 GMT; path=/; domain=.weibo.com;myuid=uid;SinaRot_wb_r_topic=39;UV5PAGE=usr511_179;; UV5=usr319_182;;SSOLoginState=1463630142;;UOR=,,login.sina.com.cn;;_s_tentry=login.sina.com.cn;";

	// private static final String cookie = //
	// "SINAGLOBAL=9789192951284.35.1463123735478; login_sid_t=c402ca2692c5d18fd3f709b71b627948; _s_tentry=-; Apache=6723377585876.733.1463621458560; ULV=1463621458574:4:4:3:6723377585876.733.1463621458560:1463538173816; NSC_wjq_txfjcp_mjotij=ffffffff094113d745525d5f4f58455e445a4a423660; UOR=,,www.sootoo.com; WBtopGlobal_register_version=60539f809b40ed0d; SUS=SID-5934175140-1463624124-XD-9xrle-6cda58cc24a89cd09f600d2f5459af78; SUE=es%3D68e0e23e170a6b4da03cd8f442b2896b%26ev%3Dv1%26es2%3D05ed16657c9e02165b5fd06c2bce0cf3%26rs0%3D5LZHX2xIpNqO%252BByIe2Su%252FUQBhbqNh%252FpPl0bxQXLr55sXLuy4no3sMNvMzYfL1%252FMEMnb1chNxeTuvMpv97AFup2tsmdyJudsdCMWdt1v8UrLkrBBBuPwS3aiMtVTHfAE%252BWF2NG%252B90zOnuWcWmx4tfxdwfOShlxZXYtxx2TYI%252FHeA%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1463624124%26et%3D1463710524%26d%3Dc909%26i%3Daf78%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D2%26uid%3D5934175140%26name%3D2799194663%2540qq.com%26nick%3D%25E5%25B0%2591%25E5%25B9%25B4%25E6%2589%2580%25E4%25BB%25A5%25E5%2591%25A2%26fmp%3D%26lcp%3D; SUB=_2A256OVHsDeTRGeNH6FYQ9yvNzzyIHXVZT8QkrDV8PUNbuNAPLU38kW9LHeuDcMgHO83QMw9nyXrnudAYkRCIKQ..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9Wh4IVygLL4e2lfOqbe5h.rk5JpX5K2hUgL.Fo-4e0BpS0-pSh5t; SUHB=09Pta6lqL9wMmP; ALF=1464228929; SSOLoginState=1463624124; un=2799194663@qq.com; WBStore=8ca40a3ef06ad7b2|undefined";

	public static HttpUriRequest createHttpUriRequest(String url, boolean useproxy) {
		String authHeader = Mayi.buildAuthHeader();
		RequestConfig.Builder requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(30000);
		RequestBuilder requestBuilder = RequestBuilder.get().setUri(url)
				.addHeader(HttpHeaders.USER_AGENT, UserAgent.IE).addHeader(HttpHeaders.COOKIE, cookie);
		HttpUriRequest request = null;
		if (useproxy) {
			request = requestBuilder.setConfig(requestConfig.setProxy(new HttpHost(Mayi.host, Mayi.port)).build())
					.addHeader(Mayi.auth, authHeader).build();
		} else {
			request = requestBuilder.setConfig(requestConfig.build()).build();
		}
		return request;
	}

	protected static CloseableHttpClient buildHttpClient() {
		HttpClientBuilder builder = HttpClients.custom();
		builder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy(5L));
		builder.setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build());
		List<Header> defaultHeaders = new ArrayList<Header>();
		defaultHeaders.add(new BasicHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
		defaultHeaders.add(new BasicHeader("Accept-Encoding", "gzip, deflate, sdch"));
		defaultHeaders.add(new BasicHeader("Accept-Language", "zh-CN,zh;q=0.8"));
		builder.setDefaultHeaders(defaultHeaders);
		builder.setConnectionManager(HttpClientUtils.getConnectionmanager());
		return builder.build();
	}

}
