package se.bitcraze.crazyflie.lib.log;

import java.util.Map;

public abstract class LogListener {

    public abstract void logConfigAdded(LogConfig logConfig);

    public abstract void logConfigError(LogConfig logConfig);

    public abstract void logConfigStarted(LogConfig logConfig);

    public abstract void logDataReceived(LogConfig logConfig, Map<String, Number> data);

}
