package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;

@Category(OfflineTests.class)
public class Cf1ConfigTest {

    @Test
    public void testCf1Config() {
        int channel = 1;
        int speed = 2;
        int pitchTrim = 1;
        int rollTrim = -2;

        String toString = "CF1Config: Channel: " + channel + ", Speed: " + speed + ", PitchTrim: " + (float) pitchTrim + ", RollTrim: " + (float) rollTrim;

        Cf1Config config = new Cf1Config(channel, speed, pitchTrim, rollTrim);

        byte[] prepareConfig = config.prepareConfig();
        assertEquals(toString, config.toString());

        Cf1Config config2 = new Cf1Config();
        config2.parse(prepareConfig);
        assertEquals(toString, config2.toString());
    }

}
