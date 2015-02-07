package se.bitcraze.crazyflie.lib.crazyradio;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;


/**
 * Radio link receiver thread is used to read data from the Crazyradio USB driver.
 */
// Transmit/receive radio thread
public class RadioDriverThread implements Runnable {

    final Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final static int RETRYCOUNT_BEFORE_DISCONNECT = 10;
    private Crazyradio mCradio;
    private BlockingDeque<CrtpPacket> mInQueue;
    private BlockingDeque<CrtpPacket> mOutQueue;
    private int mRetryBeforeDisconnect;

    /**
     * Create the object
     */
    public RadioDriverThread(Crazyradio cradio, BlockingDeque<CrtpPacket> inQueue, BlockingDeque<CrtpPacket> outQueue) {
        this.mCradio = cradio;
        this.mInQueue = inQueue;
        this.mOutQueue = outQueue;
        /*
        self.link_error_callback = link_error_callback
        self.link_quality_callback = link_quality_callback
        */
        this.mRetryBeforeDisconnect = RETRYCOUNT_BEFORE_DISCONNECT;
    }

    /**
     * Run the receiver thread
     *
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        byte[] dataOut = new byte[] {(byte) 0xFF}; //Null packet

        double waitTime = 0;
        int emptyCtr = 0;

        while(this.mCradio != null) {
            try {
                RadioAck ackStatus = this.mCradio.sendPacket(dataOut);

                // Analyze the data packet
                if (ackStatus == null) {
                    //if (self.link_error_callback is not None):
                    //    self.link_error_callback("Dongle communication error (ackStatus==None)")
                    mLogger.warn("Dongle communication error (ackStatus == null)");
                    continue;
                }

                /*
                if (self.link_quality_callback is not None):
                    self.link_quality_callback((10 - ackStatus.retry) * 10)
                */

                // If no copter, retry
                //TODO: how is this actually possible?
                if (!ackStatus.isAck()) {
                    this.mRetryBeforeDisconnect--;
                    if (this.mRetryBeforeDisconnect == 0) {
                        mLogger.warn("Too many packets lost");
                        System.err.println("Too many packets lost");
                    }
                    continue;
                }
                this.mRetryBeforeDisconnect = RETRYCOUNT_BEFORE_DISCONNECT;

                byte[] data = ackStatus.getData();

                // if there is a copter in range, the packet is analyzed and the next packet to send is prepared
                if (data != null && data.length > 0) {
                    CrtpPacket inPacket = new CrtpPacket(data);
                    this.mInQueue.put(inPacket);

                    waitTime = 0;
                    emptyCtr = 0;
                } else {
                    emptyCtr += 1;
                    if (emptyCtr > 10) {
                        emptyCtr = 10;
                        // Relaxation time if the last 10 packet where empty
                        waitTime = 0.01;
                    } else {
                        waitTime = 0;
                    }
                }

                // get the next packet to send of relaxation (wait 10ms)
                CrtpPacket outPacket = null;
                outPacket = mOutQueue.pollFirst((long) waitTime, TimeUnit.MILLISECONDS);

                if (outPacket != null) {
                    dataOut = outPacket.toByteArray();
                } else {
                    dataOut = new byte[]{(byte) 0xFF};
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                mLogger.debug("RadioDriverThread was interrupted.");
                break;
            }
        }

    }

}
