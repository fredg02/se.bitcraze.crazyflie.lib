/**
 *    ||          ____  _ __
 * +------+      / __ )(_) /_______________ _____  ___
 * | 0xBC |     / __  / / __/ ___/ ___/ __ `/_  / / _ \
 * +------+    / /_/ / / /_/ /__/ /  / /_/ / / /_/  __/
 *  ||  ||    /_____/_/\__/\___/_/   \__,_/ /___/\___/
 *
 * Copyright (C) 2015 Bitcraze AB
 *
 * Crazyflie Nano Quadcopter Client
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package se.bitcraze.crazyflie.lib.toc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.TestUtilities;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;

@SuppressWarnings("java:S106")
public class TocCacheTest {

    private static final String TEST_DIR = "src/test";
    private static final Map<String, Integer> loggingMap = new HashMap<>();
    private static final Map<String, Integer> parameterMap = new HashMap<>();

    static {
        // Firmware version ??
        loggingMap.put("7508BC21", 180);
        parameterMap.put("2DB36E98", 131);

        // Firmware version 2018.01
        loggingMap.put("22A2A8DA", 224);
        parameterMap.put("355AE774", 141);
    }

    private List<TocElement> fetchedElements = new ArrayList<>();
    private List<TocElement> cachedElements = new ArrayList<>();

    @Test
    public void testTocCacheLOGGING() {
        testTocCache("22A2A8DA", CrtpPort.LOGGING);
    }

    @Test
    public void testTocCachePARAMETERS() {
        testTocCache("355AE774", CrtpPort.PARAMETERS);
    }

    public void testTocCache(String crc, CrtpPort port) {
        int tocSize = (port == CrtpPort.LOGGING) ? loggingMap.get(crc) : parameterMap.get(crc);

        TocCache tocCache = new TocCache(new File(TEST_DIR));
        Toc fetchedToc = tocCache.fetch((int) Long.parseLong(crc, 16), port);

        if (fetchedToc != null) {
            int fetchedTocSize = fetchedToc.getTocSize();
            assertEquals(tocSize, fetchedTocSize);
            for (TocElement te : fetchedToc.getElements()) {
                System.out.println(te.getIdent() + " " + te);
            }
        } else {
            fail("fetchedToc is null");
        }
    }

    @Test
    public void testParamTocCacheAgainstFetchedToc() {
        testTocCacheAgainstFetchedToc(CrtpPort.PARAMETERS);
    }

    @Test
    public void testLogTocCacheAgainstFetchedToc() {
        testTocCacheAgainstFetchedToc(CrtpPort.LOGGING);
    }

    private void testTocCacheAgainstFetchedToc(final CrtpPort port) {

        if (!TestUtilities.isCrazyradioAvailable()) {
            fail("Crazyradio not connected");
        }

        final Crazyflie crazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl(), new File(TEST_DIR));

//        crazyflie.clearTocCache();

        crazyflie.getDriver().addConnectionListener(new TestConnectionAdapter() {

            @Override
            public void setupFinished() {
                //TODO: force fetching it from copter
                Toc fetchedToc = port == CrtpPort.PARAMETERS ? crazyflie.getParam().getToc() : crazyflie.getLogg().getToc();
                if (fetchedToc != null) {
                    int fetchedCrc = fetchedToc.getCrc();
                    System.out.println("Fetched " + port.name() + " CRC: " + String.format("%08X", fetchedCrc));
                    fetchedElements = fetchedToc.getElements();
                   System.out.println("Number of " + port.name() + " TOC elements (fetched): " + fetchedElements.size());

                    TocCache tocCache = new TocCache(new File(TEST_DIR));
                    Toc cachedToc = tocCache.fetch(fetchedCrc, CrtpPort.PARAMETERS);
                    if (cachedToc != null) {
                        cachedElements = cachedToc.getElements();
                        System.out.println("Number of " + port.name() + " TOC elements (cached): " + cachedElements.size());
                    } else {
                        fail("TocCache is NULL.");
                    }
                } else {
                    fail("FetchedToc is NULL!");
                }
            }
        });

        crazyflie.setConnectionData(new ConnectionData(CrazyflieTest.CHANNEL, CrazyflieTest.DATARATE));
        crazyflie.connect();

        for (int i = 0; i < 300; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();

        assertTrue("Number of cached elements must be bigger than zero.", !cachedElements.isEmpty());
        assertTrue("Number of fetched elements must be bigger than zero.", !fetchedElements.isEmpty());
        assertEquals(cachedElements.size(), fetchedElements.size());

        for(int i = 0; i < fetchedElements.size(); i++) {
            assertEquals(fetchedElements.get(i), cachedElements.get(i));
        }
    }

}
