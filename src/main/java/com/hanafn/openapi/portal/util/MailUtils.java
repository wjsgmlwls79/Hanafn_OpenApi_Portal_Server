package com.hanafn.openapi.portal.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Random;

@Component
@Slf4j
public class MailUtils {

    private MimeMessage message;
    private MimeMessageHelper messageHelper;
    @Value("${header-img-url}")
    private String headerUrl;
    @Value("${footer-img-url}")
    private String footerUrl;

    @Value("${spring.profiles.active}")
    private String thisServer;

    @Autowired
    public JavaMailSender mailSender;

    public void initMail() {
        try {
            message = this.mailSender.createMimeMessage();
            messageHelper = new MimeMessageHelper(message, true, "UTF-8");
        }catch(Exception e) {
            e.printStackTrace();
            log.error("MailUtils initMail Error: " + e.toString());
            throw new RuntimeException("MailUtils initMail Error", e.getCause());
        }
    }

    public void setSubject(String subject){
        try {
            messageHelper.setSubject(subject);
        }catch(Exception e) {
            e.printStackTrace();
            log.error("MailUtils setSubject Error: " + e.toString());
            throw new RuntimeException("MailUtils setSubject Error", e.getCause());
        }
    }

    public void setText(String content) {
        try {
            messageHelper.setText(content, true);
        }catch(Exception e) {
            e.printStackTrace();
            log.error("MailUtils setText Error: " + e.toString());
            throw new RuntimeException("MailUtils setText Error", e.getCause());
        }
    }

    public void setFrom(String email, String name) {
        try {
            messageHelper.setFrom(email, name);
        }catch(Exception e) {
            e.printStackTrace();
            log.error("MailUtils setFrom Error: " + e.toString());
            throw new RuntimeException("MailUtils setFrom Error", e.getCause());
        }
    }

    public void setTo(String email) {
        try {
            if (thisServer.equals("production")) {
                email = AES256Util.decrypt(email);
            }

            messageHelper.setTo(email);
        }catch(Exception e) {
            e.printStackTrace();
            log.error("MailUtils setTo Error: " + e.toString());
            throw new RuntimeException("MailUtils setTo Error", e.getCause());
        }
    }

    public void addInline() {
        try {
            FileSystemResource mailHeaderLogo = new FileSystemResource(new File(headerUrl));
            messageHelper.addInline("mail_logo.png", mailHeaderLogo);
            FileSystemResource mailFooterLogo = new FileSystemResource(new File(footerUrl));
            messageHelper.addInline("mail_footer.png", mailFooterLogo);
        }catch(Exception e) {
            e.printStackTrace();
            log.error("MailUtils addInline Error: " + e.toString());
            throw new RuntimeException("MailUtils addInline Error", e.getCause());
        }
    }

    public void send() {
        try {
            mailSender.send(message);
        }catch(Exception e) {
            e.printStackTrace();
            log.error("MailUtils send Error: " + e.toString());
            throw new RuntimeException("MailUtils send Error", e.getCause());
        }
    }

    public String makeAuthNum() {

        Random rand = new Random();
        String numStr = "";

        for(int i=0;i<6;i++) {
            String ran = Integer.toString(rand.nextInt(10));
            numStr += ran;
        }

        return numStr;
    }
}
