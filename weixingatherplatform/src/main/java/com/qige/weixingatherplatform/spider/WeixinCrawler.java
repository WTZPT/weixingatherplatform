package com.qige.weixingatherplatform.spider;

import com.qige.weixingatherplatform.spider.commons.Crawler;
import com.qige.weixingatherplatform.spider.commons.CrawlerImpl;
import com.qige.weixingatherplatform.spider.commons.SpendMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.IOException;
import java.util.Random;

/**
 * @Author weitangzhao
 **/
@Component
public class WeixinCrawler implements PageProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeixinCrawler.class);

    @Autowired
    CrawlerImpl crawlerImpl;


    private Site site = Site.me().setRetryTimes(3).setTimeOut(3000).setSleepTime(90000)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");

    @Override
    public void process(Page page) {

        if(page.getUrl().toString().equals("http://mp.weixin.qq.com")) {
            try {
                String str_cookies = crawlerImpl.setCookieAndTooken(page);
                this.site.addHeader("Cookie",str_cookies);
                page.setSkip(true);
            } catch (InterruptedException e) {
                e.printStackTrace();
                LOGGER.info(e.getMessage());
            } catch (IOException e) {

                e.printStackTrace();
                LOGGER.info(e.getMessage());
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
                LOGGER.error(e.getMessage());
            }
        }  else if(page.getUrl().regex("https://mp.weixin.qq.com/cgi-bin/searchbiz?").match()) {
            crawlerImpl.getFakeid(page);
        }  else if(page.getUrl().regex("https://mp.weixin.qq.com/cgi-bin/appmsg?").match()){
            crawlerImpl.getAppPageUrl(page);
        } else if(page.getUrl().regex("http://mp.weixin.qq.com/s?").match()) {
            crawlerImpl.getAppPage(page);
        }



    }

    @Override
    public Site getSite() {
        return site;
    }
}
