package se.bitcraze.crazyflie.lib.param;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.VariableType;


/**
 * An element in the Param TOC
 *
 */
public class ParamTocElement extends TocElement {

    /*
    types = {0x08: ("uint8_t",  '<B'),
             0x09: ("uint16_t", '<H'),
             0x0A: ("uint32_t", '<L'),
             0x0B: ("uint64_t", '<Q'),
             0x00: ("int8_t",   '<b'),
             0x01: ("int16_t",  '<h'),
             0x02: ("int32_t",  '<i'),
             0x03: ("int64_t",  '<q'),
             0x05: ("FP16",     ''),
             0x06: ("float",    '<f'),
             0x07: ("double",   '<d')}
    */

    public static final Map<Integer, VariableType> VARIABLE_TYPE_MAP;

    static {
        VARIABLE_TYPE_MAP = new HashMap<Integer, VariableType>(10);
        VARIABLE_TYPE_MAP.put(0x08, VariableType.UINT8_T);
        VARIABLE_TYPE_MAP.put(0x09, VariableType.UINT16_T);
        VARIABLE_TYPE_MAP.put(0x0A, VariableType.UINT32_T);
        VARIABLE_TYPE_MAP.put(0x0B, VariableType.UINT64_T);
        VARIABLE_TYPE_MAP.put(0x00, VariableType.INT8_T);
        VARIABLE_TYPE_MAP.put(0x01, VariableType.INT16_T);
        VARIABLE_TYPE_MAP.put(0x02, VariableType.INT32_T);
        VARIABLE_TYPE_MAP.put(0x03, VariableType.INT64_T);
        VARIABLE_TYPE_MAP.put(0x06, VariableType.FLOAT);
        VARIABLE_TYPE_MAP.put(0x07, VariableType.DOUBLE);
    }


    /**
     * TocElement creator. Data is the binary payload of the element.
     */
    //TODO: strip first byte of payload
    public ParamTocElement(byte[] payload) {
        if (payload != null) {
            setGroupAndName(payload);

            //self.ident = ord(data[0])
            setIdent(payload[1]);

            // self.ctype = self.types[ord(data[1]) & 0x0F][0]
            setCtype(VARIABLE_TYPE_MAP.get(payload[2] & 0x0F));
            // self.pytype = self.types[ord(data[1]) & 0x0F][1]
            // setPytype(mTypes[payload[1] & 0x0F]);

            /*
            if ((ord(data[1]) & 0x40) != 0):
                self.access = ParamTocElement.RO_ACCESS
            else:
                self.access = ParamTocElement.RW_ACCESS
            */
            if ((payload[2] & 0x40) != 0) {
                setAccess(RO_ACCESS);
            } else {
                setAccess(RW_ACCESS);
            }
        }
    }

    /*
    strs = struct.unpack("s" * len(data[2:]), data[2:])
    strs = ("{}" * len(strs)).format(*strs).split("\0")
    self.group = strs[0]
    self.name = strs[1]
     */
//    ByteBuffer buffer = ByteBuffer.wrap(payload, 2, payload.length - 2).order(CrtpPacket.BYTE_ORDER);
//    String temp = new String(buffer.array(), Charset.forName("US-ASCII"));
//    System.out.println("ParamTocElement: " + temp);
//    String[] split = temp.split("\0");
//    setGroup(split[0]);
//    setName(split[1]);
    //TODO: Make it simpler
    private void setGroupAndName(byte[] payload) {
        int offset = 3;
        int byteCount;
        // search end of first null terminated string
        for (byteCount = 0; byteCount + offset < payload.length && payload[byteCount + offset] != 0; byteCount++) {
        }
        final String groupName = new String(payload, offset, byteCount, Charset.forName("US-ASCII"));
        // offset of second null terminated string is last character of first + 1
        offset = offset + byteCount + 1;
        // search end of second null terminated string
        for (byteCount = 0; byteCount + offset < payload.length && payload[byteCount + offset] != 0; byteCount++) {
        }
        final String variableName = new String(payload, offset, byteCount, Charset.forName("US-ASCII"));

        setGroup(groupName);
        setName(variableName);
    }

    /*
    def get_readable_access(self):
        if (self.access == ParamTocElement.RO_ACCESS):
            return "RO"
        return "RW"
    */
    public String getReadableAccess() {
        return (getAccess() == RO_ACCESS) ? "RO" : "RW";
    }

    @Override
    public String toString() {
        return "ParamTocElement: " + this.getGroup() + "." + this.getName() + " (" + this.getIdent() + ", " + this.getCtype() + ", " + this.getReadableAccess() + ")";

    }
}
