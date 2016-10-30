package sogou;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

public class LoginSogouByIe2 {
	public static void main(String[] args) throws Exception {
		System.setProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY, "E:/dev/selenium/IEDriverServer.exe");
		String url = "https://graph.qq.com/oauth/show?which=Login&display=pc&scope=get_user_info%2Cget_app_friends&response_type=code&show_auth_items=0&redirect_uri=https%3A%2F%2Faccount.sogou.com%2Fconnect%2Fcallback%2Fqq%3Fclient_id%3D2017%26ip%3D124.65.120.206%26ru%3Dhttp%25253A%25252F%25252Fweixin.sogou.com%25252Fpcindex%25252Flogin%25252Fqq_login_callback_page.html%26type%3Dweb&state=dbec60bd-e729-4e52-8457-177160bae3fe&client_id=100294784";
		String user = "1930036742";
		String pwd = "pb22d8eahp";
		WebDriver driver = null;
		try {
			DesiredCapabilities desiredCapabilities = DesiredCapabilities.internetExplorer();
			desiredCapabilities.setCapability(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY,
					"E:/dev/selenium/IEDriverServer.exe");
			desiredCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
					true);
			desiredCapabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
			driver = new InternetExplorerDriver(desiredCapabilities);
			driver.get(url);
			TimeUnit.SECONDS.sleep(1);
			WebDriver subdriver = driver.switchTo().frame(driver.findElement(By.id("ptlogin_iframe")));
			FileUtils.writeStringToFile(new File("d:/sogou.html"), subdriver.getPageSource());
			subdriver.findElement(By.id("switcher_plogin")).click();
			TimeUnit.SECONDS.sleep(1);
			subdriver.findElement(By.id("u")).sendKeys(user);
			subdriver.findElement(By.id("p")).sendKeys(pwd);
			TimeUnit.SECONDS.sleep(1);
			subdriver.findElement(By.id("login_button")).click();
			TimeUnit.SECONDS.sleep(1);
		} finally {
			// TimeUnit.SECONDS.sleep(1);
			// if (driver != null) {
			// driver.quit();
			// }
		}
	}
}
