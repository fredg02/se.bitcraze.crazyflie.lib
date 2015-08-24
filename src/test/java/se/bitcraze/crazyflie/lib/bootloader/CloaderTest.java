package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.bootloader.Utilities.BootVersion;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

//TODO: mock bootloader reply packages
//TODO: Fix USB error after reset to firmware
//TODO: test info on NRF51
public class CloaderTest {

    /*
     * Output of "cfloader info":
     * CF 1.0:
     * Connected to bootloader on Crazyflie Nano Quadcopter (1.0) (version=0x1)
     * Target info: stm32 (0xFF)
     * Flash pages: 128 | Page size: 1024 | Buffer pages: 10 | Start page: 10
     * 118 KBytes of flash available for firmware image.
     *
     * CF 2.0:
     * Connected to bootloader on Crazyflie 2.0 (version=0x10)
     * Target info: nrf51 (0xFE)
     * Flash pages: 232 | Page size: 1024 | Buffer pages: 1 | Start page: 88
     * 144 KBytes of flash available for firmware image.
     * Target info: stm32 (0xFF)
     * Flash pages: 1024 | Page size: 1024 | Buffer pages: 10 | Start page: 16
     * 1008 KBytes of flash available for firmware image.
     *
     */

    @Test
    public void testCloaderInfo() {
        Cloader cloader = new Cloader(new RadioDriver(new UsbLinkJava()));
        System.out.println("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        ConnectionData bootloaderConnection = cloader.scanForBootloader();
        if (bootloaderConnection != null) {
            System.out.println("BootloaderConnection: " + bootloaderConnection);
            cloader.openBootloaderConnection(bootloaderConnection);
            boolean checkLinkAndGetInfo = cloader.checkLinkAndGetInfo(TargetTypes.STM32); //CF1
            assertTrue(checkLinkAndGetInfo);

            // Test values
            Target target = cloader.getTargets().get(0);
            System.out.println(target.toString());

            //CPU ID is different for every Crazyflie, therefore this can't be tested
            //TODO: check that CPU ID is a 12 byte string
            assertEquals(0xFF, target.getId());
            assertEquals(1024, target.getPageSize());
            assertEquals(10, target.getBufferPages());
            if (target.getFlashPages() == 128) { //CF 1.0
                assertEquals(BootVersion.CF1_PROTO_VER_1, target.getProtocolVersion());
                assertEquals(128, target.getFlashPages());
                assertEquals(10, target.getStartPage());
                assertEquals(118, target.getAvailableFlash());
            } else if (target.getFlashPages() == 1024) { //CF 2.0
                assertEquals(BootVersion.CF2_PROTO_VER, target.getProtocolVersion());
                assertEquals(1024, target.getFlashPages());
                assertEquals(16, target.getStartPage());
                assertEquals(1008, target.getAvailableFlash());
            } else {
                fail("Number of flash pages seems to be wrong");
            }
        } else {
            cloader.close();
            fail("No bootloader connection found.");
        }
        cloader.close();
    }

    @Test
    public void testCloader_resetToFirmware() {
        Cloader cloader = new Cloader(new RadioDriver(new UsbLinkJava()));
        System.out.println("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        ConnectionData bootloaderConnection = cloader.scanForBootloader();
        if (bootloaderConnection != null) {
            System.out.println("BootloaderConnection: " + bootloaderConnection);
            cloader.openBootloaderConnection(bootloaderConnection);
            boolean checkLinkAndGetInfo = cloader.checkLinkAndGetInfo(TargetTypes.STM32); //CF1
            assertTrue(checkLinkAndGetInfo);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Target target = cloader.getTargets().get(0);

            System.out.println("Reset to firmware mode...");
            boolean resetToFirmware = false;
            if (target.getFlashPages() == 128) { //CF 1.0
                resetToFirmware = cloader.resetToFirmware(TargetTypes.STM32); //CF1
            } else if (target.getFlashPages() == 1024) { //CF 2.0
                resetToFirmware = cloader.resetToFirmware(TargetTypes.NRF51); //CF1

            }
            assertTrue(resetToFirmware);
            //TODO: check that reset really worked
        } else {
            cloader.close();
            fail("No bootloader connection found.");
        }
        cloader.close();
    }

    @Test @Ignore
    public void testCloader_updateMapping() {
        Cloader cloader = new Cloader(new RadioDriver(new UsbLinkJava()));
        System.out.println("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        ConnectionData bootloaderConnection = cloader.scanForBootloader();
        if (bootloaderConnection != null) {
            System.out.println("BootloaderConnection: " + bootloaderConnection);
            cloader.openBootloaderConnection(bootloaderConnection);
            boolean checkLinkAndGetInfo = cloader.checkLinkAndGetInfo(TargetTypes.STM32); //CF1
            assertTrue(checkLinkAndGetInfo);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Target target = cloader.getTargets().get(0);
            if (target.getFlashPages() == 128) { //CF 1.0
                fail("Update mapping can only be tested on CF 2.0.");
            }
            Integer[] updateMapping = cloader.updateMapping(TargetTypes.STM32);
            System.out.println("UpdateMapping: " + Arrays.toString(updateMapping));

            // TODO: Mapping for STM32: [4, 16, 1, 64, 7, 128]

        } else {
            cloader.close();
            fail("No bootloader connection found.");
        }
        cloader.close();
    }

    @Test
    public void testCloaderCF1_readFlash() throws IOException {
        Cloader cloader = new Cloader(new RadioDriver(new UsbLinkJava()));
        System.out.println("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        ConnectionData bootloaderConnection = cloader.scanForBootloader();
        if (bootloaderConnection != null) {
            System.out.println("BootloaderConnection: " + bootloaderConnection);
            cloader.openBootloaderConnection(bootloaderConnection);
            boolean checkLinkAndGetInfo = cloader.checkLinkAndGetInfo(TargetTypes.STM32); //CF1
            assertTrue(checkLinkAndGetInfo);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Reading flash...");
            byte[] readFlash = cloader.readFlash(0xFF, 0x00);
            assertNotNull("readFlash should not be null", readFlash);
            assertTrue(readFlash.length > 0);
            System.out.println("Readflash length: " + readFlash.length);
            System.out.println("Flash:");
            for(int i = 0; i < 40; i++) {
                System.out.println(UsbLinkJava.getByteString(Arrays.copyOfRange(readFlash, i*25, (i*25)+25)));
            }

            FileOutputStream fos = new FileOutputStream("flashFromCrazyflie.bin");
            fos.write(readFlash);
            fos.close();

            //TODO: check if flash data is correct
        } else {
            cloader.close();
            fail("No bootloader connection found.");
        }
        cloader.close();
    }

    @Test @Ignore
    public void testCloaderCF1_setAddress() {
        Cloader cloader = new Cloader(new RadioDriver(new UsbLinkJava()));
        System.out.println("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        ConnectionData bootloaderConnection = cloader.scanForBootloader();
        if (bootloaderConnection != null) {
            System.out.println("BootloaderConnection: " + bootloaderConnection);
            cloader.openBootloaderConnection(bootloaderConnection);
            boolean checkLinkAndGetInfo = cloader.checkLinkAndGetInfo(TargetTypes.STM32); //CF1
            assertTrue(checkLinkAndGetInfo);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            byte[] newAddress = new byte[]{(byte) 0xE6, (byte) 0xE6, (byte) 0xE6, (byte) 0xE6, (byte) 0xE6};

            System.out.println("Set address to " + Cloader.getHexString(newAddress));
            cloader.setAddress(newAddress);

            //TODO: check address
            //TODO: reset address
        } else {
            cloader.close();
            fail("No bootloader connection found.");
        }
        cloader.close();
    }



}
