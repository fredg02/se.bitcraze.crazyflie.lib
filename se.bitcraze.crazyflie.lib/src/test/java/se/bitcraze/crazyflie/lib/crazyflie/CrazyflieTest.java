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

package se.bitcraze.crazyflie.lib.crazyflie;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.MockDriver;
import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.TestUtilities;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

@SuppressWarnings("java:S106")
public class CrazyflieTest {

    public static final int CHANNEL = 80;
    public static final int DATARATE = 0;

    private boolean connectionRequested = false;
    private boolean connected = false;
    private boolean setupFinished = false;
//    private boolean connectionFailed = false;
//    private boolean connectionLost = false;
    private boolean disconnected = false;
    private boolean linkQuality = false;

    public static CrtpDriver getConnectionImpl() {
        CrtpDriver mDriver = null;
        if (TestUtilities.isCrazyradioAvailable()) {
            mDriver = new RadioDriver(new UsbLinkJava());
        } else {
            mDriver = new MockDriver();
        }
        return mDriver;
    }

    @Test
    public void testDataListener() {
        System.out.println("=== TEST START - testDataListener() ===");
        Crazyflie crazyflie = new Crazyflie(getConnectionImpl());

        final ArrayList<CrtpPacket> packetList = new ArrayList<>();

        //FIXME: only works right after start up
        crazyflie.addDataListener(new DataListener(CrtpPort.CONSOLE) {

            @Override
            public void dataReceived(CrtpPacket packet) {
                packetList.add(packet);
                System.out.println("Received " + packet);
            }

        });

        crazyflie.setConnectionData(new ConnectionData(CHANNEL, DATARATE));
        crazyflie.connect();

        for (int i = 0; i < 15; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
            try {
                Thread.sleep(50, 0);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();

        assertFalse("PacketList should not be empty", packetList.isEmpty());
        System.out.println("=== TEST END - testDataListener() ===\n");
    }

    @Test
    public void testConnectionListener() {
        System.out.println("=== TEST START - testConnectionListener() ===");
        final Crazyflie crazyflie = new Crazyflie(getConnectionImpl());

        crazyflie.getDriver().addConnectionListener(new TestConnectionAdapter() {

            @Override
            public void connectionRequested() {
                super.connectionRequested();
                connectionRequested = true;
            }

            @Override
            public void connected() {
                super.connected();
                connected = true;
            }

            @Override
            public void setupFinished() {
                super.setupFinished();
                setupFinished = true;
            }

            @Override
            public void disconnected() {
                super.disconnected();
                disconnected = true;
            }

            @Override
            public void linkQualityUpdated(int percent) {
//                System.out.println("LINK QUALITY: " + percent);
                linkQuality = true;
            }

        });

        crazyflie.setConnectionData(new ConnectionData(CHANNEL, DATARATE));
        crazyflie.connect();

        int timeout = 1000;

        while (!setupFinished && timeout > 0) {
            try {
                Thread.sleep(50, 0);
            } catch (InterruptedException e) {
                break;
            }
            timeout -= 50;
        }

        crazyflie.disconnect();

        assertTrue(connectionRequested);
        assertTrue(connected);
        assertTrue(setupFinished);
        assertTrue(disconnected);
        assertTrue(linkQuality);
        System.out.println("=== TEST END - testConnectionListener() ===\n");
    }
}
