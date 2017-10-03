package se.bitcraze.crazyflie.lib.log;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LogConfigTest {

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
}
