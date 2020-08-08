/*
 *  Copyright (C) 2020 Future Studio
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
