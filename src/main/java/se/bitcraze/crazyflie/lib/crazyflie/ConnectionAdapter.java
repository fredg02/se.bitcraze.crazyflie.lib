package se.bitcraze.crazyflie.lib.crazyflie;

/**
 * An abstract adapter class for receiving connection events. The methods in
 * this class are empty. This class exists as convenience for creating listener
 * objects.
 */
public abstract class ConnectionAdapter implements ConnectionListener {

    public void connectionRequested(String linkUri) {
    }

    public void connected(String linkUri) {
    }

    public void setupFinished(String linkUri) {
    }

    public void connectionFailed(String linkUri, String msg) {
    }

    public void connectionLost(String linkUri, String msg) {
    }

    public void disconnected(String linkUri, String msg) {
    }

    public void linkQualityUpdated(int percent) {
    }


}
