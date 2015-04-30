package se.bitcraze.crazyflie.lib.crazyflie;

public interface LinkListener {

    public void linkQualityUpdated(int percent);

    public void linkError(String msg);

}
