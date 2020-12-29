package com.niklasarndt.awswatchdog.services;

import com.niklasarndt.awswatchdog.util.NameConstants;

/**
 * Created by Niklas on 2020/12/28.
 */
public class MailConstants {

    public static final String MAIL_TEXT = "Hey there!<br><br>" +
            "This is an automated from " + NameConstants.INSTANCE_NAME + ". This message has been sent " +
            "to inform you that the S3 Bucket <b>%s</b> received <i>%d</i> new object(s).<br><br>" +
            "Here's a quick summary:<br><ul>%s</ul>" +
            "Goodbye \uD83D\uDE80<br>" + NameConstants.INSTANCE_NAME + " v.%s (Build date: %s)";
}
