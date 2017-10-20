package se.bitcraze.crazyflie.lib.examples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie.State;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.log.LogConfig;
import se.bitcraze.crazyflie.lib.param.Param;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

public class CrazyflieWrapper {

    private Crazyflie mCrazyflie;
    private boolean mSetupFinished;
    private Param mParam;

    public CrazyflieWrapper() {
        mCrazyflie = new Crazyflie(new RadioDriver(new UsbLinkJava()));
    }

    public void connect() {
        List<ConnectionData> foundCrazyflies = scan();
        if (foundCrazyflies.isEmpty()) {
            System.err.println("No crazyflies found.");
            return;
        }

        mCrazyflie.getDriver().addConnectionListener(new ConnectionAdapter() {

            public void setupFinished(String connectionInfo) {
                System.out.println("SETUP FINISHED: " + connectionInfo);
                mSetupFinished = true;
            }

        });
        mCrazyflie.connect(foundCrazyflies.get(0));
    }

    private List<ConnectionData> scan() {
        RadioDriver radioDriver = new RadioDriver(new UsbLinkJava());
        List<ConnectionData> foundCrazyflies = radioDriver.scanInterface();
        radioDriver.disconnect();
        return foundCrazyflies;
    }

    public void disconnect() {
        mCrazyflie.disconnect();
    }

    public void sendCommandPacket(float roll, float pitch, float yaw, char thrust) {
        // TODO: if(mCrazyflie.isConnected()) {
        if(mCrazyflie.getState().ordinal() >= State.CONNECTED.ordinal()) {
            mCrazyflie.sendPacket(new CommanderPacket(roll, pitch, yaw, thrust));
        } else {
            System.err.println("No crazyflie connected.");
        }
    }

    public void setParameterValue(String parameterName, Number value) {
        if(mSetupFinished) {
            mParam = mCrazyflie.getParam();
            mParam.setValue(parameterName, value);
        } else {
            System.out.println("Setup not finished yet.");
        }
    }

    public Number getParameterValue(String parameterName) {
        if(mSetupFinished) {
            mParam = mCrazyflie.getParam();
            mParam.requestParamUpdate(parameterName);
            // TODO: return mParam.getValue(parameterName);
            String[] paraName = parameterName.split("\\.");
            return mParam.getValuesMap().get(paraName[0]).get(paraName[1]);
        } else {
            System.out.println("Setup not finished yet.");
            return null;
        }
    }

    public Map<String, Map<String, Number>> getParameterList() {
        if(mSetupFinished) {
            mParam = mCrazyflie.getParam();
            mParam.requestUpdateOfAllParams();
            return mParam.getValuesMap();
        } else {
            System.out.println("Setup not finished yet.");
            return new HashMap<String, Map<String, Number>>();
        }
    }

    public void addLogConfig(LogConfig logConfig) {

    }

    public void startLogConfig(LogConfig logConfig) {

    }

    public void stopLogConfig(LogConfig logConfig) {

    }

    public void deleteLogConfig(LogConfig logConfig) {

    }

    //TODO: getLogConfig data
    //TODO: get Console data

    //TODO: connectionStateChangedListener?

}
