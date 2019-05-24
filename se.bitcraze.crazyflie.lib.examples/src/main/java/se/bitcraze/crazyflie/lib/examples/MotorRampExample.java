package se.bitcraze.crazyflie.lib.examples;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 *  Simple example that connects to a Crazyflie with the given channel and data rate,
 *  ramps up/down the motors and disconnects.
 *
 */
public class MotorRampExample {

    private Crazyflie mCrazyflie;
    /**
     * Initialize and run the example with the specified link_uri
     */
    public MotorRampExample(ConnectionData connectionData) {

        mCrazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));

        mCrazyflie.getDriver().addConnectionListener(new ConnectionAdapter() {

            /**
             * This callback is called from the Crazyflie API when a Crazyflie
             * has been connected and the TOCs have been downloaded.
             */
            @Override
            public void connected() {
                System.out.println("CONNECTED");
            }
            
            /**
             * Callback when the Crazyflie has finished it's setup
             */
            @Override
            public void setupFinished() {
            	System.out.println("SETUP FINISHED");
            	
            	// Start a separate thread to do the motor test.
                rampMotors();
            }

            /*
             * Callback when the Crazyflie is disconnected (called in all cases)
             */
            public void disconnected() {
                System.out.println("DISCONNECTED");
            }

            /*
             * Callback when connection initial connection fails (i.e no Crazyflie at the specified address)
             */
            public void connectionFailed(String msg) {
                System.out.println("CONNECTION FAILED: " +  msg);
            }

            /**
             * Callback when disconnected after a connection has been made (i.e Crazyflie moves out of range)
             *
             * @param connectionInfo
             */
            public void connectionLost(String msg) {
                System.out.println("CONNECTION LOST: " +  msg);
            }

        });

        mCrazyflie.setConnectionData(connectionData);
        mCrazyflie.connect();

        System.out.println("Connected to " + connectionData);
    }

    public void rampMotors() {
        int thrust_mult = 1;
        int thrust_step = 500;
        long thrust = 15000;
        float pitch = 0;
        float roll = 0;
        float yawrate = 0;

        // send packet with zero thrust to arm the copter
        this.mCrazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
        while (thrust >= 15000) {
            System.out.println("sendPacket: " + thrust);
            this.mCrazyflie.sendPacket(new CommanderPacket(roll, pitch, yawrate, (char) thrust));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (thrust >= 20000) {
                thrust_mult = -1;
            }
            thrust += thrust_step * thrust_mult;
        }
        this.mCrazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
        // Make sure that the last packet leaves before the link is closed
        // since the message queue is not flushed before closing
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.mCrazyflie.disconnect();
    }

    public static void main(String[] args) {
        int channel = 80;
        int datarate = Crazyradio.DR_250KPS;

        new MotorRampExample(new ConnectionData(channel, datarate));
    }

}
