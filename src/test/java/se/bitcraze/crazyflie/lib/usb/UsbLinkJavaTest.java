package se.bitcraze.crazyflie.lib.usb;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class UsbLinkJavaTest {

    private UsbLinkJava mUsbLinkJava;

    @Before
    public void setUp() throws Exception {
        mUsbLinkJava = new UsbLinkJava();
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
        List<UsbDevice> usbDeviceList = mUsbLinkJava.findDevices();
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
