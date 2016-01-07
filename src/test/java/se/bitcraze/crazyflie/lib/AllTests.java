package se.bitcraze.crazyflie.lib;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import se.bitcraze.crazyflie.lib.bootloader.BootloaderTest;
import se.bitcraze.crazyflie.lib.bootloader.CloaderTest;
import se.bitcraze.crazyflie.lib.bootloader.TargetTest;
import se.bitcraze.crazyflie.lib.bootloader.UtilitiesTest;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crazyradio.CrazyradioTest;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriverTest;
import se.bitcraze.crazyflie.lib.log.LogTocElementTest;
import se.bitcraze.crazyflie.lib.log.LoggTest;
import se.bitcraze.crazyflie.lib.param.ParamTest;
import se.bitcraze.crazyflie.lib.param.ParamTocElementTest;
import se.bitcraze.crazyflie.lib.toc.TocCacheTest;
import se.bitcraze.crazyflie.lib.toc.TocFetcherTest;
import se.bitcraze.crazyflie.lib.toc.TocTest;
import se.bitcraze.crazyflie.lib.toc.VariableTypeTest;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJavaTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BootloaderTest.class,
    CloaderTest.class,
    TargetTest.class,
    UtilitiesTest.class,
    CrazyflieTest.class,
    CrazyradioTest.class,
    RadioDriverTest.class,
    LoggTest.class,
    LogTocElementTest.class,
    ParamTest.class,
    ParamTocElementTest.class,
    TocCacheTest.class,
    TocFetcherTest.class,
    TocTest.class,
    VariableTypeTest.class,
    UsbLinkJavaTest.class})
public class AllTests {
  //nothing
}

