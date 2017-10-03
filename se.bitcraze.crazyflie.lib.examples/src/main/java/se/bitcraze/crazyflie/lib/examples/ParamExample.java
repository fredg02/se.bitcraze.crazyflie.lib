package se.bitcraze.crazyflie.lib.examples;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.param.ParamListener;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 * Simple example that connects to the first Crazyflie found, triggers
 * reading of all the parameters and displays their values. It then modifies
 * one parameter and reads back it's value. Finally it disconnects.
 */
public class ParamExample extends ConnectionAdapter{

    private boolean mConnected = true;
    private Crazyflie mCrazyflie;
    private List<String> mParamCheckList = new ArrayList<String>();
    private List<String> mParamGroups = new ArrayList<String>();

    /**
     * Initialize and run the example with the specified connection data
     *
     * @param connectionData
     */
    public ParamExample(ConnectionData connectionData) {
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

        // Print the param TOC
        Toc paramToc = this.mCrazyflie.getParam().getToc();
        List<String> list = new ArrayList<String>(paramToc.getTocElementMap().keySet());
        Collections.sort(list);
        
        for (String completeName : list) {
            System.out.println(completeName);
            mParamCheckList.add(completeName);
            
            String[] split = completeName.split("\\.");
            String group = split[0];
            
            mParamGroups.add(group);

            // For every group, register the callback
            this.mCrazyflie.getParam().addParamListener(new ParamListener(group, null) {
                @Override
                public void updated(String name, Number value) {
                    System.out.println(name + ": " + value);

                    // Remove each parameter from the list and close the link when all are fetched
                    mParamCheckList.remove(name);
                    if (mParamCheckList.size() == 0) {
                        System.out.println("Have fetched all parameter values.");
                        // First remove all the group callbacks

                        for (String group : mParamGroups) {
                            mCrazyflie.getParam().removeParamListeners(group, null);
                        }

                        setRandomValue();
                    }
                }
            });
        }

        // You can also register a callback for a specific group.name combo
        this.mCrazyflie.getParam().addParamListener(new ParamListener("cpu", "flash") {
            @Override
            public void updated(String name, Number value) {
                System.out.println("The connected Crazyflie has " + value + "kb of flash.");
            }
        });

        System.out.println("\nReading back all parameter values");
        // Request update for all the parameters using the full name group.name
        for (String p : mParamCheckList) {
            this.mCrazyflie.getParam().requestParamUpdate(p);
        }
    }

    private void setRandomValue () {
        // Create a new random value [0.00,1.00] for pid_attitude.pitch_kd and set it
        double pkd = Math.random();
        Double truncatedDouble = new BigDecimal(pkd).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        System.out.println("\nWrite: pid_attitude.pitch_kd=" + truncatedDouble);

        mCrazyflie.getParam().addParamListener(new ParamListener("pid_attitude", "pitch_kd") {
            @Override
            public void updated(String name, Number value) {
                System.out.println("Readback: " + name + "=" + value);

                // End the example by closing the link (will cause the app to quit)
                if ((Float) value == 0.00f) {
                    mCrazyflie.disconnect();
                }
            }
        });

        // When setting a value the parameter is automatically read back and the registered callbacks will get the updated value
        mCrazyflie.getParam().setValue("pid_attitude.pitch_kd", truncatedDouble);

        // reset
        mCrazyflie.getParam().setValue("pid_attitude.pitch_kd", 0.00f);
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
        foundCrazyflies.add(new ConnectionData(10, 0));

        if (foundCrazyflies.size() > 0) {
            ParamExample paramExample = new ParamExample(foundCrazyflies.get(0));

            /**
             * The Crazyflie lib doesn't contain anything to keep the application alive,
             * so this is where your application should do something. In our case we
             * are just waiting until we are disconnected.
             */
            while (paramExample.isConnected()) {
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
