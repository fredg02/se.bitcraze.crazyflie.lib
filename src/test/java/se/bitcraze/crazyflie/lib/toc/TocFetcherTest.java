package se.bitcraze.crazyflie.lib.toc;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.DummyDriver;
import se.bitcraze.crazyflie.lib.TestConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.CrazyflieTest;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket.Header;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.param.ParamTocElement;
import se.bitcraze.crazyflie.lib.toc.TocFetcher.TocState;

public class TocFetcherTest {

    boolean mStateConnectionRequested = false;
    boolean mStateConnected = false;
    boolean mStateSetupFinished = false;
    boolean mStateDisconnected = false;

    @Test
    public void testTocFetcherOffline() {

        Crazyflie crazyflieDummy = new Crazyflie(new DummyDriver());
        Toc toc = new Toc();
        TocFetcher tocFetcher = new TocFetcher(crazyflieDummy, CrtpPort.PARAMETERS, toc, null);

        tocFetcher.setState(TocState.GET_TOC_INFO); //TODO: try to remove

        // Manually injecting toc info packet
        Header tocInfoHeader = new Header(TocFetcher.TOC_CHANNEL, CrtpPort.PARAMETERS);
        CrtpPacket tocInfoPacket = new CrtpPacket(tocInfoHeader.getByte(), new byte[] {1,53,-83,125,-68,-24,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
        tocFetcher.newPacketReceived(tocInfoPacket);

        // Manually injecting toc element packet
        Header tocElementHeader = new Header(TocFetcher.TOC_CHANNEL, CrtpPort.PARAMETERS);
        CrtpPacket tocElementPacket = new CrtpPacket(tocElementHeader.getByte(), new byte[] {0,0,72,105,109,117,95,116,101,115,116,115,0,77,80,85,54,48,53,48,0,0,0,0,0,0,0,0,0,0,0});
        tocFetcher.newPacketReceived(tocElementPacket);

        TocElement id00 = toc.getElementById(0);
        assertEquals("imu_tests.MPU6050", id00.getCompleteName());
        assertEquals(VariableType.UINT8_T, id00.getCtype());
        assertEquals(ParamTocElement.RO_ACCESS, id00.getAccess());
    }


    @Test
    public void testTocFetcher() {
        final Crazyflie crazyflie = new Crazyflie(CrazyflieTest.getConnectionImpl());

        crazyflie.clearTocCache();

        crazyflie.addConnectionListener(new TestConnectionAdapter() {

            public void connectionRequested(String connectionInfo) {
                System.out.println("CONNECTION REQUESTED: " + connectionInfo);
                mStateConnectionRequested = true;
            }

            public void connected(String connectionInfo) {
                System.out.println("CONNECTED: " + connectionInfo);
                mStateConnected = true;
            }

            public void setupFinished(String connectionInfo) {
                System.out.println("SETUP FINISHED: " + connectionInfo);
                mStateSetupFinished = true;
            }

            public void disconnected(String connectionInfo) {
                System.out.println("DISCONNECTED: " + connectionInfo);
                mStateDisconnected = true;
            }

        });

        crazyflie.connect(CrazyflieTest.channel, CrazyflieTest.datarate);

        for (int i = 0; i < 200; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();

        assertEquals(true, mStateConnectionRequested);
        assertEquals(true, mStateConnected);
        assertEquals(true, mStateSetupFinished);
        assertEquals(true, mStateDisconnected);

        Toc toc = crazyflie.getParam().getToc();
        List<TocElement> elements = toc.getElements();
        System.out.println("Number of Param TOC elements: " + elements.size());

        assertEquals(53, elements.size());

        for (TocElement tocElement : elements) {
            System.out.println(tocElement);
        }
    }

}
