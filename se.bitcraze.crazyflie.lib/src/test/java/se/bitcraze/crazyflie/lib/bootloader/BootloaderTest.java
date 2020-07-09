package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.MockDriver;
import se.bitcraze.crazyflie.lib.OfflineTests;
import se.bitcraze.crazyflie.lib.TestUtilities;
import se.bitcraze.crazyflie.lib.bootloader.Bootloader.BootloaderListener;
import se.bitcraze.crazyflie.lib.bootloader.Bootloader.FlashTarget;
import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

@Category(OfflineTests.class)
@SuppressWarnings("java:S106")
public class BootloaderTest {

    private static final String STM32 = "stm32";
    private static final String DONE = " Done!";
    private static final String FIRMWARE_FILE_CF1_2015_08_1_BIN = "src/test/fw/cf1-2015.08.1.bin";
    private static final String BOOTLOADER_NOT_STARTED = "Bootloader not started.";
    private static final String RESTART_THE_CRAZYFLIE_MSG = "Restart the Crazyflie you want to bootload in the next 10 seconds ...";

    private Bootloader mBootloader = null;
    private CrtpDriver mDriver = null;

    @Before
    public void setUp() {
        if (TestUtilities.isCrazyradioAvailable()) {
            mDriver = new RadioDriver(new UsbLinkJava());
        } else {
            mDriver = new MockDriver();
        }
        mBootloader = new Bootloader(mDriver);
    }

    @After
    public void tearDown(){
        if(mBootloader != null){
            mBootloader.close();
        }
    }

    @Test
    public void testBootloader() {
        System.out.print(RESTART_THE_CRAZYFLIE_MSG);
        mBootloader.addBootloaderListener(new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(DONE);

            assertNotNull("Cloader should not be null", mBootloader.getCloader());

            assertNotNull("Target STM32 should be found", mBootloader.getTarget(TargetTypes.STM32));

            Target target = mBootloader.getCloader().getTargets().get(TargetTypes.STM32);
            System.out.println("target.getFlashPages(): " + target.getFlashPages());
            System.out.println("bootloader.getProtocolVersion(): " + BootVersion.toVersionString(mBootloader.getProtocolVersion()));
            if (target.getFlashPages() == 128) { //128 = CF 1.0
                assertTrue(mBootloader.getProtocolVersion() == BootVersion.CF1_PROTO_VER_0 ||
                           mBootloader.getProtocolVersion() == BootVersion.CF1_PROTO_VER_1);
            } else if (target.getFlashPages() == 1024) { //1024 = CF 2.0
                assertTrue(mBootloader.getProtocolVersion() == BootVersion.CF2_PROTO_VER);
                assertNotNull("Target NRF51 should be found", mBootloader.getTarget(TargetTypes.NRF51));
            } else {
                fail("Number of flash pages seems to be wrong (" + target.getFlashPages() + ")");
            }
        } else {
            fail(BOOTLOADER_NOT_STARTED);
        }
    }

    @Test
    public void testFlashSingleTarget() throws IOException {
        System.out.print(RESTART_THE_CRAZYFLIE_MSG);
        mBootloader.addBootloaderListener(new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(DONE);

            //Load firmware file directly into byte array/buffer
            //Flash firmware

            long startTime = System.currentTimeMillis();

            Target target = mBootloader.getCloader().getTargets().get(TargetTypes.STM32);
            System.out.println("FlashPages: " + target.getFlashPages());
            if (target.getFlashPages() == 128) { //128 = CF 1.0
                System.out.println("CF 1.0");
                mBootloader.flash(new File(FIRMWARE_FILE_CF1_2015_08_1_BIN), STM32);
//            mBootloader.flash(new File("Crazyflie1-2015.1.bin"), "stm32");
            } else if (target.getFlashPages() == 1024) { //1024 = CF 2.0
                System.out.println("CF 2.0");
                mBootloader.flash(new File("src/test/fw/cf2-2015.08.1.bin"), STM32);
//                mBootloader.flash(new File("src/test/fw/Crazyflie2_2014.12.0.zip"), "");
//                mBootloader.flash(new File("cflie2.bin"), "stm32");
            } else {
                System.err.println("Problem with getFlashPages().");
                return;
            }
            System.out.println("Flashing took " + (System.currentTimeMillis() - startTime)/1000 + "s");

//            mBootloader.resetToFirmware();
            //Check if everything still works
        } else {
            fail(BOOTLOADER_NOT_STARTED);
        }
    }

    @Test
    public void testFlashMultipleTargets() throws IOException {
        System.out.print(RESTART_THE_CRAZYFLIE_MSG);
        mBootloader.addBootloaderListener(new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(DONE);

            Target target = mBootloader.getCloader().getTargets().get(TargetTypes.STM32);
            if (target.getFlashPages() == 128) { //128 = CF 1.0
                //TODO: should this be tested with CF1 as well?
                fail("Test only works with CF2.0.");
            } else if (target.getFlashPages() == 1024) { //1024 = CF 2.0
                mBootloader.flash(new File ("src/test/fw/cf2.2014.12.1.zip"), "");
//            mBootloader.flash(new File ("cf2.2014.12.1.zip"), "stm32");
            }

            //mBootloader.resetToFirmware();
            //TODO: Check if everything still works
        } else {
            fail(BOOTLOADER_NOT_STARTED);
        }
    }

    @Test
    public void testGetFlashTargets() throws IOException {
        System.out.print(RESTART_THE_CRAZYFLIE_MSG);
        mBootloader.addBootloaderListener(new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(DONE);

            ((RadioDriver) this.mDriver).disconnect();

            Target target = mBootloader.getCloader().getTargets().get(TargetTypes.STM32);

            // #1 Zip file
            List<FlashTarget> targets1 = mBootloader.getFlashTargets(new File("src/test/fw/cf2.2014.12.1.zip"), "");
            if (targets1.isEmpty()) {
                fail("No targets found.");
            }
            System.out.println("#1 Zipfile:");
            for (FlashTarget ft : targets1) {
                System.out.println("\t" + ft);
            }

            if (target.getFlashPages() == 128) { //128 = CF 1.0
                assertEquals("#1 Should return a list with size 1.", 1, targets1.size());
            } else if (target.getFlashPages() == 1024) { //1024 = CF 2.0
                assertEquals("#1 Should return a list with size 2.", 2, targets1.size());
            }

            System.out.println();


            //#2 Bin file with no target name given
            assertEquals("#2 Should return an empty list.", 0, mBootloader.getFlashTargets(new File(FIRMWARE_FILE_CF1_2015_08_1_BIN), "").size());

            //#3 Bin file with target name given
            List<FlashTarget> targets2 = mBootloader.getFlashTargets(new File(FIRMWARE_FILE_CF1_2015_08_1_BIN), STM32);
            assertEquals("#3 Should return a list with size 1.", 1, targets2.size());

            System.out.println("#3 bin file:");
            for (FlashTarget ft : targets2) {
                System.out.println("\t" + ft);
            }
            System.out.println();

            //TODO: works, even though file is not found!!!
            //#4 Bin file with more than one target name given -> should return an empty list and an error
            List<FlashTarget> targets3 = mBootloader.getFlashTargets(new File("cf1-2015.08.1.bin"), STM32, "nrf51");
            assertEquals("Should return a list with size 0.", 0, targets3.size());

            //TODO: clean up, delete unzipped files

        } else {
            mBootloader.close();
            fail(BOOTLOADER_NOT_STARTED);
        }
        mBootloader.close();
    }

    /**
     * Tests that only one STM32 flash target for CF1 is found
     *
     * @throws IOException
     */
    @Test
    public void testFW201602CF1() throws IOException {
        //TODO: simplify
        byte[] data = new byte[] {-1,16,0,4,10,0,-128,0,10,0,80,-1,118,6,73,-123,86,84,81,38,20,-121,1,0,0,0,0,0,0,0,0};

        Target target = new Target(0xFF);
        target.parseData(data);

        mBootloader.getCloader().getTargets().put(TargetTypes.STM32, target);
        List<FlashTarget> targets = mBootloader.getFlashTargets(new File("src/test/fw/crazyflie-2016.02.zip"), "");
        for (FlashTarget ft : targets) {
            System.out.println(ft);
        }
        System.out.println();

        // should only find one flash target (for CF1)
        assertEquals(1, targets.size());
        assertEquals(86152, targets.get(0).getData().length);
    }

    /**
     * Tests that only one STM32 flash target for CF2 is found
     * TODO: does not check NRF51 flash target
     *
     * @throws IOException
     */
    @Test
    public void testFW201602CF2() throws IOException {
        //TODO: simplify
        byte[] data = new byte[] {-1,16,0,4,10,0,0,4,16,0,-89,4,48,106,79,-34,98,94,-1,-27,28,16,16,0,0,0,0,0,0,0,0};
        Target target = new Target(0xFF);
        target.parseData(data);

        mBootloader.getCloader().getTargets().put(TargetTypes.STM32, target);
        List<FlashTarget> targets = mBootloader.getFlashTargets(new File("src/test/fw/crazyflie-2016.02.zip"), "");
        for (FlashTarget ft : targets) {
            System.out.println(ft);
        }
        System.out.println();

        // should only find one flash target (for CF2)
        assertEquals(1, targets.size());
        assertEquals(127792, targets.get(0).getData().length);
    }

    /* Utility class */

    public class BootloaderAdapter implements BootloaderListener {

        @Override
        public void updateProgress(int progress, int max) {
            System.out.println("Update progress: " +  progress + " max: " + max);
        }

        @Override
        public void updateStatus(String status) {
            System.out.println("Update status: " +  status);
        }

        @Override
        public void updateError(String error) {
            System.err.println("Update error: " + error);
        }

    }

}
