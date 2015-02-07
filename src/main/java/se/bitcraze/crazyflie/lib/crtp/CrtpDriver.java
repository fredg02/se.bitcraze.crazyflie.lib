package se.bitcraze.crazyflie.lib.crtp;

import se.bitcraze.crazyflie.lib.crazyradio.LinkListener;

/**
 * CTRP Driver main class
 * This class is inherited by all the CRTP link drivers.
 *
 */
public abstract class CrtpDriver {

    /**
     * Driver constructor. Throw an exception if the driver is unable to open the URI
     */
    public CrtpDriver() {
    }

    /**
     * Connect the driver
     *
     * @param channel
     * @param datarate
     */
    public abstract void connect(int channel, int datarate);

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


    /**
     * Add a link listener
     *
     * @param linkListener
     */
    public abstract void addLinkListener(LinkListener linkListener);

    /**
     * Remove a link listener
     *
     * @param linkListener
     */
    public abstract void removeLinkListener(LinkListener linkListener);

    /**
     * Close the link
     */
    public abstract void close();
}
