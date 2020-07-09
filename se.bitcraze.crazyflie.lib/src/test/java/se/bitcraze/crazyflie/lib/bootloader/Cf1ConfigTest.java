package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.MockDriver;
import se.bitcraze.crazyflie.lib.OfflineTests;
import se.bitcraze.crazyflie.lib.TestUtilities;
import se.bitcraze.crazyflie.lib.Utilities;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

@Category(OfflineTests.class)
@SuppressWarnings("java:S106")
public class Cf1ConfigTest {

    private static final String RESTART_THE_CRAZYFLIE_MSG = "Restart the Crazyflie you want to bootload in the next 10 seconds ...";
    private static final String DONE = " Done!";
    private static final String BOOTLOADER_NOT_STARTED = "Bootloader not started.";

    @Test
    public void testCf1Config() {
        int channel = 1;
        int speed = 2;
        int pitchTrim = 1;
        int rollTrim = -2;

        String toString = "CF1Config: Channel: " + channel + ", Speed: " + speed + ", PitchTrim: " + (float) pitchTrim + ", RollTrim: " + (float) rollTrim;

        Cf1Config config = new Cf1Config(channel, speed, pitchTrim, rollTrim);

        byte[] prepareConfig = config.prepareConfig();
        assertEquals(toString, config.toString());

        Cf1Config config2 = new Cf1Config();
        config2.parse(prepareConfig);
        assertEquals(toString, config2.toString());
    }

    @Test
    public void testReadWriteCF1Config() {
        Bootloader mBootloader = null;
        CrtpDriver mDriver = null;

        if (TestUtilities.isCrazyradioAvailable()) {
            mDriver = new RadioDriver(new UsbLinkJava());
        } else {
            mDriver = new MockDriver();
        }
        mBootloader = new Bootloader(mDriver);


        System.out.print(RESTART_THE_CRAZYFLIE_MSG);
        mBootloader.addBootloaderListener(new BootloaderTest().new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(DONE);

            Target target = mBootloader.getCloader().getTargetsAsList().get(0);
            if (target.getFlashPages() != 128) { //128 = CF 1.0, 1024 = CF 2.0
                mBootloader.close();
                fail("testReadWriteCF1Config can only be tested on CF 1.0.");
            }

            // Read original CF1 config
            byte[] cf1ConfigOriginal = mBootloader.readCF1Config();
            System.out.println("CF1 config (original): " + Utilities.getHexString(cf1ConfigOriginal));
            Cf1Config oldConfig = new Cf1Config();
            oldConfig.parse(cf1ConfigOriginal);
            System.out.println("Old config: " + oldConfig);

            // Write new CF1 config
            System.out.println("\nWriting CF1 config ...");
            mBootloader.writeCF1Config(new Cf1Config(11, 2, 4, 3).prepareConfig());

            // Read modified CF1 config (check if write worked)
            byte[] cf1ConfigChanged = mBootloader.readCF1Config();
            System.out.println("\nCF1 config (changed): " + Utilities.getHexString(cf1ConfigChanged));
            System.out.println("Reading config block ...");
            Cf1Config newConfig = new Cf1Config();
            newConfig.parse(cf1ConfigChanged);

            /*
            if data[0:4] == "0xBC":
                # Skip 0xBC and version at the beginning
            */
            //0xBC -> every character is encoded as ascii value in hex (one byte -> one character)

            //TODO: simplify
            byte[] bcArray = new byte[4];
            System.arraycopy(cf1ConfigChanged, 0, bcArray, 0, 4);
            assertArrayEquals(new byte[]{0x30, 0x78, 0x42, 0x43}, bcArray);
            String bcString = new String(bcArray);
            System.out.println("BC: " + bcString);
            assertEquals("0xBC", bcString);

            System.out.println("New config: " + newConfig);

            //Write original CF1 config
            System.out.println("\nResetting CF1 config ...");
            mBootloader.writeCF1Config(oldConfig.prepareConfig());
        } else {
            fail(BOOTLOADER_NOT_STARTED);
        }
    }

    @Test
    public void testCf1ConfigPrepareConfig() {
        Cf1Config cf1Config = new Cf1Config(11,  2,  4,  3);
        String hexString = Utilities.getHexString(cf1Config.prepareConfig());
        System.out.println("Result: " + hexString);
        assertEquals("0x30 0x78 0x42 0x43 0x00 0x0B 0x02 0x00 0x00 0x40 0x40 0x00 0x00 0x80 0x40 0x86 ", hexString);
    }

    @Test
    public void testCf1ConfigChecksum() {
        byte[] array = new byte[] {47,11,42,13};
        assertEquals(113, new Cf1Config().checksum256(array));
    }
}
