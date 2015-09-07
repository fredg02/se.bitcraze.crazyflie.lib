package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
            System.out.println("CF1 config (original): " + Cloader.getHexString(cf1ConfigOriginal));

            // Write new CF1 config
            System.out.println("Writing CF1 config ...");
            bootloader.writeCF1Config(prepareConfig(11, 2, 4, 3));

            // Read modified CF1 config (check if write workded)
            byte[] cf1ConfigChanged = bootloader.readCF1Config();
            System.out.println("CF1 config (changed): " + Cloader.getHexString(cf1ConfigChanged));
            System.out.println("Reading config block ...");

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

            //[channel, speed, pitchTrim, rollTrim] = struct.unpack("<BBff", data[5:15])
            int offset = 5;
            ByteBuffer cf1ConfigBuffer = ByteBuffer.wrap(cf1ConfigChanged, offset, 10).order(ByteOrder.LITTLE_ENDIAN);
            byte channel = cf1ConfigBuffer.get();
            byte speed = cf1ConfigBuffer.get();
            float pitchTrim = cf1ConfigBuffer.getFloat();
            float rollTrim = cf1ConfigBuffer.getFloat();

            System.out.println("Channel: " + (int) channel);
            System.out.println("Speed: " + (int) speed);
            System.out.println("PitchTrim: " + pitchTrim);
            System.out.println("RollTrim: " + rollTrim);

            //TODO:
            //store cf1Config

            //reset to original cf1Config
        } else {
            fail("Bootloader not started.");
        }
        bootloader.close();
    }

    @Test
    public void testPrepareConfig() {
        String hexString = Cloader.getHexString(prepareConfig(11, 2, 4, 3));
        System.out.println("Result: " + hexString);
        assertEquals("0x30 0x78 0x42 0x43 0x00 0x0B 0x02 0x00 0x00 0x40 0x40 0x00 0x00 0x80 0x40 0x86 ", hexString);
    }

    @Test
    public void testChecksum() {
        byte[] array = new byte[] {47,11,42,13};
        assertEquals(113, checksum256(array));
    }

    //TODO: fix order of rollTrim and pitchTrim?
    private byte[] prepareConfig(int channel, int speed, float rollTrim, float pitchTrim) {
        ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        bb.put((byte) '0');
        bb.put((byte) 'x');
        bb.put((byte) 'B');
        bb.put((byte) 'C');
        bb.put((byte) 0x00);
        bb.put((byte) channel);
        bb.put((byte) speed);
        bb.putFloat(pitchTrim);
        bb.putFloat(rollTrim);
        int checksum = checksum256(bb.array());
        bb.put((byte) (256 - checksum));
        return bb.array();
    }

    private int checksum256(byte[] array) {
        // return reduce(lambda x, y: x + y, map(ord, st)) % 256
        int result = array[0];
        for(int i = 1; i < array.length; i++) {
            result += (int) array[i];
        }
        return result % 256;
    }

    @Test
    public void testInternalFlash() throws InterruptedException {
        System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        Bootloader bootloader = new Bootloader(new RadioDriver(new UsbLinkJava()));
        if (bootloader.startBootloader(false)) {
            System.out.println(" Done!");

            //Load firmware file directly into byte array/buffer

            //Flash firmware

            //Check if everything still works

        } else {
            fail("Bootloader not started.");
        }
        bootloader.close();
    }

    @Test
    public void testFlash() throws InterruptedException {
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
