package com.niklasarndt.awswatchdog;

import com.niklasarndt.awswatchdog.util.BuildInfo;
import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Niklas on 2020/12/25.
 */
public class Bootstrap {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);


    public static void main(String[] args) {
        logger.info("This is AWS S3 Watchdog v.{} (Build {})",
                BuildInfo.VERSION, BuildInfo.TIMESTAMP);

        Watchdog service = new Watchdog();

        activateSentryLogging();
        service.start();
    }

    private static void activateSentryLogging() {
        if (System.getenv("SENTRY_DSN") != null || System.getProperty("sentry.dsn") != null) {
            Sentry.init();
        } else
            logger.warn("Sentry is not initialized. Logging will be local only.");


        if (Sentry.isEnabled()) {
            Sentry.configureScope(scope -> {
                scope.setExtra("App Version", BuildInfo.VERSION);
                scope.setExtra("App Build Time", BuildInfo.TIMESTAMP);
                scope.setExtra("Java Vendor", System.getProperty("java.vendor"));
                scope.setExtra("Java Version", System.getProperty("java.vm.version"));
                scope.setExtra("Operating System", System.getProperty("os.name"));
            });
            logger.info("Sentry logging is active now");
        }
    }
}
