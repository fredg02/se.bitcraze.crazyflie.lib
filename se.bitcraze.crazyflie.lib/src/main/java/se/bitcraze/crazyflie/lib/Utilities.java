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

package se.bitcraze.crazyflie.lib;

public class Utilities {

    private Utilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns byte array as comma separated string
     * (for debugging purposes)
     *
     * @param data
     * @return
     */
    public static String getByteString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(b);
            sb.append(",");
        }
        return sb.toString();
    }

    public static String getHexString(byte... array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02X", b));
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Strip bytes of the beginning of an array
     *
     * @param array
     * @param offset
     * @return
     */
    public static byte[] strip(byte[] array, int offset) {
        byte[] strippedArray = new byte[array.length-offset];
        System.arraycopy(array, offset, strippedArray, 0, strippedArray.length);
        return strippedArray;
    }

}
