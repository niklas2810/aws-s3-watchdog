package com.niklasarndt.awswatchdog.services;

/**
 * Created by Niklas on 2020/12/28.
 */
public class Constants {

    public static final String MAIL_TEXT = "Hey there!<br><br>" +
            "This is an automated from AWS S3 Watchdog. This message has been sent " +
            "to inform you that the S3 Bucket <b>%s</b> received <i>%d</i> new object(s).<br><br>" +
            "Here's a quick summary:<br><ul>%s</ul>" +
            "Goodbye \uD83D\uDE80<br>AWS S3 Watchdog v.%s (Build date: %s)";
}
