package se.bitcraze.crazyflie.lib.crazyflie;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;



public interface PacketListener {

    /**
     * Called for every packet received
     *
     * @param packet
     */
    public void packetReceived(CrtpPacket packet);

    /**
     * Called for every packet sent
     */
    public void packetSent();

}
