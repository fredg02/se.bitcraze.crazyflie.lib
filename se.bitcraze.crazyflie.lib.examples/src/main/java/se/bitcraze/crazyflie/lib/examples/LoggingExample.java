package se.bitcraze.crazyflie.lib.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.log.LogConfig;
import se.bitcraze.crazyflie.lib.log.LogListener;
import se.bitcraze.crazyflie.lib.log.Logg;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.toc.VariableType;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 * Simple example that connects to the Crazyflie on the given channel and data rate.
 * It prints all elements of the Logg table of contents.
 * Then it adds log configurations:
 * * the battery voltage
 * * the barometer temperature & pressue
 * and prints the values to the console.
 * After 10s the application disconnects and exits.
 */
public class LoggingExample extends ConnectionAdapter{

    //Variable used to keep main loop occupied until disconnect
    private boolean mConnected = true;
    private Crazyflie mCrazyflie;

    /**
     * Initialize and run the example with the specified connection data
     *
     * @param connectionData
     */
    public LoggingExample(ConnectionData connectionData) {
        // Create a Crazyflie object without specifying any cache dirs
        mCrazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));
        //TODO: do not use cache

        // Connect some callbacks from the Crazyflie API
        mCrazyflie.getDriver().addConnectionListener(this);

        System.out.println("Connecting to " + connectionData);

        // Try to connect to the Crazyflie
        mCrazyflie.setConnectionData(connectionData);
        mCrazyflie.connect();
    }

    public boolean isConnected() {
        return this.mConnected;
    }

    private void setConnected(boolean connected) {
        this.mConnected = connected;
    }

    /**
     * This callback is called form the Crazyflie API when a Crazyflie
     * has been connected and the TOCs have been downloaded.
     *
     * @param connectionInfo
     */
    @Override
    public void setupFinished() {
        System.out.println("Setup finished");
        
        printLoggingTOC();
        
        addLogConfigs();
    }

    private void printLoggingTOC() {
        final Toc logToc = this.mCrazyflie.getLogg().getToc();
        List<String> keyList = new ArrayList<String>(logToc.getTocElementMap().keySet());
        Collections.sort(keyList);
        System.out.println("Number of logging elements: " + keyList.size());

        // Print all logging elements
        for(String completeName : keyList) {
            System.out.println(completeName);
        }
        
    }

    private void addLogConfigs() {
        // The definition of the logconfig can be made before the setup is finished
        final LogConfig lcBattery = new LogConfig("Battery", 1000);
        lcBattery.addVariable("pm.vbat", VariableType.FLOAT);

        final LogConfig lcBaro = new LogConfig("Baro", 1000);
        lcBaro.addVariable("baro.temp", VariableType.FLOAT);
        lcBaro.addVariable("baro.pressure", VariableType.FLOAT);

        /**
         *  Adding the configuration cannot be done until a Crazyflie is connected and
         *  the setup is finished, since we need to check that the variables we
         *  would like to log are in the TOC.
         */

        final Logg logg = this.mCrazyflie.getLogg();

        if (logg != null) {
            //self._cf.log.add_config(self._lg_stab)
            logg.addConfig(lcBattery);
            logg.addConfig(lcBaro);

            System.out.println("\nNumber of logConfigs: " + logg.getLogConfigs().size());

            logg.addLogListener(new LogListener() {

                @Override
                public void logConfigAdded(LogConfig logConfig) {
                    String msg = "";
                    if(logConfig.isAdded()) {
                        msg = "' added";
                    } else {
                        msg = "' deleted";
                    }
                    System.out.println("LogConfig '" + logConfig.getName() + " (ID: " + logConfig.getId() + ")" + msg);
                }

                @Override
                public void logConfigError(LogConfig logConfig) {
                    System.err.println("Error when logging '" + logConfig.getName() + "': " + logConfig.getErrMsg());
                }

                @Override
                public void logConfigStarted(LogConfig logConfig) {
                    String msg = "";
                    if(logConfig.isStarted()) {
                        msg = "' started";
                    } else {
                        msg = "' stopped";
                    }
                    System.out.println("LogConfig '" + logConfig.getName() + " (ID: " + logConfig.getId() + ")" + msg);
                }

                @Override
                public void logDataReceived(LogConfig logConfig, Map<String, Number> data, int timestamp) {
                    System.out.println("LogConfig '" + logConfig.getName()  + "', timestamp: " + timestamp + ", data : ");
                    // TODO sort?
                    for (Entry<String, Number> entry : data.entrySet()) {
                        System.out.println("\t Name: " + entry.getKey() + ", data: " + entry.getValue());
                    }
                }

            });

            // Start the logging
            logg.start(lcBattery);
            logg.start(lcBaro);

            // Start a timer to disconnect after 5s
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    logg.stop(lcBattery);
                    logg.delete(lcBattery);
                    logg.stop(lcBaro);
                    logg.delete(lcBaro);
                }

            }, 5000);

            // Start a timer to disconnect after 10s
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    mCrazyflie.disconnect();
                }

            }, 10000);
        } else {
            System.err.println("Logg was null!!");
        }
    }

    /*
     * Callback when connection initial connection fails (i.e no Crazyflie at the specified address)
     */
    @Override
    public void connectionFailed(String msg) {
        System.out.println("Connection failed: " + msg);
        setConnected(false);
    }

    /*
     * Callback when disconnected after a connection has been made (i.e. Crazyflie moves out of range)
     */
    @Override
    public void connectionLost(String msg) {
        System.out.println("Connection lost: " + msg);
        setConnected(false);
    }

    /*
     * Callback when the Crazyflie is disconnected (called in all cases)
     */
    @Override
    public void disconnected() {
        System.out.println("Disconnected");
        setConnected(false);
    }

    public static void main(String[] args) {
        int channel = 80;
        int datarate = Crazyradio.DR_250KPS;;

        LoggingExample loggingExample = new LoggingExample(new ConnectionData(channel, datarate));

        /**
         * The Crazyflie lib doesn't contain anything to keep the application alive,
         * so this is where your application should do something. In our case we
         * are just waiting until we are disconnected.
         */
        while (loggingExample.isConnected()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
