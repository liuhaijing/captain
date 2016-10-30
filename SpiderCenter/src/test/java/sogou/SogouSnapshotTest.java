package sogou;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.common.net.HttpHeaders;
import com.jiou.httpclients.HttpClientUtils;
import com.jiou.httpclients.UserAgent;

public class SogouSnapshotTest {
	// ppinf
	// pprdig
	public static void main(String[] args) throws Exception {
		String cookie = //
		"ppinf=5|1463823633|1465033233|dHJ1c3Q6MToxfGNsaWVudGlkOjQ6MTEyMHx1bmlxbmFtZTowOnxjcnQ6MTA6MTQ2MzgyMzYzM3xyZWZuaWNrOjA6fHVzZXJpZDoyMDpsaXpoZW51bWJlcjFAMTI2LmNvbXw;Domain=.sogou.com;GMT;pprdig=jEFkJDLcvCmDea5jVlVd6mmcTKNWRqPs76KKUWrqxHQ1XobWumoHEnvy2HZMk-XVoZUaUN-BKotrhSKfKUlrAkqcWOhZpWuUqumhUS-BSZbklOb0pUWh8keqjb2Pe-QvMZKJVZDT8VSkDWOQtyi_bLek6nS3iMZjq05RqNucdHw;Domain=.sogou.com;";
		// String cookie =
		// "SUV=00AD44327C4178CE573E8B806919B691;SUID=CE78417C6A28920A00000000573E8B80;SNUID=04B28BB5CACCFAF4EAD1AE8BCAE9E96F;ppinf=5|1463722829|1464932429|dHJ1c3Q6MToxfGNsaWVudGlkOjQ6MjAxN3x1bmlxbmFtZToyNzolRTglQUYlOUElRTUlOEYlOTklRTclQkMlOTh8Y3J0OjEwOjE0NjM3MjI4Mjl8cmVmbmljazoyNzolRTglQUYlOUElRTUlOEYlOTklRTclQkMlOTh8dXNlcmlkOjQ0OkFFNzE0QjY3Nzk0REQxOTE5ODA2MDk0RDdEOEYwMkI3QHFxLnNvaHUuY29tfA; pprdig=LrfmR7m2-NaGbaZzubbh2zA3CQs8sVoyW8XBykJwma0z1DVN0sIsQGgvWNs_mQmgfNqmO00yz_05BLBKqEqV5TUR1lD2GpYy05ZEiEjEGyZeqkhPea1Pgma4Yu0NLq3X_LUzGdCVn8178VeJY6BKh1opijK-IxVAWbU1fyBbUBk; ppmdig=146372282900000089612545992119f09aa534f6b2c1f39a; LSTMV=862%2C626; LCLKINT=322590";
		// String url =
		// "http://mp.weixin.qq.com/s?src=3&timestamp=1463724493&ver=1&signature=4IJ9x0DVY*2iGkYB-8p9fVk-Xj0UKLC1-Cmj1jhIAQsoH-TARrL8wNdvuqwtVN6RmLyEzwT00wEZFkj3cThtekqQlci2CPlp-sb0u0-BQPpOPyT7o59fJslFH0uUY7w5T7Ao4EJS0Nav3o8B-SWg0g==";
		String url = "http://weixin.sogou.com/weixin?query=%E7%BB%B4%E5%8D%A1%E8%8F%B2&_sug_type_=&_sug_=n&type=2&page=14&ie=utf8";
		CloseableHttpClient client = HttpClients.createDefault();
		HttpUriRequest request = RequestBuilder.get().addHeader(HttpHeaders.USER_AGENT, UserAgent.IE).setUri(url)
				.addHeader(HttpHeaders.COOKIE, cookie).build();
		CloseableHttpResponse resp = client.execute(request);
		String html = HttpClientUtils.getHtml(resp, true);
		FileUtils.writeStringToFile(new File("d:/sogou.html"), html);
		System.out.println(html);
	}
}
