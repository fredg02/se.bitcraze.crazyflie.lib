package se.bitcraze.crazyflie.lib.crazyflie;

/**
 * An abstract adapter class for receiving connection events. The methods in
 * this class are empty. This class exists as convenience for creating listener
 * objects.
 */
public abstract class ConnectionAdapter implements ConnectionListener {

    public void connectionRequested(String connectionInfo) {
    }

    public void connected(String connectionInfo) {
    }

    public void setupFinished(String connectionInfo) {
    }

    public void connectionFailed(String connectionInfo, String msg) {
    }

    public void connectionLost(String connectionInfo, String msg) {
    }

    public void disconnected(String connectionInfo) {
    }

    public void linkQualityUpdated(int percent) {
    }

}
