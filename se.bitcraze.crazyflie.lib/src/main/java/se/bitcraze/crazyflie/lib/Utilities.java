package se.bitcraze.crazyflie.lib;

public class Utilities {

    /**
     * Returns byte array as comma separated string
     * (for debugging purposes)
     *
     * @param data
     * @return
     */
    public static String getByteString(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            sb.append(b);
            sb.append(",");
        }
        return sb.toString();
    }

    public static String getHexString(byte... array) {
        StringBuffer sb = new StringBuffer();
        for (byte b : array) {
            sb.append(String.format("%02X", b));
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Strip bytes of the beginning of an array
     *
     * @param array
     * @param offset
     * @return
     */
    public static byte[] strip(byte[] array, int offset) {
        byte[] strippedArray = new byte[array.length-offset];
        System.arraycopy(array, offset, strippedArray, 0, strippedArray.length);
        return strippedArray;
    }
    
}
