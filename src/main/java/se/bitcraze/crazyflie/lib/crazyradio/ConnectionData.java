package se.bitcraze.crazyflie.lib.crazyradio;

public class ConnectionData {
    private int mChannel;
    private int mDataRate;

    public ConnectionData(int channel, int dataRate) {
        this.mChannel = channel;
        this.mDataRate = dataRate;
    }

    public int getChannel() {
        return mChannel;
    }

    public int getDataRate() {
        return mDataRate;
    }

}