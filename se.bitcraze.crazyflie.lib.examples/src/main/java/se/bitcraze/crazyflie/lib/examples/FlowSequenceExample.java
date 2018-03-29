package se.bitcraze.crazyflie.lib.examples;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.HoverPacket;
import se.bitcraze.crazyflie.lib.crtp.StopPacket;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 *  Simple example that connects to the Crazyflie,
 *  and executes a sequence of movements.
 *  
 *  This example requires the FlowDeck and a firmware version > 2017.06
 *
 */
public class FlowSequenceExample {

    private Crazyflie mCrazyflie;
    private float mHeight = 0.4f;
    private float mSpeed = 0.5f;
    /**
     * Initialize and run the example with the specified link_uri
     */
    public FlowSequenceExample(ConnectionData connectionData) {

        mCrazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));

        mCrazyflie.getDriver().addConnectionListener(new ConnectionAdapter() {

            /**
             * This callback is called from the Crazyflie API when a Crazyflie
             + has been connected and the TOCs have been downloaded.
             */
            @Override
            public void connected() {
                System.out.println("CONNECTED");

                // Start a separate thread to do the motor test.
                // Do not hijack the calling thread!
//                Thread(target=self._ramp_motors).start()
            }

            @Override
            public void setupFinished() {
                System.out.println("SETUP FINISHED");
                try {
                    sendDirections();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            /*
             * Callback when the Crazyflie is disconnected (called in all cases)
             */
            @Override
            public void disconnected() {
                System.out.println("DISCONNECTED");
            }

            /*
             * Callback when connection initial connection fails (i.e no Crazyflie at the specified address)
             */
            @Override
            public void connectionFailed(String msg) {
                System.out.println("CONNECTION FAILED: " + msg);
            }

            /**
             * Callback when disconnected after a connection has been made (i.e Crazyflie moves out of range)
             *
             * @param connectionInfo
             */
            @Override
            public void connectionLost(String msg) {
                System.out.println("CONNECTION LOST: " + msg);
            }

        });

        mCrazyflie.setConnectionData(connectionData);
        mCrazyflie.connect();

        System.out.println("Connection to " + connectionData);
    }

    public void sendDirections() throws InterruptedException {

        //TODO: reset kalman estimator

        // ascend
        for (float y = 0; y < mHeight; y += (float) mHeight/10) {
            this.mCrazyflie.sendPacket(new HoverPacket(0, 0, 0, y));
            Thread.sleep(100);
        }
        
        // hover
        for (float y = 0; y < 20; y++) {
            this.mCrazyflie.sendPacket(new HoverPacket(0, 0, 0, mHeight));
            Thread.sleep(100);
        }

        // circle right
        for (float y = 0; y < 50; y++) {
            this.mCrazyflie.sendPacket(new HoverPacket(mSpeed, 0, 36 * 2, mHeight));
            Thread.sleep(100);
        }

        // circle left
        for (float y = 0; y < 50; y++) {
            this.mCrazyflie.sendPacket(new HoverPacket(mSpeed, 0, -36 * 2, mHeight));
            Thread.sleep(100);
        }

        // hover
        for (float y = 0; y < 20; y++) {
            this.mCrazyflie.sendPacket(new HoverPacket(0, 0, 0, mHeight));
            Thread.sleep(100);
        }

        // descend
        for (float y = mHeight; y > 0; y -= (float) mHeight/10) {
            this.mCrazyflie.sendPacket(new HoverPacket(0, 0, 0, y));
            Thread.sleep(100);
        }

        this.mCrazyflie.sendPacket(new StopPacket());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        this.mCrazyflie.disconnect();
    }

    public static void main(String[] args) {
        new FlowSequenceExample(new ConnectionData(80, Crazyradio.DR_250KPS));
    }

}
