package com.rbaciam.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.rbaciam.exception.EmailSendingException;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;
	private final String fromEmail;
	private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

	@Autowired
	public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromEmail) {
		this.mailSender = mailSender;
		this.fromEmail = fromEmail;
	}

	public void sendWelcomeEmail(String toEmail, String name) {
		try {
			logger.info("Sending welcome email to: {}", toEmail);

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(new InternetAddress(fromEmail, "Your Application"));
			helper.setTo(toEmail);
			helper.setSubject("Welcome to Our Platform!");

			String htmlContent = buildWelcomeEmailTemplate(name);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			logger.info("Welcome email sent to: {}", toEmail);
		} catch (Exception e) {
			logger.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage(), e);
			throw new EmailSendingException("Failed to send welcome email to " + toEmail, e);
		}
	}

	private String buildWelcomeEmailTemplate(String name) {
		return String.format("""
				<!DOCTYPE html>
				<html>
				<head>
				    <style>
				        body { font-family: Arial, sans-serif; line-height: 1.6; }
				        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
				        .header { color: #2c3e50; border-bottom: 1px solid #eee; padding-bottom: 10px; }
				        .footer { margin-top: 20px; font-size: 0.9em; color: #777; }
				    </style>
				</head>
				<body>
				    <div class="container">
				        <div class="header">
				            <h2>Welcome, %s!</h2>
				        </div>
				        <p>We're excited to have you onboard. Your account has been activated successfully.</p>
				        <p>You can now log in and start using the platform.</p>
				        <div class="footer">
				            <p>Best regards,<br>Your Application Team</p>
				        </div>
				    </div>
				</body>
				</html>
				""", name);
	}

	public void sendUserValidationLink(String toEmail, String name, String validationUrl) {
		try {
			logger.info("Attempting to send validation email to: {}", toEmail);

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(new InternetAddress(fromEmail, "Your Application"));
			helper.setTo(toEmail);
			helper.setSubject("Complete Your Registration");

			String htmlContent = buildValidationEmailTemplate(name, validationUrl);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			logger.info("Validation email sent successfully to: {}", toEmail);
		} catch (Exception e) {
			logger.error("Failed to send validation email to {}: {}", toEmail, e.getMessage(), e);
			throw new EmailSendingException("Failed to send validation email to " + toEmail, e);
		}
	}

	private String buildValidationEmailTemplate(String name, String validationUrl) {
		return String.format(
				"""
						<!DOCTYPE html>
						<html>
						<head>
						    <style>
						        body { font-family: Arial, sans-serif; line-height: 1.6; }
						        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
						        .header { color: #2c3e50; border-bottom: 1px solid #eee; padding-bottom: 10px; }
						        .link { background: #f0f0f0; padding: 10px; margin: 20px 0; border-radius: 5px; text-align: center; }
						        .footer { margin-top: 20px; font-size: 0.9em; color: #777; }
						    </style>
						</head>
						<body>
						    <div class="container">
						        <div class="header">
						            <h2>Welcome, %s!</h2>
						        </div>
						        <p>Your account has been created. Please verify your email and complete your registration by clicking the link below:</p>
						        <div class="link">
						            <a href="%s" target="_blank">Complete Registration</a>
						        </div>
						        <p>If you did not request this, please ignore this email.</p>
						        <div class="footer">
						            <p>Best regards,</p>
						            <p>Your Application Team</p>
						        </div>
						    </div>
						</body>
						</html>
						""",
				name, validationUrl);
	}

}