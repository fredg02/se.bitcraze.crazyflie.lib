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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.bitcraze.crazyflie.lib.Utilities;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;


/**
 * TODO: clean up, do not fail tests if no Crazyflie is found?
 *
 */
@SuppressWarnings("java:S106")
public class CrazyradioTest {

    private Crazyradio mCrazyradio;
    private List<ConnectionData> mConnectionDataList = new ArrayList<>();

    @Before
    public void setUp() {
        mCrazyradio = new Crazyradio(new UsbLinkJava());
    }

    @After
    public void tearDown() {
        mCrazyradio.disconnect();
    }

    @Test
    public void testScanChannels() {
        mCrazyradio.setDatarate(Crazyradio.DR_250KPS);
        List<Integer> scanChannels250k = mCrazyradio.scanChannels();
        mCrazyradio.setDatarate(Crazyradio.DR_1MPS);
        List<Integer> scanChannels1m = mCrazyradio.scanChannels();
        mCrazyradio.setDatarate(Crazyradio.DR_2MPS);
        List<Integer> scanChannels2m = mCrazyradio.scanChannels();
        if (scanChannels250k.isEmpty() && scanChannels1m.isEmpty() && scanChannels2m.isEmpty()) {
            System.out.println("No active channels found. Please make sure at least one Crazyflie is turned on.");
            fail("No active channels found. Please make sure at least one Crazyflie is turned on.");
        } else {
            System.out.println("Found active channels 250k:");
            String channelString = "  Channel ";
            for (Integer i : scanChannels250k) {
                System.out.println(channelString + i);
                mConnectionDataList.add(new ConnectionData(i, Crazyradio.DR_250KPS));
            }
            System.out.println("Found active channels 1M:");
            for (Integer i : scanChannels1m) {
                System.out.println(channelString + i);
                mConnectionDataList.add(new ConnectionData(i, Crazyradio.DR_1MPS));
            }
            System.out.println("Found active channels 2M:");
            for (Integer i : scanChannels2m) {
                System.out.println(channelString + i);
                mConnectionDataList.add(new ConnectionData(i, Crazyradio.DR_2MPS));
            }
        }
    }

    @Test
    public void testSendPacket() {
        testScanChannels();
        if (!mConnectionDataList.isEmpty()) {
            ConnectionData connectionData = mConnectionDataList.get(0);
            mCrazyradio.setDatarate(connectionData.getDataRate());
            mCrazyradio.setChannel(connectionData.getChannel());
            System.out.println("Sending packet to first found Crazyflie (" + connectionData.getDataRate() + ", " + connectionData.getChannel() + ")...");
            RadioAck radioAck = mCrazyradio.sendPacket(new byte[]{(byte) 0xff});

            if(radioAck.isAck()) {
                System.out.println("isAck: " + radioAck.isAck());
                System.out.println("isPowerDet: " + radioAck.isPowerDet());
                System.out.println("getRetry: " + radioAck.getRetry());
                System.out.println("getData: " + Utilities.getByteString(radioAck.getData()));
                assertTrue(radioAck.isAck());
                assertFalse(radioAck.isPowerDet());
                assertTrue(radioAck.getData().length > 0);
            } else {
                fail("No RadioAck packet received.");
            }
        } else {
            fail("Connection data list is empty.");
        }
    }

    @Test
    public void testGetVersion() {
        float firmwareVersion = mCrazyradio.getVersion();
        if (firmwareVersion > 0.0f) {
            System.out.println("Crazyradio firmware version: " + firmwareVersion);
        } else {
            fail("Could not read Crazyradio firmware version");
        }
    }

    @Test
    public void testGetSerialNumber() {
        String serialNumber = mCrazyradio.getSerialNumber();
        //TODO: check with regex
        if (!serialNumber.isEmpty()) {
            System.out.println("Serial number: " + serialNumber);
        } else {
            fail("Could not read Crazyradio serial number");
        }
    }

}
