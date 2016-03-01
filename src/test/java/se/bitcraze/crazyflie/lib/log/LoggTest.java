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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.TestLogAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;

public class LoggTest {

    //TODO: test adding multiple log configs
    //TODO: improve timeout handling


    private Logg mLogg;
    private boolean mSetupFinished = false;

    ConnectionData mConnectionData = new ConnectionData(CrazyflieTest.channel, CrazyflieTest.datarate);

    @Test
    public void testLogg() {
        //TODO: refactor this into a test utility method
        final Crazyflie crazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl());

        crazyflie.clearTocCache();

        // create log config
        final LogConfig testConfig = new LogConfig("testConfig");
        testConfig.addVariable("motor.m1");
        testConfig.addVariable("motor.m2");
        testConfig.addVariable("motor.m3");
        testConfig.addVariable("motor.m4");

        crazyflie.getDriver().addConnectionListener(new TestConnectionAdapter() {

            public void setupFinished(String connectionInfo) {
                System.out.println("SETUP FINISHED: " + connectionInfo);
                mLogg = crazyflie.getLogg();
                System.out.println("Number of TOC elements: " + mLogg.getToc().getElements().size());

                mLogg.addLogListener(new TestLogAdapter() {

                    public void logDataReceived(LogConfig logConfig, Map<String, Number> data) {
                        System.out.println("LogConfig '" + logConfig.getName()  + "', data : ");
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

                // Start a timer to disconnect after 5s
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

        crazyflie.disconnect();
    }

}
