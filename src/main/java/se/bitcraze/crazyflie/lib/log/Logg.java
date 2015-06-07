package se.bitcraze.crazyflie.lib.log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.DataListener;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket.Header;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.toc.TocCache;
import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.TocFetcher;

//TODO: find better name
//TODO: instead of "block" always use "log config"
public class Logg {

    final Logger mLogger = LoggerFactory.getLogger("Logger");

    // The max size of a CRTP packet payload
    private final static int MAX_LOG_DATA_PACKET_SIZE = 30;

    private Crazyflie mCrazyflie;
    private List<LogConfig> mLogBlocks = new ArrayList<LogConfig>();
    private Toc mToc = null;
    private TocCache mTocCache = null;


    /*
     * These codes can be decoded using os.stderror, but
     * some of the text messages will look very strange
     * in the UI, so they are redefined here
     */
    public enum ErrCodes {
        ENOMEM ("No more memory available"),
        ENOEXEC ("Command not found"),
        ENOENT ("No such block ID"),
        E2BIG ("Block too large"),
        EEXIST ("Block already exists");

        private String mMsg;

        ErrCodes(String msg) {
            this.mMsg = msg;
        }

        public String getMsg() {
            return this.mMsg;
        }
    }

    public Logg(Crazyflie crazyflie) {
        this.mCrazyflie = crazyflie;

        /*
        # Called with newly created blocks
        self.block_added_cb = Caller()
         */

        // self.cf.add_port_callback(CRTPPort.LOGGING, self._new_packet_cb)
        mCrazyflie.addDataListener(new DataListener(CrtpPort.LOGGING) {
            @Override
            public void dataReceived(CrtpPacket packet) {
                newPacketReceived(packet);
            }
        });

        /*
        self.toc_updated = Caller()
        self.state = IDLE
        self.fake_toc_crc = 0xDEADBEEF

        self._refresh_callback = None
        */
    }

    /**
     * Add a log configuration to the logging framework
     *
     * When doing this the contents of the log configuration will be validated
     * and listeners for new log configurations will be notified.
     * When validating the configuration the variables are checked against the TOC
     * to see that they actually exist. If they don't then the configuration
     * cannot be used. Since a valid TOC is required, a Crazyflie has to be
     * connected when calling this method, otherwise it will fail.
     *
     * @param logConfig
     */
    public void addConfig(LogConfig logConfig) {
        //TODO: check if really connected
        if (this.mCrazyflie.getDriver() == null) {
            mLogger.error("Cannot add configs without being connected to a Crazyflie!");
            return;
        }
        /*
         * If the log configuration contains variables that we added without
         * type (i.e we want the stored as type for fetching as well) then
         * resolve this now and add them to the block again.
         */
        for(LogVariable logVariable : logConfig.getLogVariables()) {
            if (logVariable.getVariableType() == null) {
                String name = logVariable.getName();
                TocElement tocElement = mToc.getElementByCompleteName(name);
                if (tocElement == null) {
                    mLogger.warn(name + "is not in TOC, this block cannot be used!");
                    logConfig.setValid(false);
                    // raise KeyError("Variable {} not in TOC".format(name))
                } else {
                    // Now that we know what type this variable has, set the correct type
                    logVariable.setVariableType(tocElement.getCtype());
                }
            }
        }
        /*
         * Now check that all the added variables are in the TOC and that
         * the total size constraint of a data packet with logging data is not ???
         */

        // TODO: iterate only once over all log variables?

        int size = 0;
        for (LogVariable logVariable : logConfig.getLogVariables()) {
            // size += LogTocElement.get_size_from_id(var.fetch_as)
            size += logVariable.getVariableType().getSize();

            /*
             * Check that we are able to find the variable in the TOC so
             * we can return error already now and not when the config is sent
             */

            // TODO: seems to be redundant
            if (logVariable.isTocVariable()) {
                if (mToc.getElementByCompleteName(logVariable.getName()) == null) {
                    mLogger.warn("Log: " + logVariable.getName() + " not in TOC, this block cannot be used!");
                    logConfig.setValid(false);
                    // raise KeyError("Variable {} not in TOC".format(var.name))
                }
            }
        }

        if (size <= MAX_LOG_DATA_PACKET_SIZE && (logConfig.getPeriod() > 0 && logConfig.getPeriod() < 0xFF)) {
            logConfig.setValid(true);
            // logconf.cf = self.cf         -> not necessary in Java
            mLogBlocks.add(logConfig);
            // TODO: self.block_added_cb.call(logconf)
        } else {
            logConfig.setValid(false);
            // raise AttributeError("The log configuration is too large or has an invalid parameter")
        }
    }

    /**
     * Start refreshing the table of loggable variables
     */
    // def refresh_toc(self, refresh_done_callback, toc_cache):
    public void refreshToc(/*RefreshDoneListener listener, */TocCache tocCache) {

        this.mTocCache = tocCache;
        // self._refresh_callback = refresh_done_callback
        this.mToc = null;

        Header header = new Header(CHAN_SETTINGS, CrtpPort.LOGGING);
        CrtpPacket packet = new CrtpPacket(header.getByte(), new byte[]{CMD_RESET_LOGGING});
        packet.setExpectedReply(new byte[]{CMD_RESET_LOGGING});
        this.mCrazyflie.sendPacket(packet);
    }

    private LogConfig findLogConfig (int id) {
        for (LogConfig logConfig : mLogBlocks) {
            if (logConfig.getId() == id) {
                return logConfig;
            }
        }
        return null;
    }

    /**
     * Callback for newly arrived packets with TOC information
     *
     * @param packet
     */
    public void newPacketReceived(CrtpPacket packet) {
        int channel = packet.getHeader().getChannel();

        //TODO
        //cmd = packet.datal[0]
        byte cmd = 1;

        // payload = struct.pack("B" * (len(packet.datal) - 1), *packet.datal[1:])
        byte[] payload = packet.getPayload();

        if (channel == CHAN_SETTINGS) {
            int id = payload[0];
            int errorStatus = payload[1];
            LogConfig logConfig = findLogConfig(id);

            if (cmd == CMD_CREATE_BLOCK) {
                if (logConfig != null) {
                    if (errorStatus == 0x00) {
                        if (!logConfig.isAdded()) {
                            mLogger.debug("Have successfully added id=" + id);

                            // TODO: call start(LogConfig) instead?
                            // TODO: double check with start method (add & start vs just add)
                            Header header = new Header(CHAN_SETTINGS, CrtpPort.LOGGING);
                            CrtpPacket newPacket = new CrtpPacket(header.getByte(), new byte[]{CMD_START_LOGGING, (byte) id, (byte) logConfig.getPeriod()});
                            packet.setExpectedReply(new byte[]{CMD_START_LOGGING, (byte) id});
                            this.mCrazyflie.sendPacket(newPacket);

                            logConfig.setAdded(true);
                        }
                        //TODO else?

                    } else {
                        // msg = self._err_codes[error_status]
                        String msg = ErrCodes.values()[errorStatus].getMsg();
                        mLogger.warn("Error " + errorStatus + " when adding id=" + id + "(" + msg + ")");

                        logConfig.setErrNo(errorStatus);
                        /*
                        TODO:
                        block.added_cb.call(False)
                        block.error_cb.call(block, msg)
                        */
                    }
                } else {
                    mLogger.warn("No LogEntry to assign block to !!!");
                }
            } else if (cmd == CMD_START_LOGGING) {
                if (errorStatus == 0x00) {
                    mLogger.info("Have successfully started logging for id=" +id);
                    if (logConfig != null) {
                        logConfig.setStarted(true);
                    }
                } else {
                    // msg = self._err_codes[error_status]
                    String msg = ErrCodes.values()[errorStatus].getMsg();
                    mLogger.warn("Error " + errorStatus + " when starting id=" + id + "(" + msg + ")");

                    if (logConfig != null) {
                        logConfig.setErrNo(errorStatus);
                        /*
                        block.started_cb.call(False)
                        # This is a temporary fix, we are adding a new issue
                        # for this. For some reason we get an error back after
                        # the block has been started and added. This will show
                        # an error in the UI, but everything is still working.
                        #block.error_cb.call(block, msg)
                        */
                    }
                }
            } else if (cmd == CMD_STOP_LOGGING) {
                if (errorStatus == 0x00) {
                    mLogger.info("Have successfully stopped logging for id=" + id);
                    if (logConfig != null) {
                        logConfig.setStarted(false);
                    }
                }
            } else if (cmd == CMD_DELETE_BLOCK) {
                /*
                 * Accept deletion of a block that isn't added. This could
                 * happen due to timing (i.e add/start/delete in fast sequence)
                 */
                if (errorStatus == 0x00) {
                    mLogger.info("Have successfully deleted id=" + id);
                    if (logConfig != null) {
                        logConfig.setStarted(false);
                        logConfig.setAdded(false);
                    }
                }
            } else if (cmd == CMD_RESET_LOGGING) {
                // Guard against multiple responses due to re-sending
                if (mToc == null) {
                    mLogger.debug("Logging reset, continue with TOC download");
                    mLogBlocks = new ArrayList<LogConfig>();

                    mToc = new Toc();
                    // toc_fetcher = TocFetcher(self.cf, LogTocElement, CRTPPort.LOGGING, self.toc, self._refresh_callback, self._toc_cache)
                    TocFetcher tocFetcher = new TocFetcher(mCrazyflie, CrtpPort.LOGGING, mToc, mTocCache);
                    tocFetcher.start();
                }
            }
        } else if (channel == CHAN_LOGDATA) {
            // TODO: extract into separate method
            int id = payload[0];
            LogConfig logConfig = findLogConfig(id);

            if (logConfig != null) {
                int timestamp = parseTimestamp(payload[1], payload[2], payload[3]);
                // logdata = packet.data[4:]
                int offset = 4;
                byte[] logData = new byte[payload.length-offset];
                System.arraycopy(payload, offset, logData, 0, logData.length);

                logConfig.unpackLogData(logData);
                //TODO: what to do with the unpacked data?
                //TODO: timestamps and callback (either here or in LogConfig.unpackLogData())

                mLogger.debug("Unpacked log data (ID: " + id + ") with timestamp + " + timestamp);
            } else {
                mLogger.warn("Error no LogEntry to handle id=" + id);
            }
        }
    }

    // timestamps = struct.unpack("<BBB", packet.data[1:4])
    // timestamp = (timestamps[0] | timestamps[1] << 8 | timestamps[2] << 16)
    private static int parseTimestamp(byte data1, byte data2, byte data3) {
        //allocate 4 bytes for an int
        ByteBuffer buffer = ByteBuffer.allocate(4).order(CrtpPacket.BYTE_ORDER);
        buffer.put(data1);
        buffer.put(data2);
        buffer.put(data3);
        buffer.rewind();
        return buffer.getInt();
    }


    /* Methods from LogConfig class */

    // Commands used when accessing the Log configurations
    private final static int CMD_CREATE_BLOCK = 0;
    private final static int CMD_APPEND_BLOCK = 1;
    private final static int CMD_DELETE_BLOCK = 2;
    private final static int CMD_START_LOGGING = 3;
    private final static int CMD_STOP_LOGGING = 4;
    private final static int CMD_RESET_LOGGING = 5;


    // Channels used for the logging port
    private final static int CHAN_TOC = 0;
    private final static int CHAN_SETTINGS = 1;
    private final static int CHAN_LOGDATA = 2;


    /*
    this.mId = mConfigIdCounter;
    mConfigIdCounter = (mConfigIdCounter + 1) % 255;
    */

    //TODO: callbacks

    /**
     * Save the log configuration in the Crazyflie
     */
    public void create(LogConfig logConfig) {
        int id = logConfig.getId();

        ByteBuffer bb = ByteBuffer.allocate(31);
        bb.put((byte) CMD_CREATE_BLOCK);
        bb.put((byte) logConfig.getId());

        for (LogVariable variable : logConfig.getLogVariables()) {
            if(!variable.isTocVariable()) { // Memory location
                // logger.debug("Logging to raw memory %d, 0x%04X", var.get_storage_and_fetch_byte(), var.address)
                mLogger.debug("Logging to raw memory.");
                // pk.data += struct.pack('<B', var.get_storage_and_fetch_byte())
                // pk.data += struct.pack('<I', var.address)
                bb.put(new byte[] {(byte) variable.getVariableType().ordinal(), (byte) variable.getAddress()});
            } else { // Item in TOC
                // logger.debug("Adding %s with id=%d and type=0x%02X", var.name, self.cf.log.toc.get_element_id(var.name), var.get_storage_and_fetch_byte())
                mLogger.debug("Adding " + variable.getName() + " with id " + id + " and type " + variable.getVariableType());
                // pk.data += struct.pack('<B', var.get_storage_and_fetch_byte())
                // pk.data += struct.pack('<B', self.cf.log.toc.get_element_id(var.name))
                bb.put(new byte[] {(byte) variable.getVariableType().ordinal(), (byte) mToc.getElementId(variable.getName())});
            }
        }
        mLogger.debug("Adding log block ID " + id);

        // Create packet
        Header header = new Header(CHAN_SETTINGS, CrtpPort.LOGGING);
        CrtpPacket packet = new CrtpPacket(header.getByte(), bb.array());
        packet.setExpectedReply(new byte[]{CMD_CREATE_BLOCK, (byte) id});
        this.mCrazyflie.sendPacket(packet);
    }

    /**
     * Start the logging for this entry
     */
    public void start(LogConfig logConfig) {
        //if (self.cf.link is not None):
        // TODO:
        // if (mCrazyflie.getDriver() != null && mCrazyflie.getDriver().isConnected()) {
        if (mCrazyflie.getDriver() != null) {
            if (!logConfig.isAdded()) {
                create(logConfig);
                mLogger.debug("First time block is started, add block");
            } else {
                mLogger.debug("Block already registered, starting logging for id" + logConfig.getId());
            }

            Header header = new Header(CHAN_SETTINGS, CrtpPort.LOGGING);
            // pk.data = (CMD_START_LOGGING, self.id, self.period)
            CrtpPacket packet = new CrtpPacket(header.getByte(), new byte[] {CMD_START_LOGGING, (byte) logConfig.getId(), (byte) logConfig.getPeriod()});
            packet.setExpectedReply(new byte[]{CMD_START_LOGGING, (byte) logConfig.getId()});
            mCrazyflie.sendPacket(packet);
        }
    }

    /**
     * Stop the logging for this entry
     */
    public void stop(LogConfig logConfig) {
        // TODO:
        // if (mCrazyflie.getDriver() != null && mCrazyflie.getDriver().isConnected()) {
        if (mCrazyflie.getDriver() != null) {
            if (logConfig.getId() == -1) {
                mLogger.warn("Stopping block, but no block registered");
            } else {
                mLogger.debug("Sending stop logging for block id=" + logConfig.getId());
                Header header = new Header(CHAN_SETTINGS, CrtpPort.LOGGING);
                CrtpPacket packet = new CrtpPacket(header.getByte(), new byte[] {CMD_STOP_LOGGING, (byte) logConfig.getId()});
                packet.setExpectedReply(new byte[]{CMD_STOP_LOGGING, (byte) logConfig.getId()});
                mCrazyflie.sendPacket(packet);
            }
        }
    }

    /**
     * Delete this entry in the Crazyflie
     */
    public void delete(LogConfig logConfig) {
        // TODO:
        // if (mCrazyflie.getDriver() != null && mCrazyflie.getDriver().isConnected()) {
        if (mCrazyflie.getDriver() != null) {
            if (logConfig.getId() == -1) {
                mLogger.warn("Delete block, but no block registered");
            } else {
                // mLogger.debug("LogEntry: Sending delete logging for block id=%d" % self.id)
                Header header = new Header(CHAN_SETTINGS, CrtpPort.LOGGING);
                CrtpPacket packet = new CrtpPacket(header.getByte(), new byte[] {CMD_DELETE_BLOCK, (byte) logConfig.getId()});
                packet.setExpectedReply(new byte[]{CMD_DELETE_BLOCK, (byte) logConfig.getId()});
                mCrazyflie.sendPacket(packet);
            }
        }
    }

}
