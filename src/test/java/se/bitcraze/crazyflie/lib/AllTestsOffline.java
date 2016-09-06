package se.bitcraze.crazyflie.lib;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import se.bitcraze.crazyflie.lib.bootloader.TargetTest;
import se.bitcraze.crazyflie.lib.bootloader.UtilitiesTest;
import se.bitcraze.crazyflie.lib.log.LogDataStaticTest;
import se.bitcraze.crazyflie.lib.log.LogTocElementTest;
import se.bitcraze.crazyflie.lib.param.ParamTocElementTest;
import se.bitcraze.crazyflie.lib.toc.TocTest;
import se.bitcraze.crazyflie.lib.toc.VariableTypeTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    /*TocFetcherTest.class,*/ // only partially offline
    TocTest.class,
    VariableTypeTest.class,
    LogTocElementTest.class,
    LogDataStaticTest.class,
    ParamTocElementTest.class,
    TargetTest.class,
    UtilitiesTest.class,
    /*BootloaderTest.class*/}) // mock test not complete yet
public class AllTestsOffline {
  //nothing
}

