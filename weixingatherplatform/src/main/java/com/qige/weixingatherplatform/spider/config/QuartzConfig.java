package com.qige.weixingatherplatform.spider.config;

import com.qige.weixingatherplatform.spider.quartz.WeixinJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author weitangzhao
 **/
@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail WeixinJobDetail() {
        return JobBuilder.newJob(WeixinJob.class).withIdentity("weixinTask","group1").storeDurably().build();
    }

    @Bean
    public Trigger WeixinTrigger() {   //0/30 * * * * ? *
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 03 23 * * ? *");
        return TriggerBuilder.newTrigger().forJob(WeixinJobDetail())
                .withIdentity("weixinTriggle","group1")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
