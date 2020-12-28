package com.niklasarndt.awswatchdog.services;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.niklasarndt.awswatchdog.mail.MailService;
import com.niklasarndt.awswatchdog.util.BuildInfo;
import com.niklasarndt.awswatchdog.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Niklas on 2020/12/28.
 */
public class AwsWatcher implements Runnable {

    private final MailService mailer;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AmazonS3 client;
    private String[] bucketFilter;
    private long lastCheck = 0;

    public AwsWatcher(MailService mailer) {
        this.mailer = mailer;
    }

    public void setUp() {
        PropertiesFileProvider credentials = new PropertiesFileProvider();
        Regions region = Regions.fromName(Configuration.require("AWS_REGION"));
        this.bucketFilter = Configuration.has("AWS_BUCKETS") ?
                Configuration.require("AWS_BUCKETS").split(",") : new String[0];

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
        List<String> added = Collections.synchronizedList(res.getObjectSummaries()
                .stream().filter(i -> i.getLastModified().getTime() > lastCheck)
                .map(S3ObjectSummary::getKey).collect(Collectors.toList()));

        logger.info("[{}] Found {} objects ({} added).", bucketName, res.getKeyCount(),
                added.size());

        if (added.size() == 0)
            return;

        logger.info("Building...");

        StringBuilder body = new StringBuilder();

        added.forEach(el -> body.append("<li>").append(el).append("</li>\n"));

        String subject = Configuration.require("MAIL_SUBJECT")
                .replace("%amount%", added.size() + "")
                .replace("%bucket%", bucketName);

        String content = String.format(Constants.MAIL_TEXT, bucketName, added.size(),
                body.toString(), BuildInfo.VERSION, BuildInfo.TIMESTAMP);

        mailer.send(subject, content);
    }

    @Override
    public void run() {
        if (this.lastCheck == 0 && !Configuration.DEBUG)
            this.lastCheck = System.currentTimeMillis();

        logger.debug("[Routine] Refreshing buckets...");

        try {
            if (bucketFilter.length == 0)
                client.listBuckets().forEach(i -> this.applyBucketWatch(i.getName()));
            else
                for (String bucket : bucketFilter) {
                    applyBucketWatch(bucket);
                }
        } catch (Exception e) {
            logger.error("Failed to request bucket objects", e);
        }


        this.lastCheck = System.currentTimeMillis();
    }
}
