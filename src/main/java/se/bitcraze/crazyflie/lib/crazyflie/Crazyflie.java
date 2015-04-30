package se.bitcraze.crazyflie.lib.crazyflie;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.param.Param;
import se.bitcraze.crazyflie.lib.toc.TocCache;
import se.bitcraze.crazyflie.lib.toc.TocFetcher.TocFetchFinishedListener;

public class Crazyflie {

    final Logger mLogger = LoggerFactory.getLogger("Crazyflie");

    private CrtpDriver mDriver;
    private Thread mIncomingPacketHandlerThread;

    private LinkedBlockingDeque<CrtpPacket> mResendQueue = new LinkedBlockingDeque<CrtpPacket>();
    private Thread mResendQueueHandlerThread;

    private Set<DataListener> mDataListeners = new CopyOnWriteArraySet<DataListener>();
    private Set<PacketListener> mPacketListeners = new CopyOnWriteArraySet<PacketListener>();
    private Set<ConnectionListener> mConnectionListeners = new CopyOnWriteArraySet<ConnectionListener>();

    private State mState = State.DISCONNECTED;

    private ConnectionData mConnectionData;

    private LinkListener mLinkListener;
    private PacketListener mPacketListener;

    private Param mParam;
    private TocCache mTocCache;

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

        this.mTocCache = new TocCache("ro_cache", "rw_cache");
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


        mPacketListener = new PacketListener() {

            public void packetReceived(CrtpPacket packet) {
                checkReceivedPackets(packet);
            }

            public void packetSent() {
            }

        };
        addPacketListener(mPacketListener);

        // try to connect
        mDriver.connect(mConnectionData);

        if (mIncomingPacketHandlerThread == null) {
            IncomingPacketHandler iph = new IncomingPacketHandler();
            mIncomingPacketHandlerThread = new Thread(iph);
            mIncomingPacketHandlerThread.start();
        }

        if (mResendQueueHandlerThread == null) {
            ResendQueueHandler rqh = new ResendQueueHandler();
            mResendQueueHandlerThread = new Thread(rqh);
            mResendQueueHandlerThread.start();
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
                mDriver.disconnect();
                mDriver = null;
            }
            if(mIncomingPacketHandlerThread != null) {
                mIncomingPacketHandlerThread.interrupt();
            }
            notifyDisconnected();
            mState = State.DISCONNECTED;
        }
    }

    // TODO: should this be public?
    public State getState() {
        return mState;
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

            if (packet.getExpectedReply() != null && packet.getExpectedReply().length > 0) {
                //add packet to resend queue
                if(!mResendQueue.contains(packet)) {
                    mResendQueue.add(packet);
                } else {
                    mLogger.warn("Packet already exists in Queue.");
                }
            }
        }
    }

    /**
     * Callback called for every packet received to check if we are
     * waiting for an packet like this. If so, then remove it from the queue.
     *
     * @param packet
     */
    private void checkReceivedPackets(CrtpPacket packet) {
        // compare received packet with expectedReplies in resend queue
        for(CrtpPacket resendQueuePacket : mResendQueue) {
            if(isPacketMatchingExpectedReply(resendQueuePacket, packet)) {
                mResendQueue.remove(resendQueuePacket);
                mLogger.debug("QUEUE REMOVE: " + resendQueuePacket);
                break;
            }
        }
    }

    private boolean isPacketMatchingExpectedReply(CrtpPacket resendQueuePacket, CrtpPacket packet) {
        //Only check equality for the amount of bytes in expected reply
        byte[] expectedReply = resendQueuePacket.getExpectedReply();
        for(int i = 0; i < expectedReply.length;i++) {
            if(expectedReply[i] != packet.getPayload()[i]) {
                return false;
            }
        }
        return true;
    }

    private class ResendQueueHandler implements Runnable {

        public void run() {
            while(true) {
                if (!mResendQueue.isEmpty()) {
                    CrtpPacket resendPacket = mResendQueue.poll();
                    mLogger.debug("RESEND: " + resendPacket + " ID: " + resendPacket.getPayload()[0]);
                    sendPacket(resendPacket);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
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
            notifyConnected();
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
        mLogger.info("We are connected [" + mConnectionData.toString() + "], requesting connection setup...");
        //FIXME
        //Skipping log and param setup for now
        //this.mLog.refreshToc(self._log_toc_updated_cb, self._toc_cache);

        mParam = new Param(this);
        TocFetchFinishedListener paramTocFetchFinishedListener = new TocFetchFinishedListener() {

            public void tocFetchFinished() {
                //_param_toc_updated_cb(self):
                mLogger.info("Param TOC finished updating.");
                //mParam.requestUpdateOfAllParams();
                //TODO: should be set only after log, param, mems are all updated
                mState = State.SETUP_FINISHED;
                notifySetupFinished();

            }
        };
        mParam.refreshToc(paramTocFetchFinishedListener, mTocCache);
    }

    public Param getParam() {
        return mParam;
    }

    //TODO: do this properly
    public void clearTocCache() {
        mTocCache = new TocCache(null, null);
    }

    /** DATA LISTENER **/

    /**
     * Add a data listener for data that comes on a specific port
     *
     * @param dataListener
     */
    public void addDataListener(DataListener dataListener) {
        mLogger.debug("Adding data listener for port [" + dataListener.getPort() + "]");
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
        for (DataListener dataListener : mDataListeners) {
            dataListener.dataReceived(inPacket);
        }
    }

    /* PACKET LISTENER */

    public void addPacketListener(PacketListener listener) {
        mLogger.debug("Adding packet listener...");
        this.mPacketListeners.add(listener);
    }

    public void removePacketListener(PacketListener listener) {
        mLogger.debug("Removing packet listener...");
        this.mPacketListeners.remove(listener);
    }

    private void notifyPacketReceived(CrtpPacket inPacket) {
        checkForInitialPacketCallback(inPacket);
        for (PacketListener pl : this.mPacketListeners) {
            pl.packetReceived(inPacket);
        }
    }

    /* CONNECTION LISTENER */

    public void addConnectionListener(ConnectionListener listener) {
        this.mConnectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        this.mConnectionListeners.remove(listener);
    }

    /**
     * Notify all registered listeners about a requested connection
     */
    private void notifyConnectionRequested() {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionRequested(mConnectionData.toString());
            }
        }


    /**
     * Notify all registered listeners about a connect.
     */
    private void notifyConnected() {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connected(mConnectionData.toString());
            }
        }

    /**
     * Notify all registered listeners about a finished setup.
     */
    private void notifySetupFinished() {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.setupFinished(mConnectionData.toString());
            }
        }

    /**
     * Notify all registered listeners about a failed connection attempt.
     *
     * @param msg
     */
    private void notifyConnectionFailed(String msg) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionFailed(mConnectionData.toString(), msg);
            }
        }

    /**
     * Notify all registered listeners about a lost connection.
     *
     * @param msg
     */
    private void notifyConnectionLost(String msg) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionLost(mConnectionData.toString(), msg);
            }
        }

    /**
     * Notify all registered listeners about a disconnect.
     */
    private void notifyDisconnected() {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.disconnected(mConnectionData.toString());
            }
        }

    /**
     * Notify all registered listeners about a link quality update.
     *
     * @param percent quality of the link (0 = connection lost, 100 = good)
     */
    private void notifyLinkQualityUpdated(int percent) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.linkQualityUpdated(percent);
            }
        }

    /**
     * Handles incoming packets and sends the data to the correct listeners
     *
     * TODO: respect also channel specific data listeners?
     *
     */
    public class IncomingPacketHandler implements Runnable {

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
                        //mLogger.warn("Got packet on port [" + packet.getHeader().getPort() + "] but found no data listener to handle it.");
                    }
                } catch(InterruptedException e) {
                    mLogger.debug("IncomingPacketHandlerThread was interrupted.");
                    break;
                }
            }
        }

    }

}
