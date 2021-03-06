/**
 *    ||          ____  _ __
 * +------+      / __ )(_) /_______________ _____  ___
 * | 0xBC |     / __  / / __/ ___/ ___/ __ `/_  / / _ \
 * +------+    / /_/ / / /_/ /__/ /  / /_/ / / /_/  __/
 *  ||  ||    /_____/_/\__/\___/_/   \__,_/ /___/\___/
 *
 * Copyright (C) 2017 Bitcraze AB
 *
 * Crazyflie Nano Quadcopter Client
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package se.bitcraze.crazyflie.lib.bootloader;

public class BootVersion {

    public static final int CF1_PROTO_VER_0 = 0x00;
    public static final int CF1_PROTO_VER_1 = 0x01;
    public static final int CF2_PROTO_VER = 0x10;

    private BootVersion() {
        throw new IllegalStateException("Utility class");
    }

    public static String toVersionString(int ver) {
        if (ver == BootVersion.CF1_PROTO_VER_0 || ver == BootVersion.CF1_PROTO_VER_1) {
            return "Crazyflie Nano Quadcopter (1.0)";
        } else if (ver == BootVersion.CF2_PROTO_VER) {
            return "Crazyflie 2.0";
        }
        return "Unknown";
    }

}