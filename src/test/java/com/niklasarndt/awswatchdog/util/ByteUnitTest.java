package com.niklasarndt.awswatchdog.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ByteUnitTest {

    @Test
    public void testUnitSelection() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ByteUnit.findForBytes(-1));
        Assertions.assertEquals(ByteUnit.B, ByteUnit.findForBytes(0));
        Assertions.assertEquals(ByteUnit.B, ByteUnit.findForBytes(999));
        Assertions.assertEquals(ByteUnit.KB, ByteUnit.findForBytes(1000L));
        Assertions.assertEquals(ByteUnit.MB, ByteUnit.findForBytes(1000L * 1000));
        Assertions.assertEquals(ByteUnit.GB, ByteUnit.findForBytes(1000L * 1000 * 1000));
        Assertions.assertEquals(ByteUnit.TB, ByteUnit.findForBytes(1000L * 1000 * 1000 * 1000));
        Assertions.assertEquals(ByteUnit.PB, ByteUnit.findForBytes(1000L * 1000 * 1000 * 1000 * 1000));
    }

    @Test
    public void testShortNames() {
        Assertions.assertEquals(ByteUnit.toShortText(1000), "1KB");
        Assertions.assertEquals(ByteUnit.toShortText(2340), "2.34KB");
        Assertions.assertEquals(ByteUnit.toShortText(23400 * 100), "2.34MB");
    }

    @Test
    public void testLongNames() {
        Assertions.assertEquals(ByteUnit.toLongText(0), "0 Byte");
        Assertions.assertEquals(ByteUnit.toLongText(1), "1 Byte");
        Assertions.assertEquals(ByteUnit.toLongText(2), "2 Bytes");

        Assertions.assertEquals(ByteUnit.toLongText(1340), "1.34 Kilobyte");
        Assertions.assertEquals(ByteUnit.toLongText(1990), "1.99 Kilobyte");
        Assertions.assertEquals(ByteUnit.toLongText(2000), "2 Kilobytes");

    }
}
