package com.qige.weixingatherplatform.spider;

import com.qige.weixingatherplatform.spider.commons.SpendMail;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author weitangzhao
 **/
@PropertySource("classpath:application.properties")
public class WeixinLogin  implements PageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeixinLogin.class);
    @Value("${xm.userName}")
    private String userName;

    @Value("${xm.userPassword}")
    private String userPassword;



    @Autowired
    SpendMail spendMail;

    private String tooken = null;

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(3000)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");

    @Override
    public void process(Page page) {

        LOGGER.info(page.getUrl().toString());
        if(page.getUrl().toString().equals("http://mp.weixin.qq.com")) {

            //登录获取tooken cookie
            try {
                setCookieAndTooken(page);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if(page.getUrl().regex("https://mp.weixin.qq.com/cgi-bin/searchbiz?").match()) {
            //获取fakie
            LOGGER.info(page.getJson().toString());
            List<String> listFakeid = new JsonPathSelector("$.list[*].fakeid").selectList(page.getRawText());
            LOGGER.info(listFakeid.get(0));
            page.addTargetRequest("https://mp.weixin.qq.com/cgi-bin/appmsg?token=" + this.tooken + "&lang=zh_CN&f=json&ajax=1&random=0.02650980321670149&action=list_ex&begin=0&count=5&query=&fakeid=" + listFakeid.get(0)+ "&type=9");
        } else if(page.getUrl().regex("https://mp.weixin.qq.com/cgi-bin/appmsg?").match()) {
            //获取推文URL
            getAppMsgUrl(page);
        } else if(page.getUrl().regex("http://mp.weixin.qq.com/s?").match()) {
            //获取推文
           getAppMsgPage(page);
        }

    }

    private void getAppMsgPage(Page page) {

        //文章主题
        page.putField("title",page.getHtml().getDocument().title());
        //源代码
        page.putField("text",page.getHtml().toString());
        //源地址
        page.putField("addressOriginal",page.getUrl().toString());
        //获取主体内容
        page.putField("bodyContent",page.getHtml().getDocument().select("div#js_content").text());

        //公众号名称
        page.putField("author",page.getHtml().getDocument().select("a#js_name").text());

        //发表时间
        page.putField("timePost",page.getHtml()
                .regex("([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))",1)
                + "-" + page.getHtml().regex("([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))",2));
        System.out.println(page.getHtml().regex("([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))").all());

        //跟新时间
        page.putField("update",new Date());
    }

    private void getAppMsgUrl(Page page) {
        LOGGER.info("执行开始");
        LOGGER.info(page.getJson().toString());
        List<String> listUrl = new JsonPathSelector("$.app_msg_list[*].link").selectList(page.getRawText());

        if(listUrl.size() == 0) {
            LOGGER.info("无法获取链接信息 跳过！");
            page.setSkip(true);
        } else {
            List<String> time = new JsonPathSelector("$.app_msg_list[0].update_time").selectList(page.getRawText());
            for (String str : listUrl)
                LOGGER.info(str);

            //获取最近两年
            long flagYear = 6307200;
            if( ((System.currentTimeMillis()/1000) - Long.valueOf(time.get(0))) < flagYear) {

                LOGGER.info("添加下一页信息");
            }
        }
    }


    public void setCookieAndTooken(Page page) throws IOException, InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C:\\\\Program Files (x86)\\\\Google\\\\Chrome\\\\Application\\\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get("https://mp.weixin.qq.com");
        //插入用户名以及密码
        ((ChromeDriver) driver).findElementByXPath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[1]/div/span/input").clear();
        ((ChromeDriver) driver).findElementByXPath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[1]/div/span/input").sendKeys("1277146050@qq.com");
        ((ChromeDriver) driver).findElementByXPath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[2]/div/span/input").clear();
        ((ChromeDriver) driver).findElementByXPath("//*[@id=\"header\"]/div[2]/div/div/form/div[1]/div[2]/div/span/input").sendKeys("QQ06608989563.");
        //模拟登陆
        ((ChromeDriver) driver).findElementByXPath("//*[@id=\"header\"]/div[2]/div/div/form/div[4]/a").click();
        Thread.sleep(40000);

        //截屏操作
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(srcFile, new File("D:\\1.png"));

        /**
        try {
            spendMail.sendAttachedImageMail();
        } catch (MessagingException e) {
            e.printStackTrace();
            LOGGER.debug(e.getMessage());
        }**/

        Thread.sleep(20000);

        //获取cookies
       Set<Cookie> cookies = driver.manage().getCookies();

        LOGGER.info(cookies.toString());

        //构建cookies
        String str_cookies = "";
        for (Cookie cookie : cookies) {
            if (!str_cookies.equals(""))
                str_cookies += "; ";

            str_cookies += cookie.getName() + "=" + cookie.getValue();
        }
        LOGGER.info(driver.getCurrentUrl());
        LOGGER.info(str_cookies);

        this.site.addHeader("Cookie",str_cookies);
        String[] tookens =driver.getCurrentUrl().split("token=");
        this.tooken = tookens[1];

        LOGGER.info("Cookies获取成功！");

        driver.close();

        String[] account = {"北京师范大学","北京师范大学珠海分校","一格北师"};

        for (String query : account) {
            page.addTargetRequest("https://mp.weixin.qq.com/cgi-bin/searchbiz?action=search_biz&token="+this.tooken+"&lang=zh_CN&f=json&ajax=1&random=0.3311901815753633&query="+query+"&begin=0&count=5");
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new WeixinLogin()).addUrl("http://mp.weixin.qq.com")
                .thread(5).run();

    }
}
