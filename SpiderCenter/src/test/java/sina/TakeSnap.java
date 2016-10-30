package sina;

import java.io.File;

import org.apache.commons.io.FileUtils;

import us.codecraft.webmagic.Request;

import com.jiou.downloader.SinaSpreadDownloader;

public class TakeSnap {
	public static void main(String[] args) throws Exception {
		SinaSpreadDownloader downloader = new SinaSpreadDownloader();
		String html = downloader.download(new Request("http://weibo.com/u/5747171241?refer_flag=1001030103_&is_hot=1"),
				null).getRawText();
		FileUtils.writeStringToFile(new File("d:/sina.html"), html);
	}
}
