package se.bitcraze.crazyflie.lib.toc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionListener;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.toc.TocFetcher.TocState;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class TocFetcherTest {

    protected TocFetcher mTocFetcher;

    @Test
    public void test() {
        final Crazyflie crazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));

        crazyflie.addConnectionListener(new ConnectionListener() {

            public void connectionRequested(String connectionInfo) {
                System.out.println("CONNECTION REQUESTED: " + connectionInfo);
            }

            public void connected(String connectionInfo) {
                System.out.println("CONNECTED: " + connectionInfo);

                mTocFetcher = new TocFetcher(crazyflie, CrtpPort.PARAMETERS);
                mTocFetcher.start();
            }

            public void setupFinished(String connectionInfo) {
                System.out.println("SETUP FINISHED: " + connectionInfo);
            }

            public void connectionFailed(String connectionInfo, String msg) {
                System.out.println("CONNECTION FAILED: " + connectionInfo);
            }

            public void connectionLost(String connectionInfo, String msg) {
                System.out.println("CONNECTION LOST: " + connectionInfo);
            }

            public void disconnected(String connectionInfo) {
                System.out.println("DISCONNECTED: " + connectionInfo);
            }

            public void linkQualityUpdated(int percent) {
                //System.out.println("LINK QUALITY: " + percent);
            }

        });

        crazyflie.connect(10, 0);

        for (int i = 0; i < 200; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();

        assertEquals(TocState.TOC_FETCH_FINISHED, mTocFetcher.getState());
    }

}
