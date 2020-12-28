package com.niklasarndt.awswatchdog;

import com.niklasarndt.awswatchdog.mail.MailService;
import com.niklasarndt.awswatchdog.services.AwsWatcher;
import com.niklasarndt.awswatchdog.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public Watchdog() {
        if (mailer.isInvalid()) {
            logger.error("Invalid mail service!");
            System.exit(1);
        }
    }

    public void start() {
        if (executor != null && !executor.isShutdown()) {
            logger.info("Shutting down existing executor...");
            executor.shutdown();
        }

        executor = Executors.newScheduledThreadPool(1);
        watcher.setUp();
        int interval = Configuration.requireInt("POLLING_INTERVAL");
        logger.info("Setting polling interval to {} seconds (every {} minutes)", interval,
                interval / 60);

        executor.scheduleAtFixedRate(watcher, 0, interval, TimeUnit.SECONDS);

    }
}
