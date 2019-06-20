package se.bitcraze.crazyflie.lib;


import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import se.bitcraze.crazyflie.lib.bootloader.BootloaderTest;
import se.bitcraze.crazyflie.lib.bootloader.TargetTest;
import se.bitcraze.crazyflie.lib.bootloader.UtilitiesTest;
import se.bitcraze.crazyflie.lib.log.LogConfigTest;
import se.bitcraze.crazyflie.lib.log.LogDataStaticTest;
import se.bitcraze.crazyflie.lib.log.LogTocElementTest;
import se.bitcraze.crazyflie.lib.log.LoggTest;
import se.bitcraze.crazyflie.lib.param.ParamTest;
import se.bitcraze.crazyflie.lib.param.ParamTocElementTest;
import se.bitcraze.crazyflie.lib.toc.TocFetcherTest;
import se.bitcraze.crazyflie.lib.toc.TocTest;
import se.bitcraze.crazyflie.lib.toc.VariableTypeTest;

@RunWith(Categories.class)
@Categories.IncludeCategory(OfflineTests.class)
@Suite.SuiteClasses({
    TocFetcherTest.class,
    TocTest.class,
    VariableTypeTest.class,
    LogTocElementTest.class,
    LogConfigTest.class,
    LoggTest.class,
    LogDataStaticTest.class,
    ParamTest.class,
    ParamTocElementTest.class,
    TargetTest.class,
    UtilitiesTest.class,
    BootloaderTest.class})
public class AllTestsOffline {
  //nothing
}

