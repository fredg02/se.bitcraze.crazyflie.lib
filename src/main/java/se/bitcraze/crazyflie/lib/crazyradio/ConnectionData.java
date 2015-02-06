package se.bitcraze.crazyflie.lib.crazyradio;

class ConnectionData {
    private int mDataRate;
    private int mChannel;

    public ConnectionData(int dataRate, int channel) {
        this.mDataRate = dataRate;
        this.mChannel = channel;
    }

    public int getDataRate() {
        return mDataRate;
    }

    public int getChannel() {
        return mChannel;
    }
}