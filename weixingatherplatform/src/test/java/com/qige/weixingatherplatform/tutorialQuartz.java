package com.qige.weixingatherplatform;

import org.junit.Before;
import org.quartz.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @Author weitangzhao
 **/
public class tutorialQuartz {

   public static  void main(String[] args) throws SchedulerException {
       SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();

       Scheduler sched = schedFact.getScheduler();

       sched.start();

       // define the job and tie it to our HelloJob class
       JobDetail job = newJob(HelloJob.class)
               .withIdentity("myJob", "group1")
               .usingJobData("MyfloatValue",3.14)
               .usingJobData("MyString","定时执行")
               .build();

       // Trigger the job to run now, and then every 40 seconds
       Trigger trigger = newTrigger()
               .withIdentity("myTrigger", "group1")
               .startNow()  //定义即时执行
               .withSchedule(simpleSchedule()
                       .withIntervalInSeconds(40)   //每40秒执行一次
                       .repeatForever())
               .build();

       // Tell quartz to schedule the job using our trigger
       sched.scheduleJob(job, trigger);
   }
}
