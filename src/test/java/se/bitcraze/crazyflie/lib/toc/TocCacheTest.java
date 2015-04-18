package se.bitcraze.crazyflie.lib.toc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;

public class TocCacheTest {

    private final static String CURRENT_CRC = "BE353DB4";

    @Test
    public void testTocCache() {
        TocCache tocCache = new TocCache(null, "src/test");
        Toc fetchedToc = tocCache.fetch((int) Long.parseLong(CURRENT_CRC, 16));

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

    @Test
    public void testTocCacheAgainstFetchedToc() {
        final Crazyflie crazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl());

        crazyflie.clearTocCache();

        crazyflie.addConnectionListener(new TestConnectionAdapter() {});

        crazyflie.connect(CrazyflieTest.channel, CrazyflieTest.datarate);

        for (int i = 0; i < 300; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();

        Toc fetchedToc = crazyflie.getParam().getToc();
        List<TocElement> fetchedElements = fetchedToc.getElements();
        System.out.println("Number of Param TOC elements (fetched): " + fetchedElements.size());
        assertEquals(53, fetchedElements.size());

        TocCache tocCache = new TocCache(null, "src/test");
        Toc cachedToc = tocCache.fetch((int) Long.parseLong(CURRENT_CRC, 16));
        List<TocElement> cachedElements = cachedToc.getElements();
        System.out.println("Number of Param TOC elements (cached): " + cachedElements.size());
        assertEquals(53, cachedElements.size());

        for(int i = 0; i < fetchedElements.size(); i++) {
            assertEquals(fetchedElements.get(i), cachedElements.get(i));
        }
    }

}
