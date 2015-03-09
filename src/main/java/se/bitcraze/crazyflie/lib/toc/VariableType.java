package se.bitcraze.crazyflie.lib.toc;

import java.nio.ByteBuffer;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;

public enum VariableType {
    UINT8_T,
    UINT16_T,
    UINT32_T,
    UINT64_T,
    INT8_T,
    INT16_T,
    INT32_T,
    INT64_T,
    FLOAT,
    DOUBLE;


    /**
     * Parse one variable of the given type.
     *
     * @param buffer the buffer to read raw data from
     * @return the parsed variable
     */
    public Number parse(ByteBuffer buffer) {
        ByteBuffer tempBuffer = ByteBuffer.allocate(4).order(CrtpPacket.BYTE_ORDER);
        tempBuffer.put(buffer.get());
        tempBuffer.put(buffer.get());
        if(this == UINT8_T || this == UINT16_T || this == UINT32_T || this == FLOAT) {
            tempBuffer.put(buffer.get());
            tempBuffer.put(buffer.get());
        }
        tempBuffer.rewind();
        switch (this) {
            case UINT8_T:
                return ((short) tempBuffer.get()) & 0xff;
            case UINT16_T:
                return ((int) tempBuffer.getShort()) & 0xffff;
            case UINT32_T:
                return ((long) tempBuffer.getInt()) & 0xffffffffL;
            case UINT64_T:
                System.out.println("UINT64_T not yet implemented");
                return -1;
            case INT8_T:
                return tempBuffer.get();
            case INT16_T:
                return tempBuffer.getShort();
            case INT32_T:
                return tempBuffer.getInt();
            case INT64_T:
                System.out.println("INT64_T not yet implemented");
                return -1;
            case FLOAT:
                return tempBuffer.getFloat();
            case DOUBLE:
                return tempBuffer.getDouble();
        }
        return -1;
    }

}