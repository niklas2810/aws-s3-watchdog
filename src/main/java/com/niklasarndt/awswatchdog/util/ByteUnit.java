package com.niklasarndt.awswatchdog.util;

import java.util.Locale;

public enum ByteUnit {


    B(1, "Byte"),
    KB(B.conversionUnit * 1000, "Kilobyte"),
    MB(KB.conversionUnit * 1000, "Megabyte"),
    GB(MB.conversionUnit * 1000, "Gigabyte"),
    TB(GB.conversionUnit * 1000, "Terabyte"),
    PB(TB.conversionUnit * 1000, "Petabyte");


    public final long conversionUnit;
    public final String longName;

    ByteUnit(long conversionUnit, String longName) {
        this.conversionUnit = conversionUnit;
        this.longName = longName;
    }


    private double bytesToUnitValue(long bytes) {
        return (double) bytes / conversionUnit;
    }

    private String createReadableValueForUnitValue(double unitValue) {
        return Math.floor(unitValue) == unitValue ? ((long)unitValue)+"" : String.format(Locale.ROOT, "%.2f", unitValue);
    }

    private String createReadableValue(long bytes)
    {
        return createReadableValueForUnitValue(bytesToUnitValue(bytes));
    }

    public String formatBytesWithLongName(long bytes) {
        return createReadableValue(bytes) + name();
    }

    public String formatBytesWithShortName(long bytes) {
        String name = bytesToUnitValue(bytes) < 2 ? longName : longName+"s";
        return createReadableValue(bytes) + " " + name;
    }

    public static String toShortText(long bytes) {
        return findForBytes(bytes).formatBytesWithLongName(bytes);
    }

    public static String toLongText(long bytes) {
        return findForBytes(bytes).formatBytesWithShortName(bytes);
    }

    public static ByteUnit findForBytes(long bytes) {
        if(bytes < 0)
            throw new IllegalArgumentException("Cannot process negative size");

        for (int i = ByteUnit.values().length - 1; i >= 0; i--) {
            ByteUnit unit = ByteUnit.values()[i];

            if(bytes >= unit.conversionUnit)
                return unit;
        }

        return B;
    }
}
