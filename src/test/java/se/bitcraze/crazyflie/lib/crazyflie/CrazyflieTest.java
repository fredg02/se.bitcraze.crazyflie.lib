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

import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

//TODO: use MockDriver if no real Crazyflie is available
public class CrazyflieTest {

    public static int channel = 10;
    public static int datarate = 0;

    private boolean connectionRequested = false;
    private boolean connected = false;
    private boolean setupFinished = false;
//    private boolean connectionFailed = false;
//    private boolean connectionLost = false;
    private boolean disconnected = false;
    private boolean linkQuality = false;

    public static CrtpDriver getConnectionImpl() {
        return new RadioDriver(new UsbLinkJava());
    }

    @Test
    public void testDataListener() {
        Crazyflie crazyflie = new Crazyflie(getConnectionImpl());

        final ArrayList<CrtpPacket> packetList = new ArrayList<CrtpPacket>();
        
        crazyflie.addDataListener(new DataListener(CrtpPort.CONSOLE) {

            @Override
            public void dataReceived(CrtpPacket packet) {
                packetList.add(packet);
                System.out.println("Received " + packet);
            }

        });

        crazyflie.connect(channel, datarate);

        for (int i = 0; i < 30; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
            try {
                Thread.sleep(50, 0);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();
        
        assertFalse("PacketList should not be empty", packetList.isEmpty());
    }

    @Test
    public void testConnectionListener() {
        final Crazyflie crazyflie = new Crazyflie(getConnectionImpl());

        crazyflie.getDriver().addConnectionListener(new TestConnectionAdapter() {

            @Override
            public void connectionRequested(String connectionInfo) {
                super.connectionRequested(connectionInfo);
                connectionRequested = true;
            }

            @Override
            public void connected(String connectionInfo) {
                super.connected(connectionInfo);
                connected = true;
            }

            @Override
            public void setupFinished(String connectionInfo) {
                super.setupFinished(connectionInfo);
                setupFinished = true;
            }

            @Override
            public void disconnected(String connectionInfo) {
                super.disconnected(connectionInfo);
                disconnected = true;
            }

            public void linkQualityUpdated(int percent) {
                System.out.println("LINK QUALITY: " + percent);
                linkQuality = true;
            }

        });

        crazyflie.connect(channel, datarate);

        int timeout = 5000;
        
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
    }
}
