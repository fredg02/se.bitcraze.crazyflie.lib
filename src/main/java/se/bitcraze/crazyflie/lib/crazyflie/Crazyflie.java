package se.bitcraze.crazyflie.lib.crazyflie;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.DataListener;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;

public class Crazyflie {

    final Logger mLogger = LoggerFactory.getLogger("Crazyflie");

    private CrtpDriver mDriver;
    private Thread mIncomingPacketHandlerThread;

    private List<DataListener> mDataListeners = new ArrayList<DataListener>();


    public Crazyflie(CrtpDriver driver) {
        this.mDriver = driver;
    }

    public void connect(ConnectionData connectionData) {
        connect(connectionData.getChannel(), connectionData.getDataRate());
    }

    public void connect(int channel, int datarate) {
        mLogger.debug("Connect");
        mDriver.connect(channel, datarate);

        if (mIncomingPacketHandlerThread == null) {
            IncomingPacketHandler iph = new IncomingPacketHandler();
            mIncomingPacketHandlerThread = new Thread(iph);
            mIncomingPacketHandlerThread.start();
        }
    }

    public void disconnect() {
        mLogger.debug("Disconnect");
        if (mDriver != null) {
            //Send commander packet with all values set to 0 before closing the connection
            sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
            mDriver.close();
            mDriver = null;
        }
        mIncomingPacketHandlerThread.interrupt();
    }

    /**
     * Send a packet through the driver interface
     *
     * @param packet
     */
    // def send_packet(self, pk, expected_reply=(), resend=False):
    public void sendPacket(CrtpPacket packet){
        if (mDriver != null) {
            mDriver.sendPacket(packet);
        }
    }

    public CrtpDriver getDriver(){
        return mDriver;
    }



    /** DATA LISTENER **/

    /**
     * Add a data listener for data that comes on a specific port
     *
     * @param dataListener
     */
    public void addDataListener(DataListener dataListener) {
        mLogger.debug("Adding data listener for port [" + dataListener.getPort() + "]");
        if (mDataListeners.contains(dataListener)) {
            mLogger.warn("DataListener " + dataListener.toString() + " already registered.");
            return;
        }
        this.mDataListeners.add(dataListener);
    }

    /**
     * Remove a data listener for data that comes on a specific port
     *
     * @param dataListener
     */
    public void removeDataListener(DataListener dataListener) {
        mLogger.debug("Removing data listener for port [" + dataListener.getPort() + "]");
        this.mDataListeners.remove(dataListener);
    }

    //public void removeDataListener(CrtpPort); ?

    /**
     * @param inPacket
     */
    private void notifyDataReceived(CrtpPacket inPacket) {
        synchronized (mDataListeners) {
            for (DataListener dataListener : mDataListeners) {
                dataListener.dataReceived(inPacket);
            }
        }
    }





    /**
     * Handles incoming packets and sends the data to the correct listeners
     *
     * TODO: respect also channel specific data listeners?
     *
     */
    public class IncomingPacketHandler implements Runnable{

        final Logger mLogger = LoggerFactory.getLogger("IncomingPacketHandler");

        public void run() {
            while(true) {
                try {
                    if (getDriver() == null) {
                        // time.sleep(1)
                        Thread.sleep(100);
                        continue;
                    }

                    CrtpPacket packet = getDriver().receivePacket(1);
                    if(packet == null) {
                        continue;
                    }

                    //All-packet callbacks
                    //self.cf.packet_received.call(pk)
                    //this.mCrazyflie.packetReceived(packet);

                    boolean found = false;
                    for (DataListener dataListener : mDataListeners) {
                        if (dataListener.getPort() == packet.getHeader().getPort()) {
                            notifyDataReceived(packet);
                            found = true;
                        }
                    }
                    if (!found) {
                        mLogger.warn("Got packet on port [" + packet.getHeader().getPort() + "] but found no data listener to handle it.");
                    }
                } catch(InterruptedException e) {
                    break;
                }
            }
        }

    }

}
