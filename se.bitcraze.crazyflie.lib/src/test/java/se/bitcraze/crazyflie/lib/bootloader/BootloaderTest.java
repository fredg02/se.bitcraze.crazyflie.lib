package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.usb.UsbException;

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
import se.bitcraze.crazyflie.lib.bootloader.Utilities.BootVersion;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

//TODO: Fix testCf1ConfigPrepareConfig
public class BootloaderTest {

    private Bootloader mBootloader = null;
    private CrtpDriver mDriver = null;

    @Before
    public void setUp() throws SecurityException, UsbException {
        if (TestUtilities.isCrazyradioAvailable()) {
            mDriver = new RadioDriver(new UsbLinkJava());
        } else {
            mDriver = new MockDriver(MockDriver.CF1);
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
    public void testBootloader() throws InterruptedException {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        mBootloader.addBootloaderListener(new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(" Done!");

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
            fail("Bootloader not started.");
        }
    }

    @Test
    public void testReadWriteCF1Config() throws InterruptedException {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        mBootloader.addBootloaderListener(new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(" Done!");

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
            fail("Bootloader not started.");
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

    @Test
    public void testFlashSingleTarget() throws InterruptedException, IOException {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        mBootloader.addBootloaderListener(new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(" Done!");

            //Load firmware file directly into byte array/buffer
            //Flash firmware

            long startTime = System.currentTimeMillis();

            Target target = mBootloader.getCloader().getTargets().get(TargetTypes.STM32);
            System.out.println("FlashPages: " + target.getFlashPages());
            if (target.getFlashPages() == 128) { //128 = CF 1.0
                System.out.println("CF 1.0");
                mBootloader.flash(new File("src/test/fw/cf1-2015.08.1.bin"), "stm32");
//            mBootloader.flash(new File("Crazyflie1-2015.1.bin"), "stm32");
            } else if (target.getFlashPages() == 1024) { //1024 = CF 2.0
                System.out.println("CF 2.0");
                mBootloader.flash(new File("src/test/fw/cf2-2015.08.1.bin"), "stm32");
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
            fail("Bootloader not started.");
        }
    }

    @Test
    public void testFlashMultipleTargets() throws InterruptedException, IOException {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        mBootloader.addBootloaderListener(new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(" Done!");

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
            fail("Bootloader not started.");
        }
    }

    @Test
    public void testGetFlashTargets() throws Exception {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        mBootloader.addBootloaderListener(new BootloaderAdapter());
        if (mBootloader.startBootloader(false)) {
            System.out.println(" Done!");

            ((RadioDriver) this.mDriver).stopSendReceiveThread();

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
            assertEquals("#2 Should return an empty list.", 0, mBootloader.getFlashTargets(new File("src/test/fw/cf1-2015.08.1.bin"), "").size());

            //#3 Bin file with target name given
            List<FlashTarget> targets2 = mBootloader.getFlashTargets(new File("src/test/fw/cf1-2015.08.1.bin"), "stm32");
            assertEquals("#3 Should return a list with size 1.", 1, targets2.size());

            System.out.println("#3 bin file:");
            for (FlashTarget ft : targets2) {
                System.out.println("\t" + ft);
            }
            System.out.println();

            //TODO: works, even though file is not found!!!
            //#4 Bin file with more than one target name given -> should return an empty list and an error
            List<FlashTarget> targets3 = mBootloader.getFlashTargets(new File("cf1-2015.08.1.bin"), new String[] {"stm32", "nrf51"});
            assertEquals("Should return a list with size 0.", 0, targets3.size());

            //TODO: clean up, delete unzipped files

        } else {
            mBootloader.close();
            fail("Bootloader not started.");
        }
        mBootloader.close();
    }

    /**
     * Tests that only one STM32 flash target for CF1 is found
     * 
     * @throws IOException
     */
    @Test
    public void testFW201602_cf1() throws IOException {
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
    public void testFW201602_cf2() throws IOException {
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

    @Category(OfflineTests.class)
    @Test
    public void testReadManifest() throws IOException {
        Manifest readManifest = Bootloader.readManifest(new File("src/test/manifest.json"));

        assertNotNull(readManifest);
        System.out.println("Version: " + readManifest.getVersion());
        for (String name : readManifest.getFiles().keySet()) {
            System.out.println("Name: " + name);
            System.out.println(readManifest.getFiles().get(name).toString());
        }

        assertEquals(1, readManifest.getVersion());
        assertTrue(readManifest.getFiles().containsKey("cflie2.bin"));
        FirmwareDetails firmwareDetails1 = readManifest.getFiles().get("cflie2.bin");
        assertEquals("cf2", firmwareDetails1.getPlatform());
        assertEquals("stm32", firmwareDetails1.getTarget());
        assertEquals("fw", firmwareDetails1.getType());

        assertTrue(readManifest.getFiles().containsKey("cf2_nrf_1.1.bin"));
        FirmwareDetails firmwareDetails2 = readManifest.getFiles().get("cf2_nrf_1.1.bin");
        assertEquals("cf2", firmwareDetails2.getPlatform());
        assertEquals("nrf51", firmwareDetails2.getTarget());
        assertEquals("fw", firmwareDetails2.getType());
    }

    @Category(OfflineTests.class)
    @Test
    public void writeManifest() throws IOException {
        Manifest manifest = new Manifest();
        manifest.setVersion(1);
        Map<String, FirmwareDetails> map = new HashMap<String, FirmwareDetails>();
        FirmwareDetails firmwareDetails = new FirmwareDetails("cf2", "stm32", "fw", "2015.01.11", "release-repo");
        map.put("cflie2.bin", firmwareDetails);
        manifest.setFiles(map);

        String testFileName = "manifestTest.json";
        Bootloader.writeManifest(testFileName, manifest);

        File testFile = new File(testFileName);
        assertTrue("Test file should exist.", testFile.exists());
        assertTrue("Test file should have a length > 0.", testFile.length() > 0);

//        System.out.println("Version: " + readManifest.getVersion());
//        for (String name : readManifest.getFiles().keySet()) {
//            System.out.println("Name: " + name);
//            System.out.println(readManifest.getFiles().get(name).toString());
//        }

    }

    /* Utility class */

    public class BootloaderAdapter implements BootloaderListener {

        public void updateProgress(int progress, int max) {
            System.out.println("Update progress: " +  progress + " max: " + max);
        }

        public void updateStatus(String status) {
            System.out.println("Update status: " +  status);
        }

        public void updateError(String error) {
            System.err.println("Update error: " + error);
        }

    }

}
