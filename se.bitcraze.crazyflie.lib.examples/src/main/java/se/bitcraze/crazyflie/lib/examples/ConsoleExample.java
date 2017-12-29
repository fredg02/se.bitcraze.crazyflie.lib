package se.bitcraze.crazyflie.lib.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie.State;
import se.bitcraze.crazyflie.lib.crazyflie.DataListener;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class ConsoleExample {

    private Crazyflie mCrazyflie;
    private StringBuffer consoleBuffer = new StringBuffer();

    public ConsoleExample(ConnectionData connectionData) {
        // Create a Crazyflie object without specifying any cache dirs
        //TODO: do not use cache
        mCrazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));
//        mCrazyflie = new Crazyflie(new MockDriver());

        /*
        # This might be done prettier ;-)
        console_text = "%s" % struct.unpack("%is" % len(packet.data), packet.data)
        */

        mCrazyflie.addDataListener(new DataListener(CrtpPort.CONSOLE) {

            @Override
            public void dataReceived(CrtpPacket packet) {
                byte[] payload = packet.getPayload();
                
                //skip packet when it only contains zeros
                if (containsOnly00(payload)) {
                    return;
                }
                String trimmedText = new String(payload).trim();
                if (contains0A(payload)) {
                    consoleBuffer.append(trimmedText);
                    System.out.println(consoleBuffer);
                    consoleBuffer = new StringBuffer();
                } else {
                    consoleBuffer.append(trimmedText);
                }
            }
        });

        System.out.println("Connecting to " + connectionData);

        // Try to connect to the Crazyflie
        mCrazyflie.setConnectionData(connectionData);
        mCrazyflie.connect();

        // Start a timer to disconnect after 5s
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                System.out.println("Disconnected after 5 seconds...");
                mCrazyflie.disconnect();
                System.exit(0);
            }

        }, 1000);
    }

    private boolean contains0A(byte[] payload) {
        for (byte b : payload) {
            if (b == 10) {
                return true;
            }
        }
        return false;
    }

    private boolean containsOnly00(byte[] payload) {
        for (byte b : payload) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    public Crazyflie getCrazyflie() {
        return this.mCrazyflie;
    }

    public static void main(String[] args) {
        // Initialize the low-level drivers (don't list the debug drivers)
        // cflib.crtp.init_drivers(enable_debug_driver=False)

        // Scan for Crazyflies and use the first one found
//        System.out.println("Scanning interfaces for Crazyflies...");
//
//        RadioDriver radioDriver = new RadioDriver(new UsbLinkJava());
//        List<ConnectionData> foundCrazyflies = radioDriver.scanInterface();
//        radioDriver.disconnect();
//
//        System.out.println("Crazyflies found:");
//        for (ConnectionData connectionData : foundCrazyflies) {
//            System.out.println(connectionData);
//        }

        List<ConnectionData> foundCrazyflies = new ArrayList<ConnectionData>();
        foundCrazyflies.add(new ConnectionData(80, 0));

        if (foundCrazyflies.size() > 0) {
            ConsoleExample consoleExample = new ConsoleExample(foundCrazyflies.get(0));

            /**
             * The Crazyflie lib doesn't contain anything to keep the application alive,
             * so this is where your application should do something. In our case we
             * are just waiting until we are disconnected.
             */
            while (!(consoleExample.getCrazyflie().getState() == State.DISCONNECTED)) {
                try {
                    Thread.sleep(1000);
//                    consoleExample.getCrazyflie().sendPacket(new CrtpPacket());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No Crazyflies found, cannot run example");
        }
    }
}
