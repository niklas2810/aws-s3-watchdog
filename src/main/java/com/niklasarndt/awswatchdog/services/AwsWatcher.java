package com.niklasarndt.awswatchdog.services;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.niklasarndt.awswatchdog.health.HealthchecksClient;
import com.niklasarndt.awswatchdog.mail.MailService;
import com.niklasarndt.awswatchdog.util.BuildConstants;
import com.niklasarndt.awswatchdog.util.EnvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Niklas on 2020/12/28.
 */
public class AwsWatcher implements Runnable {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMMMM dd yyyy HH:mm:ss z", Locale.US);

    private final MailService mailer;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HealthchecksClient health = EnvHelper.has("HEALTHCHECKS_ID") ?
            new HealthchecksClient() : null;

    private AmazonS3 client;
    private String[] bucketFilter;
    private long lastCheck = EnvHelper.DEBUG ? 0 : System.currentTimeMillis();

    public AwsWatcher(MailService mailer) {
        this.mailer = mailer;
        if (health != null)
            logger.info("Reporting to healthchecks.io has been set up!");
    }

    public void setUp() {
        Regions region = Regions.fromName(EnvHelper.require("AWS_REGION"));
        this.bucketFilter = EnvHelper.has("AWS_BUCKETS") ?
                EnvHelper.require("AWS_BUCKETS").split(",") : new String[0];

        this.client = AmazonS3Client.builder()
                .withCredentials(new PropertiesFileProvider()).withRegion(region).build();

        logger.debug("Region {} [{}] will be watched!",
                region.getName(), region.getDescription());
        if (bucketFilter.length > 0)
            logger.debug("Watchdog is limited to the following buckets: {}",
                    Arrays.toString(bucketFilter));
    }

    public boolean watchesBucket(String bucketName) {
        if (bucketFilter == null || bucketFilter.length == 0)
            return true;

        for (String name : bucketFilter)
            if (name.equals(bucketName))
                return true;

        return false;
    }

    public boolean watchesBucket(Bucket bucket) {
        return watchesBucket(bucket.getName());
    }

    private void applyBucketWatch(String bucketName) {
        ListObjectsV2Result res = client.listObjectsV2(bucketName);
        List<S3ObjectSummary> added = Collections.synchronizedList(res.getObjectSummaries()
                .stream().filter(i -> i.getLastModified().getTime() > lastCheck)
                .collect(Collectors.toList()));

        logger.info("[{}] Found {} objects ({} added).", bucketName, res.getKeyCount(),
                added.size());

        if (added.size() > 0)
            buildMail(bucketName, added);
    }

    private String generateListElement(S3ObjectSummary el) {
        return "<li>" + el.getKey().substring(el.getKey().lastIndexOf("/") + 1) +
                " (added: " +
                DATE_FORMAT.format(el.getLastModified()) + ")" + "</li>\n";
    }

    private void buildMail(String bucketName, List<S3ObjectSummary> added) {
        Map<String, List<S3ObjectSummary>> items = new TreeMap<>();

        for (S3ObjectSummary item : added) {
            String key = item.getKey();
            if (!key.contains("/") || key.lastIndexOf("/") == key.length() - 1) {
                items.put(key, new ArrayList<>());
                continue;
            }

            String folder = key.substring(0, key.lastIndexOf("/") + 1);

            if (items.containsKey(folder))
                items.get(folder).add(item);
            else
                items.put(folder, new ArrayList<>(Collections.singletonList(item)));
        }

        StringBuilder body = new StringBuilder();

        items.forEach((index, subelements) -> {
            if (subelements.size() == 0)
                return;

            body.append("<li>").append(index).append("<ul>\n");
            subelements.forEach(el -> body.append(generateListElement(el)));
            body.append("</ul></li>\n");
        });

        added.stream().filter(i -> !i.getKey().contains("/"))
                .forEach(el -> body.append(generateListElement(el)));

        body.append("</ul>\n");

        String subject = EnvHelper.require("MAIL_SUBJECT")
                .replace("%amount%", added.size() + "")
                .replace("%bucket%", bucketName);

        String content = String.format(MailConstants.MAIL_TEXT, bucketName, added.size(),
                body.toString(), BuildConstants.VERSION, BuildConstants.TIMESTAMP);

        mailer.send(subject, content);
    }

    @Override
    public void run() {
        if (health != null)
            health.sendHeartbeat(HealthchecksClient.EventType.START);

        logger.debug("[Routine] Refreshing buckets...");

        boolean error = false;
        try {
            if (bucketFilter.length == 0)
                client.listBuckets().forEach(i -> this.applyBucketWatch(i.getName()));
            else
                for (String bucket : bucketFilter) {
                    applyBucketWatch(bucket);
                }
        } catch (Exception e) {
            logger.error("Failed to request bucket objects", e);
            error = true;
            if (health != null)
                health.sendHeartbeat(HealthchecksClient.EventType.FAIL);
        }

        if (!error && health != null)
            health.sendHeartbeat();


        this.lastCheck = System.currentTimeMillis();
    }
}
