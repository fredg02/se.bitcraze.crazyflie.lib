/*
 *    ||          ____  _ __
 * +------+      / __ )(_) /_______________ _____  ___
 * | 0xBC |     / __  / / __/ ___/ ___/ __ `/_  / / _ \
 * +------+    / /_/ / / /_/ /__/ /  / /_/ / / /_/  __/
 *  ||  ||    /_____/_/\__/\___/_/   \__,_/ /___/\___/
 *
 * Copyright (C) 2016 Bitcraze AB
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

package se.bitcraze.crazyflie.lib.log;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.toc.VariableType;

@Category(OfflineTests.class)
public class LogDataStaticTest {

    /*
     * FLOAT             Header (Port 5, Channel 2)  Block ID    Timestamp           Log values (in little endian format)
     * 
     * original byte: 1, 82,                         1,          67,-58,0,           -59,68,-126,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
     * 
     * 0       BLOCK_ID                ID of the log config block
     * 1       ID                      Timestamp in ms from the copter startup as a little-endian 3 bytes integer
     * 4..     Log variable values     Packed log values in little endian format
     */

    // original array:                                 1,82,1,67,-58,0,-59,68,-126,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
    private final byte[] originalByteArray = new byte[] {82,1,67,-58,0,-59,68,-126,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
    
    private static final LogConfig lc_battery = new LogConfig("battery");
    private static final LogVariable lv_pmVbat = new LogVariable("pm.vbat", VariableType.FLOAT);
    static {
        lc_battery.getLogVariables().add(lv_pmVbat);
    }
    
    private static int TIMESTAMP_VALUE = 50755;
    private static float PM_VBAT_VALUE = 4.0708947f;

    @Test
    public void testManualParseLogData() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(originalByteArray).order(CrtpPacket.BYTE_ORDER);
        
        //strip header byte
        byteBuffer.get();
        
        //get ID?
        byte id = byteBuffer.get();
        System.out.println("ID: " + id);
        
        //TODO: how can timestamp extraction be simplified? (without using another ByteBuffer?)
        //see also se.bitcraze.crazyflie.lib.log.Logg.parseTimestamp(byte, byte, byte)
        
        //get timestamp
        byte[] timestampByteArray = new byte[4];
        byteBuffer.get(timestampByteArray, 0, 3);
        
        ByteBuffer bb = ByteBuffer.wrap(timestampByteArray).order(CrtpPacket.BYTE_ORDER);
        int timestamp = bb.getInt();
        
        System.out.println("Timestamp: " + timestamp);
        
        //get log value
        // Note that only the current position of the ByteBuffer has shifted, the underlying array is still the same
        Float parsedValue = (Float) VariableType.FLOAT.parse(byteBuffer);
        System.out.println("Log value: " + parsedValue);
        
        assertEquals(1, id);
        assertEquals(TIMESTAMP_VALUE, timestamp);
        assertEquals(PM_VBAT_VALUE, parsedValue, 0);
        
        System.out.println();
    }

    @Test
    public void testVariableTypeParse() {
        int offset = 5;
        // Note that the offset will just shift the current position, it does not change the underlying array
        ByteBuffer logVariablesRaw = ByteBuffer.wrap(originalByteArray, offset, originalByteArray.length-offset);
        
        Number parsedValue = lv_pmVbat.getVariableType().parse(logVariablesRaw);
        System.out.println("ParsedValue: " + parsedValue);
        
        assertEquals(PM_VBAT_VALUE, parsedValue);
        
        System.out.println();
    }

    @Test
    public void testLogConfigUnpackLogData() {
        int offset = 5;
        byte[] logVariablesByteArray = new byte[originalByteArray.length-offset];
        System.arraycopy(originalByteArray, offset, logVariablesByteArray, 0, logVariablesByteArray.length);
        
        Map<String, Number> unpackLogData = lc_battery.unpackLogData(logVariablesByteArray);
        System.out.println("LogData (pm.vbat): " + unpackLogData.get("pm.vbat"));

        System.out.println();
    }

    @Test
    public void testLoggParseLogData () {
        CrtpPacket packet = new CrtpPacket(originalByteArray);
        byte[] payload = packet.getPayload();
        
        Map<String, Number> parseLogData = new HashMap<String, Number>();
        int timestamp = Logg.parseLogData(payload, lc_battery, parseLogData);
        for (Entry<String, Number> entry : parseLogData.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", value: " + entry.getValue() + ", timestamp: " + timestamp);
        }
        
        //TODO: test timestamp
        
        assertEquals(PM_VBAT_VALUE, parseLogData.get("pm.vbat"));
        
        System.out.println();
    }

}
