package se.bitcraze.crazyflie.lib.crazyradio;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.usb.CrazyUsbInterface;

/**
 * Crazyradio link driver
 *
 */
public class RadioDriver extends CrtpDriver{

    final static Logger mLogger = LoggerFactory.getLogger("RadioDriver");

    private Crazyradio mCradio;
    private Thread mRadioDriverThread;

    private CrazyUsbInterface mUsbInterface;

    private final BlockingDeque<CrtpPacket> mInQueue;
    private final BlockingDeque<CrtpPacket> mOutQueue;

    /**
     * Create the link driver
     */
    public RadioDriver(CrazyUsbInterface usbInterface) {
        this.mUsbInterface = usbInterface;
        this.mCradio = null;
        /*
        self.link_error_callback = None
        self.link_quality_callback = None
        */
        this.mInQueue = new LinkedBlockingDeque<CrtpPacket>();
        this.mOutQueue = new LinkedBlockingDeque<CrtpPacket>(); //TODO: Limit size of out queue to avoid "ReadBack" effect?
        this.mRadioDriverThread = null;
    }

    /* (non-Javadoc)
     * @see se.bitcraze.crazyflie.lib.crtp.CrtpDriver#connect(int, int)
     */
    public void connect(int channel, int datarate) {
        if(mCradio == null) {
            this.mCradio = new Crazyradio(mUsbInterface);
        } else {
            System.err.println("Link already open");
        }

        /*
        if self.cradio is None:
            self.cradio = Crazyradio(devid=int(uri_data.group(1)))
        else:
            raise Exception("Link already open!")
        */

        if (this.mCradio.getVersion() >= 0.4) {
            this.mCradio.setArc(10);
        } else {
            mLogger.warn("Radio version <0.4 will be obsolete soon!");
        }

        this.mCradio.setChannel(channel);
        this.mCradio.setDatarate(datarate);

        /*
        if uri_data.group(9):
            addr = "{:X}".format(int(uri_data.group(9)))
            new_addr = struct.unpack("<BBBBB", binascii.unhexlify(addr))
            self.cradio.set_address(new_addr)
         */

        // Launch the comm thread
        if (mRadioDriverThread == null) {
            //self._thread = _RadioDriverThread(self.cradio, self.in_queue, self.out_queue, link_quality_callback, link_error_callback)
            RadioDriverThread rDT = new RadioDriverThread(this.mCradio, this.mInQueue, this.mOutQueue);
            //FIXME
//            rDT.addPacketListener(packetListener);
            mRadioDriverThread = new Thread(rDT);
            mRadioDriverThread.start();
        }

        //self.link_error_callback = link_error_callback
    }

    /*
     *  Receive a packet though the link. This call is blocking but will
     *  timeout and return None if a timeout is supplied.
     */
    @Override
    public CrtpPacket receivePacket(int time) {
        try {
            return mInQueue.pollFirst((long) time, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            //TODO: does this needs to be dealt with?
            //e.printStackTrace();
            return null;
        }
    }

    /*
     * Send the packet though the link
     *
     *  (non-Javadoc)
     * @see cflib.crtp.CrtpDriver#sendPacket(cflib.crtp.CrtpPacket)
     */
    @Override
    public void sendPacket(CrtpPacket packet) {
        if (this.mCradio == null) {
            return;
        }

        //TODO: does it make sense to be able to queue packets even though
        //the connection is not established yet?

        /*
        try:
            self.out_queue.put(pk, True, 2)
        except Queue.Full:
            if self.link_error_callback:
                self.link_error_callback("RadioDriver: Could not send packet"
                                         " to copter")
        */

        // this.mOutQueue.addLast(packet);
        try {
            this.mOutQueue.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     *  Close the link.
     *
     * (non-Javadoc)
     * @see cflib.crtp.CrtpDriver#close()
     */
    @Override
    public void close() {
        mLogger.debug("close");
        // Stop the comm thread
        if (this.mRadioDriverThread != null) {
            this.mRadioDriverThread.interrupt();
            this.mRadioDriverThread = null;
        }
        if(this.mCradio != null) {
            this.mCradio.close();
            this.mCradio = null;
        }
        if(this.mUsbInterface != null) {
            this.mUsbInterface.releaseInterface();
            this.mUsbInterface = null;
        }
    }

    public List<ConnectionData> scanInterface() {
        return scanInterface(mCradio, mUsbInterface);
    }

    /**
     * Scan interface for Crazyflies
     */
    public static List<ConnectionData> scanInterface(Crazyradio crazyRadio, CrazyUsbInterface crazyUsbInterface) {
        List<ConnectionData> connectionDataList = new ArrayList<ConnectionData>();

        if(crazyRadio == null) {
            crazyRadio = new Crazyradio(crazyUsbInterface);
        } else {
            mLogger.error("Cannot scan for links while the link is open!");
            //TODO: throw exception?
        }

        mLogger.info("Found Crazyradio with version " + crazyRadio.getVersion() + " and serial number " + crazyRadio.getSerialNumber());

        crazyRadio.setArc(1);

        crazyRadio.setDatarate(Crazyradio.DR_250KPS);
        List<Integer> scanRadioChannels250k = crazyRadio.scanChannels();
        for(Integer channel : scanRadioChannels250k) {
            connectionDataList.add(new ConnectionData(channel, Crazyradio.DR_250KPS));
        }
        crazyRadio.setDatarate(Crazyradio.DR_1MPS);
        List<Integer> scanRadioChannels1m = crazyRadio.scanChannels();
        for(Integer channel : scanRadioChannels1m) {
            connectionDataList.add(new ConnectionData(channel, Crazyradio.DR_1MPS));
        }
        crazyRadio.setDatarate(Crazyradio.DR_2MPS);
        List<Integer> scanRadioChannels2m = crazyRadio.scanChannels();
        for(Integer channel : scanRadioChannels2m) {
            connectionDataList.add(new ConnectionData(channel, Crazyradio.DR_2MPS));
        }

//        crazyRadio.close();
//        crazyRadio = null;

        return connectionDataList;
    }

}
