package net.wlgzs.futurenovel.service;

import java.util.Objects;
import javax.mail.MessagingException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String toAddress, String subject, String msgBody) throws MailException, MessagingException {
        var mimeMessage = mailSender.createMimeMessage();
        var mailMessageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");
        if (mailSender instanceof JavaMailSenderImpl)
            mailMessageHelper.setFrom(Objects.requireNonNull(((JavaMailSenderImpl) mailSender).getUsername()));
        mailMessageHelper.setTo(toAddress);
        mailMessageHelper.setSubject(subject);
        mailMessageHelper.setText(msgBody, true);
        mailSender.send(mimeMessage);
    }
}
