package se.bitcraze.crazyflie.lib.examples;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import se.bitcraze.crazyflie.lib.MockDriver;
import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie.State;
import se.bitcraze.crazyflie.lib.crazyflie.DataListener;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;

public class ConsoleExample {

    private Crazyflie mCrazyflie;
    private StringBuffer consoleBuffer = new StringBuffer();
    private int counter = 0;

    public ConsoleExample(ConnectionData connectionData) {
        // Create a Crazyflie object without specifying any cache dirs
//        mCrazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));
        mCrazyflie = new Crazyflie(new MockDriver());
        //TODO: do not use cache

        /*
        # This might be done prettier ;-)
        console_text = "%s" % struct.unpack("%is" % len(packet.data), packet.data)
        */


        mCrazyflie.addDataListener(new DataListener(CrtpPort.CONSOLE) {

            @Override
            public void dataReceived(CrtpPacket packet) {
                //TODO: trying to filter out empty console packets
                byte[] payload = packet.getPayload();
//                System.out.println("Console Example: " + Utilities.getHexString(payload));
                String text = filterPayload(payload);
//                System.out.println(text);
                if (text != null) {
                    if (!scanFor0A(payload)) {
                        //if 0A is not found just append to buffer
                        System.out.println("no 0A");
                        consoleBuffer.append(text);
                        counter++;
                        System.out.println("counter: " + counter);
                    } else {
                        //if 0A is found append to buffer, then dump
                        System.out.println("found 0A");
                        consoleBuffer.append(text);
                        counter++;
                        System.out.println(consoleBuffer.toString());
                        System.out.println("counter: " + counter);
                        //clearing buffer
                        consoleBuffer = new StringBuffer();
                        counter = 0;
                    }
                }
            }
        });

        mCrazyflie.getDriver().addConnectionListener(new ConnectionAdapter() {
            
            @Override
            public void setupFinished(String connectionInfo) {
                System.out.println("setupFinished");
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
                System.out.println("Console Packets: " + consoleBuffer.toString());
                System.out.println("Disconnected after 5 seconds...");
                mCrazyflie.disconnect();
                System.exit(0);
            }

        }, 1000);
    }

    private boolean scanFor0A(byte[] payload) {
        for (byte b : payload) {
            if (b == 10) {
                return true;
            }
        }
        return false;
    }
    
    protected String filterPayload(byte[] payload) {
        ByteBuffer tempDecodeBuffer = ByteBuffer.allocate(payload.length);
        int continueCounter = 0;
        for (int n=0; n < payload.length; n++) {
            //read in one byte from packetByteBuffer
            byte b = payload[n];
            if (b == 0) {
                continueCounter++;
                continue;
            } else {
                tempDecodeBuffer.put(b);
            }
        }
        if (continueCounter == payload.length) {
            return null;
        }
        return new String(tempDecodeBuffer.array());
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
