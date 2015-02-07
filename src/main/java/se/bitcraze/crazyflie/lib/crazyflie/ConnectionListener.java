package se.bitcraze.crazyflie.lib.crazyflie;


public interface ConnectionListener {

    /*
    # Called on disconnect, no matter the reason
    disconnected = Caller()
    # Called on unintentional disconnect only
    connection_lost = Caller()
    # Called when the first packet in a new link is received
    link_established = Caller()
    # Called when the user requests a connection
    connection_requested = Caller()
    # Called when the link is established and the TOCs (that are not cached) have been downloaded
    connected = Caller()
    # Called if establishing of the link fails (i.e times out)
    connection_failed = Caller()
    # Called for every packet received
    packet_received = Caller()
    # Called for every packet sent
    packet_sent = Caller()
    # Called when the link driver updates the link quality measurement
    link_quality_updated = Caller()
    */

    /**
     * This callback is called from the Crazyflie API when a Crazyflie has been connected and the TOCs have been downloaded.
     *
     * @param connectionInfo
     */
    public void connected(String connectionInfo);

    /**
     * Callback when connection initial connection fails (i.e no Crazyflie at the specified address)
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
     * Callback when the first packet in a new link is received
     *
     * @param connectionInfo
     */
    public void linkEstablished(String connectionInfo);

    /**
     * Callback when the user requests a connection
     *
     * @param connectionInfo
     */
    public void connectionRequested(String connectionInfo);

    /**
     * Called when the link driver updates the link quality measurement
     *
     * @param percent
     */
    public void linkQualityUpdated(int percent);

}
