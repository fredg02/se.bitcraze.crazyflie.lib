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

package se.bitcraze.crazyflie.lib.param;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;
import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.TestUtilities;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;

@SuppressWarnings("java:S106")
public class ParamTest {

    private Crazyflie mCrazyflie;
    private Param mParam;
    private boolean mSetupFinished = false;

    private ConnectionData mConnectionData = new ConnectionData(CrazyflieTest.CHANNEL, CrazyflieTest.DATARATE);

    //TODO: when cf disconnects, testParam is still stuck in the while loop

    @Before
    public void setup() {
        mCrazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl(), new File("src/test"));
    }

    @Test
    public void testParam() {
        if (!TestUtilities.isCrazyradioAvailable()) {
            fail("Test only works when Crazyflie is connected.");
        }
        mCrazyflie.clearTocCache();

        mCrazyflie.getDriver().addConnectionListener(new TestConnectionAdapter() {

            @Override
            public void setupFinished() {
                System.out.println("SETUP FINISHED");
                mParam = mCrazyflie.getParam();
                System.out.println("Number of TOC elements: " + mParam.getToc().getElements().size());
                mParam.requestUpdateOfAllParams();
                mSetupFinished = true;
            }

        });

        mCrazyflie.setConnectionData(mConnectionData);
        mCrazyflie.connect();

        setupFinishedTimeout();
        boolean isTimeout2 = firstTimeout();
        mCrazyflie.disconnect();

        if (mParam == null) {
            fail("mParam is null");
        } else {
            Map<String, Map<String, Number>> valuesMap = mParam.getValuesMap();

            int noOfValueMapElements = countValueMapElements(valuesMap);

            //TODO: why are not all values fetched in a reasonable time?
    //        assertEquals(mParam.getToc().getTocSize(), valuesMap.keySet().size());
            System.out.println("TocSize: " + mParam.getToc().getTocSize() + ", No of valueMap elements: " + noOfValueMapElements);

            if(mParam.getToc().getTocSize() != noOfValueMapElements) {
                for(String name : mParam.getToc().getTocElementMap().keySet()) {
                    if(valuesMap.get(name) == null) {
                        System.out.println("Missing param in ValueMap: " + name);
                    }
                }
            }

            if(!isTimeout2) {
                checkValues(valuesMap);
            }
        }
    }

    private boolean firstTimeout() {
        boolean isTimeout2 = false;
        long startTime2 = System.currentTimeMillis();
        while(mParam != null && !mParam.checkIfAllUpdated() && !isTimeout2) {
            isTimeout2 = (System.currentTimeMillis() - startTime2) > 30000;
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
            System.out.println("It took " + (endTime2 - startTime2) + "ms until all parameters were updated.");
        }
        return isTimeout2;
    }

    private void setupFinishedTimeout() {
        boolean isTimeout = false;
        long startTime = System.currentTimeMillis();
        while(!mSetupFinished && !isTimeout) {
            isTimeout = (System.currentTimeMillis() - startTime) > 10000;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("It took " + (endTime - startTime) + "ms until setup finished.");
    }

    private int countValueMapElements(Map<String, Map<String, Number>> valuesMap) {
        int noOfValueMapElements = 0;
        for(Map.Entry<String, Map<String, Number>> entry : valuesMap.entrySet()) {
            noOfValueMapElements += entry.getValue().keySet().size();
        }
        return noOfValueMapElements;
    }

    private void checkValues(Map<String, Map<String, Number>> valuesMap) {
        for(Map.Entry<String, Map<String, Number>> entry : valuesMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        //TODO: use values that hardly change

        //identify CF1 and CF2 by CPU flash (CF1 has 128kb, CF2 has 1MB)
        int flash = valuesMap.get("cpu").get("flash").intValue();
        if(flash == 128) { // CF1
            //uint8_t
            assertEquals(13, valuesMap.get("imu_acc_lpf").get("factor"));

            //uint32_t == Long
            assertEquals(2266244689L, valuesMap.get("cpu").get("id2"));
        } else if(flash == 1024) { // CF2
            //uint8_t
            assertEquals(1, valuesMap.get("imu_tests").get("MPU6500"));

            //uint32_t == Long
            assertEquals(926103090L, valuesMap.get("cpu").get("id2"));
        } else {
            fail("cpu.flash value is not 128 or 1024.");
        }

        //uint16_t
        assertEquals(4000, valuesMap.get("sound").get("freq"));

        //float
        assertEquals(4.2f, valuesMap.get("ring").get("fullCharge"));
    }

    private void waitForNotNull(String group, String name) {
        int maxIterations = 100;
        int pause = 5;
        for (int i = 0; i < maxIterations; i++) {
            if (mParam.getValuesMap() != null
                    && mParam.getValuesMap().get(group) != null
                    && mParam.getValuesMap().get(group).get(name) != null) {
                System.out.println("waitForNotNull done after: " + (i * pause) + "s");
                return;
            } else {
                try {
                    Thread.sleep(pause);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.out.println("waitForNotNull timeout reached: " + (maxIterations * pause) + "s");
    }

    @Test
    @Category(OfflineTests.class)
    public void testParamSet() throws InterruptedException {
        //crazyflie.clearTocCache();

        mCrazyflie.setConnectionData(mConnectionData);
        mCrazyflie.connect();

        // wait until setup is finished
        while (!mCrazyflie.isConnected()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                break;
            }
        }

        mParam = mCrazyflie.getParam();
        assertNotNull("mParam should not be null!", mParam);
        System.out.println("Number of TOC elements: " + mParam.getToc().getElements().size());
        // Requesting initial param update
        String group = "sound";
        String name = "freq";
        String param = group + "." + name;

        mParam.requestParamUpdate(param);

        // Getting original param value
        waitForNotNull(group, name);
        Number originalValue = mParam.getValuesMap().get(group).get(name);
        System.out.println(param + " - original value: " + originalValue);
        assertEquals(4000, originalValue);

        // Setting new param value
        mParam.setValue(param, 4001);
        Thread.sleep(500);

        // Requesting param update
        mParam.requestParamUpdate(param);
        waitForNotNull(group, name);
        Number newValue = mParam.getValuesMap().get(group).get(name);
        System.out.println(param + " - new value: " + newValue);
        assertEquals(4001, newValue);

        // Reset param value to original value
        mParam.setValue(param, 4000);
        Thread.sleep(500);
        mParam.requestParamUpdate(param);
        waitForNotNull(group, name);
        Number resetValue = mParam.getValuesMap().get(group).get(name);
        System.out.println(param + " - reset value: " + resetValue);
        assertEquals(4000, resetValue);

        mCrazyflie.disconnect();
    }
}
