package se.bitcraze.crazyflie.lib.toc;


import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.DataListener;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket.Header;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.param.ParamTocElement;

/**
 * Fetches TOC entries from the Crazyflie
 *
 */
public class TocFetcher {

    final Logger mLogger = LoggerFactory.getLogger("TocFetcher");

    private Crazyflie mCrazyFlie;
    private CrtpPort mPort;
    private int mCrc = 0;
    private TocState mState = null;
    private Toc mToc;

    private final int TOC_CHANNEL = 0;
    public static final int CMD_TOC_ELEMENT = 0;
    public static final int CMD_TOC_INFO= 1;

    private int mRequestedIndex = -1;
    private int mNoOfItems = -1;

    private DataListener mDataListener;

    public enum TocState {
        IDLE, GET_TOC_INFO, GET_TOC_ELEMENT, TOC_FETCH_FINISHED;
    }

    public TocFetcher(Crazyflie crazyFlie, CrtpPort port, Toc tocHolder) {
        this.mCrazyFlie = crazyFlie;
        this.mPort = port;
        this.mToc = tocHolder;
    }

    /**
     * Initiate fetching of the TOC
     *
     */
    public void start() {
        mLogger.debug("Starting to fetch TOC (Port: " + this.mPort + ")...");

        mDataListener = new DataListener(this.mPort) {
            @Override
            public void dataReceived(CrtpPacket packet) {
                newPacketReceived(packet);
            }
        };
        this.mCrazyFlie.addDataListener(mDataListener);

        requestTocInfo();
    }


    /**
     * Callback for when the TOC fetching is finished
     */
    public void tocFetchFinished() {
        /*
        self.cf.remove_port_callback(self.port, self._new_packet_cb)
        logger.debug("[%d]: Done!", self.port)
        self.finished_callback()
         */
        // this.mCrazyFlie.removeDataListener(mDataListener);
        mLogger.debug("Fetching TOC (Port: " + this.mPort + ") done.");
        this.mState = TocState.TOC_FETCH_FINISHED;
        // finishedCallback();
        // notifyTocFetchFinished();
    }

    public TocState getState() {
        return this.mState;
    }

    public void newPacketReceived(CrtpPacket packet) {
        if (packet.getHeader().getChannel() != TOC_CHANNEL) {
            return;
        }
        // payload = struct.pack("B" * (len(packet.datal) - 1), *packet.datal[1:])
        ByteBuffer payload = ByteBuffer.wrap(packet.getPayload(), 1, packet.getPayload().length-1);

        if (mState == TocState.GET_TOC_INFO) {
            if (packet.getPayload()[0] == CMD_TOC_INFO) {
                /*
                [self.nbr_of_items, self._crc] = struct.unpack("<BI", payload[:5])
                 */
                this.mNoOfItems = payload.get();
                this.mCrc = payload.getInt();

                mLogger.debug("[" + this.mPort + "]: Got TOC CRC, " + this.mNoOfItems + " items and CRC=" + String.format("0x%08X", this.mCrc));

                this.mState = TocState.GET_TOC_ELEMENT;
                this.mRequestedIndex = 0;
                requestTocElement(this.mRequestedIndex);
            }
        } else if (mState == TocState.GET_TOC_ELEMENT) {

            if (packet.getPayload()[0] == CMD_TOC_ELEMENT) {

            // Always add new element, but only request new if it's not the last one.

            // if self.requested_index != ord(payload[0]):
                if (this.mRequestedIndex != payload.get(1)) {
                /*
                # TODO: There might be a timing issue here with resending old
                #       packets while loosing new ones. Then if 7 is requested
                #       but 6 is send back due to timing issues with the resend
                #       while 7 is lost then we will never resend for 7.
                #       This is pretty hard to reproduce but happens...
                */
                mLogger.warn("[" + this.mPort + "]: Was expecting " + this.mRequestedIndex + " but got " + payload.get(1));
                return;
            }

            // self.toc.add_element(self.element_class(payload))

            if (mPort == CrtpPort.LOGGING) {
                //TODO: mToc.addElement(new LogTocElement(payload));
            } else {
                ParamTocElement paramTocElement = new ParamTocElement(payload.array());
                mToc.addElement(paramTocElement);
            }

            //logger.debug("Added element [%s]", self.element_class(payload).ident)

            if(mRequestedIndex < (mNoOfItems - 1)) {
                    mLogger.debug("[" + this.mPort + "]: More variables, requesting index " + (this.mRequestedIndex + 1));
                    this.mRequestedIndex++;
                    requestTocElement(this.mRequestedIndex);
            } else {
                // No more variables in TOC
                System.out.println("No more variables in TOC.");
                //self._toc_cache.insert(self._crc, self.toc.toc)
                //this.mTocCache.insert(this.mCrc, this.mToc);
                tocFetchFinished();
            }
        }
    }
    }

    private void requestTocInfo() {
        //# Request the TOC CRC
        this.mState = TocState.GET_TOC_INFO;

        mLogger.debug("Requesting TOC info on port " + this.mPort);
        Header header = new Header(TOC_CHANNEL, mPort);
        CrtpPacket packet = new CrtpPacket(header.getByte(), new byte[]{CMD_TOC_INFO});
        packet.setExpectedReply(new byte[]{CMD_TOC_INFO});
        this.mCrazyFlie.sendPacket(packet);
    }

    private void requestTocElement(int index) {
        mLogger.debug("Requesting index " + index + " on port " + this.mPort);
        Header header = new Header(TOC_CHANNEL, this.mPort);
        CrtpPacket packet = new CrtpPacket(header.getByte(), new byte[]{CMD_TOC_ELEMENT, (byte) index});
        packet.setExpectedReply(new byte[]{CMD_TOC_ELEMENT, (byte) index});
        this.mCrazyFlie.sendPacket(packet);
    }

}
