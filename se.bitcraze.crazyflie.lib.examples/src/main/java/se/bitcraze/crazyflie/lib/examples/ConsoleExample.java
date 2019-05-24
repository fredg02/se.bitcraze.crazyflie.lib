package se.bitcraze.crazyflie.lib.examples;

import java.util.Timer;
import java.util.TimerTask;

import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie.State;
import se.bitcraze.crazyflie.lib.crazyflie.DataListener;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 * Simple example that connects to the Crazyflie on the given channel and data rate.
 * It prints the contents of the console.
 */
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
                System.out.println("Disconnected after 3 seconds...");
                mCrazyflie.disconnect();
                System.exit(0);
            }

        }, 3000);
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
        int channel = 80;
        int datarate = Crazyradio.DR_250KPS;

        ConsoleExample consoleExample = new ConsoleExample(new ConnectionData(channel, datarate));

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
    }
}
