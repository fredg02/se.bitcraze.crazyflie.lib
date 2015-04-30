package se.bitcraze.crazyflie.lib.crtp;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import se.bitcraze.crazyflie.lib.crazyflie.LinkListener;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;

/**
 * CTRP Driver main class
 * This class is inherited by all the CRTP link drivers.
 *
 */
public abstract class CrtpDriver {

    protected Set<LinkListener> mLinkListeners = new CopyOnWriteArraySet<LinkListener>();

    /**
     * Driver constructor. Throw an exception if the driver is unable to open the URI
     */
    public CrtpDriver() {
    }

    /**
     * Connect the driver
     *
     * @param connectionData
     */
    public abstract void connect(ConnectionData connectionData);

    /**
     * Close the link
     */
    public abstract void disconnect();


    /**
     * Send a CRTP packet
     *
     * @param packet
     */
    public abstract void sendPacket(CrtpPacket packet);

    /**
     * Receive a CRTP packet.
     *
     * @param wait The time to wait for a packet in milliseconds. -1 means forever
     * @return One CRTP packet or None if no packet has been received.
     */
    public abstract CrtpPacket receivePacket(int wait);


    /* LINK LISTENER */

    /**
     * Add a link listener
     *
     * @param linkListener
     */
    public void addLinkListener(LinkListener listener) {
        this.mLinkListeners.add(listener);
    }

    /**
     * Remove a link listener
     *
     * @param linkListener
     */
    public void removeLinkListener(LinkListener listener) {
        this.mLinkListeners.remove(listener);
    }

    protected void notifyLinkQualityUpdated(int percent) {
        for (LinkListener pl : this.mLinkListeners) {
            pl.linkQualityUpdated(percent);
        }
    }

    protected void notifyLinkError(String msg) {
        for (LinkListener pl : this.mLinkListeners) {
            pl.linkError(msg);
        }
    }
}
