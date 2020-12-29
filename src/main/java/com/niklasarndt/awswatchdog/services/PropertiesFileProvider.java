package com.niklasarndt.awswatchdog.services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.niklasarndt.awswatchdog.util.EnvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Niklas on 2020/12/28.
 */
public class PropertiesFileProvider implements AWSCredentialsProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public AWSCredentials getCredentials() {
        return new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return EnvHelper.require("AWS_ACCESS_KEY_ID");
            }

            @Override
            public String getAWSSecretKey() {
                return EnvHelper.require("AWS_SECRET_ACCESS_KEY");
            }
        };
    }

    @Override
    public void refresh() {
        logger.debug("Skipping credential reload (provided via environment variables)");
    }
}
