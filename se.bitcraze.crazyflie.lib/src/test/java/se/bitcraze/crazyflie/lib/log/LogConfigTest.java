package se.bitcraze.crazyflie.lib.log;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.toc.VariableType;

@Category(OfflineTests.class)
public class LogConfigTest {

    // header + blockID(1) + timestamp(3) + logValues(0-28)
    // original: 01 52 01 3D 3C 01 D7 8F 72 40 (00 ...)
    private final static byte[] PAYLOAD_SINGLE = new byte[]{(byte) 0x01, (byte) 0x3D, (byte) 0x3C, (byte) 0x01, (byte) 0xD7, (byte) 0x8F, (byte) 0x72, (byte) 0x40};
    private final static byte[] PAYLOAD_SINGLE_WITHOUT_TIMESTAMP = new byte[]{(byte) 0xD7, (byte) 0x8F, (byte) 0x72, (byte) 0x40};
    private final static int PAYLOAD_SINGLE_TIMESTAMP = 80957;
    private final static float PAYLOAD_SINGLE_PM_VBAT_VALUE = 3.7900293f;

    // original: 01 52 01 AD 28 00 53 E6 72 40 9A 99 B2 41 7C DB 7D 44 (00 ...) => 3.7953079f, 22.325f, 1015.42944f (pm.vbat, baro.temp, baro.pressure)
    private final static byte[] PAYLOAD_MULTI = new byte[]{(byte) 0x01, (byte) 0xAD, (byte) 0x28, (byte) 0x00, (byte) 0x53, (byte) 0xE6, (byte) 0x72, (byte) 0x40, (byte) 0x9A, (byte) 0x99, (byte) 0xB2, (byte) 0x41, (byte) 0x7C, (byte) 0xDB, (byte) 0x7D, (byte) 0x44};
    private final static byte[] PAYLOAD_MULTI_WITHOUT_TIMESTAMP = new byte[]{(byte) 0x53, (byte) 0xE6, (byte) 0x72, (byte) 0x40, (byte) 0x9A, (byte) 0x99, (byte) 0xB2, (byte) 0x41, (byte) 0x7C, (byte) 0xDB, (byte) 0x7D, (byte) 0x44};
    private final static int PAYLOAD_MULTI_TIMESTAMP = 10413;
    private final static float PAYLOAD_MULTI_PM_VBAT_VALUE = 3.7953079f;
    private final static float PAYLOAD_MULTI_BARO_TEMP_VALUE = 22.325f;
    private final static float PAYLOAD_MULTI_BARO_PRESSURE_VALUE = 1015.42944f;

    @Test
    public void testLogConfig() {
        LogConfig testConfig = new LogConfig("testConfig");
        testConfig.setErrNo(2);
        assertEquals("No such log config ID", testConfig.getErrMsg());

        testConfig.setErrNo(7);
        assertEquals("Log config too large", testConfig.getErrMsg());

        testConfig.setErrNo(8);
        assertEquals("Command not found", testConfig.getErrMsg());

        testConfig.setErrNo(12);
        assertEquals("No more memory available", testConfig.getErrMsg());

        testConfig.setErrNo(17);
        assertEquals("Log config already exists", testConfig.getErrMsg());

        testConfig.setErrNo(4);
        assertEquals("Unknown error code", testConfig.getErrMsg());
    }

    @Test
    public void testParseTimestamp_single() {
        int timestamp = Logg.parseTimestamp((byte) 0x3D, (byte) 0x3C, (byte) 0x01);
        assertEquals(PAYLOAD_SINGLE_TIMESTAMP, timestamp);
    }

    @Test
    public void testParseTimestamp_multi() {
        int timestamp = Logg.parseTimestamp((byte) 0xAD, (byte) 0x28, (byte) 0x00);
        assertEquals(PAYLOAD_MULTI_TIMESTAMP, timestamp);
    }

    @Test
    public void testParseLogVariable() {
        LogConfig lc = createTestLogConfig();

        LogVariable pmVbat = lc.getLogVariables().get(0);
        VariableType vt = pmVbat.getVariableType();
        assertEquals(VariableType.FLOAT, vt);

        ByteBuffer rawBytesBuffer = ByteBuffer.wrap(PAYLOAD_SINGLE_WITHOUT_TIMESTAMP).order(CrtpPacket.BYTE_ORDER);
        float parsedNumber = vt.parse(rawBytesBuffer).floatValue();
        assertEquals(PAYLOAD_SINGLE_PM_VBAT_VALUE, parsedNumber, 0.0f);
    }

   @Test
    public void testParseLogData_single() {
        LogConfig lc = createTestLogConfig();
        Map<String, Number> logDataMap = new HashMap<String, Number>();
        int timestamp = Logg.parseLogData(PAYLOAD_SINGLE, lc, logDataMap);

        assertEquals(PAYLOAD_SINGLE_TIMESTAMP, timestamp);
        assertEquals(PAYLOAD_SINGLE_PM_VBAT_VALUE, logDataMap.get("pm.vbat").floatValue(), 0.0f);
    }

   @Test
   public void testParseLogData_multi() {
       LogConfig lc = createTestLogConfig();
       lc.addVariable("baro.temp", VariableType.FLOAT);
       lc.addVariable("baro.pressure", VariableType.FLOAT);

       Map<String, Number> logDataMap = new HashMap<String, Number>();
       int timestamp = Logg.parseLogData(PAYLOAD_MULTI, lc, logDataMap);
       
       assertEquals(PAYLOAD_MULTI_TIMESTAMP, timestamp);
       assertEquals(PAYLOAD_MULTI_PM_VBAT_VALUE, logDataMap.get("pm.vbat").floatValue(), 0.0f);
       assertEquals(PAYLOAD_MULTI_BARO_TEMP_VALUE, logDataMap.get("baro.temp").floatValue(), 0.0f);
       assertEquals(PAYLOAD_MULTI_BARO_PRESSURE_VALUE, logDataMap.get("baro.pressure").floatValue(), 0.0f);
   }

    @Test
    public void testUnpackLogData_single() {
        LogConfig lc = createTestLogConfig();
        Map<String, Number> logDataMap = lc.unpackLogData(PAYLOAD_SINGLE_WITHOUT_TIMESTAMP);
        assertEquals(PAYLOAD_SINGLE_PM_VBAT_VALUE, logDataMap.get("pm.vbat").floatValue(), 0.0f);
    }

    @Test
    public void testUnpackLogData_multi() {
        LogConfig lc = createTestLogConfig();
        lc.addVariable("baro.temp", VariableType.FLOAT);
        lc.addVariable("baro.pressure", VariableType.FLOAT);
        
        Map<String, Number> logDataMap = lc.unpackLogData(PAYLOAD_MULTI_WITHOUT_TIMESTAMP);
        assertEquals(PAYLOAD_MULTI_PM_VBAT_VALUE, logDataMap.get("pm.vbat").floatValue(), 0.0f);
        assertEquals(PAYLOAD_MULTI_BARO_TEMP_VALUE, logDataMap.get("baro.temp").floatValue(), 0.0f);
        assertEquals(PAYLOAD_MULTI_BARO_PRESSURE_VALUE, logDataMap.get("baro.pressure").floatValue(), 0.0f);
    }

    /* Utilities */
    private LogConfig createTestLogConfig() {
        LogConfig lc = new LogConfig("battery", 500);
        lc.setId(1);
        lc.addVariable("pm.vbat", VariableType.FLOAT);
        return lc;
    }

}
