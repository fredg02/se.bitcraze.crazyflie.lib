package se.bitcraze.crazyflie.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;


//TODO: only send ACKs when appropriate
public class MockDriver extends RadioDriver {

    final Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public static final int CF1 = 1;
    public static final int CF2 = 2;

    protected int mCFmodel = CF1;

    public MockDriver() {
        super(null);
        this.mCradio = new MockRadio();
    }

    @Override
    public void connect() {
        mLogger.debug("MockDriver connect()");

        notifyConnectionRequested();
        
        // Launch the comm thread
        startSendReceiveThread();
    }

    @Override
    public void disconnect() {
        mLogger.debug("MockDriver disconnect()");
        super.disconnect();
    }

    //TODO: scanInterface?
    
    @Override
    public boolean scanSelected(ConnectionData connectionData, byte[] packet) {
        return true;
    }

    //TODO: setBootloaderAddress?
    
    // TODO: workaround until everything is mocked
//    public Crazyradio getRadio() {
//        return new Crazyradio(new UsbLinkJava());
//    }

}
