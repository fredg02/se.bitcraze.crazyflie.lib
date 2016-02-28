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

package se.bitcraze.crazyflie.lib.log;

import static org.junit.Assert.assertEquals;

import java.util.Map.Entry;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.param.ParamTocElement;
import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.VariableType;

public class LogTocElementTest {

    @Test
    public void testLogTocElement() {
        // First two !? bytes of payload need to be stripped away?

        //FLOAT
        //original byte: 80,0,0,7,112,109,0,118,98,97,116,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        byte[] id0 = new byte[] {0,7,112,109,0,118,98,97,116,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        LogTocElement lteId00 = new LogTocElement(id0);

        assertEquals("pm", lteId00.getGroup());
        assertEquals("vbat", lteId00.getName());
        assertEquals("pm.vbat", lteId00.getCompleteName());
        assertEquals(VariableType.FLOAT, lteId00.getCtype());
        assertEquals(0, lteId00.getIdent());                              //ID can change after firmware update

        //INT8_T
        //original byte: 80,0,1,4,112,109,0,115,116,97,116,101,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        byte[] id1 = new byte[] {1,4,112,109,0,115,116,97,116,101,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};

        LogTocElement lteId01 = new LogTocElement(id1);

        assertEquals("pm", lteId01.getGroup());
        assertEquals("state", lteId01.getName());
        assertEquals("pm.state", lteId01.getCompleteName());
        assertEquals(VariableType.INT8_T, lteId01.getCtype());
        assertEquals(1, lteId01.getIdent());                              //ID can change after firmware update

        //UINT16_T
        //original byte: 80,0,20,2,115,116,97,98,105,108,105,122,101,114,0,116,104,114,117,115,116,0,0,0,0,0,0,0,0,0,0,0,
        byte[] id20 = new byte[] {20,2,115,116,97,98,105,108,105,122,101,114,0,116,104,114,117,115,116,0,0,0,0,0,0,0,0,0,0,0,};

        LogTocElement lteId20 = new LogTocElement(id20);

        assertEquals("stabilizer", lteId20.getGroup());
        assertEquals("thrust", lteId20.getName());
        assertEquals("stabilizer.thrust", lteId20.getCompleteName());
        assertEquals(VariableType.UINT16_T, lteId20.getCtype());
        assertEquals(20, lteId20.getIdent());                              //ID can change after firmware update

        //INT32_T
        //original byte: 80,0,13,6,109,111,116,111,114,0,109,52,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        byte[] id13 = new byte[] {13,6,109,111,116,111,114,0,109,52,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};

        LogTocElement lteId13 = new LogTocElement(id13);

        assertEquals("motor", lteId13.getGroup());
        assertEquals("m4", lteId13.getName());
        assertEquals("motor.m4", lteId13.getCompleteName());
        assertEquals(VariableType.INT32_T, lteId13.getCtype());
        assertEquals(13, lteId13.getIdent());                              //ID can change after firmware update
    }

    @Test
    public void testGetVariableTypeId() {
        //pm.vbat
        byte[] pmVbat = new byte[] {0,7,112,109,0,118,98,97,116,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        LogTocElement lte_pmVbat = new LogTocElement(pmVbat);
        
        VariableType ctype = lte_pmVbat.getCtype();
        int variableTypeId = lte_pmVbat.getVariableTypeId();
        
        System.out.println("Ctype: " + ctype.name());
        System.out.println("VariableTypeId: " + variableTypeId);
        
        assertEquals(VariableType.FLOAT, ctype);
        assertEquals(7, variableTypeId);
    }

    @Test
    public void testTocElements() {
        System.out.println("LogTocElement VariableTypeMap:");
        LogTocElement logTocElement = new LogTocElement();
        showMap(logTocElement);

        System.out.println("ParamTocElement VariableTypeMap:");
        ParamTocElement paramTocElement = new ParamTocElement();
        showMap(paramTocElement);

        
    }
    
    private void showMap(TocElement tocElement) {
        for (Entry<Integer, VariableType> entry : tocElement.getMap().entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        System.out.println();
    }
}
