package se.bitcraze.crazyflie.lib.crtp;

import java.nio.ByteBuffer;

/**
 * Control mode where the height is sent as an absolute
 * setpoint (intended to be the distance to the surface
 * under the Crazflie).
 */
public class HoverPacket extends CrtpPacket {
    private final float mVx;
    private final float mVy;
    private final float mYawrate;
    private final float mZdistance;

    /**
     * Create a new commander packet.
     *
     * @param vx (m/s)
     * @param vy (m/s)
     * @param yawrate (Deg./s)
     * @param zDistance (m)
     */
    public HoverPacket(float vx, float vy, float yawrate, float zDistance) {
        super(0, CrtpPort.COMMANDER_GENERIC);
        this.mVx = vx;
        this.mVy = vy;
        this.mYawrate = yawrate;
        this.mZdistance = zDistance;
    }

    @Override
    protected void serializeData(ByteBuffer buffer) {
        buffer.put((byte)0x05);
        buffer.putFloat(mVx);
        buffer.putFloat(-mVy); //invert axis
        buffer.putFloat(mYawrate);
        buffer.putFloat(mZdistance);
    }

    @Override
    protected int getDataByteCount() {
        return 1 + 4 * 4; // 1 byte (type), 4 floats with size 4
    }

    @Override
    public String toString() {
        return "HoverPacket: vx: " + this.mVx + " vy: " + this.mVy + " yawrate: " + this.mYawrate + " zDistance: " + this.mZdistance;
    }
}
