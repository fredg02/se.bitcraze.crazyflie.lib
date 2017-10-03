package se.bitcraze.crazyflie.lib.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.log.LogConfig;
import se.bitcraze.crazyflie.lib.log.LogListener;
import se.bitcraze.crazyflie.lib.log.Logg;
import se.bitcraze.crazyflie.lib.toc.VariableType;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;


/**
 * Simple example that connects to the first Crazyflie found, logs the Stabilizer
 * and prints it to the console. After 10s the application disconnects and exits.
 *
 */
public class LoggingExample extends ConnectionAdapter{

    //# Only output errors from the logging framework
    //logging.basicConfig(level=logging.ERROR)

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
        mCrazyflie.connect(connectionData);
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
    public void setupFinished(String connectionInfo) {
        System.out.println("Setup finished for " + connectionInfo);

        // The definition of the logconfig can be made before the setup is finished
        final LogConfig lcBattery = new LogConfig("Battery", 1000);
        lcBattery.addVariable("pm.vbat", VariableType.FLOAT);

        /**
         *  Adding the configuration cannot be done until a Crazyflie is connected and
         *  the setup is finished, since we need to check that the variables we
         *  would like to log are in the TOC.
         */

        final Logg logg = this.mCrazyflie.getLogg();

        if (logg != null) {
            //self._cf.log.add_config(self._lg_stab)
            logg.addConfig(lcBattery);

            /*
            # This callback will receive the data
            self._lg_stab.data_received_cb.add_callback(self._stab_log_data)
            # This callback will be called on errors
            self._lg_stab.error_cb.add_callback(self._stab_log_error)
            */

            logg.addLogListener(new LogListener() {

                public void logConfigAdded(LogConfig logConfig) {
                    String msg = "";
                    if(logConfig.isAdded()) {
                        msg = "' added";
                    } else {
                        msg = "' deleted";
                    }
                    System.out.println("LogConfig '" + logConfig.getName() + msg);
                }

                public void logConfigError(LogConfig logConfig) {
                    System.err.println("Error when logging '" + logConfig.getName() + "': " + logConfig.getErrNo());
                }

                public void logConfigStarted(LogConfig logConfig) {
                    String msg = "";
                    if(logConfig.isStarted()) {
                        msg = "' started";
                    } else {
                        msg = "' stopped";
                    }
                    System.out.println("LogConfig '" + logConfig.getName() + msg);
                }

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

            /*
            try:
                [...]
            except KeyError as e:
                print "Could not start log configuration," \
                      "{} not found in TOC".format(str(e))
            except AttributeError:
                print "Could not add Stabilizer log config, bad configuration."
             */

            // Start a timer to disconnect after 5s
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    logg.stop(lcBattery);
                    logg.delete(lcBattery);
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
    public void connectionFailed(String connectionInfo, String msg) {
        System.out.println("Connection to " + connectionInfo + " failed: " + msg);
        setConnected(false);
    }

    /*
     * Callback when disconnected after a connection has been made (i.e. Crazyflie moves out of range)
     */
    @Override
    public void connectionLost(String connectionInfo, String msg) {
        System.out.println("Connection to " + connectionInfo + " lost: " + msg);
    }

    /*
     * Callback when the Crazyflie is disconnected (called in all cases)
     */
    @Override
    public void disconnected(String connectionInfo) {
        System.out.println("Disconnected from " + connectionInfo);
        setConnected(false);
    }

    public static void main(String[] args) {
        // Initialize the low-level drivers (don't list the debug drivers)
        // cflib.crtp.init_drivers(enable_debug_driver=False)

        // Scan for Crazyflies and use the first one found
//        System.out.println("Scanning interfaces for Crazyflies...");
//
//        RadioDriver radioDriver = new RadioDriver(new UsbLinkJava());
//        List<ConnectionData> foundCrazyflies = radioDriver.scanInterface();
//        radioDriver.disconnect();
//
//        System.out.println("Crazyflies found:");
//        for (ConnectionData connectionData : foundCrazyflies) {
//            System.out.println(connectionData);
//        }

        List<ConnectionData> foundCrazyflies = new ArrayList<ConnectionData>();
        foundCrazyflies.add(new ConnectionData(85, 0));

        if (foundCrazyflies.size() > 0) {
            LoggingExample loggingExample = new LoggingExample(foundCrazyflies.get(0));

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
        } else {
            System.out.println("No Crazyflies found, cannot run example");
        }
    }

}
