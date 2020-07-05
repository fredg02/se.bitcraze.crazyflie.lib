package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.MockDriver;
import se.bitcraze.crazyflie.lib.OfflineTests;
import se.bitcraze.crazyflie.lib.TestUtilities;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

@Category(OfflineTests.class)
@SuppressWarnings("java:S106")
public class CfloaderTest {

    private static final String FLASH = "flash";
    private Cfloader mCfloader;

    @Before
    public void setUp() {
        CrtpDriver driver;
        if (TestUtilities.isCrazyradioAvailable()) {
            driver = new RadioDriver(new UsbLinkJava());
        } else {
            driver = new MockDriver();
        }
        mCfloader = new Cfloader(driver);
    }

    @Test
    public void testCfLoaderInfo() {
        assertTrue(mCfloader.initialiseBootloaderLib(new String[]{"info"}));
    }

    @Test
    public void testCfLoaderReset() {
        assertTrue(mCfloader.initialiseBootloaderLib(new String[]{"reset"}));
    }

    @Test
    public void testCfLoaderflashNoFile() {
        assertTrue(mCfloader.initialiseBootloaderLib(new String[]{FLASH}));
    }

    @Test
    public void testCfLoaderUnknownAction() {
        assertTrue(mCfloader.initialiseBootloaderLib(new String[]{"foobar"}));
    }

    @Test
    public void testCfLoaderflashBin() {
        assertTrue(mCfloader.initialiseBootloaderLib(new String[]{FLASH, "src/test/fw/cf1-2015.08.1.bin"}));
    }

    @Test @Ignore
    public void testCfLoaderflashZip() {
        assertTrue(mCfloader.initialiseBootloaderLib(new String[]{FLASH, "src/test/fw/cf2.2014.12.1.zip"}));
    }

}
