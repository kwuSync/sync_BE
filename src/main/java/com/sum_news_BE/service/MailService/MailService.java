package com.sum_news_BE.service.MailService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender javaMailSender;
	private final MailVerificationService mailVerificationService;
	
	@Value("${spring.mail.username}")
	private String senderEmail;
	
	private static int number;

	public static void createNumber() {
		number = (int)(Math.random() * 8999) + 1000;
	}

	public MimeMessage createMail(String mail) {
		createNumber();
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();

		try {
			mimeMessage.setFrom(senderEmail);
			mimeMessage.setRecipients(MimeMessage.RecipientType.TO, mail);
			mimeMessage.setSubject("이메일 인증");

			String body = "";
			body += "<h2 style='font-size: 30px;'>이메일 인증 번호 입니다.</h2>";
			body += "<p>" + number + "</p>";
			body += "<p>" + "인증번호를 입력하여 이메일 인증을 완료할 수 있습니다." + "</p>";
			mimeMessage.setText(body, "UTF-8", "html");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return mimeMessage;
	}

	public int sendMail(String mail) {
		MimeMessage mimeMessage = createMail(mail);
		javaMailSender.send(mimeMessage);
		mailVerificationService.saveAuthNumber(mail, number);
		return number;
	}
}
