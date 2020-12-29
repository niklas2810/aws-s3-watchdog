package com.niklasarndt.awswatchdog.health;

import com.niklasarndt.awswatchdog.util.EnvHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * Created by Niklas on 2020/12/29.
 */
public class HealthchecksClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String token = EnvHelper.require("HEALTHCHECKS_ID");

    public void sendHeartbeat() {
        sendHeartbeat(EventType.SUCCESS);
    }

    public void sendHeartbeat(EventType type) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://hc-ping.com/" + token + type).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 200)
                    logger.info("healthchecks.io received {}!", type.name());
                else
                    logger.error("Healthchecks.io rejected {}: {} ({})",
                            type.name(),
                            response.code(), response.body().string());
            } catch (IOException e) {
                logger.error("Could not reach healthchecks.io", e);
            }
        } catch (Exception e) {
            logger.error("There was an issue while sending a heartbeat to healtchecks.io", e);
        }

    }

    public enum EventType {
        SUCCESS(""), FAIL("/fail"), START("/start");

        private final String path;

        EventType(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return path;
        }
    }
}
