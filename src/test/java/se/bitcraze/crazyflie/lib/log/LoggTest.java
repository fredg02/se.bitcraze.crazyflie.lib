package se.bitcraze.crazyflie.lib.log;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;

public class LoggTest {

    //TODO: test adding multiple log configs


    private Logg mLogg;
    private boolean mSetupFinished = false;

    ConnectionData mConnectionData = new ConnectionData(CrazyflieTest.channel, CrazyflieTest.datarate);

    @Test
    public void testLogg() {
        //TODO: refactor this into a test utility method
        final Crazyflie crazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl());

        crazyflie.clearTocCache();

        final LogConfig testConfig = new LogConfig("testConfig");
        testConfig.addVariable("motor.m1");
        testConfig.addVariable("motor.m2");
        testConfig.addVariable("motor.m3");
        testConfig.addVariable("motor.m4");

        crazyflie.addConnectionListener(new TestConnectionAdapter() {

            public void setupFinished(String connectionInfo) {
                System.out.println("SETUP FINISHED: " + connectionInfo);
                mLogg = crazyflie.getLogg();
                System.out.println("Number of TOC elements: " + mLogg.getToc().getElements().size());

                // Add config
                mLogg.addConfig(testConfig);

                // Start config
                mLogg.start(testConfig);

//                mLogg.stop(logConfig);
//                mLogg.delete(logConfig);

                mSetupFinished = true;
            }

        });

        crazyflie.connect(mConnectionData);

        // setup finished timeout
        boolean isTimeout = false;
        long startTime = System.currentTimeMillis();
        while(!mSetupFinished && !isTimeout) {
            isTimeout = (System.currentTimeMillis() - startTime) > 30000;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("It took " + (endTime - startTime) + "ms until setup finished.");

        // timeout
        boolean isTimeout2 = false;
        long startTime2 = System.currentTimeMillis();
        while(/*!mLogg.checkIfAllUpdated() &&*/ !isTimeout2) {
            isTimeout2 = (System.currentTimeMillis() - startTime2) > 15000;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        long endTime2 = System.currentTimeMillis();
        if(isTimeout2) {
            System.out.println("Timeout2!");
        } else {
            //TODO: fix text
            System.out.println("It took " + (endTime2 - startTime2) + "ms until all parameters were updated.");
        }

        assertTrue(testConfig.isAdded());
        assertTrue(testConfig.isStarted());

        mLogg.stop(testConfig);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        assertFalse(testConfig.isStarted());

        mLogg.delete(testConfig);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        assertFalse(testConfig.isAdded());

        crazyflie.disconnect();
    }

}
