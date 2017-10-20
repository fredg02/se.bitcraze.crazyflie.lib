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

package se.bitcraze.crazyflie.lib.param;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.VariableType;

@Category(OfflineTests.class)
public class ParamTocElementTest {


    @Test
    public void testParamTocElement() {

        // First three bytes of payload need to be stripped away?

        //UINT8_T
        //original byte: 1,32,0,0,72,105,109,117,95,116,101,115,116,115,0,77,80,85,54,48,53,48,0,0,0,0,0,0,0,0,0,0,0
        byte[] id0 = new byte[] {0,72,105,109,117,95,116,101,115,116,115,0,77,80,85,54,48,53,48,0,0,0,0,0,0,0,0,0,0,0};
        TocElement pteId00 = new TocElement(CrtpPort.PARAMETERS, id0);
        checkTocElement(pteId00, "imu_tests", "MPU6050", VariableType.UINT8_T, TocElement.RO_ACCESS);
        assertEquals(0, pteId00.getIdent());                              //ID can change after firmware update

        //UINT16_T
        //original byte: 1,32,0,6,73,99,112,117,0,102,108,97,115,104,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        byte[] id6 = new byte[] {6,73,99,112,117,0,102,108,97,115,104,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        TocElement pteId06 = new TocElement(CrtpPort.PARAMETERS, id6);
        checkTocElement(pteId06, "cpu", "flash", VariableType.UINT16_T, TocElement.RO_ACCESS);
        assertEquals(6, pteId06.getIdent());                              //ID can change after firmware update

        //UINT32_T
        //original byte: 1,32,0,7,74,99,112,117,0,105,100,48,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        byte[] id7 = new byte[] {7,74,99,112,117,0,105,100,48,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        TocElement pteId07 = new TocElement(CrtpPort.PARAMETERS, id7);
        checkTocElement(pteId07, "cpu", "id0", VariableType.UINT32_T, TocElement.RO_ACCESS);
        assertEquals(7, pteId07.getIdent());                              //ID can change after firmware update

        //FLOAT
        //original byte: 1,32,0,11,6,112,105,100,95,114,97,116,101,0,114,111,108,108,95,107,112,0,0,0,0,0,0,0,0,0,0,0,0
        byte[] id11 = new byte[] {11,6,112,105,100,95,114,97,116,101,0,114,111,108,108,95,107,112,0,0,0,0,0,0,0,0,0,0,0,0};
        TocElement pteId11 = new TocElement(CrtpPort.PARAMETERS, id11);
        checkTocElement(pteId11, "pid_rate", "roll_kp", VariableType.FLOAT, TocElement.RW_ACCESS);
        assertEquals(11, pteId11.getIdent());                              //ID can change after firmware update
    }

    private static void checkTocElement(TocElement pte, String group, String name, VariableType ctype, int access) {
        assertEquals(group, pte.getGroup());
        assertEquals(name, pte.getName());
        assertEquals(group + "." + name, pte.getCompleteName());
        assertEquals(ctype, pte.getCtype());
        assertEquals(access, pte.getAccess());
        assertEquals(access == TocElement.RW_ACCESS ? "RW" : "RO", pte.getReadableAccess());
    }
}
