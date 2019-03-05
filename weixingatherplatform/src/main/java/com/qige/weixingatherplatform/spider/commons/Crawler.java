package com.qige.weixingatherplatform.spider.commons;

import us.codecraft.webmagic.Page;

import javax.mail.MessagingException;
import java.io.IOException;

public interface Crawler {

    /**
     *  登录获取token 和 cookie
     */
    public String  setCookieAndTooken(Page page) throws InterruptedException, IOException, MessagingException;

    /**
     * 获取公众号fakeid
     */
    public void getFakeid(Page page);

    /**
     * 获取公众号推文URL
     */
    public void getAppPageUrl(Page page);

    /**
     * 爬取对应文章
     */
    public void getAppPage(Page page);

}
