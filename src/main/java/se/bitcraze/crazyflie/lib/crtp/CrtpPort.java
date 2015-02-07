package se.bitcraze.crazyflie.lib.crtp;


/**
 * Lists the available ports for the CRTP.
 *
 */
public enum CrtpPort {
    CONSOLE(0),
    PARAMETERS(2),
    COMMANDER(3),
    MEMORY(4),
    LOGGING(5),
    DEBUGDRIVER(14),
    LINKCTRL(15),
    ALL(255),
    UNKNOWN(-1); //FIXME

    private byte mNumber;

    private CrtpPort(int number) {
        this.mNumber = (byte) number;
    }

    /**
     * Get the number associated with this port.
     *
     * @return the number of the port
     */
    public byte getNumber() {
        return mNumber;
    }

    /**
     * Get the port with a specific number.
     *
     * @param number
     *            the number of the port.
     * @return the port or <code>null</code> if no port with the specified number exists.
     */
    public static CrtpPort getByNumber(byte number) {
        for (CrtpPort p : CrtpPort.values()) {
            if (p.getNumber() == number) {
                return p;
            }
        }
        return null;
    }
}
