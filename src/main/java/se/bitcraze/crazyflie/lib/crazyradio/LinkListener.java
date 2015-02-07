package se.bitcraze.crazyflie.lib.crazyradio;

public interface LinkListener {

    public void linkQualityUpdated(int percent);

    public void linkError(String msg);

}
