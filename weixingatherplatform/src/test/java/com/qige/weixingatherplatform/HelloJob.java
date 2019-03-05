package com.qige.weixingatherplatform;

import org.junit.runner.RunWith;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author weitangzhao
 **/

public class HelloJob implements Job {

    static  final Logger LOGGER = LoggerFactory.getLogger(HelloJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
                    LOGGER.info("Hell0 Thiz is a Job!");
        JobKey key = jobExecutionContext.getJobDetail().getKey();
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();

        String mystr = jobDataMap.getString("MyString");
        Double value = jobDataMap.getDouble("MyfloatValue");
        System.err.println("Instance " + key + " of DumbJob says: " + mystr + ", and val is: " + value);
    }

}
