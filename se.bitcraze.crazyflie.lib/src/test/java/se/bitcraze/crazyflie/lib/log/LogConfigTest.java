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

    private static final String FOOBAR = "foobar";
    private static final String BATTERY = "battery";
    private static final String RANGE_ZRANGE = "range.zrange";
    private static final String OA_RIGHT = "oa.right";
    private static final String OA_LEFT = "oa.left";
    private static final String OA_BACK = "oa.back";
    private static final String OA_FRONT = "oa.front";
    private static final String OA_UP = "oa.up";
    private static final String BARO_PRESSURE = "baro.pressure";
    private static final String BARO_TEMP = "baro.temp";
    private static final String PM_VBAT = "pm.vbat";

    // header + blockID(1) + timestamp(3) + logValues(0-28)
    // original: 01 52 01 3D 3C 01 D7 8F 72 40 (00 ...)
    private static final byte[] PAYLOAD_SINGLE = new byte[]{(byte) 0x01, (byte) 0x3D, (byte) 0x3C, (byte) 0x01, (byte) 0xD7, (byte) 0x8F, (byte) 0x72, (byte) 0x40};
    private static final int PAYLOAD_SINGLE_TIMESTAMP = 80957;
    private static final float PAYLOAD_SINGLE_PM_VBAT_VALUE = 3.7900293f;

    // original: 01 52 01 AD 28 00 53 E6 72 40 9A 99 B2 41 7C DB 7D 44 (00 ...) => 3.7953079f, 22.325f, 1015.42944f (pm.vbat, baro.temp, baro.pressure)
    private static final byte[] PAYLOAD_MULTI = new byte[]{(byte) 0x01, (byte) 0xAD, (byte) 0x28, (byte) 0x00, (byte) 0x53, (byte) 0xE6, (byte) 0x72, (byte) 0x40, (byte) 0x9A, (byte) 0x99, (byte) 0xB2, (byte) 0x41, (byte) 0x7C, (byte) 0xDB, (byte) 0x7D, (byte) 0x44};
    private static final int PAYLOAD_MULTI_TIMESTAMP = 10413;
    private static final float PAYLOAD_MULTI_PM_VBAT_VALUE = 3.7953079f;
    private static final float PAYLOAD_MULTI_BARO_TEMP_VALUE = 22.325f;
    private static final float PAYLOAD_MULTI_BARO_PRESSURE_VALUE = 1015.42944f;

    // timestamp: 13988, pm.vbat: 3.9800587f, oa.up: 110, oa.front: 215
    private static final byte[] PAYLOAD_OA1 = new byte[]{(byte) 0x01, (byte) 0xA4, (byte) 0x36, (byte) 0x00, (byte) 0x48, (byte) 0xB9, (byte) 0x7E, (byte) 0x40, (byte) 0x6E, (byte) 0x00, (byte) 0xD7, (byte) 0x00};
    private static final byte[] PAYLOAD_OA2 = new byte[]{(byte) 0x01, (byte) 0xE0, (byte) 0xDD, (byte) 0x02, (byte) 0x70, (byte) 0x58, (byte) 0x7B, (byte) 0x40, (byte) 0xFE, (byte) 0x1F, (byte) 0xF6, (byte) 0x00, (byte) 0x2B, (byte) 0x01, (byte) 0xD0, (byte) 0x01, (byte) 0x65, (byte) 0x00, (byte) 0x1B, (byte) 0x00};
    private static final byte[] PAYLOAD_BUE = new byte[]{(byte) 0x01, (byte) 0xE0, (byte) 0xDD, (byte) 0x02, (byte) 0x70, (byte) 0x58, (byte) 0x7B, (byte) 0x40, (byte) 0xFE, (byte) 0x1F, (byte) 0xF6, (byte) 0x00, (byte) 0x2B, (byte) 0x01, (byte) 0xD0, (byte) 0x01, (byte) 0x65, (byte) 0x00, (byte) 0x1B};

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
    public void testParseTimestampSingle() {
        assertEquals(PAYLOAD_SINGLE_TIMESTAMP, getTimestamp(PAYLOAD_SINGLE));
    }

    @Test
    public void testParseTimestampMulti() {
        assertEquals(PAYLOAD_MULTI_TIMESTAMP, getTimestamp(PAYLOAD_MULTI));
    }

    @Test
    public void testParseLogVariable() {
        LogConfig lc = createTestLogConfig();

        LogVariable pmVbat = lc.getLogVariables().get(0);
        VariableType vt = pmVbat.getVariableType();
        assertEquals(VariableType.FLOAT, vt);

        ByteBuffer rawBytesBuffer = ByteBuffer.wrap(getPayloadWithoutTimestamp(PAYLOAD_SINGLE)).order(CrtpPacket.BYTE_ORDER);
        float parsedNumber = vt.parse(rawBytesBuffer).floatValue();
        assertEquals(PAYLOAD_SINGLE_PM_VBAT_VALUE, parsedNumber, 0.0f);
    }

   @Test
    public void testParseLogDataSingle() {
        LogConfig lc = createTestLogConfig();
        Map<String, Number> logDataMap = new HashMap<>();
        int timestamp = Logg.parseLogData(PAYLOAD_SINGLE, lc, logDataMap);

        assertEquals(PAYLOAD_SINGLE_TIMESTAMP, timestamp);
        assertEquals(PAYLOAD_SINGLE_PM_VBAT_VALUE, logDataMap.get(PM_VBAT).floatValue(), 0.0f);
    }

   @Test
   public void testParseLogDataMulti() {
       LogConfig lc = createTestLogConfig();
       lc.addVariable(BARO_TEMP, VariableType.FLOAT);
       lc.addVariable(BARO_PRESSURE, VariableType.FLOAT);

       Map<String, Number> logDataMap = new HashMap<>();
       int timestamp = Logg.parseLogData(PAYLOAD_MULTI, lc, logDataMap);

       assertEquals(PAYLOAD_MULTI_TIMESTAMP, timestamp);
       assertEquals(PAYLOAD_MULTI_PM_VBAT_VALUE, logDataMap.get(PM_VBAT).floatValue(), 0.0f);
       assertEquals(PAYLOAD_MULTI_BARO_TEMP_VALUE, logDataMap.get(BARO_TEMP).floatValue(), 0.0f);
       assertEquals(PAYLOAD_MULTI_BARO_PRESSURE_VALUE, logDataMap.get(BARO_PRESSURE).floatValue(), 0.0f);
   }

    @Test
    public void testUnpackLogDataSingle() {
        LogConfig lc = createTestLogConfig();
        Map<String, Number> logDataMap = lc.unpackLogData(getPayloadWithoutTimestamp(PAYLOAD_SINGLE));
        assertEquals(PAYLOAD_SINGLE_PM_VBAT_VALUE, logDataMap.get(PM_VBAT).floatValue(), 0.0f);
    }

    @Test
    public void testUnpackLogDataMulti() {
        LogConfig lc = createTestLogConfig();
        lc.addVariable(BARO_TEMP, VariableType.FLOAT);
        lc.addVariable(BARO_PRESSURE, VariableType.FLOAT);

        Map<String, Number> logDataMap = lc.unpackLogData(getPayloadWithoutTimestamp(PAYLOAD_MULTI));
        assertEquals(PAYLOAD_MULTI_PM_VBAT_VALUE, logDataMap.get(PM_VBAT).floatValue(), 0.0f);
        assertEquals(PAYLOAD_MULTI_BARO_TEMP_VALUE, logDataMap.get(BARO_TEMP).floatValue(), 0.0f);
        assertEquals(PAYLOAD_MULTI_BARO_PRESSURE_VALUE, logDataMap.get(BARO_PRESSURE).floatValue(), 0.0f);
    }

    @Test
    public void testUnpackLogDataOA1() {
        LogConfig lc = createTestLogConfig();
        lc.addVariable(OA_UP, VariableType.UINT16_T);
        lc.addVariable(OA_FRONT, VariableType.UINT16_T);

        assertEquals(13988, getTimestamp(PAYLOAD_OA1));

        Map<String, Number> logDataMap = lc.unpackLogData(getPayloadWithoutTimestamp(PAYLOAD_OA1));
        assertEquals(3.9800587f, logDataMap.get(PM_VBAT).floatValue(), 0.0f);
        assertEquals(110, logDataMap.get(OA_UP));
        assertEquals(215, logDataMap.get(OA_FRONT));
    }

    @Test
    public void testUnpackLogDataOA2() {
        LogConfig lc = createTestLogConfig();
        lc.addVariable(OA_UP, VariableType.UINT16_T);
        lc.addVariable(OA_FRONT, VariableType.UINT16_T);
        lc.addVariable(OA_BACK, VariableType.UINT16_T);
        lc.addVariable(OA_LEFT, VariableType.UINT16_T);
        lc.addVariable(OA_RIGHT, VariableType.UINT16_T);
        lc.addVariable(RANGE_ZRANGE, VariableType.UINT16_T);

        assertEquals(187872, getTimestamp(PAYLOAD_OA2));

        Map<String, Number> logDataMap = lc.unpackLogData(getPayloadWithoutTimestamp(PAYLOAD_OA2));
        assertEquals(3.9272728f, logDataMap.get(PM_VBAT).floatValue(), 0.0f);
        assertEquals(8190, logDataMap.get(OA_UP));
        assertEquals(246, logDataMap.get(OA_FRONT));
        assertEquals(299, logDataMap.get(OA_BACK));
        assertEquals(464, logDataMap.get(OA_LEFT));
        assertEquals(101, logDataMap.get(OA_RIGHT));
        assertEquals(27, logDataMap.get(RANGE_ZRANGE));
    }

    @Test(expected = IllegalStateException.class)
    public void testUnpackLogDataBufferToSmall() {
        LogConfig lc = createTestLogConfig();
        lc.addVariable(OA_UP, VariableType.UINT16_T);
        lc.addVariable(OA_FRONT, VariableType.UINT16_T);
        lc.addVariable(OA_BACK, VariableType.UINT16_T);
        lc.addVariable(OA_LEFT, VariableType.UINT16_T);
        lc.addVariable(OA_RIGHT, VariableType.UINT16_T);
        lc.addVariable(RANGE_ZRANGE, VariableType.UINT16_T);

        assertEquals(187872, getTimestamp(PAYLOAD_BUE));

        Map<String, Number> logDataMap = lc.unpackLogData(getPayloadWithoutTimestamp(PAYLOAD_BUE));
        assertEquals(3.9272728f, logDataMap.get(PM_VBAT).floatValue(), 0.0f);
        assertEquals(8190, logDataMap.get(OA_UP));
        assertEquals(246, logDataMap.get(OA_FRONT));
        assertEquals(299, logDataMap.get(OA_BACK));
        assertEquals(464, logDataMap.get(OA_LEFT));
        assertEquals(101, logDataMap.get(OA_RIGHT));
        assertEquals(27, logDataMap.get(RANGE_ZRANGE));
    }

    @Category(OfflineTests.class)
    @Test
    public void testDuplicateLogVariables() {
        LogConfig lc = createTestLogConfig();
        lc.addVariable(PM_VBAT, VariableType.FLOAT);
//        for (LogVariable lv : lc.getLogVariables()) {
//            System.out.println("" + lv.getName());
//            System.out.println("" + lv.getType());
//            System.out.println("" + lv.getVariableType());
//        }
        assertEquals(1, lc.getLogVariables().size());
    }

    @Category(OfflineTests.class)
    @Test
    public void testDuplicateLogVariables2() {
        LogConfig lc = new LogConfig(BATTERY, 500);
        lc.setId(1);
        // 1st
        lc.addVariable(PM_VBAT);
        // 2nd
        lc.addVariable(PM_VBAT);
        assertEquals(1, lc.getLogVariables().size());
    }

    @Category(OfflineTests.class)
    @Test
    public void testDuplicateLogVariablesMemory() {
        LogConfig lc = new LogConfig(BATTERY, 500);
        lc.setId(1);
        // 1st
        lc.addMemory(FOOBAR, VariableType.FLOAT, 1234);
        // 2nd
        lc.addMemory(FOOBAR, VariableType.FLOAT, 1234);
        assertEquals(1, lc.getLogVariables().size());
    }

    /* Utilities */
    private LogConfig createTestLogConfig() {
        LogConfig lc = new LogConfig(BATTERY, 500);
        lc.setId(1);
        lc.addVariable(PM_VBAT, VariableType.FLOAT);
        return lc;
    }

    private int getTimestamp(byte[] payload) {
        return Logg.parseTimestamp(payload[1], payload[2], payload[3]);
    }

    private byte[] getPayloadWithoutTimestamp(byte[] payload) {
        byte[] returnArray = new byte[payload.length-4];
        System.arraycopy(payload, 4, returnArray, 0, payload.length-4);
        return returnArray;
    }

}
