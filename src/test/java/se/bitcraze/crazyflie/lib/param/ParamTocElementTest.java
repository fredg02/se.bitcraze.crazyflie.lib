package se.bitcraze.crazyflie.lib.param;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.VariableType;

public class ParamTocElementTest {


    @Test
    public void testParamTocElement() {

        // First three bytes of payload need to be stripped away?

        //UINT8_T
        //original byte: 1,32,0,0,72,105,109,117,95,116,101,115,116,115,0,77,80,85,54,48,53,48,0,0,0,0,0,0,0,0,0,0,0
        byte[] id0 = new byte[] {0,72,105,109,117,95,116,101,115,116,115,0,77,80,85,54,48,53,48,0,0,0,0,0,0,0,0,0,0,0};

        ParamTocElement pteId00 = new ParamTocElement(id0);

        assertEquals("imu_tests", pteId00.getGroup());
        assertEquals("MPU6050", pteId00.getName());
        assertEquals("imu_tests.MPU6050", pteId00.getCompleteName());
        assertEquals(VariableType.UINT8_T, pteId00.getCtype());
        assertEquals(TocElement.RO_ACCESS, pteId00.getAccess());
        assertEquals("RO", pteId00.getReadableAccess());
        assertEquals(0, pteId00.getIdent());                              //ID can change after firmware update

        //UINT16_T
        //original byte: 1,32,0,6,73,99,112,117,0,102,108,97,115,104,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        byte[] id6 = new byte[] {6,73,99,112,117,0,102,108,97,115,104,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        ParamTocElement pteId06 = new ParamTocElement(id6);

        assertEquals("cpu", pteId06.getGroup());
        assertEquals("flash", pteId06.getName());
        assertEquals("cpu.flash", pteId06.getCompleteName());
        assertEquals(VariableType.UINT16_T, pteId06.getCtype());
        assertEquals(TocElement.RO_ACCESS, pteId06.getAccess());
        assertEquals("RO", pteId06.getReadableAccess());
        assertEquals(6, pteId06.getIdent());                              //ID can change after firmware update

        //UINT32_T
        //original byte: 1,32,0,7,74,99,112,117,0,105,100,48,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        byte[] id7 = new byte[] {7,74,99,112,117,0,105,100,48,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

        ParamTocElement pteId07 = new ParamTocElement(id7);

        assertEquals("cpu", pteId07.getGroup());
        assertEquals("id0", pteId07.getName());
        assertEquals("cpu.id0", pteId07.getCompleteName());
        assertEquals(VariableType.UINT32_T, pteId07.getCtype());
        assertEquals(TocElement.RO_ACCESS, pteId07.getAccess());
        assertEquals("RO", pteId07.getReadableAccess());
        assertEquals(7, pteId07.getIdent());                              //ID can change after firmware update

        //FLOAT
        //original byte: 1,32,0,11,6,112,105,100,95,114,97,116,101,0,114,111,108,108,95,107,112,0,0,0,0,0,0,0,0,0,0,0,0
        byte[] id11 = new byte[] {11,6,112,105,100,95,114,97,116,101,0,114,111,108,108,95,107,112,0,0,0,0,0,0,0,0,0,0,0,0};

        ParamTocElement pteId11 = new ParamTocElement(id11);

        assertEquals("pid_rate", pteId11.getGroup());
        assertEquals("roll_kp", pteId11.getName());
        assertEquals("pid_rate.roll_kp", pteId11.getCompleteName());
        assertEquals(VariableType.FLOAT, pteId11.getCtype());
        assertEquals(TocElement.RW_ACCESS, pteId11.getAccess());
        assertEquals("RW", pteId11.getReadableAccess());
        assertEquals(11, pteId11.getIdent());                              //ID can change after firmware update

    }
}
