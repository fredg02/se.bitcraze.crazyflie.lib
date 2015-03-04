package se.bitcraze.crazyflie.lib.toc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TocCacheTest {

    @Test
    public void testTocCache() {
        String crc = "BE353DB4";
        TocCache tocCache = new TocCache(null, "src/test");
        Toc fetchedToc = tocCache.fetch((int) Long.parseLong(crc, 16));

        if (fetchedToc != null) {
            int tocSize = fetchedToc.getTocSize();
            assertEquals(53, tocSize);
            for(int i = 0; i < tocSize; i++) {
                TocElement elementById = fetchedToc.getElementById(i);
                System.out.println(elementById);
            }
        } else {
            fail("fetchedToc is null");
        }
    }

}
