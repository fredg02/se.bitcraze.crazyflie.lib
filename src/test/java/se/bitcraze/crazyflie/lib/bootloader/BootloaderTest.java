package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.bootloader.Utilities.BootVersion;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class BootloaderTest {

    @Test
    public void testBootloader() throws InterruptedException {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        Bootloader bootloader = new Bootloader(new RadioDriver(new UsbLinkJava()));
        if (bootloader.startBootloader(false)) {
            System.out.println(" Done!");

            assertNotNull("Cloader should not be null", bootloader.getCloader());

            assertNotNull("Target STM32 should be found", bootloader.getTarget(TargetTypes.STM32));

            assertTrue(bootloader.getProtocolVersion() == BootVersion.CF1_PROTO_VER_0 ||
                       bootloader.getProtocolVersion() == BootVersion.CF1_PROTO_VER_1 ||
                       bootloader.getProtocolVersion() == BootVersion.CF2_PROTO_VER);

            if (bootloader.getProtocolVersion() == BootVersion.CF2_PROTO_VER) {
                assertNotNull("Target NRF51 should be found", bootloader.getTarget(TargetTypes.NRF51));
            }
        } else {
            fail("Bootloader not started.");
        }
        bootloader.close();
    }

    @Test
    public void testReadWriteCF1Config() throws InterruptedException {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        Bootloader bootloader = new Bootloader(new RadioDriver(new UsbLinkJava()));
        if (bootloader.startBootloader(false)) {
            System.out.println(" Done!");

            Target target = bootloader.getCloader().getTargetsAsList().get(0);
            if (target.getFlashPages() != 128) { //CF 2.0
                fail("testReadWriteCF1Config can only be tested on CF 1.0.");
            }

            // Read original CF1 config
            byte[] cf1ConfigOriginal = bootloader.readCF1Config();
            System.out.println("CF1 config (original): " + Utilities.getHexString(cf1ConfigOriginal));
            Cf1Config oldConfig = new Cf1Config();
            oldConfig.parse(cf1ConfigOriginal);
            System.out.println("Old config: " + oldConfig);

            // Write new CF1 config
            System.out.println("\nWriting CF1 config ...");
            bootloader.writeCF1Config(new Cf1Config(11, 2, 4, 3).prepareConfig());

            // Read modified CF1 config (check if write workded)
            byte[] cf1ConfigChanged = bootloader.readCF1Config();
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
            bootloader.writeCF1Config(oldConfig.prepareConfig());
        } else {
            fail("Bootloader not started.");
        }
        bootloader.close();
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
    public void testFlashSingleTarget() throws InterruptedException {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        Bootloader bootloader = new Bootloader(new RadioDriver(new UsbLinkJava()));
        if (bootloader.startBootloader(false)) {
            System.out.println(" Done!");

            //Load firmware file directly into byte array/buffer
            //Flash firmware

            long startTime = System.currentTimeMillis();
            bootloader.flash(new File("cf1-2015.08.bin"), TargetTypes.STM32);
//            bootloader.flash(new File("Crazyflie1-2015.1.bin"), TargetTypes.STM32);

            System.out.println("Flashing took " + (System.currentTimeMillis() - startTime) + "ms");

            bootloader.resetToFirmware();
            //Check if everything still works
        } else {
            fail("Bootloader not started.");
        }
        bootloader.close();
    }

    @Test @Ignore
    public void testFlashMultipleTargets() throws InterruptedException {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        Bootloader bootloader = new Bootloader(new RadioDriver(new UsbLinkJava()));
        if (bootloader.startBootloader(false)) {
            System.out.println(" Done!");

            //Load firmware from zip file

            //Flash firmware

            //Check if everything still works

        } else {
            fail("Bootloader not started.");
        }
        bootloader.close();
    }

}
