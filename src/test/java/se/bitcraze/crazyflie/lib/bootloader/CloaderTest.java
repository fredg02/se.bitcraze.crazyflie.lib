package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

//TODO: mock bootloader reply packages
//TODO: check that CPU ID is a 12 byte string
//TODO: Fix USB error after reset to firmware
public class CloaderTest {

    /*
     * Output of "cfloader info":
     * Restart the Crazyflie you want to bootload in the next  10 seconds ...  done!
     * Connected to bootloader on Crazyflie Nano Quadcopter (1.0) (version=0x1)
     * Target info: stm32 (0xFF)
     * Flash pages: 128 | Page size: 1024 | Buffer pages: 10 | Start page: 10
     * 118 KBytes of flash available for firmware image.
     */

    @Test
    public void testCloaderCF1() {
        Cloader cloader = new Cloader(new RadioDriver(new UsbLinkJava()));
        System.out.println("Restart the Crazyflie you want to bootload in the next 10 seconds ...");
        ConnectionData bootloaderConnection = cloader.scanForBootloader();
        if (bootloaderConnection != null) {
            System.out.println("BootloaderConnection: " + bootloaderConnection);
            cloader.openBootloaderConnection(bootloaderConnection);
            boolean checkLinkAndGetInfo = cloader.checkLinkAndGetInfo(TargetTypes.STM32); //CF1
            assertTrue(checkLinkAndGetInfo);

            // Test values
            Target targetCF1 = cloader.getTargets().get(0);
            System.out.println(targetCF1.toString());
            assertEquals(0xFF, targetCF1.getId());
            assertEquals(1, targetCF1.getProtocolVersion());
            assertEquals(1024, targetCF1.getPageSize());
            assertEquals(10, targetCF1.getBufferPages());
            assertEquals(128, targetCF1.getFlashPages());
            assertEquals(10, targetCF1.getStartPage());
            //CPU ID is different for every Crazyflie
            //assertEquals("50:FF:76:06:49:85:56:54:51:26:14:87", targetCF1.getCpuId());
            assertEquals(118, targetCF1.getAvailableFlash());
        } else {
            cloader.close();
            fail("No bootloader connection found.");
        }
        cloader.close();
    }

    @Test
    public void testCloaderCF1_resetToFirmware() {
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

            System.out.println("Reset to firmware mode...");
            boolean resetToFirmware = cloader.resetToFirmware(TargetTypes.STM32); //CF1
            assertTrue(resetToFirmware);
            //TODO: check that reset really worked
        } else {
            cloader.close();
            fail("No bootloader connection found.");
        }
        cloader.close();
    }

    @Test
    public void testCloaderCF1_readFlash() {
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
            System.out.println("Flash: " + UsbLinkJava.getByteString(readFlash));

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
