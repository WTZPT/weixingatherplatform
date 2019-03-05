package com.qige.weixingatherplatform.spider.quartz;

import com.qige.weixingatherplatform.spider.WeixinCrawler;
import com.qige.weixingatherplatform.spider.WeixinPageCrawler;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.FileCacheQueueScheduler;

/**
 * @Author weitangzhao
 **/
@Component
public class WeixinJob extends QuartzJobBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(WeixinJob.class);

    @Autowired
    WeixinCrawler weixinCrawler;

    @Autowired
    WeixinPageCrawler weixinPageCrawler;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOGGER.info("任务开始");

        Spider.create(weixinCrawler).addUrl("http://mp.weixin.qq.com")
                .thread(1).run();


        Spider.create(weixinPageCrawler).setScheduler(new FileCacheQueueScheduler("D:webmagic/cache/"))
                .thread(5).run();

        LOGGER.info("任务结束");
    }
}
