package sina;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import us.codecraft.webmagic.downloader.HttpClientGenerator;

import com.jiou.httpclients.HttpClientUtils;

public class GanjiTest {
	public static void main(String[] args) throws Exception {
		String url = "http://anshun.ganji.com/motuoche/1991381413x.htm";
		CloseableHttpClient client = new HttpClientGenerator().getClient(null);
		HttpUriRequest request = RequestBuilder.get(url)//
				.build();
		CloseableHttpResponse resp = client.execute(request);
		String html = HttpClientUtils.getHtml(resp, true);
		FileUtils.writeStringToFile(new File("d:/ganji.html"), html);
		System.out.println(html);
		Document doc = Jsoup.parse(html);
		try {
			String contacts = doc.select("label:contains(联系人)").get(0).nextSibling().toString().trim()
					.replace("&nbsp;", "");
			System.out.println(contacts);
		} catch (Exception ignore) {
		}
		try {
			String tel = doc.select("label:contains(话) ~ span.phoneNum-style").get(0).text().trim();
			System.out.println(tel);
		} catch (Exception ignore) {
		}
		try {
			String qq = doc.select("label:contains(QQ) ~ span.phoneNum-style").get(0).text().trim();
			System.out.println(qq);
		} catch (Exception ignore) {
		}
	}
}
