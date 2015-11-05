package se.bitcraze.crazyflie.lib.bootloader;

import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 * Bootloading utilities for the Crazyflie.
 *
 */
public class Utilities {

    public static class BootVersion {
        public final static int CF1_PROTO_VER_0 = 0x00;
        public final static int CF1_PROTO_VER_1 = 0x01;
        public final static int CF2_PROTO_VER = 0x10;

        public static String toVersionString(int ver) {
            if (ver == BootVersion.CF1_PROTO_VER_0 || ver == BootVersion.CF1_PROTO_VER_1) {
                return "Crazyflie Nano Quadcopter (1.0)";
            } else if (ver == BootVersion.CF2_PROTO_VER) {
                return "Crazyflie 2.0";
            }
            return "Unknown";
        }
    }

    public static String getHexString(byte... array) {
        StringBuffer sb = new StringBuffer();
        for (byte b : array) {
            sb.append(String.format("0x%02X", b));
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Strip bytes of the beginning of an array
     *
     * @param array
     * @param offset
     * @return
     */
    public static byte[] strip(byte[] array, int offset) {
        byte[] strippedArray = new byte[array.length-offset];
        System.arraycopy(array, offset, strippedArray, 0, strippedArray.length);
        return strippedArray;
    }


    public static boolean isCrazyradioAvailable() {
        try {
            UsbServices services = UsbHostManager.getUsbServices();
            UsbHub rootHub = services.getRootUsbHub();
            List<UsbDevice> usbDeviceList = UsbLinkJava.findUsbDevices(rootHub, (short) Crazyradio.CRADIO_VID, (short) Crazyradio.CRADIO_PID);
            return !usbDeviceList.isEmpty();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (UsbException e) {
            e.printStackTrace();
        }
        return false;
    }
}
