package com.niklasarndt.awswatchdog.mail;

import static com.niklasarndt.awswatchdog.util.Configuration.require;
import static com.niklasarndt.awswatchdog.util.Configuration.requireInt;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.mail.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Niklas on 2020/12/25.
 */
public class MailService {


    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final String host;
    private final int port;
    private final String from;
    private final Recipient[] to;
    private final String username;
    private final String password;

    private boolean invalid = false;

    private Mailer mailer;

    public MailService() {
        host = require("MAIL_HOST");
        port = requireInt("MAIL_PORT");
        from = require("MAIL_FROM");
        List<Recipient> recipients = new ArrayList<>();
        String[] addresses = require("MAIL_TO").split(",");
        for (String address : addresses) {
            recipients.add(new Recipient(address, address,
                    Message.RecipientType.TO));
        }
        to = recipients.toArray(new Recipient[0]);
        username = require("MAIL_USERNAME");
        password = require("MAIL_PASSWORD");

        if (invalid) return;

        mailer = MailerBuilder.withSMTPServer(host, port, username, password)
                .withTransportStrategy(TransportStrategy.SMTP_TLS).buildMailer();

        logger.info("Success! Mail service is ready to go.");
    }

    public void send(String subject, String message) {
        try {
            EmailPopulatingBuilder builder = EmailBuilder.startingBlank()
                    .to(to)
                    .from("AWS S3 Watchdog", from)
                    .withSubject(subject).withHTMLText(message);


            send(builder.buildEmail(), true);
        } catch (Exception e) {
            logger.error("Could not build email!", e);
        }

    }

    public void send(Email mail, boolean async) {
        if (invalid || mailer == null) {
            logger.warn("Trying to send mail with invalid mail configuration!");
            return;
        }

        Runnable r = () -> {
            logger.info("Sending email...");
            try {
                mailer.sendMail(mail, false);
            } catch (Exception e) {
                logger.error("Failed to send email!", e);
            }

        };

        if (async)
            executor.submit(r);
        else
            r.run();

    }

    public boolean isInvalid() {
        return invalid;
    }
}
