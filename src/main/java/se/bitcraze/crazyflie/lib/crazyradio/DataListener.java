package se.bitcraze.crazyflie.lib.crazyradio;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;

/**
 * TODO:
 * -add channel?
 * -add port mask (or use CrtpPort.ALL?)
 * -add channel mask
 *
 */
public abstract class DataListener {

    private CrtpPort mPort;

    public DataListener(CrtpPort port) {
        mPort = port;
    }

    public CrtpPort getPort() {
        return mPort;
    }

    public abstract void dataReceived(CrtpPacket packet);

}
