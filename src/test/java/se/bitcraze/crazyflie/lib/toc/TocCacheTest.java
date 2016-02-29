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
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;

public class TocCacheTest {

    private final static String CURRENT_CRC = "F09FB2CA";

    @Test
    public void testTocCache() {
        TocCache tocCache = new TocCache(null, "src/test");
        Toc fetchedToc = tocCache.fetch((int) Long.parseLong(CURRENT_CRC, 16), CrtpPort.LOGGING);

        if (fetchedToc != null) {
            int tocSize = fetchedToc.getTocSize();
            assertEquals(59, tocSize);
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
        int fetchedCrc = fetchedToc.getCrc();
        System.out.println("Fetched CRC: " + String.format("%08X", fetchedCrc));
        List<TocElement> fetchedElements = fetchedToc.getElements();
        System.out.println("Number of Param TOC elements (fetched): " + fetchedElements.size());

        TocCache tocCache = new TocCache(null, "src/test");
        Toc cachedToc = tocCache.fetch(fetchedCrc, CrtpPort.LOGGING);
        if (cachedToc != null) {
            List<TocElement> cachedElements = cachedToc.getElements();
            System.out.println("Number of Param TOC elements (cached): " + cachedElements.size());

            assertEquals(cachedElements.size(), fetchedElements.size());

            for(int i = 0; i < fetchedElements.size(); i++) {
                assertEquals(fetchedElements.get(i), cachedElements.get(i));
            }
        } else {
            System.out.println("TocCache is NULL.");
        }
        
    }

}
