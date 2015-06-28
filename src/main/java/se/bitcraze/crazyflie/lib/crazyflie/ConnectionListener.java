package se.bitcraze.crazyflie.lib.crazyflie;


public interface ConnectionListener {

    /*
    # Called for every packet received
    packet_received = Caller()
    # Called for every packet sent
    packet_sent = Caller()
    */

    /**
     * Callback when the user requests a connection
     *
     * @param connectionInfo
     */
    public void connectionRequested(String connectionInfo);

    /**
     * Callback when the first packet in a new link is received
     *
     * @param connectionInfo
     */
    public void connected(String connectionInfo);

    /**
     * Callback when a Crazyflie has been connected and the TOCs have been downloaded.
     *
     * @param connectionInfo
     */
    public void setupFinished(String connectionInfo);

    /**
     * Callback when initial connection fails (i.e no Crazyflie at the specified address)
     *
     * @param connectionInfo
     * @param msg
     */
    public void connectionFailed(String connectionInfo, String msg);

    /**
     * Callback when disconnected after a connection has been made (i.e Crazyflie moves out of range)
     *
     * @param connectionInfo
     * @param msg
     */
    public void connectionLost(String connectionInfo, String msg);

    /**
     * Callback when the Crazyflie is disconnected (called in all cases)
     *
     * @param connectionInfo
     */
    public void disconnected(String connectionInfo);

    /**
     * Called when the link driver updates the link quality measurement
     *
     * @param percent
     */
    public void linkQualityUpdated(int percent);

}
