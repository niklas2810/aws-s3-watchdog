package com.niklasarndt.awswatchdog.util;

/**
 * Created by Niklas on 2020/12/29.
 */
public class NameConstants {

    public static final String INSTANCE_NAME =
            EnvHelper.get("INSTANCE_NAME", BuildConstants.NAME);
}
