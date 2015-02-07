package se.bitcraze.crazyflie.lib.crazyflie;

import org.junit.Test;

import se.bitcraze.crazyflie.lib.crazyradio.DataListener;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class CrazyflieTest {

    @Test
    public void testCrazyflie() {
        Crazyflie crazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));

        crazyflie.connect(10, 0);

        for (int i = 0; i < 10; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 15000));
            try {
                Thread.sleep(50, 0);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();
    }

    @Test
    public void testDataListener() {
        Crazyflie crazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));

        crazyflie.addDataListener(new DataListener(CrtpPort.CONSOLE) {

            @Override
            public void dataReceived(CrtpPacket packet) {
                System.out.println("Received " + packet);
            }

        });

        crazyflie.connect(10, 0);

        for (int i = 0; i < 30; i++) {
            crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 15000));
            try {
                Thread.sleep(50, 0);
            } catch (InterruptedException e) {
                break;
            }
        }
        crazyflie.disconnect();
    }

}
