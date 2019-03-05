package com.qige.weixingatherplatform.spider.config;

import com.qige.weixingatherplatform.spider.WeixinPageCrawler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

/**
 * @Author weitangzhao
 **/

public class WebmagicConfig {

    public FileCacheQueueScheduler fileCacheQueueScheduler() {
        return new FileCacheQueueScheduler("D:webmagic/cache/");
    }

    public Spider spider(){
       return Spider.create(new WeixinPageCrawler()).setScheduler(fileCacheQueueScheduler())
                .thread(5);
    }

}
