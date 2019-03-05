package com.qige.weixingatherplatform.spider.commons;

import com.qige.weixingatherplatform.spider.WeixinPageCrawler;
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
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;
import us.codecraft.webmagic.selector.JsonPathSelector;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author weitangzhao
 **/
@Service
@PropertySource("classpath:application.properties")
public class CrawlerImpl implements Crawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerImpl.class);

    @Value("${xm.webdrivePath}")
    private String webdrivePath;

    @Value("${xm.webdriveName}")
    private String webdriveName;

    @Value("${xm.hostURL}")
    private String hostURL;

    @Value("${xm.userName}")
    private String userName;

    @Value("${xm.userPassword}")
    private String userPassword;

    //生产随机数
    Random random = new Random();

    @Autowired
    SpendMail spendMail ;

    private String tooken = null;

    @Override
    public String setCookieAndTooken(Page page) throws InterruptedException, IOException, MessagingException {


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
        Thread.sleep(4000);

        //截屏操作
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(srcFile, new File("D:\\1.png"));



         spendMail.sendAttachedImageMail();


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


        String[] tookens =driver.getCurrentUrl().split("token=");
        this.tooken = tookens[1];

        LOGGER.info("Cookies获取成功！");

        driver.close();
        //"北京师范大学","北京师范大学珠海分校","一格北师","团聚北师"
        String[] account = {"团聚北师"};

        for (String query : account) {
            page.addTargetRequest("https://mp.weixin.qq.com/cgi-bin/searchbiz?action=search_biz&token="+this.tooken+"&lang=zh_CN&f=json&ajax=1&random=0.3311901815753633&query="+query+"&begin=0&count=5");
        }

        return str_cookies;
    }

    @Override
    public void getFakeid(Page page) {
        List<String> listFakeid = new JsonPathSelector("$.list[*].fakeid").selectList(page.getRawText());
        LOGGER.info(listFakeid.get(0));
        page.addTargetRequest("https://mp.weixin.qq.com/cgi-bin/appmsg?token=" + this.tooken + "&lang=zh_CN&f=json&ajax=1&random=0.02650980321670149&action=list_ex&begin=0&count=5&query=&fakeid=" + listFakeid.get(0)+ "&type=9");
    }


    FileCacheQueueScheduler fileCacheQueueScheduler = new FileCacheQueueScheduler("D:webmagic/cache/");
    @Override
    public void getAppPageUrl(Page page) {

        LOGGER.info("抓取文章链接：");

        if(page.getJson().toString().equals("{\"base_resp\":{\"err_msg\":\"freq control\",\"ret\":200013}}"))
        {
            LOGGER.warn("访问频繁，出现空白");
            page.addTargetRequest(page.getUrl().toString());
        }
        else
        {
            List<String> listUrl = new JsonPathSelector("$.app_msg_list[*].link").selectList(page.getRawText());
            if(listUrl.size() == 0) {
                LOGGER.info("获取该公众号全部文章链接信息 跳过！");
                page.setSkip(true);
            } else {
                List<String> time = new JsonPathSelector("$.app_msg_list[0].update_time").selectList(page.getRawText());

                for (String str : listUrl) {
                    LOGGER.info(str);
                    // page.addTargetRequest(str);
                    ///fileCacheQueueScheduler.push(new Request(msgDTO.getLink()),spider);
                    fileCacheQueueScheduler.push(new Request(str),Spider.create(new WeixinPageCrawler()));
                }

                //获取最近两年
                long flagYear = 63072000;
                LOGGER.info(time.get(0));
                if( ((System.currentTimeMillis()/1000) - Long.valueOf(time.get(0))) < flagYear) {

                    String url = page.getUrl().toString();
                    String name = "begin";
                    String accessToken = valueProcess(url);
                    url = url.replaceAll("(" + name + "=[^&]*)", name + "=" + accessToken);
                    page.addTargetRequest(url);
                    LOGGER.info("下一页URL: "+ url);

                }
            }
        }



    }

    @Override
    public void getAppPage(Page page) {
        //文章主题
     //   page.putField("title",page.getHtml().getDocument().title());
        LOGGER.warn(page.getHtml().getDocument().title());
        //源代码
    //    page.putField("text",page.getHtml().toString());
        //源地址
        page.putField("addressOriginal",page.getUrl().toString());

        //获取主体内容
    //    page.putField("bodyContent",page.getHtml().getDocument().select("div#js_content").text());

        //公众号名称
        page.putField("author",page.getHtml().getDocument().select("a#js_name").text());
     //   LOGGER.warn(page.getHtml().getDocument().select("a#js_name").text());
        //发表时间
        page.putField("timePost",page.getHtml()
                .regex("([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))",1)
                + "-" + page.getHtml().regex("([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))",2));
     //   System.out.println(page.getHtml().regex("([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))").all());

        //跟新时间
        page.putField("update",new Date());


    }


    public static String valueProcess(String url) {

        Pattern r = Pattern.compile("(begin=[^&]*)");
        Matcher string =  r.matcher(url);
        String strvalue="begin=0";
        String strvaluee[] = null;

        while (string.find()) {
            strvalue = string.group(1);
        }

        strvaluee = strvalue.split("=");
        int value = Integer.valueOf(strvaluee[1]) + 5;

        return String.valueOf(value);
    }
}
