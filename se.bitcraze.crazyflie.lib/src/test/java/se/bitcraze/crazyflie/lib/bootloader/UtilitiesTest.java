package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;
import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;

@Category(OfflineTests.class)
@SuppressWarnings("java:S106")
public class UtilitiesTest {

    @Test
    public void testBootVersion() {
        assertEquals("Crazyflie Nano Quadcopter (1.0)", BootVersion.toVersionString(BootVersion.CF1_PROTO_VER_0));
        assertEquals("Crazyflie Nano Quadcopter (1.0)", BootVersion.toVersionString(BootVersion.CF1_PROTO_VER_1));
        assertEquals("Crazyflie 2.0", BootVersion.toVersionString(BootVersion.CF2_PROTO_VER));
        assertEquals("Unknown", BootVersion.toVersionString(0x12));
    }

    @Test
    public void testTargetTypes() {
        assertEquals("stm32", TargetTypes.toString(TargetTypes.STM32));
        assertEquals("nrf51", TargetTypes.toString(TargetTypes.NRF51));
        assertEquals("Unknown", TargetTypes.toString(0xBB));

        assertEquals(TargetTypes.STM32, TargetTypes.fromString("stm32"));
        assertEquals(TargetTypes.NRF51, TargetTypes.fromString("nrf51"));
        assertEquals(0, TargetTypes.fromString("foobar"));
    }

    @Test
    public void testTarget() {
        //TODO fix this
        Target t0 = new Target(0xFF);
        System.out.println(t0.toString());

        Target t1 = new Target(0xFE);
        System.out.println(t1.toString());

        Target t2 = new Target(123);
        System.out.println(t2.toString());
    }

    @Test
    public void testSetAddress() throws Exception {
        //TODO: is this working as expected?
        byte[] newAddress = new byte[5];
        newAddress[0] = (byte) 0xbc;
        for (int n = 1; n < 5; n++) {
            int zahl = (int) ((Math.random()*1000) % 255);
            System.out.print(zahl + ", ");
            newAddress[n] = (byte) zahl;
        }
        System.out.println();
        System.out.println(Arrays.toString(newAddress));
    }
}
