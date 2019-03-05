package com.qige.weixingatherplatform.spider.commons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Component
public class SpendMail {

    @Autowired
    private JavaMailSenderImpl mailSender;

    public void sendAttachedImageMail() throws MessagingException {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

        // multipart模式
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        // 对应发送服务器的地址
        mimeMessageHelper.setTo("1277146050@qq.com");
        // 对应接受邮件地址
        mimeMessageHelper.setFrom("1277146050@qq.com");
        // 设置主题
        mimeMessageHelper.setSubject("微信公众号 【图片】");

        // 构建HTML代码
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head></head>");
        sb.append("<body><h1>登录验证</h1><p>请在一个小时内验证。</p>");
        // cid为固定写法，imageId指定一个标识
        sb.append("<img src=\"cid:imageId\"/></body>");
        sb.append("</html>");

        // 启用html
        mimeMessageHelper.setText(sb.toString(), true);

        // 设置imageId
        FileSystemResource img = new FileSystemResource(new File("D:/1.png"));
        mimeMessageHelper.addInline("imageId", img);

        // 发送邮件
        mailSender.send(mimeMessage);
    }
}
