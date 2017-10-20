package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;

@Category(OfflineTests.class)
public class TargetTest {


    @Test
    public void testParseDataSTM32_cf1() {
        //original: 1, -1,-1,16,0,4,10,0,-128,0,10,0,80,-1,118,6,73,-123,86,84,81,38,20,-121,1,0,0,0,0,0,0,0,0
        // removed the first two bytes
        
        byte[] data = new byte[] {-1,16,0,4,10,0,-128,0,10,0,80,-1,118,6,73,-123,86,84,81,38,20,-121,1,0,0,0,0,0,0,0,0};
        Target target = new Target(0xFF);
        checkParseData(target, data, 118, 10, 128, 255, 1024, 1 /*0x01*/, 10);
    }

    @Test
    public void testParseDataSTM32_cf2() {
        //original: 1,-1,-1,16,0,4,10,0,0,4,16,0,-89,4,48,106,79,-34,98,94,-1,-27,28,16,16,0,0,0,0,0,0,0,0,
        // removed the first two bytes

        byte[] data = new byte[] {-1,16,0,4,10,0,0,4,16,0,-89,4,48,106,79,-34,98,94,-1,-27,28,16,16,0,0,0,0,0,0,0,0};
        Target target = new Target(0xFF);
        checkParseData(target, data, 1008, 10, 1024, 255, 1024, 16 /*0x0F*/, 16);
    }

    @Test
    public void testParseDataNRF51() {
        //original: 1,-1,-2,16,0,4,1,0,-24,0,88,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        // removed the first two bytes

        byte[] data = new byte[] {-2,16,0,4,1,0,-24,0,88,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
        Target target = new Target(0xFE);
        checkParseData(target, data, 144, 1, 232, 254, 1024, 0 /*0x00*/, 88);
    }

    private static void checkParseData(Target target, byte[] data, int flash, int buffer, int flashPages, int id, int pageSize, int protocolVersion, int startPage) {
        target.parseData(data);

        System.out.println(target.toString());
        System.out.println("CPU ID: " + target.getCpuId());
        System.out.println();

        assertEquals(flash, target.getAvailableFlash());
        assertEquals(buffer, target.getBufferPages());
        assertEquals(flashPages, target.getFlashPages());
        assertEquals(id, target.getId());
        assertEquals(pageSize, target.getPageSize());
        assertEquals(protocolVersion, target.getProtocolVersion());
        assertEquals(startPage, target.getStartPage());
    }
}
