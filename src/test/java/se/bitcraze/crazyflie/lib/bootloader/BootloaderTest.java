package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

            assertNotNull(bootloader.getCloader());

            assertNotNull(bootloader.getTarget(TargetTypes.STM32));

            assertTrue(bootloader.getProtocolVersion() == BootVersion.CF1_PROTO_VER_0 ||
                       bootloader.getProtocolVersion() == BootVersion.CF1_PROTO_VER_1 ||
                       bootloader.getProtocolVersion() == BootVersion.CF2_PROTO_VER);
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

            byte[] cf1Config = bootloader.readCF1Config();
            System.out.println("CF1 config: " + Cloader.getHexString(cf1Config));

            //TODO:
            //store cf1Config

            //write modified cf1Config
            //bootloader.writeCF1Config(cf1Config);

            //check cf1Config, if change can be found

            //reset to original cf1Config
        } else {
            fail("Bootloader not started.");
        }
        bootloader.close();
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
