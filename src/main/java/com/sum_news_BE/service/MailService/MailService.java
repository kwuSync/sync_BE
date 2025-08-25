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

	public MimeMessage createPasswordResetMail(String mail) {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();

		try {
			mimeMessage.setFrom(senderEmail);
			mimeMessage.setRecipients(MimeMessage.RecipientType.TO, mail);
			mimeMessage.setSubject("비밀번호 재설정 인증");
			
			// 비밀번호 재설정용 인증번호 생성
			int number = (int)(Math.random() * 8999) + 1000;
			
			String body = "";
			body += "<h2 style='font-size: 30px;'>비밀번호 재설정 인증</h2>";
			body += "<p>아래 인증번호를 입력하여 비밀번호를 재설정하세요:</p>";
			body += "<p style='font-size: 20px; font-weight: bold; color: #007bff;'>" + number + "</p>";
			body += "<p>이 인증번호는 보안을 위해 안전하게 보관하시고, 타인과 공유하지 마세요.</p>";
			mimeMessage.setText(body, "UTF-8", "html");
			
			// 인증번호 저장
			mailVerificationService.saveAuthNumber(mail, number);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return mimeMessage;
	}

	public MimeMessage createSignupMail(String mail) {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();

		try {
			mimeMessage.setFrom(senderEmail);
			mimeMessage.setRecipients(MimeMessage.RecipientType.TO, mail);
			mimeMessage.setSubject("이메일 인증");
			
			// 회원가입용 인증번호 생성
			int number = (int)(Math.random() * 8999) + 1000;
			
			String body = "";
			body += "<h2 style='font-size: 30px;'>이메일 인증 번호 입니다.</h2>";
			body += "<p style='font-size: 20px; font-weight: bold; color: #007bff;'>" + number + "</p>";
			body += "<p>" + "인증번호를 입력하여 이메일 인증을 완료할 수 있습니다." + "</p>";
			mimeMessage.setText(body, "UTF-8", "html");
			
			// 인증번호 저장
			mailVerificationService.saveAuthNumber(mail, number);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return mimeMessage;
	}

	public void sendPasswordResetMail(String mail) {
		MimeMessage mimeMessage = createPasswordResetMail(mail);
		javaMailSender.send(mimeMessage);
	}

	public int sendMail(String mail) {
		MimeMessage mimeMessage = createSignupMail(mail);
		javaMailSender.send(mimeMessage);

		return 0; // 임시
	}
}
