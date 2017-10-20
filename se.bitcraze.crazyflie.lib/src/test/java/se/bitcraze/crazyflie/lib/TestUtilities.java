package se.bitcraze.crazyflie.lib;

import java.util.List;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbServices;

import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class TestUtilities {

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
