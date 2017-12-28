package se.bitcraze.crazyflie.lib;

import se.bitcraze.crazyflie.lib.bootloader.Cloader;
import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;

/**
 * Mocks replies from CF1
 *
 */
public class MockRadioLegacy extends MockRadio  {

    @Override
    protected byte[] bootloader(byte[] payload) {
        byte[] data = new byte[] {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,}; // default data

        if (payload[1] == (byte) Cloader.GET_INFO) {
            if (payload[0] == (byte) TargetTypes.STM32) {
                mLogger.debug("Bootloader - Command: GET_INFO - STM32 - CF1");
                /*
                  OUT:   -1,-1,16,
                  IN:    17,-1,-1,16,0,4,10,0,-128,0,10,0,80,-1,118,6,73,-123,86,84,81,38,20,-121,1,0,0,0,0,0,0,0,0,
                 */
                data = new byte[] {-1,-1,16,0,4,10,0,-128,0,10,0,80,-1,118,6,73,-123,86,84,81,38,20,-121,1,0,0,0,0,0,0,0,0};
            } else {
                return super.bootloader(payload);
            }
        } else if (payload[1] == Cloader.READ_FLASH) {
            mLogger.debug("Bootloader - Command: READ_FLASH - CF1");
            // Read CF1 config
            /*
                OUT:   -1,28,127,0,0,0,
                IN:    1,-1,-1,28,127,0,0,0,48,120,66,67,0,10,0,0,0,0,0,0,0,0,0,-55,0,0,0,0,0,0,0,0,0,
             */
            data = new byte[] {-1,-1,28,127,0,0,0,48,120,66,67,0,10,0,0,0,0,0,0,0,0,0,-55,0,0,0,0,0,0,0,0,0};
            //TODO: improve!?
        } else {
            return super.bootloader(payload);
        }
        return data;
    }
}
