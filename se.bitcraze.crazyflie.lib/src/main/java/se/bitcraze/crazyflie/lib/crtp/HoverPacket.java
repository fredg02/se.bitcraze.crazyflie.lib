package se.bitcraze.crazyflie.lib.crtp;

import java.nio.ByteBuffer;

/**
 * Hover packet (for FlowDeck)
 */

public class HoverPacket extends CrtpPacket {
    private final float mYx;
    private final float mYy;
    private final float mYawrate;
    private final float mZdistance;

    /**
     * Create a new commander packet.
     *
     * @param vx (m/s)
     * @param vy (m/s)
     * @param yaw (Deg./s)
     * @param zDistance (m)
     */
    public HoverPacket(float vx, float vy, float yaw, float zDistance) {
        super(0, CrtpPort.GENERIC_COMMANDER);

        this.mYx = vx;
        this.mYy = vy;
        this.mYawrate = yaw;
        this.mZdistance = zDistance;
    }

    @Override
    protected void serializeData(ByteBuffer buffer) {
        buffer.put((byte)0x05);
        buffer.putFloat(mYx);
        buffer.putFloat(-mYy); //invert axis
        buffer.putFloat(mYawrate);
        buffer.putFloat(mZdistance);
    }

    @Override
    protected int getDataByteCount() {
        return 4 * 4 + 1; // 4 floats with size 4, 1 byte (type)
    }

    @Override
    public String toString() {
        return "HoverPacket: yx: " + this.mYx + " yy: " + this.mYy + " yawrate: " + this.mYawrate + " zDistance: " + this.mZdistance;
    }
}
