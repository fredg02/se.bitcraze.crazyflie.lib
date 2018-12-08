/**
 *    ||          ____  _ __
 * +------+      / __ )(_) /_______________ _____  ___
 * | 0xBC |     / __  / / __/ ___/ ___/ __ `/_  / / _ \
 * +------+    / /_/ / / /_/ /__/ /  / /_/ / / /_/  __/
 *  ||  ||    /_____/_/\__/\___/_/   \__,_/ /___/\___/
 *
 * Copyright (C) 2015 Bitcraze AB
 *
 * Crazyflie Nano Quadcopter Client
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package se.bitcraze.crazyflie.lib.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.MockDriver;
import se.bitcraze.crazyflie.lib.OfflineTests;
import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.TestLogAdapter;
import se.bitcraze.crazyflie.lib.TestUtilities;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.VariableType;

public class LoggTest {

    //TODO: test adding multiple log configs
    //TODO: improve timeout handling


    private Logg mLogg;
    private boolean mSetupFinished = false;

    ConnectionData mConnectionData = new ConnectionData(CrazyflieTest.channel, CrazyflieTest.datarate);

    @Category(OfflineTests.class)
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNPE() {
        new Logg(null);
    }

    @Test
    public void testLogg() {
        if (!TestUtilities.isCrazyradioAvailable()) {
            System.out.println("Skipping testLogg() for now, since Crazyradio is not available.");
            return;
        }

        //TODO: refactor this into a test utility method
        final Crazyflie crazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl(), new File("src/test"));

        crazyflie.clearTocCache();

        // create log config
        final LogConfig testConfig = new LogConfig("testConfig");
        testConfig.addVariable("motor.m1");
        testConfig.addVariable("motor.m2");
        testConfig.addVariable("motor.m3");
        testConfig.addVariable("motor.m4");

        crazyflie.getDriver().addConnectionListener(new TestConnectionAdapter() {

            @Override
            public void setupFinished() {
                System.out.println("SETUP FINISHED");
                mLogg = crazyflie.getLogg();
                System.out.println("Number of TOC elements: " + mLogg.getToc().getElements().size());

                mLogg.addLogListener(new TestLogAdapter() {

                    @Override
                    public void logDataReceived(LogConfig logConfig, Map<String, Number> data, int timestamp) {
                        System.out.println("LogConfig '" + logConfig.getName()  + "', timestamp: " + timestamp + ", data: ");
                        // TODO sort?
                        for (Entry<String, Number> entry : data.entrySet()) {
                            System.out.println("Name: " + entry.getKey() + ", data: " + entry.getValue());
                        }
                    }

                });

                // Add config
                mLogg.addConfig(testConfig);

                assertFalse(testConfig.isAdded());
                assertFalse(testConfig.isStarted());

                // Start config
                mLogg.start(testConfig);

                // Start a timer to disconnect after 3s
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        assertTrue(testConfig.isAdded());
                        assertTrue(testConfig.isStarted());
                        mLogg.stop(testConfig);
                    }

                }, 3000);

                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        assertTrue(testConfig.isAdded());
                        assertFalse(testConfig.isStarted());
                        mLogg.delete(testConfig);
                    }

                }, 6000);

                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        assertFalse(testConfig.isAdded());
                        assertFalse(testConfig.isStarted());
                mSetupFinished = true;
            }

                }, 10000);
            }

        });

        crazyflie.setConnectionData(mConnectionData);
        crazyflie.connect();

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

        crazyflie.disconnect();
    }

    /**
     * Trying to create a log config when TOC is null caused NPEs in Logg.create()
     */
    @Category(OfflineTests.class)
    @Test(expected = IllegalStateException.class)
    public void testCreateConfigWithNonExistentToc() {
        LogConfig testConfig = new LogConfig("testConfig");
        testConfig.addVariable("foo.bar");

        Crazyflie cf = new Crazyflie(new MockDriver());

        Logg logg = new Logg(cf);

        // TOC is null, Exception expected
        logg.create(testConfig);
    }

    /**
     * Trying to create a log config with an existing TOC, an existing TocElement, but without a VariableType caused NPEs in Logg.create()
     */
    @Category(OfflineTests.class)
    @Test(expected = IllegalStateException.class)
    public void testCreateConfigWithNonExistentVariableType() {
        LogConfig testConfig = new LogConfig("testConfig");
        testConfig.addVariable("foo.bar");

        Crazyflie cf = new Crazyflie(new MockDriver());

        Logg logg = new Logg(cf);

        // TOC exists, TocElement exists, but no VariableType, Exception expected
        Toc loggToc = new Toc();
        TocElement fooBar = new TocElement();
        fooBar.setGroup("foo");
        fooBar.setName("bar");
        loggToc.addElement(fooBar);

        logg.setToc(loggToc);

        logg.create(testConfig);
    }

    @Category(OfflineTests.class)
    @Test(expected = IllegalStateException.class)
    public void testLogCreateEmpyLogVariables() {
        Logg logg = new Logg(new Crazyflie(new MockDriver()));
        LogConfig logConfig = new LogConfig("Empty");
        logg.create(logConfig);
    }

    @Category(OfflineTests.class)
    @Test(expected = IllegalStateException.class)
    public void testLogCreateBufferOverflowException() {
        int noOfVariables = 20;
        Logg logg = new Logg(new Crazyflie(new MockDriver()));
        // fill TOC
        Toc toc = new Toc();
        for (int i = 1; i <= noOfVariables; i++) {
            TocElement tocElement = new TocElement();
            tocElement.setGroup("Test");
            tocElement.setName("V"+i);
            tocElement.setIdent(i);
            tocElement.setCtype(VariableType.UINT8_T);
            toc.addElement(tocElement);
        }
        logg.setToc(toc);

        LogConfig logConfig = new LogConfig("BigBuffer");
        for (int i = 1; i <= noOfVariables; i++) {
            logConfig.addVariable("Test.V"+i, VariableType.UINT8_T);
            logg.create(logConfig);
            System.out.println(i + " log variables");
        }
    }

    @Category(OfflineTests.class)
    @Test(expected = IllegalArgumentException.class)
    public void testAddConfigNullException() {
         Logg logg = new Logg(new Crazyflie(new MockDriver()));
         logg.addConfig(null);
    }

    @Category(OfflineTests.class)
    @Test(expected = IllegalArgumentException.class)
    public void testCreateConfigNullException() {
        Logg logg = new Logg(new Crazyflie(new MockDriver()));
        logg.create(null);
    }

    @Category(OfflineTests.class)
    @Test(expected = IllegalArgumentException.class)
    public void testStartConfigNullException() {
        Logg logg = new Logg(new Crazyflie(new MockDriver()));
        logg.start(null);
    }

    @Category(OfflineTests.class)
    @Test(expected = IllegalArgumentException.class)
    public void testStopConfigNullException() {
        Logg logg = new Logg(new Crazyflie(new MockDriver()));
        logg.stop(null);
    }

    @Category(OfflineTests.class)
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteConfigNullException() {
        Logg logg = new Logg(new Crazyflie(new MockDriver()));
        logg.delete(null);
    }

    @Test
    public void testTotalSizeOfVariables() {
        assertEquals(13, checkMaxNoOfLogVariablesInLogConfig(VariableType.UINT8_T));
        assertEquals(13, checkMaxNoOfLogVariablesInLogConfig(VariableType.UINT16_T));
        assertEquals(6, checkMaxNoOfLogVariablesInLogConfig(VariableType.UINT32_T));
        assertEquals(13, checkMaxNoOfLogVariablesInLogConfig(VariableType.INT8_T));
        assertEquals(13, checkMaxNoOfLogVariablesInLogConfig(VariableType.INT16_T));
        assertEquals(6, checkMaxNoOfLogVariablesInLogConfig(VariableType.INT32_T));
        assertEquals(6, checkMaxNoOfLogVariablesInLogConfig(VariableType.FLOAT));
        assertEquals(3, checkMaxNoOfLogVariablesInLogConfig(VariableType.DOUBLE));
    }

    private int checkMaxNoOfLogVariablesInLogConfig(VariableType type) {
        int noOfVariables = 50;
        Logg logg = new Logg(new Crazyflie(new MockDriver()));
        // fill TOC
        Toc toc = new Toc();
        for (int i = 1; i <= noOfVariables; i++) {
            TocElement tocElement = new TocElement();
            tocElement.setGroup("Test");
            tocElement.setName("V"+i);
            tocElement.setIdent(i);
            tocElement.setCtype(type);
            toc.addElement(tocElement);
        }
        logg.setToc(toc);

        LogConfig logConfig = new LogConfig("BigBuffer");
        int i = 0;
        try {
            for (i = 1; i <= noOfVariables; i++) {
                logConfig.addVariable("Test.V"+i, type);
                logg.create(logConfig);
            }
        } catch (IllegalStateException ise) {
           //ise.printStackTrace();
            i--;
        }
        return i;
    }

}
