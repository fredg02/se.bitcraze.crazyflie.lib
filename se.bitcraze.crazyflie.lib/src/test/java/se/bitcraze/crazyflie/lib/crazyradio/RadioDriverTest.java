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

package se.bitcraze.crazyflie.lib.crazyradio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class RadioDriverTest {

    private static final String NO_ACTIVE_CONNECTIONS_FOUND = "No active connections found. Please make sure at least one Crazyflie is turned on";
    private RadioDriver mRadioDriver;
    private UsbLinkJava mUsbLinkJava;

    @Before
    public void setUp() {
        mUsbLinkJava = new UsbLinkJava();
        mRadioDriver = new RadioDriver(mUsbLinkJava);
    }

    @After
    public void tearDown() {
        if (mRadioDriver != null) {
            mRadioDriver.disconnect();
            mRadioDriver = null;
        }
    }

    @Test
    public void testSendPacket() {
        List<ConnectionData> connectionDataList = mRadioDriver.scanInterface();
        if (connectionDataList.isEmpty()) {
            fail(NO_ACTIVE_CONNECTIONS_FOUND);
        } else {
            mRadioDriver.setConnectionData(connectionDataList.get(0));
            mRadioDriver.connect();
            for (int i = 0; i <= 50; i++) {
                mRadioDriver.sendPacket(CrtpPacket.NULL_PACKET);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    public void testReceivePacket() {
        List<CrtpPacket> receivedPackets = new ArrayList<>();
        List<ConnectionData> connectionDataList = mRadioDriver.scanInterface();
        if (connectionDataList.isEmpty()) {
            fail(NO_ACTIVE_CONNECTIONS_FOUND);
        } else {
            mRadioDriver.setConnectionData(connectionDataList.get(0));
            mRadioDriver.connect();
            for (int i = 0; i <= 10; i++) {
                mRadioDriver.sendPacket(CrtpPacket.NULL_PACKET);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                CrtpPacket receivePacket = mRadioDriver.receivePacket(50);
                receivedPackets.add(receivePacket);
                System.out.println("Received : " + receivePacket);
            }
        }
        if (receivedPackets.isEmpty()) {
            fail("Did not receive any packets.");
        }
    }

    @Test
    public void testScanInterface() {
        List<ConnectionData> connectionDataList = mRadioDriver.scanInterface();
        if (connectionDataList.isEmpty()) {
            fail(NO_ACTIVE_CONNECTIONS_FOUND);
        } else {
            System.out.println("Found active connections:");
            for (ConnectionData cd : connectionDataList) {
                System.out.println("Data rate: " + cd.getDataRate() + ", channel: " + cd.getChannel());
            }
        }
    }

    @Test
    public void testClose() {
        mRadioDriver.disconnect();
        assertFalse(mUsbLinkJava.isUsbConnected());
    }

}
