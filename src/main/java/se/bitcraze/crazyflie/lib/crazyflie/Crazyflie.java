package se.bitcraze.crazyflie.lib.crazyflie;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.LinkListener;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;

public class Crazyflie {

    final Logger mLogger = LoggerFactory.getLogger("Crazyflie");

    private CrtpDriver mDriver;
    private Thread mIncomingPacketHandlerThread;

    private List<DataListener> mDataListeners = Collections.synchronizedList(new LinkedList<DataListener>());
    private List<PacketListener> mPacketListeners = Collections.synchronizedList(new LinkedList<PacketListener>());
    private List<ConnectionListener> mConnectionListeners = Collections.synchronizedList(new LinkedList<ConnectionListener>());

    private State mState = State.DISCONNECTED;

    private ConnectionData mConnectionData;

    private LinkListener mLinkListener;

    /**
     * State of the connection procedure
     */
    public enum State {
        DISCONNECTED,
        INITIALIZED,
        CONNECTED,
        SETUP_FINISHED;
    }


    public Crazyflie(CrtpDriver driver) {
        this.mDriver = driver;
    }

    public void connect(int channel, int datarate) {
        connect(new ConnectionData(channel, datarate));
    }

    public void connect(ConnectionData connectionData) {
        mLogger.debug("Connect");
        mConnectionData = connectionData;
        notifyConnectionRequested();
        mState = State.INITIALIZED;

        //TODO: can this be done more elegantly?
        mLinkListener = new LinkListener(){

            public void linkQualityUpdated(int percent) {
                notifyLinkQualityUpdated(percent);
            }

            public void linkError(String msg) {
                //TODO
            };
        };
        mDriver.addLinkListener(mLinkListener);

        // try to connect
        mDriver.connect(mConnectionData.getChannel(), mConnectionData.getDataRate());

        if (mIncomingPacketHandlerThread == null) {
            IncomingPacketHandler iph = new IncomingPacketHandler();
            mIncomingPacketHandlerThread = new Thread(iph);
            mIncomingPacketHandlerThread.start();
        }

        //TODO: better solution to wait for connected state?
        //Timeout: 10x50ms = 500ms
        int i = 0;
        while(i < 10) {
            if (mState == State.CONNECTED) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }

        if (mState == State.CONNECTED) {
            startConnectionSetup();

        } else {
            notifyConnectionFailed("Connection failed");
            disconnect();
        }

    }

    public void disconnect() {
        if (mState != State.DISCONNECTED) {
            mLogger.debug("Disconnect");

            if (mDriver != null) {
                mDriver.removeLinkListener(mLinkListener);
                //Send commander packet with all values set to 0 before closing the connection
                sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
                mDriver.close();
                mDriver = null;
            }
            if(mIncomingPacketHandlerThread != null) {
                mIncomingPacketHandlerThread.interrupt();
            }
            notifyDisconnected();
            mState = State.DISCONNECTED;
        }
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

    /**
     * Called when first packet arrives from Crazyflie.
     * This is used to determine if we are connected to something that is answering.
     *
     * @param data
     */
    public void checkForInitialPacketCallback(CrtpPacket packet) {
        //TODO: should be made more reliable
        if (this.mState == State.INITIALIZED) {
            this.mState = State.CONNECTED;
            //self.link_established.call(self.link_uri)
            notifyLinkEstablished();
        }
        //self.packet_received.remove_callback(self._check_for_initial_packet_cb)
        // => IncomingPacketHandler
    }

    public CrtpDriver getDriver(){
        return mDriver;
    }


    /**
     * Start the connection setup by refreshing the TOCs
     */
    public void startConnectionSetup() {
        mLogger.info("We are connected[" + mConnectionData.toString() + "], requesting connection setup...");
        //FIXME
        //Skipping log and param setup for now
        //this.mLog.refreshToc(self._log_toc_updated_cb, self._toc_cache);
        notifyConnected();
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


    /* PACKET LISTENER */

    public void addPacketListener(PacketListener listener) {
        if (mPacketListeners.contains(listener)) {
            mLogger.warn("PacketListener " + listener.toString() + " already registered.");
            return;
        }
        this.mPacketListeners.add(listener);
    }

    public void removePacketListener(PacketListener listener) {
        this.mPacketListeners.remove(listener);
    }

    private void notifyPacketReceived(CrtpPacket inPacket) {
        checkForInitialPacketCallback(inPacket);
        synchronized (this.mPacketListeners) {
            for (PacketListener pl : this.mPacketListeners) {
                pl.packetReceived(inPacket);
            }
        }
    }

    /* CONNECTION LISTENER */

    public void addConnectionListener(ConnectionListener listener) {
        if (mConnectionListeners.contains(listener)) {
            mLogger.warn("ConnectionListener " + listener.toString() + " already registered.");
            return;
        }
        this.mConnectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        this.mConnectionListeners.remove(listener);
    }

    /**
     * Notify all registered listeners about a requested connection
     *
     * @param linkUri
     */
    private void notifyConnectionRequested() {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionRequested(mConnectionData.toString());
            }
        }
    }

    /**
     * Notify all registered listeners about an established link.
     */
    private void notifyLinkEstablished() {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.linkEstablished(mConnectionData.toString());
            }
        }
    }

    /**
     * Notify all registered listeners about a connect.
     */
    private void notifyConnected() {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connected(mConnectionData.toString());
            }
        }
    }

    /**
     * Notify all registered listeners about a failed connection attempt.
     */
    private void notifyConnectionFailed(String msg) {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionFailed(mConnectionData.toString(), msg);
            }
        }
    }

    /**
     * Notify all registered listeners about a lost connection.
     */
    private void notifyConnectionLost(String msg) {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionLost(mConnectionData.toString(), msg);
            }
        }
    }

    /**
     * Notify all registered listeners about a disconnect.
     */
    private void notifyDisconnected() {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.disconnected(mConnectionData.toString());
            }
        }
    }

    /**
     * Notify all registered listeners about a link quality update.
     */
    private void notifyLinkQualityUpdated(int percent) {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.linkQualityUpdated(percent);
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
                    notifyPacketReceived(packet);

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
