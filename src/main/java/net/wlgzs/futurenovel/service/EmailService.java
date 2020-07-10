package net.wlgzs.futurenovel.service;

import java.util.Objects;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final MailSender mailSender;

    public EmailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String toAddress, String subject, String msgBody) throws MailException {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        if (mailSender instanceof JavaMailSenderImpl)
            mailMessage.setFrom(Objects.requireNonNull(((JavaMailSenderImpl) mailSender).getUsername()));
        mailMessage.setTo(toAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(msgBody);
        mailSender.send(mailMessage);
    }
}
