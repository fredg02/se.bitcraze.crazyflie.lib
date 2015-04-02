package se.bitcraze.crazyflie.lib;

import se.bitcraze.crazyflie.lib.crazyradio.LinkListener;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;

/**
 * Dummy driver for testing purposes
 *
 */
public class DummyDriver extends CrtpDriver {

    @Override
    public void connect(int channel, int datarate) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendPacket(CrtpPacket packet) {
        // TODO Auto-generated method stub
    }

    @Override
    public CrtpPacket receivePacket(int wait) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addLinkListener(LinkListener linkListener) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeLinkListener(LinkListener linkListener) {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

}
