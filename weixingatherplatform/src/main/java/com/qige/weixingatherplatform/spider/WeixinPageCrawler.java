package com.qige.weixingatherplatform.spider;

import com.qige.weixingatherplatform.spider.commons.CrawlerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * @Author weitangzhao
 **/
@Component
public class WeixinPageCrawler  implements PageProcessor {
    @Autowired
    CrawlerImpl crawlerImpl;


    private Site site = Site.me().setRetryTimes(3).setSleepTime(4000).setTimeOut(3000)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");


    @Override
    public void process(Page page) {

         if(page.getUrl().regex("http://mp.weixin.qq.com/s?").match()) {
            crawlerImpl.getAppPage(page);
        }

    }

    @Override
    public Site getSite() {
        return site;
    }
}
