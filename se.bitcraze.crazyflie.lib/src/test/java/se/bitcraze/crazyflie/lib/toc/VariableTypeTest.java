/**
 *    ||          ____  _ __
 * +------+      / __ )(_) /_______________ _____  ___
 * | 0xBC |     / __  / / __/ ___/ ___/ __ `/_  / / _ \
 * +------+    / /_/ / / /_/ /__/ /  / /_/ / / /_/  __/
 *  ||  ||    /_____/_/\__/\___/_/   \__,_/ /___/\___/
 *
 * Copyright (C) 2015 Bitcraze AB
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

package se.bitcraze.crazyflie.lib.toc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;

@Category(OfflineTests.class)
public class VariableTypeTest {

    private ByteBuffer mBuffer = ByteBuffer.allocate(8);

    @Test
    public void testVariableTypeByteToNumber() {

        // UINT8_T
        mBuffer.put(new byte[] {32,0,0,0});
        mBuffer.rewind();
        assertEquals(32, VariableType.UINT8_T.parse(mBuffer));
        mBuffer.clear();

        mBuffer.put(new byte[] {-32,0,0,0});
        mBuffer.rewind();
        assertEquals(224, VariableType.UINT8_T.parse(mBuffer));
        mBuffer.clear();

        // UINT16_T
        mBuffer.put(new byte[] {-8,-89,0,0});
        mBuffer.rewind();
        assertEquals(43000, VariableType.UINT16_T.parse(mBuffer));
        mBuffer.clear();

        // UINT32_T
        mBuffer.put(new byte[] {73,13,-35,2});
        mBuffer.rewind();
        assertEquals(48041289L, VariableType.UINT32_T.parse(mBuffer));
        mBuffer.clear();

        //TODO: UINT64_T

        // INT8_T
        mBuffer.put(new byte[] {-32,0,0,0});
        mBuffer.rewind();
        assertEquals(-32, VariableType.INT8_T.parse(mBuffer).byteValue());
        mBuffer.clear();

        //TODO: INT16_T

        // INT32_T
        mBuffer.put(new byte[] {-46,2,-106,73});
        mBuffer.rewind();
        assertEquals(1234567890, VariableType.INT32_T.parse(mBuffer).intValue());
        mBuffer.clear();

        // INT64_T
        mBuffer.put(new byte[] {-46,2,-106,73,0,0,0,0});
        mBuffer.rewind();
        assertEquals(1234567890L, VariableType.INT64_T.parse(mBuffer).longValue());
        mBuffer.clear();

        mBuffer.put(new byte[] {121,-33,13,-122,72,112,0,0});
        mBuffer.rewind();
        assertEquals(123456789012345L, VariableType.INT64_T.parse(mBuffer).longValue());
        mBuffer.clear();

        // FLOAT
        mBuffer.put(new byte[] {-20,81,56,62});
        mBuffer.rewind();
        assertEquals(0.18f, VariableType.FLOAT.parse(mBuffer));
        mBuffer.clear();

        //TODO: DOUBLE
    }

    @Test
    public void testVariableTypeNumberToByte() {

        // UINT8_T
        //TODO: is this correct !?
        assertArrayEquals(new byte[] {32,0,0,0}, VariableType.UINT8_T.parse(32));
        assertArrayEquals(new byte[] {-32,0,0,0}, VariableType.UINT8_T.parse(-32));
        //System.out.println(UsbLinkJava.getByteString(VariableType.UINT8_T.parse(200)));
        assertArrayEquals(new byte[] {-56,0,0,0}, VariableType.UINT8_T.parse(200));
        assertArrayEquals(new byte[] {56,0,0,0}, VariableType.UINT8_T.parse(-200));

        // UINT16_T
        assertArrayEquals(new byte[] {-8,-89,0,0}, VariableType.UINT16_T.parse(43000));

        // UINT32_T
        assertArrayEquals(new byte[] {73,13,-35,2}, VariableType.UINT32_T.parse(48041289L));

        //TODO: UINT64_T

        // INT8_T
        assertArrayEquals(new byte[] {32,0,0,0}, VariableType.INT8_T.parse(32));
        assertArrayEquals(new byte[] {-32,0,0,0}, VariableType.INT8_T.parse(-32));

        //TODO: INT16_T

        // INT32_T
        assertArrayEquals(new byte[] {-46,2,-106,73}, VariableType.INT32_T.parse(1234567890L));
        // INT64_T
        assertArrayEquals(new byte[] {-46,2,-106,73,0,0,0,0}, VariableType.INT64_T.parse(1234567890L));
        assertArrayEquals(new byte[] {121,-33,13,-122,72,112,0,0}, VariableType.INT64_T.parse(123456789012345L));

        // FLOAT
        assertArrayEquals(new byte[] {-20,81,56,62}, VariableType.FLOAT.parse(0.18f));

        //TODO: DOUBLE
    }

    @Test
    public void testVariableTypeSize() {
        assertEquals(1, VariableType.UINT8_T.getSize());
        assertEquals(2, VariableType.UINT16_T.getSize());
        assertEquals(4, VariableType.UINT32_T.getSize());
        assertEquals(8, VariableType.UINT64_T.getSize());
        assertEquals(1, VariableType.INT8_T.getSize());
        assertEquals(2, VariableType.INT16_T.getSize());
        assertEquals(4, VariableType.INT32_T.getSize());
        assertEquals(8, VariableType.INT64_T.getSize());
        assertEquals(4, VariableType.FLOAT.getSize());
        assertEquals(8, VariableType.DOUBLE.getSize());
    }

    @Test
    public void testParseVariableType4byte_negativeTest() {
        // buffer capacity is smaller than 4, therefore the parse method should return -1
        ByteBuffer testBuffer = ByteBuffer.allocate(3);
        testBuffer.put(new byte[] {32,0,0});
        testBuffer.rewind();
        assertEquals(-1, VariableType.UINT8_T.parse(testBuffer));
        testBuffer.clear();
    }

    @Test
    public void testParseVariableType4byte_positiveTest() {
        ByteBuffer testBuffer = ByteBuffer.allocate(4);
        testBuffer.put(new byte[] {32,0,0,0});
        testBuffer.rewind();
        assertEquals(32, VariableType.UINT8_T.parse(testBuffer));
        testBuffer.clear();
    }

    @Test
    public void testParseVariableType8byte_negativeTest() {
        // buffer capacity is smaller than 8, therefore the parse method should return -1
        ByteBuffer testBuffer = ByteBuffer.allocate(7);
        testBuffer.put(new byte[] {-46,2,-106,73,0,0,0});
        testBuffer.rewind();
        assertEquals(-1, VariableType.INT64_T.parse(testBuffer));
        testBuffer.clear();
    }

    @Test
    public void testParseVariableType8byte_positiveTest() {
        ByteBuffer testBuffer = ByteBuffer.allocate(8);
        testBuffer.put(new byte[] {-46,2,-106,73,0,0,0,0});
        testBuffer.rewind();
        assertEquals(1234567890L, VariableType.INT64_T.parse(testBuffer));
        testBuffer.clear();
    }
}
