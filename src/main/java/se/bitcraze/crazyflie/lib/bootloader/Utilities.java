package se.bitcraze.crazyflie.lib.bootloader;

/**
 * Bootloading utilities for the Crazyflie.
 *
 */
public class Utilities {

    public static class BootVersion {
        public final static int CF1_PROTO_VER_0 = 0x00;
        public final static int CF1_PROTO_VER_1 = 0x01;
        public final static int CF2_PROTO_VER = 0x10;

        public static String toVersionString(int ver) {
            if (ver == BootVersion.CF1_PROTO_VER_0 || ver == BootVersion.CF1_PROTO_VER_1) {
                return "Crazyflie Nano Quadcopter (1.0)";
            } else if (ver == BootVersion.CF2_PROTO_VER) {
                return "Crazyflie 2.0";
            }
            return "Unknown";
        }
    }

}
