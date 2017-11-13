package se.bitcraze.crazyflie.lib;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.bootloader.Cloader;
import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;


//TODO: only send ACKs when appropriate
public class MockDriver extends RadioDriver  {

    final static Logger mLogger = LoggerFactory.getLogger("MockDriver");
    private final BlockingDeque<CrtpPacket> mInQueue = new LinkedBlockingDeque<CrtpPacket>();

    public static final int CF1 = 1;
    public static final int CF2 = 2;

    private int mCFmodel;

    public MockDriver(int cf) {
        super(null);
        this.mCFmodel = cf;
    }

    public MockDriver() {
        this(CF2);
    }

    @Override
    public void connect() {
        mLogger.debug("MockDriver connect()");
    }

    @Override
    public void disconnect() {
        mLogger.debug("MockDriver disconnect()");
    }

    @Override
    public void sendPacket(CrtpPacket packet) {
        byte headerByte = packet.getHeaderByte();
        byte[] payload = packet.getPayload();

        mLogger.debug("MockDriver sendPacket header byte: " + UsbLinkJava.getByteString(new byte[] {headerByte}));
        mLogger.debug("MockDriver sendPacket payload: " + UsbLinkJava.getByteString(payload));

        byte[] data = new byte[] {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,}; // default data

        // Bootloader
        if (headerByte == (byte) 0xFF) {
            if (payload[1] == (byte) Cloader.GET_INFO) {
                if (payload[0] == (byte) TargetTypes.STM32) {
                    if (mCFmodel == CF2) {
                        mLogger.debug("Bootloader - Command: GET_INFO - STM32 - CF2");
                        /*
                        OUT:    -1,-1,16,
                        IN:     1,-1,-1,16,0,4,10,0,0,4,16,0,-89,4,48,106,79,-33,34,94,-1,-27,20,-112,16,0,0,0,0,0,0,0,0,
                         */
                        data = new byte[] {-1,-1,16,0,4,10,0,0,4,16,0,-89,4,48,106,79,-33,34,94,-1,-27,20,-112,16,0,0,0,0,0,0,0,0};
                    } else if (mCFmodel == CF1) {
                        mLogger.debug("Bootloader - Command: GET_INFO - STM32 - CF1");
                        /*
                        OUT:   -1,-1,16,
                        IN:    17,-1,-1,16,0,4,10,0,-128,0,10,0,80,-1,118,6,73,-123,86,84,81,38,20,-121,1,0,0,0,0,0,0,0,0,
                        */
                        data = new byte[] {-1,-1,16,0,4,10,0,-128,0,10,0,80,-1,118,6,73,-123,86,84,81,38,20,-121,1,0,0,0,0,0,0,0,0};
                    }
                } else if (payload[0] == (byte) TargetTypes.NRF51) {
                    mLogger.debug("Bootloader - Command: GET_INFO - NRF51");
                    /*
                    OUT:    -1,-2,16,
                    IN:     1,-1,-2,16,0,4,1,0,-24,0,88,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                    */
                    data = new byte[] {-1,-2,16,0,4,1,0,-24,0,88,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
                }
            } else if (payload[1] == Cloader.WRITE_FLASH) {
                mLogger.debug("Bootloader - Command: WRITE_FLASH");
                // Reply from CF is always the same
                /*
                OUT:   -1,-1,24,0,0,10,0,10,0,
                IN:    113,-1,-1,24,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                */

                /*
                OUT:   -2,24,0,0,88,0,1,0, //NRF51
                 */
                //TODO: add error messages
                //TODO: adapt better support for NRF51
                data = new byte[] {-1, payload[0],24,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
            } else if (payload[0] == (byte) TargetTypes.NRF51 && payload[1] == (byte) 0xF0) {
                mLogger.debug("Bootloader - Command: Reset to firmware - CF2");
                /*
                Reset to firmware (CF2)
                OUT: -2,-1,1,2,4,5,6,7,8,9,10,11,12,
                OUT: -2,-16,1,
                 */
            } else if (payload[0] == (byte) TargetTypes.STM32 && payload[1] == (byte) 0xF0) {
                mLogger.debug("Bootloader - Command: Reset to firmware - CF1");
                /*
                Reset to firmware (CF1)
                OUT: -1,-1,-1,1,2,4,5,6,7,8,9,10,11,12,
                OUT: -1,-1,-16,1,2,4,5,6,7,8,9,10,11,12,
                */
            } else if (payload[1] == Cloader.READ_FLASH) {
                mLogger.debug("Bootloader - Command: READ_FLASH - CF1");
                // Read CF1 config
                /*
                OUT:   -1,28,127,0,0,0,
                IN:    1,-1,-1,28,127,0,0,0,48,120,66,67,0,10,0,0,0,0,0,0,0,0,0,-55,0,0,0,0,0,0,0,0,0,
                */
                data = new byte[] {-1,-1,28,127,0,0,0,48,120,66,67,0,10,0,0,0,0,0,0,0,0,0,-55,0,0,0,0,0,0,0,0,0};
                //TODO: improve!?
            } else if (payload[1] == Cloader.GET_MAPPING && mCFmodel == CF2) {
                mLogger.debug("Bootloader - Command: GET_MAPPING - CF2");
                // Update mapping
                /*
                OUT:   -1,-1,18,
                IN:     1,-1,-1,18,4,16,1,64,7,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                */
                data = new byte[] {-1,-1,18,4,16,1,64,7,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
                //TODO: improve!?
            }
        }
        // add CRTP packet with mock data to incoming queue
        try {
            CrtpPacket inPacket = new CrtpPacket(data);
            mInQueue.put(inPacket);
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }
    }

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

    @Override
    public boolean scanSelected(ConnectionData connectionData, byte[] packet) {
        return true;
    }

    // TODO: workaround until everything is mocked
    public Crazyradio getRadio() {
        return new Crazyradio(new UsbLinkJava());
    }

}
