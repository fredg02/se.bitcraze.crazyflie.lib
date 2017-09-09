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
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie.State;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.VariableType;

public class ParamTest {

    //TODO: separate testing of Param class methods
    //TODO: separate testing of ParamTocElement class

    private Param mParam;
    private boolean mSetupFinished = false;

    ConnectionData mConnectionData = new ConnectionData(CrazyflieTest.channel, CrazyflieTest.datarate);


    //TODO: when cf disconnects, testParam is still stuck in the while loop

    @Test
    public void testParam() {
        //TODO: refactor this into a test utility method
        final Crazyflie crazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl(), new File("src/test"));

        //TODO: test that TocCache actually works
        crazyflie.clearTocCache();

        crazyflie.getDriver().addConnectionListener(new TestConnectionAdapter() {

            public void setupFinished(String connectionInfo) {
                System.out.println("SETUP FINISHED: " + connectionInfo);
                mParam = crazyflie.getParam();
                System.out.println("Number of TOC elements: " + mParam.getToc().getElements().size());
                mParam.requestUpdateOfAllParams();
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

        crazyflie.disconnect();

        if (mParam == null) {
            fail("mParam is null");
        }

        Map<String, Map<String, Number>> valuesMap = mParam.getValuesMap();

        int noOfValueMapElements = 0;
        for(String g : valuesMap.keySet()) {
            for(String n : valuesMap.get(g).keySet()) {
                noOfValueMapElements++;
            }
        }

        //TODO: why are not all values fetched in a reasonable time?
//        assertEquals(mParam.getToc().getTocSize(), valuesMap.keySet().size());
        System.out.println("TocSize: " + mParam.getToc().getTocSize() + ", No of valueMap elements: " + noOfValueMapElements);

        if(mParam.getToc().getTocSize() != noOfValueMapElements) {
            for(String name : mParam.getToc().getTocElementMap().keySet()) {
                if(valuesMap.get(name) == null) {
                    System.out.println("Missing param in ValueMap: " + name);
                    continue;
                }
            }
        }

        if(!isTimeout2) {
            for(String s : valuesMap.keySet()) {
                System.out.println(s + ": " + valuesMap.get(s));
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
    }

    @Test
    public void testParamElements() {
        //TODO: refactor this into a test utility method
        Crazyflie crazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl(), new File("src/test"));

        crazyflie.clearTocCache();

        crazyflie.getDriver().addConnectionListener(new TestConnectionAdapter() {});

        crazyflie.connect(mConnectionData);

        for (int i = 0; i < 200; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();

        if (crazyflie.getParam() == null) {
            fail("crazyflie.getParam() is null");
        }

        if (crazyflie.getParam() == null) {
            fail("crazyflie.getParam() is null");
        }

        Toc toc = crazyflie.getParam().getToc();
        List<TocElement> elements = toc.getElements();

        for (TocElement tocElement : elements) {
            System.out.println(tocElement);
        }

        //Check only a few TOC elements

        TocElement imu_tests = toc.getElementByCompleteName("imu_tests.HMC5883L");
        assertEquals(VariableType.UINT8_T, imu_tests.getCtype());
        assertEquals(TocElement.RO_ACCESS, imu_tests.getAccess());

        TocElement cpuFlash = toc.getElementByCompleteName("cpu.flash");
        assertEquals(VariableType.UINT16_T, cpuFlash.getCtype());
        assertEquals(TocElement.RO_ACCESS, cpuFlash.getAccess());

        TocElement cpuId0 = toc.getElementByCompleteName("cpu.id0");
        assertEquals(VariableType.UINT32_T, cpuId0.getCtype());
        assertEquals(TocElement.RO_ACCESS, cpuId0.getAccess());

        TocElement althold = toc.getElementByCompleteName("flightmode.althold");
        assertEquals(VariableType.UINT8_T, althold.getCtype());
        assertEquals(TocElement.RW_ACCESS, althold.getAccess());

        TocElement pitch_kd = toc.getElementByCompleteName("pid_attitude.pitch_kd");
        assertEquals(VariableType.FLOAT, pitch_kd.getCtype());
        assertEquals(TocElement.RW_ACCESS, pitch_kd.getAccess());
    }

    @Test
    public void testParamSet() throws InterruptedException {
        //TODO: refactor this into a test utility method
        Crazyflie crazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl(), new File("src/test"));

        //crazyflie.clearTocCache();

        crazyflie.connect(mConnectionData);

        // wait until setup is finished
        while (crazyflie.getState() != State.SETUP_FINISHED) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                break;
            }
        }

        mParam = crazyflie.getParam();
        System.out.println("Number of TOC elements: " + mParam.getToc().getElements().size());
        // Requesting initial param update
        String group = "sound";
        String name = "freq";
        String param = group + "." + name;

        mParam.requestParamUpdate(param);
        Thread.sleep(150);

        // Getting original param value
        Number originalValue = mParam.getValuesMap().get(group).get(name);
        System.out.println(param + " - original value: " + originalValue);
        assertEquals(4000, originalValue);

        // Setting new param value
        mParam.setValue(param, 4001);
        Thread.sleep(150);

        // Requesting param update
        mParam.requestParamUpdate(param);
        Thread.sleep(150);
        Number newValue = mParam.getValuesMap().get(group).get(name);
        System.out.println(param + " - new value: " + newValue);
        assertEquals(4001, newValue);

        // Reset param value to original value
        mParam.setValue(param, 4000);
        Thread.sleep(150);
        mParam.requestParamUpdate(param);
        Thread.sleep(150);
        Number resetValue = mParam.getValuesMap().get(group).get(name);
        System.out.println(param + " - reset value: " + resetValue);
        assertEquals(4000, resetValue);

        crazyflie.disconnect();
    }
}
