package se.bitcraze.crazyflie.lib.crazyradio;

public class RadioAck {
    private boolean ack = false;
    private boolean powerDet = false;
    private int retry = 0;
    private byte[] data;

    public boolean isAck() {
        return ack;
    }
    public void setAck(boolean ack) {
        this.ack = ack;
    }
    public boolean isPowerDet() {
        return powerDet;
    }
    public void setPowerDet(boolean powerDet) {
        this.powerDet = powerDet;
    }
    public int getRetry() {
        return retry;
    }
    public void setRetry(int retry) {
        this.retry = retry;
    }
    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }
}