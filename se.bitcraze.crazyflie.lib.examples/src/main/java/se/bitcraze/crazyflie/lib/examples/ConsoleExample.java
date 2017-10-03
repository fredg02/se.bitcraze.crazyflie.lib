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
import se.bitcraze.crazyflie.lib.crtp.ConsolePacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class ConsoleExample {

    private Crazyflie mCrazyflie;

    public ConsoleExample(ConnectionData connectionData) {
        // Create a Crazyflie object without specifying any cache dirs
        mCrazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));
        //TODO: do not use cache

        /*
        # This might be done prettier ;-)
        console_text = "%s" % struct.unpack("%is" % len(packet.data), packet.data)
        */

        final StringBuffer consoleBuffer = new StringBuffer();

        mCrazyflie.addDataListener(new DataListener(CrtpPort.CONSOLE) {

            @Override
            public void dataReceived(CrtpPacket packet) {
                //TODO: trying to filter out empty console packets
                String text = ConsolePacket.parse(packet.getPayload()).getText();
                if (!text.isEmpty() && !"".equals(text)) {
                    System.out.println(text);
                    consoleBuffer.append(text);
                }
            }
        });

        System.out.println("Connecting to " + connectionData);

        // Try to connect to the Crazyflie
        mCrazyflie.connect(connectionData);

        // Start a timer to disconnect after 5s
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                System.out.println("Console Packets: " + consoleBuffer.toString());
                System.out.println("Disconnected after 10 seconds...");
                mCrazyflie.disconnect();
            }

        }, 5000);
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
        foundCrazyflies.add(new ConnectionData(10, 0));

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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No Crazyflies found, cannot run example");
        }
    }
}
