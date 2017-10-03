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

package se.bitcraze.crazyflie.lib.usb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;


public class UsbLinkJavaTest {

    private UsbLinkJava mUsbLinkJava;

    @Before
    public void setUp() throws Exception {
        this.mUsbLinkJava = new UsbLinkJava();
        try {
            this.mUsbLinkJava.initDevice(Crazyradio.CRADIO_VID, Crazyradio.CRADIO_PID);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws Exception {
        if(mUsbLinkJava != null && mUsbLinkJava.isUsbConnected()) {
            mUsbLinkJava.releaseInterface();
        }
    }

    @Test
    public void testConnected() {
        assertTrue(mUsbLinkJava.isUsbConnected());
    }

    @Test
    public void testGetFirmwareVersionSingleRadio() {
        float firmwareVersion = mUsbLinkJava.getFirmwareVersion();
        if (firmwareVersion > 0.0f) {
            System.out.println("Firmware version: " + firmwareVersion);
        } else {
            fail("Could not read Crazyradio firmware version");
        }
    }

    @Test
    public void testGetFirmwareVersionMutipleRadios() throws UnsupportedEncodingException, UsbDisconnectedException, UsbException {
        List<UsbDevice> usbDeviceList = mUsbLinkJava.findDevices(Crazyradio.CRADIO_VID, Crazyradio.CRADIO_PID);
        if (usbDeviceList.isEmpty()) {
            fail("No Crazyradios found");
        } else {
            System.out.println("Found Crazyradio(s):");
            for (UsbDevice usbDevice : usbDeviceList) {
                System.out.print("  Crazyradio: " + UsbLinkJava.getSerialNumber(usbDevice));
                checkFirmwareVersion(usbDevice);
            }
        }
    }

    private void checkFirmwareVersion(UsbDevice usbDevice) {
        float firmwareVersion = UsbLinkJava.getFirmwareVersion(usbDevice);
        if (firmwareVersion > 0.0f) {
            System.out.println(", firmware version: " + firmwareVersion);
        } else {
            fail("Could not read Crazyradio firmware version");
        }
    }

    @Test
    public void testSerialNumber() {
        String serialNumber = mUsbLinkJava.getSerialNumber();
        //TODO: check with regex
        if (!serialNumber.isEmpty()) {
            System.out.println("Serial number: " + serialNumber);
        } else {
            fail("Could not read Crazyradio serial number");
        }
    }

    @Test
    public void testReleaseInterface() {
        assertTrue(mUsbLinkJava.isUsbConnected());
        mUsbLinkJava.releaseInterface();
        assertFalse(mUsbLinkJava.isUsbConnected());
    }

}
