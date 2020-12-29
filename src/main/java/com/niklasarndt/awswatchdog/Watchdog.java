package com.niklasarndt.awswatchdog;

import com.niklasarndt.awswatchdog.mail.MailService;
import com.niklasarndt.awswatchdog.services.AwsWatcher;
import com.niklasarndt.awswatchdog.util.EnvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Niklas on 2020/12/25.
 */
public class Watchdog {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final MailService mailer = new MailService();
    private final AwsWatcher watcher = new AwsWatcher(mailer);

    private ScheduledExecutorService executor;

    public void start() {
        if (mailer.isInvalid()) {
            logger.error("Email configuration is invalid, aborting startup");
            return;
        }

        if (executor != null && !executor.isShutdown()) {
            logger.info("Shutting down existing executor...");
            executor.shutdown();
        }

        executor = Executors.newScheduledThreadPool(1);
        watcher.setUp();
        int interval = EnvHelper.requireInt("POLLING_INTERVAL");
        if (interval <= 0) {
            logger.error("The polling interval must be larger than 0!");
            return;
        }
        logger.info("Setting polling interval to {} seconds (every {} minutes)", interval,
                interval / 60);

        boolean startImmediately = !EnvHelper.has("START_IMMEDIATELY")
                || !EnvHelper.require("START_IMMEDIATELY").equals("false");

        int delay = startImmediately ? 0 : calculateStartDelay(interval);
        executor.scheduleAtFixedRate(watcher, delay, interval, TimeUnit.SECONDS);

    }

    private int calculateStartDelay(int secondInterval) {
        ZoneOffset offset = OffsetDateTime.now().getOffset
                ();
        long start = LocalDate.now().atStartOfDay().toInstant(offset).toEpochMilli();
        long iter = secondInterval * 1000;
        while (start < System.currentTimeMillis())
            start += iter;

        ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(start),
                ZoneId.systemDefault());
        int res = (int) ((start - System.currentTimeMillis()) / 1000);

        logger.info("Starting AWS S3 Watchdog at {} (in {} seconds)!",
                startTime.toString(), res);
        return res;
    }
}
