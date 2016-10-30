package sogou;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

public class LoginSogouByIe {
	public static void main(String[] args) throws Exception {
		System.setProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY, "E:/dev/selenium/IEDriverServer.exe");
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
			driver.get("http://weixin.sogou.com/");
			TimeUnit.SECONDS.sleep(1);
			driver.findElement(By.id("loginBtn")).click();

			TargetLocator loginFrame = driver.switchTo();
			WebDriver subDriver = loginFrame.frame(0);
			// System.out.println(subDriver.getPageSource());
			TimeUnit.SECONDS.sleep(2);
			subDriver.findElement(By.id("switcher_plogin")).click();
			subDriver.findElement(By.id("u")).sendKeys(user);
			subDriver.findElement(By.id("p")).sendKeys(pwd);
			TimeUnit.SECONDS.sleep(2);
			subDriver.findElement(By.id("login_button")).click();
			TimeUnit.SECONDS.sleep(2);
		} finally {
			TimeUnit.SECONDS.sleep(2);
			if (driver != null) {
				driver.quit();
			}
		}
	}
}
