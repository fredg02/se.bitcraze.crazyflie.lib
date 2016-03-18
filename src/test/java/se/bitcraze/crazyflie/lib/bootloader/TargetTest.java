package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TargetTest {


    @Test
    public void testParseDataSTM32_cf1() {
        
        //original: 1, -1,-1,16,0,4,10,0,-128,0,10,0,80,-1,118,6,73,-123,86,84,81,38,20,-121,1,0,0,0,0,0,0,0,0
        // removed the first two bytes
        
        byte[] data = new byte[] {-1,16,0,4,10,0,-128,0,10,0,80,-1,118,6,73,-123,86,84,81,38,20,-121,1,0,0,0,0,0,0,0,0};
        Target target = new Target(0xFF);
        
        target.parseData(data);
        
        System.out.println(target.toString());
        System.out.println(target.getCpuId());
        System.out.println();
        
        assertEquals(118, target.getAvailableFlash());
        assertEquals(10, target.getBufferPages());
        assertEquals(128, target.getFlashPages());
        assertEquals(255, target.getId());
        assertEquals(1024, target.getPageSize());
        assertEquals(1, target.getProtocolVersion()); //0x01
        assertEquals(10, target.getStartPage());
    }

    @Test
    public void testParseDataSTM32_cf2() {

        //original: 1,-1,-1,16,0,4,10,0,0,4,16,0,-89,4,48,106,79,-34,98,94,-1,-27,28,16,16,0,0,0,0,0,0,0,0,
        // removed the first two bytes

        byte[] data = new byte[] {-1,16,0,4,10,0,0,4,16,0,-89,4,48,106,79,-34,98,94,-1,-27,28,16,16,0,0,0,0,0,0,0,0};
        Target target = new Target(0xFF);

        target.parseData(data);

        System.out.println(target.toString());
        System.out.println(target.getCpuId());
        System.out.println();

        assertEquals(1008, target.getAvailableFlash());
        assertEquals(10, target.getBufferPages());
        assertEquals(1024, target.getFlashPages());
        assertEquals(255, target.getId());
        assertEquals(1024, target.getPageSize());
        assertEquals(16, target.getProtocolVersion()); //0x0F
        assertEquals(16, target.getStartPage());
    }

    @Test
    public void testParseDataNRF51() {

        //original: 1,-1,-2,16,0,4,1,0,-24,0,88,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        // removed the first two bytes

        byte[] data = new byte[] {-2,16,0,4,1,0,-24,0,88,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
        Target target = new Target(0xFE);

        target.parseData(data);

        System.out.println(target.toString());
        System.out.println(target.getCpuId());
        System.out.println();

        assertEquals(144, target.getAvailableFlash());
        assertEquals(1, target.getBufferPages());
        assertEquals(232, target.getFlashPages());
        assertEquals(254, target.getId());
        assertEquals(1024, target.getPageSize());
        assertEquals(0, target.getProtocolVersion()); //0x00
        assertEquals(88, target.getStartPage());
    }
}
