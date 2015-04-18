package se.bitcraze.crazyflie.lib.crazyradio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class RadioDriverTest {

    private RadioDriver mRadioDriver;
    private UsbLinkJava mUsbLinkJava;

    @Before
    public void setUp() throws Exception {
        mUsbLinkJava = new UsbLinkJava(Crazyradio.CRADIO_VID,Crazyradio.CRADIO_PID);
        mRadioDriver = new RadioDriver(mUsbLinkJava);
    }

    @After
    public void tearDown() throws Exception {
        if (mRadioDriver != null) {
            mRadioDriver.close();
            mRadioDriver = null;
        }
    }

    @Test
    public void testSendPacket() {
        List<ConnectionData> connectionDataList = mRadioDriver.scanInterface();
        if (connectionDataList.isEmpty()) {
            fail("No active connections found. Please make sure at least one Crazyflie is turned on");
        } else {
            mRadioDriver.connect(connectionDataList.get(0).getChannel(), connectionDataList.get(0).getDataRate());
            for (int i = 0; i <= 50; i++) {
                mRadioDriver.sendPacket(CrtpPacket.NULL_PACKET);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testReceivePacket() {
        List<CrtpPacket> receivedPackets = new ArrayList<CrtpPacket>();
        List<ConnectionData> connectionDataList = mRadioDriver.scanInterface();
        if (connectionDataList.isEmpty()) {
            fail("No active connections found. Please make sure at least one Crazyflie is turned on");
        } else {
            mRadioDriver.connect(connectionDataList.get(0).getChannel(), connectionDataList.get(0).getDataRate());
            for (int i = 0; i <= 10; i++) {
                mRadioDriver.sendPacket(CrtpPacket.NULL_PACKET);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                CrtpPacket receivePacket = mRadioDriver.receivePacket(50);
                receivedPackets.add(receivePacket);
                System.out.println("Received : " + receivePacket);
            }
        }
        if (receivedPackets.isEmpty()) {
            fail("Did not receive any packets.");
        }
    }

    @Test
    public void testScanInterface() {
        List<ConnectionData> connectionDataList = mRadioDriver.scanInterface();
        if (connectionDataList.isEmpty()) {
            fail("No active connections found. Please make sure at least one Crazyflie is turned on");
        } else {
            System.out.println("Found active connections:");
            for (ConnectionData cd : connectionDataList) {
                System.out.println("Data rate: " + cd.getDataRate() + ", channel: " + cd.getChannel());
            }
        }
    }

    @Test
    public void testClose() {
        mRadioDriver.close();
        assertFalse(mUsbLinkJava.isUsbConnected());
    }

}
