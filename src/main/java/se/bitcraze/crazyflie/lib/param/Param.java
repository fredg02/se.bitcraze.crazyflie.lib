package se.bitcraze.crazyflie.lib.param;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.DataListener;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket.Header;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.toc.TocCache;
import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.TocFetcher;
import se.bitcraze.crazyflie.lib.toc.TocFetcher.TocFetchFinishedListener;

/**
 * Enables reading/writing of parameter values to/from the Crazyflie.
 * When a Crazyflie is connected it's possible to download a TableOfContent of all
 * the parameters that can be written/read.
 *
 */
public class Param {

    final Logger mLogger = LoggerFactory.getLogger("Param");

    private Toc mToc;
    private Crazyflie mCrazyflie;

    private Thread mParamUpdaterThread;
    private ParamUpdaterThread mPut;
    private Map<String, Map<String, Number>> mValues = new HashMap<String, Map<String, Number>>();
    private boolean mHaveUpdated = false;

    // Possible states
    private int IDLE = 0;
    private int WAIT_TOC = 1;
    private int WAIT_READ = 2;
    private int WAIT_WRITE = 3;

    private int TOC_CHANNEL = 0;
    private int READ_CHANNEL = 1;
    private int WRITE_CHANNEL = 2;

    // TOC access command
    private int TOC_RESET = 0;
    private int TOC_GETNEXT = 1;
    private int TOC_GETCRC32 = 2;



    public Param(Crazyflie crazyflie) {
        this.mCrazyflie = crazyflie;
        /*
        self.param_update_callbacks = {}
        self.group_update_callbacks = {}
        */

        // self.param_updater = None
        // self.param_updater = _ParamUpdater(self.cf, self._param_updated)
        // self.param_updater.start()
        mParamUpdaterThread = null;
        if (mParamUpdaterThread == null) {
            mPut = new ParamUpdaterThread();
            mParamUpdaterThread = new Thread(mPut);
            mParamUpdaterThread.start();
        }

        // self.cf.disconnected.add_callback(self.param_updater.close)
        mCrazyflie.addConnectionListener(new ConnectionAdapter() {
            @Override
            public void disconnected(String connectionInfo) {
                mPut.close();
            }
        });

        // self.all_updated = Caller()
    }

    /**
     * Request an update of all the parameters in the TOC
     */
    public void requestUpdateOfAllParams() {
        for (TocElement tocElement : mToc.getElements()) {
            requestParamUpdate(tocElement.getCompleteName());
        }
    }

    /**
     * Check if all parameters from the TOC have at least been fetched once
     */
    public boolean checkIfAllUpdated() {
        /*
        for g in self.toc.toc:
            if not g in self.values:
                return False
            for n in self.toc.toc[g]:
                if not n in self.values[g]:
                    return False

        return True
        */
        for (TocElement tocElement : mToc.getElements()) {
            if (mValues.get(tocElement.getGroup()) == null) {
                return false;
            } else {
                if (mValues.get(tocElement.getGroup()).get(tocElement.getName()) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Callback with data for an updated parameter
     */
    public void paramUpdated(CrtpPacket packet) {
        int varId = packet.getPayload()[0];
        TocElement tocElement = mToc.getElementById(varId);
        if (tocElement != null) {
            //s = struct.unpack(element.pytype, pk.data[1:])[0]
            //s = s.__str__()
            // TODO: probably does not work as intended, use System.arrayCopy instead
            ByteBuffer payload = ByteBuffer.wrap(packet.getPayload(), 1, packet.getPayload().length-1);
            Number number = tocElement.getCtype().parse(payload);

            String completeName = tocElement.getCompleteName();

            // Save the value for synchronous access
            if (!mValues.containsKey(tocElement.getGroup())) {
                mValues.put(tocElement.getGroup(), new HashMap<String, Number>());
            }
            mValues.get(tocElement.getGroup()).put(tocElement.getName(), number);

            // This will only be called once
            if (checkIfAllUpdated() && !mHaveUpdated) {
                mHaveUpdated = true;
                // self.all_updated.call()
            }
            mLogger.debug("Updated parameter " + completeName);
            /*
            if complete_name in self.param_update_callbacks:
                self.param_update_callbacks[complete_name].call(complete_name, s)
            if element.group in self.group_update_callbacks:
                self.group_update_callbacks[element.group].call(complete_name, s)
            */
        } else {
            mLogger.debug("Variable id " + varId + " not found in TOC");
        }
    }

    public Map<String, Map<String, Number>> getValuesMap() {
        return mValues;
    }

    /**
     * Remove the supplied callback for a group or a group.name
     */
    //def remove_update_callback(self, group, name=None, cb=None):
    public void removeUpdateCallback() {
            /*
        if not cb:
            return

        if not name:
            if group in self.group_update_callbacks:
                self.group_update_callbacks[group].remove_callback(cb)
        else:
            paramname = "{}.{}".format(group, name)
            if paramname in self.param_update_callbacks:
                self.param_update_callbacks[paramname].remove_callback(cb)
            */
        }

    /**
     * Add a callback for a specific parameter name. This callback will be
     * executed when a new value is read from the Crazyflie.
     */
    //def add_update_callback(self, group, name=None, cb=None):
    public void addUpdateCallback() {
            /*
        if not name:
            if not group in self.group_update_callbacks:
                self.group_update_callbacks[group] = Caller()
                self.group_update_callbacks[group].add_callback(cb)
        else:
            paramname = "{}.{}".format(group, name)
            if not paramname in self.param_update_callbacks:
                self.param_update_callbacks[paramname] = Caller()
            self.param_update_callbacks[paramname].add_callback(cb)
            */
        }

    /**
     * Initiate a refresh of the parameter TOC.
     */
    // def refresh_toc(self, refresh_done_callback, toc_cache):
    public void refreshToc(TocFetchFinishedListener listener, TocCache tocCache) {
       this.mToc = new Toc();
       // toc_fetcher = TocFetcher(self.cf, ParamTocElement, CRTPPort.PARAM, self.toc, refresh_done_callback, toc_cache)
       TocFetcher tocFetcher = new TocFetcher(mCrazyflie, CrtpPort.PARAMETERS, mToc, tocCache);
       tocFetcher.addTocFetchFinishedListener(listener);
       tocFetcher.start();
    }

    //TODO: only for debugging
    public Toc getToc() {
        return this.mToc;
    }

    /*
    def disconnected(self, uri):
        """Disconnected callback from Crazyflie API"""
        self.param_updater.close()
        self._have_updated = False

     */

    /**
     * Request an update of the value for the supplied parameter.
     *
     * @param completeName
     */
    //TODO: public?
    public void requestParamUpdate(String completeName) {
        // self.param_updater.request_param_update(self.toc.get_element_id(complete_name))
        mPut.requestParamUpdate(mToc.getElementId(completeName));
    }


    /**
     * Set the value for the supplied parameter.
     *
     * @param completeName
     * @param value
     */
    //TODO: is Number the right data type for value?
    public void setValue(String completeName, Number value) {
        TocElement tocElement = mToc.getElementByCompleteName(completeName);
        if (tocElement == null) {
            mLogger.warn("Cannot set value for " + completeName + ", it's not in the TOC!");
        } else if (tocElement.getAccess() == TocElement.RO_ACCESS) {
            mLogger.debug(completeName + " is read only, not trying to set value");
        } else {
            //TODO: extract into method
            Header header = new Header(WRITE_CHANNEL, CrtpPort.PARAMETERS);
            //pk.data = struct.pack('<B', varid)
            //pk.data += struct.pack(element.pytype, eval(value))
            //TODO: value.byteValue() might not be the right method to use, because it can involve rounding or truncation!
            byte[] parse = tocElement.getCtype().parse(value);
            ByteBuffer bb = ByteBuffer.allocate(parse.length+1);
            bb.put((byte) tocElement.getIdent());
            bb.put(parse);
            CrtpPacket packet = new CrtpPacket(header.getByte(), bb.array());
            //self.param_updater.request_param_setvalue(pk)
            mPut.requestParamSetValue(packet);
        }
    }

    /**
     * This thread will update params through a queue to make sure that we get back values
     *
     */
    //TODO: when is this thread ever interrupted?
    public class ParamUpdaterThread implements Runnable {

        private final BlockingDeque<CrtpPacket> mRequestQueue = new LinkedBlockingDeque<CrtpPacket>();
        private int mReqParam = -1;

        /**
         * Initialize the thread
         */
        public ParamUpdaterThread() {
            /*
            Thread.__init__(self)
            self.setDaemon(True)
            self.wait_lock = Lock()
            self.updated_callback = updated_callback
            */

            // self.cf.add_port_callback(CRTPPort.PARAM, self._new_packet_cb)
            mCrazyflie.addDataListener(new DataListener(CrtpPort.PARAMETERS) {
                @Override
                public void dataReceived(CrtpPacket packet) {
                    newPacketReceived(packet);
                }
            });
        }

        // def close(self, uri):
        public void close () {
            /*
            # First empty the queue from all packets
            while not self.request_queue.empty():
                self.request_queue.get()
            # Then force an unlock of the mutex if we are waiting for a packet
            # we didn't get back due to a disconnect for example.
            try:
                self.wait_lock.release()
            except:
                pass
             */
            mRequestQueue.clear();
        }

        /**
         * Place a param set value request on the queue. When this is sent to
         * the Crazyflie it will answer with the updated param value.
         *
         * @param packet
         */
        public void requestParamSetValue(CrtpPacket packet) {
            try {
                //TODO: is put() the right method?
                mRequestQueue.put(packet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Callback for newly arrived packets
         *
         * @param packet
         */
        private void newPacketReceived(CrtpPacket packet) {
            int channel = packet.getHeader().getChannel();
            if (channel == READ_CHANNEL || channel == WRITE_CHANNEL) {
                int varId = packet.getPayload()[0];
                //if (pk.channel != TOC_CHANNEL and self._req_param == var_id and pk is not None):
                if (channel != TOC_CHANNEL && mReqParam == varId) {
                    //self.updated_callback(pk)
                    paramUpdated(packet);
                    //self._req_param = -1
                    mReqParam = -1;
                    /*
                    try:
                        self.wait_lock.release()
                    except:
                        pass
                    */
                }
            }
        }

        /**
         * Place a param update request on the queue
         *
         * @param varId
         */
        public void requestParamUpdate(int varId) {
            Header header = new Header(READ_CHANNEL, CrtpPort.PARAMETERS);
            CrtpPacket packet = new CrtpPacket(header.getByte(), new byte[]{(byte) varId});
            mLogger.debug("Requesting update for param with ID " + varId);
            try {
                //TODO: is put() the right method?
                mRequestQueue.put(packet);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (true) {
                CrtpPacket packet = null;
                try {
                    //pk = self.request_queue.get()  # Wait for request update
                    //TODO: is "take()" the right method?
                    packet = mRequestQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //self.wait_lock.acquire()
                //if self.cf.link:
                if (mCrazyflie.getDriver() != null && packet != null) {
                    if (packet.getPayload().length > 0) {
                        mReqParam = packet.getPayload()[0];
                        //self.cf.send_packet(pk, expected_reply=(pk.datat[0:2]))
                        packet.setExpectedReply(new byte[]{packet.getPayload()[0]});
                        mCrazyflie.sendPacket(packet);
                    }
                } else {
                    //self.wait_lock.release()
                }
                //TODO: is this sleep necessary?
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
