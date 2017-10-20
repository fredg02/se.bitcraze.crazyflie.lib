package se.bitcraze.crazyflie.lib;


import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import se.bitcraze.crazyflie.lib.bootloader.BootloaderTest;
import se.bitcraze.crazyflie.lib.bootloader.CloaderTest;
import se.bitcraze.crazyflie.lib.bootloader.TargetTest;
import se.bitcraze.crazyflie.lib.bootloader.UtilitiesTest;
import se.bitcraze.crazyflie.lib.log.LogDataStaticTest;
import se.bitcraze.crazyflie.lib.log.LogTocElementTest;
import se.bitcraze.crazyflie.lib.param.ParamTocElementTest;
import se.bitcraze.crazyflie.lib.toc.TocFetcherTest;
import se.bitcraze.crazyflie.lib.toc.TocTest;
import se.bitcraze.crazyflie.lib.toc.VariableTypeTest;

@RunWith(Categories.class)
@Categories.IncludeCategory(OfflineTests.class)
@Suite.SuiteClasses({
    CloaderTest.class,
    TocFetcherTest.class,
    TocTest.class,
    VariableTypeTest.class,
    LogTocElementTest.class,
    LogDataStaticTest.class,
    ParamTocElementTest.class,
    TargetTest.class,
    UtilitiesTest.class,
    BootloaderTest.class})
public class AllTestsOffline {
  //nothing
}

