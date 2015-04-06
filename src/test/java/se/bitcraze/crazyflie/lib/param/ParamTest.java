package se.bitcraze.crazyflie.lib.param;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie.State;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.VariableType;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class ParamTest {

    //TODO: separate testing of Param class methods
    //TODO: separate testing of ParamTocElement class

    Param mParam;

    ConnectionData mConnectionData = new ConnectionData(10, 0);

    @Test
    public void testParam() {
        //TODO: refactor this into a test utility method
        final Crazyflie crazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));

        //TODO: test that TocCache actually works
        crazyflie.clearTocCache();

        crazyflie.addConnectionListener(new TestConnectionAdapter() {

            public void setupFinished(String connectionInfo) {
                System.out.println("SETUP FINISHED: " + connectionInfo);
                mParam = crazyflie.getParam();
                System.out.println("Number of TOC elements: " + mParam.getToc().getElements().size());
                mParam.requestUpdateOfAllParams();
            }

        });

        crazyflie.connect(mConnectionData);

        for (int i = 0; i < 800; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();

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

        for(String s : valuesMap.keySet()) {
            System.out.println(s + ": " + valuesMap.get(s));
        }

        //TODO: use values that hardly change, they might differ between CF1 and CF2

        //identify CF1 and CF2 by CPU id?

        //uint8_t
        //32 is the correct value for CF1 according to Python client
        assertEquals(32, valuesMap.get("imu_acc_lpf").get("factor"));

        //uint16_t
        //43000 is the correct value for CF1 according to Python client
        assertEquals(43000, valuesMap.get("altHold").get("baseThrust"));
        //38444 is the correct value for CF1 according to Python client
        assertEquals(38444, valuesMap.get("firmware").get("revision1"));

        //uint32_t == Long
        //48041289 is the correct value for CF1 according to Python client
        assertEquals(48041289L, valuesMap.get("firmware").get("revision0"));

        //float
        //0.180000007153 is the correct value for CF1 according to Python client
        //TODO: is 0.18 exact enough, or is there too much rounding?
        assertEquals(0.18f, valuesMap.get("altHold").get("ki"));
    }

    @Test
    public void testParamElements() {
        //TODO: refactor this into a test utility method
        Crazyflie crazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));

        crazyflie.clearTocCache();

        crazyflie.addConnectionListener(new TestConnectionAdapter() {});

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

        Toc toc = crazyflie.getParam().getToc();
        List<TocElement> elements = toc.getElements();

        for (TocElement tocElement : elements) {
            System.out.println(tocElement);
        }

        //TODO: are IDs always the same? No, they can change after firmware upgrades!
        //TODO: can this be checked easier?
        TocElement id00 = toc.getElementById(0);
        assertEquals("imu_tests.MPU6050", id00.getCompleteName());
        assertEquals(VariableType.UINT8_T, id00.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id00.getAccess());
        TocElement id01 = toc.getElementById(1);
        assertEquals("imu_tests.HMC5883L", id01.getCompleteName());
        assertEquals(VariableType.UINT8_T, id01.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id01.getAccess());
        TocElement id02 = toc.getElementById(2);
        assertEquals("imu_tests.MS5611", id02.getCompleteName());
        assertEquals(VariableType.UINT8_T, id02.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id02.getAccess());
        TocElement id03 = toc.getElementById(3);
        assertEquals("imu_sensors.HMC5883L", id03.getCompleteName());
        assertEquals(VariableType.UINT8_T, id03.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id03.getAccess());
        TocElement id04 = toc.getElementById(4);
        assertEquals("imu_sensors.MS5611", id04.getCompleteName());
        assertEquals(VariableType.UINT8_T, id04.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id04.getAccess());
        TocElement id05 = toc.getElementById(5);
        assertEquals("imu_acc_lpf.factor", id05.getCompleteName());
        assertEquals(VariableType.UINT8_T, id05.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id05.getAccess());
        TocElement id06 = toc.getElementById(6);
        assertEquals("cpu.flash", id06.getCompleteName());
        assertEquals(VariableType.UINT16_T, id06.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id06.getAccess());
        TocElement id07 = toc.getElementById(7);
        assertEquals("cpu.id0", id07.getCompleteName());
        assertEquals(VariableType.UINT32_T, id07.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id07.getAccess());
        TocElement id08 = toc.getElementById(8);
        assertEquals("cpu.id1", id08.getCompleteName());
        assertEquals(VariableType.UINT32_T, id08.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id08.getAccess());
        TocElement id09 = toc.getElementById(9);
        assertEquals("cpu.id2", id09.getCompleteName());
        assertEquals(VariableType.UINT32_T, id09.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id09.getAccess());
        TocElement id10 = toc.getElementById(10);
        assertEquals("flightmode.althold", id10.getCompleteName());
        assertEquals(VariableType.UINT8_T, id10.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id10.getAccess());
        TocElement id11 = toc.getElementById(11);
        assertEquals("pid_rate.roll_kp", id11.getCompleteName());
        assertEquals(VariableType.FLOAT, id11.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id11.getAccess());
        TocElement id12 = toc.getElementById(12);
        assertEquals("pid_rate.roll_ki", id12.getCompleteName());
        assertEquals(VariableType.FLOAT, id12.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id12.getAccess());
        TocElement id13 = toc.getElementById(13);
        assertEquals("pid_rate.roll_kd", id13.getCompleteName());
        assertEquals(VariableType.FLOAT, id13.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id13.getAccess());
        TocElement id14 = toc.getElementById(14);
        assertEquals("pid_rate.pitch_kp", id14.getCompleteName());
        assertEquals(VariableType.FLOAT, id14.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id14.getAccess());
        TocElement id15 = toc.getElementById(15);
        assertEquals("pid_rate.pitch_ki", id15.getCompleteName());
        assertEquals(VariableType.FLOAT, id15.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id15.getAccess());
        TocElement id16 = toc.getElementById(16);
        assertEquals("pid_rate.pitch_kd", id16.getCompleteName());
        assertEquals(VariableType.FLOAT, id16.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id16.getAccess());
        TocElement id17 = toc.getElementById(17);
        assertEquals("pid_rate.yaw_kp", id17.getCompleteName());
        assertEquals(VariableType.FLOAT, id17.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id17.getAccess());
        TocElement id18 = toc.getElementById(18);
        assertEquals("pid_rate.yaw_ki", id18.getCompleteName());
        assertEquals(VariableType.FLOAT, id18.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id18.getAccess());
        TocElement id19 = toc.getElementById(19);
        assertEquals("pid_rate.yaw_kd", id19.getCompleteName());
        assertEquals(VariableType.FLOAT, id19.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id19.getAccess());
        TocElement id20 = toc.getElementById(20);
        assertEquals("pid_attitude.roll_kp", id20.getCompleteName());
        assertEquals(VariableType.FLOAT, id20.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id20.getAccess());
        TocElement id21 = toc.getElementById(21);
        assertEquals("pid_attitude.roll_ki", id21.getCompleteName());
        assertEquals(VariableType.FLOAT, id21.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id21.getAccess());
        TocElement id22 = toc.getElementById(22);
        assertEquals("pid_attitude.roll_kd", id22.getCompleteName());
        assertEquals(VariableType.FLOAT, id22.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id22.getAccess());
        TocElement id23 = toc.getElementById(23);
        assertEquals("pid_attitude.pitch_kp", id23.getCompleteName());
        assertEquals(VariableType.FLOAT, id23.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id23.getAccess());
        TocElement id24 = toc.getElementById(24);
        assertEquals("pid_attitude.pitch_ki", id24.getCompleteName());
        assertEquals(VariableType.FLOAT, id24.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id24.getAccess());
        TocElement id25 = toc.getElementById(25);
        assertEquals("pid_attitude.pitch_kd", id25.getCompleteName());
        assertEquals(VariableType.FLOAT, id25.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id25.getAccess());
        TocElement id26 = toc.getElementById(26);
        assertEquals("pid_attitude.yaw_kp", id26.getCompleteName());
        assertEquals(VariableType.FLOAT, id26.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id26.getAccess());
        TocElement id27 = toc.getElementById(27);
        assertEquals("pid_attitude.yaw_ki", id27.getCompleteName());
        assertEquals(VariableType.FLOAT, id27.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id27.getAccess());
        TocElement id28 = toc.getElementById(28);
        assertEquals("pid_attitude.yaw_kd", id28.getCompleteName());
        assertEquals(VariableType.FLOAT, id28.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id28.getAccess());
        TocElement id29 = toc.getElementById(29);
        assertEquals("sensorfusion6.kp", id29.getCompleteName());
        assertEquals(VariableType.FLOAT, id29.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id29.getAccess());
        TocElement id30 = toc.getElementById(30);
        assertEquals("sensorfusion6.ki", id30.getCompleteName());
        assertEquals(VariableType.FLOAT, id30.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id30.getAccess());

        //TODO: no sensorfusion6.kd?

        TocElement id31 = toc.getElementById(31);
        assertEquals("altHold.aslAlpha", id31.getCompleteName());
        assertEquals(VariableType.FLOAT, id31.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id31.getAccess());
        TocElement id32 = toc.getElementById(32);
        assertEquals("altHold.aslAlphaLong", id32.getCompleteName());
        assertEquals(VariableType.FLOAT, id32.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id32.getAccess());
        TocElement id33 = toc.getElementById(33);
        assertEquals("altHold.errDeadband", id33.getCompleteName());
        assertEquals(VariableType.FLOAT, id33.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id33.getAccess());
        TocElement id34 = toc.getElementById(34);
        assertEquals("altHold.altHoldChangeSens", id34.getCompleteName());
        assertEquals(VariableType.FLOAT, id34.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id34.getAccess());
        TocElement id35 = toc.getElementById(35);
        assertEquals("altHold.altHoldErrMax", id35.getCompleteName());
        assertEquals(VariableType.FLOAT, id35.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id35.getAccess());
        TocElement id36 = toc.getElementById(36);
        assertEquals("altHold.kd", id36.getCompleteName());
        assertEquals(VariableType.FLOAT, id36.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id36.getAccess());
        TocElement id37 = toc.getElementById(37);
        assertEquals("altHold.ki", id37.getCompleteName());
        assertEquals(VariableType.FLOAT, id37.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id37.getAccess());
        TocElement id38 = toc.getElementById(38);
        assertEquals("altHold.kp", id38.getCompleteName());
        assertEquals(VariableType.FLOAT, id38.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id38.getAccess());
        TocElement id39 = toc.getElementById(39);
        assertEquals("altHold.pidAlpha", id39.getCompleteName());
        assertEquals(VariableType.FLOAT, id39.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id39.getAccess());
        TocElement id40 = toc.getElementById(40);
        assertEquals("altHold.pidAslFac", id40.getCompleteName());
        assertEquals(VariableType.FLOAT, id40.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id40.getAccess());
        TocElement id41 = toc.getElementById(41);
        assertEquals("altHold.vAccDeadband", id41.getCompleteName());
        assertEquals(VariableType.FLOAT, id41.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id41.getAccess());
        TocElement id42 = toc.getElementById(42);
        assertEquals("altHold.vBiasAlpha", id42.getCompleteName());
        assertEquals(VariableType.FLOAT, id42.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id42.getAccess());
        TocElement id43 = toc.getElementById(43);
        assertEquals("altHold.vSpeedAccFac", id43.getCompleteName());
        assertEquals(VariableType.FLOAT, id43.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id43.getAccess());
        TocElement id44 = toc.getElementById(44);
        assertEquals("altHold.vSpeedASLDeadband", id44.getCompleteName());
        assertEquals(VariableType.FLOAT, id44.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id44.getAccess());
        TocElement id45 = toc.getElementById(45);
        assertEquals("altHold.vSpeedASLFac", id45.getCompleteName());
        assertEquals(VariableType.FLOAT, id45.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id45.getAccess());
        TocElement id46 = toc.getElementById(46);
        assertEquals("altHold.vSpeedLimit", id46.getCompleteName());
        assertEquals(VariableType.FLOAT, id46.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id46.getAccess());
        TocElement id47 = toc.getElementById(47);
        assertEquals("altHold.baseThrust", id47.getCompleteName());
        assertEquals(VariableType.UINT16_T, id47.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id47.getAccess());
        TocElement id48 = toc.getElementById(48);
        assertEquals("altHold.maxThrust", id48.getCompleteName());
        assertEquals(VariableType.UINT16_T, id48.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id48.getAccess());
        TocElement id49 = toc.getElementById(49);
        assertEquals("altHold.minThrust", id49.getCompleteName());
        assertEquals(VariableType.UINT16_T, id49.getCtype());
        assertEquals(ParamTocElement.RW_ACCESS, id49.getAccess());
        TocElement id50 = toc.getElementById(50);
        assertEquals("firmware.revision0", id50.getCompleteName());
        assertEquals(VariableType.UINT32_T, id50.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id50.getAccess());
        TocElement id51 = toc.getElementById(51);
        assertEquals("firmware.revision1", id51.getCompleteName());
        assertEquals(VariableType.UINT16_T, id51.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id51.getAccess());
        TocElement id52 = toc.getElementById(52);
        assertEquals("firmware.modified", id52.getCompleteName());
        assertEquals(VariableType.UINT8_T, id52.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id52.getAccess());
    }

    @Test
    public void testParamSet() throws InterruptedException {
        //TODO: refactor this into a test utility method
        final Crazyflie crazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));

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
        mParam.requestParamUpdate("altHold.maxThrust");
        Thread.sleep(150);

        // Getting original param value
        Number originalValue = mParam.getValuesMap().get("altHold").get("maxThrust");
        System.out.println("altHold.maxThrust - original value: " + originalValue);
        assertEquals(60000, originalValue);

        // Setting new param value
        mParam.setValue("altHold.maxThrust", 60001);
        Thread.sleep(150);

        // Requesting param update
        mParam.requestParamUpdate("altHold.maxThrust");
        Thread.sleep(150);
        Number newValue = mParam.getValuesMap().get("altHold").get("maxThrust");
        System.out.println("altHold.maxThrust - new value: " + newValue);
        assertEquals(60001, newValue);

        // Reset param value to original value
        mParam.setValue("altHold.maxThrust", 60000);
        Thread.sleep(150);
        mParam.requestParamUpdate("altHold.maxThrust");
        Thread.sleep(150);
        Number resetValue = mParam.getValuesMap().get("altHold").get("maxThrust");
        System.out.println("altHold.maxThrust - reset value: " + resetValue);
        assertEquals(60000, resetValue);

        crazyflie.disconnect();
    }
}
