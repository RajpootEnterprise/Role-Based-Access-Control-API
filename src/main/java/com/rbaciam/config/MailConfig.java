package com.rbaciam.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.rbaciam.service.impl.EmailService;

@Configuration
public class MailConfig {
	
	@Bean
    public EmailService emailService(JavaMailSender mailSender,
                                   @Value("${spring.mail.username}") String fromEmail) {
        return new EmailService(mailSender, fromEmail);
    }

	@Bean
	public JavaMailSender javaMailSender(
	        @Value("${spring.mail.host}") String host,
	        @Value("${spring.mail.port}") int port,
	        @Value("${spring.mail.username}") String username,
	        @Value("${spring.mail.password}") String password) {
	    
	    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost(host);
	    mailSender.setPort(port);
	    mailSender.setUsername(username);
	    mailSender.setPassword(password);
	    
	    Properties props = mailSender.getJavaMailProperties();
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.smtp.starttls.required", "true");
	    props.put("mail.smtp.connectiontimeout", 5000);
	    props.put("mail.smtp.timeout", 5000);
	    props.put("mail.smtp.writetimeout", 5000);
	    
	    
	    props.put("mail.debug", "true");
	    
	    return mailSender;
	}
}

