package se.bitcraze.crazyflie.lib.bootloader;

import javax.usb.UsbException;

import org.junit.Before;
import org.junit.Test;

import se.bitcraze.crazyflie.lib.MockDriver;
import se.bitcraze.crazyflie.lib.TestUtilities;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class CfloaderTest {

    private CrtpDriver mDriver;
    private Cfloader mCfloader;

    @Before
    public void setUp() throws SecurityException, UsbException {
        if (TestUtilities.isCrazyradioAvailable()) {
            mDriver = new RadioDriver(new UsbLinkJava());
        } else {
            mDriver = new MockDriver(MockDriver.CF1);
        }
        mCfloader = new Cfloader(mDriver);
    }

    @Test
    public void testCfLoaderInfo() {
        mCfloader.initialiseBootloaderLib(new String[]{"info"});
    }

    @Test
    public void testCfLoaderReset() {
        mCfloader.initialiseBootloaderLib(new String[]{"reset"});
    }

    @Test
    public void testCfLoaderflashNoFile() {
        mCfloader.initialiseBootloaderLib(new String[]{"flash"});
    }

    @Test
    public void testCfLoaderUnknownAction() {
        mCfloader.initialiseBootloaderLib(new String[]{"foobar"});
    }

    @Test
    public void testCfLoaderflashBin() {
        mCfloader.initialiseBootloaderLib(new String[]{"flash", "src/test/fw/cf1-2015.08.1.bin"});
    }

    @Test
    public void testCfLoaderflashZip() {
        mCfloader.initialiseBootloaderLib(new String[]{"flash", "src/test/fw/cf2.2014.12.1.zip"});
    }

}
