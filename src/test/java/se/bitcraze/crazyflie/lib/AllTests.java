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
import se.bitcraze.crazyflie.lib.log.LogDataLiveTest;
import se.bitcraze.crazyflie.lib.log.LogDataStaticTest;
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
    UsbLinkJavaTest.class,
    RadioDriverTest.class,
    CrazyradioTest.class,
    CrazyflieTest.class,
    TocCacheTest.class,
    TocFetcherTest.class,
    TocTest.class,
    VariableTypeTest.class,
    LogTocElementTest.class,
    LoggTest.class,
    LogDataStaticTest.class,
    LogDataLiveTest.class,
    ParamTocElementTest.class,
    ParamTest.class,
    TargetTest.class,
    UtilitiesTest.class,
    BootloaderTest.class,
    CloaderTest.class})
public class AllTests {
  //nothing
}

