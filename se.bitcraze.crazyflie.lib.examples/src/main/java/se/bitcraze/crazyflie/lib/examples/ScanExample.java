package se.bitcraze.crazyflie.lib.examples;

import java.util.List;

import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 * Simple example that scans for available Crazyflies and lists them.
 */
public class ScanExample {

    public static void main(String[] args) {
        System.out.println("Scanning interfaces for Crazyflies...");
        RadioDriver radioDriver = new RadioDriver(new UsbLinkJava());
        List<ConnectionData> foundCrazyflies = radioDriver.scanInterface();
        radioDriver.disconnect();
        System.out.println("Crazyflies found:");
        for (ConnectionData connectionData : foundCrazyflies) {
            System.out.println(connectionData);
        }
    }
}
