package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

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
            assertTrue(bootloader.getProtocolVersion() == BootVersion.CF1_PROTO_VER_0 ||
                       bootloader.getProtocolVersion() == BootVersion.CF1_PROTO_VER_1 ||
                       bootloader.getProtocolVersion() == BootVersion.CF2_PROTO_VER);
        } else {
            fail("Bootloader not started.");
        }
        bootloader.close();
    }

}
