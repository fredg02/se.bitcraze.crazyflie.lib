package se.bitcraze.crazyflie.lib;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionListener;

/**
 * An abstract adapter class for receiving connection events. 
 * This class exists as convenience for creating listener objects in test.
 */
public abstract class TestConnectionAdapter implements ConnectionListener {

    public void connectionRequested(String connectionInfo) {
        System.out.println("CONNECTION REQUESTED: " + connectionInfo);
    }

    public void connected(String connectionInfo) {
        System.out.println("CONNECTED: " + connectionInfo);
    }

    public void setupFinished(String connectionInfo) {
        System.out.println("SETUP FINISHED: " + connectionInfo);
    }

    public void connectionFailed(String connectionInfo, String msg) {
        System.out.println("CONNECTION FAILED: " + connectionInfo);
    }

    public void connectionLost(String connectionInfo, String msg) {
        System.out.println("CONNECTION LOST: " + connectionInfo);
    }

    public void disconnected(String connectionInfo) {
        System.out.println("DISCONNECTED: " + connectionInfo);
    }

    public void linkQualityUpdated(int percent) {
        //System.out.println("LINK QUALITY: " + percent);
    }

}
