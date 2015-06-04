package se.bitcraze.crazyflie.lib.log;

import java.util.HashMap;
import java.util.Map;

import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.VariableType;

/**
 * An element in the Log TOC.
 *
 */
public class LogTocElement extends TocElement {

    /*
    types = {0x01: ("uint8_t",  '<B', 1),
             0x02: ("uint16_t", '<H', 2),
             0x03: ("uint32_t", '<L', 4),
             0x04: ("int8_t",   '<b', 1),
             0x05: ("int16_t",  '<h', 2),
             0x06: ("int32_t",  '<i', 4),
             0x08: ("FP16",     '<h', 2),
             0x07: ("float",    '<f', 4)}
    */

    public static final Map<Integer, VariableType> VARIABLE_TYPE_MAP;

    static {
        VARIABLE_TYPE_MAP = new HashMap<Integer, VariableType>(10);
        VARIABLE_TYPE_MAP.put(0x01, VariableType.UINT8_T);
        VARIABLE_TYPE_MAP.put(0x02, VariableType.UINT16_T);
        VARIABLE_TYPE_MAP.put(0x03, VariableType.UINT32_T);
        VARIABLE_TYPE_MAP.put(0x04, VariableType.INT8_T);
        VARIABLE_TYPE_MAP.put(0x05, VariableType.INT16_T);
        VARIABLE_TYPE_MAP.put(0x06, VariableType.INT32_T);
        /*TODO: 0x08 FP16*/
        VARIABLE_TYPE_MAP.put(0x07, VariableType.FLOAT);
    }

    // empty constructor is needed for (de)serialization
    public LogTocElement() {
    }

    /**
     * TocElement creator. Data is the binary payload of the element.
     */
    public LogTocElement(byte[] data) {
        if (data != null) {
            setGroupAndName(data);

            setIdent(data[0]);

            setCtype(VARIABLE_TYPE_MAP.get(data[1] & 0x0F));

            // setting pytype not needed in Java cf lib

            //TODO: self.access = ord(data[1]) & 0x10 ?!
            if ((data[1] & 0x40) != 0) {
                setAccess(RO_ACCESS);
            } else {
                setAccess(RW_ACCESS);
            }
        }
    }
    /*
        @staticmethod
        def get_id_from_cstring(name):
            """Return variable type id given the C-storage name"""
            for key in LogTocElement.types.keys():
                if (LogTocElement.types[key][0] == name):
                    return key
            raise KeyError("Type [%s] not found in LogTocElement.types!" % name)

        @staticmethod
        def get_cstring_from_id(ident):
            """Return the C-storage name given the variable type id"""
            try:
                return LogTocElement.types[ident][0]
            except KeyError:
                raise KeyError("Type [%d] not found in LogTocElement.types"
                               "!" % ident)

        @staticmethod
        def get_size_from_id(ident):
            """Return the size in bytes given the variable type id"""
            try:
                return LogTocElement.types[ident][2]
            except KeyError:
                raise KeyError("Type [%d] not found in LogTocElement.types"
                               "!" % ident)

        @staticmethod
        def get_unpack_string_from_id(ident):
            """Return the Python unpack string given the variable type id"""
            try:
                return LogTocElement.types[ident][1]
            except KeyError:
                raise KeyError("Type [%d] not found in LogTocElement.types"
                               "!" % ident)
    */

}
